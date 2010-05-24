package org.mazur.stplan.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.mazur.stplan.model.NodeVertex;
import org.mazur.stplan.model.SystemParameters;

import com.mxgraph.model.mxGeometry;

/**
 * 
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
public class SystemGraphEditor extends GraphEditor {

  /** Error cells set. */
  private def errorCells = new HashSet<NodeVertex>(), processed = new HashSet<NodeVertex>() 

  private SystemParameters systemParameters = new SystemParameters()
  
  /** Action to set current creation state to creating a new task. */
  private def newProcessorElementAction = swing.action(
    name : "Вузол",
    shortDescription : "Новий вузол системи",
    closure : { creationState = ElementType.PROCESSOR }
  )
  /** Action to set current creation state to creating a new task. */
  private def cursorAction = swing.action(
    name : "Курсор",
    closure : { creationState = null }
  )
  
  /** Action to set current creation state to creating a new task. */
  private def paramsAction = swing.action(
    name : "Параметри",
    closure : { 
      new ParametersDialog(model : systemParameters, title : "Параметри системи").show {
        systemParameters = it
      }
    }
  )

  @Override
  protected void addElement(final ElementType type, final MouseEvent event) {
    switch (type) {
    case ElementType.PROCESSOR: addProcessor event; break
    }
  }

  @Override
  protected String getStyleName() { return "system" }

  @Override
  public boolean isError(final Object cell) {
//    if (!(cell instanceof NodeVertex)) { return false }
//    def p = graph.getDefaultParent()
//    def n = graph.model.getChildCount(p)
//    if (n <= 1) { return false }
//    return !(cell.edges && !cell.edges.empty)
    return errorCells.contains(cell)
  }

  @Override
  protected Object toolBar() {
    return swing.toolBar() {
      label(text : "Граф системи", border : emptyBorder(0, 20, 0, 20), foreground : Color.BLUE)
      button(action : cursorAction)
      button(action : newProcessorElementAction)
      button(action : paramsAction)
    }
  }
  
  private def fill(def root) {
    if (!root || processed.contains(root)) { return }
    processed += root
    println processed
    root.outputs().each() {
      fill(it.target)
      fill(it.source)
    }
  }
  
  protected void validate() { 
    errorCells.clear()
    processed.clear()
    def p = graph.getDefaultParent()
    def n = graph.model.getChildCount(p)
    if (n <= 1) {
      touch()
      return
    }
    def root
    for (int i in 0..<n) {
      root = graph.model.getChildAt(p, i)
      if (root instanceof NodeVertex) { break } 
    }
    println "root: " + root
    fill(root)
    println "processed $processed"
    for (int i in 0..<n) {
      def v = graph.model.getChildAt(p, i)
      if ((v instanceof NodeVertex) && !processed.contains(v)) { 
        errorCells += v 
      }
    }
    println "error $errorCells"
    touch()
  }

  public void addProcessor(final MouseEvent event) {
    graph.model.beginUpdate()
    def p = gComponent.getPointForEvent(event)
    def cell = graph.addCell(new NodeVertex(null, new mxGeometry((int)p.x, (int)p.y, 50, 50), this), graph.getDefaultParent())
    graph.model.endUpdate()
  }
  
  protected void saveData(final ObjectOutputStream out) throws IOException {
    super.saveData out
    out.writeObject systemParameters
  }
  
  protected def loadData(final ObjectInputStream oi) {
    super.loadData(oi)
    this.systemParameters = oi.readObject()
    return this
  }
}
