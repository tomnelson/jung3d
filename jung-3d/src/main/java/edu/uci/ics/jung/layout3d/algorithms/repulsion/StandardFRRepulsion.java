package edu.uci.ics.jung.layout3d.algorithms.repulsion;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;

import java.util.ConcurrentModificationException;
import java.util.Random;

/**
 * @author Tom Nelson
 * @param <N> the node type
 * @param <R> the Repulsion type
 * @param <B> the Repulsion Builder type
 */
public class StandardFRRepulsion<
        N, R extends StandardFRRepulsion<N, R, B>, B extends StandardFRRepulsion.Builder<N, R, B>>
    implements StandardRepulsion<N, R, B> {

  public static class Builder<N, R extends StandardFRRepulsion<N, R, B>, B extends Builder<N, R, B>>
      implements StandardRepulsion.Builder<N, R, B> {

    protected LoadingCache<N, Point> frNodeData =
            CacheBuilder.newBuilder()
                    .build(
                            new CacheLoader<N, Point>() {
                              public Point load(N node) {
                                return Point.ORIGIN;
                              }
                            });
    protected double repulsionConstant;
    protected Random random = new Random();
    protected LayoutModel<N> layoutModel;

    public B setFRNodeData(LoadingCache<N, Point> frNodeData) {
      this.frNodeData = frNodeData;
      return (B) this;
    }

    public B setRepulsionConstant(double repulstionConstant) {
      this.repulsionConstant = repulstionConstant;
      return (B) this;
    }

    @Override
    public B setLayoutModel(LayoutModel<N> layoutModel) {
      this.layoutModel = layoutModel;
      return (B) this;
    }

    @Override
    public B setRandom(Random random) {
      this.random = random;
      return (B) this;
    }

    public R build() {
      return (R) new StandardFRRepulsion<>(this);
    }
  }

  protected LoadingCache<N, Point> frNodeData;
  protected double repulsionConstant;
  protected double EPSILON = 0.000001D;
  protected Random random = new Random();
  protected LayoutModel<N> layoutModel;

  public static Builder standardBuilder() {
    return new Builder();
  }

  protected StandardFRRepulsion(Builder<N, R, B> builder) {
    this.layoutModel = builder.layoutModel;
    this.random = builder.random;
    this.frNodeData = builder.frNodeData;
    this.repulsionConstant = builder.repulsionConstant;
  }

  public void step() {}

  public Random getRandom() {
    return random;
  }

  //  @Override
  //  public void calculateRepulsion() {
  //    for (N node1 : layoutModel.getGraph().nodes()) {
  //      Point fvd1 = frNodeData.getUnchecked(node1);
  //      if (fvd1 == null) {
  //        return;
  //      }
  //      frNodeData.put(node1, Point.ORIGIN);
  //
  //      try {
  //        for (N node2 : layoutModel.getGraph().nodes()) {
  //
  //          if (node1 != node2) {
  //            fvd1 = frNodeData.getUnchecked(node1);
  //            Point p1 = layoutModel.apply(node1);
  //            Point p2 = layoutModel.apply(node2);
  //            if (p1 == null || p2 == null) {
  //              continue;
  //            }
  //            double xDelta = p1.x - p2.x;
  //            double yDelta = p1.y - p2.y;
  //
  //            double deltaLength =
  //                    Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));
  //
  //            double force = (repulsionConstant * repulsionConstant) / deltaLength;
  //
  //            if (Double.isNaN(force)) {
  //              throw new RuntimeException(
  //                      "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
  //            }
  //            fvd1 = fvd1.add((xDelta / deltaLength) * force, (yDelta / deltaLength) * force);
  //            frNodeData.put(node1, fvd1);
  //          }
  //        }
  //      } catch (ConcurrentModificationException cme) {
  //        calculateRepulsion();
  //      }
  //    }
  //  }
  //}

  //
  //public class StandardFRRepulsion<
  //        N,
  //        R extends StandardFRRepulsion<N, R, B>,
  //        B extends StandardFRRepulsion.Builder<N, R, B>>
  //        extends edu.uci.ics.jung.layout.algorithms.repulsion.StandardFRRepulsion<N, R, B>
  //        implements StandardRepulsion<N, R, B> {
  //
  //
  //
  ////public class StandardFRRepulsion<
  ////        N, R extends edu.uci.ics.jung.layout.algorithms.repulsion.StandardFRRepulsion<N, R, B>, B extends StandardFRRepulsion.Builder<N, R, B>>
  ////    implements StandardRepulsion<N, R, B> {
  //
  ////  public static class Builder<N, R extends edu.uci.ics.jung.layout.algorithms.repulsion.StandardFRRepulsion<N, R, B>, B extends Builder<N, R, B>>
  ////      implements StandardRepulsion.Builder<N, R, B> {
  //
  //
  //    public static class Builder<
  //            N, R extends StandardFRRepulsion<N, R, B>, B extends Builder<N, R, B>>
  //            extends edu.uci.ics.jung.layout.algorithms.repulsion.StandardFRRepulsion.Builder<N,R,B>
  //            implements StandardRepulsion.Builder<N, R, B> {
  //
  //
  //
  ////      protected LoadingCache<N, Point> frNodeData;
  ////    protected double repulsionConstant;
  ////    protected Random random = new Random();
  ////    protected LayoutModel<N> layoutModel;
  ////
  ////    public B setFRNodeData(LoadingCache<N, Point> frNodeData) {
  ////      this.frNodeData = frNodeData;
  ////      return (B) this;
  ////    }
  ////
  ////    public B setRepulsionConstant(double repulstionConstant) {
  ////      this.repulsionConstant = repulstionConstant;
  ////      return (B) this;
  ////    }
  ////
  ////    @Override
  ////    public B setLayoutModel(LayoutModel<N> layoutModel) {
  ////      this.layoutModel = layoutModel;
  ////      return (B) this;
  ////    }
  ////
  ////    @Override
  ////    public B setRandom(Random random) {
  ////      this.random = random;
  ////      return (B) this;
  ////    }
  //      public R build() {
  //          return (R) new StandardFRRepulsion(this);
  //      }
  //
  ////    public R build() {
  ////      return (R) new StandardFRRepulsion<>(this);
  ////    }
  //  }
  //
  //  protected LoadingCache<N, Point> frNodeData;
  //  protected double repulsionConstant;
  //  protected double EPSILON = 0.000001D;
  //  protected Random random = new Random();
  //  protected LayoutModel<N> layoutModel;
  //
  //  public static Builder standardBuilder() {
  //    return new Builder();
  //  }
  //
  //  protected StandardFRRepulsion(Builder<N, R, B> builder) {
  //    super(builder);
  ////    this.layoutModel = builder.layoutModel;
  ////    this.random = builder.random;
  ////    this.frNodeData = builder.frNodeData;
  ////    this.repulsionConstant = builder.repulsionConstant;
  //  }
  //
  //  public void step() {}
  //
  //  public Random getRandom() {
  //    return random;
  //  }
  //
  @Override
  public void calculateRepulsion() {
    for (N node1 : layoutModel.getGraph().nodes()) {
      Point fvd1 = frNodeData.getUnchecked(node1);
      if (fvd1 == null) {
        return;
      }
      frNodeData.put(node1, Point.ORIGIN);

      try {
        for (N node2 : layoutModel.getGraph().nodes()) {

          if (node1 != node2) {
            fvd1 = frNodeData.getUnchecked(node1);
            Point p1 = (Point) layoutModel.apply(node1);
            Point p2 = (Point) layoutModel.apply(node2);
            if (p1 == null || p2 == null) {
              continue;
            }
            double xDelta = p1.x - p2.x;
            double yDelta = p1.y - p2.y;
            double zDelta = p1.z - p2.z;

            double deltaLength =
                Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

            double force = (repulsionConstant * repulsionConstant) / deltaLength;

            if (Double.isNaN(force)) {
              throw new RuntimeException(
                  "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
            }
            fvd1 =
                fvd1.add(
                    (xDelta / deltaLength) * force,
                    (yDelta / deltaLength) * force,
                    (zDelta / deltaLength) * force);
            frNodeData.put(node1, fvd1);
          }
        }
      } catch (ConcurrentModificationException cme) {
        calculateRepulsion();
      }
    }
  }
}
