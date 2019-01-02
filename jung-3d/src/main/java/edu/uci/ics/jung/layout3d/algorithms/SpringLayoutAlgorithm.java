package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout3d.algorithms.repulsion.StandardSpringRepulsion;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import java.util.ConcurrentModificationException;
import java.util.function.Function;

/**
 * adapted from the JUNG SpringLayout class. Refactored to be a visitor to a LayoutModel holding
 * node positions. Extended to 3 dimensions with a z coordinate and depth
 *
 * @param <N> the graph node type
 * @author Tom Nelson
 */
public class SpringLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  protected double stretch = 0.70;
  protected Function<? super EndpointPair<N>, Integer> lengthFunction;
  protected int repulsion_range_sq = 100 * 100;
  protected double force_multiplier = 1.0 / 3.0;

  protected LoadingCache<N, SpringNodeData> springNodeData =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> new SpringNodeData()));

  protected StandardSpringRepulsion.Builder repulsionContractBuilder;
  protected StandardSpringRepulsion repulsionContract;

  public static class Builder<N, T extends SpringLayoutAlgorithm<N>, B extends Builder<N, T, B>>
      extends AbstractIterativeLayoutAlgorithm.Builder<N, T, B> {
    private StandardSpringRepulsion.Builder repulsionContractBuilder =
        StandardSpringRepulsion.standardBuilder();
    private Function<? super EndpointPair<N>, Integer> lengthFunction = n -> 30;

    public B withRepulsionContractBuilder(
        StandardSpringRepulsion.Builder repulsionContractBuilder) {
      this.repulsionContractBuilder = (StandardSpringRepulsion.Builder) repulsionContractBuilder;
      return (B) this;
    }

    public B withLengthFunction(Function<? super EndpointPair<N>, Integer> lengthFunction) {
      this.lengthFunction = lengthFunction;
      return (B) this;
    }

    public T build() {
      return (T) new SpringLayoutAlgorithm(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  protected SpringLayoutAlgorithm(Builder builder) {
    super(builder);
    this.lengthFunction = builder.lengthFunction;
    this.repulsionContractBuilder = builder.repulsionContractBuilder;
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);

    // setting the layout model will build the BHQT if the builder is the
    // Optimized one
    repulsionContract =
        repulsionContractBuilder
            .withSpringNodeData(springNodeData)
            .withLayoutModel(layoutModel)
            .withRandom(random)
            .build();
  }

  /** @return the current value for the stretch parameter */
  public double getStretch() {
    return stretch;
  }

  public void setStretch(double stretch) {
    this.stretch = stretch;
  }

  public int getRepulsionRange() {
    return (int) (Math.sqrt(repulsion_range_sq));
  }

  public void setRepulsionRange(int range) {
    this.repulsion_range_sq = range * range;
  }

  public double getForceMultiplier() {
    return force_multiplier;
  }

  public void setForceMultiplier(double force) {
    this.force_multiplier = force;
  }

  public void initialize() {}

  public void step() {
    this.repulsionContract.step();
    Graph<N> graph = layoutModel.getGraph();
    try {
      for (N node : graph.nodes()) {
        SpringNodeData svd = springNodeData.getUnchecked(node);
        if (svd == null) {
          continue;
        }
        svd.dx /= 4;
        svd.dy /= 4;
        svd.dz /= 4;
        svd.edgedx = svd.edgedy = svd.edgedz = 0;
        svd.repulsiondx = svd.repulsiondy = svd.repulsiondz = 0;
      }
    } catch (ConcurrentModificationException cme) {
      step();
    }

    relaxEdges();
    repulsionContract.calculateRepulsion();
    moveNodes();
  }

  protected void relaxEdges() {
    Graph<N> graph = layoutModel.getGraph();
    try {
      for (EndpointPair<N> endpoints : layoutModel.getGraph().edges()) {
        N node1 = endpoints.nodeU();
        N node2 = endpoints.nodeV();

        Point p1 = this.layoutModel.get(node1);
        Point p2 = this.layoutModel.get(node2);
        if (p1 == null || p2 == null) {
          continue;
        }
        double vx = p1.x - p2.x;
        double vy = p1.y - p2.y;
        double vz = p1.z - p2.z;
        double len = Math.sqrt(vx * vx + vy * vy + vz * vz);

        double desiredLen = lengthFunction.apply(endpoints);

        // round from zero, if needed [zero would be Bad.].
        len = (len == 0) ? .0001 : len;

        double f = force_multiplier * (desiredLen - len) / len;

        f = f * Math.pow(stretch, (graph.degree(node1) + graph.degree(node2) - 2));

        // the actual movement distance 'dx' is the force multiplied by the
        // distance to go.
        double dx = f * vx;
        double dy = f * vy;
        double dz = f * vz;
        SpringNodeData v1D, v2D;
        v1D = springNodeData.getUnchecked(node1);
        v2D = springNodeData.getUnchecked(node2);

        v1D.edgedx += dx;
        v1D.edgedy += dy;
        v1D.edgedz += dz;
        v2D.edgedx += -dx;
        v2D.edgedy += -dy;
        v2D.edgedz += -dz;
      }
    } catch (ConcurrentModificationException cme) {
      relaxEdges();
    }
  }

  protected void calculateRepulsion() {
    Graph<N> graph = layoutModel.getGraph();

    try {
      for (N node : graph.nodes()) {
        if (layoutModel.isLocked(node)) {
          continue;
        }

        SpringNodeData svd = springNodeData.getUnchecked(node);
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
            dx += Math.random();
            dy += Math.random();
            dz += Math.random();
          } else if (distanceSq < repulsion_range_sq) {
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

  protected void moveNodes() {
    Graph<N> graph = layoutModel.getGraph();

    synchronized (layoutModel) {
      try {
        for (N node : graph.nodes()) {
          if (layoutModel.isLocked(node)) {
            continue;
          }
          SpringNodeData vd = springNodeData.getUnchecked(node);
          if (vd == null) {
            continue;
          }
          Point xyd = layoutModel.apply(node);
          double posX = xyd.x;
          double posY = xyd.y;
          double posZ = xyd.z;

          vd.dx += vd.repulsiondx + vd.edgedx;
          vd.dy += vd.repulsiondy + vd.edgedy;
          vd.dz += vd.repulsiondz + vd.edgedz;

          // keeps nodes from moving any faster than 5 per time unit\
          posX = posX + Math.max(-5, Math.min(5, vd.dx));
          posY = posY + Math.max(-5, Math.min(5, vd.dy));
          posZ = posZ + Math.max(-5, Math.min(5, vd.dz));

          int radiusX = layoutModel.getWidth() / 2;
          int radiusY = layoutModel.getHeight() / 2;
          int radiusZ = layoutModel.getDepth() / 2;

          if (posX < -radiusX) {
            posX = -radiusX;
          } else if (posX > radiusX) {
            posX = radiusX;
          }
          if (posY < -radiusY) {
            posY = -radiusY;
          } else if (posY > radiusY) {
            posY = radiusY;
          }
          if (posZ < -radiusZ) {
            posZ = -radiusZ;
          } else if (posZ > radiusZ) {
            posZ = radiusZ;
          }

          // after the bounds have been honored above, really set the location
          // in the layout model
          layoutModel.set(node, posX, posY, posZ);
        }
      } catch (ConcurrentModificationException cme) {
        moveNodes();
      }
    }
  }

  public static class SpringNodeData {
    protected double edgedx;
    protected double edgedy;
    protected double edgedz;
    public double repulsiondx;
    public double repulsiondy;
    public double repulsiondz;

    /** movement speed, x */
    protected double dx;

    /** movement speed, y */
    protected double dy;

    protected double dz;
  }

  public boolean done() {
    return false;
  }
}
