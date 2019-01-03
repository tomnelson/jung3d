package edu.uci.ics.jung.layout3d;

import edu.uci.ics.jung.layout3d.model.LayoutModel;
import edu.uci.ics.jung.layout3d.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout3d.model.Point;
import edu.uci.ics.jung.layout3d.util.Spherical;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

public class SphereTest {

    @Test
    public void makeTest() {
        LayoutModel<String> layoutModel = LoadingCacheLayoutModel.<String>builder().withSize(100,100,100).build();
        Collection<String> nodes = new ArrayList<>();
        for (int i=0; i<4000; i++) {
            nodes.add("node"+i);
        }
        Spherical.distribute(layoutModel, nodes, Point.ORIGIN, 1000);

    }
}
