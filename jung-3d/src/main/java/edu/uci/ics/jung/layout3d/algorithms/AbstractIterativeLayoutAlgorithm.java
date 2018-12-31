package edu.uci.ics.jung.layout3d.algorithms;

import edu.uci.ics.jung.layout3d.model.LayoutModel;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For Iterative algorithms that perform delayed operations on a Thread, save off the layoutModel so
 * that it can be accessed by the threaded code. The layoutModel could be removed and instead passed
 * via all of the iterative methods (for example step(layoutModel) instead of step() )
 *
 * @author Tom Nelson
 */
public abstract class AbstractIterativeLayoutAlgorithm<N> implements IterativeLayoutAlgorithm<N> {

  private static final Logger log = LoggerFactory.getLogger(AbstractIterativeLayoutAlgorithm.class);
  public abstract static class Builder<
          N, T extends AbstractIterativeLayoutAlgorithm<N>, B extends Builder<N, T, B>> {
    protected Random random = new Random();
    protected boolean shouldPrerelax = true;
    protected int preRelaxDurationMs = 500;

    public B setRandomSeed(long randomSeed) {
      this.random = new Random(randomSeed);
      return (B) this;
    }

    public B setShouldPrerelax(boolean shouldPrerelax) {
      this.shouldPrerelax = shouldPrerelax;
      return (B) this;
    }

    public B setPreRelaxDuration(int preRelaxDurationMs) {
      this.preRelaxDurationMs = preRelaxDurationMs;
      return (B) this;
    }

    public abstract T build();
  }

  protected AbstractIterativeLayoutAlgorithm(Builder builder) {
    this.random = builder.random;
    this.shouldPreRelax = builder.shouldPrerelax;
    this.preRelaxDurationMs = builder.preRelaxDurationMs;
  }

  /**
   * because the IterativeLayoutAlgorithms use multithreading to continuously update node positions,
   * the layoutModel state is saved (during the visit method) so that it can be used continuously
   */
  protected LayoutModel<N> layoutModel;

  // both of these can be set at instance creation time
  protected boolean shouldPreRelax = true;
  protected int preRelaxDurationMs = 500; // how long should the prerelax phase last?

  protected Random random = new Random();

  public void setRandomSeed(long randomSeed) {
    this.random = new Random(randomSeed);
  }

  // returns true iff prerelaxing happened
  public final boolean preRelax() {
    if (!shouldPreRelax) {
      return false;
    }
    long timeNow = System.currentTimeMillis();
    while (System.currentTimeMillis() - timeNow < preRelaxDurationMs && !done()) {
      step();
    }
    return true;
  }

  /**
   * because the IterativeLayoutAlgorithms use multithreading to continuously update node positions,
   * the layoutModel state is saved (during the visit method) so that it can be used continuously
   */
  public void visit(LayoutModel<N> layoutModel) {
    log.trace("visiting " + layoutModel);
    this.layoutModel = layoutModel;
  }
}
