package RegressionModels;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.SimpleCurveFitter;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class LogisitcRegression extends RegressionModel {
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
                return new double[]{1 / (1 + expPart), a * (x - b) * expPart / denom, -a * b * expPart / denom};
            }
        };
        modelName = "Logistic";
    }

    public void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(logisitc, new double[]{1, 1, 1});
        double[] coeff = fitter.fit(points.toList());
        double a = coeff[0];
        double b = coeff[1];
        double c = coeff[2];
        for (double x = x_range[0]; x <= x_range[1]; x += 0.05) {
            double y = a / (1 + Math.exp(-b * (x - c)));
            xFit.add(x);
            yFit.add(y);
        }
        //y = \frac{a}{1 + e^{-b(x - c)}}
        function = "y = " + "\\frac{" + String.format("%.3f", a) + "}{ 1 + e^{-" + String.format("%.3f", b) + "(x - " + String.format("%.3f", c) + ")}}";
        System.out.println(function);
    }
}
