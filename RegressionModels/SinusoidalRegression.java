package RegressionModels;

import com.google.gson.annotations.Expose;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.SimpleCurveFitter;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class SinusoidalRegression extends RegressionModel {
    private ParametricUnivariateFunction sinusoidal;

    public SinusoidalRegression(ArrayList<Point2D.Double> data) {
        for (Point2D point : data) {
            points.add(point.getX(), point.getY());
        }
        this.sinusoidal = new ParametricUnivariateFunction() {
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
                return new double[]{sinTerm, a * x * cosTerm, b * cosTerm, 1};
            }
        };
        modelName = "Sinusoidal";
    }

    public void fit() {
        SimpleCurveFitter fitter = SimpleCurveFitter.create(sinusoidal, new double[]{0, 0, 1, 1});
        double[] coeff = fitter.fit(points.toList());
        double a = coeff[0];
        double b = coeff[1];
        double c = coeff[2];
        double d = coeff[3];
        for (double x = x_range[0]; x <= x_range[1]; x += detail) {
            double y = a * Math.sin(b * x + c) + d;
            xFit.add(x);
            yFit.add(y);
            if (x == x_range[0]){
                y_range[0] = y;
            }
            if (x + detail > x_range[1]) {
                y_range[1] = y;
            }
        }
        function = "y = " + String.format("%.3f", a) + "\\sin{\\left(" + String.format("%.3f", b) + "x + " + String.format("%.3f", c) + "\\right)} + " + String.format("%.3f", d);
        //System.out.println(function);
    }
}
