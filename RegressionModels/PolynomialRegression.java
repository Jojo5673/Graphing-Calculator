package RegressionModels;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class PolynomialRegression extends RegressionModel {
    private PolynomialCurveFitter fitter;

    //dictionary to store model names based on which order function the model is being called for
    public PolynomialRegression(ArrayList<Point2D.Double> data, int order) { // can calulate any order polynomial
        for (Point2D point : data) {
            points.add(point.getX(), point.getY());
        } //obtains point
        fitter = PolynomialCurveFitter.create(order); //creates fitter object
        HashMap<Integer, String> models = new HashMap<>() {{
            put(1, "Linear");
            put(2, "Quadratic");
            put(3, "Cubic");
            put(4, "Quartic");
        }};
        modelName = models.get(order);
    }

    public void fit() {
        double[] coeff = fitter.fit(points.toList());
        String func = "y = ";
        StringBuilder function_builder = new StringBuilder("");

        for (int i = coeff.length - 1; i >= 0; i--) {
            if (coeff[i] == 0) continue; // We dont want to include any 0x^n so we dont write 0 coefficients

            // Handle sign formatting
            if (i == coeff.length - 1) {
                function_builder.append(String.format("%.3f", coeff[i])); //First term. Negative values keep their sighn
            } else {
                //checks if it needs to add a + or - to the coefficient the  adds the coefficient without its normal sign
                function_builder.append(coeff[i] >= 0 ? " + " : " - ");
                function_builder.append(String.format("%.3f", Math.abs(coeff[i])));
            }

            if (i > 0) {
                function_builder.append("x"); // Append x
                if (i > 1)
                    function_builder.append("^").append(i); // Append exponent if i > 1 we want to print x instead of x^1
            }
        }
        if (function_builder.toString().equals("")) {
            function_builder.append("0");
        }
        function = func + function_builder.toString();
        for (double x = x_range[0]; x <= x_range[1]; x += detail) {
            double y = 0;
            for (int i = coeff.length - 1; i >= 0; i--) {
                y += coeff[i] * Math.pow(x, i); // Compute y value
            }
            if (y < y_limits[0] || y > y_limits[1]) {
                continue;
            }
            xFit.add(x);
            yFit.add(y);
            if (y < y_range[0]) {
                y_range[0] = y;
            }
            if (y > y_range[1]) {
                y_range[1] = y;
            }
        }
    }

}
