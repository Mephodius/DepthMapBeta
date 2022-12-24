import javax.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.Locale;

public class DMComparator extends JFrame {
    private Toolkit kit = Toolkit.getDefaultToolkit();
    JLabel LeftImageLabel = new JLabel("");
    JLabel RightImageLabel = new JLabel("");
    JLabel Metrics = new JLabel();
    JLabel Deviation = new JLabel();
    ImageProcessor improc = new ImageProcessor();
    private Box cvBox = Box.createVerticalBox();
    private Box bhBox = Box.createHorizontalBox();
    private Box thBox = Box.createHorizontalBox();
    private Box fhBox = Box.createHorizontalBox();

    public DMComparator(MainFrame mf, BufferedImage truemap, BufferedImage mymap, double hdeviation, double metrics){
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

        Metrics.setAlignmentX(Component.CENTER_ALIGNMENT);
        Deviation.setAlignmentX(Component.CENTER_ALIGNMENT);


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
        add(fhBox);

        int guiImageWidth = mymap.getWidth();
        int guiImageHeight = mymap.getHeight();

        LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(truemap, guiImageWidth, guiImageHeight)));
        RightImageLabel.setIcon(new ImageIcon(mymap));
        Metrics.setText("Metrics: " + String.format(Locale.US,"%.3f",metrics));
        Deviation.setText("Deviation: " + (int)hdeviation);
        setSize((int)(guiImageWidth * 2.3), (int)(guiImageHeight * 1.2));
        setLocation((kit.getScreenSize().width - this.getWidth()) / 2, (kit.getScreenSize().height - this.getHeight()) / 2);
        repaint();
        setVisible(true);
    }
}
