package it.ziotob.puzzlesolver.tools;

import it.ziotob.puzzlesolver.model.RawPiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class ToolsPanel extends JPanel {

    private final RawPiece piece;
    private final ImagePanel imagePanel;
    private JButton saveButton;
    private PieceEditor.ImageFrame imageFrame;

    public ToolsPanel(Integer width, Integer height, RawPiece piece, ImagePanel imagePanel, PieceEditor.ImageFrame imageFrame) {

        this.imageFrame = imageFrame;

        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(layout);

        this.setBackground(Color.GRAY);
        this.setMinimumSize(new Dimension(width, height));
        this.setPreferredSize(new Dimension(width, height));
        this.setMaximumSize(new Dimension(width, height));

        Label label = new Label("Hull Points");
        label.setMaximumSize(new Dimension(180, 20));
        this.add(label);

        var hullPoints = new JList<>(piece.getHullPoints().toArray());
        hullPoints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hullPoints.setLayoutOrientation(JList.VERTICAL);
        hullPoints.setVisibleRowCount(5);

        JScrollPane hullPointsBox = new JScrollPane(hullPoints);
        hullPointsBox.setMaximumSize(new Dimension(150, 100));

        this.add(hullPointsBox);

        JButton showHullPointsButton = new JButton();
        showHullPointsButton.setText("Show");
        showHullPointsButton.addActionListener(actionEvent -> imagePanel.showHullPoints());
        this.add(showHullPointsButton);

        JButton addHullPointButton = new JButton();
        addHullPointButton.addActionListener(actionEvent -> imagePanel.addHullPoint());
        addHullPointButton.setText("Add");
        this.add(addHullPointButton);

        JButton testPieceButton = new JButton();
        testPieceButton.addActionListener(actionEvent -> imagePanel.testPiece());
        testPieceButton.setText("Test");
        this.add(testPieceButton);

        saveButton = new JButton();
        saveButton.addActionListener(actionEvent -> imageFrame.dispatchEvent(new WindowEvent(imageFrame, WindowEvent.WINDOW_CLOSING)));
        saveButton.setText("Save");
        saveButton.setEnabled(false);
        this.add(saveButton);

        this.piece = piece;
        this.imagePanel = imagePanel;
    }

    public void canSave() {
        saveButton.setEnabled(true);
    }

    public void cantSave() {
        saveButton.setEnabled(false);
    }
}
