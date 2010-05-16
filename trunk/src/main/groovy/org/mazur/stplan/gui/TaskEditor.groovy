package org.mazur.stplan.gui

import java.awt.BorderLayout;

import groovy.swing.SwingBuilder;

import java.util.EventObject;

import javax.swing.JComponent;

import com.mxgraph.swing.view.mxCellEditor;
import com.mxgraph.swing.view.mxICellEditor;

/**
 * Task vertexes editor.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class TaskEditor implements mxICellEditor {

  /** Container. */
  private JComponent container

  private def currentCell
  
  private EventObject trigger
 
  private SwingBuilder swing = new SwingBuilder()
  
  private def nameField, volumeField
  
  private def panel = swing.panel() {
    gridLayout(cols: 1, rows: 4)
    label("Ім'я")
    nameField = textField()
    label("Об'єм")
    volumeField = textField()
  }
  
  public TaskEditor(final JComponent container) {
    this.container = container
  }
  
  /**
   * Returns the cell that is currently being edited.
   */
  public Object getEditingCell() { return currentCell }

  /**
   * Starts editing the given cell.
   */
  public void startEditing(Object cell, EventObject trigger) {
    this.currentCell = cell
    this.trigger = trigger
    println "ok"
    this.container.add BorderLayout.CENTER, panel
  }

  /**
   * Stops the current editing.
   */
  public void stopEditing(boolean cancel) {
    this.currentCell = null
    this.trigger = null
    this.container.remove panel
  }
  
}
