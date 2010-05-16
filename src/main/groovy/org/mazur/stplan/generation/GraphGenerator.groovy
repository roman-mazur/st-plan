package org.mazur.stplan.generation

import org.mazur.stplan.gui.TaskGraphEditor;
import org.mazur.stplan.model.NodeVertex;
import org.mazur.stplan.model.TaskVertex;

import com.mxgraph.model.mxGeometry;

/**
 * Graph generator.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class GraphGenerator implements Runnable {
  
  /** Task graph editor. */
  TaskGraphEditor editor
  
  /** Parameters. */
  GenerationParameteres parameters
  
  private Random random = new Random()
  
  private boolean checkForCycles(final Vertex src, final Vertex dst) {
    if (src == dst) { return false }
    def visited = new HashSet<Vertex>()
    def stack = []
    stack.push dst
    while (stack) {
      def v = stack.pop()
      for (def t in v.outputs) {
        if (t.dst == src) { return false }
        stack.push t.dst
      }
    }
    return true
  }
  
  @Override void run() {
    int n = parameters.n
    float k = parameters.k
    int r0 = parameters.minVertexWeight
    int dr = parameters.maxVertexWeight - r0 + 1
    
    int s = 0 // sum of calculations
    def v = new ArrayList<Vertex>(n)
    n.times {
      int w = r0 + random.nextInt(dr)
      v += new Vertex(weight : w)
      s += w
    }
    
    /*
     * k = s / (s + t) => t = (s - k * s) / k = s * (1 - k) / k 
     */
    int t = s * (1 - k) / k // sum of transitions
    int baseR = n * (n - 1)
    def base = 4 * t / baseR
    println base + ' ' + t 
    
    if (t < 0) { return }
    
    while (t) {
      Vertex rndSrc = v[random.nextInt(n)], rndDst = v[random.nextInt(n)] 
      //println "try src $rndSrc, dst $rndDst"
      if (checkForCycles(rndSrc, rndDst)) {
        int w = t > 4 ? t >> 2 : 1
        if (w > 1) { w = random.nextInt(w) + 1 }
        t -= w
        Transition dt = rndSrc.outputs.find { it.dst == rndDst }
        if (!dt) {
          rndSrc.outputs += new Transition(weight : w, dst : rndDst)
        } else {
          dt.weight += w
        }
        //println v
      } else {
        //println "--no--"
      }
    }
    
    int y = 5
    def vMap = [:]
    while (v) {
      def currentList = new ArrayList(v)
      v.each { srcV ->
        srcV.outputs.each { currentList -= it.dst }
      }
      currentList.eachWithIndex { vertex, i ->
        int x = random.nextInt(300)
        TaskVertex tv = new TaskVertex(vertex.weight, new mxGeometry(x, y, 50, 50), editor)
        editor.addTask tv
        vMap[vertex] = tv
      }
      v -= currentList
      y += 70
    }
    
    vMap.each { vertex, tv ->
      vertex.outputs.each { dt ->
        def dst = vMap[dt.dst]
        editor.addEdge dt.weight, tv, dst               
      }
    }
    
    editor.touch()
  }
  
}

class Vertex {
  int weight
  def outputs = []
  String toString() { return "V<$weight/$outputs>" }
}

class Transition {
  int weight
  def dst
  String toString() { return "T$weight" }
}
