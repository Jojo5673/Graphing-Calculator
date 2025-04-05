import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.knowm.xchart.*;
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

public class DrawGraph {
    public static void main(String[] args) {
        ArrayList<Point2D> data = new ArrayList<>();
        RegressionModel regression;
        data.add(new Point2D.Double(1, 5));
        data.add(new Point2D.Double(2, 3));
        data.add(new Point2D.Double(3, 5));
        data.add(new Point2D.Double(4, 7));
        data.add(new Point2D.Double(5, 11));

        Point2D min_axis = new Point2D.Double(-10, -30);
        Point2D max_axis = new Point2D.Double(10, 30);

        //regression = new PolynomialRegression(data, 2);
        regression = new SinusoidalRegression(data);
        // Plot using XChart
        List<Double> x_data = new ArrayList<>();
        List<Double> y_data = new ArrayList<>();
        for (Point2D point : data) {
            x_data.add(point.getX());
            y_data.add(point.getY());
        }
        XYChart chart = new XYChartBuilder().width(800).height(600).title(regression.modelName).xAxisTitle("X").yAxisTitle("Y").build();
        chart.addSeries("Data Points", x_data, y_data).setMarker(SeriesMarkers.CIRCLE).setLineStyle(SeriesLines.NONE).setShowInLegend(false);
        chart.addSeries(regression.function, regression.xFit, regression.yFit).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.SOLID);
        chart.addSeries("y=0", new double[]{0, 0}, new double[]{-30, 30}).setMarker(SeriesMarkers.NONE).setLineColor(Color.BLACK).setLineWidth(1).setShowInLegend(false); // y=0 axis
        chart.addSeries("x=0", new double[]{-30, 30}, new double[]{0, 0}).setMarker(SeriesMarkers.NONE).setLineColor(Color.BLACK).setLineWidth(1).setShowInLegend(false); // x=0 axis
        chart.getStyler().setZoomEnabled(true);
        chart.getStyler().setZoomResetByButton(true);

        //ChartZoom zoomer = new ChartZoom(chart, chart.get)
        new SwingWrapper<>(chart).displayChart();
    }
}

abstract class RegressionModel{
    String function;
    String modelName;
    ArrayList<Double> xFit = new ArrayList<>();
    ArrayList<Double> yFit = new ArrayList<>();
    WeightedObservedPoints points = new WeightedObservedPoints();

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

class PolynomialRegression extends RegressionModel{
    private PolynomialCurveFitter fitter;
    private HashMap<Integer, String> models = new HashMap<>(){{
        put(0, "Constant");
        put(1, "Linear");
        put(2, "Quadratic");
        put(3, "Cubic");
        put(4, "Quartic");
    }};

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

        for (double x = -6; x <= 6; x += 0.1) {
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
        fit();
        modelName = "Exponential";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(exponential, new double[]{1,1});
        double[] coeff = fitter.fit(points.toList());
        for (double x = -6; x <= 6; x += 0.05) {
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
        fit();
        modelName = "Power";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(power, new double[]{1,1});
        double[] coeff = fitter.fit(points.toList());
        for (double x = -6; x <= 6; x += 0.05) {
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
        fit();
        modelName = "Logarithmic";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(logarithmic, new double[]{1,1});
        double[] coeff = fitter.fit(points.toList());
        for (double x = -6; x <= 10; x += 0.05) {
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
        fit();
        modelName = "Logistic";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(logisitc, new double[]{1,1,1});
        double[] coeff = fitter.fit(points.toList());
        double a = coeff[0];
        double b = coeff[1];
        double c = coeff[2];
        for (double x = -6; x <= 10; x += 0.05) {
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
            // this class is passed to a fitter which uses expressions for the y value and gradient to calculate coeffiecients
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
        fit();
        modelName = "Sinusoidal";
    }

    protected void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(sinusoidal, new double[]{0,0,1,1});
        double[] coeff = fitter.fit(points.toList());
        double a = coeff[0];
        double b = coeff[1];
        double c = coeff[2];
        double d = coeff[3];
        for (double x = -6; x <= 10; x += 0.05) {
            double y = a * Math.sin(b * x + c) + d;
            xFit.add(x);
            yFit.add(y);
        }
        function = "y = " + String.format("%.3f", a) + "\\sin{\\left(" + String.format("%.3f", b) + "x + "+ String.format("%.3f", c)+"\\right)} + " + String.format("%.3f", d) ;
        System.out.println(function);
    }
}