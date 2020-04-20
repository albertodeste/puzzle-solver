package it.ziotob.puzzlesolver.tools;

import it.ziotob.puzzlesolver.model.RawPiece;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.event.*;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public class PieceEditor {

    private final RawPiece piece;
    private static final Object lock = new Object();

    public RawPiece editPiece() {

        ImageFrame frame = new ImageFrame(piece);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent arg0) {
                synchronized (lock) {
                    frame.setVisible(false);
                    lock.notify();
                }
            }

        });

        synchronized (lock) {
            while (frame.isVisible()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return frame.getResultingPiece()
                .orElseGet(this::editPiece);
    }

    static class ImageFrame extends JFrame {

        ImagePanel imagePanel;

        public ImageFrame(RawPiece piece) {

            setTitle("Piece editor");
            setSize(DEFAULT_WIDTH + 200, DEFAULT_HEIGHT);

            imagePanel = new ImagePanel(piece, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            ToolsPanel toolsPanel = new ToolsPanel(200, DEFAULT_HEIGHT, piece, imagePanel, this);
            imagePanel.setToolsPanel(toolsPanel);

            JPanel layoutPanel = new JPanel();
            BoxLayout layout = new BoxLayout(layoutPanel, BoxLayout.X_AXIS);
            layoutPanel.setLayout(layout);

            layoutPanel.add(imagePanel);
            layoutPanel.add(toolsPanel);

            imagePanel.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseDragged(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseMoved(MouseEvent mouseEvent) {
                    imagePanel.updateCursor(mouseEvent.getX(), mouseEvent.getY());
                }
            });

            imagePanel.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                   imagePanel.mouseClicked(mouseEvent);
                }

                @Override
                public void mousePressed(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {

                }
            });

            getContentPane().add(layoutPanel);
            getContentPane().validate();
            getContentPane().repaint();
        }

        public static final int DEFAULT_WIDTH = 800;
        public static final int DEFAULT_HEIGHT = 600;

        public Optional<RawPiece> getResultingPiece() {
            return imagePanel.getResultingPiece();
        }
    }
}
