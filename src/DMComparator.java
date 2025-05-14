import com.sun.tools.javac.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DMComparator extends JFrame {

    MainFrame mainframe;
    private int interpol_choice;
    JPanel panel = new JPanel();
    private Toolkit kit = Toolkit.getDefaultToolkit();
    JLabel LeftImageLabel = new JLabel("");
    JLabel RightImageLabel = new JLabel("");
    JLabel Metrics = new JLabel();
    JLabel Correlation = new JLabel();
    JLabel STD = new JLabel();
    JLabel Deviation = new JLabel();
    ImageProcessor improc = new ImageProcessor();

    JButton ChangeDMButton = new JButton("Change DM");
    private Box cvBox = Box.createVerticalBox();
    private Box bhBox = Box.createHorizontalBox();
    private Box thBox = Box.createHorizontalBox();
    private Box fhBox = Box.createHorizontalBox();

    private BufferedImage mymap;
    private BufferedImage truemap;
    private boolean use_approx = false;
    private double[] metrics;
    private double std1;
    private double std2;

    private Action changeAction;
    private Action closeAction;

    private void LoadDM(File DM) throws IOException {
        setEnabled(false);
        //image1 = improc.SizeChangerLinear(ImageIO.read(LI), guiImageWidth*2, guiImageHeight*2);
        mymap = ImageIO.read(DM);
        metrics = getMapMetrics(improc.ImageToMatrix(truemap), improc.ImageToMatrix(mymap), use_approx);
        repaint();
        setEnabled(true);
    }
    private void ConfigureAllActions(){
        changeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    JFileChooser image_loader = mainframe.getImageLoader();
                    File DM = null;
                    int loaddmret = image_loader.showDialog(null, "Load DepthMap image");
                    if (loaddmret == JFileChooser.APPROVE_OPTION) {
                        DM = image_loader.getSelectedFile();
                    }
                    LoadDM(DM);

                } catch (Exception ignored) {
                    JOptionPane.showMessageDialog(DMComparator.this, "Something went wrong while reading, try again");
                }
            }
        };
        ChangeDMButton.addActionListener(changeAction);

        closeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                DMComparator.this.setVisible(false);
//                mf.setEnabled(false);
                mainframe.setVisible(true);
                mainframe.toFront();
                DMComparator.this.dispose();
//                MainFrame.this.dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING));
            }
        };


    }

    private Action GenClickAction(JButton but){
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                but.doClick();
            }
        };
    }
    private void ConfigureKeyBindings() {

        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        actionMap.put("Load", GenClickAction(ChangeDMButton));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "Load");

        actionMap.put("Close", closeAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), "Close");


    }

    public DMComparator(MainFrame mf, BufferedImage truemap, BufferedImage mymap, boolean use_approx){
        mainframe = mf;
        interpol_choice = mainframe.getInterpChoice();
        setTitle("Depth Map Comparator");
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                mainframe.toBack();
                mainframe.setVisible(false);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                mainframe.toFront();
                mainframe.setVisible(true);
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

        this.mymap = mymap;
        this.truemap = truemap;
        this.use_approx = use_approx;

        this.metrics = getMapMetrics(improc.ImageToMatrix(this.truemap), improc.ImageToMatrix(this.mymap), this.use_approx);

        Metrics.setAlignmentX(Component.CENTER_ALIGNMENT);
        Correlation.setAlignmentX(Component.CENTER_ALIGNMENT);
        STD.setAlignmentX(Component.CENTER_ALIGNMENT);
        Deviation.setAlignmentX(Component.CENTER_ALIGNMENT);
        ChangeDMButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        LoadDM(droppedFiles.get(0));
                    }

                    evt.dropComplete(true);
                } catch (Exception ignored) {
                    JOptionPane.showMessageDialog(DMComparator.this, "Something went wrong while reading, try again");
                }
            }
        });


        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Correlation);
//        cvBox.add(Box.createVerticalGlue());
//        cvBox.add(Metrics);
        cvBox.add(Box.createVerticalStrut(5));
        cvBox.add(STD);
        cvBox.add(Box.createVerticalStrut(5));
        cvBox.add(Deviation);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(ChangeDMButton);
        cvBox.add(Box.createVerticalGlue());



        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(LeftImageLabel);
        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(Box.createHorizontalStrut(5));
        fhBox.add(cvBox);
        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(Box.createHorizontalStrut(5));
        fhBox.add(RightImageLabel);
        fhBox.add(Box.createHorizontalGlue());
        panel.add(fhBox);
        add(panel);

