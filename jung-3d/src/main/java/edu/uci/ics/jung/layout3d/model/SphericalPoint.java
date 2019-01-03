package edu.uci.ics.jung.layout3d.model;

import edu.uci.ics.jung.layout3d.spatial.Box;
import edu.uci.ics.jung.layout3d.spatial.Sphere;
import edu.uci.ics.jung.layout3d.util.Spherical;

import java.util.Objects;

/** @author Tom Nelson */
public class SphericalPoint {

  public final double r;
  public final double theta;
  public final double phi;
  public static final SphericalPoint ORIGIN = new SphericalPoint(0, 0, 0);

  public static SphericalPoint of(double x, double y, double z) {
    return new SphericalPoint(x, y, z);
  }

  private SphericalPoint(double r, double theta, double phi) {
    if (Double.isNaN(r) || Double.isNaN(theta) || Double.isNaN(phi)) {
      this.r = 0;
      this.theta = 0;
      this.phi = 0;
      return;
    }
    this.r = r;
    this.theta = theta;
    this.phi = phi;
  }

  public SphericalPoint add(SphericalPoint other) {
    return add(other.x, other.y, other.z);
  }

  public SphericalPoint add(double dx, double dy, double dz) {
    return new SphericalPoint(x + dx, y + dy, z + dz);
  }

  public double distanceSquared(SphericalPoint other) {
    return distanceSquared(other.x, other.y, other.z);
  }

  public double distanceSquared(double ox, double oy, double oz) {
    double dx = x - ox;
    double dy = y - oy;
    double dz = z - oz;
    return dx * dx + dy * dy + dz * dz;
  }

  public boolean inside(Sphere c) {
    //  fast-fail bounds check
    if (!inside(
        c.center.x - c.radius,
        c.center.y - c.radius,
        c.center.z - c.radius,
        c.center.x + c.radius,
        c.center.y + c.radius,
        c.center.z + c.radius)) {
      return false;
    }
    return c.center.distance(this) <= c.radius;
  }

  public boolean inside(Box r) {
    return inside(r.x, r.y, r.z, r.maxX, r.maxY, r.maxZ);
  }

  public boolean inside(
      double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    if (x < minX || maxX < x || y < minY || maxY < y || z < minZ || maxZ < z) {
      return false;
    }
    return true;
  }

  public double length() {
    return r;
  }

  public double distance(SphericalPoint other) {
    return Math.sqrt(distanceSquared(other));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SphericalPoint)) {
      return false;
    }

    SphericalPoint other = (SphericalPoint) o;

    return (Double.compare(other.x, x) == 0
        && Double.compare(other.y, y) == 0
        && Double.compare(other.z, z) == 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, z);
  }

  @Override
  public String toString() {
    return "Point{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
  }

  public static SphericalPoint fromCartesian(Point p) {
    double r = Math.sqrt(p.x*p.x + p.y*p.y + p.x*p.x);
    return SphericalPoint.of(
            r,
            Math.atan2(p.y,p.x),
            Math.acos(p.z/r)
    );

  }
  public static Point toCartesian(SphericalPoint sp) {
    return Point.of(
            sp.r*Math.sin(sp.theta)*Math.cos(sp.phi),
            sp.r*Math.sin(sp.theta)*Math.sin(sp.phi),
            sp.r*Math.cos(sp.theta)
    );

  }
}
