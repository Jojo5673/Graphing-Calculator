import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.time.Instant;

import RegressionModels.*;

public class Graph {
    private String id;
    private String title;
    private ArrayList<Point2D.Double> points;
    private transient RegressionModel regression = null;   //marked transient to avoid showing
    private String modelName = "";
    private Instant timeStamp;
    private Boolean connect_points = false;
    private String imagePath = "";

    public Graph() {}

    public Boolean isConnect_points() {return connect_points;}
    public RegressionModel getRegression() {return regression;}
    public ArrayList<Point2D.Double> getPoints() {return points;}
    public String getTitle() {return title;}
    public String getId() {return id;}
    public Instant getTimeStamp() {return timeStamp;}
    public String getImagePath() {return imagePath;}

    public void setImagePath(String imagePath) {this.imagePath = imagePath;}
    public void setConnect_points(Boolean connect_points) {this.connect_points = connect_points;}
    public void setPoints(ArrayList<Point2D.Double> points) {this.points = points;}
    public void setTitle(String title) {this.title = title;}
    public void setRegression(RegressionModel regression) {
        this.regression = regression;
        this.modelName = regression.getModelName();
    }

    public Graph(String title, ArrayList<Point2D.Double> points) {
        this.title = title;
        this.points = points;
        GenerateID();
        this.timeStamp = Instant.now();
    }

    private void GenerateID(){
        String base = title.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9\\-]", "");
        id = base + "-" + Instant.now().toEpochMilli();
    }
    public void LoadRegression() {
        switch (modelName) {
            case "Sinusoidal":
                regression = new SinusoidalRegression(points);
                break;
            case "Linear":
                regression = new PolynomialRegression(points, 1);
                break;
            case "Quadratic":
                regression = new PolynomialRegression(points, 2);
                break;
            case "Cubic":
                regression = new PolynomialRegression(points, 3);
                break;
            case "Quartic":
                regression = new PolynomialRegression(points, 4);
                break;
            case "Exponential":
                regression = new ExponentialRegression(points);
                break;
            case "Logarithmic":
                regression = new LogarithmicRegression(points);
                break;
            case "Power":
                regression = new PowerRegression(points);
                break;
            case "Logistic":
                break;
            default:
                System.out.println("Unknown model name");
        }
    }
}
