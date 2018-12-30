package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.spatial.BarnesHutOctTree;
import edu.uci.ics.jung.layout3d.spatial.ForceObject;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.function.Function;

/**
 * Subclass of SpringLayoutAlgorithm that uses a Barnes-Hut OctTree for O(NlogN) performance in the
 * repulsion calculation
 *
 * @param <N> the graph node type
 * @author Tom Nelson
 */
public class SpringBHLayoutAlgorithm<N> extends SpringLayoutAlgorithm<N>
    implements IterativeContext {

  private BarnesHutOctTree<N> tree;

  public SpringBHLayoutAlgorithm() {}

  public SpringBHLayoutAlgorithm(Function<? super EndpointPair<N>, Integer> length_function) {
    super(length_function);
  }
  /**
   * because the IterativeLayoutAlgorithms use multithreading to continuously update node positions,
   * the layoutModel state is saved (during the visit method) so that it can be used continuously
   *
   * @param layoutModel
   */
  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);
    tree = new BarnesHutOctTree(layoutModel);
  }

  public void step() {
    tree.rebuild();
    super.step();
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

        ForceObject<N> nodeForceObject =
            new ForceObject(node, layoutModel.apply(node)) {
              @Override
              protected void addForceFrom(ForceObject other, Optional userData) {

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
                } else if (distanceSq < repulsion_range_sq) {
                  double factor = 1;
                  f =
                      f.add(
                          factor * vx / distanceSq,
                          factor * vy / distanceSq,
                          factor * vz / distanceSq);
                }
              }
            };
        tree.visit(nodeForceObject, Optional.empty());
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
