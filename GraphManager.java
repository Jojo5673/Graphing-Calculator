import java.awt.geom.Point2D;
import java.util.ArrayList;

public class GraphManager {

    public static void main(String[] args) {
        ArrayList<Point2D> data = new ArrayList<>();
        data.add(new Point2D.Double(1, 5));
        data.add(new Point2D.Double(2, 3));
        data.add(new Point2D.Double(3, 5));
        data.add(new Point2D.Double(4, 7));
        data.add(new Point2D.Double(5, 11));

        Graph graph  = new Graph("Test Graph", data);
        GraphScreen plot = new GraphScreen(graph);


    }
}
