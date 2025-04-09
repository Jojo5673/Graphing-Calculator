import RegressionModels.*;
import com.google.gson.Gson;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GraphManager {
    public static String JsonfilePath = "files/graphs.json";
    //keeps the data file's path for access anywhere

    public static void main(String[] args) {
    //everything here right now is just for testing
    //its likely this class won't even be the class with the main function but below is just a demonstration of how things should work
    //the entire purpose of this class right now is to hold readGraphs and WriteGraphs methods
    // however, it is open to manage anything with the graphs later in the future
    //more than likely this class will be used to create graphs and deal with all their data though

        //TO BE IMPLEMENTED: getting graph data, regression, and connect points flag from the ui

        //DEMONSTRATION OF LOADING DATA INTO THE GRAPHS.
        //we need to get this data from the user interface later
        ArrayList<Point2D.Double> data = new ArrayList<>();
        data.add(new Point2D.Double(1, 5));
        data.add(new Point2D.Double(2, 3));
        data.add(new Point2D.Double(3, 5));
        data.add(new Point2D.Double(4, 7));
        data.add(new Point2D.Double(5, 11));

        ArrayList<Point2D.Double> data2 = new ArrayList<>();
        data2.add(new Point2D.Double(1, 4));
        data2.add(new Point2D.Double(2, 4));
        data2.add(new Point2D.Double(4, 5));
        data2.add(new Point2D.Double(6.5, 7));
        data2.add(new Point2D.Double(7, 11));

        //DEMONSTRATION OF CONSTRUCTING GRAPHS AND SETTING THEIR PROPERTIES
        // Their data needs to be initialised and packed into an arraylist of points
        // The must be constructed with a title and said data
        // the setREgression and setConnect_points will be utilised in the Graph Screen when the user is editing the graphs
        Graph graph = new Graph("Test Graph", data);
        Graph graph2 = new Graph("Test Graph2", data2);
        graph2.setRegression(new PolynomialRegression(data2, 2));
        graph.setConnect_points(true);
        graph.setRegression(new SinusoidalRegression(data));

        //DEMONSTRATION OF FILE READ/WRITE WITH GRAPHS
        //they are written by passing an arraylist of any amount of graphs to writeGraphs()
        //they are read in arraylists as well
        ArrayList<Graph> graphs = new ArrayList<>();
        graphs.add(graph);
        graphs.add(graph2);

        //writing graphs to file
        try {
            writeGraphs(graphs);
        }catch (IOException e){System.out.println("Unable to write graphs");}

        //reading graphs from the file
        //read graphs returns an arraylist of graphs so they need to be accessed with .get() or with a for loop
        try{
            for (Graph g : readGraphs())
                GraphScreen.plot(g); //this is the function to display graphs by the way
        }catch (IOException e){System.out.println("Unable to read to file");}
    }

    //it's important to clarify why all this has been done to store the file in a json
    //json stores key value pairs kind of like a hashmap or dictionary
    //This means that our file data is going to be extremely human-readable which is good for debuggin
    //Also using the gson library allows us to outsource all the hard work of parsing our custom Graph object when reading its data from a file
    //a text file means we'd have to read straight text and implement the logic to handle all of our data (nobody wants to do this)
    //other file methods are lowk too much work to learn so here we are

    //function that handles loading the graphs stored in the file
    public static ArrayList<Graph> readGraphs() throws IOException {
        //json handling is done by the Gson library (one of the external packages we imported)
        //we first create a json manager (an instance of Gson) and register the type adapter for our Instant class
        //the type adapter tells the json manager how to handle the Instant class (stores our graph timestamp)
        //setPrettyPrinting means everything wont go in one line and it will get formatted nice and pretty
        Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).setPrettyPrinting().create();
        //we read the data from the file into a string and gson parses this string and loads our data into a list of graphs
        String json = Files.readString(Paths.get(JsonfilePath));
        Type listType = new TypeToken<ArrayList<Graph>>(){}.getType(); //this tells gson what type of object we are lokoing for (Graph)
        ArrayList<Graph>graphs = gson.fromJson(json, listType); //we use the type we defined and the json string to load everything
        for (Graph g : graphs) {
            g.LoadRegression(); //this sets the regression based on the regression model's name stored for the graph.
            //we are unable to store regression models. they are a bit too complex and writing logic to make Gson parse it is too much work
        }

        //code to clean up unused images
        //graphs only store their image path so when a graph gets deleted or replaced in the file, its image stays
        //this code checks for the used image paths and deletes the garbage
        File folder = new File("files/images"); //gets the images folder
        List<String> usedImages = graphs.stream().map(Graph::getImagePath).toList(); //creates a list of the stored image paths
        //loops through the images in the folder and if it was not in the list of used images it gets packed up
        for (File image: folder.listFiles()) {
            if (!usedImages.contains(image.getPath())) {
                image.delete();
            }
        }
        return graphs;
    }

    //function that handles storing the graphs in the file
    public static void writeGraphs(ArrayList<Graph> graphs) throws IOException {
        //see readGraphs for explanations on the json stuff
        Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).setPrettyPrinting().create();

        //loads a file writer linked to our json file
        FileWriter writer = new FileWriter(JsonfilePath);
        //converts our data into a json string
        String json = gson.toJson(graphs);
        //writes the json string to our file
        writer.write(json);
        writer.close();
    }

    //this can be safely ignored, but they just tell the json file manager how to handle the Instant class (once again stores our timestamps for graphs)
    //its not normally able to handle complex classes by default so it needs to be told how
    static class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString()); // ISO-8601 format
        }

        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return Instant.parse(json.getAsString());
        }
    }
}
