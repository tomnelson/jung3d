/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Dec 4, 2003
 */
package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.collect.Maps;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.util.Spherical;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code LayoutAlgorithm} implementation that positions nodes equally spaced on the surface of a
 * Sphere
 *
 * @author Tom Nelson
 */
public class SphereLayoutAlgorithm<N> implements Spherical, LayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(SphereLayoutAlgorithm.class);
  private double radius;

  private Map<Point, Integer> sphereLocations = Maps.newHashMap();

  @Override
  public void visit(LayoutModel<N> layoutModel) {

    float width = layoutModel.getWidth();
    float height = layoutModel.getHeight();
    float depth = layoutModel.getDepth();
    this.radius = Math.max(width, Math.max(height, depth));

    initialize(layoutModel);
  }

  public void initialize(LayoutModel<N> layoutModel) {

    if (layoutModel == null) {
      log.warn("layoutModel was null for initialization");
      return;
    }

    this.arrangeInSphere(layoutModel, this.radius / 4);
    sphereLocations.put(Point.ORIGIN, (int) this.radius / 4);
  }

  protected void arrangeInSphere(LayoutModel<N> layoutModel, double radius) {

    Collection<N> nodes = layoutModel.getGraph().nodes();
    Spherical.distribute(layoutModel, nodes, Point.ORIGIN, radius);
  }

  @Override
  public Map<Point, Integer> getSphereLocations() {
    return sphereLocations;
  }
}
