import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
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
        ArrayList<Point2D> data = new ArrayList<>();
        RegressionModel regression;
        data.add(new Point2D.Double(1, 5));
        data.add(new Point2D.Double(2, 3));
        data.add(new Point2D.Double(3, 5));
        data.add(new Point2D.Double(4, 7));
        data.add(new Point2D.Double(5, 11));

        //SimpleRegression regression = new SimpleRegression();
//        // Add data points (x, y)
//        for (Point2D point : data) {
//            regression.addData(point.getX(), point.getY());
//        }
//
//        // Get the slope (m) and intercept (b) for y = mx + b
//        double slope = regression.getSlope();
//        double intercept = regression.getIntercept();
//
//        System.out.println("Best-fit line: y = " + slope + "x + " + intercept);
//
//        // Generate data for the fitted line
//        List<Double> xFit = new ArrayList<>();
//        List<Double> yFit = new ArrayList<>();
//
//        for (double x = 0; x <= 6; x += 0.1) {
//            double y = slope * x + intercept;
//            xFit.add(x);
//            yFit.add(y);
//        }

        regression = new LinearRegression(data);

        // Plot using XChart
        List<Double> x_data = new ArrayList<>();
        List<Double> y_data = new ArrayList<>();
        for (Point2D point : data) {
            x_data.add(point.getX());
            y_data.add(point.getY());
        }
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Linear Regression").xAxisTitle("X").yAxisTitle("Y").build();
        chart.addSeries("Data Points", x_data, y_data).setMarker(SeriesMarkers.CIRCLE).setLineStyle(SeriesLines.NONE);
        chart.addSeries(regression.function, regression.xFit, regression.yFit).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.SOLID);

        new SwingWrapper<>(chart).displayChart();
    }
}

abstract class RegressionModel{
    String function;
    ArrayList<Double> xFit = new ArrayList<>();
    ArrayList<Double> yFit = new ArrayList<>();

    protected abstract void fit();

    JPanel DrawFunction(){
        JPanel panel = new JPanel();

        TeXFormula formula = new TeXFormula("E = mc^2");
        TeXIcon icon = formula.createTeXIcon(TeXFormula.SERIF, 50);
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g2, 0, 0);

        JLabel label = new JLabel(new ImageIcon(image));
        panel.add(label);
        return panel;
    }
}

class LinearRegression extends RegressionModel{
    SimpleRegression regression = new SimpleRegression();

    public LinearRegression(ArrayList<Point2D> data) {
        for (Point2D point : data) {
            regression.addData(point.getX(), point.getY());
        }
        fit();
    }

    protected void fit() {
        double slope = regression.getSlope();
        double intercept = regression.getIntercept();
        for (double x = 0; x <= 6; x += 0.1) {
            double y = slope * x + intercept;
            xFit.add(x);
            yFit.add(y);
        }
        function = "y = " + slope + "x " + (intercept>=0? "+ "+intercept: "- "+(-intercept));
    }
}

class PolynomialRegression extends RegressionModel{
    WeightedObservedPoints points = new WeightedObservedPoints();
    PolynomialCurveFitter fitter;

    public PolynomialRegression(ArrayList<Point2D> data, int order) {
        for (Point2D point : data) {
            points.add(point.getX(), point.getY());
        }
        fitter = PolynomialCurveFitter.create(order);
        fit();
    }

    protected void fit() {
        double[] coeff = fitter.fit(points.toList());
        StringBuilder function_builder = new StringBuilder("y = ");

        for (double x = 0; x <= 6; x += 0.1) {
            double y = 0;
            for (int i = coeff.length - 1; i >= 0; i--) {
                y += coeff[i] * Math.pow(x, i); // Compute y value
                if (coeff[i] == 0) continue; // We dont want to include any 0x^n so we dont write 0 coefficients

                // Handle sign formatting
                if (i == coeff.length - 1) {
                    function_builder.append(coeff[i]); //First term. Negative values keep their sighn
                } else {
                    //checks if it needs to add a + or - to the coefficient the  adds the coefficient without its normal sign
                    function_builder.append(coeff[i] >= 0 ? " + " : " - ");
                    function_builder.append(Math.abs(coeff[i]));
                }

                if (i > 0) {
                    function_builder.append("x"); // Append x
                    if (i > 1) function_builder.append("^").append(i); // Append exponent if i > 1 we want to print x instead of x^1
                }
            }

            xFit.add(x);
            yFit.add(y);
            function = function_builder.toString();
        }
    }

}