package edu.uci.ics.jung.layout3d.spatial;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Node in the BarnesHutOctTree. Has a box dimension and a ForceObject that may be either a graph
 * node or a representation of the combined forces of the child nodes. May have 8 child nodes.
 *
 * @author Tom Nelson
 */
public class Node<T> {

  private static final Logger log = LoggerFactory.getLogger(Node.class);
  public static final double THETA = 0.5f;

  // a node contains a ForceObject and possibly 8 Nodes
  protected ForceObject<T> forceObject;

  Node BNW;
  Node BNE;
  Node BSE;
  Node BSW;
  Node FNW;
  Node FNE;
  Node FSE;
  Node FSW;

  private Box area;

  public Node(double x, double y, double z, double width, double height, double depth) {
    this(new Box(x, y, z, width, height, depth));
  }

  public Node(Box r) {
    area = r;
  }

  public ForceObject<T> getForceObject() {
    return forceObject;
  }

  public boolean isLeaf() {
    return BNW == null
        && BNE == null
        && BSE == null
        && BSW == null
        && FNW == null
        && FNE == null
        && FSE == null
        && FSW == null;
  }

  /**
   * insert a new ForceObject into the tree. This changes the combinedMass and the forceVector for
   * any Node that it is inserted into
   *
   * @param element
   */
  public void insert(ForceObject<T> element) {
    //    if (log.isTraceEnabled()) {
    log.debug("insert {} into {}", element, this);
    //    }
    if (forceObject == null) {
      forceObject = element;
      return;
    }
    if (isLeaf()) {
      // there already is a forceObject, so split
      log.trace("must split {}", this);
      split();
      log.trace("into this {} and re-insert {} and {}", this, this.forceObject, element);
      // put the current resident and the new one into the correct quardrants
      insertForceObject(this.forceObject);
      insertForceObject(element);
      // update the centerOfMass, Mass, and Force on this node
      this.forceObject = this.forceObject.add(element);

    } else {
      if (forceObject == element) {
        log.error("can't insert {} into {}", element, this.forceObject);
      }
      // we're already split, update the forceElement for this new element
      forceObject = forceObject.add(element);
      //and follow down the tree to insert
      insertForceObject(element);
    }
  }

  private void insertForceObject(ForceObject<T> forceObject) {
    if (FNW.area.contains(forceObject.p)) {
      FNW.insert(forceObject);
    } else if (FNE.area.contains(forceObject.p)) {
      FNE.insert(forceObject);
    } else if (FSE.area.contains(forceObject.p)) {
      FSE.insert(forceObject);
    } else if (FSW.area.contains(forceObject.p)) {
      FSW.insert(forceObject);
    } else if (BNW.area.contains(forceObject.p)) {
      BNW.insert(forceObject);
    } else if (BNE.area.contains(forceObject.p)) {
      BNE.insert(forceObject);
    } else if (BSE.area.contains(forceObject.p)) {
      BSE.insert(forceObject);
    } else if (BSW.area.contains(forceObject.p)) {
      BSW.insert(forceObject);
    } else {
      log.error("no home for {}", forceObject);
    }
  }

  public Box getBounds() {
    return area;
  }

  public void clear() {
    forceObject = null;
    FNW = FNE = FSW = FSE = BNW = BNE = BSW = BSE = null;
  }

  /*
   * Splits the Quadtree into 4 sub-QuadTrees
   */
  protected void split() {
    if (log.isTraceEnabled()) {
      log.info("splitting {}", this);
    }
    double width = area.width / 2;
    double height = area.height / 2;
    double depth = area.depth / 2;
    double x = area.x;
    double y = area.y;
    double z = area.z;
    FNE = new Node(x + width, y, z + depth, width, height, depth);
    FNW = new Node(x, y, z + depth, width, height, depth);
    FSW = new Node(x, y + height, z + depth, width, height, depth);
    FSE = new Node(x + width, y + height, z + depth, width, height, depth);
    BNE = new Node(x + width, y, z, width, height, depth);
    BNW = new Node(x, y, z, width, height, depth);
    BSW = new Node(x, y + height, z, width, height, depth);
    BSE = new Node(x + width, y + height, z, width, height, depth);
    if (log.isTraceEnabled()) {
      log.trace("after split, this node is {}", this);
    }
  }

  public void visit(ForceObject<T> target, Optional userData) {
    if (this.forceObject == null || target.getElement().equals(this.forceObject.getElement())) {
      return;
    }

    if (isLeaf()) {
      if (log.isTraceEnabled()) {
        log.trace(
            "isLeaf, Node {} at {} visiting {} at {}",
            this.forceObject.getElement(),
            this.forceObject.p,
            target.getElement(),
            target.p);
      }
      target.addForceFrom(this.forceObject, userData);
      log.trace("added force from {} so its now {}", this.forceObject, target);
    } else {
      // not a leaf
      //  this node is an internal node
      //  calculate s/d
      double s = this.area.width;
      //      distance between the incoming node's position and
      //      the center of mass for this node
      double d = this.forceObject.p.distance(target.p);
      if (s / d < THETA) {
        // this node is sufficiently far away
        // just use this node's forces
        if (log.isTraceEnabled()) {
          log.trace(
              "Node {} at {} visiting {} at {}",
              this.forceObject.getElement(),
              this.forceObject.p,
              target.getElement(),
              target.p);
        }
        target.addForceFrom(this.forceObject, userData);
        log.trace("added force from {} so its now {}", this.forceObject, target);

      } else {
        // down the tree we go
        FNW.visit(target, userData);
        FNE.visit(target, userData);
        FSW.visit(target, userData);
        FSE.visit(target, userData);
        BNW.visit(target, userData);
        BNE.visit(target, userData);
        BSW.visit(target, userData);
        BSE.visit(target, userData);
      }
    }
  }

  static String asString(Box r) {
    return "["
        + (int) r.x
        + ","
        + (int) r.y
        + ","
        + (int) r.z
        + ","
        + (int) r.width
        + ","
        + (int) r.height
        + ","
        + (int) r.depth
        + "]";
  }

  static <T> String asString(Node<T> node, String margin) {
    StringBuilder s = new StringBuilder();
    s.append("\n");
    s.append(margin);
    s.append("bounds=");
    s.append(asString(node.getBounds()));
    ForceObject forceObject = node.getForceObject();
    if (forceObject != null) {
      s.append(", forceObject:=");
      s.append(forceObject.toString());
    }
    if (node.FNW != null) s.append(asString(node.FNW, margin + marginIncrement));
    if (node.FNE != null) s.append(asString(node.FNE, margin + marginIncrement));
    if (node.FSW != null) s.append(asString(node.FSW, margin + marginIncrement));
    if (node.FSE != null) s.append(asString(node.FSE, margin + marginIncrement));
    if (node.BNW != null) s.append(asString(node.BNW, margin + marginIncrement));
    if (node.BNE != null) s.append(asString(node.BNE, margin + marginIncrement));
    if (node.BSW != null) s.append(asString(node.BSW, margin + marginIncrement));
    if (node.BSE != null) s.append(asString(node.BSE, margin + marginIncrement));

    return s.toString();
  }

  static String marginIncrement = "   ";

  @Override
  public String toString() {
    return asString(this, "");
  }
}
