
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Arrays;

public class PlotFrame extends JFrame {
    private Toolkit kit = Toolkit.getDefaultToolkit();
    JLabel LeftImageLabel = new JLabel("");
    JLabel RightImageLabel = new JLabel("");
    JLabel Metrics = new JLabel();
    JLabel Deviation = new JLabel();
    ImageProcessor improc = new ImageProcessor();
    PlotVisualizator pv = new PlotVisualizator();
    private Box cvBox = Box.createVerticalBox();
    private Box fhBox = Box.createHorizontalBox();
    private Box fvBox = Box.createVerticalBox();

    public PlotFrame(MainFrame mf, BufferedImage left, BufferedImage right, double hdeviation, double metrics, double[][] logs){
        setTitle("Correlation function plot");
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                mf.toFront();
                mf.setEnabled(true);
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        pv.showGraphics(logs);

        Metrics.setAlignmentX(Component.CENTER_ALIGNMENT);
        Deviation.setAlignmentX(Component.CENTER_ALIGNMENT);
        pv.setAlignmentX(Component.CENTER_ALIGNMENT);

        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Metrics);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Deviation);
        cvBox.add(Box.createVerticalGlue());

        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(LeftImageLabel);
        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(cvBox);
        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(RightImageLabel);
        fhBox.add(Box.createHorizontalGlue());

        fvBox.add(Box.createVerticalGlue());
        fvBox.add(fhBox);
        fvBox.add(Box.createVerticalGlue());
        fvBox.add(pv);
        fvBox.add(Box.createVerticalGlue());

        add(pv);

        int guiImageWidth = right.getWidth();
        int guiImageHeight = right.getHeight();

        LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(left, guiImageWidth, guiImageHeight)));
        RightImageLabel.setIcon(new ImageIcon(right));
        Metrics.setText("Best correlation: " + String.format(Locale.US,"%.3f",metrics));
        Deviation.setText("Optimal deviation: " + (int)hdeviation);
        setSize((int)(guiImageWidth * 2.3), (int)(guiImageHeight * 2.3));
        setLocation((kit.getScreenSize().width - this.getWidth()) / 2, (kit.getScreenSize().height - this.getHeight()) / 2);
        repaint();
        setVisible(true);
    }
}

