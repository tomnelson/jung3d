/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 *
 * Created on Apr 12, 2005
 */
package edu.uci.ics.jung.layout3d.util;

import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import java.util.ConcurrentModificationException;

/**
 * Simple implementation of PickSupport that returns the vertex or edge that is closest to the
 * specified location. This implementation provides the same picking options that were available in
 * previous versions of
 *
 * <p>No element will be returned that is farther away than the specified maximum distance.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class RadiusGraphElementAccessor<N> implements GraphElementAccessor<N> {

  protected double maxDistance;

  /** Creates an instance with an effectively infinite default maximum distance. */
  public RadiusGraphElementAccessor() {
    this(Math.sqrt(Double.MAX_VALUE - 1000));
  }

  /** Creates an instance with the specified default maximum distance. */
  public RadiusGraphElementAccessor(double maxDistance) {
    this.maxDistance = maxDistance;
  }

  /**
   * Gets the vertex nearest to the location of the (x,y) location selected, within a distance of
   * <tt>maxDistance</tt>. Iterates through all visible vertices and checks their distance from the
   * click. Override this method to provde a more efficient implementation.
   */
  public N getNode(LayoutModel<N> layoutModel, Point p) {
    return getNode(layoutModel, p, this.maxDistance);
  }

  /**
   * Gets the vertex nearest to the location of the (x,y) location selected, within a distance of
   * <tt>maxDistance</tt>. Iterates through all visible vertices and checks their distance from the
   * click. Override this method to provde a more efficient implementation.
   *
   * @param maxDistance temporarily overrides member maxDistance
   */
  public N getNode(LayoutModel<N> layoutModel, Point p, double maxDistance) {
    double minDistance = maxDistance * maxDistance;
    N closest = null;
    while (true) {
      try {
        for (N v : layoutModel.getGraph().nodes()) {

          Point p2 = layoutModel.apply(v);
          double dist = Math.sqrt(p.distanceSquared(p2));
          //                  p.distance(p2);
          if (dist < minDistance) {
            minDistance = dist;
            closest = v;
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }
}
