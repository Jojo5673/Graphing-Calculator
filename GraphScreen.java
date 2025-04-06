import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.knowm.xchart.*;

import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.lines.SeriesLines;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class GraphScreen {
    public static void main(String[] args) {
        //data is stored as a list of points
        ArrayList<Point2D> data = new ArrayList<>();
        JFrame frame = new JFrame("Graph");

        //initalises data and a model
        //TO BE IMPLEMENTED: getting data and model from the ui
        data.add(new Point2D.Double(1, 5));
        data.add(new Point2D.Double(2, 3));
        data.add(new Point2D.Double(3, 5));
        data.add(new Point2D.Double(4, 7));
        data.add(new Point2D.Double(5, 11));
        RegressionModel regression = new SinusoidalRegression(data);

        //displays chart window with equation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.add(drawGraph(data, regression, frame), BorderLayout.CENTER);
        frame.add(regression.RenderEquation(), BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel drawGraph(ArrayList<Point2D> data, RegressionModel regression, Frame frame) {
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
        XYChart chart = new XYChartBuilder().width(800).height(600).title(regression.modelName).xAxisTitle("X").yAxisTitle("Y").build();
        XYStyler chartStyler = chart.getStyler();

        //adding the data points and regression curve
        Point2D min_padded = new Point2D.Double(min_axis.getX() - xy_padding.getX(), min_axis.getY() - xy_padding.getY());
        Point2D max_padded = new Point2D.Double(max_axis.getX() + xy_padding.getX(), max_axis.getY() + xy_padding.getY());
        regression.setX_range(min_padded.getX(), max_padded.getX());
        regression.fit();
        chart.addSeries("Data Points", x_data, y_data).setMarker(SeriesMarkers.CIRCLE).setLineStyle(SeriesLines.NONE).setShowInLegend(false);
        chart.addSeries(regression.function, regression.xFit, regression.yFit).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.SOLID).setShowInLegend(false);

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

abstract class RegressionModel{
    String function;
    String modelName;
    ArrayList<Double> xFit = new ArrayList<>();
    ArrayList<Double> yFit = new ArrayList<>();
    WeightedObservedPoints points = new WeightedObservedPoints();
    double[] x_range;

    //Regression model is a base class that forces its children to have a fit() method and provides a base function to render equations
    //The fit method generates a math equation for the best fit curve and provides points to plot this best fit curve
    //all regression models will have a model name and a function string that stores Latex code to render an equation in RenderEquation
    // also stores points to draw on the graph and points to calculate a curve for
    //finally it stores an x_range to ensure that the fit points don't exceed the boundaries set for the graph plot
    protected abstract void fit();

    protected JPanel RenderEquation(){
        JPanel panel = new JPanel();

        //renders LaTex
        TeXFormula formula = new TeXFormula(function);
        TeXIcon icon = formula.createTeXIcon(TeXFormula.SERIF, 17);
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g2, 0, 0);

        //adds the render to ui
        JLabel label = new JLabel(new ImageIcon(image));
        panel.add(label);
        return panel;
    }

    protected void setX_range(double min, double max) {
        x_range = new double[]{min, max};
    }

}

class PolynomialRegression extends RegressionModel{
    private PolynomialCurveFitter fitter;
    private HashMap<Integer, String> models = new HashMap<>(){{
        put(0, "Constant");
        put(1, "Linear");
        put(2, "Quadratic");
        put(3, "Cubic");
        put(4, "Quartic");
    }};
    //dictionary to store model names based on which order function the model is being called for
    public PolynomialRegression(ArrayList<Point2D> data, int order) { // can calulate any order polynomial
        for (Point2D point : data) {
            points.add(point.getX(), point.getY());
        } //obtains point
        fitter = PolynomialCurveFitter.create(order); //creates fitter object
        //calls below funtion
    }

    protected void fit() {
        double[] coeff = fitter.fit(points.toList());
        StringBuilder function_builder = new StringBuilder("y = ");
        modelName = models.get(coeff.length - 1);

        for (int i = coeff.length - 1; i >= 0; i--) {
            if (coeff[i] == 0) continue; // We dont want to include any 0x^n so we dont write 0 coefficients

            // Handle sign formatting
            if (i == coeff.length - 1) {
                function_builder.append(String.format("%.3f",coeff[i])); //First term. Negative values keep their sighn
            } else {
                //checks if it needs to add a + or - to the coefficient the  adds the coefficient without its normal sign
                function_builder.append(coeff[i] >= 0 ? " + " : " - ");
                function_builder.append(String.format("%.3f",Math.abs(coeff[i])));
            }

            if (i > 0) {
                function_builder.append("x"); // Append x
                if (i > 1) function_builder.append("^").append(i); // Append exponent if i > 1 we want to print x instead of x^1
            }
        }
        function = function_builder.toString();

        for (double x = x_range[0];x <= x_range[1]; x += 0.1) {
            double y = 0;
            for (int i = coeff.length - 1; i >= 0; i--) {
                y += coeff[i] * Math.pow(x, i); // Compute y value
            }
            xFit.add(x);
            yFit.add(y);
        }
    }

}

class ExponentialRegression extends RegressionModel{
    private ParametricUnivariateFunction exponential;

    public ExponentialRegression(ArrayList<Point2D> data) {
        for (Point2D point : data) {
            points.add(point.getX(), point.getY());
        }
        //calculates coefficients A and B of function in the form: y = Ae^(Bx)
        exponential = new ParametricUnivariateFunction() {
            //implementing methods for the parametric univariate function
            // this class is passed to a fitter which uses expressions for the y value and gradient to calculate coeffiecients
            public double value(double x, double... params) {
                double a = params[0];
                double b = params[1];
                return a * Math.exp(b * x);
            }
            public double[] gradient(double x, double... params) {
                // returns the partial derivatives of the expression with respect to each paramter in an array
                double b = params[1];
                double a = params[0];
                return new double[]{Math.exp(b * x), (a * x * Math.exp(b * x))};
            }
        };
        modelName = "Exponential";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(exponential, new double[]{1,1});
        double[] coeff = fitter.fit(points.toList());
        for (double x = x_range[0];x <= x_range[1]; x += 0.05) {
            double y = coeff[0] * Math.exp(x * coeff[1]);
            xFit.add(x);
            yFit.add(y);
        }

        function = "y = " + String.format("%.3f", coeff[0]) + "e" + "^{" + String.format("%.3f", coeff[1]) + "x}";
        System.out.println(function);
    }
}

class PowerRegression extends RegressionModel{
    private ParametricUnivariateFunction power;

    public PowerRegression(ArrayList<Point2D> data) {
        for (Point2D point : data) {
            points.add(point.getX(), point.getY());
        }
        power = new ParametricUnivariateFunction() {
            //implementing methods for the parametric univariate function
            // this class is passed to a fitter which uses expressions for the y value and gradient to calculate coeffiecients
            public double value(double x, double... params) {
                double a = params[0];
                double b = params[1];
                return a * Math.pow(x, b);
            }
            public double[] gradient(double x, double... params) {
                // returns the partial derivatives of the expression with respect to each paramter in an array
                double b = params[1];
                double a = params[0];
                return new double[]{Math.pow(x, b), (a * Math.pow(x, b) * Math.log(x))};
            }
        };
        modelName = "Power";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(power, new double[]{1,1});
        double[] coeff = fitter.fit(points.toList());
        for (double x = x_range[0];x <= x_range[1]; x += 0.05) {
            double y = coeff[0] * Math.pow(x, coeff[1]);
            xFit.add(x);
            yFit.add(y);
        }

        function = "y = " + String.format("%.3f", coeff[0]) + "x" + "^{" + String.format("%.3f", coeff[1]) + "}";
        System.out.println(function);
    }
}

class LogarithmicRegression extends RegressionModel{
    private ParametricUnivariateFunction logarithmic;

    public LogarithmicRegression(ArrayList<Point2D> data) {
        for (Point2D point : data) {
            points.add(point.getX(), point.getY());
        }
        logarithmic = new ParametricUnivariateFunction() {
            //implementing methods for the parametric univariate function
            // this class is passed to a fitter which uses expressions for the y value and gradient to calculate coeffiecients
            public double value(double x, double... params) {
                double a = params[0];
                double b = params[1];
                return a + b * Math.log(x);
            }
            public double[] gradient(double x, double... params) {
                // returns the partial derivatives of the expression with respect to each paramter in an array
                double b = params[1];
                double a = params[0];
                return new double[]{1, (Math.log(x))};
            }
        };
        modelName = "Logarithmic";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(logarithmic, new double[]{1,1});
        double[] coeff = fitter.fit(points.toList());
        for (double x = x_range[0];x <= x_range[1]; x += 0.05) {
            double y = coeff[0] + coeff[1] * Math.log(x);
            xFit.add(x);
            yFit.add(y);
        }

        function = "y = " + String.format("%.3f", coeff[0]) + " + " + String.format("%.3f", coeff[1]) + "\\ln{x}" ;
        System.out.println(function);
    }
}

class LogisitcRegression extends RegressionModel{
    private ParametricUnivariateFunction logisitc;

    public LogisitcRegression(ArrayList<Point2D> data) {
        for (Point2D point : data) {
            points.add(point.getX(), point.getY());
        }
        logisitc = new ParametricUnivariateFunction() {
            //implementing methods for the parametric univariate function
            // this class is passed to a fitter which uses expressions for the y value and gradient to calculate coeffiecients
            public double value(double x, double... params) {
                double a = params[0];
                double b = params[1];
                double c = params[2];
                return a / (1 + Math.exp(-b * (x - c)));
            }
            public double[] gradient(double x, double... params) {
                // returns the partial derivatives of the expression with respect to each paramter in an array
                double b = params[1];
                double a = params[0];
                double c = params[2];
                double expPart = Math.exp(-b * (x - c));
                double denom = Math.pow(1 + expPart, 2);
                return new double[]{1/(1+expPart), a * (x-b) * expPart/denom, -a * b * expPart/denom};
            }
        };
        modelName = "Logistic";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(logisitc, new double[]{1,1,1});
        double[] coeff = fitter.fit(points.toList());
        double a = coeff[0];
        double b = coeff[1];
        double c = coeff[2];
        for (double x = x_range[0];x <= x_range[1]; x += 0.05) {
            double y = a / (1 + Math.exp(-b * (x - c)));
            xFit.add(x);
            yFit.add(y);
        }
        //y = \frac{a}{1 + e^{-b(x - c)}}
        function = "y = " + "\\frac{" + String.format("%.3f", a) + "}{ 1 + e^{-" + String.format("%.3f", b) + "(x - " + String.format("%.3f", c) + ")}}";
        System.out.println(function);
    }
}

class SinusoidalRegression extends RegressionModel{
    private ParametricUnivariateFunction sinusoidal;

    public SinusoidalRegression(ArrayList<Point2D> data) {
        for (Point2D point : data) {
            points.add(point.getX(), point.getY());
        }
        sinusoidal = new ParametricUnivariateFunction() {
            //implementing methods for the parametric univariate function
            // this class is passed to a fitter which uses expressions for the y value and gradient to calculate coefficients
            public double value(double x, double... params) {
                double a = params[0];
                double b = params[1];
                double c = params[2];
                double d = params[3];
                return a * Math.sin(b * x + c) + d;
            }
            public double[] gradient(double x, double... params) {
                // returns the partial derivatives of the expression with respect to each paramter in an array
                double a = params[0];
                double b = params[1];
                double c = params[2];
                // Partial derivatives w.r.t A, B, C, D
                double sinTerm = Math.sin(b * x + c);
                double cosTerm = Math.cos(b * x + c);
                return new double[] {sinTerm, a * x * cosTerm, b * cosTerm, 1};
            }
        };
        modelName = "Sinusoidal";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(sinusoidal, new double[]{0,0,1,1});
        double[] coeff = fitter.fit(points.toList());
        double a = coeff[0];
        double b = coeff[1];
        double c = coeff[2];
        double d = coeff[3];
        for (double x = x_range[0];x <= x_range[1]; x += 0.05) {
            double y = a * Math.sin(b * x + c) + d;
            xFit.add(x);
            yFit.add(y);
        }
        function = "y = " + String.format("%.3f", a) + "\\sin{\\left(" + String.format("%.3f", b) + "x + "+ String.format("%.3f", c)+"\\right)} + " + String.format("%.3f", d) ;
        System.out.println(function);
    }
}