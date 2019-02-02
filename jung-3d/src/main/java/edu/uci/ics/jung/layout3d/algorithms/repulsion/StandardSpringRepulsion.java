package edu.uci.ics.jung.layout3d.algorithms.repulsion;

import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout3d.algorithms.SpringLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import java.util.ConcurrentModificationException;
import java.util.Random;

/**
 * @author Tom Nelson
 * @param <N> the node type
 */
public class StandardSpringRepulsion<
        N,
        R extends StandardSpringRepulsion<N, R, B>,
        B extends StandardSpringRepulsion.Builder<N, R, B>>
    implements StandardRepulsion<N, R, B> {

  public static class Builder<
          N, R extends StandardSpringRepulsion<N, R, B>, B extends Builder<N, R, B>>
      implements StandardRepulsion.Builder<N, R, B> {

    protected LoadingCache<N, SpringLayoutAlgorithm.SpringNodeData> springNodeData;
    protected int repulsionRangeSquared = 100 * 100;
    protected Random random = new Random();
    protected LayoutModel<N> layoutModel;

    public B withSpringNodeData(
        LoadingCache<N, SpringLayoutAlgorithm.SpringNodeData> springNodeData) {
      this.springNodeData = springNodeData;
      return (B) this;
    }

    public B setRepulsionRangeSquared(int repulsionRangeSquared) {
      this.repulsionRangeSquared = repulsionRangeSquared;
      return (B) this;
    }

    @Override
    public B withLayoutModel(LayoutModel<N> layoutModel) {
      this.layoutModel = layoutModel;
      return (B) this;
    }

    @Override
    public B withRandom(Random random) {
      this.random = random;
      return (B) this;
    }

    public R build() {
      return (R) new StandardSpringRepulsion(this);
    }
  }

  protected LoadingCache<N, SpringLayoutAlgorithm.SpringNodeData> springNodeData;
  protected int repulsionRangeSquared = 100 * 100;
  protected Random random = new Random();
  protected LayoutModel<N> layoutModel;

  public static Builder standardBuilder() {
    return new Builder();
  }

  protected StandardSpringRepulsion(Builder<N, R, B> builder) {
    this.layoutModel = builder.layoutModel;
    this.random = builder.random;
    this.springNodeData = builder.springNodeData;
    this.repulsionRangeSquared = builder.repulsionRangeSquared;
  }

  public void step() {}

  //  public void calculateRepulsion() {
  //    Graph<N> graph = layoutModel.getGraph();
  //
  //    try {
  //      for (N node : graph.nodes()) {
  //        if (layoutModel.isLocked(node)) {
  //          continue;
  //        }
  //
  //        SpringNodeData svd = springNodeData.getUnchecked(node);
  //        if (svd == null) {
  //          continue;
  //        }
  //        double dx = 0, dy = 0;
  //
  //        for (N node2 : graph.nodes()) {
  //          if (node == node2) {
  //            continue;
  //          }
  //          Point p = layoutModel.apply(node);
  //          Point p2 = layoutModel.apply(node2);
  //          if (p == null || p2 == null) {
  //            continue;
  //          }
  //          double vx = p.x - p2.x;
  //          double vy = p.y - p2.y;
  //          double distanceSq = p.distanceSquared(p2);
  //          if (distanceSq == 0) {
  //            dx += random.nextDouble();
  //            dy += random.nextDouble();
  //          } else if (distanceSq < repulsionRangeSquared) {
  //            double factor = 1;
  //            dx += factor * vx / distanceSq;
  //            dy += factor * vy / distanceSq;
  //          }
  //        }
  //        double dlen = dx * dx + dy * dy;
  //        if (dlen > 0) {
  //          dlen = Math.sqrt(dlen) / 2;
  //          svd.repulsiondx += dx / dlen;
  //          svd.repulsiondy += dy / dlen;
  //        }
  //      }
  //    } catch (ConcurrentModificationException cme) {
  //      calculateRepulsion();
  //    }
  //  }
  //}

  //public class StandardSpringRepulsion<
  //        N,
  //        R extends StandardSpringRepulsion<N, R, B>,
  //        B extends StandardSpringRepulsion.Builder<N, R, B>>
  //  extends edu.uci.ics.jung.layout3d.algorithms.repulsion.StandardSpringRepulsion<N, R, B>
  //    implements StandardRepulsion<N, R, B> {
  //
  //  public static class Builder<
  //          N, R extends StandardSpringRepulsion<N, R, B>, B extends Builder<N, R, B>>
  //    extends edu.uci.ics.jung.layout3d.algorithms.repulsion.StandardSpringRepulsion.Builder<N,R,B>
  //      implements StandardRepulsion.Builder<N, R, B> {
  //
  ////    protected LoadingCache<N, SpringNodeData> springNodeData;
  ////    protected int repulsionRangeSquared = 100 * 100;
  ////    protected Random random = new Random();
  ////    protected LayoutModel<N> layoutModel;
  //
  ////    public B withSpringNodeData(LoadingCache<N, SpringNodeData> springNodeData) {
  ////      this.springNodeData = springNodeData;
  ////      return (B) this;
  ////    }
  ////
  ////    public B setRepulsionRangeSquared(int repulsionRangeSquared) {
  ////      this.repulsionRangeSquared = repulsionRangeSquared;
  ////      return (B) this;
  ////    }
  //
  ////    @Override
  ////    public B withLayoutModel(LayoutModel<N> layoutModel) {
  ////      this.layoutModel = layoutModel;
  ////      return (B) this;
  ////    }
  //
  ////    @Override
  ////    public B withRandom(Random random) {
  ////      this.random = random;
  ////      return (B) this;
  ////    }
  //
  //    public R build() {
  //      return (R) new StandardSpringRepulsion(this);
  //    }
  //  }
  //
  //  protected LoadingCache<N, SpringNodeData> springNodeData;
  //  protected int repulsionRangeSquared = 100 * 100;
  //  protected Random random = new Random();
  //  protected LayoutModel<N> layoutModel;
  //
  //  public static Builder standardBuilder() {
  //    return new Builder();
  //  }
  //
  //  protected StandardSpringRepulsion(Builder<N, R, B> builder) {
  //    super (builder);
  ////    this.layoutModel = builder.layoutModel;
  ////    this.random = builder.random;
  ////    this.springNodeData = builder.springNodeData;
  ////    this.repulsionRangeSquared = builder.repulsionRangeSquared;
  //  }
  //
  //  public void step() {}
  //
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
        double dx = 0, dy = 0, dz = 0;

        for (N node2 : graph.nodes()) {
          if (node == node2) {
            continue;
          }
          Point p = layoutModel.apply(node);
          Point p2 = layoutModel.apply(node2);
          if (p == null || p2 == null) {
            continue;
          }
          double vx = p.x - p2.x;
          double vy = p.y - p2.y;
          double vz = p.z - p2.z;
          double distanceSq = p.distanceSquared(p2);
          if (distanceSq == 0) {
            dx += random.nextDouble();
            dy += random.nextDouble();
            dz += random.nextDouble();
          } else if (distanceSq < repulsionRangeSquared) {
            double factor = 1;
            dx += factor * vx / distanceSq;
            dy += factor * vy / distanceSq;
            dz += factor * vz / distanceSq;
          }
        }
        double dlen = dx * dx + dy * dy + dz * dz;
        if (dlen > 0) {
          dlen = Math.sqrt(dlen) / 2;
          svd.repulsiondx += dx / dlen;
          svd.repulsiondy += dy / dlen;
          svd.repulsiondz += dz / dlen;
        }
      }
    } catch (ConcurrentModificationException cme) {
      calculateRepulsion();
    }
  }
}
