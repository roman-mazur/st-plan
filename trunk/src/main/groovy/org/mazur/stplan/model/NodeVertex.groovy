package org.mazur.stplan.model

import org.mazur.stplan.gui.SystemGraphEditor;
import org.mazur.stplan.gui.TaskGraphEditor;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * System vertex.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class NodeVertex extends Vertex {

  private static final long serialVersionUID = 1
  
  private static int globalN = 0
  
  public NodeVertex(final Object v, final mxGeometry geometry, final SystemGraphEditor editor) {
    super(v, geometry, "pVertex")
    this.vertex = true
    this.connectable = true
    this.editor = editor
    int n = ++globalN
    this.value = new NodeData(name : "P$n", number : n - 1)
  }

  String getStyle() { 
    return editor?.isError(this) ? "errorPVertex" : "pVertex" 
  }

  String toString() { return "NodeVertex=$value" }

  public def outputs() {
    def v = this
    return this.edges
  }
  
  public void setValue(final Object value) {
    NodeData v = null
    if (value instanceof String) {
      v = new NodeData(name : value, number : Integer.parseInt(value.substring(1)) - 1)
    } else {
      v = (NodeData)value
    }
    super.setValue v
  }
  
}

class NodeData implements Serializable {
  private static final long serialVersionUID = 1
  /**Node name. */
  String name
  int number
  String toString() { return name }
}

