/*
 * Created on Jul 19, 2005
 *
 * Copyright (c) 2005, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.layout3d.util;

import edu.uci.ics.jung.layout3d.model.Point;
import java.util.Date;
import java.util.Random;
import java.util.function.Function;

/**
 * Transforms the input type into a random location within the bounds of the Dimension property.
 * This is used as the backing Transformer for the LazyMap for many Layouts, and provides a random
 * location for unmapped vertex keys the first time they are accessed.
 *
 * @author Tom Nelson
 * @param <N>
 */
public class RandomLocationTransformer<N> implements Function<N, Point> {

  int width;
  int height;
  int depth;
  Random random;

  public RandomLocationTransformer(int width, int height, int depth) {
    this(width, height, depth, new Date().getTime());
  }

  public RandomLocationTransformer(int width, int height, int depth, long seed) {
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.random = new Random(seed);
  }

  //  private float random() {
  //    return (random.nextFloat() * 2 - 1) * (float) (d.getRadius());
  //  }

  //  public P animate(N v) {
  //    return new Point3f(random(), random(), random());
  //  }

  public Point apply(N node) {
    double radiusX = width / 2;
    double radiusY = height / 2;
    double radiusZ = depth / 2;
    return Point.of(
        random.nextDouble() * width - radiusX,
        random.nextDouble() * height - radiusY,
        random.nextDouble() * depth - radiusZ);
  }
}
