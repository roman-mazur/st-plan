package org.mazur.stplan.gui.graph

import java.awt.Color;
import java.awt.Point;

import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

/**
 * Custom graph component.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class GraphComponent extends mxGraphComponent {

  GraphComponent(final mxGraph graph) {
    super(graph)
//    this.pageVisible = true
    this.gridVisible = true
    setToolTips true
    this.connectionHandler.createTarget = true
    this.viewport.setOpaque true
    this.background = Color.WHITE
  }
  
  /**
   * Overrides drop behaviour to set the cell style if the target
   * is not a valid drop target and the cells are of the same
   * type (eg. both vertices or both edges). 
   */
  @Override
  Object[] importCells(final Object[] cells, final double dx, final double dy,
      final Object target, final Point location) {
    if (!target && cells.length == 1 && location) {
      target = getCellAt(location.x, location.y)
      if (target instanceof mxICell && cells[0] instanceof mxICell) {
        mxICell targetCell = (mxICell)target
        mxICell dropCell = (mxICell)cells[0]

        if (targetCell.isVertex() == dropCell.isVertex() || targetCell.isEdge() == dropCell.isEdge()) {
          mxIGraphModel model = graph.model
          model.setStyle(target, model.getStyle(cells[0]))
          graph.selectionCell = target
          return null
        }
      }
    }
    return super.importCells(cells, dx, dy, target, location)
  } 
}
