import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.lines.SeriesLines;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class DrawGraph {
    public static void main(String[] args) {
        SimpleRegression regression = new SimpleRegression();
        ArrayList<Point2D> data = new ArrayList<>();
        data.add(new Point2D.Double(1, 2));
        data.add(new Point2D.Double(2, 3));
        data.add(new Point2D.Double(3, 5));
        data.add(new Point2D.Double(4, 7));
        data.add(new Point2D.Double(5, 11));

        // Add data points (x, y)
        for (Point2D point : data) {
            regression.addData(point.getX(), point.getY());
        }

        // Get the slope (m) and intercept (b) for y = mx + b
        double slope = regression.getSlope();
        double intercept = regression.getIntercept();

        System.out.println("Best-fit line: y = " + slope + "x + " + intercept);

        // Generate data for the fitted line
        List<Double> xFit = new ArrayList<>();
        List<Double> yFit = new ArrayList<>();

        for (double x = 0; x <= 6; x += 0.1) {
            double y = slope * x + intercept;
            xFit.add(x);
            yFit.add(y);
        }

        // Plot using XChart
        double[] x_data = new double[data.size()];
        double[] y_data = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            x_data[i] = data.get(i).getX();
            y_data[i] = data.get(i).getY();
        }
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Linear Regression").xAxisTitle("X").yAxisTitle("Y").build();
        chart.addSeries("Data Points", x_data, y_data).setMarker(SeriesMarkers.CIRCLE).setLineStyle(SeriesLines.NONE);
        chart.addSeries("Regression Line", xFit, yFit).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.SOLID);

        new SwingWrapper<>(chart).displayChart();
    }
}
