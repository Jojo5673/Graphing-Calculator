import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.table.*;
import javax.swing.table.DefaultTableModel;
import java.util.Comparator;
import java.util.Collections;
import java.awt.Color;

public class MainScreen extends JPanel{
    //button declarations
    private JButton cmdAddGraph;
    private JButton cmdEditGraph;
    private JButton cmdDeleteGraph;
    private JButton cmdClose;
    private JButton cmdSortTitle;
    private JButton cmdSortModel;

    private JPanel      pnlCommand;
    private JPanel      pnlDisplay;
    private MainScreen thisForm;
    private  JScrollPane scrollPane;

    private JTable table;
    private DefaultTableModel model;

    private ArrayList<Graph> glist = new ArrayList<>();

    //constructor for MainScreen
    public MainScreen() {
        super(new GridLayout(2,1));
        thisForm = this;

        pnlCommand = new JPanel();
        pnlDisplay = new JPanel();

        //smthn for glist
        String[] columnNames = {"ID", "Title", "Time Created", "Regression Model"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        //showtable

        table.setPreferredScrollableViewportSize(new Dimension(500, glist.size()*15 +50));
        table.setFillsViewportHeight(true);

       // table.setBackground(new Color(240, 255, 255)); // Light cyan table background
      //  table.setGridColor(Color.BLACK);
        //table.setSelectionBackground(new Color(0,128,128)); //teal highlight colour

        scrollPane = new JScrollPane(table);

        add(scrollPane);

        //text fields for buttons
        cmdAddGraph = new JButton("Add Graph");
        cmdEditGraph = new JButton("Edit Graph");
        cmdDeleteGraph = new JButton("Delete Graph");
        cmdClose = new JButton("Close");
        cmdSortTitle = new JButton("Sort by Title");
        cmdSortModel = new JButton("Sort by Model");

        cmdClose.addActionListener(new CloseButtonListener());
        cmdAddGraph.addActionListener(new AddGraphButtonListener());
        cmdEditGraph.addActionListener(new EditGraphListener());
        cmdDeleteGraph.addActionListener(new DeleteGraphListener());
        cmdSortTitle.addActionListener(new SortTitleListener());
        cmdSortModel.addActionListener(new SortModelListener());


        cmdClose.setBackground(new Color(255,199,206)); //sets close to a light red colour

        pnlCommand.add(cmdAddGraph);
        pnlCommand.add(cmdEditGraph);
        pnlCommand.add(cmdDeleteGraph);
        pnlCommand.add(cmdSortTitle);
        pnlCommand.add(cmdSortModel);
        pnlCommand.add(cmdClose);

        add(pnlCommand);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Graph Information");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        MainScreen newContentPane = new MainScreen();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static class CloseButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            System.exit(0);
        }

    }

    private class AddGraphButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            new GraphScreen(thisForm);
        }
    }

    private static class EditGraphListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            //new GraphManager();
        }
    }

    private static class DeleteGraphListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            //new GraphManager();
        }
    }

    private static class SortTitleListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {

           // Collections.sort(plist, Comparator.comparingInt(Person::getAge));

            //resets table
           // model.setRowCount(0);

            //populates table
           // showTable(plist);
        }
    }

    private static class SortModelListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {

            // Collections.sort(plist, Comparator.comparingInt(Person::getAge));

            //resets table
            // model.setRowCount(0);

            //populates table
            // showTable(plist);
        }
    }



}


