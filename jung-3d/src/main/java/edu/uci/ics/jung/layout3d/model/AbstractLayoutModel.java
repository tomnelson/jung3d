package edu.uci.ics.jung.layout3d.model;

import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout3d.event.LayoutNodePositionChange;
import edu.uci.ics.jung.layout.util.VisRunnable;
import edu.uci.ics.jung.layout3d.algorithms.IterativeLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout3d.event.LayoutChange;
import edu.uci.ics.jung.layout3d.event.LayoutStateChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * superclass for 3D LayoutModels. Holds the required attributes for placing graph nodes in a
 * visualization
 *
 * @author Tom Nelson
 * @param <N> the node type
 */
public abstract class AbstractLayoutModel<N> implements LayoutModel<N> {

  private static final Logger log = LoggerFactory.getLogger(AbstractLayoutModel.class);

  public abstract static class Builder<
      N, T extends AbstractLayoutModel<N>, B extends Builder<N, T, B>> {
    protected Graph<N> graph;
    protected int width;
    protected int height;
    protected int depth;

    public B withGraph(Graph<N> graph) {
      this.graph = graph;
      return (B) this;
    }

    public B withSize(int width, int height, int depth) {
      this.width = width;
      this.height = height;
      this.depth = depth;
      return (B) this;
    }

    public B withWidth(int width) {
      this.width = width;
      return (B) this;
    }

    public B withHeight(int height) {
      this.height = height;
      return (B) this;
    }

    public B withDepth(int depth) {
      this.depth = depth;
      return (B) this;
    }
  }

  private Set<N> lockedNodes = Sets.newHashSet();
  protected boolean locked;
  protected int width;
  protected int height;
  protected int depth;
  protected Graph<N> graph;
  protected VisRunnable visRunnable;
  /** @value relaxing true is this layout model is being accessed by a running relaxer */
  protected boolean relaxing;

  protected CompletableFuture theFuture;
    protected LayoutNodePositionChange.Support layoutNodePositionSupport =
            LayoutNodePositionChange.Support.create();
    protected LayoutStateChange.Support layoutStateChangeSupport = LayoutStateChange.Support.create();
    protected LayoutChange.Support layoutChangeSupport = LayoutChange.Support.create();

  protected AbstractLayoutModel(Builder builder) {
    this.graph = builder.graph;
    setSize(builder.width, builder.height, builder.depth);
  }

  protected AbstractLayoutModel(Graph<N> graph, int width, int height, int depth) {
    this.graph = graph;
    setSize(width, height, depth);
  }

  /** stop any running Relaxer */
  public void stopRelaxer() {
    if (this.visRunnable != null) {
      this.visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }
    setRelaxing(false);
  }

  public CompletableFuture getTheFuture() {
    return theFuture;
  }

  /**
   * accept the visit of a LayoutAlgorithm. If it is an IterativeContext, create a VisRunner to run
   * its relaxer in a new Thread. If there is a current VisRunner, stop it first.
   *
   * @param layoutAlgorithm
   */
  @Override
  public void accept(LayoutAlgorithm<N> layoutAlgorithm) {
    // the layoutMode is active with a new LayoutAlgorithm
    layoutStateChangeSupport.fireLayoutStateChanged(this, true);
    log.trace("accepting {}", layoutAlgorithm);
    layoutNodePositionSupport.setFireEvents(true);
    layoutChangeSupport.fireLayoutChanged();
    if (this.visRunnable != null) {
      log.trace("stopping {}", visRunnable);
      this.visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }
    if (log.isTraceEnabled()) {
      log.trace("{} will visit {}", layoutAlgorithm, this);
    }
    if (layoutAlgorithm != null) {
      layoutAlgorithm.visit(this);

      if (layoutAlgorithm instanceof IterativeLayoutAlgorithm) {
        setRelaxing(true);
        setupVisRunner((IterativeLayoutAlgorithm) layoutAlgorithm);

        // need to have the visRunner fire the layoutStateChanged event when it finishes
      } else {
        if (log.isTraceEnabled()) {
          log.trace("no visRunner for this {}", this);
        }
        // the layout model has finished with the layout algorithm
        log.trace("will fire layoutStateCHanged with false");
        layoutStateChangeSupport.fireLayoutStateChanged(this, false);
      }
    }
  }

    @Override
    public LayoutStateChange.Support getLayoutStateChangeSupport() {
        return layoutStateChangeSupport;
    }

    @Override
    public LayoutChange.Support getLayoutChangeSupport() {
        return this.layoutChangeSupport;
    }


    /**
   * create and start a new VisRunner for the passed IterativeContext
   *
   * @param iterativeContext
   */
  protected void setupVisRunner(IterativeLayoutAlgorithm iterativeContext) {
    log.trace("this {} is setting up a visRunnable with {}", this, iterativeContext);
    if (visRunnable != null) {
      visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }

    // layout becomes active
    layoutStateChangeSupport.fireLayoutStateChanged(this, true);
    // prerelax phase
      layoutNodePositionSupport.setFireEvents(false);
      layoutChangeSupport.setFireEvents(false);
      iterativeContext.preRelax();
      layoutChangeSupport.setFireEvents(true);
      layoutNodePositionSupport.setFireEvents(true);
    log.trace("prerelax is done");

    visRunnable = new VisRunnable(iterativeContext);
    theFuture =
        CompletableFuture.runAsync(visRunnable)
            .thenRun(
                () -> {
                  log.info("We're done");
                  setRelaxing(false);
                    this.layoutChangeSupport.fireLayoutChanged();
                    // fire an event to say that the layout relax is done
                    this.layoutStateChangeSupport.fireLayoutStateChanged(this, false);
                });
  }

