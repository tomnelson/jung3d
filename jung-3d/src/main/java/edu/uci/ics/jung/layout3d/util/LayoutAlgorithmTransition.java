package edu.uci.ics.jung.layout3d.util;

import edu.uci.ics.jung.layout3d.algorithms.AnimationLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.visualization3d.VisualizationViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the transition to a new LayoutAlgorithm. The transition can me animated or immediate. The
 * view side has a reference to the VisualizationServer so that it can manage activity of the
 * Spatial structures during the transition. Typically, they are turned off until the transition is
 * complete to minimize unnecessary work.
 *
 * @param <N>
 * @param <E>
 */
public class LayoutAlgorithmTransition<N, E> {

  private static Logger log = LoggerFactory.getLogger(LayoutAlgorithmTransition.class);

  public static <N, E> void animate(
      VisualizationViewer<N, E> visualizationServer, LayoutAlgorithm<N> endLayoutAlgorithm) {
    fireLayoutStateChanged(visualizationServer.getLayoutModel(), true);
    LayoutAlgorithm<N> transitionLayoutAlgorithm =
        new AnimationLayoutAlgorithm<>(visualizationServer, endLayoutAlgorithm);
    visualizationServer.setLayoutAlgorithm(transitionLayoutAlgorithm);
  }

  public static <N, E> void apply(
      VisualizationViewer<N, E> visualizationServer, LayoutAlgorithm<N> endLayoutAlgorithm) {
    visualizationServer.setLayoutAlgorithm(endLayoutAlgorithm);
  }

  private static void fireLayoutStateChanged(LayoutModel layoutModel, boolean state) {
    log.trace("fireLayoutStateChanged to {}", state);
    layoutModel.getLayoutStateChangeSupport().fireLayoutStateChanged(layoutModel, state);
  }
}
