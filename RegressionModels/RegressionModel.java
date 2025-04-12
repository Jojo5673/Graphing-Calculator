package RegressionModels;

import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class RegressionModel {
    protected double detail = 0.1; //sets how many fit points are created
    protected String function;
    protected String modelName;
    protected ArrayList<Double> xFit = new ArrayList<>();
    protected ArrayList<Double> yFit = new ArrayList<>();
    protected WeightedObservedPoints points = new WeightedObservedPoints();
    protected double[] x_range;

    public String getFunction() {return function;}
    public String getModelName() {return modelName;}
    public ArrayList<Double> getxFit() {return xFit;}
    public ArrayList<Double> getyFit() {return yFit;}

    //Regression model is a base class that forces its children to have a fit() method and provides a base function to render equations
    //The fit method generates a math equation for the best fit curve and provides points to plot this best fit curve
    //all regression models will have a model name and a function string that stores Latex code to render an equation in RenderEquation
    // also stores points to draw on the graph and points to calculate a curve for
    //finally it stores an x_range to ensure that the fit points don't exceed the boundaries set for the graph plot
    public abstract void fit();

    public JPanel RenderEquation(JPanel controller) {
        JLabel label = new JLabel();
        JPanel panel = new JPanel();
        int maxWidth = 250;
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //renders LaTex
        TeXFormula formula = new TeXFormula(function);
        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 26);
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        icon.paintIcon(label, image.getGraphics(), 0, 0);

        //adds the render to ui
        label.setIcon(icon);
        panel.add(label);
        return panel;
    }

    public void setX_range(double min, double max) {
        x_range = new double[]{min, max};
    }

}