  /** @return the graph */
  @Override
  public Graph<N> getGraph() {
    return graph;
  }

  public void setGraph(Graph<N> graph) {
    this.graph = graph;
      this.layoutChangeSupport.fireLayoutChanged();
    if (log.isTraceEnabled()) {
      log.trace("withGraph to n:{} e:{}", graph.nodes(), graph.edges());
    }
  }

  /**
   * set locked state for the provided node
   *
   * @param node the node to affect
   * @param locked to lock or not
   */
  @Override
  public void lock(N node, boolean locked) {
    if (locked) {
      this.lockedNodes.add(node);
    } else {
      this.lockedNodes.remove(node);
    }
  }

  /**
   * @param node the node whose locked state is being queried
   * @return whether the passed node is locked
   */
  @Override
  public boolean isLocked(N node) {
    return this.lockedNodes.contains(node);
  }

  /**
   * lock the entire model (all nodes)
   *
   * @param locked
   */
  @Override
  public void lock(boolean locked) {
    log.trace("lock:{}", locked);
    this.locked = locked;
  }

  /** @return whether this LayoutModel is locked for all nodes */
  @Override
  public boolean isLocked() {
    return this.locked;
  }

  /**
   * When a visualization is resized, it presumably wants to fix the locations of the nodes and
   * possibly to reinitialize its data. The current method calls <tt>initializeLocations</tt>
   * followed by <tt>initialize_local</tt>.
   */
  public void setSize(int width, int height, int depth) {
    if (width == 0 || height == 0 || depth == 0) {
      throw new IllegalArgumentException("Can't be zeros " + width + "/" + height + "/" + depth);
    }
    int oldWidth = this.width;
    int oldHeight = this.height;
    int oldDepth = this.depth;

    if (oldWidth == width && oldHeight == height && oldDepth == depth) {
      return;
    }

    if (oldWidth != 0 || oldHeight != 0 || oldDepth != 0) {
      adjustLocations(oldWidth, oldHeight, oldDepth, width, height, depth); //, size);
    }
    this.width = width;
    this.height = height;
    this.depth = depth;
  }

  /**
   * mode all the nodes to the new center of the layout domain
   *
   * @param oldWidth
   * @param oldHeight
   * @param width
   * @param height
   */
  private void adjustLocations(
      int oldWidth, int oldHeight, int oldDepth, int width, int height, int depth) {
    if (oldWidth == width && oldHeight == height && oldDepth == depth) {
      return;
    }

    int xOffset = (width - oldWidth) / 2;
    int yOffset = (height - oldHeight) / 2;
    int zOffset = (depth - oldDepth) / 2;

    // now, move each node to be at the new screen center
    while (true) {
      try {
        for (N node : this.graph.nodes()) {
          offsetnode(node, xOffset, yOffset, zOffset);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
  }

  /** @return the width of the layout domain */
  @Override
  public int getWidth() {
    return width;
  }

  /** @return the height of the layout domain */
  @Override
  public int getHeight() {
    return height;
  }

  /** @return the depth of the layout domain */
  @Override
  public int getDepth() {
    return depth;
  }

  @Override
  public void set(N node, double x, double y, double z) {
    this.set(node, x, y, z);
  }

  @Override
  public void set(N node, Point location) {
      layoutNodePositionSupport.fireLayoutNodePositionChanged(node, location);
      layoutChangeSupport.fireLayoutChanged();
  }

  /**
   * @param node the node whose coordinates are to be offset
   * @param xOffset the change to apply to this node's x coordinate
   * @param yOffset the change to apply to this node's y coordinate
   */
  protected void offsetnode(N node, double xOffset, double yOffset, double zOffset) {
    if (!locked && !isLocked(node)) {
      Point p = get(node);
      this.set(node, p.add(xOffset, yOffset, zOffset));
    }
  }

    @Override
    public LayoutNodePositionChange.Support<N> getLayoutNodePositionSupport() {
        return this.layoutNodePositionSupport;
    }


//  @Override
//  public boolean isFireEvents() {
//    return changeSupport.isFireEvents();
//  }
//
//  @Override
//  public void setFireEvents(boolean fireEvents) {
//    changeSupport.setFireEvents(fireEvents);
//  }
//
//  @Override
//  public void addChangeListener(ChangeListener l) {
//    changeSupport.addChangeListener(l);
//  }
//
//  @Override
//  public void addLayoutChangeListener(LayoutChangeListener<N> listener) {
//    layoutChangeListeners.add(listener);
//  }
//
//  @Override
//  public void removeLayoutChangeListener(LayoutChangeListener<N> listener) {
//    layoutChangeListeners.remove(listener);
//  }
//
//  protected void fireLayoutChanged(N node, Point location) {
//    if (layoutChangeListeners.size() > 0) {
//      LayoutEvent<N> layoutEvent = new LayoutEvent(node, location);
//      for (LayoutChangeListener<N> layoutChangeListener : layoutChangeListeners) {
//        layoutChangeListener.layoutChanged(layoutEvent);
//      }
//    }
//  }
//
//  @Override
//  public void removeChangeListener(ChangeListener l) {
//    changeSupport.removeChangeListener(l);
//  }
//
  public boolean isRelaxing() {
    return relaxing;
  }

  public void setRelaxing(boolean relaxing) {
    log.trace("setRelaxing:{}", relaxing);
    this.relaxing = relaxing;
  }

//  @Override
//  public void fireChanged() {
//    log.trace("fireChanged");
//    changeSupport.fireChanged();
//  }

  @Override
  public String toString() {
    return "AbstractLayoutModel{"
        + "hashCode="
        + hashCode()
        + ", width="
        + width
        + ", height="
        + height
        + ", graph of size ="
        + graph.nodes().size()
        + '}';
  }
}