//        int guiImageWidth = mymap.getWidth();
//        int guiImageHeight = mymap.getHeight();
        repaint();
        setVisible(true);
        ConfigureAllActions();
        ConfigureKeyBindings();
    }
    public double[] getMapMetrics(int[][][] matrix1, int[][][] matrix2, boolean use_approx) {
        // matrix2 is our map and is smaller
        std1 = improc.Std(matrix1);
        std2 = improc.Std(matrix2);
        int width = matrix2[0].length;

        int height = matrix2.length;
        double best_metric = 0;
        double best_correlation = 0;
        int opt_deviation = width / 4;
        int[][][] temp_matrix1, temp_matrix2;

        int n_rnd = width / 15;
        int size = width / 15;

        CompareMethod comp_method = new SAD();
        CompareMethod corr_method = new NCC();
        //double area = width/3.5;
        //int stripe = Math.max((int)area/75, 1);
        int drange = 1;
        System.out.println("Metrics calculation");
        for (int deviation = Math.max(0,matrix1[0].length - matrix2[0].length - drange); deviation <= matrix1[0].length - matrix2[0].length; deviation += 1) {
            temp_matrix1 = new int[height][width][3];
            temp_matrix2 = new int[height][width][3];
            for (int i = 0; i < Math.min(width, width-deviation); i++) {
                for (int j = 0; j < height; j++) {
                    for (int k = 0; k < 3; k++) {
                        //System.out.println(j + " " + (i + deviation) + " "+ matrix1.length + " " + matrix1[0].length);
                        temp_matrix1[j][i][k] = matrix1[j][i + deviation][k];
                        temp_matrix2[j][i][k] = matrix2[j][i][k];
                    }
                }
            }
            double correlation = 0;
            double metric = 0;
            double counter = 0;
            if (use_approx) {
                Random rand = new Random();
                for (int i = 0; i < n_rnd; i++) {
                    int[][][] rbatch1 = new int[size][size][3];
                    int[][][] rbatch2 = new int[size][size][3];
                    int y_r = rand.nextInt(width - size + 1);
                    int x_r = rand.nextInt(height - size + 1);
                    for (int n = 0; n < size; n++) {
                        for (int m = 0; m < size; m++) {
                            for (int k = 0; k < 3; k++) {
                                rbatch1[n][m][k] = temp_matrix1[x_r + n][y_r + m][k];
                                rbatch2[n][m][k] = temp_matrix2[x_r + n][y_r + m][k];
                            }
                        }
                    }
                    double temp = comp_method.get_similarity(rbatch1, rbatch2);
                    double temp1 = corr_method.get_similarity(rbatch1, rbatch2);
                    //double temp = improc.PSNR(rbatch1, rbatch2);
                    if (!Double.isNaN(temp)) {
                        metric += temp;
                        correlation += temp1;
                        counter++;
                    }
                }
                metric /= counter;
                correlation /= counter;
            } else {
                metric = comp_method.get_similarity(temp_matrix1, temp_matrix2);
                correlation = corr_method.get_similarity(temp_matrix1, temp_matrix2);
                //correlation = improc.PSNR(temp_matrix1, temp_matrix2);
            }
            if (metric > best_metric) {
                best_metric = metric;
                best_correlation = correlation;
                opt_deviation = deviation;
            }

            System.out.println(metric + " " + correlation + " " + deviation);
        }
        return new double[]{opt_deviation, best_metric, best_correlation};
    }
    public BufferedImage ImageCopy(BufferedImage img) {
        BufferedImage temp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                temp.setRGB(i, j, img.getRGB(i, j));
            }
        }
        return temp;
    }



    public void repaint(){
        int guiImageWidth = mymap.getWidth();
        int guiImageHeight = 560;
        double aspect_ratio = (double)guiImageHeight/truemap.getHeight();
        int twidthl = (int)((double)truemap.getWidth()*aspect_ratio);
        BufferedImage vtmap = improc.SizeChangerS(truemap, twidthl, guiImageHeight, interpol_choice);
        if (metrics[0] > 0) {
            for (int i = 0; i < guiImageHeight; i += 2) {
//            System.out.println(i);
                vtmap.setRGB((int) ((metrics[0]) * aspect_ratio) - 1, i, new Color(255, 0, 0).getRGB());
                vtmap.setRGB((int) ((metrics[0]) * aspect_ratio), i + 1, new Color(255, 0, 0).getRGB());
                vtmap.setRGB((int) ((metrics[0]) * aspect_ratio) + 1, i, new Color(255, 0, 0).getRGB());
            }
        }
        LeftImageLabel.setIcon(new ImageIcon(vtmap));
        int twidthr = guiImageWidth*guiImageHeight/truemap.getHeight();
        RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(mymap, twidthr, guiImageHeight, interpol_choice)));
//        RightImageLabel.setIcon(new ImageIcon(mymap));
        Metrics.setText("1-NMAE: " + String.format(Locale.US,"%.3f",metrics[1]));
        Correlation.setText("Correlation: " + String.format(Locale.US,"%.3f",metrics[2]));
        STD.setText("STD: " + String.format(Locale.US,"%.1f",std1) + " vs " + String.format(Locale.US,"%.1f",std2));
        Deviation.setText("Deviation: " + (int)metrics[0]);
//        setSize((int)(1.2*twidthl+twidthr), (int)(guiImageHeight * 1.08));
        pack();
        setLocation((kit.getScreenSize().width - this.getWidth()) / 2, (kit.getScreenSize().height - this.getHeight()) / 2);
    }
}
