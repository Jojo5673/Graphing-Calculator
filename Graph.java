import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.time.Instant;

import RegressionModels.*;

public class Graph {
    private int id;
    private static int nextId = 0;
    private String title;
    private ArrayList<Point2D> points;
    private RegressionModel regression = null;
    private Instant timeStamp;
    private Boolean connect_points = false;

    public Boolean getConnect_points() {return connect_points;}
    public RegressionModel getRegression() {return regression;}
    public ArrayList<Point2D> getPoints() {return points;}
    public String getTitle() {return title;}
    public int getId() {return id;}
    public Instant getTimeStamp() {return timeStamp;}

    public void setConnect_points(Boolean connect_points) {this.connect_points = connect_points;}
    public void setRegression(RegressionModel regression) {this.regression = regression;}
    public void setPoints(ArrayList<Point2D> points) {this.points = points;}
    public void setTitle(String title) {this.title = title;}

    public Graph(String title, ArrayList<Point2D> points) {
        this.title = title;
        this.points = points;
        this.id = nextId++;
        this.timeStamp = Instant.now();
    }

}
