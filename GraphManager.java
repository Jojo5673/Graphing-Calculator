import RegressionModels.*;
import com.google.gson.Gson;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.lang.reflect.Type;
import java.time.Instant;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GraphManager {
    private static String JsonfilePath = "files/graphs.json";

    public static void main(String[] args) {
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

        Graph graph = new Graph("Test Graph", data);
        Graph graph2 = new Graph("Test Graph2", data2);
        graph2.setRegression(new PolynomialRegression(data2, 2));
        graph.setConnect_points(true);
        graph.setRegression(new SinusoidalRegression(data));

        //TO BE IMPLEMENTED: getting data and model from the ui

        //saving graphs to the file
        ArrayList<Graph> graphs = new ArrayList<>();
        graphs.add(graph);
        graphs.add(graph2);

        //writing graphs to file
        try {
            writeGraphs(graphs);
        }catch (IOException e){System.out.println("Unable to write graphs");}

        //reading graphs from the file
        try{
            for (Graph g : readGraphs()) {
                g.LoadRegression();
                GraphScreen.plot(g);
            }
        }catch (IOException e){System.out.println("Unable to read to file");}
    }

    public static ArrayList<Graph> readGraphs() throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).setPrettyPrinting().create();
        String json = Files.readString(Paths.get(JsonfilePath));
        Type listType = new TypeToken<ArrayList<Graph>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    public static void writeGraphs(ArrayList<Graph> graphs) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).setPrettyPrinting().create();
        FileWriter writer = new FileWriter(JsonfilePath);
        String json = gson.toJson(graphs);
        writer.write(json);
        writer.close();
    }

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
