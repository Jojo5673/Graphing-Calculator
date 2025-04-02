import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.lines.SeriesLines;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DrawGraph {
    public static void main(String[] args) {
        SimpleRegression regression = new SimpleRegression();

        // Add data points (x, y)
        regression.addData(1, 2);
        regression.addData(2, 3);
        regression.addData(3, 5);
        regression.addData(4, 7);
        regression.addData(5, 11);

        // Get the slope (m) and intercept (b) for y = mx + b
        double slope = regression.getSlope();
        double intercept = regression.getIntercept();

        System.out.println("Best-fit line: y = " + slope + "x + " + intercept);

        // Generate data for the fitted line
        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();
        List<Double> yFit = new ArrayList<>();

        for (double x = 0; x <= 6; x += 0.1) {
            double y = slope * x + intercept;
            xData.add(x);
            yFit.add(y);
        }

        // Plot using XChart
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Linear Regression").xAxisTitle("X").yAxisTitle("Y").build();
        chart.addSeries("Data Points", new double[]{1, 2, 3, 4, 5}, new double[]{2, 3, 5, 7, 11}).setMarker(SeriesMarkers.CIRCLE).setLineStyle(SeriesLines.NONE);
        chart.addSeries("Regression Line", xData, yFit).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.SOLID);

        new SwingWrapper<>(chart).displayChart();
    }
}
