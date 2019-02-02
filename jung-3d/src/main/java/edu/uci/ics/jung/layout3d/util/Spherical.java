package edu.uci.ics.jung.layout3d.util;

import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/** @author Tom Nelson */
public interface Spherical {

  Map<Point, Integer> getSphereLocations();

  static <N> void distribute(LayoutModel<N> layoutModel, Collection<N> nodes, Point center, double radius) {
    int NN = nodes.size();
    double dlong = Math.PI * (3 - Math.sqrt(5));
    double dz = 2.0 / NN;
    double lng = 0;
    double z = 1 - dz/2;
    int k=0;
    for (N node : nodes) {
      double r = Math.sqrt(1 - z*z);
      layoutModel.set(node, Point.of(radius*Math.cos(lng)*r, radius*Math.sin(lng)*r, radius*z).add(center));
      z = z - dz;
      lng = lng + dlong;
    }
  }
}
