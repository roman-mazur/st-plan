package org.mazur.stplan.sim

import org.mazur.stplan.State

/**
 * Simulation state.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class SimState {

  /** Tasks queue. */
  def tasksQueue
  /** Completed tasks. */
  private def completedTasks = [:]
  
  /** Simultation time. */
  private int time = -1
  
  /** Collection of free processors. */
  def freeProcessors, usedProcessors = []

  private def routes = [:]                                        
                                        
  private void log(def what) { println "[$time]: $what" }

  void setFreeProcessors(def freeProcessors) { 
    this.freeProcessors = freeProcessors
    buildRouteTables freeProcessors
  }
  
  def nextTask() {
    def t = tasksQueue.find { 
      def inp = it.inputs() 
      inp.empty || inp.every { completedTasks[it.source] }
    }
    tasksQueue.remove(t)
    return t
  }
  
  int getTime() { return time }
  
  void complete(def task) { completedTasks[task] = true }
  
  void nextStep() {
    time++
    log "Next step. Free P: $freeProcessors. Tasks: $tasksQueue"
  }
  
  void useNode(final def p) {
    usedProcessors += p; freeProcessors -= p
  }
  
  void releaseNode(final def p) {
    freeProcessors += p; usedProcessors -= p
  }
  
  def selectProcessor(def task) {
    return State.INSTANCE.selectProcessorAlg.select(freeProcessors, task)
  }
  
  private int minNotVisited(final PathMetric[] row, final BitSet visited) {
    int n = visited.nextClearBit(0), minValue = row[n].d 
    int s = n
    for (int i in s..<row.length) {
      if (visited.get(i)) { continue; }
      if ((minValue > row[i].d && row[i].d >= 0) || minValue < 0) {
        minValue = row[i].d
        n = i
      }
    }
    return visited.get(n) ? -1 : n
  }

  private PathMetric[][] minDistances(final boolean[][] matrix) {
    int n = matrix.length 
    PathMetric[][] result = new PathMetric[n][n]
    for (int i in 0 .. n - 1) {
      for (int j in 0 .. n - 1) { 
        result[i][j] = new PathMetric(d : (i == j ? 0 : matrix[i][j] ? 1 : -1), nextVertex : j) 
      }
    }
    def sourceDistances = { int srcVertexIndex ->
      BitSet visited = new BitSet(n)
      PathMetric[] row = result[srcVertexIndex]
      int haveToFind = n - srcVertexIndex - 1
      row[srcVertexIndex].d = 0
      visited.set(srcVertexIndex)
      n.times() {
        if (!haveToFind) { return }
        // i - current vertex index
        int i = minNotVisited(row, visited)
        int d = row[i].d + 1
        matrix[i].eachWithIndex() { mv, index ->
          // if connected -> modify distance
          if (mv && i != index && !visited.get(index)) {
            PathMetric pm = row[index] 
            if (pm.d == -1 || pm.d > d) {
              pm.nextVertex = i
              pm.d = d 
            }   
          }
        }
        visited.set(i)
        if (i >= srcVertexIndex) { haveToFind-- }
      }
    }
    int k = n.div(2)
    if (n % 2) { k++; }
    
    Thread t1 = new CThread(closure : {
      k.times() { sourceDistances(it) }
    })
    Thread t2 = new CThread(closure : {
      (n - k).times() { sourceDistances(k + it) }
    })
    t1.start()
    t2.start()
    t1.join()
    t2.join()
    // Duplicate symmetric
    result.eachWithIndex() { row, int index ->
      result.length.times() {
        result[it][index] = row[it]
      }
    }
    return result
  }

  private void buildRouteTables(def processors) {
    def visited = new HashSet()
    boolean[][] matrix = new boolean[processors.size()][processors.size()] 
    processors.each {
      if (visited.contains(it)) { return }
      def stack = []
      stack.push it
      while (stack) {
        def s1 = stack.pop()
        if (visited.contains(s1)) { continue }
        visited += s1
        s1.connections().each() {
          def s2 = it.target
          int i1 = s1.value.number, i2 = s2.value.number
          matrix[i1][i2] = matrix[i2][i1] = true
          stack.push s2
        }
      }
    }
    
    // FIXME incorrect min distances!!!!
    PathMetric[][] routingTable = minDistances(matrix)
    routingTable.each {
      println it
    }
    
    for (int i in 0..<routingTable.length) {
      def source = processors.find { it.value.number == i }
      for (int j in 0..<routingTable[0].length) {
        if (i == j) { continue }
        def target = processors.find { it.value.number == j }
        def nextV = processors.find { it.value.number == routingTable[i][j].nextVertex }
        this.routes[new PathPair(source : source, target : target)] = nextV
      }
    }
    
  }
  
  public def getNextVertex(def source, def target) { return routes[new PathPair(source : source, target : target)] }
  
}

class PathPair { 
  def source, target
  
  int hashCode() { return source.hashCode() + target.hashCode() }
  
  boolean equals(final Object o) {
    return source.equals(o.source) && target.equals(o.target) 
  }
  
}

class PathMetric {
  int nextVertex
  int d
  String toString() { return "$d/$nextVertex" }
}

class CThread extends Thread {
  def closure
  void run() { closure() }
}

