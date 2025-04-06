package RegressionModels;

import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class RegressionModel {
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

    public JPanel RenderEquation() {
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

    public void setX_range(double min, double max) {
        x_range = new double[]{min, max};
    }

}
