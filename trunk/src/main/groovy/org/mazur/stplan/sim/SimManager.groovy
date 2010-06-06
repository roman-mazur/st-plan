package org.mazur.stplan.sim

import org.jfree.data.gantt.Task;

/**
 * 
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class SimManager {
  
  SimState simState
  
  public int getTime() { return simState.time }
  
  private String taskName(final def p, final def task) { return p.value.name + "($task.value.name)" }
  
  private void log(def message) { simState.log(message) }
  
  private def resolvePath(def task, def targetProcessor) {
    def paths = [:]
    task.inputs().each {
      def sourceProcessor = simState.getTaskProcessor(it.source)
      paths[it.source] = simState.findPath(sourceProcessor, targetProcessor)
    }
    return paths
  }
  
  private def planTask(def task) {
    log "Plan $task"
    def selection = simState.selectProcessor(task) 
    def p = selection[0], delay = selection[1]
    log "Processor: $p"
    if (!p) { return null }
    
    def diagramParts = []
    
    // make transitions
    def messagePaths = resolvePath(task, p)
    log "Transitions: $messagePaths"
    int planTime = simState.time
    messagePaths.each { def sourceTask, def path ->
      if (path.size() < 2) { return }
      int pt = simState.getTaskFinishTime(sourceTask)
      int weight = (sourceTask.connections().find { it.target == task }).value as int
      log "Transitions from $sourceTask ($weight)"
      def source = path.remove(0)
      while (source != p) {
        def target = path.remove(0)
        log "  --> to $target"
        def rt = simState.resolveTransition(source, target, pt, weight)
        pt = rt[0]
        diagramParts += new TimeDescriptor(
            type : "$source.value.name",
            task : new Task("$sourceTask.value.name - $task.value.name ($target.value.name)", new Date(pt), new Date(pt + rt[1]))
        )
                      
        source = target
        pt += rt[1]
      }
      if (pt > planTime) { planTime = pt }
    }
    
    if (delay) { planTime += delay }
    def taskWrapper = simState.useNode(task, p, planTime)

    diagramParts += new TimeDescriptor(
        type : "$p.value.name", 
        task : new Task(taskName(p, task), new Date(taskWrapper.startTime), new Date(taskWrapper.finishTime))
    )
    return diagramParts
  }
  
  public def next() {
    simState.nextStep()
    def task = simState.nextTask()
    def out = []
    while (task) {
      def planResult = planTask(task)
      if (!planResult) { break }
      simState.confirmTaskPlanned task
      out += planResult
      task = simState.nextTask()
    }
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
