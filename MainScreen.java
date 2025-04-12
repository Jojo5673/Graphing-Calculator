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
    private JButton cmdSortModel;

    private JPanel pnlCommand;
    private JPanel pnlDisplay;
    private MainScreen thisForm;

    private ArrayList<Graph> glist = new ArrayList<>();

    // Constructor
    public MainScreen() {
        super(new BorderLayout());
        thisForm = this;

        pnlCommand = new JPanel();
        pnlDisplay = new JPanel();
        pnlDisplay.setLayout(new GridLayout(0, 2, 10, 10)); // Display 2 graph cards per row

        JScrollPane scrollPane = new JScrollPane(pnlDisplay);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        cmdAddGraph = new JButton("Add Graph");
        cmdEditGraph = new JButton("Edit Graph");
        cmdDeleteGraph = new JButton("Delete Graph");
        cmdClose = new JButton("Close");
        cmdSortTitle = new JButton("Sort by Title");
        cmdSortModel = new JButton("Sort by Model");

        // Button Listeners
        cmdAddGraph.addActionListener(e -> {
            new GraphScreen(thisForm);
            refreshDisplayPanel();
        });
        cmdEditGraph.addActionListener(new EditGraphListener());
        cmdDeleteGraph.addActionListener(new DeleteGraphListener());
        cmdSortTitle.addActionListener(new SortTitleListener());
        cmdSortModel.addActionListener(new SortModelListener());
        cmdClose.addActionListener(e -> System.exit(0));
        cmdClose.setBackground(new Color(255, 199, 206)); // Light red for Close

        // Add buttons to panel
        pnlCommand.add(cmdAddGraph);
        pnlCommand.add(cmdEditGraph);
        pnlCommand.add(cmdDeleteGraph);
        pnlCommand.add(cmdSortTitle);
        pnlCommand.add(cmdSortModel);
        pnlCommand.add(cmdClose);

        add(pnlCommand, BorderLayout.SOUTH);

        refreshDisplayPanel(); // Load graphs on startup
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graph Information");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new MainScreen());
            frame.pack();
            frame.setVisible(true);
        });
    }

    // NEW: Refresh the image + label-based graph cards
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
                JLabel modelLabel = new JLabel("Model: " +
                        (graph.getRegression() != null ? graph.getRegression().getModelName() : "None"));

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

    // Placeholder listeners
    private static class EditGraphListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // TODO: Implement Edit
        }
    }

    private static class DeleteGraphListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // TODO: Implement Delete
        }
    }

    private static class SortTitleListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // TODO: Sort by title
        }
    }

    private static class SortModelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // TODO: Sort by model
        }
    }
}
