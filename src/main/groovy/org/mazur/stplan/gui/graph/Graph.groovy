package org.mazur.stplan.gui.graph

import org.mazur.stplan.model.NodeVertex;
import org.mazur.stplan.model.Vertex;
import org.mazur.stplan.gui.GraphEditor;
import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

/**
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class Graph extends mxGraph {

  private def editor
  
  Graph(final String styleName, def GraphEditor editor) {
    Document document = mxUtils.loadDocument(
        getClass().getResource("/resources/${styleName}-style.xml").toString()
    )
    new mxCodec().decode document.documentElement, this.stylesheet
    this.editor = editor
  }

  public void cellsAdded(Object[] cells, Object parent, Integer index,
      Object source, Object target, boolean absolute) {
    cells.each() { if (it instanceof Vertex) { it.editor = editor } }
    super.cellsAdded(cells, parent, index, source, target, absolute)
  }

  public Object[] cloneCells(final Object[] cells, final boolean allowInvalidEdges) {
    Object[] res = super.cloneCells(cells, allowInvalidEdges)
    res.each() { if (it instanceof Vertex) { it.editor = editor } }
    return res
  }
  
  public Object addEdge(Object edge, Object parent, Object source,
      Object target, Integer index) {
    if (edge instanceof mxCell) { 
      edge.style = "connection"
      if (!edge.value && !(source instanceof NodeVertex)) { edge.value = 0 }
    }
    return super.addEdge(edge, parent, source, target, index)
  }
  
}
