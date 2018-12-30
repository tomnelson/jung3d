package edu.uci.ics.jung.layout3d.algorithms;

import edu.uci.ics.jung.layout3d.model.LayoutModel;

/**
 * LayoutAlgorithm is a visitor to the LayoutModel. When it visits, it runs the algorithm to place
 * the graph nodes at locations.
 *
 * @author Tom Nelson.
 */
public interface LayoutAlgorithm<N> {

  /**
   * visit the passed layoutModel and set its locations
   *
   * @param layoutModel
   */
  void visit(LayoutModel<N> layoutModel);
}
