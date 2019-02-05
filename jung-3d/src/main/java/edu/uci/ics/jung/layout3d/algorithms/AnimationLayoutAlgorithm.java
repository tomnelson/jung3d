package edu.uci.ics.jung.layout3d.algorithms;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.visualization3d.VisualizationViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class AnimationLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(AnimationLayoutAlgorithm.class);

  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;

  LayoutModel<N> transitionLayoutModel;
  VisualizationViewer<N, ?> visualizationServer;
  LayoutAlgorithm<N> endLayoutAlgorithm;
  LayoutModel<N> layoutModel;

  public static class Builder<N, T extends AnimationLayoutAlgorithm<N>, B extends Builder<N, T, B>>
      extends AbstractIterativeLayoutAlgorithm.Builder<N, T, B> {

    private VisualizationViewer<N, ?> visualizationServer;
    private LayoutAlgorithm<N> endLayoutAlgorithm;
    private boolean shouldPrerelax = false;

    public B withVisualizationServer(VisualizationViewer<N, ?> visualizationServer) {
      this.visualizationServer = visualizationServer;
      return (B) this;
    }

    public B withEndLayoutAlgorithm(LayoutAlgorithm<N> endLayoutAlgorithm) {
      this.endLayoutAlgorithm = endLayoutAlgorithm;
      return (B) this;
    }

    @Override
    public B shouldPrerelax(boolean shouldPrerelax) {
      this.shouldPrerelax = shouldPrerelax;
      return (B) this;
    }

    public T build() {
      return (T) new AnimationLayoutAlgorithm<>(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  protected AnimationLayoutAlgorithm(Builder builder) {
    super(builder);
    this.visualizationServer = builder.visualizationServer;
    this.endLayoutAlgorithm = builder.endLayoutAlgorithm;
    this.shouldPreRelax = builder.shouldPrerelax;
  }

  public void visit(LayoutModel<N> layoutModel) {
    // save off the existing layoutModel
    this.layoutModel = layoutModel;
    // create a LayoutModel to hold points for the transition
    this.transitionLayoutModel =
        LoadingCacheLayoutModel.<N>builder()
            .withGraph(visualizationServer.getNetwork().asGraph())
            .withLayoutModel(layoutModel)
            .withInitializer(layoutModel)
            .build();
    // start off the transitionLayoutModel with the endLayoutAlgorithm
    transitionLayoutModel.accept(endLayoutAlgorithm);
  }

  /**
   * each step of the animation moves every pouit 1/count of the distance from its old location to
   * its new location
   */
  public void step() {
    for (N v : layoutModel.getGraph().nodes()) {
      Point tp = layoutModel.apply(v);
      Point fp = transitionLayoutModel.apply(v);
      double dx = (fp.x - tp.x) / (count - counter);
      double dy = (fp.y - tp.y) / (count - counter);
      double dz = (fp.z - tp.z) / (count - counter);
      log.trace("dx:{},dy:{}", dx, dy);
      layoutModel.set(v, tp.add(dx, dy, dz));
    }
    counter++;
    if (counter >= count) {
      done = true;
      this.transitionLayoutModel.stopRelaxer();
      this.visualizationServer.setLayoutAlgorithm(endLayoutAlgorithm);
    }
  }

  public boolean done() {
    return done;
  }
}
