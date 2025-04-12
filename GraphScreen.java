import org.apache.commons.math3.exception.ConvergenceException;
import org.knowm.xchart.*;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.lines.SeriesLines;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.List;

import RegressionModels.*;

public class GraphScreen {
    private MainScreen mscreen; //instance of main screen
    private GraphScreen gscreen;

    public GraphScreen(MainScreen mscreen) {
        this.mscreen = mscreen;
        Graph emptyGraph = new Graph("Untitled", new ArrayList<>()); // Pass empty graph for editing
        plot(emptyGraph);
    }


    public void plot(Graph graph) {
        JFrame frame = new JFrame("Graph");
        //makes the frame for the graph manager

        //displays chart window with equation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        //adds the graph panel
        frame.add(new XChartPanel<>(drawGraph(graph, frame)), BorderLayout.CENTER);
        //adds the equation panel is there is a regression stored for the graph

        //Control panels for input and options
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));//Gives a vertical layout
        controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));//Padding around panel

        JTextArea pointArea = new JTextArea(5,20);//Text Area for points
        JComboBox<String> regressionMenu = new JComboBox<>(new String[]{//Drop down menu for regression
                "None", "Exponential", "Logarithmic", "Logistic", "Polynomial", "Power", "Sinusoidal"
        });
        regressionMenu.setMaximumSize(new Dimension(1500,100));//Change dimension of drop down

        JCheckBox connectPoints = new JCheckBox("Connect Points");//Checkbox for connecting points
        JButton plotButton = new JButton("Plot Graph");//Button to plot graph
        JButton saveButton = new JButton("Save Graph");//Button to save graph
        JPanel equation = new JPanel();
        JLabel eqLabel = new JLabel("Best Fit Equation: ");
        eqLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        equation.setLayout(new BoxLayout(equation, BoxLayout.Y_AXIS));
        if (graph.getRegression() != null) {
            equation.add(new JLabel("Best Fit Equation: "));
            equation.add(graph.getRegression().RenderEquation());
            equation.setVisible(true);
        }else{
            equation.setVisible(false);
        }
        //Adding components to panel
        controlPanel.add(new JLabel("Points (x,y per line):"));
        controlPanel.add(new JScrollPane(pointArea));
        controlPanel.add(equation);
        controlPanel.add(new JLabel("Regression Type:"));
        controlPanel.add(regressionMenu);
        controlPanel.add(connectPoints);
        controlPanel.add(plotButton);
        controlPanel.add(saveButton);
        for (Component comp : controlPanel.getComponents()) {
            if (comp instanceof JComponent) {
                ((JComponent) comp).setAlignmentX(Component.CENTER_ALIGNMENT);
            }
        }

        //Add the control panel and regression to display frame
        frame.add(controlPanel, BorderLayout.WEST);
        frame.pack();
        frame.setVisible(true);

        //Plot Button Logic
        plotButton.addActionListener(e -> {
            //Create graph from user input
            updateFromInput(graph, graph.getTitle(), pointArea.getText(), (String) regressionMenu.getSelectedItem(), connectPoints.isSelected(), frame);
            //Replaces current graph display with new one
            JPanel newGraphPanel = new XChartPanel<>(drawGraph(graph, frame));
            frame.getContentPane().removeAll(); // clear old content
            frame.add(controlPanel, BorderLayout.WEST);
            frame.add(newGraphPanel, BorderLayout.CENTER);
            if (graph.getRegression() != null) {
                eqLabel.setSize(equation.getWidth(), equation.getHeight());
                eqLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                equation.removeAll();
                equation.add(eqLabel);
                equation.add(graph.getRegression().RenderEquation());
                equation.setVisible(true);
            }else{
                equation.setVisible(false);
            }
            frame.revalidate();//Refreshes the layout
            frame.repaint();//Repaints the frame
        });
        //Save Button Logic
        saveButton.addActionListener(e->{
            //Prompt that asks user for the name of the graph
            String name = JOptionPane.showInputDialog(frame,"Enter graph name:");

            if(name == null || name.trim().isEmpty()){
                JOptionPane.showMessageDialog(frame,"No name provided");
                return;
            }
            //Create graph
            updateFromInput(graph, name.trim(),pointArea.getText(),(String) regressionMenu.getSelectedItem(),connectPoints.isSelected(),frame);
            JPanel newGraphPanel = new XChartPanel<>(drawGraph(graph, frame));
            frame.getContentPane().removeAll(); // clear old content
            frame.add(controlPanel, BorderLayout.WEST);
            frame.add(newGraphPanel, BorderLayout.CENTER);
            frame.revalidate();//Refreshes the layout
            frame.repaint();//Repaints the frame
            try{
                //handles generating a picture of the graph plot for the graph preveiw in the graph inventory manager (to be implemented)
                //firstly it makes an image path as files/images/graphid.png
                String imagePath = "files/images/" + graph.getId();
                graph.setImagePath(imagePath + ".png");
                save(drawGraph(graph, frame), imagePath);
                //Adds and saves the graph to the graph file
                ArrayList<Graph> graphs = GraphManager.readGraphs();
                graphs.add(graph);
                GraphManager.writeGraphs(graphs);

                //refreshes table
                if (this.mscreen != null) {
                   this.mscreen.refreshTable();
                }

                JOptionPane.showMessageDialog(frame,"Graph Saved");
            }catch(IOException ex){
                JOptionPane.showMessageDialog(frame,"Error saving graph: " +ex.getMessage());
            }

        });
    }
    //Builds a graph from user input
    private static void updateFromInput(Graph graph, String title, String newPoints, String regType, boolean connect, Component parent){
        //Parse input into graph points
        String[] lines = newPoints.split("\\n");
        ArrayList<Point2D.Double> points = new ArrayList<>();
        for (String line:lines){
            try{
                String[] parts = line.split(",");
                double x = Double.parseDouble(parts[0].trim());
                double y = Double.parseDouble(parts[1].trim());
                points.add(new Point2D.Double(x,y));
            } catch(Exception e){
                JOptionPane.showMessageDialog(parent, "Invalid point format: " + line);
            }
        }
        graph.setPoints(points);
        graph.setTitle(title);
        graph.setConnect_points(connect);//Connect option
        //Determines the regression type for the graph
        switch(regType){
            case "Exponential" -> graph.setRegression(new ExponentialRegression(points));
            case "Logarithmic" -> graph.setRegression(new LogarithmicRegression(points));
            case "Logistic" -> graph.setRegression(new LogisitcRegression(points));
            case "Polynomial" -> {
                //Prompt for polynomial order
                String input = JOptionPane.showInputDialog("Enter the order(1-4): ");

                int order = 2; //default to quadratic
                try {
                    order = Integer.parseInt(input);
                    if(order<1||order>4) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Invalid input. Defaulting to order 2");
                }
                graph.setRegression(new PolynomialRegression(points,order));
            }

            case "Power" -> graph.setRegression(new PowerRegression(points));
            case "Sinusoidal" -> graph.setRegression(new SinusoidalRegression(points));
        }
    }

    private static XYChart drawGraph(Graph graph, Frame frame) {
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

        if (!graph.getPoints().isEmpty()) {
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
                try {
                    regression.fit();
                    chart.addSeries(regression.getModelName(), regression.getxFit(), regression.getyFit()).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.SOLID).setShowInLegend(false);
                } catch (ConvergenceException | IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid Points for Regression");
                }
                //this generates the points and calculates the math function representing the best fit curve line
                //plots the points generated by the regression model

                //below is code for scaling the x and y axis
                double[] y_range = regression.getY_range();
                double min = y_range[0] < 0?y_pad:-y_pad;
                boolean scale_to_min = Math.abs(y_range[1]-y_range[0])<Math.abs(y_range[1]-min);
                chart.addSeries("y=0", new double[]{min_padded.getX(), max_padded.getX()}, new double[]{0,0}).setMarker(SeriesMarkers.NONE); // y=0 axis
                chart.addSeries("x=0", new double[]{0,0}, new double[]{scale_to_min ?min:y_range[0], y_range[1]}).setMarker(SeriesMarkers.NONE); // x=0 axis
            }else{
                chart.addSeries("y=0", new double[]{-10, 10}, new double[]{0,0}).setMarker(SeriesMarkers.NONE); // y=0 axis
                chart.addSeries("x=0", new double[]{0,0}, new double[]{-10, 10}).setMarker(SeriesMarkers.NONE); // x=0 axis
            }

            //drawing x and y axes on the graph sheet

        }
        //allows us to zoom into sections of the graph
        chartStyler.setZoomEnabled(true).setZoomResetByButton(true).setLegendVisible(false);
        chartStyler.setChartBackgroundColor(frame.getBackground());

        //returns the chart panel to the plot function
        return chart;
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

