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

/**
 * The GraphScreen class provides a user interface for plotting and managing graphs.
 * It allows for the creation, editing, and visualization of 2D graphs, including the
 * application of various regression models (e.g., polynomial, exponential, logistic).
 *
 * Graphs can be saved, and the UI supports interactive updates of regression type and data points.
 * Uses XChart for plotting and supports saving graphs with rendered previews.
 *
 * Dependencies:
 * - XChart library for chart plotting.
 * - Apache Commons Math for regression model fitting.
 * - RegressionModels package for handling regression logic.
 *
 * @author
 */

public class GraphScreen {
    private MainScreen mscreen; //instance of main screen
    private GraphScreen gscreen;

    /**
     * Constructs a GraphScreen with a reference to the main screen and a graph to edit.
     * If no graph is provided, creates a new empty graph.
     *
     * @param mscreen      The parent MainScreen object.
     * @param graphToEdit  The graph object to edit; if null, a blank graph is used.
     */

    public GraphScreen(MainScreen mscreen, Graph graphToEdit) {
        this.mscreen = mscreen;
        plot(graphToEdit != null ? graphToEdit : new Graph("Untitled", new ArrayList<>()));
    }

    /**
     * Alternate constructor that creates an empty editable graph.
     *
     * @param mscreen  The parent MainScreen object.
     */

    public GraphScreen(MainScreen mscreen) {
        this.mscreen = mscreen;
        Graph emptyGraph = new Graph("Untitled", new ArrayList<>()); // Pass empty graph for editing
        plot(emptyGraph);
    }

    /**
     * Displays a JFrame containing the interactive graph plot and control panel for:
     * - Editing points
     * - Selecting regression models
     * - Connecting points
     * - Saving the graph
     * - Viewing best-fit equations
     *
     * @param graph  The graph object to display and edit.
     */

