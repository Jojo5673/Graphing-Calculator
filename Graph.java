import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.time.Instant;

import RegressionModels.*;

public class Graph {
    private int id;
    private static int nextId = 0;
    private String title;
    private ArrayList<Point2D> points;
    private RegressionModel regression;
    private Instant timeStamp;

    public Graph(String title, ArrayList<Point2D> points, RegressionModel regression) {
        this.title = title;
        this.points = points;
        this.regression = regression;
        this.id = nextId++;
        this.timeStamp = Instant.now();
    }
}
