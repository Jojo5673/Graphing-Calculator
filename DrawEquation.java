import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.lines.SeriesLines;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DrawEquation {
    public static void main(String[] args) {
        JFrame frame = new JFrame("LaTeX Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        // Render a LaTeX equation
        TeXFormula formula = new TeXFormula("E = mc^2");
        TeXIcon icon = formula.createTeXIcon(TeXFormula.SERIF, 20);
        Image image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g2, 0, 0);

        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);

        frame.setVisible(true);
    }
}
