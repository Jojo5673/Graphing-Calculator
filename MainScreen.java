import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.IOException;

public class MainScreen extends JPanel {
    // Buttons
    private JButton cmdAddGraph;
    private JButton cmdEditGraph;
    private JButton cmdDeleteGraph;
    private JButton cmdClose;
    private JButton cmdSortTitle;
    private JButton cmdSortTime;

    //Panel declarations
    private JPanel pnlCommand;
    private JPanel pnlDisplay;
    private MainScreen thisForm;

    private ArrayList<Graph> glist = new ArrayList<>();

    // Constructor for Main Screen
    public MainScreen() {
        super(new BorderLayout());
        thisForm = this;

        pnlCommand = new JPanel();
        pnlDisplay = new JPanel();
        pnlDisplay.setLayout(new GridLayout(0, 2, 10, 10)); // Display 2 graph cards per row

        //Adds scroll pane
        JScrollPane scrollPane = new JScrollPane(pnlDisplay);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        cmdAddGraph = new JButton("Add Graph");
        cmdEditGraph = new JButton("Edit Graph");
        cmdDeleteGraph = new JButton("Delete Graph");
        cmdClose = new JButton("Close");
        cmdSortTitle = new JButton("Sort by Title");
        cmdSortTime = new JButton("Sort by Time");

        // Button Listeners
        cmdAddGraph.addActionListener(e -> {
            new GraphScreen(thisForm);
            refreshDisplayPanel();
        });
        cmdEditGraph.addActionListener(new EditGraphListener());
        cmdDeleteGraph.addActionListener(new DeleteGraphListener());
        cmdSortTitle.addActionListener(new SortTitleListener());
        cmdSortTime.addActionListener(new SortTimeListener());
        cmdClose.addActionListener(e -> System.exit(0));
        cmdClose.setBackground(new Color(255, 199, 206)); // Light red for Close

        // Add buttons to panel
        pnlCommand.add(cmdAddGraph);
        pnlCommand.add(cmdEditGraph);
        pnlCommand.add(cmdDeleteGraph);
        pnlCommand.add(cmdSortTitle);
        pnlCommand.add(cmdSortTime);
        pnlCommand.add(cmdClose);

        add(pnlCommand, BorderLayout.SOUTH);

        refreshDisplayPanel(); // Load graphs on startup
    }

