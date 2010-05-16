package org.mazur.stplan.gui

import org.jfree.data.gantt.TaskSeries;

import org.mazur.stplan.State
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
  
  private TaskSeries calculationTasks = new TaskSeries("Обислення")
  
  private ArrayList<TaskSeries> trasmitionTasksList = new ArrayList<TaskSeries>() 
  
  private JFreeChart chart
  
  private def tasksQueueMessage, pQueueMessage
  
  private SimManager simManager
  
  private SwingBuilder swing = new SwingBuilder()
  
  private def nextAction = swing.action(
    name : "Наступний крок",
    closure : {
      addToSet simManager.next()
    }
  )
  
  private def allAction = swing.action(
    name : "До кінця",
    closure : {
      // TODO start a queue
    }
  )

  private void addToSet(def data) {
    data?.each {
      TaskSeries ts = null
      if ("calc" == it.type) { ts = calculationTasks }
      if (!ts) { return }
      ts.add it.task
    }
  }
  
  public void init(final def taskM, final def processorM) {
    dataset.add calculationTasks
    chart = ChartFactory.createGanttChart("Діаграма Ганта", "Вузол", "Час", dataset, true, true, false)
    
    def tq = State.INSTANCE.queueAlg.formQueue(taskM[0], taskM[1])
    tasksQueueMessage = tq[1]
    def pq = State.INSTANCE.processorsAlg(processorM[0], processorM[1])
    pQueueMessage = pq[1]
                       
    simManager = new SimManager(simState : new SimState(
      freeProcessors : pq[0],
      tasksQueue : tq[0]                    
    ))
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