    public void plot(Graph graph) {
        JFrame frame = new JFrame("Graph");
        //makes the frame for the graph manager

        //displays chart window with equation
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
                "None", "Exponential", "Logarithmic", "Logistic", "Polynomial", "Power"
        });
        regressionMenu.setMaximumSize(new Dimension(1500,100));//Change dimension of drop down

        JCheckBox connectPoints = new JCheckBox("Connect Points");//Checkbox for connecting points

        //if graph has points populate
        if (!graph.getPoints().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Point2D.Double p : graph.getPoints()) {
                sb.append(p.getX()).append(", ").append(p.getY()).append("\n");
            }
            pointArea.setText(sb.toString());
            regressionMenu.setSelectedItem(graph.getRegression().getModelName());
            connectPoints.setSelected(graph.isConnect_points());
        }

        JButton plotButton = new JButton("Plot Graph");//Button to plot graph
        JButton saveButton = new JButton("Save Graph");//Button to save graph
        JButton cmdClose = new JButton("Close");
        cmdClose.setBackground(new Color(255, 199, 206)); // Light red for Close

        JPanel equation = new JPanel();
        JLabel eqLabel = new JLabel("Best Fit Equation: ");
        eqLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        equation.setLayout(new BoxLayout(equation, BoxLayout.Y_AXIS));
        if (!Objects.equals(graph.getRegression().getModelName(), "None")) {
            eqLabel.setSize(equation.getWidth(), equation.getHeight());
            eqLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            equation.removeAll();
            equation.add(eqLabel);
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
        controlPanel.add(cmdClose);
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
            if (!Objects.equals(graph.getRegression().getModelName(), "None")){
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
            String name = JOptionPane.showInputDialog(frame,"Enter graph name:", graph.getTitle());

            if(name == null || name.trim().isEmpty()){
                JOptionPane.showMessageDialog(frame,"No name provided");
                return;
            }
            //Create graph
            updateFromInput(graph, name.trim(),pointArea.getText(),(String) regressionMenu.getSelectedItem(),connectPoints.isSelected(),frame);
            XYChart newChart = drawGraph(graph, frame);
            JPanel newGraphPanel = new XChartPanel<>(newChart);
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
                save(newChart, imagePath);
                //Adds and saves the graph to the graph file
                ArrayList<Graph> graphs = GraphManager.readGraphs();
                graphs.removeIf(g -> g.getId().equals(graph.getId()));
                graphs.add(graph);
                GraphManager.writeGraphs(graphs);

                //refreshes table
                if (this.mscreen != null) {
                   this.mscreen.refreshDisplayPanel();
                }

                JOptionPane.showMessageDialog(frame,"Graph Saved");
            }catch(IOException ex){
                JOptionPane.showMessageDialog(frame,"Error saving graph: " +ex.getMessage());
            }

        });

        //Close button logic
        cmdClose.addActionListener(e -> frame.dispose());
    }

    /**
     * Builds or updates a Graph object based on user input:
     * - Parses input point text
     * - Sets regression type
     * - Handles polynomial degree prompts
     *
     * @param graph     The graph object to modify.
     * @param title     The title for the graph.
     * @param newPoints A string containing points in (x,y) format per line.
     * @param regType   The regression model name.
     * @param connect   Whether to connect points with lines.
     * @param parent    The UI component for error dialogs.
     */
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
            case "None" -> graph.setRegression(new None());
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
                    order = 2;
                }
                graph.setRegression(new PolynomialRegression(points,order));
            }

            case "Power" -> graph.setRegression(new PowerRegression(points));
        }
    }

    /**
     * Draws and returns an XYChart representation of a Graph object.
     * Applies padding and calculates bounds from the data. Also renders:
     * - Scatter points
     * - Regression lines (if applicable)
     * - X and Y axes (y=0 and x=0)
     *
     * @param graph The Graph object to render.
     * @param frame The parent frame used to sync colors with the chart background.
     * @return An XYChart ready to be used in an XChartPanel.
     */
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
        //a max range is set that is 6x the range of the data points (3x above and below). these are the boundaries for the autoscaling
        double max_range = max_axis.getY() - min_axis.getY() * 3;
        double[] maxY_fit = {min_axis.getY() - max_range, max_axis.getY() + max_range};
        //calculates padding by applying the padding % to the width of the points spread
        double x_pad = max_axis.getX() - min_axis.getX()!=0 ? padding * (max_axis.getX() - min_axis.getX()) : 5;
        double y_pad = max_axis.getY() - min_axis.getY()!=0 ? padding * (max_axis.getY() - min_axis.getY()) : 5;
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
            if (!Objects.equals(regression.getModelName(), "None")) {
                //the graphing library (xChart) doesnt allow continuous lines so the regression model needs to generate points along its curve line
                //these points are plotted and connected by the charting library
                regression.setX_range(min_padded.getX(), max_padded.getX()); //this passes the plot area to the regression
                regression.setY_limits(maxY_fit);
                //this allows it to know where it needs to generate points for
                try {
                    regression.fit();
                    chart.addSeries(regression.getModelName(), regression.getxFit(), regression.getyFit()).setMarker(SeriesMarkers.NONE).setLineStyle(SeriesLines.SOLID).setShowInLegend(false);
                    double[] y_range = regression.getY_range();
                    double min = Math.min(y_range[0], min_padded.getY());
                    double max = Math.max(y_range[1], max_padded.getY());
                    chart.addSeries("y=0", new double[]{min_padded.getX(), max_padded.getX()}, new double[]{0,0}).setMarker(SeriesMarkers.NONE).setLineColor(Color.MAGENTA); // y=0 axis
                    chart.addSeries("x=0", new double[]{0,0}, new double[]{min, max}).setMarker(SeriesMarkers.NONE).setLineColor(Color.GREEN); // x=0 axis
                } catch (ConvergenceException | IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid Points for Regression");
                    regression = new None();
                    graph.setRegression(regression);
                }
                //this generates the points and calculates the math function representing the best fit curve line
                //plots the points generated by the regression model

                //below is code for scaling the x and y axis
            }
            if (Objects.equals(regression.getModelName(), "None")) {
                chart.addSeries("y=0", new double[]{min_padded.getX(), max_padded.getX()}, new double[]{0,0}).setMarker(SeriesMarkers.NONE).setLineColor(Color.MAGENTA); // y=0 axis
                chart.addSeries("x=0", new double[]{0,0}, new double[]{min_padded.getY(), max_padded.getY()}).setMarker(SeriesMarkers.NONE).setLineColor(Color.GREEN); // x=0 axis
            }

            //drawing x and y axes on the graph sheet

        }
        //allows us to zoom into sections of the graph
        chartStyler.setZoomEnabled(true).setZoomResetByButton(true).setLegendVisible(false);
        chartStyler.setChartBackgroundColor(frame.getBackground());

        //returns the chart panel to the plot function
        return chart;
    }


    /**
     * Saves a chart as a PNG image file. Also updates graph metadata and serializes it.
     *
     * @param chart     The chart to save.
     * @param imagePath Path to save the image (without extension).
     * @throws IOException if image saving or file writing fails.
     */

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
            chart.addSeries(name, xData, yData).setMarker(series.getMarker()).setLineStyle(series.getLineStyle()).setLineColor(series.getLineColor());
        }
        //new styling to remove all the extra stuff we dont want in the picture
        chartStyler.setChartBackgroundColor(saveStyler.getChartBackgroundColor()).setLegendVisible(false);
        chartStyler.setAxisTicksVisible(false).setAxisTitlesVisible(false).setChartTitleBoxVisible(false);
        chartStyler.setPlotMargin(0);

        //exports the image to a png
        BitmapEncoder.saveBitmapWithDPI(chart, filepath, BitmapEncoder.BitmapFormat.PNG, 300);
    }
}

