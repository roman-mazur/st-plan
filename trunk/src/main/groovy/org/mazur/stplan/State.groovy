package org.mazur.stplan

import org.mazur.stplan.model.ModelUtil;
import org.mazur.stplan.model.SystemParameters 
import org.mazur.stplan.model.TaskVertex

/**
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
enum State {
  /** State instance. */
  INSTANCE
  
  /** Current queue algorithm. */
  QueueAlg queueAlg = QueueAlg.CRITICAL_DOWN
  
  /** Select processor algorithm. */
  SelectProcessorAlg selectProcessorAlg = SelectProcessorAlg.PRIORITY
  
  def processorsAlg = { p, m ->
    ModelUtil mu = new ModelUtil()
    def result = mu.getProcessors(p, m)
    def message = new StringBuilder()
    result.each { message << "$it.value (${it.connections().size()})\n" }
    return [result, message.toString()]
  }
  
  SystemParameters sysParams
  
}

enum QueueAlg {
  CRITICAL_DOWN("За пронормованими критичними шляхами", { p, m ->
    ModelUtil mu = new ModelUtil()
    def map = new HashMap(mu.criticalTaskPaths(p, m, true, true))
    def mapL = new HashMap(mu.criticalTaskPaths(p, m, false, true))
    int maxPath = map.values().max(), maxPathL = mapL.values().max()
    def f = { v -> (map[v] / maxPath) + (mapL[v] / maxPathL) }
    def q = map.keySet().sort { v1, v2 -> f(v2) <=> f(v1) }
    def message = new StringBuilder()
    q.each { message << "$it -> ${map[it]}/${mapL[it]}/${f(it)}\n" }
    return [q, message.toString()]
  }),
  READY("За часом готовності", { p, m ->
    ModelUtil mu = new ModelUtil()
    def q = new ArrayList(mu.getAllVertexes(p, m))
    def message = new StringBuilder()
    q.each { message << "$it \n" }
    return [q, message]
  }),
  CRITICAL_UP("За критичним шляхом від початку графа", { p, m ->
    ModelUtil mu = new ModelUtil()
    def map = new HashMap(mu.criticalTaskPaths(p, m, false, false))
    def q = map.keySet().sort { v1, v2 ->
      int res = map[v1] <=> map[v2]
      res == 0 ? v2.value.volume <=> v1.value.volume : res
    }
    def message = new StringBuilder()
    q.each { message << "$it -> ${map[it]}/${it.value.volume}\n" }
    return [q, message]
  })
  
  def formQueue
  
  def caption
  
  private QueueAlg(String name, def alg) {
    this.caption = name
    this.formQueue = alg 
  }
}

enum SelectProcessorAlg {
  PRIORITY("За приорітетами ", { def params ->
    return [params.freeProcessors.max { a, b -> a.connections().size() <=> b.connections().size() }, 0]
  }),
  NEAR_WITH_ALL("Сусіднє призначення з у рахуванням усіх процесорів", { def params ->
    if (params.firstStep) { return PRIORITY.select(params) }
    def allP = []; allP += params.freeProcessors; allP += params.usedProcessors.keySet()
    def calculateForP = { p ->
      def upT = params.usedProcessors[p]
      int startTime = upT ? upT : 0
      int sendTime = (params.task.inputs().collect { [params.processorSelector(it.source), it.value as int] }).inject(0) { sum, value -> 
        sum += params.distanceSelector(p, value[0]) * value[1] 
      }
      println "$p -> $startTime, $sendTime"
      return [startTime, sendTime].max()
    }
    def result = allP.min { pa, pb -> calculateForP(pa) <=> calculateForP(pb) }
    return [result, params.usedProcessors[result]]
  })
  
  def caption
  def select
  
  private SelectProcessorAlg(def caption, def select) {
    this.caption = caption
    this.select = select
  }
}
