package edu.uci.ics.jung.layout3d.algorithms.repulsion;

import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout3d.algorithms.SpringLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.spatial.BarnesHutOctTree;
import edu.uci.ics.jung.layout3d.spatial.ForceObject;
import edu.uci.ics.jung.layout3d.spatial.Node;
import java.util.ConcurrentModificationException;
import java.util.Random;

/**
 * @author Tom Nelson
 * @param <N> the node type
 */
public class BarnesHutSpringRepulsion<N>
    extends StandardSpringRepulsion<
        N, BarnesHutSpringRepulsion<N>, BarnesHutSpringRepulsion.Builder<N>>
    implements BarnesHutRepulsion<
        N, BarnesHutSpringRepulsion<N>, BarnesHutSpringRepulsion.Builder<N>> {

  public static class Builder<N>
      extends StandardSpringRepulsion.Builder<N, BarnesHutSpringRepulsion<N>, Builder<N>>
      implements BarnesHutRepulsion.Builder<N, BarnesHutSpringRepulsion<N>, Builder<N>> {

    private double theta = Node.DEFAULT_THETA;
    private BarnesHutOctTree<N> tree; // = BarnesHutOctTree.<N>builder().build();

    public Builder<N> withLayoutModel(LayoutModel<N> layoutModel) {
      this.layoutModel = layoutModel;
      this.tree =
          BarnesHutOctTree.<N>builder()
              .withBounds(
                  -layoutModel.getWidth() / 2,
                  -layoutModel.getHeight() / 2,
                  -layoutModel.getDepth() / 2,
                  layoutModel.getWidth(),
                  layoutModel.getHeight(),
                  layoutModel.getDepth())
              .withTheta(theta)
              .build();
      return this;
    }

    public Builder<N> withTheta(double theta) {
      this.theta = theta;
      return this;
    }

    public Builder<N> withSpringNodeData(
        LoadingCache<N, SpringLayoutAlgorithm.SpringNodeData> springNodeData) {
      this.springNodeData = springNodeData;
      return this;
    }

    public Builder<N> setRepulsionRangeSquared(int repulsionRangeSquared) {
      this.repulsionRangeSquared = repulsionRangeSquared;
      return this;
    }

    @Override
    public Builder<N> withRandom(Random random) {
      this.random = random;
      return this;
    }

    public BarnesHutSpringRepulsion<N> build() {
      return new BarnesHutSpringRepulsion(this);
    }
  }

  protected double theta;
  protected BarnesHutOctTree<N> tree;

  public static Builder barnesHutBuilder() {
    return new Builder();
  }

  protected BarnesHutSpringRepulsion(Builder<N> builder) {
    super(builder);
    this.theta = builder.theta;
    this.tree = builder.tree;
  }

  public void step() {
    tree.rebuild(layoutModel.getLocations());
  }

  public void calculateRepulsion() {
    Graph<N> graph = layoutModel.getGraph();

    try {
      for (N node : graph.nodes()) {

        if (layoutModel.isLocked(node)) {
          continue;
        }

        SpringLayoutAlgorithm.SpringNodeData svd = springNodeData.getUnchecked(node);
        if (svd == null) {
          continue;
        }
        ForceObject<N> nodeForceObject =
            new ForceObject(node, layoutModel.apply(node)) {
              @Override
              protected void addForceFrom(ForceObject other) {

                if (other == null || node == other.getElement()) {
                  return;
                }
                Point p = this.p;
                Point p2 = other.p;
                if (p == null || p2 == null) {
                  return;
                }
                double vx = p.x - p2.x;
                double vy = p.y - p2.y;
                double vz = p.z - p2.z;
                double distanceSq = p.distanceSquared(p2);
                if (distanceSq == 0) {
                  f = f.add(random.nextDouble(), random.nextDouble(), random.nextDouble());
                } else if (distanceSq < repulsionRangeSquared) {
                  double factor = 1;
                  f =
                      f.add(
                          factor * vx / distanceSq,
                          factor * vy / distanceSq,
                          factor * vz / distanceSq);
                }
              }
            };
        tree.applyForcesTo(nodeForceObject);
        Point f = nodeForceObject.f;
        double dlen = f.x * f.x + f.y * f.y + f.z * f.z;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          svd.repulsiondx += f.x / dlen;
          svd.repulsiondy += f.y / dlen;
          svd.repulsiondz += f.z / dlen;
        }
      }
    } catch (ConcurrentModificationException cme) {
      calculateRepulsion();
    }
  }
}
