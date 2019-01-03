package edu.uci.ics.jung.layout3d.util;

import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/** @author Tom Nelson */
public interface Spherical {
  Map<Point, Integer> getSphereLocations();

  //https://www.cmu.edu/biolphys/deserno/pdf/sphere_equi.pdf
  static <N> void distribute(LayoutModel<N> layoutModel, Collection<N> nodes, Point center, double radius) {
    int ncount = 0;
    Iterator<N> iterator = nodes.iterator();
    double a = 4*Math.PI*radius*radius / nodes.size();
    double d = Math.sqrt(a);
    double mtheta = Math.round(Math.PI / d);
    if (mtheta == 0) mtheta = 1;
    double dtheta = Math.PI / mtheta;
    double dphi = a / dtheta;
    for (int m=0; m<mtheta; m++) {
      double theta = Math.PI * (m  + 0.5) / mtheta;
      double mphi = Math.round(2*Math.PI*Math.sin(theta)/dphi);
      if (mphi == 0) mphi = 1;
      for (int n=0; n < mphi; n++) {
        double phi = 2 * Math.PI * n / mphi;
        Point p = Point.of(
                center.x + radius*Math.sin(theta)*Math.cos(phi),
                center.y + radius * Math.sin(theta)*Math.sin(phi),
                center.z + radius*Math.cos(theta)
        );
        layoutModel.set(iterator.next(), p);
      }
    }
  }

}
