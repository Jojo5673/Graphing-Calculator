package RegressionModels;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.SimpleCurveFitter;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class PowerRegression extends RegressionModel {
    private ParametricUnivariateFunction power;

    public PowerRegression(ArrayList<Point2D.Double> data) {
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

    public void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(power, new double[]{1, 1});
        double[] coeff = fitter.fit(points.toList());
        for (double x = x_range[0]; x <= x_range[1]; x += detail) {
            double y = coeff[0] * Math.pow(x, coeff[1]);
            xFit.add(x);
            yFit.add(y);
            if (x == x_range[0]){
                y_range[0] = y;
            }
            if (x + detail > x_range[1]) {
                y_range[1] = y;
            }
        }

        function = "y = " + String.format("%.3f", coeff[0]) + "x" + "^{" + String.format("%.3f", coeff[1]) + "}";
        //System.out.println(function);
    }
}
