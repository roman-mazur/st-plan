package org.mazur.stplan.model

import org.mazur.stplan.gui.TaskGraphEditor;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;

/**
 * Task vertex.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class TaskVertex extends Vertex {

  private static final long serialVersionUID = 1
  
  private static int globalN = 0
  
  TaskVertex(final Object value, final mxGeometry geometry, final TaskGraphEditor editor) {
    super(value, geometry, "taskVertex")
    this.vertex = true
    this.connectable = true
    this.editor = editor
    this.value = new TaskData(name : "T${++globalN}", volume : value ? value as int : 0)
  }
  
  public String getStyle() {
    def v = this
    if (editor?.isError(v)) { return "errorTaskVertex" }
    return  v.isStartVertex() ? "startTaskVertex" : v.isEndVertex() ? "endTaskVertex" : "taskVertex"
  }
  
  @Override
  String toString() { return "TaskVertex[${value}]" }
  
  @Override
  void setValue(final Object value) {
    TaskData v = null
    if (value instanceof String) {
      def parts = value?.trim().split(/\s*\/\s*/)
      if (parts) {
        v = new TaskData(name : parts[0], volume : parts[1] as int)
      }
    } else if (value instanceof TaskData) {
      v = (TaskData)value
    }
    super.setValue v
  }
  
  @Override
  TaskData getValue() { return (TaskData)super.getValue() }
  
}

class TaskData implements Serializable {
  private static final long serialVersionUID = 1
  /** Vertex name. */
  String name
  /** Volume. */
  int volume
  
  String toString() { return "$name / $volume" }
}