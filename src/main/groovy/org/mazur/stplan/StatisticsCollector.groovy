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

def genParametesrs = new GenerationParameteres(
  minVertexWeight : vertexWeightRange.from,
  maxVertexWeight : vertexWeightRange.to,
  n : n
)

def out = new FileWriter(output)

def serializeResult = { def result, long time, int gNumber, int maxTime ->
  def timeF = { x -> x.task.duration.end}
  
  int algTime = result.max { a,b -> a.task}
}

9.times {
  println "-----------------------------------------------"
  float k = (it + 1) * 0.1
  println "Generation for ${k}"
  6.times {
    int graphNumber = it + 1
    println "GRAPH ${graphNumber}"
    genParameters.k = k 
    def graphEditor = new TaskGraphEditor(name : "auto")
    new GraphGenerator(editor : graphEditor, parameters : genParameters).run()
    int totalGraphTime = ModelUtil.getTotalGraphTime(graphEditor.model, graphEditor.parent)
    println "Generation finished $totalGraphTime"
    
    combinations.each {
      State.INSTANCE.queueAlg = it[0]
      State.INSTANCE.selectProcessorAlg = it[1]
      println "Simulate for ${State.INSTANCE}"
      def simManager = new SimManager(simState : new SimState(
        freeProcessors : processors,
        tasksQueue : State.INSTANCE.queueAlg.formQueue(graphEditor.parent, graphEditor.model)                   
      ))
      long startTime = System.currentTimeMillis()
      def result = simManager.all()
      long time = System.currentTimeMillis() - startTime
      println "Time: $time"
      serializeResult(result, time, graphNumber, totalGraphTime)
    }
  }
}
