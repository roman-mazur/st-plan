package org.mazur.stplan.gui

import org.jfree.data.gantt.TaskSeries;

import org.mazur.stplan.State
import org.mazur.stplan.model.SystemParameters;
import org.mazur.stplan.sim.SimManager;
import org.mazur.stplan.sim.SimState;

import java.awt.BorderLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import groovy.swing.SwingBuilder;

/**
 * 
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class SimWindow {

  private IntervalCategoryDataset dataset = new TaskSeriesCollection()
  
  private def seriesList = [:]
  
  private JFreeChart chart
  
  private def tasksQueueMessage, pQueueMessage
  
  private SimManager simManager
  
  private SwingBuilder swing = new SwingBuilder()
  
  private def nextAction = swing.action(
    name : "Наступний крок",
    closure : {
      def data = simManager.next()
      if (!data) {
        GUIBuilder.locate swing.dialog(title : "Завершено", pack : true, visible : true) {
          label("OK: " + simManager.time)
        }
        return
      }
      addToSet data
    }
  )
  
  private def allAction = swing.action(
    name : "До кінця",
    closure : {
      def waitD = swing.dialog(title : "Моделювання", pack : true, visible : true) {
        borderLayout()
        label(constraints : BorderLayout.NORTH, text : "Зачекайте будь ласка...")
        progressBar(indeterminate : true)
      }
      GUIBuilder.locate waitD
      def worker = new Thread(new CRunnable({
        def result = simManager.all()
        GUIBuilder.invokeSwing {
          addToSet result
          waitD.visible = false
        }
      }))
      worker.start()
    }
  )

  private void addToSet(def data) {
    data?.each {
      TaskSeries ts = seriesList[it.type]
      if (!ts) { return }
      ts.add it.task
    }
  }
  
  public void init(final def taskM, final def processorM, final SystemParameters sysParams) {
    State.INSTANCE.sysParams = sysParams
    
    def tq = State.INSTANCE.queueAlg.formQueue(taskM[0], taskM[1])
    tasksQueueMessage = tq[1]
    def pq = State.INSTANCE.processorsAlg(processorM[0], processorM[1])
    pQueueMessage = pq[1]
                       
    simManager = new SimManager(simState : new SimState(
      freeProcessors : pq[0],
      tasksQueue : tq[0]                    
    ))

    (pq[0].sort {a,b-> a.value.name <=> b.value.name}).each {
      TaskSeries ts = new TaskSeries("$it.value.name")
      seriesList[it.value.name] = ts
      dataset.add ts
    }
    chart = ChartFactory.createGanttChart("Діаграма Ганта", "Вузол", "Час", dataset, true, true, false)
    
  }
  
  public void show() {
    GUIBuilder.locate swing.frame(title : "Моделювання", visible : true, pack : true) {
      borderLayout()
      panel(constraints : BorderLayout.NORTH) {
        hbox {
          button(action : nextAction)
          button(action : allAction)
        }
      }
      panel(constraints : BorderLayout.EAST) {
        vbox {
          label(text : "Черга задач")
          label(text : "<html>" + String.valueOf(tasksQueueMessage).replaceAll(/\n/, "<br/>") + "<br/></html>")
          label(text : "Приорітети процесорів")
          label(text : "<html>" + String.valueOf(pQueueMessage).replaceAll(/\n/, "<br/>") + "<br/></html>")
        }
      }
      panel {
        widget(new ChartPanel(chart))
      }
    }
  }
  
}
