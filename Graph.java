import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.time.Instant;

import RegressionModels.*;

//object class for a Graph that stores information for it and useful functions
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
    public void setRegression(RegressionModel regression) {
        this.regression = regression;
        this.modelName = regression.getModelName();
    }

    //constructor we use
    //its gets the title and points, generates its ID, and stores when the graph was created
    public Graph(String title, ArrayList<Point2D.Double> points) {
        this.title = title;
        this.points = points;
        regression = new None();
        GenerateID();
        this.timeStamp = Instant.now();
    }

    //used to generate a unique code for each graph based on their title and when they were created
    //format: graph-title-345698708263
    private void GenerateID(){
        //removes all special characters from the string and replaces whitespaces with "-"
        String base = title.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9\\-]", "");
        //creates the appends the time to the title to create the id
        id = base + "-" + Instant.now().toEpochMilli();
    }

    //used for when the graph is being loaded from storage since we cant store the regression classes in the file
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
                break;
            default:
                System.out.println("Unknown model name");
        }
    }
}
