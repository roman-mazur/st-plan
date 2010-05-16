package org.mazur.stplan.gui

import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import org.mazur.stplan.model.ModelUtil;
import org.mazur.stplan.model.TaskVertex;
import org.mazur.stplan.State
import com.mxgraph.model.mxGeometry;

/**
 * Task graph editor.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class TaskGraphEditor extends GraphEditor {

  /** Error cells set. */
  private def errorCells = new HashSet<TaskVertex>()
  
  /** Action to set current creation state to creating a new task. */
  private def newTaskElementAction = swing.action(
    name : "Задача",
    shortDescription : "Нова вершина графу задач",
    closure : { creationState = ElementType.TASK }
  )
  /** Action to set current creation state to creating a new task. */
  private def cursorAction = swing.action(
    name : "Курсор",
    closure : { creationState = null }
  )
  /** Action to show the queue of tasks. */
  private def queueAction = swing.action(
    name : "Черга",
    closure : { 
      if (!errorCells.empty) {
        JOptionPane.showMessageDialog gComponent, "Граф містить помилки", "Помилка", JOptionPane.ERROR_MESSAGE
        return
      }
      def p = graph.defaultParent
      def r = State.INSTANCE.queueAlg.formQueue(p, graph.model)
      JOptionPane.showMessageDialog gComponent, r[1], "Черга", JOptionPane.INFORMATION_MESSAGE
    }
  )
  
  
  protected void installGraphComponent() {
    initEditPanel()
    //gComponent.cellEditor = new TaskEditor(editPanel)
    super.installGraphComponent()
  }

  @Override
  protected def toolBar() {
    return swing.toolBar() {
      label(text : "Граф задачі", border : emptyBorder(0, 20, 0, 20), foreground : Color.BLUE)
      button(action : cursorAction)
      button(action : newTaskElementAction)
      button(action : queueAction)
    }
  }
  
  @Override
  protected String getStyleName() { return "task" }
 
  @Override
  protected void addElement(final ElementType type, final MouseEvent event) {
    switch (type) {
    case ElementType.TASK: addTask event
    }
  }
  
  private boolean analyzeTree(final TaskVertex root, final def processed, final LinkedList<TaskVertex> path) {
    if (!root) { return true }
    if (processed.contains(root)) { return false }
    processed += root
    path += root
    root.outputs().each() { 
      if (!analyzeTree(it.target, processed, path)) {
        Iterator<TaskVertex> i = path.descendingIterator()
        while (i.hasNext()) {
          def v = i.next()
          println "Error with $v"
          errorCells += v 
          if (v == it.target) { break }
        }
      } 
    }
    path.pop()
  }
  
  protected void validate() {
    log("Validation start")
    errorCells.clear()
    def processed = new HashSet<TaskVertex>()
    def p = graph.getDefaultParent()
    def n = graph.model.getChildCount(p)
    def root = null
    int index = 0
    while (index < n) {
      int k = index
      boolean found = false
      for (int i in k..<n) {
        root = graph.model.getChildAt(p, i)
        if ((root instanceof TaskVertex) && !processed.contains(root)) { index = i; found = true; break }
      }
      if (!found) { index = n; root = null }
      index++
      if (!root) { continue }
      analyzeTree root, processed, new LinkedList<TaskVertex>()
    }
    log("Validation end: $errorCells")
    touch()
  }
  
  public void addTask(final MouseEvent event) {
    def p = gComponent.getPointForEvent(event)
    addTask(new TaskVertex(null, new mxGeometry((int)p.x, (int)p.y, 50, 50), this))
  }
  
  public void addTask(final TaskVertex vertex) {
    graph.model.beginUpdate()
    def cell = graph.addCell(vertex, graph.getDefaultParent())
    graph.model.endUpdate()
  }
  
  public void addEdge(final int weight, final TaskVertex src, final TaskVertex dst) {
    graph.model.beginUpdate()
    graph.insertEdge(graph.getDefaultParent(), null, weight.toString(), src, dst)
    graph.model.endUpdate()
  }
  
  public boolean isError(final def cell) {
    return errorCells.contains(cell)
  }
  
}
