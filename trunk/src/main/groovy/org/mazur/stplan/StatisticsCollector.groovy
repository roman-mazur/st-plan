package org.mazur.stplan

import org.mazur.stplan.generation.GenerationParameteres;
import org.mazur.stplan.generation.GraphGenerator;
import org.mazur.stplan.gui.GraphEditor;
import org.mazur.stplan.gui.TaskGraphEditor;
import org.mazur.stplan.model.ModelUtil;
import org.mazur.stplan.sim.SimManager;
import org.mazur.stplan.sim.SimState;

def system = "c:/Users/MoRFey/Documents/mesh2d.g"

def n = 9

def vertexWeightRange = 5..15

def output = "stats.txt"

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
systemEditor = null                   

def genParameters = new GenerationParameteres(
  minVertexWeight : vertexWeightRange.from,
  maxVertexWeight : vertexWeightRange.to,
  n : n
)

def out = new FileWriter(output)

out << "k\t"
out << "Задача\t"
combinations.eachWithIndex { c, i -> out << "$i\t\t\t\t\t" }
out << "\r\n"

def serializeResult = { def result, long time, int gNumber, int maxTime, float k, int algNumber ->
  def timeF = { x -> x.task.duration.end.time }
  int algTime = timeF(result.max { a, b -> timeF(a) <=> timeF(b) })
  float ka = maxTime / algTime
  if (!algNumber) { out << "$k\t$gNumber\t" }
  out << "$ka\tn/a\tn/a\t$time\t"
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
    int totalGraphTime = new ModelUtil().getTotalGraphTime(graphEditor.parent, graphEditor.model)
    println "Generation finished $totalGraphTime"
    
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
      serializeResult(result, time, graphNumber, totalGraphTime, k, algNumber)
    }
  }
}

out.close()
