import com.sun.tools.javac.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;


public class LogsVisualizator extends JFrame {

    MainFrame mainframe;
    private int interpol_choice;
    JPanel panel = new JPanel();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int screen_width = (int)screenSize.getWidth();
    int screen_height = (int)screenSize.getHeight();
    private Toolkit kit = Toolkit.getDefaultToolkit();
    JLabel LeftImageLabel = new JLabel("");
    JLabel RightImageLabel = new JLabel("");
    JLabel CLeftImageLabel = new JLabel("");
    JLabel CRightImageLabel = new JLabel("");
    JLabel CenterImageLabel = new JLabel("");
    JLabel CorrImageLabel = new JLabel("");
    JLabel Counter = new JLabel();
    JLabel CCounter = new JLabel();
    JLabel STD = new JLabel();
    JLabel Metrics = new JLabel();
    JLabel Deviation = new JLabel();
    JButton Next = new JButton("→");
    JCheckBox InvertFilter = new JCheckBox("InvertF");
    JLabel FilterLab = new JLabel("Filter by");
    JComboBox FiltrateBy = new JComboBox(new String[]{"Metrics", "Deviation", "STD1", "STD2", "ELen"});
    JTextField FilterMT = new JTextField("0.7");
    //JTextField FilterTo = new JTextField("0.9");
    JButton Previous = new JButton("←");
    ImageProcessor improc = new ImageProcessor();
    private Box cvBox = Box.createVerticalBox();
    private Box chBox = Box.createHorizontalBox();
    private Box bhBox = Box.createHorizontalBox();
    private Box thBox = Box.createHorizontalBox();
    private Box fhBox = Box.createHorizontalBox();
    private int counter;
    private int[][][] matrix1;
    private int[][][] matrix2;
    private int[][][] matrix1sc;
    private int[][][] matrix2sc;
    private int[][][] logs;
    private double[][][] correlation_m;
    private int width;
    private int height;
    private int vdeviation;
    private int opt_dev;
    private int dtis;

    private double scalex, scaley;

    private MouseHandler mouseHandler;

    private Action closeAction;
    private Action previousAction;
    private Action nextAction;

    private void ConfigureAllActions(){
        closeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LogsVisualizator.this.setVisible(false);
                mainframe.setVisible(true);
                mainframe.toFront();
                LogsVisualizator.this.dispose();
            }
        };

        previousAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int shift = FiltrateBy.getSelectedIndex();
                int tempy, tempx;
                double value, thresh;
                boolean fits = true;

                while (fits && counter > 0) {
                    counter--;
                    tempy = (int) ((double) counter / logs[0].length);
                    tempx = counter % logs[0].length;
                    value = Math.abs((double) logs[tempy][tempx][6 + shift] / dtis);
                    thresh = Double.parseDouble(FilterMT.getText());

                    fits = (value < thresh);

                    if (InvertFilter.isSelected())
                        fits = !fits;
                }

                if (counter >= 0) {
                    repaint();
                    Next.setEnabled(true);
                }
                if (counter == 0) {
                    JOptionPane.showMessageDialog(LogsVisualizator.this, "You have reached the first element");
                    Previous.setEnabled(false);
                }
            }
        };
        Previous.addActionListener(previousAction);

        nextAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //                for (int i = 0; i < logs.length; i++){
