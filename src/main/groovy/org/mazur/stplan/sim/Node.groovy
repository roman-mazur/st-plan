package org.mazur.stplan.sim

import org.mazur.stplan.model.TaskVertex;

/**
 * System node.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class Node {

  /** Output queue of messages. */
  def outputQueue
  
}

class Message {
  TaskVertex tSource, tDestination
  Node nSource, nDestination
}
