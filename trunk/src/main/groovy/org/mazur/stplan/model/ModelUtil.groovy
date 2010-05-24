package org.mazur.stplan.model

/**
 * Model util. Not thread safe.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
class ModelUtil {

  private def visited = new HashSet<Object>() 
  
  private Map<Vertex, Integer> paths = new HashMap<Vertex, Integer>()  

  private int calcVertex(final TaskVertex v, final boolean volumeMode, final boolean toEnd) {
    int l = paths[v] 
    if (l) { return l }
    l = (toEnd ? v.outputs() : v.inputs()).inject(0) { int maxPath, def edge -> 
      int current = calcVertex(toEnd ? edge.target : edge.source, volumeMode, toEnd)
      current > maxPath ? current : maxPath
    }
    l += (volumeMode ? v.value.volume : 1)
    paths[v] = l
    return l
  }
  
  public Map<Vertex, Integer> criticalTaskPaths(final Object parent, final def model, final boolean volumeMode, final boolean toEnd) {
    paths.clear()
    int n = model.getChildCount(parent)
    for (int i in 0..<n) {
      def v = model.getChildAt(parent, i)
      if (!(v instanceof TaskVertex)) { continue }
      calcVertex(v, volumeMode, toEnd)
    }
    return paths                               
  }
  
  public List<TaskVertex> getReadyVertexes(final Object parent, final def model) {
    int n = model.getChildCount(parent)
    List<TaskVertex> result = []
    for (int i in 0..<n) {
      def v = model.getChildAt(parent, i)
      if (!(v instanceof TaskVertex)) { continue }
      if (v.inputs().empty) { result += v }
    }
    return result
  }
  
  public List<TaskVertex> getAllVertexes(final Object parent, final def model) {
    int n = model.getChildCount(parent)
    List<TaskVertex> result = []
    for (int i in 0..<n) {
      def v = model.getChildAt(parent, i)
      if (!(v instanceof TaskVertex)) { continue }
      result += v
    }
    return result
  }
  
  public List<NodeVertex> getProcessors(final Object parent, final def model) {
    int n = model.getChildCount(parent)
    List<TaskVertex> result = []
    for (int i in 0..<n) {
      def v = model.getChildAt(parent, i)
      if (v instanceof NodeVertex) { result += v }
    }
    return result.sort { b, a -> a.connections().size() <=> b.connections().size() }
  }
  
}
