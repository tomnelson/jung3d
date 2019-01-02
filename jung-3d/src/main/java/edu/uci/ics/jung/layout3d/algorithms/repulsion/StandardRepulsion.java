package edu.uci.ics.jung.layout3d.algorithms.repulsion;

import edu.uci.ics.jung.layout3d.model.LayoutModel;
import java.util.Random;

/**
 * @author Tom Nelson
 * @param <N> the node type
 * @param <R> the Repulsion type
 * @param <B> the Repulsion Builder type
 */
public interface StandardRepulsion<
    N, R extends StandardRepulsion<N, R, B>, B extends StandardRepulsion.Builder<N, R, B>> {

  interface Builder<N, R extends StandardRepulsion<N, R, B>, B extends Builder<N, R, B>> {

    B withLayoutModel(LayoutModel<N> layoutModel);

    B withRandom(Random random);

    R build();
  }

  /**
   * called from the layout algorithm on every step. this version is a noop but the subclass
   * BarnesHut version rebuilds the tree on every step
   */
  void step();

  void calculateRepulsion();
}
