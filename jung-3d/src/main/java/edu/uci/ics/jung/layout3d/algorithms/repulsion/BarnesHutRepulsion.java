package edu.uci.ics.jung.layout3d.algorithms.repulsion;

import edu.uci.ics.jung.layout3d.model.LayoutModel;

/**
 * @author Tom Nelson
 * @param <N> the node type
 * @param <R> the Repulsion type
 * @param <B> the Repulsion Builder type
 */
public interface BarnesHutRepulsion<
        N, R extends BarnesHutRepulsion<N, R, B>, B extends BarnesHutRepulsion.Builder<N, R, B>>
    extends StandardRepulsion<N, R, B> {

  interface Builder<N, R extends BarnesHutRepulsion<N, R, B>, B extends Builder<N, R, B>>
      extends StandardRepulsion.Builder<N, R, B> {

    B withLayoutModel(LayoutModel<N> layoutModel);

    B withTheta(double theta);

    R build();
  }
}
