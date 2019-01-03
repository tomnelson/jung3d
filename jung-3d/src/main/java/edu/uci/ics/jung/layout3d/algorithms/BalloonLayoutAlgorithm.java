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
      int subDepth = depth / (1 + getDepth(tree, root));
      arrangeInSphere(kids, center, subDepth);
      buildTrees(kids);
    }
  }

  protected void buildTrees(Collection<N> roots) {
    for (N node : roots) {
      buildTree(node);
    }
  }
//https://www.cmu.edu/biolphys/deserno/pdf/sphere_equi.pdf
  protected void distributeOnSphere(Collection<N> nodes, Point center, double radius) {
    int ncount = 0;
    Iterator<N> iterator = nodes.iterator();
    double a = 4*Math.PI*radius*radius / nodes.size();
    double d = Math.sqrt(a);
    double mtheta = Math.round(Math.PI / d);
    double dtheta = Math.PI / mtheta;
    double dphi = a / dtheta;
    for (int m=0; m<mtheta; m++) {
      double theta = Math.PI * (m  + 0.5) / mtheta;
      double mphi = Math.round(2*Math.PI*Math.sin(theta)/dphi);
      for (int n=0; n< mphi; n++) {
        double phi = 2 * Math.PI * n / mphi;
        Point p = Point.of(
                center.x + radius*Math.sin(theta)*Math.cos(phi),
                center.y + radius * Math.sin(theta)*Math.sin(phi),
                center.z + radius*Math.cos(theta)
        );
        layoutModel.set(iterator.next(), p);
      }
    }
  }

  protected void arrangeInSphere(Collection<N> nodes, Point center, double radius) {
    sphereLocations.put(center, (int) radius);

    int i = 0;
    double offset = 2.0 / nodes.size();
    int count = nodes.size();
    double rnd = 1.0;
    double increment = Math.PI * (3. - Math.sqrt(5.));
    double centerX = center.x;
    double centerY = center.y;
    double centerZ = center.z;

    for (N node : nodes) {

      double y = ((i * offset) - 1) + (offset / 2);
      double r = Math.sqrt(1 - Math.pow(y, 2));

      double phi = ((i + rnd) % count) * increment;

      double x = Math.cos(phi) * r;
      double z = Math.sin(phi) * r;

      x *= radius;
      y *= radius;
      z *= radius;
      x += centerX;
      y += centerY;
      z += centerZ;
      Point coord = Point.of(x, y, z);
      layoutModel.set(node, coord);
      log.debug("placed {} at {}", node, coord);
      i++;
      buildTree(node);
    }
  }
}
