package edu.uci.ics.jung.layout3d.util;

import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;

import java.util.Collection;

public interface Circular {

    /**
     * distribute in a circle in the x,z plane
     * @param layoutModel
     * @param nodes
     * @param center
     * @param radius
     * @param <N>
     */
    static <N> void distributeXZ(
            LayoutModel<N> layoutModel, Collection<N> nodes, Point center, double radius) {

        double width = layoutModel.getWidth();
        double depth = layoutModel.getDepth();
        if (radius <= 0) {
            radius = 0.45 * (depth < width ? depth : width);
        }

        int i = 0;
        for (N node : nodes) {

            double angle = (2 * Math.PI * i) / nodes.size();

            double posX = Math.cos(angle) * radius + width / 2;
            double posZ = Math.sin(angle) * radius + depth / 2;
            double posY = center.y;
            layoutModel.set(node, Point.of(posX, posY, posZ).add(center));
            i++;
        }
    }

    /**
     * distribute in a circle in the x,y plane
     * @param layoutModel
     * @param nodes
     * @param center
     * @param radius
     * @param <N>
     */
    static <N> void distributeXY(
            LayoutModel<N> layoutModel, Collection<N> nodes, Point center, double radius) {

        double width = layoutModel.getWidth();
        double height = layoutModel.getHeight();
        if (radius <= 0) {
            radius = 0.45 * (height < width ? height : width);
        }

        int i = 0;
        for (N node : nodes) {

            double angle = (2 * Math.PI * i) / nodes.size();

            double posX = Math.cos(angle) * radius + width / 2;
            double posY = Math.sin(angle) * radius + height / 2;
            double posZ = center.z;
            layoutModel.set(node, Point.of(posX, posY, posZ).add(center));
            i++;
        }
    }

}