    //Main declaration
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graph Information");
            frame.setPreferredSize(new Dimension(600, 800));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new MainScreen());
            frame.pack();
            frame.setVisible(true);
        });
    }

    // Refresh the image + label-based graph cards
    public void refreshDisplayPanel() {
        pnlDisplay.removeAll();

        try {
            glist = GraphManager.readGraphs();

            for (Graph graph : glist) {
                JPanel card = new JPanel();
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                card.setBackground(new Color(245, 245, 245));
                card.setPreferredSize(new Dimension(250, 250));

                JLabel imageLabel = new JLabel();
                ImageIcon icon = new ImageIcon(graph.getImagePath()); // Path to graph image
                Image img = icon.getImage().getScaledInstance(200, 120, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));

                JLabel titleLabel = new JLabel("Title: " + graph.getTitle());
                JLabel idLabel = new JLabel("ID: " + graph.getId());
                JLabel timeLabel = new JLabel("Created: " + graph.getTimeStamp());
                JLabel modelLabel = new JLabel("Model: " + graph.getRegression().getModelName());

                card.add(imageLabel);
                card.add(Box.createVerticalStrut(5));
                card.add(titleLabel);
                card.add(idLabel);
                card.add(timeLabel);
                card.add(modelLabel);

                pnlDisplay.add(card);
            }

            pnlDisplay.revalidate();
            pnlDisplay.repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load graphs: " + ex.getMessage());
        }
    }

   //Listeners
    private class EditGraphListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String inputId = JOptionPane.showInputDialog(thisForm, "Enter Graph ID to edit:");
            //checks that ID is not null
            if (inputId == null || inputId.trim().isEmpty()) {
                return;
            }

            try {
                ArrayList<Graph> allGraphs = GraphManager.readGraphs();
                for (Graph g : allGraphs) {
                    if (g.getId().equals(inputId.trim())) {
                        g.LoadRegression(); // Reload regression
                        new GraphScreen(thisForm, g); // Launch with existing graph
                        return;
                    }
                }
                JOptionPane.showMessageDialog(thisForm, "Graph ID not found.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(thisForm, "Error loading graphs: " + ex.getMessage());
            }
        }
    }

    //Listener for delete graph
    private class DeleteGraphListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String inputId = JOptionPane.showInputDialog(thisForm, "Enter Graph ID to delete:");

            if (inputId == null || inputId.trim().isEmpty()) {
                return;
            }

            try {
                ArrayList<Graph> allGraphs = GraphManager.readGraphs();
                boolean found = false;

                //loops through graphs to find id and get confirmation message
                for (int i = 0; i < allGraphs.size(); i++) {
                    Graph g = allGraphs.get(i);
                    if (g.getId().equals(inputId.trim())) {
                        int confirm = JOptionPane.showConfirmDialog(thisForm,
                                "Are you sure you want to delete the graph titled \"" + g.getTitle() + "\"?",
                                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            allGraphs.remove(i);
                            // Try deleting associated image file if it exists
                            if (g.getImagePath() != null) {
                                java.io.File imageFile = new java.io.File(g.getImagePath());
                                if (imageFile.exists()) {
                                    imageFile.delete();
                                }
                            }
                            GraphManager.writeGraphs(allGraphs);
                            thisForm.refreshDisplayPanel();
                            JOptionPane.showMessageDialog(thisForm, "Graph deleted.");
                        }
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    JOptionPane.showMessageDialog(thisForm, "Graph ID not found.");
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(thisForm, "Error deleting graph: " + ex.getMessage());
            }
        }
    }

    //SortTitle listener
    private class SortTitleListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Create radio buttons for the options
            JRadioButton rbtnAZ = new JRadioButton("A-Z (ascending order)");
            JRadioButton rbtnZA = new JRadioButton("Z-A (descending order)");

            // Group the radio buttons so only one can be selected at a time
            ButtonGroup group = new ButtonGroup();
            group.add(rbtnAZ);
            group.add(rbtnZA);

            // Create a panel to hold the radio buttons
            JPanel panel = new JPanel();
            panel.add(rbtnAZ);
            panel.add(rbtnZA);

            // Show the option dialog
            int option = JOptionPane.showConfirmDialog(
                    thisForm,
                    panel,
                    "Please choose sort option: ",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            // If ok is selected, action is performed
            if (option == JOptionPane.OK_OPTION) {
                try {
                    glist = GraphManager.readGraphs(); //reads graphs

                    // Sort based on selected radio button
                    if (rbtnAZ.isSelected()) {
                        // sorts from A-Z
                        glist.sort((g1, g2) -> g1.getTitle().compareToIgnoreCase(g2.getTitle()));
                    } else if (rbtnZA.isSelected()) {
                        // sorts from Z-A
                        glist.sort((g1, g2) -> g2.getTitle().compareToIgnoreCase(g1.getTitle()));
                    }

                    // Refresh the display with sorted list
                    pnlDisplay.removeAll();
                    for (Graph graph : glist) {
                        JPanel card = new JPanel();
                        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                        card.setBackground(new Color(245, 245, 245));
                        card.setPreferredSize(new Dimension(250, 250));

                        JLabel imageLabel = new JLabel();
                        ImageIcon icon = new ImageIcon(graph.getImagePath()); // Path to graph image
                        Image img = icon.getImage().getScaledInstance(200, 120, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(img));

                        JLabel titleLabel = new JLabel("Title: " + graph.getTitle());
                        JLabel idLabel = new JLabel("ID: " + graph.getId());
                        JLabel timeLabel = new JLabel("Created: " + graph.getTimeStamp());
                        JLabel modelLabel = new JLabel("Model: " + graph.getRegression().getModelName());

                        card.add(imageLabel);
                        card.add(Box.createVerticalStrut(5));
                        card.add(titleLabel);
                        card.add(idLabel);
                        card.add(timeLabel);
                        card.add(modelLabel);

                        pnlDisplay.add(card);
                    }

                    pnlDisplay.revalidate();
                    pnlDisplay.repaint();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(thisForm, "Error sorting by title: " + ex.getMessage());
                }
            }
        }
    }

    //sorts graphs by time created
    private class SortTimeListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Create radio buttons for the options
            JRadioButton rbtnNewestToOldest = new JRadioButton("Newest to Oldest");
            JRadioButton rbtnOldestToNewest = new JRadioButton("Oldest to Newest");

            // Group the radio buttons so only one can be selected at a time
            ButtonGroup group = new ButtonGroup();
            group.add(rbtnNewestToOldest);
            group.add(rbtnOldestToNewest);

            // Create a panel to hold the radio buttons
            JPanel panel = new JPanel();
            panel.add(rbtnNewestToOldest);
            panel.add(rbtnOldestToNewest);

            // Show the option dialog
            int option = JOptionPane.showConfirmDialog(
                    thisForm,
                    panel,
                    "Please choose sort option: ",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            // If ok is selected, action is performed
            if (option == JOptionPane.OK_OPTION) {
                try {
                    glist = GraphManager.readGraphs(); //reads graphs

                    // Sort based on selected radio button
                    if (rbtnNewestToOldest.isSelected()) {
                        // sorts by Newest to Oldest
                        glist.sort((g1, g2) -> g2.getTimeStamp().compareTo(g1.getTimeStamp()));
                    } else if (rbtnOldestToNewest.isSelected()) {
                        // Oldest to Newest
                        glist.sort((g1, g2) -> g1.getTimeStamp().compareTo(g2.getTimeStamp()));
                    }

                    // Refresh the display with sorted list
                    pnlDisplay.removeAll();
                    for (Graph graph : glist) {
                        JPanel card = new JPanel();
                        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                        card.setBackground(new Color(245, 245, 245));
                        card.setPreferredSize(new Dimension(250, 250));

                        JLabel imageLabel = new JLabel();
                        ImageIcon icon = new ImageIcon(graph.getImagePath()); // Path to graph image
                        Image img = icon.getImage().getScaledInstance(200, 120, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(img));

                        JLabel titleLabel = new JLabel("Title: " + graph.getTitle());
                        JLabel idLabel = new JLabel("ID: " + graph.getId());
                        JLabel timeLabel = new JLabel("Created: " + graph.getTimeStamp());
                        JLabel modelLabel = new JLabel("Model: " + graph.getRegression().getModelName());

                        card.add(imageLabel);
                        card.add(Box.createVerticalStrut(5));
                        card.add(titleLabel);
                        card.add(idLabel);
                        card.add(timeLabel);
                        card.add(modelLabel);

                        pnlDisplay.add(card);
                    }

                    pnlDisplay.revalidate();
                    pnlDisplay.repaint();

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(thisForm, "Error sorting by time: " + ex.getMessage());
                }
            }
        }
    }

}
