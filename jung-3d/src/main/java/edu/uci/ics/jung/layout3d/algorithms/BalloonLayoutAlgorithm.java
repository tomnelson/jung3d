/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.util.TreeUtils;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.util.Spherical;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that assigns positions to {@code Tree} or {@code Network} nodes
 * using associations with nested circles ("balloons"). A balloon is nested inside another balloon
 * if the first balloon's subtree is a subtree of the second balloon's subtree.
 *
 * @author Tom Nelson
 */
public class BalloonLayoutAlgorithm<N> implements Spherical, LayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(BalloonLayoutAlgorithm.class);

  private Map<Point, Integer> sphereLocations = new HashMap<>();

  private LayoutModel<N> layoutModel;

  int depth = 200;

  private Graph<N> tree;
  /**
   * Creates an instance based on the input Network.
   *
   * @param
   */
  public BalloonLayoutAlgorithm() {}

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    this.layoutModel = layoutModel;
    if (log.isTraceEnabled()) {
      log.trace("visit {}", layoutModel);
    }

    this.tree = layoutModel.getGraph();
    Set<N> roots = TreeUtils.roots(layoutModel.getGraph());

    if (roots.size() == 1) {
      layoutModel.set(roots.iterator().next(), Point.ORIGIN);
    }
    buildTrees(roots);
  }

  public Map<Point, Integer> getSphereLocations() {
    return sphereLocations;
  }

  protected void breadth(N root) {
    List<N> queue = new ArrayList<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      N n = queue.remove(0);
      depth *= .9;
      Point center = layoutModel.get(n);
      arrangeInSphere(tree.successors(n), center, depth);

      for (N kid : tree.successors(n)) {
        queue.add(kid);
      }
    }
  }

  public static <N> int getDepth(Graph<N> tree, N node) {
    int depth = 0;
    N kid = node;
    while (tree.predecessors(kid).size() > 0) {
      depth++;
      kid = tree.predecessors(kid).iterator().next();
    }
    return depth;
  }

  protected void buildTree(N root) {

    Collection<N> kids = tree.successors(root);
    if (kids.size() > 0) {
      Point center = layoutModel.get(root);
      int subDepth = depth / (1 + 2 * getDepth(tree, root));
      arrangeInSphere(kids, center, subDepth);
      buildTrees(kids);
    }
  }

  protected void buildTrees(Collection<N> roots) {
    for (N node : roots) {
      buildTree(node);
    }
  }

  protected void arrangeInSphere(Collection<N> nodes, Point center, double radius) {

    Spherical.distribute(layoutModel, nodes, center, radius);
    sphereLocations.put(center, (int) radius);
  }
}
