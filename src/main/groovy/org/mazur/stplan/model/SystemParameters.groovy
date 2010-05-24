package org.mazur.stplan.model

class SystemParameters implements Serializable {

  private static final long serialVersionUID = 1L
  
  float processorSpeed = 1f
  
  float chanelSpeed = 1f
  
  int linksCount = 1
  
  private int duplex = 0
  
  void setDuplex(final int d) { this.duplex = d }
  void setDuplex(final boolean d) { this.duplex = d ? 1 : 0 }
  boolean getDuplex() { return duplex == 1 }
  
}
