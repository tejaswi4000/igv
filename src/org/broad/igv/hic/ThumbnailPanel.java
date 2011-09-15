package org.broad.igv.hic;

import org.broad.igv.hic.data.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.*;
import java.io.Serializable;

/**
 * @author jrobinso
 * @date Aug 2, 2010
 */
public class ThumbnailPanel extends JComponent implements Serializable {

    private MainWindow mainWindow;
    private int binWidth = 1;

    Image image;

    HeatmapRenderer renderer = new HeatmapRenderer();
    public static final AlphaComposite ALPHA_COMP = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
    private double yScale;
    private double xScale;
    Point lastPoint = null;


    public ThumbnailPanel() {

        addMouseListener(new MouseAdapter() {


            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int xBP = (int) (mouseEvent.getX() * xScale);
                int yBP = (int) (mouseEvent.getY() * yScale);
                mainWindow.center(xBP, yBP);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                lastPoint = mouseEvent.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                lastPoint = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                if (lastPoint != null) {
                    int dxBP = ((int) ((mouseEvent.getX() - lastPoint.x) * xScale));
                    int dyBP = ((int) ((mouseEvent.getY() - lastPoint.y) * xScale));
                    mainWindow.moveBy(dxBP, dyBP);
                }
                lastPoint = mouseEvent.getPoint();

            }
        });

    }

    public void setImage(Image image) {
        this.image = image;
        int maxLen = Math.max(mainWindow.xContext.getChrLength(), mainWindow.yContext.getChrLength());
        xScale = ((double) maxLen) / getWidth();
        yScale = xScale;
    }

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public String getName() {
        return null;
    }

    public void setName(String nm) {

    }

    @Override
    protected void paintComponent(Graphics g) {

        if (image != null) {
            g.drawImage(image, 0, 0, null);
            renderVisibleWindow((Graphics2D) g);
        }
    }

    private void renderVisibleWindow(Graphics2D g) {
        if (mainWindow != null && mainWindow.xContext != null && mainWindow.xContext.getVisibleWidth() > 0) {


            int originX = mainWindow.xContext.getOrigin();
            int x = (int) (originX / xScale);

            int originY = mainWindow.yContext.getOrigin();
            int y = (int) (originY / yScale);

            int wBP = mainWindow.xContext.getVisibleWidth();
            int w = (int) (wBP / xScale);


            int yBP = mainWindow.yContext.getVisibleWidth();
            int h = (int) (yBP / yScale);

            Rectangle outerRectangle = new Rectangle(0, 0, getBounds().width, getBounds().height);
            Rectangle innerRectangle = new Rectangle(x, y, w, h);
            Shape shape = new SquareDonut(outerRectangle, innerRectangle);

            g.setColor(Color.gray);
            g.setComposite(ALPHA_COMP);
            g.fill(shape);

            g.draw(innerRectangle);
        }
    }

    public void setBinWidth(int binWidth) {
        this.binWidth = binWidth;
    }

    public int getBinWidth() {
        return binWidth;
    }

    public void clearImage() {
        image = null;
    }


    private static class SquareDonut implements Shape {
        private final Area area;

        public SquareDonut(Rectangle outerRectangle, Rectangle innerRectangle) {
            this.area = new Area(outerRectangle);
            area.subtract(new Area(innerRectangle));
        }

        public Rectangle getBounds() {
            return area.getBounds();
        }

        public Rectangle2D getBounds2D() {
            return area.getBounds2D();
        }

        public boolean contains(double v, double v1) {
            return area.contains(v, v1);
        }

        public boolean contains(Point2D point2D) {
            return area.contains(point2D);
        }

        public boolean intersects(double v, double v1, double v2, double v3) {
            return area.intersects(v, v1, v2, v3);
        }

        public boolean intersects(Rectangle2D rectangle2D) {
            return area.intersects(rectangle2D);
        }

        public boolean contains(double v, double v1, double v2, double v3) {
            return area.contains(v, v1, v2, v3);
        }

        public boolean contains(Rectangle2D rectangle2D) {
            return area.contains(rectangle2D);
        }

        public PathIterator getPathIterator(AffineTransform affineTransform) {
            return area.getPathIterator(affineTransform);
        }

        public PathIterator getPathIterator(AffineTransform affineTransform, double v) {
            return area.getPathIterator(affineTransform, v);
        }
    }
}
