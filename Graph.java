import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.time.Instant;
import java.util.Random;
import java.util.Set;

import RegressionModels.*;

/**
 * Represents a graph containing a set of points, a regression model, and metadata.
 * This class handles storing, identifying, and reconstructing graphs for later use.
 */
public class Graph {
    private String id; //unique hash generated to help with searching
    private String title;
    private ArrayList<Point2D.Double> points;
    private transient RegressionModel regression;   //marked transient to avoid showing up in json file (unable to store regression models)
    private String modelName = "";
    private Instant timeStamp; //stores the instant that the graph was created to help with sorting
    private Boolean connect_points = false; //used to determine whether to connect the points when rendering the graph
    private String imagePath = ""; //stores the path of its exported image for use in the graph inventory manager display

    public Graph() {} //no arguments constructor for serialization
    //i.e. to store it in json, this is necessary so that the graph class can be built as the json storing functions cant send it arguments
    //they instead use its getters and setters to get and load data

   //getters
    public Boolean isConnect_points() {return connect_points;}
    public RegressionModel getRegression() {return regression;}
    public ArrayList<Point2D.Double> getPoints() {return points;}
    public String getTitle() {return title;}
    public String getId() {return id;}
    public Instant getTimeStamp() {return timeStamp;}
    public String getImagePath() {return imagePath;}

    //setters
    public void setImagePath(String imagePath) {this.imagePath = imagePath;}
    public void setConnect_points(Boolean connect_points) {this.connect_points = connect_points;}
    public void setPoints(ArrayList<Point2D.Double> points) {this.points = points;}
    public void setTitle(String title) {this.title = title;}

    /**
     * Sets the regression model and updates the model name for future deserialization.
     * @param regression the regression model to apply to the graph
     */
    public void setRegression(RegressionModel regression) {
        this.regression = regression;
        this.modelName = regression.getModelName();
    }

    /**
     * Constructs a new Graph object with the given title and points.
     * Generates a unique ID and records the current timestamp.
     * @param title the title of the graph
     * @param points the 2D points to be plotted
     */
    public Graph(String title, ArrayList<Point2D.Double> points) {
        this.title = title;
        this.points = points;
        regression = new None();
        GenerateID();
        this.timeStamp = Instant.now();
    }

    /**
     * Generates a unique ID for the graph using a random 5-digit number.
     * Ensures that the ID is not already in use.
     */
    private void GenerateID() {
        Set<String> existingIds = GraphManager.getExistingGraphIds(); // You'd need to implement this

        Random rand = new Random();
        String newId;
        do {
            int randomId = 10000 + rand.nextInt(90000);
            newId = String.valueOf(randomId);
        } while (existingIds.contains(newId));

        id = newId;
    }


    /**
     * Loads the appropriate regression model based on the stored model name.
     * This is used when reconstructing a graph from a saved JSON file.
     */
    public void LoadRegression() {
        switch (modelName) {
            case "None":
                regression = new None();
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
                regression = new LogisitcRegression(points);
                break;
            default:
                System.out.println("Unknown model name");
        }
    }
}
