package edu.uci.ics.jung.layout3d.util;

import edu.uci.ics.jung.layout3d.model.Point;
import java.util.Map;

/** @author Tom Nelson */
public interface Spherical {
  Map<Point, Integer> getSphereLocations();
}
