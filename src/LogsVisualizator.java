import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Locale;

public class LogsVisualizator extends JFrame {
    private Toolkit kit = Toolkit.getDefaultToolkit();
    JLabel LeftImageLabel = new JLabel("");
    JLabel RightImageLabel = new JLabel("");
    JLabel TopImageLabel = new JLabel("");
    JLabel BotImageLabel = new JLabel("");
    JLabel CenterImageLabel = new JLabel("");
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
    private int[][][] logs;
    private int width;
    private int height;
    private int hdeviation;
    private int vdeviation;
    private int dtis;
    public LogsVisualizator(MainFrame mf, int[][][] matrix1, int[][][] matrix2, int[][][] logs, int hdeviation, int vdeviation, int dtis){
        setTitle("Result Analyzer");
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

        this.matrix1 = matrix1;
        this.matrix2 = matrix2;
        this.logs = logs;
        this.counter = 0;
        this.width = matrix1.length;
        this.height = matrix1[0].length;
        this.hdeviation = hdeviation;
        this.vdeviation = vdeviation;
        this.dtis = dtis;

        CenterImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
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
        thBox.add(TopImageLabel);
        thBox.add(Box.createHorizontalGlue());
        thBox.add(Box.createHorizontalStrut(10));
        thBox.add(Box.createHorizontalGlue());
        thBox.add(BotImageLabel);
        thBox.add(Box.createHorizontalGlue());
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(thBox);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(CenterImageLabel);
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
        fhBox.add(LeftImageLabel);
        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(cvBox);
        fhBox.add(Box.createHorizontalGlue());
        fhBox.add(RightImageLabel);
        fhBox.add(Box.createHorizontalGlue());
        add(fhBox);
        LeftImageLabel.setIcon(new ImageIcon(improc.MatrixToImage(matrix1)));
        RightImageLabel.setIcon(new ImageIcon(improc.MatrixToImage(matrix2)));
        Next.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                while(fits && counter < logs.length*logs[0].length - 1){
                    counter++;
                    tempy = (int)((double)counter/logs[0].length);
                    tempx = counter%logs[0].length;
                    value = Math.abs((double)logs[tempy][tempx][6+shift]/dtis);
                    thresh = Double.parseDouble(FilterMT.getText());

                    fits = (value < thresh);
                    //System.out.println(counter + " " + logs[tempy][tempx][10]/dtis);

                    if (InvertFilter.isSelected())
                        fits = !fits;

                }
                //System.out.println(counter + " out of " + ((logs.length-1)*logs[0].length-1));

                if(counter <= logs.length*logs[0].length - 1) {
                    repaint();
                    Previous.setEnabled(true);
                }
                if(counter == logs.length*logs[0].length - 1){
                    JOptionPane.showMessageDialog(LogsVisualizator.this, "You have reached the last element");
                    Next.setEnabled(false);
                }
            }
        });
        Previous.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int shift = FiltrateBy.getSelectedIndex();
                int tempy, tempx;
                double value, thresh;
                boolean fits = true;

                while(fits && counter > 0) {
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
                if(counter == 0){
                    JOptionPane.showMessageDialog(LogsVisualizator.this, "You have reached the first element");
                    Previous.setEnabled(false);
                }
            }
        });
        Previous.setEnabled(false);
        //setSize(LeftImageLabel.getHeight() * 3, (int)(LeftImageLabel.getHeight() * 1.2));
        setSize(1050, 500);
        setLocation((kit.getScreenSize().width - this.getWidth()) / 2, (kit.getScreenSize().height - this.getHeight()) / 2);
        repaint();
        setVisible(true);
    }
    public int[][][] MCopy(int[][][] matrix){
        int[][][] temp = new int[matrix.length][matrix[0].length][matrix[0][0].length];
        for (int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[0].length; j++){
                for(int k = 0; k < matrix[0][0].length; k++)
                    temp[i][j][k] = matrix[i][j][k];
            }
        }
        return temp;
    }
    @Override
    public void repaint() {
        //col_image1, row_image1, coincidentx, coincidenty, sc_width, sc_height, metrics, (int)distance, (int)std1, (int)std2, extension_len, counter
        int[][][] tempmatrix1 = MCopy(matrix1);
        int[][][] tempmatrix2 = MCopy(matrix2);

        int tempx, tempy;
        tempy = (int)((double)counter/logs[0].length);
        tempx = counter%logs[0].length;

        int winw = logs[tempy][tempx][4];
        int winh = logs[tempy][tempx][5];
        int y1 = logs[tempy][tempx][0];
        int x1 = logs[tempy][tempx][1];
        int y2 = logs[tempy][tempx][2];
        int x2 = logs[tempy][tempx][3];

        int[][][] win1 = new int[winw][winh][3];
        int[][][] win2 = new int[winw][winh][3];
        int[][][] residualw = new int[winw][winh][3];
        //System.out.println("WTF????" + winw + " " + winh);

        int simsize = 100;
        double heightsc = (double)tempmatrix1[0].length/tempmatrix1.length;
        int fw = 4*(matrix1.length + matrix1[0].length)/(4*simsize + (int)(4*simsize*heightsc));
        while (fw > Math.sqrt(winh*winw) && fw > 3){
            fw/=2;
        }
        //System.out.println("FW: " + fw);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
            {

//                if (Math.pow((double)(i-y1)/Math.abs(hdeviation), 2) + Math.pow((double)(j-x1)/Math.abs(vdeviation), 2) > 1 && Math.pow((double)(i-y1)/(Math.abs(hdeviation) +fw), 2) + Math.pow((double)(j-x1)/(Math.abs(vdeviation) + fw), 2) < 1){
//                        tempmatrix1[i][j] = new int[]{255,255,255};
//                        tempmatrix2[i][j] = new int[]{255,255,255};
//                }
                if (hdeviation > 0) {
                    // left
                    if ((i - y1) < 0 && (i - y1) > -fw && j >= x1 - vdeviation && j <= x1 + winh + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // right
                    if ((i - y1) > Math.abs(hdeviation) + winw && (i - y1) < Math.abs(hdeviation) + winw + fw && j >= x1 - vdeviation && j <= x1 + winh + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // top
                    if (((j - x1) < -vdeviation && (j - x1) > -fw - vdeviation) && i > y1 - fw && i < y1 + Math.abs(hdeviation) + winw + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    //bottom
                    if ((j - x1) > vdeviation + winh && (j - x1) < vdeviation + winh + fw && i > y1 - fw && i < y1 + Math.abs(hdeviation) + winw + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                }
                else{
                    // left
                    if ((i - y1) < 0 + hdeviation && (i - y1) > -fw + hdeviation && j >= x1 - vdeviation && j <= x1 + winh + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // right
                    if ((i - y1) > winw && (i - y1) <  winw + fw && j >= x1 - vdeviation && j <= x1 + winh + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // top
                    if (((j - x1) < -vdeviation && (j - x1) > -fw - vdeviation) && i > y1 - fw + hdeviation && i < y1 + winw + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    //bottom
                    if ((j - x1) > vdeviation + winh && (j - x1) < vdeviation + winh + fw && i > y1 - fw + hdeviation && i < y1 + winw + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                }

                if ((i-y1) < 0 && (i-y1) > -fw && j >= x1 && j <= x1 + winh){
                    tempmatrix1[i][j] = new int[]{255,0,0};
                }
                if ((i-y1) > winw && (i-y1) < winw + fw && j >= x1 && j <= x1 + winh){
                    tempmatrix1[i][j] = new int[]{255,0,0};
                }
                if (((j-x1) < 0 && (j-x1) > -fw) && i > y1 - fw && i <y1 + winw + fw){
                    tempmatrix1[i][j] = new int[]{255,0,0};
                }
                if ((j-x1) > winh && (j-x1) < winh + fw && i > y1 - fw && i < y1 + winw + fw){
                    tempmatrix1[i][j] = new int[]{255,0,0};
                }

                if ((i-y2) < 0 && (i-y2) > -fw && j >= x2 && j <= x2 + winh){
                    tempmatrix2[i][j] = new int[]{255,0,0};
                }
                if ((i-y2) > winw && (i-y2) < winw + fw && j >= x2 && j <= x2 + winh){
                    tempmatrix2[i][j] = new int[]{255,0,0};
                }
                if (((j-x2) < 0 && (j-x2) > -fw) && i > y2 - fw && i <y2 + winw + fw){
                    tempmatrix2[i][j] = new int[]{255,0,0};
                }
                if ((j-x2) > winh && (j-x2) < winh + fw && i > y2 - fw && i < y2 + winw + fw){
                    tempmatrix2[i][j] = new int[]{255,0,0};
                }
            }
        }
        for(int k = 0; k<3; k++){
            for (int i = 0; i < winw; i++) {
                for (int j = 0; j < winh; j++) {
                    win1[i][j][k] = matrix1[i+y1][j+x1][k];
                    win2[i][j][k] = matrix2[i+y2][j+x2][k];
                    //System.out.println(win1[i][j][k] + " " + win2[i][j][k]);
                    residualw[i][j][k] = Math.min(Math.abs(win1[i][j][k] - win2[i][j][k])*3, 255);
                }
            }
            //System.out.println("\n\nNEXT K\n\n");
        }

        LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.MatrixToImage(tempmatrix1),4*simsize, (int)(4*simsize*heightsc))));
        RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.MatrixToImage(tempmatrix2),4*simsize, (int)(4*simsize*heightsc))));
        TopImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.MatrixToImage(win1),simsize,simsize)));
        BotImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.MatrixToImage(win2),simsize,simsize)));

        CenterImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.MatrixToImage(residualw),2*simsize,2*simsize)));
        STD.setText("STD: " + (double)logs[tempy][tempx][8]/dtis +  " and " + (double)logs[tempy][tempx][9]/dtis);
        Metrics.setText("Metrics: " + (double)logs[tempy][tempx][6]/dtis);
        Deviation.setText("Deviation: " + String.format(Locale.US,"%.3f", Math.abs((double)logs[tempy][tempx][7]/dtis)));
        CCounter.setText("Comparisons: " + logs[tempy][tempx][11]);
        Counter.setText("Window counter: " + counter);
        super.repaint();

    }
}
