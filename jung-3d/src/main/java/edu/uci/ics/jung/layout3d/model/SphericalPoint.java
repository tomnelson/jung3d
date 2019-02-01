package edu.uci.ics.jung.layout3d.model;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SphericalPoint)) {
      return false;
    }

    SphericalPoint other = (SphericalPoint) o;

    return (Double.compare(other.r, r) == 0
        && Double.compare(other.theta, theta) == 0
        && Double.compare(other.phi, phi) == 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(r, theta, phi);
  }

  @Override
  public String toString() {
    return "Point{" + "r=" + r + ", theta=" + theta + ", phi=" + phi + '}';
  }

  public static SphericalPoint fromCartesian(Point p) {
    double r = Math.sqrt(p.x * p.x + p.y * p.y + p.x * p.x);
    return SphericalPoint.of(r, Math.atan2(p.y, p.x), Math.acos(p.z / r));
  }

  public static Point toCartesian(SphericalPoint sp) {
    return Point.of(
        sp.r * Math.sin(sp.theta) * Math.cos(sp.phi),
        sp.r * Math.sin(sp.theta) * Math.sin(sp.phi),
        sp.r * Math.cos(sp.theta));
  }
}
