package org.mazur.stplan.sim

import org.mazur.stplan.State
import org.mazur.stplan.model.TaskVertex;

/**
 * 
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class TimeLine {

  /** Current time. */
  int time = 0
  
  /** Time line. */
  private TreeSet<TaskWrapper> line = new TreeSet<TaskWrapper>({ TaskWrapper t1, TaskWrapper t2 ->
    int res = t1.finishTime <=> t2.finishTime
    if (!res) { return t1.task.value.name <=> t2.task.value.name }
    return res
  } as Comparator<TaskWrapper>) 
  
  def addTask(final int time, final TaskVertex t) {
    def res = new TaskWrapper(startTime : time, task : t)
    line.add res
    return res
  }
  
  def getCompletedTasksAndChangeTime() {
    if (!line) { return Collections.emptyList() }
    def result = []
    TaskWrapper t = line.pollFirst()
    result += t
    time = t.finishTime
    if (!line) { return result }
    TaskWrapper nextT = line.first()
    while (nextT?.finishTime == t.finishTime) {
      result += nextT
      line.pollFirst()
      nextT = line ? line.first() : null
    }
    return result
  }
  
  String toString() { "Timeline/$time -> $line" }
  
  int getProcessorReleaseTimeDelay(def p, def pSelector) {
    return ((line.findAll { pSelector(it.task) == p }).max { a, b -> a.finishTime <=> b.finishTime }).finishTime - time
  }
  
}

class TaskWrapper {
  int startTime
  TaskVertex task
  
  int getFinishTime() {
    float k = State.INSTANCE.sysParams.processorSpeed
    return startTime + task.value.volume * k
  }

  String toString() { return "$finishTime: $task" }
}


