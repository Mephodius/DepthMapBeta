import javax.swing.*;
import java.awt.*;
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
        setTitle("Depth Map Comparator");
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

//        int guiImageWidth = mymap.getWidth();
//        int guiImageHeight = mymap.getHeight();
        int guiImageWidth = mymap.getWidth();
        int guiImageHeight = 560;

        int twidthl = truemap.getWidth()*guiImageHeight/truemap.getHeight();
        LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerDistanceBased(truemap,
                                    twidthl, guiImageHeight)));
        int twidthr = guiImageWidth*guiImageHeight/truemap.getHeight();
        RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerDistanceBased(mymap, twidthr, guiImageHeight)));
//        RightImageLabel.setIcon(new ImageIcon(mymap));
        Metrics.setText("Metrics: " + String.format(Locale.US,"%.3f",metrics));
        Deviation.setText("Deviation: " + (int)hdeviation);
        setSize((int)(1.2*twidthl+twidthr), (int)(guiImageHeight * 1.12));
        setLocation((kit.getScreenSize().width - this.getWidth()) / 2, (kit.getScreenSize().height - this.getHeight()) / 2);
        repaint();
        setVisible(true);
    }
}
