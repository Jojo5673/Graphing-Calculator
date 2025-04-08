package RegressionModels;

import com.google.gson.annotations.Expose;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.SimpleCurveFitter;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class ExponentialRegression extends RegressionModel {
    private ParametricUnivariateFunction exponential;

    public ExponentialRegression(ArrayList<Point2D.Double> data) {
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

    public void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(exponential, new double[]{1, 1});
        double[] coeff = fitter.fit(points.toList());
        for (double x = x_range[0]; x <= x_range[1]; x += detail) {
            double y = coeff[0] * Math.exp(x * coeff[1]);
            xFit.add(x);
            yFit.add(y);
        }

        function = "y = " + String.format("%.3f", coeff[0]) + "e" + "^{" + String.format("%.3f", coeff[1]) + "x}";
        System.out.println(function);
    }
}
