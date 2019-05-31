/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */

package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.util.TreeUtils;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Karlheinz Toni
 * @author Tom Nelson - converted to jung2, refactored into Algorithm/Visitor
 */
public class TreeLayoutAlgorithm<N> implements LayoutAlgorithm<N> {

  private static final Logger log =
      LoggerFactory.getLogger(edu.uci.ics.jung.layout3d.algorithms.TreeLayoutAlgorithm.class);

  public static class Builder<N> {
    private int xNodeSpacing = DEFAULT_X_NODE_SPACING;
    private int yNodeSpacing = DEFAULT_Y_NODE_SPACING;
    private int zNodeSpacing = DEFAULT_Z_NODE_SPACING;

    public Builder withXNodeSpacing(int xNodeSpacing) {
      Preconditions.checkArgument(xNodeSpacing > 0, "xNodeSpacing must be positive");
      this.xNodeSpacing = xNodeSpacing;
      return this;
    }

    public Builder withYNodeSpacing(int yNodeSpacing) {
      Preconditions.checkArgument(yNodeSpacing > 0, "yNodeSpacing must be positive");
      this.yNodeSpacing = yNodeSpacing;
      return this;
    }

    public Builder withZNodeSpacing(int zNodeSpacing) {
      Preconditions.checkArgument(zNodeSpacing > 0, "zNodeSpacing must be positive");
      this.zNodeSpacing = zNodeSpacing;
      return this;
    }

    public TreeLayoutAlgorithm<N> build() {
      return new TreeLayoutAlgorithm(this);
    }
  }

  public static Builder builder() {
    return new Builder<>();
  }

  protected TreeLayoutAlgorithm(Builder<N> builder) {
    this(builder.xNodeSpacing, builder.yNodeSpacing, builder.zNodeSpacing);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   *
   * @param xNodeSpacing the x spacing between adjacent siblings
   * @param yNodeSpacing the y spacing between adjacent siblings
   * @param zNodeSpacing the z spacing between adjacent siblings
   */
  private TreeLayoutAlgorithm(int xNodeSpacing, int yNodeSpacing, int zNodeSpacing) {
    this.xNodeSpacing = xNodeSpacing;
    this.yNodeSpacing = yNodeSpacing;
    this.zNodeSpacing = zNodeSpacing;
  }

  protected Map<N, Integer> basePositions = new HashMap<>();

  protected transient Set<N> alreadyDone = new HashSet<N>();

  /** The default x node spacing. Initialized to 50. */
  protected static final int DEFAULT_X_NODE_SPACING = 50;

  /** The default y node spacing. Initialized to 50. */
  protected static final int DEFAULT_Y_NODE_SPACING = 50;

  /** The default z node spacing. Initialized to 50. */
  protected static final int DEFAULT_Z_NODE_SPACING = 50;

  /** The horxizontal node spacing. Defaults to {@code DEFAULT_X_NODE_SPACING}. */
  protected int xNodeSpacing = DEFAULT_X_NODE_SPACING;

  /** The y node spacing. Defaults to {@code DEFAULT_Y_NODE_SPACING}. */
  protected int yNodeSpacing = DEFAULT_Y_NODE_SPACING;

  /** The z node spacing. Defaults to {@code DEFAULT_Z_NODE_SPACING}. */
  protected int zNodeSpacing = DEFAULT_Z_NODE_SPACING;

  protected double currentX;
  protected double currentY;
  protected double currentZ;

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    buildTree(layoutModel);
  }

  protected void buildTree(LayoutModel<N> layoutModel) {
    alreadyDone = Sets.newHashSet();
    this.currentX = 0;
    this.currentY = 0;
    this.currentZ = 0;
    Set<N> roots = TreeUtils.roots(layoutModel.getGraph());
    Preconditions.checkArgument(roots.size() > 0);
    // the width of the tree under 'roots'. Includes one 'xNodeSpacing' per child node
    int overallWidth = calculateOverallWidth(layoutModel, roots);
    // add one additional 'xNodeSpacing' for each tree (each root)
    overallWidth += (roots.size() + 1) * xNodeSpacing;
    int overallHeight = calculateOverallHeight(layoutModel, roots);
    overallHeight += 2 * yNodeSpacing;
    int overallDepth = overallWidth;
    layoutModel.setSize(
        Math.max(layoutModel.getWidth(), overallWidth),
        Math.max(layoutModel.getHeight(), overallHeight),
        Math.max(layoutModel.getDepth(), overallDepth));
    // for every root in the forest or tree, descend and layout a circle of children in
    // the x/z plane, centered at the x,y_spacing,z of the root
    for (N root : roots) {
      calculateLocalWidth(layoutModel, root);
      // pass down the center and radius for the circle
      currentX += (this.basePositions.get(root) / 2 + this.xNodeSpacing);
      currentZ = currentX;
      buildTree(layoutModel, root, (int) currentX);
    }
  }

  protected void buildTree(LayoutModel<N> layoutModel, N node, int x) {
    if (alreadyDone.add(node)) {
      //go one level further down
      double newY = this.currentY + this.yNodeSpacing;
      this.currentX = x;
      this.currentY = newY;
      layoutModel.set(node, currentX, currentY, currentZ);

      int sizeXofCurrent = basePositions.get(node);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for (N element : layoutModel.getGraph().successors(node)) {
        sizeXofChild = this.basePositions.get(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(layoutModel, element, startXofChild);

        lastX = lastX + sizeXofChild + xNodeSpacing;
      }

      this.currentY -= this.yNodeSpacing;
    }
  }

  private int calculateLocalWidth(LayoutModel<N> layoutModel, N node) {

    int localWidth = 0;
    for (N element : layoutModel.getGraph().successors(node)) {
      localWidth += calculateLocalWidth(layoutModel, element) + xNodeSpacing;
    }
    localWidth = Math.max(0, localWidth - xNodeSpacing);
    basePositions.put(node, localWidth);

    return localWidth;
  }

  private int calculateOverallWidth(LayoutModel<N> layoutModel, Collection<N> roots) {

    int overallWidth = 0;
    for (N node : roots) {
      overallWidth += calculateLocalWidth(layoutModel, node);
    }

    return overallWidth;
  }

  private int calculateLocalHeight(LayoutModel<N> layoutModel, N node) {

    int localHeight = 0;
    for (N element : layoutModel.getGraph().successors(node)) {
      localHeight =
          Math.max(localHeight, calculateLocalHeight(layoutModel, element) + yNodeSpacing);
    }
    return localHeight;
  }

  private int calculateOverallHeight(LayoutModel<N> layoutModel, Collection<N> roots) {

    int overallHeight = 0;
    for (N node : roots) {
      overallHeight += calculateLocalHeight(layoutModel, node);
    }
    return overallHeight;
  }

  /** @return the center of this layout's area. */
  public Point getCenter(LayoutModel<N> layoutModel) {
    return Point.of(
        layoutModel.getWidth() / 2, layoutModel.getHeight() / 2, layoutModel.getDepth() / 2);
  }
}
