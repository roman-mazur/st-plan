package org.mazur.stplan.gui

import java.awt.BorderLayout;

import groovy.swing.SwingBuilder;

/**
 * Dialog for parameters edition.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class ParametersDialog {

  /** Model instance. */
  def model
  
  /** Title. */
  def title
  
  /** Listener. */
  private def listener
  
  /** GUI builder. */
  private def swing = new SwingBuilder()
  
  /** Dialog instance. */
  private def dialog
  
  private void construct() {
    if (!model) { throw new NullPointerException("model is not defined") }
    this.dialog = swing.dialog(
        pack  : true,
        title : this.title ? this.title : "Редагування параметрів"
    ) {
      def fieldsMap = [:]
      def fields = model.class.methods.name.grep(~/get.+/).collect { 
        it.substring(3, 4).toLowerCase() + it.substring(4) 
      }.findAll { String f ->
        ['Class', 'class', 'property'].inject(true) { v, e -> v &= !f.contains(e) }
      }
      borderLayout()
      panel() {
        gridLayout(cols : 2, rows : fields.size())
        fields.each { f ->
          label("$f: ")
          fieldsMap[f] = textField("${model[f] ? model[f] : ''}")
        }
      }
      button(constraints : BorderLayout.SOUTH, action : action(name : "OK", closure : {
        fields.each {
          def value = fieldsMap[it].text
          if (value) { model[it] = value as float }
        }
        dialog.visible = false
        listener(model)
      }))
    }
    GUIBuilder.locate this.dialog
  }
  
  public void show(def listener) {
    this.listener = listener
    if (!dialog) { construct() }
    dialog.visible = true
  }
  
}
