import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
        pnlDisplay.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlDisplay.setPreferredSize(new Dimension(790, 700));

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
            //frame.setPreferredSize(new Dimension(550, 800));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new MainScreen());
            frame.pack();
            frame.setVisible(true);
        });
    }
    // Refresh the image + label-based graph cards
    public void refreshDisplayPanel() {
        try {
            glist = GraphManager.readGraphs();
            populateDisplayPanel(glist);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load graphs: " + ex.getMessage());
        }
    }

    public void populateDisplayPanel(ArrayList<Graph> glist) {
        pnlDisplay.removeAll();
        for (Graph graph : glist) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(0, 10, 10, 10) // top, left, bottom, right padding
            ));
            card.setBackground(new Color(245, 245, 245));
            card.setPreferredSize(new Dimension(250, 250));

            JLabel imageLabel = new JLabel();
            ImageIcon icon = new ImageIcon(graph.getImagePath()); // Path to graph image
            Image img = icon.getImage().getScaledInstance(230, 138, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));

            JLabel titleLabel = new JLabel("Title: " + graph.getTitle());
            JLabel idLabel = new JLabel("ID: " + graph.getId());

            ZonedDateTime timeStamp = graph.getTimeStamp().atZone(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, MMM d, yyyy h:mm a");
            String formatted = timeStamp.format(formatter);

            JLabel timeLabel = new JLabel("Created: " + formatted);
            JLabel modelLabel = new JLabel("Model: " + graph.getRegression().getModelName());

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false); // so it blends with the card

            JButton deleteItem = new JButton("×");
            deleteItem.setFont(new Font("Arial", Font.BOLD, 16));
            deleteItem.setMargin(new Insets(0, 0, 0, 0));
            deleteItem.setFocusPainted(false);
            deleteItem.setBorderPainted(false);
            deleteItem.setContentAreaFilled(false);
            deleteItem.setForeground(Color.RED);
            deleteItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            deleteItem.setMaximumSize(new Dimension(20, 20));

            // deleting listener
            deleteItem.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(thisForm,
                        "Are you sure you want to delete the graph titled \"" + graph.getTitle() + "\"?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    glist.remove(graph);
                    // Try deleting associated image file if it exists
                    if (graph.getImagePath() != null) {
                        java.io.File imageFile = new java.io.File(graph.getImagePath());
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                    }
                    try{
                        GraphManager.writeGraphs(glist);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(thisForm, "Error deleting graph: " + ex.getMessage());
                    }
                    thisForm.refreshDisplayPanel();
                    JOptionPane.showMessageDialog(thisForm, "Graph deleted.");

                    pnlDisplay.remove(card); // Remove from UI
                    pnlDisplay.revalidate();
                    pnlDisplay.repaint();
                    pnlDisplay.remove(card); // Remove from UI
                    pnlDisplay.revalidate();
                    pnlDisplay.repaint();
                }
            });

            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
            topPanel.add(Box.createHorizontalGlue());
            topPanel.add(deleteItem);
            deleteItem.setAlignmentY(Component.CENTER_ALIGNMENT);
            deleteItem.setAlignmentX(Component.RIGHT_ALIGNMENT);
            card.add(topPanel);
            card.add(imageLabel);
            card.add(Box.createVerticalStrut(5));
            card.add(titleLabel);
            card.add(idLabel);
            card.add(modelLabel);
            card.add(timeLabel);

            //allows for each graph to be clickable by using a mouse object
            card.addMouseListener(new MouseAdapter() {
              //  @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        openGraphForEditing(graph);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                //@Override
                public void mouseEntered(MouseEvent e) {
                    card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    card.setBackground(new Color(230, 230, 230)); // hover effect for the mouse
                }

              //  @Override
                public void mouseExited(MouseEvent e) {
                    card.setCursor(Cursor.getDefaultCursor());
                    card.setBackground(new Color(245, 245, 245)); // hover effect resets
                }
            });

            for (Component comp : card.getComponents()) {
                if (comp instanceof JComponent) {
                    ((JComponent) comp).setAlignmentX(Component.CENTER_ALIGNMENT);
                }
            }
            pnlDisplay.add(card);
        }

        pnlDisplay.revalidate();
        pnlDisplay.repaint();
    }

    //Function for editing graph
    public void openGraphForEditing(Graph g) throws IOException {
        g.LoadRegression();
        new GraphScreen(thisForm, g);
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
                        openGraphForEditing(g);
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
                    populateDisplayPanel(glist);
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
                    populateDisplayPanel(glist);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(thisForm, "Error sorting by time: " + ex.getMessage());
                }
            }
        }
    }

}