//                    for (int j = 0; j < logs[0].length; j++){
//                        System.out.print(logs[i][j][6] + " ");
//                    }
//                    System.out.println();
//                }
                int shift = FiltrateBy.getSelectedIndex();
                int tempy, tempx;
                double value, thresh;
                boolean fits = true;
                while (fits && counter < logs.length * logs[0].length - 1) {
                    counter++;
                    tempy = (int) ((double) counter / logs[0].length);
                    tempx = counter % logs[0].length;
                    value = Math.abs((double) logs[tempy][tempx][6 + shift] / dtis);
                    thresh = Double.parseDouble(FilterMT.getText());

                    fits = (value < thresh);
                    //System.out.println(counter + " " + logs[tempy][tempx][10]/dtis);

                    if (InvertFilter.isSelected())
                        fits = !fits;

                }
                //System.out.println(counter + " out of " + ((logs.length-1)*logs[0].length-1));

                if (counter <= logs.length * logs[0].length - 1) {
                    repaint();
                    Previous.setEnabled(true);
                }
                if (counter == logs.length * logs[0].length - 1) {
                    JOptionPane.showMessageDialog(LogsVisualizator.this, "You have reached the last element");
                    Next.setEnabled(false);
                }
            }
        };
        Next.addActionListener(nextAction);

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

        actionMap.put("Close", closeAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), "Close");

        actionMap.put("Previous", GenClickAction(Previous));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "Previous");

        actionMap.put("Next", GenClickAction(Next));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "Next");


    }

    public LogsVisualizator(MainFrame mf, BufferedImage image1, BufferedImage image2, int[][][] logs, double[][][] correlation_m, int vdeviation, int opt_deviation, int dtis) {
        mainframe = mf;
        interpol_choice = mainframe.getInterpChoice();
        setTitle("Result Analyzer");
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                mainframe.toBack();
//                mf.setEnabled(false);
                mainframe.setVisible(false);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                mainframe.toFront();
//                mf.setEnabled(false);
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

        // Transpose needed
        int imageWidth = image1.getWidth();
        int imageHeight = image1.getHeight();

        int guiImageHeight = 560;


        this.scaley = (double)guiImageHeight/imageHeight;
        this.scalex = scaley;

        int guiImageWidth = (int) (imageWidth * scalex);
        if (guiImageWidth > (screen_width-400)/2) {

            guiImageWidth = (screen_width-400)/2;
            scalex = (double) guiImageWidth/imageWidth;
        }
        System.out.println("Original" + " " + imageWidth + " " + imageHeight);
        System.out.println("GUI" + " " + guiImageWidth + " " + guiImageHeight);
        System.out.println("Scales" + " " + scalex + " " + scaley);

        this.matrix1 = improc.ImageToMatrixT(image1);
        this.matrix2 = improc.ImageToMatrixT(image2);

        BufferedImage limagesc = improc.SizeChangerS(image1, guiImageWidth, guiImageHeight, interpol_choice);
        BufferedImage rimagesc = improc.SizeChangerS(image2, guiImageWidth, guiImageHeight, interpol_choice);
        this.matrix1sc = improc.ImageToMatrixT(limagesc);
        this.matrix2sc = improc.ImageToMatrixT(rimagesc);
        this.logs = logs;
        this.correlation_m = correlation_m;
        this.counter = 0;
        this.width = matrix1sc.length;
        this.height = matrix1sc[0].length;
        this.vdeviation = vdeviation;
        this.opt_dev = opt_deviation;
        this.dtis = dtis;

        CenterImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        CorrImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Counter.setAlignmentX(Component.CENTER_ALIGNMENT);
        STD.setAlignmentX(Component.CENTER_ALIGNMENT);
        Metrics.setAlignmentX(Component.CENTER_ALIGNMENT);
        Deviation.setAlignmentX(Component.CENTER_ALIGNMENT);
        CCounter.setAlignmentX(Component.CENTER_ALIGNMENT);

        FilterMT.setMaximumSize(FilterMT.getPreferredSize());
        FilterMT.setHorizontalAlignment(JTextField.CENTER);

        //FilterTo.setMaximumSize(FilterTo.getPreferredSize());
        //FilterTo.setHorizontalAlignment(JTextField.CENTER);

        InvertFilter.setSelected(false);

        chBox.add(Box.createHorizontalGlue());
        chBox.add(FilterLab);
        chBox.add(Box.createHorizontalGlue());
        chBox.add(FiltrateBy);
        chBox.add(Box.createHorizontalGlue());
        chBox.add(FilterMT);
        chBox.add(Box.createHorizontalGlue());

        bhBox.add(Box.createHorizontalGlue());
        bhBox.add(Previous);
        bhBox.add(Box.createHorizontalGlue());
        bhBox.add(InvertFilter);
        bhBox.add(Box.createHorizontalGlue());
        bhBox.add(Next);
        bhBox.add(Box.createHorizontalGlue());

        thBox.add(Box.createHorizontalGlue());
        thBox.add(CLeftImageLabel);
        thBox.add(Box.createHorizontalGlue());
        thBox.add(Box.createHorizontalStrut(10));
        thBox.add(Box.createHorizontalGlue());
        thBox.add(CRightImageLabel);
        thBox.add(Box.createHorizontalGlue());
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(thBox);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Box.createVerticalStrut(10));
        cvBox.add(CenterImageLabel);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Box.createVerticalStrut(10));
        cvBox.add(CorrImageLabel);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(STD);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Metrics);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Deviation);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(CCounter);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Counter);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(chBox);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(bhBox);
        cvBox.add(Box.createVerticalGlue());


        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(Box.createHorizontalStrut(25));
        fhBox.add(LeftImageLabel);
        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(Box.createHorizontalStrut(5));
        fhBox.add(cvBox);
        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(Box.createHorizontalStrut(5));
        fhBox.add(RightImageLabel);
        fhBox.add(Box.createHorizontalStrut(25));
        fhBox.add(Box.createHorizontalGlue());


        panel.add(fhBox);
        add(panel);

        LeftImageLabel.setIcon(new ImageIcon(improc.MatrixToImage(matrix1sc)));
        RightImageLabel.setIcon(new ImageIcon(improc.MatrixToImage(matrix2sc)));



        Previous.setEnabled(false);
        //setSize(LeftImageLabel.getHeight() * 3, (int)(LeftImageLabel.getHeight() * 1.2));
