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
import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout3d.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.algorithms.ISOMLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.algorithms.KKLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout3d.algorithms.SphereLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.algorithms.SpringLayoutAlgorithm;
import edu.uci.ics.jung.layout3d.algorithms.repulsion.BarnesHutFRRepulsion;
import edu.uci.ics.jung.layout3d.algorithms.repulsion.BarnesHutSpringRepulsion;
import edu.uci.ics.jung.layout3d.util.LayoutAlgorithmTransition;
import edu.uci.ics.jung.visualization3d.VisualizationViewer;
import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class GraphDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(GraphDemo.class);

  enum Layouts {
    SPRING,
    SPRING_BH,
    FRLAYOUT,
    FRBHLAYOUT,
    KK,
    ISOM,
    SPHERE;
  }

  enum Graphs {
    DEMO,
    ONECOMPONENT,
    CHAIN,
    TEST,
    ISOLATES,
    DAG;
  }

  public GraphDemo() {
    super(new BorderLayout());

    JPanel vvHolder = new JPanel();
    vvHolder.setLayout(new GridLayout(1, 1));

    JComboBox<Layouts> layoutNameComboBox = new JComboBox(Layouts.values());
    JComboBox<Graphs> graphNameComboBox = new JComboBox(Graphs.values());
    graphNameComboBox.setSelectedIndex(1);
    JPanel flowPanel = new JPanel();
    flowPanel.add(layoutNameComboBox);
    flowPanel.add(graphNameComboBox);

    Network network = getNetworkFromSelectedItem((Graphs) graphNameComboBox.getSelectedItem());
    VisualizationViewer vv = new VisualizationViewer(network);

    layoutNameComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED)
            SwingUtilities.invokeLater(
                () ->
                    LayoutAlgorithmTransition.animate(
                        vv, getLayoutAlgorithmfromSelectedItem((Layouts) e.getItem())));
        });

    graphNameComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            SwingUtilities.invokeLater(
                () -> vv.setNetwork(getNetworkFromSelectedItem((Graphs) e.getItem())));
          }
        });

    LayoutAlgorithm layoutAlgorithm =
        getLayoutAlgorithmfromSelectedItem((Layouts) layoutNameComboBox.getSelectedItem());
    vv.setLayoutAlgorithm(layoutAlgorithm);

    vvHolder.add(vv);
    add(vvHolder);
    add(flowPanel, BorderLayout.SOUTH);
  }

  private Network getNetworkFromSelectedItem(Graphs selected) {
    Network network;
    switch (selected) {
      case DEMO:
        network = TestGraphs.getDemoGraph();
        break;
      case ONECOMPONENT:
        network = TestGraphs.getOneComponentGraph();
        break;
      case DAG:
        network = TestGraphs.createDirectedAcyclicGraph(4, 10, 0.3);
        break;
      case TEST:
        network = TestGraphs.createTestGraph(false);
        break;
      case CHAIN:
        network = TestGraphs.createChainPlusIsolates(20, 4);
        break;
      case ISOLATES:
        network = TestGraphs.createChainPlusIsolates(0, 200);
        break;
      default:
        network = TestGraphs.getDemoGraph();
    }
    return network;
  }

  private LayoutAlgorithm getLayoutAlgorithmfromSelectedItem(Layouts selected) {
    LayoutAlgorithm layoutAlgorithm;
    switch (selected) {
      case SPRING:
        layoutAlgorithm = SpringLayoutAlgorithm.builder().build();
        break;
      case SPRING_BH:
        layoutAlgorithm = SpringLayoutAlgorithm.builder().setRepulsionContractBuilder(BarnesHutSpringRepulsion.barnesHutBuilder()).build();
        break;
      case SPHERE:
        layoutAlgorithm = new SphereLayoutAlgorithm();
        break;
      case FRLAYOUT:
        layoutAlgorithm = FRLayoutAlgorithm.builder().build();
                //new FRLayoutAlgorithm();
        break;
      case FRBHLAYOUT:
        layoutAlgorithm = FRLayoutAlgorithm.builder().setRepulsionContractBuilder(BarnesHutFRRepulsion.barnesHutBuilder()).build();
        break;
      case KK:
        layoutAlgorithm = KKLayoutAlgorithm.builder().build();
        break;
      case ISOM:
        layoutAlgorithm = ISOMLayoutAlgorithm.builder().build();
        break;
      default:
        log.warn("using default layout algorithm");
        layoutAlgorithm = SpringLayoutAlgorithm.builder().build();
    }
    return layoutAlgorithm;
  }

  public static void main(String argv[]) {
    final GraphDemo demo = new GraphDemo();
    JFrame f = new JFrame();
    f.add(demo);
    f.setSize(600, 600);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setVisible(true);
  }
}
