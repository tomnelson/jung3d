package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout3d.algorithms.repulsion.StandardFRRepulsion;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.util.RandomLocationTransformer;
import java.util.ConcurrentModificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Fruchterman-Reingold force-directed algorithm for node layout.
 *
 * @author Tom Nelson adapted from Jung FRLayout and extended to 3 dimensions
 */
public class FRLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(FRLayoutAlgorithm.class);

  private double forceConstant;

  private double temperature;

  private int currentIteration;

  private int mMaxIterations = 700;

  protected LoadingCache<N, Point> frNodeData =
          CacheBuilder.newBuilder()
                  .build(
                          new CacheLoader<N, Point>() {
                            public Point load(N node) {
                              return Point.ORIGIN;
                            }
                          });

  private double attractionMultiplier = 0.75;

  private double attractionConstant;

  private double repulsionMultiplier = 0.75;

  protected double repulsionConstant;

  private double maxDimension;

  private boolean initialized = false;

  private float width;
  private float height;
  private float depth;
  private float xMax;
  private float xMin;
  private float yMax;
  private float yMin;
  private float zMax;
  private float zMin;
  private float border;

  protected StandardFRRepulsion.Builder repulsionContractBuilder;
  protected StandardFRRepulsion repulsionContract;

  public static class Builder<N>
          extends AbstractIterativeLayoutAlgorithm.Builder<N, FRLayoutAlgorithm<N>, Builder<N>> {
    private StandardFRRepulsion.Builder repulsionContractBuilder =
            new StandardFRRepulsion.Builder();

    public Builder<N> setRepulsionContractBuilder(
            StandardFRRepulsion.Builder repulsionContractBuilder) {
      this.repulsionContractBuilder = repulsionContractBuilder;
      return this;
    }

    public FRLayoutAlgorithm<N> build() {
      return new FRLayoutAlgorithm(this);
    }
  }

  public static <N> Builder<N> builder() {
    return new Builder<N>();
  }

  protected FRLayoutAlgorithm(Builder<N> builder) {
    super(builder);
    this.repulsionContractBuilder = builder.repulsionContractBuilder;
  }
