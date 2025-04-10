import org.knowm.xchart.*;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.lines.SeriesLines;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import RegressionModels.*;

public class GraphScreen {
    public static void plot(Graph graph) {
        JFrame frame = new JFrame("Graph");
        //makes the frame for the graph manager

        //displays chart window with equation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        //adds the graph panel
        frame.add(drawGraph(graph, frame), BorderLayout.CENTER);
        //adds the equation panel is there is a regression stored for the graph

        //Control panels
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


        JTextArea pointArea = new JTextArea(5,20);
        JComboBox<String> regressionMenu = new JComboBox<>(new String[]{
                "None", "Exponential", "Logarithmic", "Logistic", "Polynomial", "Power", "Sinusoidal"
        });
        regressionMenu.setMaximumSize(new Dimension(1500,100));

        JCheckBox connectPoints = new JCheckBox("Connect Points");
        JButton saveButton = new JButton("Save Graph");

        controlPanel.add(new JLabel("Points (x,y per line):"));
        controlPanel.add(new JScrollPane(pointArea));
        controlPanel.add(new JLabel("Regression Type:"));
        controlPanel.add(regressionMenu);
        controlPanel.add(connectPoints);
        controlPanel.add(saveButton);

        frame.add(controlPanel, BorderLayout.WEST);
        if (graph.getRegression() != null)
            frame.add(graph.getRegression().RenderEquation(), BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel drawGraph(Graph graph, Frame frame) {
        //this block initialises parameters for the graph plot area
        //min and max axis are the points between which the graph show display its plot. this is gotten from the boundaries of the data points
        Point2D min_axis = new Point2D.Double(0, 0);
        Point2D max_axis = new Point2D.Double(0, 0);
        Point2D xy_padding; //padding is added to the data bounds to make a plot area
        double padding = 0.05; //fraction of scale to pad by

        ArrayList<Point2D.Double> data = graph.getPoints();
        RegressionModel regression = graph.getRegression();

        //Firstly the plot points need to be obtained and separated into separate x and y lists
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
        //calculates padding by applying the padding % to the width of the points spread
        double x_pad = padding * (max_axis.getX() - min_axis.getX());
        double y_pad = padding * (max_axis.getY() - min_axis.getY());
        xy_padding = new Point2D.Double(x_pad, y_pad);

        //making the chart window
        XYChart chart = new XYChartBuilder().width(800).height(600).title(graph.getTitle()).xAxisTitle("X").yAxisTitle("Y").build();
        //the chartStyler is a custom component linked to the chart that deals with a bunch of styling logic
        XYStyler chartStyler = chart.getStyler();

        //calculates the bounds for the graph to show its plot on by applying padding to the data boundaries
        Point2D min_padded = new Point2D.Double(min_axis.getX() - xy_padding.getX(), min_axis.getY() - xy_padding.getY());
        Point2D max_padded = new Point2D.Double(max_axis.getX() + xy_padding.getX(), max_axis.getY() + xy_padding.getY());

        //plots points and connects dots if necessary as shown by the ternary operator in setLineStyle()
        chart.addSeries("Data Points", x_data, y_data).setMarker(SeriesMarkers.CIRCLE).setLineStyle(graph.isConnect_points()?SeriesLines.SOLID:SeriesLines.NONE).setShowInLegend(false);
        if (regression != null) {
            //the graphing library (xChart) doesnt allow continuous lines so the regression model needs to generate points along its curve line
            //these points are plotted and connected by the charting library
            regression.setX_range(min_padded.getX(), max_padded.getX()); //this passes the plot area to the regression
            //this allows it to know where it needs to generate points for
            regression.fit();
            //this generates the points and calculates the math function representing the best fit curve line
            //plots the points generated by the regression model
            chart.addSeries(regression.getModelName(), regression.getxFit(), regression.getyFit()).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.SOLID).setShowInLegend(false);
        }

        //drawing x and y axes on the graph sheet
        chart.addSeries("y=0", new double[]{min_padded.getX(), max_padded.getX()}, new double[]{0,0}).setMarker(SeriesMarkers.NONE); // y=0 axis
        chart.addSeries("x=0", new double[]{0,0}, new double[]{min_padded.getY(), max_padded.getY()}).setMarker(SeriesMarkers.NONE); // x=0 axis
        //allows us to zoom into sections of the graph
        chartStyler.setZoomEnabled(true).setZoomResetByButton(true).setLegendVisible(false);
        chartStyler.setChartBackgroundColor(frame.getBackground());

        //handles generating a picture of the graph plot for the graph preveiw in the graph inventory manager (to be implemented)
        //firstly it makes an image path as files/images/graphid.png
        String imagePath = "files/images/" + graph.getId();
        graph.setImagePath(imagePath + ".png");
        try {
            save(chart, imagePath);
        } catch (IOException e) {
            System.out.println("Error saving image picture");
        }

        //returns the chart panel to the plot function
        return new XChartPanel<>(chart);
    }

    private static void save(XYChart saveChart, String filepath) throws IOException {
        //this works by duplicating the graph and exporting it to a png at a specified file path
        //the chart has to be duplicated to remove everything that isnt the plot itself (eg titles, axes, etc)
        //using chart = saveChart passed by reference so cloning has to be done manually

        //makes a new chart (exactly like in the drawGraph function)
        XYChart chart = new XYChartBuilder().width(saveChart.getWidth()).height(saveChart.getHeight()).build();
        //gets stylers for the original and copy
        XYStyler chartStyler = chart.getStyler();
        XYStyler saveStyler = saveChart.getStyler();

        //gets all the plots on the original chart (data points, regression line, y = 0 axis, x = 0 axis)
        Map<String, XYSeries> Series = saveChart.getSeriesMap(); // this returns a map(dictionary) that nees to be parsed
        //parsing the map
        for (XYSeries series : Series.values()) {
            //gets everything needed to make the plot
            String name = series.getName();
            double[] xData = series.getXData();
            double[] yData = series.getYData();

            //makes the plot while preserving all the styling things
            chart.addSeries(name, xData, yData).setMarker(series.getMarker()).setLineStyle(series.getLineStyle());
        }
        //new styling to remove all the extra stuff we dont want in the picture
        chartStyler.setChartBackgroundColor(saveStyler.getChartBackgroundColor()).setLegendVisible(false);
        chartStyler.setAxisTicksVisible(false).setAxisTitlesVisible(false).setChartTitleBoxVisible(false);
        chartStyler.setPlotMargin(0);

        //exports the image to a png
        BitmapEncoder.saveBitmapWithDPI(chart, filepath, BitmapEncoder.BitmapFormat.PNG, 300);
    }
}

