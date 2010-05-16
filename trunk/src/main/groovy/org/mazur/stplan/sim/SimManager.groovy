package org.mazur.stplan.sim

import org.jfree.data.gantt.Task;

/**
 * 
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class SimManager {
  
  private def tasksMap = [:]
  
  SimState simState
  
  private String taskName(final def p) { return p.value.name }
  
  private void log(def message) { simState.log(message) }
  
  private def findPath(def source, def target) {
    def path = []
    def src = source
    while (src != target) {
      src = simState.getNextVertex(src, target)
      path += src
    }
    return path
  }
  
  private def resolvePath(def task, def targetProcessor) {
    def paths = [:]
    task.inputs().each {
      def sourceProcessor = tasksMap[it.source]
      paths[it.source] = findPath(sourceProcessor, targetProcessor)
    }
    return paths
  }
  
  private def planTask(def task) {
    log "Plan $task"
    def p = simState.selectProcessor(task)
    log "Processor: $p"
    if (!p) { return null }
    simState.useNode p
    tasksMap[task] = p
    
    // make transitions
    def messagePaths = resolvePath(task, p)
    log "Path: $messagePaths"
    
    return new TimeDescriptor(type : "calc", task : new Task(taskName(p), new Date(System.currentTimeMillis() - 1000000), new Date()))
  }
  
  public def next() {
    simState.nextStep()
    def task = simState.nextTask()
    def planned = []
    def out = []
    while (task) {
      def planResult = planTask(task)
      if (!planResult) { break }
      out += planResult
      planned += task
      task = simState.nextTask()
    }
    planned.each { simState.complete it }
    return out
  }
  
  public def all() {
    def result = []
    def stepResult = next()
    while (stepResult) {
      result += stepResult
      stepResult = next()
    }
    return result
  }

}

class TimeDescriptor {
  String type
  org.jfree.data.gantt.Task task
}
