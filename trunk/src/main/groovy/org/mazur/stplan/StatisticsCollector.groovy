package org.mazur.stplan

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.mazur.stplan.generation.GenerationParameteres;
import org.mazur.stplan.generation.GraphGenerator;
import org.mazur.stplan.gui.GraphEditor;
import org.mazur.stplan.gui.TaskGraphEditor;
import org.mazur.stplan.model.ModelUtil;
import org.mazur.stplan.sim.SimManager;
import org.mazur.stplan.sim.SimState;

def system = "c:/Users/MoRFey/Documents/mesh2d.g"

def nn = [45, 54, 63, 72, 81, 90]

def vertexWeightRange = 5..15

nn.each { n ->

println "============================================="
println "N = $n"

def output = "stats-${n}.txt"

def combinations = [
  [QueueAlg.CRITICAL_DOWN, SelectProcessorAlg.PRIORITY],
  [QueueAlg.READY, SelectProcessorAlg.PRIORITY],
  [QueueAlg.CRITICAL_UP, SelectProcessorAlg.PRIORITY],

  [QueueAlg.CRITICAL_DOWN, SelectProcessorAlg.NEAR_WITH_ALL],
  [QueueAlg.READY, SelectProcessorAlg.NEAR_WITH_ALL],
  [QueueAlg.CRITICAL_UP, SelectProcessorAlg.NEAR_WITH_ALL],
]

def systemEditor = GraphEditor.load(new FileInputStream(system))
def systemModel = [systemEditor.parent, systemEditor.model]
State.INSTANCE.sysParams = systemEditor.systemParameters
def processors = State.INSTANCE.processorsAlg(systemEditor.parent, systemEditor.model)[0]
int p = processors.size()                                                                                       
systemEditor = null                   

def genParameters = new GenerationParameteres(
  minVertexWeight : vertexWeightRange.from,
  maxVertexWeight : vertexWeightRange.to,
  n : n
)

def out = new FileWriter(output)

out << "k\t"
out << "Задача\t"
combinations.eachWithIndex { c, i -> out << "$i\t\t\t\t" }
out << "\r\n"

NumberFormat nf = NumberFormat.getNumberInstance()
nf.setMaximumFractionDigits 5

def serializeResult = { def result, long time, int gNumber, int maxTime, float k, int algNumber, int criticalPath ->
  def timeF = { x -> x.task.duration.end.time }
  int algTime = timeF(result.max { a, b -> timeF(a) <=> timeF(b) })
  float ka = maxTime / algTime
  float kes = ka / p
  float kea = criticalPath / algTime
  if (!algNumber) { out << "${nf.format(k)}\t$gNumber\t" }
  out << "${nf.format(ka)}\t${nf.format(kes)}\t${nf.format(kea)}\t$time\t"
  if (algNumber == combinations.size() - 1) { out << "\r\n" }
  out.flush()
}

9.times {
  println "-----------------------------------------------"
  float k = (it + 1) * 0.1
  println "Generation for ${k}"
  10.times {
    int graphNumber = it + 1
    println "GRAPH ${graphNumber}"
    genParameters.k = k 
    def graphEditor = new TaskGraphEditor(name : "auto")
    new GraphGenerator(editor : graphEditor, parameters : genParameters).run()
    ModelUtil mu = new ModelUtil()
    int totalGraphTime = mu.getTotalGraphTime(graphEditor.parent, graphEditor.model)
    int criticalPath = mu.criticalTaskPaths(graphEditor.parent, graphEditor.model, true, true).values().max()
    println "Generation finished $totalGraphTime"
    
    try {
      combinations.eachWithIndex { c, algNumber ->
        State.INSTANCE.queueAlg = c[0]
        State.INSTANCE.selectProcessorAlg = c[1]
        println "Simulate for ${State.INSTANCE}"
        def simManager = new SimManager(simState : new SimState(
          freeProcessors : processors,
          tasksQueue : State.INSTANCE.queueAlg.formQueue(graphEditor.parent, graphEditor.model)[0]                   
        ))
        long startTime = System.currentTimeMillis()
        def result = simManager.all()
        long time = System.currentTimeMillis() - startTime
        println "Time: $time"
        serializeResult(result, time, graphNumber, totalGraphTime, k, algNumber, criticalPath)
      }
    } catch (def e) {
      graphEditor.serialize new FileOutputStream("errorGrpah.g")
      throw e
    }
  }
}

out.close()

}