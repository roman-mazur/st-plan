package org.mazur.stplan.model

import org.mazur.stplan.gui.GraphEditor;
import org.mazur.stplan.gui.TaskGraphEditor;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * Base vertex.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
abstract class Vertex extends mxCell {

  private static final long serialVersionUID = 1
  
  /** Editor instance. */
  transient GraphEditor editor

  public Vertex(Object v, mxGeometry g, String s) {
    super(v, g, s)
  }
  
  public def outputs() {
    def v = this
    return this.edges.findAll { mxCell edge -> edge.source == v }
  }
  public def inputs() {
    def v = this
    return this.edges.findAll { mxCell edge -> edge.target == v }
  }
  
  public def connections() { return this.edges  }
  
  public boolean isStartVertex() {
    def v = this
    return !this.edges.any { mxCell edge -> edge.target == v }
  }
  public boolean isEndVertex() {
    def v = this
    return !this.edges.any { mxCell edge -> edge.source == v }
  }
  
}
