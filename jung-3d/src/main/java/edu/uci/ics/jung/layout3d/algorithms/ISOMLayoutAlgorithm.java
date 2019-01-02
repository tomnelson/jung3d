/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.util.NetworkNodeAccessor;
import edu.uci.ics.jung.layout3d.util.RadiusNetworkNodeAccessor;
import edu.uci.ics.jung.layout3d.util.RandomLocationTransformer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a self-organizing map layout algorithm, based on Meyer's self-organizing graph
 * methods.
 *
 * @author Yan Biao Boey
 * @author Tom Nelson adapted to 3D
 */
public class ISOMLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(ISOMLayoutAlgorithm.class);

  public static class Builder<N, T extends ISOMLayoutAlgorithm<N>, B extends Builder<N, T, B>>
          extends AbstractIterativeLayoutAlgorithm.Builder<N, T, B> {

    public T build() {
      return (T) new ISOMLayoutAlgorithm<>(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  protected ISOMLayoutAlgorithm(Builder builder) {
    super(builder);
  }

  protected LoadingCache<N, ISOMNodeData> isomNodeData =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, ISOMNodeData>() {
                public ISOMNodeData load(N node) {
                  return new ISOMNodeData();
                }
              });

  private int maxEpoch;
  private int epoch;

  private int radiusConstantTime;
  private int radius;
  private int minRadius;

  private double adaption;
  private double initialAdaption;
  private double minAdaption;

  private float width;
  private float height;
  private float depth;
  private float xMax;
  private float xMin;
  private float yMax;
  private float yMin;
  private float zMax;
  private float zMin;
  private float diameter;

  protected NetworkNodeAccessor<N>
      elementAccessor; // = new RadiusGraphElementAccessor<N, P>(domainModel);

  private double coolingFactor;

  private List<N> queue = new ArrayList<>();
  private String status = null;

  /** Returns the current number of epochs and execution status, as a string. */
  public String getStatus() {
    return status;
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    log.trace("visiting " + layoutModel);
    layoutModel.setInitializer(new RandomLocationTransformer<N>(600, 600, 600));

    this.width = layoutModel.getWidth();
    this.height = layoutModel.getHeight();
    this.depth = layoutModel.getDepth();
    this.diameter = Math.max(width, Math.max(height, depth));
    this.xMax = width / 2;
    this.xMin = -this.xMax;
    this.yMax = height / 2;
    this.yMin = -this.yMax;
    this.zMax = depth / 2;
    this.zMin = -this.zMax;

    super.visit(layoutModel);
    elementAccessor = new RadiusNetworkNodeAccessor<N>(layoutModel.getGraph(), layoutModel);
    initialize();
  }

  public void initialize() {

    layoutModel.setInitializer(new RandomLocationTransformer<N>(300, 300, 300));
    maxEpoch = 2000;
    epoch = 1;

    radiusConstantTime = 100;
    radius = 5;
    minRadius = 1;

    initialAdaption = 90.0D / 100.0D;
    adaption = initialAdaption;
    minAdaption = 0;

    //factor = 0; //Will be set later on
    coolingFactor = 2;

    //temperature = 0.03;
    //initialJumpRadius = 100;
    //jumpRadius = initialJumpRadius;

    //delay = 100;
  }

  /** Advances the current positions of the graph elements. */
  public void step() {

    status = "epoch: " + epoch + "; ";
    if (epoch < maxEpoch) {
      adjust();
      updateParameters();
      status += " status: running";

    } else {
      status += "adaption: " + adaption + "; ";
      status += "status: done";
      //			done = true;
    }
  }

  private synchronized void adjust() {

    //Generate random position in graph space
    Point tempXYD = Point.ORIGIN;

    float radius = diameter / 2;

    tempXYD =
        Point.of(
            (10 + Math.random() * diameter) - radius,
            (10 + Math.random() * diameter) - radius,
            (10 + Math.random() * diameter) - radius);

    //Get closest vertex to random position
    N winner = elementAccessor.getNode(tempXYD.x, tempXYD.y, tempXYD.z);

    while (true) {
      try {
        for (N v : layoutModel.getGraph().nodes()) {
          ISOMNodeData ivd = getISOMNodeData(v);
          ivd.distance = 0;
          ivd.visited = false;
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    adjustNode(winner, tempXYD);
  }

  private synchronized void updateParameters() {
    epoch++;
    double factor = Math.exp(-1 * coolingFactor * (1.0 * epoch / maxEpoch));
    adaption = Math.max(minAdaption, factor * initialAdaption);
    //jumpRadius = (int) factor * jumpRadius;
    //temperature = factor * temperature;
    if ((radius > minRadius) && (epoch % radiusConstantTime == 0)) {
      radius--;
    }
  }

  private synchronized void adjustNode(N v, Point tempXYD) {

    Graph<N> graph = layoutModel.getGraph();

    queue.clear();
    ISOMNodeData ivd = getISOMNodeData(v);
    ivd.distance = 0;
    ivd.visited = true;
    queue.add(v);
    N current;

    while (!queue.isEmpty()) {
      current = queue.remove(0);
      ISOMNodeData currData = getISOMNodeData(current);
      Point currXYData = layoutModel.apply(current);

      double dx = tempXYD.x - currXYData.x;
      double dy = tempXYD.y - currXYData.y;
      double dz = tempXYD.z - currXYData.z;
      double factor = adaption / Math.pow(2, currData.distance);

      currXYData = currXYData.add(factor * dx, factor * dy, factor * dz);

      layoutModel.set(current, currXYData);

      if (currData.distance < radius) {
        Collection<N> s = graph.adjacentNodes(current);
        while (true) {
          try {
            for (N child : s) {
              ISOMNodeData childData = getISOMNodeData(child);
              if (childData != null && !childData.visited) {
                childData.visited = true;
                childData.distance = currData.distance + 1;
                queue.add(child);
              }
            }
            break;
          } catch (ConcurrentModificationException cme) {
          }
        }
      }
    }
  }

  protected ISOMNodeData getISOMNodeData(N node) {
    return isomNodeData.getUnchecked(node);
  }

  /**
   * This one is an incremental visualization.
   *
   * @return <code>true</code> is the layout algorithm is incremental, <code>false</code> otherwise
   */
  public boolean isIncremental() {
    return true;
  }

  /**
   * For now, we pretend it never finishes.
   *
   * @return <code>true</code> is the increments are done, <code>false</code> otherwise
   */
  public boolean done() {
    if (epoch >= maxEpoch) {
      log.debug("is done");
    }
    return epoch >= maxEpoch;
  }

  protected static class ISOMNodeData {
    int distance;
    boolean visited;

    protected ISOMNodeData() {
      distance = 0;
      visited = false;
    }
  }

  public void reset() {
    epoch = 0;
  }
}
