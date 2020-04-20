package it.ziotob.puzzlesolver.tools;

import it.ziotob.puzzlesolver.model.Point;
import it.ziotob.puzzlesolver.model.RawPiece;
import it.ziotob.puzzlesolver.model.RawPieceFactory;
import it.ziotob.puzzlesolver.utils.PointUtils;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class ImagePanel extends JComponent {

    private static final long serialVersionUID = 1L;
    private BufferedImage baseImage;
    private BufferedImage image;
    private final Point min;
    private final Point max;
    private final Point offset;
    private final RawPiece piece;
    private List<Point> cursorSnap;
    private Point previousPoint;
    private boolean isPlacingHullPoint = false;
    private RawPiece resultingPiece;
    private List<Point> extraHullPoints = new ArrayList<>();
    @Setter
    private ToolsPanel toolsPanel;

    public ImagePanel(RawPiece piece, Integer windowWidth, Integer windowHeight) {

        min = piece.getBorderPoints().stream()
                .reduce(new it.ziotob.puzzlesolver.model.Point(Integer.MAX_VALUE, Integer.MAX_VALUE),
                        (prev, curr) -> new it.ziotob.puzzlesolver.model.Point(Math.min(prev.getX(), curr.getX()), Math.min(prev.getY(), curr.getY())));
        max = piece.getBorderPoints().stream()
                .reduce(new it.ziotob.puzzlesolver.model.Point(Integer.MIN_VALUE, Integer.MIN_VALUE),
                        (prev, curr) -> new it.ziotob.puzzlesolver.model.Point(Math.max(prev.getX(), curr.getX()), Math.max(prev.getY(), curr.getY())));
        offset = new Point((windowWidth - (max.getX() - min.getX())) / 2, (windowHeight - (max.getY() - min.getY())) / 2);

        BufferedImage img = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_ARGB);
        piece.getPoints().forEach(point ->
                img.setRGB(point.getX() - min.getX() + offset.getX(), point.getY() - min.getY() + offset.getY(), 0XFFFFFB00)
        );

        baseImage = img;
        image = deepCopy(baseImage);
        this.piece = piece;
        this.setMinimumSize(new Dimension(windowWidth, windowHeight));
        this.setPreferredSize(new Dimension(windowWidth, windowHeight));
        this.setMaximumSize(new Dimension(windowWidth, windowHeight));
    }

    @Override
    public void paintComponent(Graphics g) {

        if (Objects.nonNull(image)) {
            g.drawImage(image, 0, 0, this);
        }
    }

    public void updateCursor(int x, int y) {

        this.getGraphics().clearRect(0, 0, 100, 100);
        this.getGraphics().drawString((x + min.getX() - offset.getX()) + ", " + (y + min.getY() - offset.getY()), 9, 30);
        this.handleCursorSnap(x, y);
    }

    public void showHullPoints() {

        image = deepCopy(baseImage);
        this.piece.getHullPoints().forEach(point -> image.setRGB(
                point.getX() - min.getX() + offset.getX(),
                point.getY() - min.getY() + offset.getY(),
                0XFFFF0000));
        this.validate();
        this.repaint();
    }

    static BufferedImage deepCopy(BufferedImage bi) {

        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public void addHullPoint() {

        this.isPlacingHullPoint = true;
        this.cursorSnap(this.piece.getBorderPoints());
    }

    private void freeCursor() {
        this.cursorSnap = null;
    }

    private void cursorSnap(List<Point> borderPoints) {
        this.cursorSnap = borderPoints;
    }

    private void handleCursorSnap(Integer cursorX, Integer cursorY) {

        if (Objects.nonNull(cursorSnap)) {

            Graphics g = this.getGraphics();
            PointUtils.getClosestPoint(cursorSnap, new Point(cursorX + min.getX() - offset.getX(), cursorY + min.getY() - offset.getY()))
                    .ifPresent(point -> {

                        this.restorePreviousPoint();
                        Point p = new Point(point.getX() - min.getX() + offset.getX(), point.getY() - min.getY() + offset.getY());
                        previousPoint = p;

                        g.setColor(Color.RED);
                        g.drawLine(p.getX(), p.getY(), p.getX(), p.getY());
                    });
        }
    }

    private void restorePreviousPoint() {

        if (Objects.nonNull(previousPoint)) {

            Graphics g = getGraphics();
            Point p = previousPoint;
            g.setColor(new Color(image.getRGB(p.getX(), p.getY())));
            g.drawLine(p.getX(), p.getY(), p.getX(), p.getY());
        }
    }

    public Optional<RawPiece> getResultingPiece() {
        return Optional.ofNullable(resultingPiece);
    }

    public void mouseClicked(MouseEvent mouseEvent) {

        if (this.isPlacingHullPoint) {

            freeCursor();
            this.extraHullPoints.add(new Point(
                    previousPoint.getX() + min.getX() - offset.getX(),
                    previousPoint.getY() + min.getY() - offset.getY()));
            this.isPlacingHullPoint = false;
        }
    }

    public void testPiece() {

        this.resultingPiece = RawPieceFactory.factory(piece.getPoints(), extraHullPoints);

        image = deepCopy(baseImage);
        resultingPiece.getInnerLocks().stream()
                .flatMap(innerLock -> innerLock.getPoints().stream())
                .forEach(point -> image.setRGB(
                        point.getX() - min.getX() + offset.getX(),
                        point.getY() - min.getY() + offset.getY(),
                        0xFF00FF00));
        resultingPiece.getOuterLocks().stream()
                .flatMap(outerLock -> outerLock.getPoints().stream())
                .forEach(point -> image.setRGB(
                        point.getX() - min.getX() + offset.getX(),
                        point.getY() - min.getY() + offset.getY(),
                        0XFFFF0000));
        this.validate();
        this.repaint();

        if (this.resultingPiece.isFullyDetected()) {
            toolsPanel.canSave();
        } else {
            toolsPanel.cantSave();
        }
    }
}
