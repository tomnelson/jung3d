/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 *
 */
package edu.uci.ics.jung.samples;

/** */
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.layout3d.algorithms.BalloonLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.visualization3d.VisualizationViewer;
import java.awt.*;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class TreeDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(TreeDemo.class);

  MutableCTreeNetwork<Object, Object> tree;

  public TreeDemo() {
    super(new BorderLayout());

    this.tree = this.createTree();
    //        growTree();

    JPanel vvHolder = new JPanel();
    vvHolder.setLayout(new GridLayout(1, 1));

    JPanel flowPanel = new JPanel();
    add(vvHolder);

    addVisualization(vvHolder);
    add(flowPanel, BorderLayout.SOUTH);
  }

  private void addVisualization(JPanel parent) {

    LayoutAlgorithm layoutAlgorithm = new BalloonLayoutAlgorithm();

    VisualizationViewer vv = new VisualizationViewer(tree, layoutAlgorithm);
    vv.getRenderContext().setVertexStringer(Object::toString);

    vv.setLayoutAlgorithm(layoutAlgorithm);
    //    vv.init();

    for (Component component : parent.getComponents()) {
      if (component instanceof VisualizationViewer) {
        LayoutModel layoutModel = ((VisualizationViewer) component).getLayoutModel();
        //        Relaxer relaxer = layoutModel.getRelaxer();
        //        if (relaxer != null) {
        //          relaxer.stop();
        //        }
      }
    }
    parent.add(vv);
    vv.revalidate();
  }

  private MutableCTreeNetwork<Object, Object> growTree() {
    int count = 0;
    String[] nodes = {
      "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s"
    };
    MutableCTreeNetwork<Object, Object> tree =
        TreeNetworkBuilder.builder().expectedNodeCount(50).build();

    tree.addNode("root");

    for (String c : nodes) {
      tree.addEdge("root", c, count++);
      for (String d : nodes) {
        String end = d + count;
        tree.addEdge(c, "" + end, count++);
        for (String b : nodes) {
          tree.addEdge(end, "" + b + b + count, count++);
        }
      }
    }
    //    log.info("returned tree");
    return tree;
  }

  private MutableCTreeNetwork<Object, Object> createTree() {
    MutableCTreeNetwork<Object, Object> tree =
        TreeNetworkBuilder.builder().expectedNodeCount(50).build();

    tree.addNode("root");

    int edgeId = 0;
    tree.addEdge("root", "V0", edgeId++);
    tree.addEdge("V0", "V1", edgeId++);
    tree.addEdge("V0", "V2", edgeId++);
    tree.addEdge("V0", "V4", edgeId++);
    tree.addEdge("V0", "V3", edgeId++);
    tree.addEdge("V0", "V5", edgeId++);
    tree.addEdge("V0", "V6", edgeId++);
    tree.addEdge("V0", "V7", edgeId++);
    tree.addEdge("V0", "V8", edgeId++);
    tree.addEdge("V0", "V9", edgeId++);
    tree.addEdge("V0", "V10", edgeId++);

    tree.addEdge("root", "A0", edgeId++);
    tree.addEdge("A0", "A1", edgeId++);
    tree.addEdge("A0", "A2", edgeId++);
    tree.addEdge("A0", "A3", edgeId++);
    tree.addEdge("A0", "A4", edgeId++);
    tree.addEdge("A0", "A5", edgeId++);
    tree.addEdge("A0", "A6", edgeId++);
    tree.addEdge("A0", "A7", edgeId++);
    tree.addEdge("A0", "A8", edgeId++);
    tree.addEdge("A0", "A9", edgeId++);
    tree.addEdge("A0", "A10", edgeId++);
    tree.addEdge("A0", "A20", edgeId++);
    tree.addEdge("A0", "A30", edgeId++);
    tree.addEdge("A0", "A40", edgeId++);
    tree.addEdge("A0", "A50", edgeId++);
    tree.addEdge("A0", "A60", edgeId++);
    tree.addEdge("A0", "A70", edgeId++);
    tree.addEdge("A0", "A80", edgeId++);
    tree.addEdge("A0", "A90", edgeId++);
    tree.addEdge("A0", "A91", edgeId++);
    tree.addEdge("A0", "A92", edgeId++);
    tree.addEdge("A0", "A93", edgeId++);
    tree.addEdge("A0", "A94", edgeId++);
    tree.addEdge("A0", "A95", edgeId++);
    tree.addEdge("A0", "A96", edgeId++);
    tree.addEdge("A0", "A97", edgeId++);
    tree.addEdge("A0", "A98", edgeId++);
    tree.addEdge("A0", "A99", edgeId++);
    tree.addEdge("A0", "A100", edgeId++);

    tree.addEdge("root", "B0", edgeId++);
    tree.addEdge("B0", "B1", edgeId++);
    tree.addEdge("B0", "B2", edgeId++);
    tree.addEdge("B0", "B4", edgeId++);
    tree.addEdge("B0", "B3", edgeId++);
    tree.addEdge("B0", "B5", edgeId++);
    tree.addEdge("B0", "B6", edgeId++);
    tree.addEdge("B0", "B7", edgeId++);
    tree.addEdge("B0", "B8", edgeId++);
    tree.addEdge("B0", "B9", edgeId++);

    tree.addEdge("root", "C0", edgeId++);
    tree.addEdge("C0", "C1", edgeId++);
    tree.addEdge("C0", "C2", edgeId++);
    tree.addEdge("C0", "C4", edgeId++);
    tree.addEdge("C0", "C3", edgeId++);
    tree.addEdge("C0", "C5", edgeId++);
    tree.addEdge("C0", "C6", edgeId++);
    tree.addEdge("C0", "C7", edgeId++);
    tree.addEdge("C0", "C8", edgeId++);
    tree.addEdge("C0", "C9", edgeId++);

    tree.addEdge("root", "D0", edgeId++);
    tree.addEdge("D0", "D1", edgeId++);
    tree.addEdge("D0", "D2", edgeId++);
    tree.addEdge("D0", "D4", edgeId++);
    tree.addEdge("D0", "D3", edgeId++);
    tree.addEdge("D0", "D5", edgeId++);
    tree.addEdge("D0", "D6", edgeId++);
    tree.addEdge("D0", "D7", edgeId++);
    tree.addEdge("D0", "D8", edgeId++);
    tree.addEdge("D0", "D9", edgeId++);

    return tree;
  }

  public static void main(String argv[]) {
    final TreeDemo demo = new TreeDemo();
    JFrame f = new JFrame();
    f.add(demo);
    f.setSize(600, 600);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
  }
}
