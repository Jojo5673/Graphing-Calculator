import org.knowm.xchart.*;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.lines.SeriesLines;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;

import RegressionModels.*;

public class GraphScreen {
    public GraphScreen(Graph graph) {
        //data is stored as a list of points

        ArrayList<Point2D> data = graph.getPoints();
        JFrame frame = new JFrame("Graph");

        //initalises data and a model
        //TO BE IMPLEMENTED: getting data and model from the ui
        RegressionModel regression = new SinusoidalRegression(data);
        graph.setRegression(regression);

        //displays chart window with equation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.add(drawGraph(data, regression, frame), BorderLayout.CENTER);
        frame.add(regression.RenderEquation(), BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel drawGraph(ArrayList<Point2D> data, RegressionModel regression, Frame frame) {
        Point2D min_axis = new Point2D.Double(0, 0);
        Point2D max_axis = new Point2D.Double(0, 0);
        Point2D xy_padding;
        double padding = 0.05; //fraction of scale to pad by

        // Plotting the graph
        //Firstly the plot points need to be obtained and separated into x and y lists
        List<Double> x_data = new ArrayList<>();
        List<Double> y_data = new ArrayList<>();
        for (Point2D point : data) {
            double y = point.getY();
            double x = point.getX();
            x_data.add(x);
            y_data.add(y);
            //sets boundaries for drawing points
            if (y < min_axis.getY())
                min_axis.setLocation(min_axis.getX(), y);
            if (y > max_axis.getY())
                max_axis.setLocation(max_axis.getX(), y);
            if (x < min_axis.getX())
                min_axis.setLocation(x, min_axis.getY());
            if (x > max_axis.getX())
                max_axis.setLocation(x, max_axis.getY());
        }
        //calculates padding by applyin the padding % to the width of the points spread
        double x_pad = padding * (max_axis.getX() - min_axis.getX());
        double y_pad = padding * (max_axis.getY() - min_axis.getY());
        xy_padding = new Point2D.Double(x_pad, y_pad);

        //making the chart window
        XYChart chart = new XYChartBuilder().width(800).height(600).title(regression.getModelName()).xAxisTitle("X").yAxisTitle("Y").build();
        XYStyler chartStyler = chart.getStyler();

        //adding the data points and regression curve
        Point2D min_padded = new Point2D.Double(min_axis.getX() - xy_padding.getX(), min_axis.getY() - xy_padding.getY());
        Point2D max_padded = new Point2D.Double(max_axis.getX() + xy_padding.getX(), max_axis.getY() + xy_padding.getY());
        regression.setX_range(min_padded.getX(), max_padded.getX());
        regression.fit();
        chart.addSeries("Data Points", x_data, y_data).setMarker(SeriesMarkers.CIRCLE).setLineStyle(SeriesLines.NONE).setShowInLegend(false);
        chart.addSeries(regression.getFunction(), regression.getxFit(), regression.getyFit()).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.SOLID).setShowInLegend(false);

        //drawing x and y axes on the graph sheet
        chart.addSeries("y=0", new double[]{min_padded.getX(), max_padded.getX()}, new double[]{0,0}).setMarker(SeriesMarkers.NONE); // y=0 axis
        chart.addSeries("x=0", new double[]{0,0}, new double[]{min_padded.getY(), max_padded.getY()}).setMarker(SeriesMarkers.NONE); // x=0 axis

        //TO BE FLESHED OUT: currently not that useful but you can zoom on the x by selecting a box to view
        chartStyler.setZoomEnabled(true);
        chartStyler.setZoomResetByButton(true);
        chartStyler.setLegendVisible(false);
        chartStyler.setChartBackgroundColor(frame.getBackground());

        return new XChartPanel<>(chart);
    }
}