//  public FRLayoutAlgorithm() {
//    this.frNodeData =
//        CacheBuilder.newBuilder()
//            .build(
//                new CacheLoader<N, Point>() {
//                  public Point load(N node) {
//                    return Point.ORIGIN;
//                  }
//                });
//  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    if (log.isTraceEnabled()) {
      log.trace("visiting " + layoutModel);
    }
    super.visit(layoutModel);
    maxDimension = Math.max(layoutModel.getWidth(), layoutModel.getHeight());

        this.width = layoutModel.getWidth() * (float) Math.sqrt(2.0f) / 2.0f;
        this.height = layoutModel.getHeight() * (float) Math.sqrt(2.0f) / 2.0f;
        this.depth = layoutModel.getDepth() * (float) Math.sqrt(2.0f) / 2.0f;
        this.xMax = width / 2;
        this.xMin = -this.xMax;
        this.yMax = height / 2;
        this.yMin = -this.yMax;
        this.zMax = depth / 2;
        this.zMin = -this.zMax;
        this.border = Math.max(width, Math.max(height, depth)) / 50;

    initialize();
    repulsionContract =
            repulsionContractBuilder
                    .setLayoutModel(layoutModel)
                    .setFRNodeData(frNodeData)
                    .setRepulsionConstant(repulsionConstant)
                    .setRandom(random)
                    .build();
  }

  public void setAttractionMultiplier(double attraction) {
    this.attractionMultiplier = attraction;
  }

  public void setRepulsionMultiplier(double repulsion) {
    this.repulsionMultiplier = repulsion;
  }

  public void reset() {
    doInit();
  }

  public void initialize() {
    doInit();
  }

  private void doInit() {
    Graph<N> graph = layoutModel.getGraph();
    if (graph != null && graph.nodes().size() > 0) {
      currentIteration = 0;
      temperature = layoutModel.getWidth() / 10;

      forceConstant =
              Math.sqrt(layoutModel.getHeight() * layoutModel.getWidth() / graph.nodes().size());

      attractionConstant = attractionMultiplier * forceConstant;
      repulsionConstant = repulsionMultiplier * forceConstant;
      initialized = true;
    }
  }

  protected double EPSILON = 0.000001D;

  /**
   * Moves the iteration forward one notch, calculation attraction and repulsion between nodes and
   * edges and cooling the temperature.
   */
  public synchronized void step() {

    if (!initialized) {
      doInit();
    }
    Graph<N> graph = layoutModel.getGraph();
    currentIteration++;

    /** Calculate repulsion */
    while (true) {

      try {
        repulsionContract.calculateRepulsion();
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    /** Calculate attraction */
    while (true) {
      try {
        for (EndpointPair<N> endpoints : graph.edges()) {
          calcAttraction(endpoints);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    while (true) {
      try {
        for (N node : graph.nodes()) {
          if (layoutModel.isLocked(node)) {
            continue;
          }
          calcPositions(node);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    cool();
  }

  protected synchronized void calcPositions(N node) {

    Point fvd = getFRData(node);
    if (fvd == null) {
      return;
    }
    Point xyd = layoutModel.apply(node);
    double deltaLength = Math.max(EPSILON, fvd.length());

    double newXDisp = fvd.x / deltaLength * Math.min(deltaLength, temperature);
    double newYDisp = fvd.y / deltaLength * Math.min(deltaLength, temperature);
    double newZDisp = fvd.z / deltaLength * Math.min(deltaLength, temperature);

    xyd = xyd.add(newXDisp, newYDisp, newZDisp);

    double newXPos = xyd.x;
    newXPos = Math.min(Math.max(newXPos, this.xMin), this.xMax);

    double newYPos = xyd.y;
    newYPos = Math.min(Math.max(newYPos, this.yMin), this.yMax);

    double newZPos = xyd.z;
    newZPos = Math.min(Math.max(newZPos, this.zMin), this.zMax);

    xyd = Point.of(newXPos, newYPos, newZPos);
    layoutModel.set(node, xyd);
    //    layoutModel.set(node, newXPos, newYPos, newZPos);
  }

  protected void calcAttraction(EndpointPair<N> endpoints) {
    N node1 = endpoints.nodeU();
    N node2 = endpoints.nodeV();
    boolean v1_locked = layoutModel.isLocked(node1);
    boolean v2_locked = layoutModel.isLocked(node2);

    if (v1_locked && v2_locked) {
      // both locked, do nothing
      return;
    }
    Point p1 = layoutModel.apply(node1);
    Point p2 = layoutModel.apply(node2);
    if (p1 == null || p2 == null) {
      return;
    }
    double xDelta = p1.x - p2.x;
    double yDelta = p1.y - p2.y;
    double zDelta = p1.z - p2.z;

    double deltaLength =
        Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta) + (zDelta * zDelta)));

    double force = (deltaLength * deltaLength) / attractionConstant;

    Preconditions.checkState(
        !Double.isNaN(force), "Unexpected mathematical result in FRLayout:calcPositions [force]");

    double dx = (xDelta / deltaLength) * force;
    double dy = (yDelta / deltaLength) * force;
    double dz = (zDelta / deltaLength) * force;

    if (v1_locked == false) {
      Point fvd1 = getFRData(node1);
      frNodeData.put(node1, fvd1.add(-dx, -dy, -dz));
    }
    if (v2_locked == false) {
      Point fvd2 = getFRData(node2);
      frNodeData.put(node2, fvd2.add(dx, dy, dz));
    }
  }

//  protected void calcRepulsion(N node1) {
//    Point fvd1 = getFRData(node1);
//    if (fvd1 == null) {
//      return;
//    }
//    frNodeData.put(node1, Point.ORIGIN);
//
//    try {
//      for (N node2 : layoutModel.getGraph().nodes()) {
//
//        if (node1 != node2) {
//          fvd1 = getFRData(node1);
//          Point p1 = layoutModel.apply(node1);
//          Point p2 = layoutModel.apply(node2);
//          if (p1 == null || p2 == null) {
//            continue;
//          }
//          double dx = p1.x - p2.x;
//          double dy = p1.y - p2.y;
//          double dz = p1.z - p2.z;
//
//          double dist = Math.max(EPSILON, Math.sqrt((dx * dx) + (dy * dy) + (dz * dz)));
//
//          double force = (repulsion_constant * repulsion_constant) / dist;
//
//          if (Double.isNaN(force)) {
//            throw new RuntimeException(
//                "Unexpected mathematical result in FRLayout:calcPositions [repulsion]");
//          }
//
//          fvd1 = fvd1.add((dx / dist) * force, (dy / dist) * force, (dz / dist) * force);
//          frNodeData.put(node1, fvd1);
//        }
//      }
//    } catch (ConcurrentModificationException cme) {
//      calcRepulsion(node1);
//    }
//  }

  private void cool() {
    temperature *= (1.0 - currentIteration / (double) mMaxIterations);
  }

  public void setMaxIterations(int maxIterations) {
    mMaxIterations = maxIterations;
  }

  protected Point getFRData(N node) {
    return frNodeData.getUnchecked(node);
  }

  /** @return true once the current iteration has passed the maximum count. */
  public boolean done() {
    if (currentIteration > mMaxIterations) { //|| temperature < 1.0 / max_dimension) {
      return true;
    }
    return false;
  }
}
