package edu.uci.ics.jung.layout3d.algorithms.repulsion;

import com.google.common.cache.LoadingCache;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.spatial.BarnesHutOctTree;
import edu.uci.ics.jung.layout3d.spatial.ForceObject;
import edu.uci.ics.jung.layout3d.spatial.Node;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Nelson
 * @param <N> the node type
 */
public class BarnesHutFRRepulsion<N>
    extends StandardFRRepulsion<N, BarnesHutFRRepulsion<N>, BarnesHutFRRepulsion.Builder<N>>
    implements BarnesHutRepulsion<N, BarnesHutFRRepulsion<N>, BarnesHutFRRepulsion.Builder<N>> {

  private static final Logger log = LoggerFactory.getLogger(BarnesHutFRRepulsion.class);

  public static class Builder<N>
      extends StandardFRRepulsion.Builder<
          N, BarnesHutFRRepulsion<N>, BarnesHutFRRepulsion.Builder<N>>
      implements BarnesHutRepulsion.Builder<
          N, BarnesHutFRRepulsion<N>, BarnesHutFRRepulsion.Builder<N>> {

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

    public Builder<N> withFRNodeData(LoadingCache<N, Point> frNodeData) {
      this.frNodeData = frNodeData;
      return this;
    }

    public Builder<N> withRepulsionConstant(double repulstionConstant) {
      this.repulsionConstant = repulstionConstant;
      return this;
    }

    @Override
    public Builder<N> withRandom(Random random) {
      this.random = random;
      return this;
    }

    public BarnesHutFRRepulsion<N> build() {
      return new BarnesHutFRRepulsion(this);
    }
  }

  protected double EPSILON = 0.000001D;
  private double theta = Node.DEFAULT_THETA;
  private BarnesHutOctTree<N> tree;

  public static Builder barnesHutBuilder() {
    return new Builder();
  }

  protected BarnesHutFRRepulsion(Builder<N> builder) {
    super(builder);
    this.theta = builder.theta;
    this.tree = builder.tree;
  }

  public void step() {
    tree.rebuild(layoutModel.getLocations());
  }

  @Override
  public void calculateRepulsion() {
    for (N node1 : layoutModel.getGraph().nodes()) {
      Point fvd1 = frNodeData.getUnchecked(node1);
      if (fvd1 == null) {
        return;
      }
      frNodeData.put(node1, Point.ORIGIN);

      ForceObject<N> nodeForceObject =
          new ForceObject(node1, layoutModel.apply(node1)) {
            @Override
            protected void addForceFrom(ForceObject other) {
              double dx = this.p.x - other.p.x;
              double dy = this.p.y - other.p.y;
              double dz = this.p.z - other.p.z;
              //                  log.trace("dx, dy:{},{}", dx, dy);
              double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
              dist = Math.max(EPSILON, dist);
              //                  log.trace("dist:{}", dist);
              double force = (repulsionConstant * repulsionConstant) / dist;
              //                  log.trace("force:{}", force);
              f = f.add(force * (dx / dist), force * (dy / dist), force * (dz / dist));
            }
          };
      tree.applyForcesTo(nodeForceObject);
      frNodeData.put(node1, nodeForceObject.f);
    }
    log.debug("frNodeData: {}", frNodeData.asMap());
  }
}
