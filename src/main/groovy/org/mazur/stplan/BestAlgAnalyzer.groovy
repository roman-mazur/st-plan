package org.mazur.stplan

import org.mazur.stplan.generation.GraphGenerator;

import org.mazur.stplan.model.ModelUtil;
import org.mazur.stplan.sim.SimManager;
import org.mazur.stplan.sim.SimState;

import java.text.NumberFormat;
import org.mazur.stplan.generation.GenerationParameteres;
import org.mazur.stplan.gui.GraphEditor;
import org.mazur.stplan.gui.TaskGraphEditor;

def alg = [QueueAlg.CRITICAL_DOWN, SelectProcessorAlg.NEAR_WITH_ALL]

def system = "c:/Users/MoRFey/Documents/mesh2d.g"

def n = 36

def linksCount = [1, 2, 3, 4]

def vertexWeightRange = 5..15

println "Start"

def genParameters = new GenerationParameteres(
  minVertexWeight : vertexWeightRange.from,
  maxVertexWeight : vertexWeightRange.to,
  n : n
)

def systemEditor = GraphEditor.load(new FileInputStream(system))
def systemModel = [systemEditor.parent, systemEditor.model]
State.INSTANCE.sysParams = systemEditor.systemParameters
def processors = State.INSTANCE.processorsAlg(systemEditor.parent, systemEditor.model)[0]
int p = processors.size()                                                                                       
systemEditor = null                   

def output = "best.txt"

def out = new FileWriter(output)

out << "k\t"
out << "Задача\t"
linksCount.each { l -> out << "$l-hd\t\t\t$l-fd\t\t\t" }
out << "\r\n"

NumberFormat nf = NumberFormat.getNumberInstance()
nf.setMaximumFractionDigits 5

def serializeResult = { def result, int gNumber, int maxTime, float k, int algNumber, int criticalPath ->
  def timeF = { x -> x.task.duration.end.time }
  int algTime = timeF(result.max { a, b -> timeF(a) <=> timeF(b) })
  float ka = maxTime / algTime
  float kes = ka / p
  float kea = criticalPath / algTime
  if (!algNumber) { out << "${nf.format(k)}\t$gNumber\t" }
  out << "${nf.format(ka)}\t${nf.format(kes)}\t${nf.format(kea)}\t"
  if (algNumber == linksCount.size() * 2 - 1) { out << "\r\n" }
  out.flush()
}

State.INSTANCE.queueAlg = alg[0]
State.INSTANCE.selectProcessorAlg = alg[1]

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
    
    linksCount.eachWithIndex { l, i ->
      println "=========================="
      println "l = $l"
    
      State.INSTANCE.sysParams.linksCount = l
      
      [false, true].eachWithIndex { duplex, j ->
        State.INSTANCE.sysParams.duplex = duplex
        
        try {
          def simManager = new SimManager(simState : new SimState(
            freeProcessors : processors,
            tasksQueue : State.INSTANCE.queueAlg.formQueue(graphEditor.parent, graphEditor.model)[0]                   
          ))
          def result = simManager.all()
          serializeResult(result, graphNumber, totalGraphTime, k, i * 2 + j, criticalPath)
        } catch (def e) {
          graphEditor.serialize new FileOutputStream("errorGrpah.g")
          throw e
        }
      }
    }
  }
}

