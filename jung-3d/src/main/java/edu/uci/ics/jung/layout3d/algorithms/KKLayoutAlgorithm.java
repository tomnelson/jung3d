/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.util.RandomLocationTransformer;
import java.util.ConcurrentModificationException;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapted to 3D from the JUNG KKLayoutAlgorithm
 *
 * @param <N> the node type
 * @author Tom Nelson - adapted to 3D
 */
public class KKLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(KKLayoutAlgorithm.class);

  private double EPSILON = 0.1d;

  private int currentIteration;
  private int maxIterations = 2000;
  private String status = "KKLayout";

  private float L; // the ideal length of an edge
  private float K = 1; // arbitrary const number
  private float[][] dm; // distance matrix

  private boolean adjustForGravity = true;
  private boolean exchangenodes = true;

  private N[] nodes;
  private Point[] xydata;

  /** Retrieves graph distances between nodes of the visible graph */
  protected BiFunction<N, N, Number> distance;

  /**
   * The diameter of the visible graph. In other words, the maximum over all pairs of nodes of the
   * length of the shortest path between a and bf the visible graph.
   */
  protected float diameter;

  /** A multiplicative factor which partly specifies the "preferred" length of an edge (L). */
  private float length_factor = 0.9f;

  /**
   * A multiplicative factor which specifies the fraction of the graph's diameter to be used as the
   * inter-node distance between disconnected nodes.
   */
  private float disconnected_multiplier = 0.5f;

  public static class Builder<N, T extends KKLayoutAlgorithm<N>, B extends Builder<N, T, B>>
      extends AbstractIterativeLayoutAlgorithm.Builder<N, T, B> {
    protected Distance<N> distance;
    protected int maxIterations = 2000;
    protected boolean adjustForGravity = true;
    protected boolean exchangeNodes = true;

    public B withDistance(Distance<N> distance) {
      this.distance = distance;
      return (B) this;
    }

    public B withMaxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
      return (B) this;
    }

    public B shouldAdjustForGravity(boolean adjustForGravity) {
      this.adjustForGravity = adjustForGravity;
      return (B) this;
    }

    public B shouldExchangeNodes(boolean exchangeNodes) {
      this.exchangeNodes = exchangeNodes;
      return (B) this;
    }

    public T build() {
      return (T) new KKLayoutAlgorithm(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  protected KKLayoutAlgorithm(Builder builder) {
    super(builder);
    this.distance = (x, y) -> builder.distance.getDistance(x, y);
    this.maxIterations = builder.maxIterations;
    this.adjustForGravity = builder.adjustForGravity;
    this.exchangenodes = builder.exchangeNodes;
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);

    Graph<N> graph = layoutModel.getGraph();
    if (graph != null) {
      Distance distance = new UnweightedShortestPath<N>(graph);
      this.distance = (x, y) -> distance.getDistance(x, y);
    }
    initialize();
  }

  /**
   * @param length_factor a multiplicative factor which partially specifies the preferred length of
   *     an edge
   */
  public void setLengthFactor(float length_factor) {
    this.length_factor = length_factor;
  }

  /**
   * // * @param disconnected_multiplier a multiplicative factor that specifies the fraction of the
   * graph's diameter to be used as the inter-node distance between disconnected nodes float public
   * void setDisconnectedDistanceMultiplier(double disconnected_multiplier) {
   * this.disconnected_multiplier = disconnected_multiplier; }
   *
   * <p>/** @return a string with information about the current status of the algorithm.
   */
  public String getStatus() {
    return status + layoutModel.getWidth() + " " + layoutModel.getHeight();
  }

  public void setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  /** @return true */
  public boolean isIncremental() {
    return true;
  }

  /** @return true if the current iteration has passed the maximum count. */
  public boolean done() {
    if (currentIteration > maxIterations) {
      log.info("is done");
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public void initialize() {
    currentIteration = 0;
    Graph<N> graph = layoutModel.getGraph();
    // KKLayoutAlgorithm will fail if all vertices start at the same location
    layoutModel.setInitializer(
        new RandomLocationTransformer<N>(
            layoutModel.getWidth(), layoutModel.getHeight(), graph.nodes().size()));
    if (graph != null && layoutModel != null) {

      float height = layoutModel.getHeight();
      float width = layoutModel.getWidth();
      float depth = layoutModel.getDepth();

      int n = graph.nodes().size();
      dm = new float[n][n];
      nodes = (N[]) graph.nodes().toArray();
      xydata = new Point[n];

      // assign IDs to all visible nodes
      while (true) {
        try {
          int index = 0;
          for (N node : graph.nodes()) {
            Point xyd = layoutModel.apply(node);
            nodes[index] = node;
            xydata[index] = xyd;
            index++;
          }
          break;
        } catch (ConcurrentModificationException cme) {
        }
      }

      diameter = (float) DistanceStatistics.<N>diameter(graph, distance, true);

      float L0 = Math.min(height, width);
      L = (L0 / diameter) * length_factor; // length_factor used to be hardcoded to 0.9
      //L = 0.75 * Math.sqrt(height * width / n);

      for (int i = 0; i < n - 1; i++) {
        for (int j = i + 1; j < n; j++) {
          Number d_ij = distance.apply(nodes[i], nodes[j]);
          log.trace("distance from " + i + " to " + j + " is " + d_ij);

          Number d_ji = distance.apply(nodes[j], nodes[i]);
          log.trace("distance from " + j + " to " + i + " is " + d_ji);

          float dist = diameter * disconnected_multiplier;
          log.trace("dist:" + dist);
          if (d_ij != null) {
            dist = Math.min(d_ij.floatValue(), dist);
          }
          if (d_ji != null) {
            dist = Math.min(d_ji.floatValue(), dist);
          }
          dm[i][j] = dm[j][i] = dist;
        }
      }
      if (log.isTraceEnabled()) {
        for (int i = 0; i < n - 1; i++) {
          for (int j = i + 1; j < n; j++) {
            System.err.print(dm[i][j] + " ");
          }
          System.err.println();
        }
      }
    }
  }

  public void step() {
    Graph<N> graph = layoutModel.getGraph();
    currentIteration++;
    double energy = calcEnergy();
    status =
        "Kamada-Kawai N="
            + graph.nodes().size()
            + "("
            + graph.nodes().size()
            + ")"
            + " IT: "
            + currentIteration
            + " E="
            + energy;

    int n = graph.nodes().size();
    if (n == 0) {
      return;
    }

    double maxDeltaM = 0;
    int pm = -1; // the node having max deltaM
    for (int i = 0; i < n; i++) {
      if (layoutModel.isLocked(nodes[i])) {
        continue;
      }
      float deltam = calcDeltaM(i);

      if (maxDeltaM < deltam) {
        maxDeltaM = deltam;
        pm = i;
      }
    }
    if (pm == -1) {
      return;
    }

    for (int i = 0; i < 100; i++) {
      float[] dxy = calcDeltaXY(pm);
      xydata[pm] = xydata[pm].add(dxy[0], dxy[1], dxy[2]);
      double deltam = calcDeltaM(pm);
      if (deltam < EPSILON) {
        break;
      }
    }

    if (adjustForGravity) {
      adjustForGravity();
    }

    if (exchangenodes && maxDeltaM < EPSILON) {
      energy = calcEnergy();
      for (int i = 0; i < n - 1; i++) {
        if (layoutModel.isLocked(nodes[i])) {
          continue;
        }
        for (int j = i + 1; j < n; j++) {
          if (layoutModel.isLocked(nodes[j])) {
            continue;
          }
          double xenergy = calcEnergyIfExchanged(i, j);
          if (energy > xenergy) {
            double sx = xydata[i].x;
            double sy = xydata[i].y;
            double sz = xydata[i].z;
            xydata[i] = Point.of(xydata[j].x, xydata[j].y, xydata[j].z);
            xydata[j] = Point.of(sx, sy, sz);
            return;
          }
        }
      }
    }
  }

  /** Shift all nodes so that the center of gravity is located at the center of the screen. */
  public void adjustForGravity() {
    float gx = 0;
    float gy = 0;
    float gz = 0;
    for (int i = 0; i < xydata.length; i++) {
      gx += xydata[i].x;
      gy += xydata[i].y;
      gz += xydata[i].z;
    }
    gx /= xydata.length;
    gy /= xydata.length;
    gz /= xydata.length;
    // move the center to the origin
    double diffx = 0 - gx;
    double diffy = 0 - gy;
    double diffz = 0 - gz;
    for (int i = 0; i < xydata.length; i++) {
      xydata[i] = xydata[i].add(diffx, diffy, diffz);
      layoutModel.set(nodes[i], xydata[i]);
      layoutModel.set(nodes[i], xydata[i]);
    }
  }

  public void setAdjustForGravity(boolean on) {
    adjustForGravity = on;
  }

  public boolean getAdjustForGravity() {
    return adjustForGravity;
  }

  /**
   * Enable or disable the local minimum escape technique by exchanging nodes.
   *
   * @param on iff the local minimum escape technique is to be enabled
   */
  public void setExchangenodes(boolean on) {
    exchangenodes = on;
  }

  public boolean getExchangenodes() {
    return exchangenodes;
  }

  /** Determines a step to new position of the node m. */
  private float[] calcDeltaXY(int m) {
    float dE_dxm = 0;
    float dE_dym = 0;
    float dE_dzm = 0;

    float d2E_d2xm = 0;
    float d2E_dxmdym = 0;
    float d2E_dymdxm = 0;
    float d2E_dzmdxm = 0;

    //    float d2E_dymdzm = 0;

    float d2E_d2ym = 0;
    //    float d2E_d2zm = 0;

    for (int i = 0; i < nodes.length; i++) {
      if (i != m) {

        float dist = dm[m][i];
        float l_mi = L * dist;
        float k_mi = K / (dist * dist);
        double dx = xydata[m].x - xydata[i].x;
        double dy = xydata[m].y - xydata[i].y;
        double dz = xydata[m].z - xydata[i].z;
        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float ddd = d * d * d;

        dE_dxm += k_mi * (1 - l_mi / d) * dx;
        dE_dym += k_mi * (1 - l_mi / d) * dy;
        dE_dzm += k_mi * (1 - l_mi / d) * dz;

        d2E_d2xm += k_mi * (1 - l_mi * dy * dy / ddd);
        d2E_dxmdym += k_mi * l_mi * dx * dy / ddd;
        d2E_d2ym += k_mi * (1 - l_mi * dz * dz / ddd);
        d2E_dzmdxm += k_mi * l_mi * dz * dx / ddd;
      }
    }

    d2E_dymdxm = d2E_dxmdym;

    float denomi = d2E_d2xm * d2E_d2ym - d2E_dxmdym * d2E_dymdxm;
    float deltaX = (d2E_dxmdym * dE_dym - d2E_d2ym * dE_dxm) / denomi;
    float deltaY = (d2E_dymdxm * dE_dxm - d2E_d2xm * dE_dym) / denomi;
    float deltaZ = (d2E_dzmdxm * dE_dxm - d2E_d2xm * dE_dzm) / denomi;

    return new float[] {deltaX, deltaY, deltaZ};
  }

  /** Calculates the gradient of energy function at the node m. */
  private float calcDeltaM(int m) {
    float dEdxm = 0;
    float dEdym = 0;
    float dEdzm = 0;
    for (int i = 0; i < nodes.length; i++) {
      if (i != m) {
        float dist = dm[m][i];
        float l_mi = L * dist;
        float k_mi = K / (dist * dist);

        double dx = xydata[m].x - xydata[i].x;
        double dy = xydata[m].y - xydata[i].y;
        double dz = xydata[m].z - xydata[i].z;
        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        float common = k_mi * (1 - l_mi / d);
        dEdxm += common * dx;
        dEdym += common * dy;
        dEdzm += common * dz;
      }
    }
    return (float) Math.sqrt(dEdxm * dEdxm + dEdym * dEdym + dEdzm * dEdzm);
  }

  /** Calculates the energy function E. */
  private float calcEnergy() {
    float energy = 0;
    for (int i = 0; i < nodes.length - 1; i++) {
      for (int j = i + 1; j < nodes.length; j++) {
        double dist = dm[i][j];
        double l_ij = L * dist;
        double k_ij = K / (dist * dist);
        double dx = xydata[i].x - xydata[j].x;
        double dy = xydata[i].y - xydata[j].y;
        double dz = xydata[i].z - xydata[j].z;

        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        energy += k_ij / 2 * (dx * dx + dy * dy + dz * dz + l_ij * l_ij - 2 * l_ij * d);
      }
    }
    return energy;
  }

  /** Calculates the energy function E as if positions of the specified nodes are exchanged. */
  private float calcEnergyIfExchanged(int p, int q) {
    if (p >= q) {
      throw new RuntimeException("p should be < q");
    }
    float energy = 0; // < 0
    for (int i = 0; i < nodes.length - 1; i++) {
      for (int j = i + 1; j < nodes.length; j++) {
        int ii = i;
        int jj = j;
        if (i == p) {
          ii = q;
        }
        if (j == q) {
          jj = p;
        }

        double dist = dm[i][j];
        double l_ij = L * dist;
        double k_ij = K / (dist * dist);
        double dx = xydata[ii].x - xydata[jj].x;
        double dy = xydata[ii].y - xydata[jj].y;
        double dz = xydata[ii].z - xydata[jj].z;

        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        energy += k_ij / 2 * (dx * dx + dy * dy + dz * dz + l_ij * l_ij - 2 * l_ij * d);
      }
    }
    return energy;
  }

  public void reset() {
    currentIteration = 0;
  }
}
