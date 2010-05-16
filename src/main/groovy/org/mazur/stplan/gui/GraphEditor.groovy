package org.mazur.stplan.gui

import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.BorderLayout;
import java.awt.Point;

import groovy.swing.SwingBuilder;

import javax.swing.JPanel;

import org.mazur.stplan.gui.graph.Graph;
import org.mazur.stplan.gui.graph.GraphComponent;
import org.mazur.stplan.model.Vertex;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxEventObject;

/**
 * Graph editor.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
abstract class GraphEditor {

  /** Swing builder. */
  protected final SwingBuilder swing = new SwingBuilder()
  
  /** Document name. */
  String name
  
  /** Graph instance. */
  protected Graph graph = new Graph(getStyleName(), this)
  /** Graph component. */
  protected GraphComponent gComponent = new GraphComponent(graph)
  
  /** Panels. */
  protected JPanel mainPanel, editPanel
  
  /** Undo manager. */
  private def undoManager = new mxUndoManager()
  
  /**
   * @return tool bar element
   */
  protected abstract def toolBar()
  
  /**
   * @return graph style name
   */
  protected abstract String getStyleName()
  
  /** Current creation state. */
  protected ElementType creationState
  
  protected void graphListener(final String event, final def worker) {
    graph.addListener event, new GraphEventListener(c : worker)
  }
  
  protected void initEditPanel() {
    if (editPanel) { return }
    def s = this.swing
    this.editPanel = s.panel(border : s.compoundBorder(s.emptyBorder(2), s.titledBorder("Редагування"))) {
      borderLayout()
    }
  }
  
  protected void installGraphComponent() {
    new mxRubberband(gComponent)
    new mxKeyboardHandler(gComponent)
    
    gComponent.getGraphControl().addMouseListener new CMouseListener(c : { MouseEvent e ->
      if (!creationState) { return }
      addElement creationState, e
      //creationState = null
    })
    def validation = { Object sender, mxEventObject evt -> validate() }
    [mxEvent.CELL_CONNECTED, mxEvent.CELLS_REMOVED, mxEvent.CELLS_ADDED].each {
      graphListener(it, validation)
    }
  }
  
  protected abstract void addElement(final ElementType type, final MouseEvent event) 
  
  /**
   * Connection done.
   */
  protected void validate() { }
  
  /**
   * @return new panel instance
   */
  protected JPanel createPanel() {
    println "Creating new editor panel"
    initEditPanel()
    installGraphComponent()
    def editPanel = this.editPanel
    return swing.panel() {
      borderLayout()
      widget(toolBar(), constraints : BorderLayout.NORTH)
      panel() {
        borderLayout()
        splitPane(
          rightComponent : widget(gComponent),
          leftComponent : panel() {
            borderLayout()
            widget(editPanel, constraints : BorderLayout.CENTER)
            widget(new mxGraphOutline(gComponent), preferredSize : [200, 200], constraints : BorderLayout.SOUTH)
          }
        )
      }
    }
  }
  
  /**
   * @return editor panel
   */
  public JPanel getMainPanel() { return mainPanel ? mainPanel : (mainPanel = createPanel()) }
  
  protected void log(def msg) {
    println(new Date().format("yyyy-MM-dd HH:mm:ss") + ": $msg")
  }
  
  public abstract boolean isError(final def cell)

  public void touch() {
    def p = graph.getDefaultParent()
    def n = graph.model.getChildCount(p)
    for (int i in 0..<n) {
      def v = graph.model.getChildAt(p, i)
      if (v instanceof Vertex) {
        gComponent.startEditingAtCell v // hack to change the style
        gComponent.stopEditing false
      }
    }
  }
  
  private Object[] modelToArray() {
    def p = graph.getDefaultParent()
    def n = graph.model.getChildCount(p)
    Object[] data = new Object[n]
    for (int i in 0..<n) {
      data[i] = graph.model.getChildAt(p, i)
    }
    return data
  }
  
  public void serialize(final OutputStream out) throws IOException {
    ObjectOutputStream oo = new ObjectOutputStream(out)
    oo.writeInt getType()
    oo.writeObject modelToArray()
  }
 
  public def getModel() { return graph.model }
  public Object getParent() { return graph.defaultParent } 
  
  protected int getType() {
    return this instanceof TaskGraphEditor ? 1 : 2
  }
  private static GraphEditor byType(final int type) {
    return type == 1 ? new TaskGraphEditor() : new SystemGraphEditor()
  }
  
  public static load(final InputStream input) {
    ObjectInputStream oi = new ObjectInputStream(input)
    int type = oi.readInt()
    GraphEditor e = byType(type)
    def data = oi.readObject()
    e.graph.addCells data
    e.validate()
    return e
  }
  
}

/**
 * Simple listener with a closure
 */
class CMouseListener extends MouseAdapter {
  def c
  void mouseClicked(final MouseEvent event) { c(event) }
}

/**
 * Simple listener with a closure
 */
class GraphEventListener implements mxIEventListener {
  def c
  void invoke(final Object sender, final mxEventObject evt) { c(sender, evt) }
}

