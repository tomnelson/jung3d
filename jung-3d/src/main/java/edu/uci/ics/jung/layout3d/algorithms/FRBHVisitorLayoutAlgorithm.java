package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.spatial.BarnesHutOctTree;
import edu.uci.ics.jung.layout3d.spatial.ForceObject;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Fruchterman-Reingold force-directed algorithm for node layout. Uses a Barnes-Hut
 * OctTree for O(NlogN) performance in the repulsion calculation
 *
 * @author Tom Nelson
 */
public class FRBHVisitorLayoutAlgorithm<N> extends FRLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(FRBHVisitorLayoutAlgorithm.class);

  private BarnesHutOctTree<N> tree;

  public FRBHVisitorLayoutAlgorithm() {
    this.frNodeData =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<N, Point>() {
                  public Point load(N node) {
                    return Point.ORIGIN;
                  }
                });
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);
    tree = new BarnesHutOctTree(layoutModel);
  }

  /**
   * Moves the iteration forward one notch, calculation attraction and repulsion between nodes and
   * edges and cooling the temperature.
   */
  public synchronized void step() {
    tree.rebuild();
    super.step();
  }

  protected void calcRepulsion(N node1) {
    Point fvd1 = getFRData(node1);
    if (fvd1 == null) {
      return;
    }
    frNodeData.put(node1, Point.ORIGIN);

    ForceObject<N> nodeForceObject =
        new ForceObject(node1, layoutModel.apply(node1)) {
          @Override
          protected void addForceFrom(ForceObject other, Optional userData) {
            double dx = this.p.x - other.p.x;
            double dy = this.p.y - other.p.y;
            double dz = this.p.z - other.p.z;
            log.trace("dx, dy:{},{},{}", dx, dy, dz);
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            dist = Math.max(EPSILON, dist);
            log.trace("dist:{}", dist);
            double force = (repulsion_constant * repulsion_constant) / dist;
            log.trace("force:{}", force);
            f = f.add(force * (dx / dist), force * (dy / dist), force * (dz / dist));
          }
        };
    tree.visit(nodeForceObject, Optional.empty());
    frNodeData.put(node1, nodeForceObject.f);
  }
}
