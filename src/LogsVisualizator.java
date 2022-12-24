import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Locale;
import java.util.Optional;

public class LogsVisualizator extends JFrame {
    private Toolkit kit = Toolkit.getDefaultToolkit();
    JLabel LeftImageLabel = new JLabel("");
    JLabel RightImageLabel = new JLabel("");
    JLabel TopImageLabel = new JLabel("");
    JLabel BotImageLabel = new JLabel("");
    JLabel CenterImageLabel = new JLabel("");
    JLabel Counter = new JLabel();
    JLabel Metrics = new JLabel();
    JLabel Deviation = new JLabel();
    JButton Next = new JButton("→");
    JButton Previous = new JButton("←");
    ImageProcessor improc = new ImageProcessor();
    private Box cvBox = Box.createVerticalBox();
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
    public LogsVisualizator(MainFrame mf, int[][][] matrix1, int[][][] matrix2, int[][][] logs, int hdeviation, int vdeviation){
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

        CenterImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Counter.setAlignmentX(Component.CENTER_ALIGNMENT);
        Metrics.setAlignmentX(Component.CENTER_ALIGNMENT);
        Deviation.setAlignmentX(Component.CENTER_ALIGNMENT);

        bhBox.add(Box.createHorizontalGlue());
        bhBox.add(Previous);
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
        cvBox.add(Metrics);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Deviation);
        cvBox.add(Box.createVerticalGlue());
        cvBox.add(Counter);
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
                if(counter < logs.length*logs[0].length-1) {
                    counter++;
                    repaint();
                    Previous.setEnabled(true);
                }
                else{
                    JOptionPane.showMessageDialog(LogsVisualizator.this, "You have reached the last element");
                    Next.setEnabled(false);
                }
            }
        });
        Previous.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (counter > 0) {
                    counter--;
                    repaint();
                    Next.setEnabled(true);
                }
                else{
                    JOptionPane.showMessageDialog(LogsVisualizator.this, "You have reached the first element");
                    Previous.setEnabled(false);
                }
            }
        });
        Previous.setEnabled(false);
        //setSize(LeftImageLabel.getHeight() * 3, (int)(LeftImageLabel.getHeight() * 1.2));
        setSize(1300, 550);
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
        //col_image1, row_image1, coincidentx, coincidenty, (int)distance, sc_width, sc_height
        int[][][] tempmatrix1 = MCopy(matrix1);
        int[][][] tempmatrix2 = MCopy(matrix2);
        
        int tempx, tempy;
        tempy = (int)((double)counter/logs[0].length);
        tempx = counter%logs[0].length;

        int winw = logs[tempy][tempx][5];
        int winh = logs[tempy][tempx][6];
        int[][][] win1 = new int[winw][winh][3];
        int[][][] win2 = new int[winw][winh][3];
        int[][][] residualw = new int[winw][winh][3];
        System.out.println("WTF????" + winw + " " + winh);
        int fw = 3;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
            {

//                if (Math.pow((double)(i-logs[tempy][tempx][0])/Math.abs(hdeviation), 2) + Math.pow((double)(j-logs[tempy][tempx][1])/Math.abs(vdeviation), 2) > 1 && Math.pow((double)(i-logs[tempy][tempx][0])/(Math.abs(hdeviation) +fw), 2) + Math.pow((double)(j-logs[tempy][tempx][1])/(Math.abs(vdeviation) + fw), 2) < 1){
//                        tempmatrix1[i][j] = new int[]{255,255,255};
//                        tempmatrix2[i][j] = new int[]{255,255,255};
//                }
                if (hdeviation > 0) {
                    // left
                    if ((i - logs[tempy][tempx][0]) < 0 && (i - logs[tempy][tempx][0]) > -fw && j >= logs[tempy][tempx][1] - vdeviation && j <= logs[tempy][tempx][1] + winh + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // right
                    if ((i - logs[tempy][tempx][0]) > Math.abs(hdeviation) + winw && (i - logs[tempy][tempx][0]) < Math.abs(hdeviation) + winw + fw && j >= logs[tempy][tempx][1] - vdeviation && j <= logs[tempy][tempx][1] + winh + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // top
                    if (((j - logs[tempy][tempx][1]) < -vdeviation && (j - logs[tempy][tempx][1]) > -fw - vdeviation) && i > logs[tempy][tempx][0] - fw && i < logs[tempy][tempx][0] + Math.abs(hdeviation) + winw + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    //bottom
                    if ((j - logs[tempy][tempx][1]) > vdeviation + winh && (j - logs[tempy][tempx][1]) < vdeviation + winh + fw && i > logs[tempy][tempx][0] - fw && i < logs[tempy][tempx][0] + Math.abs(hdeviation) + winw + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                }
                else{
                    // left
                    if ((i - logs[tempy][tempx][0]) < 0 + hdeviation && (i - logs[tempy][tempx][0]) > -fw + hdeviation && j >= logs[tempy][tempx][1] - vdeviation && j <= logs[tempy][tempx][1] + winh + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // right
                    if ((i - logs[tempy][tempx][0]) > winw && (i - logs[tempy][tempx][0]) <  winw + fw && j >= logs[tempy][tempx][1] - vdeviation && j <= logs[tempy][tempx][1] + winh + vdeviation) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    // top
                    if (((j - logs[tempy][tempx][1]) < -vdeviation && (j - logs[tempy][tempx][1]) > -fw - vdeviation) && i > logs[tempy][tempx][0] - fw + hdeviation && i < logs[tempy][tempx][0] + winw + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                    //bottom
                    if ((j - logs[tempy][tempx][1]) > vdeviation + winh && (j - logs[tempy][tempx][1]) < vdeviation + winh + fw && i > logs[tempy][tempx][0] - fw + hdeviation && i < logs[tempy][tempx][0] + winw + fw) {
                        tempmatrix1[i][j] = new int[]{255, 255, 255};
                        tempmatrix2[i][j] = new int[]{255, 255, 255};
                    }
                }

                if ((i-logs[tempy][tempx][0]) < 0 && (i-logs[tempy][tempx][0]) > -fw && j >= logs[tempy][tempx][1] && j <= logs[tempy][tempx][1] + winh){
                    tempmatrix1[i][j] = new int[]{255,0,0};
                }
                if ((i-logs[tempy][tempx][0]) > winw && (i-logs[tempy][tempx][0]) < winw + fw && j >= logs[tempy][tempx][1] && j <= logs[tempy][tempx][1] + winh){
                    tempmatrix1[i][j] = new int[]{255,0,0};
                }
                if (((j-logs[tempy][tempx][1]) < 0 && (j-logs[tempy][tempx][1]) > -fw) && i > logs[tempy][tempx][0] - fw && i <logs[tempy][tempx][0] + winw + fw){
                    tempmatrix1[i][j] = new int[]{255,0,0};
                }
                if ((j-logs[tempy][tempx][1]) > winh && (j-logs[tempy][tempx][1]) < winh + fw && i > logs[tempy][tempx][0] - fw && i < logs[tempy][tempx][0] + winw + fw){
                    tempmatrix1[i][j] = new int[]{255,0,0};
                }

                if ((i-logs[tempy][tempx][2]) < 0 && (i-logs[tempy][tempx][2]) > -fw && j >= logs[tempy][tempx][3] && j <= logs[tempy][tempx][3] + winh){
                    tempmatrix2[i][j] = new int[]{255,0,0};
                }
                if ((i-logs[tempy][tempx][2]) > winw && (i-logs[tempy][tempx][2]) < winw + fw && j >= logs[tempy][tempx][3] && j <= logs[tempy][tempx][3] + winh){
                    tempmatrix2[i][j] = new int[]{255,0,0};
                }
                if (((j-logs[tempy][tempx][3]) < 0 && (j-logs[tempy][tempx][3]) > -fw) && i > logs[tempy][tempx][2] - fw && i <logs[tempy][tempx][2] + winw + fw){
                    tempmatrix2[i][j] = new int[]{255,0,0};
                }
                if ((j-logs[tempy][tempx][3]) > winh && (j-logs[tempy][tempx][3]) < winh + fw && i > logs[tempy][tempx][2] - fw && i < logs[tempy][tempx][2] + winw + fw){
                    tempmatrix2[i][j] = new int[]{255,0,0};
                }
            }
        }
        for(int k = 0; k<3; k++){
            for (int i = 0; i < winw; i++) {
                for (int j = 0; j < winh; j++) {
                        win1[i][j][k] = matrix1[i+logs[tempy][tempx][0]][j+logs[tempy][tempx][1]][k];
                        win2[i][j][k] = matrix2[i+logs[tempy][tempx][2]][j+logs[tempy][tempx][3]][k];
                        //System.out.println(win1[i][j][k] + " " + win2[i][j][k]);
                        residualw[i][j][k] = Math.min(Math.abs(win1[i][j][k] - win2[i][j][k])*3, 255);
                }
            }
            //System.out.println("\n\nNEXT K\n\n");
        }
        LeftImageLabel.setIcon(new ImageIcon(improc.MatrixToImage(tempmatrix1)));
        RightImageLabel.setIcon(new ImageIcon(improc.MatrixToImage(tempmatrix2)));
        TopImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.MatrixToImage(win1),100,100)));
        BotImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.MatrixToImage(win2),100,100)));

        CenterImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.MatrixToImage(residualw),200,200)));
        Metrics.setText("Metrics: " + (double)logs[tempy][tempx][7]/10000);
        Deviation.setText("Deviation: " + String.format(Locale.US,"%.3f", Math.abs((double)logs[tempy][tempx][4]/hdeviation)));
        Counter.setText("Window counter: " + counter);
        super.repaint();

    }
}