//        setSize(guiImageWidth*2 + 236, 606);
        pack();
        setLocation((kit.getScreenSize().width - this.getWidth()) / 2, (kit.getScreenSize().height - this.getHeight()) / 2);
        repaint();
        setVisible(true);

        ConfigureAllActions();
        ConfigureKeyBindings();

        mouseHandler = new MouseHandler();
        this.addMouseListener(mouseHandler);
    }

    public int[][][] MCopy(int[][][] matrix) {
        int[][][] temp = new int[matrix.length][matrix[0].length][matrix[0][0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                for (int k = 0; k < matrix[0][0].length; k++)
                    temp[i][j][k] = matrix[i][j][k];
            }
        }
        return temp;
    }

    public class MouseHandler extends MouseAdapter {
        Boolean enabled = true;
        public MouseHandler() {
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public void mouseClicked(MouseEvent ev) {
            if (enabled) {
                if (ev.getButton() == 1) {
                }
            }

        }

        public void mousePressed(MouseEvent ev) {
            if (enabled) {
                if (ev.getButton() == 1) {

                }
            }
        }

        public void mouseReleased(MouseEvent ev) {
            if (enabled) {
                if (ev.getButton() == 1) {
                    Point p = MouseInfo.getPointerInfo().getLocation();
                    SwingUtilities.convertPointFromScreen(p, LeftImageLabel);
//                    System.out.println(p);

                    int tempx, tempy;
                    int height = LeftImageLabel.getHeight();
                    int width = LeftImageLabel.getWidth();
                    System.out.println(width+" "+height);
                    int shift = (int)(Math.abs(opt_dev)*scalex);
                    if (p.x >= shift && p.x < width && p.y >= 0 && p.y < height) {

                        p.x-=shift;
                        tempx = (int)((double)p.x*logs.length/(width-shift));
                        tempy = (int)((double)p.y*logs[0].length/height);


                        counter = (tempx*logs[0].length + tempy);//(tempy + tempx*logs[0].length);
                        System.out.println(tempx  +" "+shift+" "+logs.length +" "+tempy+" "+logs[0].length+" "+counter);
                        LogsVisualizator.this.repaint();
                    }
                }
            }
        }
    }

    @Override
    public void repaint() {
//        Point p = MouseInfo.getPointerInfo().getLocation();
//        SwingUtilities.convertPointFromScreen(p, RightImageLabel);
//        System.out.println(p);
        //col_image1, row_image1, coincidentx, coincidenty, sc_width, sc_height, metrics, (int)distance, (int)std1, (int)std2, extension_len, counter
        int[][][] tempmatrix1 = MCopy(matrix1sc);
        int[][][] tempmatrix2 = MCopy(matrix2sc);
        // tempx for width axis tempy for height axis
        int tempx, tempy;
        tempx = (int) ((double) counter / logs[0].length);
        tempy = counter % logs[0].length;


        int y1 = logs[tempx][tempy][0];
        int x1 = logs[tempx][tempy][1];
        int y2 = logs[tempx][tempy][2];
        int x2 = logs[tempx][tempy][3];
        int winw = logs[tempx][tempy][4];
        int winh = logs[tempx][tempy][5];


        int y1sc = (int)(y1 * scalex);
        int x1sc = (int)(x1 * scaley);
        int y2sc = (int)(y2 * scalex);
        int x2sc = (int)(x2 * scaley);
        int winwsc = (int)(winw * scalex);
        int winhsc = (int)(winh * scaley);
        int hdeviation = (int)(logs[tempx][tempy][12] * scalex);

        int[][][] win1 = new int[winw][winh][3];
        int[][][] win2 = new int[winw][winh][3];
        int[][][] residualw = new int[winw][winh][3];
        //System.out.println("WTF????" + winw + " " + winh);

        int simsize = 100;
        double heightsc = (double) tempmatrix1[0].length / tempmatrix1.length;
        int fw = 4 * (matrix1sc.length + matrix1sc[0].length) / (4 * simsize + (int) (4 * simsize * heightsc));
        while (fw > winhsc /4 && fw > 3) {
            fw /= 2;
        }
        //System.out.println("FW: " + fw);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

//                if (Math.pow((double)(i-y1)/Math.abs(hdeviation), 2) + Math.pow((double)(j-x1)/Math.abs(vdeviation), 2) > 1 && Math.pow((double)(i-y1)/(Math.abs(hdeviation) +fw), 2) + Math.pow((double)(j-x1)/(Math.abs(vdeviation) + fw), 2) < 1){
//                        tempmatrix1[i][j] = new int[]{255,255,255};
//                        tempmatrix2[i][j] = new int[]{255,255,255};
//                }
                if (hdeviation > 0) {
                    // left
                    if ((i - y1sc) < 0 && (i - y1sc) > -fw && j >= x1sc - vdeviation && j <= x1sc + winhsc + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // right
                    if ((i - y1sc) > Math.abs(hdeviation) + winwsc && (i - y1sc) < Math.abs(hdeviation) + winwsc + fw && j >= x1sc - vdeviation && j <= x1sc + winhsc + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // top
                    if (((j - x1sc) < -vdeviation && (j - x1sc) > -fw - vdeviation) && i > y1sc - fw && i < y1sc + Math.abs(hdeviation) + winwsc + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    //bottom
                    if ((j - x1sc) > vdeviation + winhsc && (j - x1sc) < vdeviation + winhsc + fw && i > y1sc - fw && i < y1sc + Math.abs(hdeviation) + winwsc + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                } else {
                    // left
                    if ((i - y1sc) < 0 + hdeviation && (i - y1sc) > -fw + hdeviation && j >= x1sc - vdeviation && j <= x1sc + winhsc + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // right
                    if ((i - y1sc) > winwsc && (i - y1sc) < winwsc + fw && j >= x1sc - vdeviation && j <= x1sc + winhsc + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // top
                    if (((j - x1sc) < -vdeviation && (j - x1sc) > -fw - vdeviation) && i > y1sc - fw + hdeviation && i < y1sc + winwsc + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    //bottom
                    if ((j - x1sc) > vdeviation + winhsc && (j - x1sc) < vdeviation + winhsc + fw && i > y1sc - fw + hdeviation && i < y1sc + winwsc + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                }

                if ((i - y1sc) < 0 && (i - y1sc) > -fw && j >= x1sc && j <= x1sc + winhsc) {
                    tempmatrix1[i][j] = new int[]{255, 0, 0};
                }
                if ((i - y1sc) > winwsc && (i - y1sc) < winwsc + fw && j >= x1sc && j <= x1sc + winhsc) {
                    tempmatrix1[i][j] = new int[]{255, 0, 0};
                }
                if (((j - x1sc) < 0 && (j - x1sc) > -fw) && i > y1sc - fw && i < y1sc + winwsc + fw) {
                    tempmatrix1[i][j] = new int[]{255, 0, 0};
                }
                if ((j - x1sc) > winhsc && (j - x1sc) < winhsc + fw && i > y1sc - fw && i < y1sc + winwsc + fw) {
                    tempmatrix1[i][j] = new int[]{255, 0, 0};
                }

                if ((i - y2sc) < 0 && (i - y2sc) > -fw && j >= x2sc && j <= x2sc + winhsc) {
                    tempmatrix2[i][j] = new int[]{255, 0, 0};
                }
                if ((i - y2sc) > winwsc && (i - y2sc) < winwsc + fw && j >= x2sc && j <= x2sc + winhsc) {
                    tempmatrix2[i][j] = new int[]{255, 0, 0};
                }
                if (((j - x2sc) < 0 && (j - x2sc) > -fw) && i > y2sc - fw && i < y2sc + winwsc + fw) {
                    tempmatrix2[i][j] = new int[]{255, 0, 0};
                }
                if ((j - x2sc) > winhsc && (j - x2sc) < winhsc + fw && i > y2sc - fw && i < y2sc + winwsc + fw) {
                    tempmatrix2[i][j] = new int[]{255, 0, 0};
                }
            }
        }
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < winw; i++) {
                for (int j = 0; j < winh; j++) {
                    win1[i][j][k] = matrix1[i + y1][j + x1][k];
                    win2[i][j][k] = matrix2[i + y2][j + x2][k];
                    //System.out.println(win1[i][j][k] + " " + win2[i][j][k]);
                    residualw[i][j][k] = Math.min(Math.abs(win1[i][j][k] - win2[i][j][k]) * 3, 255);
                }
            }
            //System.out.println("\n\nNEXT K\n\n");
        }

//        LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerDistanceBased(improc.MatrixToImage(tempmatrix1), 4 * simsize, (int) (6 * simsize * heightsc))));
//        RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerDistanceBased(improc.MatrixToImage(tempmatrix2), 4 * simsize, (int) (6 * simsize * heightsc))));
        LeftImageLabel.setIcon(new ImageIcon(improc.MatrixToImage(tempmatrix1)));
        RightImageLabel.setIcon(new ImageIcon(improc.MatrixToImage(tempmatrix2)));

        CLeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(improc.MatrixToImage(win1), simsize, simsize, interpol_choice)));
        CRightImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(improc.MatrixToImage(win2), simsize, simsize, interpol_choice)));
        CenterImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(improc.MatrixToImage(residualw), 2 * simsize, 2 * simsize, interpol_choice)));
        int nd = 2;
        int len = correlation_m[tempx][tempy].length;
        double[][] corr_mat = new double[len][nd];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < nd; j++) {
                if (hdeviation > 0) {
                    corr_mat[i][j] = correlation_m[tempx][tempy][i];
                } else {
                    corr_mat[i][j] = correlation_m[tempx][tempy][len - 1 - i];
                }
            }
        }
//        System.out.println("\nCor_Matrix " + corr_mat.length); // Why different length?
//        for (int i = 0; i < corr_mat.length; i++) {
//            System.out.println();
//            for (int j = 0; j < corr_mat[i].length; j++)
//                System.out.print(corr_mat[i][j] + " ");
//        }

        // ИСПРАВИТЬ УВЕЛИЧЕННЫЕ ПОДОБЛАСТИ!!!!
        CorrImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(improc.MatrixToImage(corr_mat),2*simsize, simsize/5, interpol_choice)));
        STD.setText("STD: " + (double)logs[tempx][tempy][8]/dtis +  " and " + (double)logs[tempx][tempy][9]/dtis);
        Metrics.setText("Metrics: " + (double)logs[tempx][tempy][6]/dtis);
        Deviation.setText("Deviation: " + String.format(Locale.US,"%.3f", Math.abs((double)logs[tempx][tempy][7]/dtis)));
        CCounter.setText("Comparisons: " + logs[tempx][tempy][11]);
        Counter.setText("Window counter: " + counter);
        super.repaint();

    }
}
