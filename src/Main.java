import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import static java.awt.datatransfer.DataFlavor.javaFileListFlavor;


abstract class CompareMethod {
    protected int stride = 3;

    public void setStride(int stride){
        this.stride = stride;
    }
    public static int[] arrayRankTransform(byte[] arr) {
        int N = arr.length;
        //create result array and re-use it to store sorted elements of original array
        byte[] sorted = Arrays.copyOf(arr, N);
        int[] ranks = new int[N];
        Arrays.sort(sorted);
        //fill map of ranks based on sorted sequence of elements
        int counter, temp;
        for (int i = 0; i < N; i++){
            counter = 0;
            temp = 0;
            for (int j = 0; j < N; j++){
                if (arr[i] == sorted[j]){
                    counter++;
                    temp += j;
                }
            }
            ranks[i] = temp/counter;
        }
        //fill result array with ranks, sequence of elements must be preserved from original array
        return ranks;
    }


    public static int[] arrayRankTransform(int[] arr) {
        int N = arr.length;
        //create result array and re-use it to store sorted elements of original array
        int[] sorted = Arrays.copyOf(arr, N);
        int[] ranks = new int[N];
        Arrays.sort(sorted);
        //fill map of ranks based on sorted sequence of elements
        int counter, temp;
        for (int i = 0; i < N; i++){
            counter = 0;
            temp = 0;
            for (int j = 0; j < N; j++){
                if (arr[i] == sorted[j]){
                    counter++;
                    temp += j;
                }
            }
            ranks[i] = temp/counter;
        }
        //fill result array with ranks, sequence of elements must be preserved from original array
        return ranks;
    }
    public double get_similarity(byte[][][] scanim1, byte[][][] scanim2){ return 0;}
    public double get_similarity(int[][][] scanim1, int[][][] scanim2){ return 0;}
}

class NCC extends CompareMethod {

    public double get_similarity(byte[][][] scanim1, byte[][][] scanim2){
        int width = scanim1.length;
        int height = scanim1[0].length;
        double[] averageim1 = {0, 0, 0};
        double[] averageim2 = {0, 0, 0};
        double numerator;
        double denominator;
        double temp;
        double[] total = {0, 0, 0};
        int N = ((width+1)/this.stride)*((height+1)/this.stride);
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < width; i+=this.stride) {
                for (int j = 0; j < height; j+=this.stride) {
                    averageim1[k] += scanim1[i][j][k];
                    averageim2[k] += scanim2[i][j][k];
                }
            }
            averageim1[k] /= N;
            averageim2[k] /= N;
            numerator = 0;
            denominator = 0;
            temp = 0;
            for (int i = 0; i < width; i+=this.stride) {
                for (int j = 0; j < height; j+=this.stride) {
                    numerator += (scanim1[i][j][k] - averageim1[k]) * (scanim2[i][j][k] - averageim2[k]);
                    denominator += Math.pow((scanim1[i][j][k] - averageim1[k]), 2);
                    temp += Math.pow((scanim2[i][j][k] - averageim2[k]), 2);
                }
            }
            denominator = Math.sqrt(denominator) * Math.sqrt(temp);

            total[k] = numerator / denominator;
        }
        return (total[0] + total[1] + total[2])/3;
    }

    public double get_similarity(int[][][] scanim1, int[][][] scanim2){
        int width = scanim1.length;
        int height = scanim1[0].length;
        double[] averageim1 = {0, 0, 0};
        double[] averageim2 = {0, 0, 0};
        double numerator;
        double denominator;
        double temp;
        double[] total = {0, 0, 0};
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    averageim1[k] += scanim1[i][j][k];
                    averageim2[k] += scanim2[i][j][k];
                }
            }
            averageim1[k] /= width*height;
            averageim2[k] /= width*height;
            numerator = 0;
            denominator = 0;
            temp = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    numerator += (scanim1[i][j][k] - averageim1[k]) * (scanim2[i][j][k] - averageim2[k]);
                    denominator += Math.pow((scanim1[i][j][k] - averageim1[k]), 2);
                    temp += Math.pow((scanim2[i][j][k] - averageim2[k]), 2);
                }
            }
            denominator = Math.sqrt(denominator) * Math.sqrt(temp);

            total[k] = numerator / denominator;
        }
        return (total[0] + total[1] + total[2])/3;
    }
}


class SCC extends CompareMethod {

    public double get_similarity(byte[][][] scanim1, byte[][][] scanim2){
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = ((width+1)/this.stride)*((height+1)/this.stride);
        byte[] array1 = new byte[N];
        byte[] array2 = new byte[N];
        int[] ranks1;
        int[] ranks2;
        long[] d = {0,0,0};
        double[] total = {0, 0, 0};
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < width; i+=this.stride) {
                for (int j = 0; j < height; j+=this.stride) {
                    array1[height*i+j] = scanim1[i][j][k];
                    array2[height*i+j] = scanim2[i][j][k];
                }
            }
            ranks1 = arrayRankTransform(array1);
            ranks2 = arrayRankTransform(array2);
            for (int i = 0; i < N; i++) {
                d[k] += Math.pow(ranks1[i]-ranks2[i],2);
            }
            total[k] = (1 - (((double) d[k] / N) * 6) / (Math.pow(N,2) - 1));
        }
        return (total[0] + total[1] + total[2])/3;
    }

    public double get_similarity(int[][][] scanim1, int[][][] scanim2) {
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = width * height;
        int[] array1 = new int[N];
        int[] array2 = new int[N];
        int[] ranks1;
        int[] ranks2;
        long[] d = {0, 0, 0};
        double[] total = {0, 0, 0};
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    array1[height * i + j] = scanim1[i][j][k];
                    array2[height * i + j] = scanim2[i][j][k];
                }
            }
            ranks1 = arrayRankTransform(array1);
            ranks2 = arrayRankTransform(array2);
            for (int i = 0; i < N; i++) {
                d[k] += Math.pow(ranks1[i] - ranks2[i], 2);
            }
            total[k] = (1 - (((double) d[k] / N) * 6) / (Math.pow(N, 2) - 1));
        }
        return (total[0] + total[1] + total[2]) / 3;
    }
}


class KCC extends CompareMethod {

    public double get_similarity(byte[][][] scanim1, byte[][][] scanim2) {
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = ((width+1)/this.stride)*((height+1)/this.stride);
        byte[] array1 = new byte[N];
        byte[] array2 = new byte[N];
        int[] ranks1;
        int[] ranks2;
        double[] t = {0,0,0};
        double[] total = {0, 0, 0};
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < width; i+=this.stride) {
                for (int j = 0; j < height; j+=this.stride) {
                    array1[height*i+j] = scanim1[i][j][k];
                    array2[height*i+j] = scanim2[i][j][k];
                }
            }
            ranks1 = arrayRankTransform(array1);
            ranks2 = arrayRankTransform(array2);
            for (int i = 0; i < N; i++) {
                for(int j = i+1; j < N; j++){
                    t[k] += Integer.signum(ranks1[i] - ranks1[j])*Integer.signum(ranks2[i] - ranks2[j]);
                }
            }
            total[k] = ((double)2*t[k]/N)/(N - 1);
            //System.out.println("CB: "+ c[k] +" "+ b[k]+" "+total[k]);
        }
        return (total[0] + total[1] + total[2])/3;
    }

    public double get_similarity(int[][][] scanim1, int[][][] scanim2) {
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = width*height;
        int[] array1 = new int[N];
        int[] array2 = new int[N];
        int[] ranks1;
        int[] ranks2;
        double[] t = {0,0,0};
        double[] total = {0, 0, 0};
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    array1[height*i+j] = scanim1[i][j][k];
                    array2[height*i+j] = scanim2[i][j][k];
                }
            }
            ranks1 = arrayRankTransform(array1);
            ranks2 = arrayRankTransform(array2);
            for (int i = 0; i < N; i++) {
                for(int j = i+1; j < N; j++){
                    t[k] += Integer.signum(ranks1[i] - ranks1[j])*Integer.signum(ranks2[i] - ranks2[j]);
                }
            }
            total[k] = ((double)2*t[k]/N)/(N - 1);
            //System.out.println("CB: "+ c[k] +" "+ b[k]+" "+total[k]);
        }
        return (total[0] + total[1] + total[2])/3;
    }

}


class SAD extends CompareMethod {
    public double get_similarity(byte[][][] scanim1, byte[][][] scanim2) {
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = ((width+1)/this.stride)*((height+1)/this.stride);
        double[] total = {0, 0, 0};
        for (int i = 0; i < width; i+=this.stride) {
            for (int j = 0; j < height; j+=this.stride) {
                for (int k = 0; k < 3; k++) {
                    total[k] += Math.abs(scanim1[i][j][k] - scanim2[i][j][k]);
                }
            }
        }

        return 1 - (total[0] + total[1] + total[2])/(3*255*N);
    }

    public double get_similarity(int[][][] scanim1, int[][][] scanim2) {
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = width*height;
        double[] total = {0, 0, 0};
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    total[k] += Math.abs(scanim1[i][j][k] - scanim2[i][j][k]);
                }
            }
        }

        return 1 - (total[0] + total[1] + total[2])/(3*255*N);
    }

    // TODO: Fix metrics
    public double get_similarity_norm(int[][][] scanim1, int[][][] scanim2) {
        ImageProcessor impr = new ImageProcessor();
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = width*height;

        double mean1 = impr.Average(scanim1);
        double mean2 = impr.Average(scanim2);

        double std1 = impr.Std(scanim1);
        double std2 = impr.Std(scanim2);

        double min = 255;
        double max = 0;
        double[] total = {0, 0, 0};
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    double temp1 = (scanim1[i][j][k]-mean1)/std1;
                    double temp2 = (scanim2[i][j][k]-mean2)/std2;
                    total[k] += Math.abs((temp1 - temp2));
                    min = Math.min(min, temp1);
                    min = Math.min(min, temp2);
                    max = Math.max(max, temp1);
                    max = Math.max(max, temp2);
                }
            }
        }

        return 1 - (total[0] + total[1] + total[2])/(3*((max-min)*N));
    }
}


class SSD extends CompareMethod {

    public double get_similarity(byte[][][] scanim1, byte[][][] scanim2) {
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = ((width+1)/this.stride)*((height+1)/this.stride);
        double[] total = {0, 0, 0};
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j+=this.stride) {
                for (int k = 0; k < 3; k+=this.stride) {
                    total[k] += Math.pow(scanim1[i][j][k] - scanim2[i][j][k], 2);
                }
            }
        }
        return 1 - (total[0] + total[1] + total[2])/(3*255*255*N);
    }

    public double get_similarity(int[][][] scanim1, int[][][] scanim2) {
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = width*height;
        double[] total = {0, 0, 0};
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    total[k] += Math.pow(scanim1[i][j][k] - scanim2[i][j][k], 2);
                }
            }
        }

        return 1 - (total[0] + total[1] + total[2])/(3*255*N);
    }
}


class MainFrame extends JFrame {
    int guiImageWidth = 345; //360, 540, 280
    int guiImageHeight = 260; //280, 360, 240

    //new File("D:\\Images\\left.jpg");
    File RI = null;
    // new File("D:\\Images\\right.jpg");
    BufferedImage image1 = null;
    BufferedImage image2 = null;
    BufferedImage gradientstripe;
    BufferedImage buff;
    BufferedImage DepthMap;
    BufferedImage DepthMap_full;

    Font FONT = new Font("OpenSans", Font.BOLD, 14);

    Deque<GenState> LogsStack = new ArrayDeque<>();
    BufferedImage ShiftedImage;

    boolean adaptive_mode;
    boolean autosave_mode;
    boolean approximate_mode;
    boolean localized_mode;
    int window_size;
    int filter_size;
    int n_segments;
    int stride;
    double ext_coef;
    CompareMethod method;

    //    Map<String, Object> gen_params = new HashMap<String, Object>();
    byte[][][] matrix1;//first image
    byte[][][] matrix2;//second image
    public double[][] matrix3;//deviation matrix
    public int[][][] logs;
    public double[][][] correlation_m;
    public LogsVisualizator lv;
    public DMComparator dmc;
    public PlotFrame pf;
    int iwidth, iheight; //ширина и высота фотки
    JFrame frame = new JFrame();
    JPanel panel = new JPanel();
    JLabel Ncc = new JLabel("NCC");
    JLabel Scc = new JLabel("SCC");
    JLabel Kcc = new JLabel("KCC");
    JLabel Sad = new JLabel("SAD");
    JLabel Ssd = new JLabel("SSD");
    JLabel BW = new JLabel("BW");
    JLabel SOBEL = new JLabel("SOBEL");
    JLabel PREVITT = new JLabel("PREVITT");
    JLabel NONE = new JLabel("NONE");
    JLabel WindowSizeLabel = new JLabel("Window size");
    JLabel VdevLabel = new JLabel("MVDev");
    JLabel TimeLabel = new JLabel("Time, ms");
    JLabel IterLabel = new JLabel("Progress");
    JLabel StrideLabel = new JLabel("Stride");
    JLabel NSLabel = new JLabel("NS");
    JLabel ECLabel = new JLabel("EC");

    JLabel Selections = new JLabel("");
    //JLabel TopImageLabel = new JLabel("Main image");
    JLabel LeftImageLabel = new JLabel("");
    JLabel RightImageLabel = new JLabel("");
    JLabel BottomImageLabel = new JLabel("");
    JLabel GradientOfColors = new JLabel();
    JRadioButton ncc = new JRadioButton("", false);
    JRadioButton scc = new JRadioButton("", false);
    JRadioButton kcc = new JRadioButton("", false);
    JRadioButton sad = new JRadioButton("", false);
    JRadioButton ssd = new JRadioButton("", false);
    JCheckBox bw = new JCheckBox("BW", false);
    JRadioButton Sobel = new JRadioButton("", false);
    JRadioButton Previtt = new JRadioButton("", false);
    JRadioButton none = new JRadioButton("", true);
    JButton GoMakeSomeMagic = new JButton("Run");
    JButton LoadDM = new JButton("LDM");
    JButton GetMetrics = new JButton("GM");
    JButton ShowLogs = new JButton("GL");
    JButton Save = new JButton("Save");
    JButton SelectLeftImage = new JButton("Load Left Image");
    JButton SelectRightImage = new JButton("Load Right Image");
    ButtonGroup methods = new ButtonGroup();
    ButtonGroup filtration = new ButtonGroup();
    JFileChooser loadimage = new JFileChooser();
    Box zero = Box.createHorizontalBox();
    Box first = Box.createHorizontalBox();
    Box second = Box.createHorizontalBox();
    Box third = Box.createHorizontalBox();
    Box fourth = Box.createHorizontalBox();
    Box fifth = Box.createHorizontalBox();
    Box sixth = Box.createHorizontalBox();
    Box firstvert = Box.createVerticalBox();
    Box finalvert = Box.createVerticalBox();
    Box deepmap = Box.createHorizontalBox();
    Box methdsb = Box.createVerticalBox();
    Box nccb = Box.createHorizontalBox();
    Box sadb = Box.createHorizontalBox();
    Box ssdb = Box.createHorizontalBox();
    Box sccb = Box.createHorizontalBox();
    Box kccb = Box.createHorizontalBox();
    Box parambox = Box.createHorizontalBox();
    JCheckBox AutoScaleCB = new JCheckBox("AutoScale");
    JCheckBox AdaptiveSizeCB = new JCheckBox("AdaptSize");
    JCheckBox ConvApprxCB = new JCheckBox("ConvApprx");
    JCheckBox ApprxAlgsCB = new JCheckBox("AX");
    JCheckBox AutoSaveCB = new JCheckBox("AutoSave");

    JCheckBox LocDevsCB = new JCheckBox("LD");

    private int WS = 10;
    private int FS = 1;
    private final int NSEG = 9;
    private final int STRIDE = 1;
    private final int VDEV = 0;
    private final double EC = 2.2;

    JTextField WindowSizeTF = new JTextField(Integer.toString(WS), 3);
    JTextField FilterSizeTF = new JTextField(Integer.toString(FS), 2);
    JTextField NSegmentsTF = new JTextField(Integer.toString(NSEG), 2);
    JTextField StrideTF = new JTextField(Integer.toString(STRIDE), 2);
    JTextField ECoefTF = new JTextField(Double.toString(EC), 3);

    JTextField VdevTF = new JTextField("0", 2);
    JTextField TimeTF = new JTextField("0", 5);
    JTextField IterTF = new JTextField("0", 5);


    JLabel text = new JLabel("Filter order");
    JButton ApplyOperation = new JButton("Apply");
    JButton UndoOperation = new JButton("Undo");
    //JComboBox Function = new JComboBox(new String[]{"amedian", "wmedian","prewitt","sobel","median", "avg", "min", "max", "gamma", "clarity", "equalize"});
    JComboBox Operation = new JComboBox(new String[]{"auto", "amedian", "median", "min", "max", "equalize"});

    GenState current_state;

    JProgressBar JPB = new JProgressBar();

    private Toolkit kit = Toolkit.getDefaultToolkit();
    private Clipboard clipboard = kit.getSystemClipboard();
    private ImageTransferable imageSelection;
    private DataFlavor flavor;

    ImageProcessor improc = new ImageProcessor();
    int[] size_adjustment = {0, 0};
    private int max_deviation;
    private int vdev;
    private int counter4saving = 0;

    private int itercounter;
    private double progress;
    public static int dtis = 10000; // scale for converting double to int and then backwards

    public BufferedImage THImage;
    public BufferedImage DevsImage;

    int start, finish;

    int interpol_choice = 2;

    // Declare all actions used
    private Action selectLAction;
    private Action selectRAction;
    private Action saveAction;
    private Action applyAction;
    private Action undoAction;
    private Action runAction;
    private Action loadDMAction;
    private Action showLogsAction;
    private Action getMetricsAction;
    private Action copyAction;
    private Action pasteAction;
    private Action closeAction;
    private Action helpAction;

    private Action SAD_Action;
    private Action SSD_Action;
    private Action NCC_Action;
    private Action SCC_Action;
    private Action KCC_Action;

    private DMGenerator DMGen;

    String sep = ""+File.separatorChar;

    private final String DataPath = "Data"+sep;
    private final String MapsPath = DataPath+"Maps"+sep;
    private final String ThresholdsPath = DataPath+"Thresholds"+sep;
    private final String ShiftedIPath = DataPath+"Shifted_Images"+sep;
    private final String DeviationsPath = DataPath+"Deviations"+sep;

    private final Pattern floatPattern = Pattern.compile("(0|([1-9][0-9]*))(\\.[0-9]+)?");// \d+\.?\d+ //[^0-9]*
    private final Pattern intPattern = Pattern.compile("[1-9][0-9]*");


    public int[][][] BtoIMatrix(byte[][][] matrix) {
        int width = matrix.length;
        int height = matrix[0].length;
        int[][][] result = new int[width][height][3];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++){
                    result[i][j][k] = matrix[i][j][k] + 128;
                }
            }
        }
        return result;
    }

    public int[][] Transpose(int[][] scanim){
        int width = scanim.length;
        int height = scanim[0].length;
        int[][] transposed = new int[height][width];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                transposed[j][i] = scanim[i][j];
            }
        }

        return transposed;
    }

    private void LLImage(File file) throws IOException {
        image1 = ImageIO.read(file);
        iwidth = image1.getWidth();
        iheight = image1.getHeight();
        frame.getContentPane();
        LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(image1, guiImageWidth, guiImageHeight, interpol_choice)));
    }
    private void LRImage(File file) throws IOException {
        image2 = ImageIO.read(file);
        iwidth = image2.getWidth();
        iheight = image2.getHeight();
        frame.getContentPane();
        RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(image2, guiImageWidth, guiImageHeight, interpol_choice)));
    }

    private void LDM(File file) throws IOException{
        buff = ImageIO.read(file); //improc.SizeChangerS(ImageIO.read(file), iwidth, iheight, apprx_choice);
        if (DepthMap != null)
            GetMetrics.setEnabled(true);
    }
    private void LoadImage(String which){
        try {
            File file = null;
            int ret = loadimage.showDialog(null, "Load "+which+" image");
            if (ret == JFileChooser.APPROVE_OPTION) {
                file = loadimage.getSelectedFile();
            }
            System.out.println(file);
            //image1 = improc.SizeChangerLinear(ImageIO.read(LI), guiImageWidth*2, guiImageHeight*2);
            if (which.equals("left")) {
                LLImage(file);
            }
            else{
                LRImage(file);
            }
            System.out.println("Relation: " + iheight + " " + iwidth + " " + guiImageWidth * 2 + " " + guiImageHeight * 2 * iheight / iwidth);

            if (image1 != null && image2 != null){
                WS = (int)((double)Math.min(iwidth, iheight)/100);
                WindowSizeTF.setText(Integer.toString(WS));
                GoMakeSomeMagic.setEnabled(true);
            }

            UndoOperation.setEnabled(false);
            LogsStack = new ArrayDeque<>();
            frame.setVisible(true);
        } catch (Exception ignored) {
            JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong while reading, try again");
        }
    }

    private Integer parseInt(JTextField tf, int basev){
        String s = tf.getText();
        Matcher m = intPattern.matcher(s);
//        System.out.println("WAT?"+m);

        int value;
        try{
            m.find();
            s = m.group();
            value = Integer.parseInt(s);
            if (value >= 1){
                tf.setText(Integer.toString(value));
                repaint();
                return value;
            }
            else
                throw new Exception();
        }catch (Exception e){
            tf.setText(Integer.toString(basev));
//            JOptionPane.showMessageDialog(MainFrame.this, "Incorrect input format in the field");
            return basev;
        }

    }

    private Double parseDouble(JTextField tf, double basev){
        String s = tf.getText();
        Matcher m = floatPattern.matcher(s);
        double value;
        try{
            m.find();
            s = m.group();
            value = Double.parseDouble(s);
            if (value > 0.1){
                tf.setText(Double.toString(value));
                repaint();
                return value;
            }
            else
                throw new Exception();
        }catch (Exception e){
            tf.setText(Double.toString(basev));
//            JOptionPane.showMessageDialog(MainFrame.this, "Incorrect input format in the param field");
            return basev;
        }

    }
    private void ApplyFilter(){

        String str = Operation.getSelectedItem().toString();
        filter_size = parseInt(FilterSizeTF, FS);
        improc.setSize(filter_size);

        switch (str) {
            case "auto":
//                improc.setSize(FS);
//                for (int i=0; i<filter_size; i++) {
//                    improc.loadFull(DepthMap);
//                    DepthMap = improc.AutoFiltration();
//                }
                double similarity;
                BufferedImage last;
                CompareMethod cm = new SAD();
                int counter = 0;
                do{
                    last = improc.ImageCopy(DepthMap);
                    improc.loadFull(DepthMap);
                    DepthMap = improc.OrderStatFiltration("amedian");
                    similarity = cm.get_similarity(improc.ImageToBMatrix(DepthMap), improc.ImageToBMatrix(last));
                    counter++;
                    System.out.println("Amedian " + counter + " was applied " + similarity);
                }while(similarity < 0.998 && counter < 10);
                break;

            case "median":
                improc.loadFull(DepthMap);
                DepthMap = improc.OrderStatFiltration("median");
                break;

            case "min":
                improc.loadFull(DepthMap);
                DepthMap = improc.OrderStatFiltration("min");
                break;

            case "max":
                improc.loadFull(DepthMap);
                DepthMap = improc.OrderStatFiltration("max");
                break;

//            case "avg":
//                improc.loadFull(DepthMap);
//                DepthMap = improc.OrderStatFiltration("avg");
//                break;

            case "amedian":
                improc.loadFull(DepthMap);
                DepthMap = improc.OrderStatFiltration("amedian");
                break;

            case "equalize":
                improc.loadFull(DepthMap);
                DepthMap = improc.ImageContrastIncrease();
                break;

//            case "wmedian":
//                improc.loadFull(DepthMap);
//                DepthMap = improc.WeightedMedian();

        }
        if (AutoScaleCB.isSelected())
            DepthMap = improc.ImageCopy(improc.ImageScaler(DepthMap));
        GenState new_state = new GenState(DepthMap, current_state.logs, current_state.correlation_m, current_state.window_size, current_state.vdev);
        LogsStack.push(new_state);
        DepthMap_full = MatrixToImage(getFullMap(improc.BWImageToMatrix(DepthMap),
                DepthMap_full.getWidth(), DepthMap_full.getHeight()));
        BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(DepthMap_full,
                guiImageWidth, guiImageHeight, interpol_choice)));
        UndoOperation.setEnabled(true);
    }

    private class GenState{
        private BufferedImage DM;
        private int[][][] logs;
        private double[][][] correlation_m;
        private int window_size;
        private int vdev;
        public GenState(BufferedImage DM, int[][][] logs, double[][][] correlation_m, int window_size, int vdev){
            this.DM = improc.ImageCopy(DM);
            this.logs = logs;
            this.correlation_m = correlation_m;
            this.window_size = window_size;
            this.vdev = vdev;
        }
        public BufferedImage getDM(){
            return DM;
        }

        public int getVdev() {
            return vdev;
        }

        public double[][][] getCorrelation_m() {
            return correlation_m;
        }

        public int getWindow_size() {
            return window_size;
        }

        public int[][][] getLogs() {
            return logs;
        }
    }

    private void UndoChanges(){
        if (LogsStack.size() > 1) {
            LogsStack.pop();
            current_state = LogsStack.peek();
            DepthMap = current_state.getDM();
            logs = current_state.getLogs();
            if (logs != null)
                ShowLogs.setEnabled(false);
            correlation_m = current_state.getCorrelation_m();
            window_size = current_state.getWindow_size();
            vdev = current_state.getVdev();
            DepthMap_full = MatrixToImage(getFullMap(improc.BWImageToMatrix(DepthMap),
                    DepthMap_full.getWidth(), DepthMap_full.getHeight()));
            BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(DepthMap_full,
                    guiImageWidth, guiImageHeight, interpol_choice)));

        }
        if (LogsStack.size() <= 1){
            UndoOperation.setEnabled(false);
            JOptionPane.showMessageDialog(MainFrame.this,
                    "You've reached the first element");
        }
    }

    /**
     * This class allows to work with images in the clipboard
     */
    public static class ImageTransferable implements java.awt.datatransfer.Transferable {
        private final Image img;

        public ImageTransferable(Image image) {
            img = image;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.imageFlavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (flavor.equals(DataFlavor.imageFlavor)) {
                return img;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }

    private void ConfigureAllActions(){
        selectLAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LoadImage("left");
            }
        };
        SelectLeftImage.addActionListener(selectLAction);

        selectRAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LoadImage("right");
            }
        };
        SelectRightImage.addActionListener(selectRAction);

        applyAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ApplyFilter();
            }
        };
        ApplyOperation.addActionListener(applyAction);

        undoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                UndoChanges();
            }
        };
        UndoOperation.addActionListener(undoAction);

        loadDMAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    int dmret = loadimage.showDialog(null, "Load ground-true map");
                    File temp = null;
                    if (dmret == JFileChooser.APPROVE_OPTION) {
                        temp = loadimage.getSelectedFile();
                    }
                    LDM(temp);
                } catch (Exception ignored) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong while reading, try again");
                }
            }
        };
        LoadDM.addActionListener(loadDMAction);

        getMetricsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dmc = new DMComparator(MainFrame.this, buff, DepthMap_full, false);
            }
        };
        GetMetrics.addActionListener(getMetricsAction);

        showLogsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                lv = new LogsVisualizator(MainFrame.this, image1, image2, logs, correlation_m, vdev, dtis);
            }
        };
        ShowLogs.addActionListener(showLogsAction);

        saveAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SaveResults();
            }
        };
        Save.addActionListener(saveAction);

        runAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                DMGen = new DMGenerator();
                DMGen.execute();

            }
        };
        GoMakeSomeMagic.addActionListener(runAction);

        copyAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                imageSelection = new ImageTransferable(DepthMap_full);
                clipboard.setContents(imageSelection, null);
            }
        };
        pasteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                flavor = DataFlavor.imageFlavor;
                if (clipboard.isDataFlavorAvailable(flavor)) {
                    try {
                        ShowLogs.setEnabled(false);
                        DepthMap_full = (BufferedImage) clipboard.getData(flavor);
                        DepthMap = MatrixToImage(Transpose(getCompressedMap(improc.BWImageToMatrix(DepthMap_full))));
                        WindowSizeTF.setText(Integer.toString(window_size));
                        BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(DepthMap_full,
                                guiImageWidth, guiImageHeight, interpol_choice)));

                        current_state = new GenState(DepthMap, null, null, window_size, 0);
                        LogsStack.push(current_state);

                    } catch (UnsupportedFlavorException | IOException unsupportedFlavorException) {
                        unsupportedFlavorException.printStackTrace();
                    }
                }
            }
        };

        SAD_Action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                sad.setSelected(true);
            }
        };
        SSD_Action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ssd.setSelected(true);
            }
        };
        NCC_Action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ncc.setSelected(true);
            }
        };
        SCC_Action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                scc.setSelected(true);
            }
        };
        KCC_Action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                kcc.setSelected(true);
            }
        };

        helpAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JOptionPane.showMessageDialog(MainFrame.this, "========== Help ===========\n" +
                        "For a program to work you need to load stereo images first\n" +

                        "\nLoad Images:\n" +
                        "Alt+1 - load left image\n" +
                        "Alt+2 - load right image\n" +

                        "\nDepth map (DM) hotkeys:\n" +
                        "Ctrl+A - apply the chosen filter to the DM\n" +
                        "Ctrl+Z - undoes your last DM action\n" +
                        "Ctrl+S - saves DM to the ./Maps folder\n" +
                        "Ctrl+C - copies DM to the clipboard.\n" +
                        "Ctrl+V - pastes clipboard into the program as a DM\n" +

                        "\nFunctional windows:\n" +
                        "Ctrl+R - run DM estimator with the current parameters\n" +
                        "Ctrl+D - load ground-true DM for metrics calculation\n" +
                        "Ctrl+F - calculate DM metrics\n" +
                        "Ctrl+H - show this help window\n" +
                        "Ctrl+Q - quit the program (or its current window)\n" +

                        "\nSimilarities:\n" +
                        "Ctrl+1 - use SAD (Sum of absolute deviations)\n" +
                        "Ctrl+2 - use SSD (Sum of squared deviations)\n" +
                        "Ctrl+3 - use NCC (Pearson correlation)\n" +
                        "Ctrl+4 - use SCC (Spearman correlation)\n" +
                        "Ctrl+5 - use KCC (Kendall correlation)\n");
            }
        };
        closeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ClearWindows();
                frame.setVisible(false);
                frame.dispose();
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
    private void ConfigureKeyBindings(){
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = panel.getActionMap();

        actionMap.put("SelectL", GenClickAction(SelectLeftImage));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK), "SelectL");

        actionMap.put("SelectR", GenClickAction(SelectRightImage));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK), "SelectR");

        actionMap.put("Run", GenClickAction(GoMakeSomeMagic));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "Run");

//        actionMap.put("Logs", showLogsAction);
        actionMap.put("Logs", GenClickAction(ShowLogs));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "Logs");

//        actionMap.put("Apply", applyAction);
        actionMap.put("Apply", GenClickAction(ApplyOperation));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), "Apply");

        actionMap.put("Load", GenClickAction(LoadDM));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "Load");

        actionMap.put("Metrics", GenClickAction(GetMetrics));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "Metrics");

        actionMap.put("Undo", GenClickAction(UndoOperation));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");

        actionMap.put("Save", GenClickAction(Save));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "Save");


        actionMap.put("SAD", SAD_Action);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK), "SAD");
        actionMap.put("SSD", SSD_Action);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK), "SSD");
        actionMap.put("NCC", NCC_Action);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_DOWN_MASK), "NCC");
        actionMap.put("SCC", SCC_Action);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_DOWN_MASK), "SCC");
        actionMap.put("KCC", KCC_Action);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_DOWN_MASK), "KCC");


        actionMap.put("Copy", copyAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "Copy");

        actionMap.put("Paste", pasteAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "Paste");

        actionMap.put("Help", helpAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK), "Help");

        actionMap.put("Close", closeAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), "Close");
    }

    public void ConfigureAllTips(){
        GoMakeSomeMagic.setToolTipText("Click to start generation process");
        ApplyOperation.setToolTipText("Apply the chosen filter to the depth map");
        LoadDM.setToolTipText("Load ground-truth depth map");
        GetMetrics.setToolTipText("Get metrics");
        ShowLogs.setToolTipText("Show the process of generation");
        Save.setToolTipText("Save the depth map to the ./Maps folder");
        UndoOperation.setToolTipText("Undo your last DM action");
        AdaptiveSizeCB.setToolTipText("Better results, slower generation");
        NSegmentsTF.setToolTipText("Number of segments for adaptive alg");
        StrideTF.setToolTipText("Pooling size");
        VdevTF.setToolTipText("Max vertical deviation, slows generation");
        ECoefTF.setToolTipText("Extension coefficient, scales search area");
        ApprxAlgsCB.setToolTipText("Worse results, quicker generation");

        sadb.setToolTipText("Sum of absolute deviations");
        ssdb.setToolTipText("Sum of squared deviations");
        nccb.setToolTipText("Normalized correlation coefficient");
        sccb.setToolTipText("Spearman correlation coefficient");
        kccb.setToolTipText("Kendall correlation coefficient");
        Operation.setToolTipText("Smoothest - amedian, strongest - wmedian");

    }

    public void setEnabled(boolean state){
        panel.setEnabled(state);
//        GoMakeSomeMagic.setEnabled(state);
//        ApplyOperation.setEnabled(state);

    }
    public void setVisible(boolean state){
        frame.setVisible(state);
    }

    public static void changeFont (Component component, Font font)
    {

        if (!(component instanceof JTextField)) {
            component.setFont(font);
            if (component instanceof Container) {
                for (Component child : ((Container) component).getComponents()) {
                    changeFont(child, font);
                }
            }
        }else{
            component.setFont(font.deriveFont(Font.PLAIN));
        }
    }

    public MainFrame() throws IOException {
        String path = System.getProperty("user.dir");

        CreateDirectories();

        loadimage.setCurrentDirectory(new File(path + sep + "StereoImages" + sep + "Middlebury_WithGT"));
        loadimage.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"));

        frame.setLayout(new BorderLayout());
        frame.setSize(guiImageWidth * 2 + 10, guiImageHeight * 2 + 36); //размер фрейма 35 70
        frame.setTitle("DMGen by Kirill Kolesnikov, inspired by Oleg Kovalev & Mikalai Yatskou");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        panel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(javaFileListFlavor);

                    for (File file: droppedFiles) {
                        String[] temp = file.toString().split(sep);
                        String name = temp[temp.length - 1].toLowerCase();
                        System.out.println(name);
                        boolean ll = name.contains("left") || name.contains("0."); // check if there is a left image
                        boolean lr = name.contains("right") || name.contains("1."); // check if there is a right image
                        boolean ldm = name.contains("gt") || name.contains("ground") || name.contains("true");  // check if there is a depth map
                        if (ll || lr){
                            if (ll)
                                LLImage(file);

                            if (lr)
                                LRImage(file);
                            UndoOperation.setEnabled(false);
                            LogsStack = new ArrayDeque<>();
                            frame.setVisible(true);
                            if (image1 != null && image2 != null){
                                WS = (int)((double)Math.min(iwidth, iheight)/100);
                                WindowSizeTF.setText(Integer.toString(WS));
                                GoMakeSomeMagic.setEnabled(true);
                            }
                        }
                        if (ldm)
                            LDM(file);
                    }




                    evt.dropComplete(true);
                } catch (Exception ignored) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong while reading, try again");
                }
            }
        });

        WindowSizeTF.setMaximumSize(WindowSizeTF.getPreferredSize());
        VdevTF.setMaximumSize(VdevTF.getPreferredSize());
        FilterSizeTF.setMaximumSize(FilterSizeTF.getPreferredSize());
        TimeTF.setMaximumSize(TimeTF.getPreferredSize());
        IterTF.setMaximumSize(IterTF.getPreferredSize());
        StrideTF.setMaximumSize(VdevTF.getPreferredSize());
        NSegmentsTF.setMaximumSize(NSegmentsTF.getPreferredSize());
        ECoefTF.setMaximumSize(ECoefTF.getPreferredSize());
        WindowSizeTF.setHorizontalAlignment(JTextField.CENTER);
        VdevTF.setHorizontalAlignment(JTextField.CENTER);
        FilterSizeTF.setHorizontalAlignment(JTextField.CENTER);
        TimeTF.setHorizontalAlignment(JTextField.CENTER);
        IterTF.setHorizontalAlignment(JTextField.CENTER);
        StrideTF.setHorizontalAlignment(JTextField.CENTER);
        NSegmentsTF.setHorizontalAlignment(JTextField.CENTER);
        ECoefTF.setHorizontalAlignment(JTextField.CENTER);


        AdaptiveSizeCB.setHorizontalTextPosition(SwingConstants.LEFT);
        ApprxAlgsCB.setHorizontalTextPosition(SwingConstants.LEFT);
        AutoScaleCB.setHorizontalTextPosition(SwingConstants.LEFT);
        AutoSaveCB.setHorizontalTextPosition(SwingConstants.LEFT);

        TimeTF.setEnabled(false);
        IterTF.setEnabled(false);

        methods.add(ncc);
        methods.add(scc);
        methods.add(kcc);
        methods.add(sad);
        methods.add(ssd);
        filtration.add(Sobel);
        filtration.add(Previtt);
        filtration.add(none);

        nccb.add(Box.createHorizontalGlue());
        nccb.add(Ncc);
        nccb.add(Box.createHorizontalGlue());
        nccb.add(ncc);
        nccb.add(Box.createHorizontalGlue());

        sadb.add(Box.createHorizontalGlue());
        sadb.add(Sad);
        sadb.add(Box.createHorizontalGlue());
        sadb.add(sad);
        sadb.add(Box.createHorizontalGlue());

        ssdb.add(Box.createHorizontalGlue());
        ssdb.add(Ssd);
        ssdb.add(Box.createHorizontalGlue());
        ssdb.add(ssd);
        ssdb.add(Box.createHorizontalGlue());

        sccb.add(Box.createHorizontalGlue());
        sccb.add(Scc);
        sccb.add(Box.createHorizontalGlue());
        sccb.add(scc);
        sccb.add(Box.createHorizontalGlue());

        kccb.add(Box.createHorizontalGlue());
        kccb.add(Kcc);
        kccb.add(Box.createHorizontalGlue());
        kccb.add(kcc);
        kccb.add(Box.createHorizontalGlue());


        methdsb.add(Box.createVerticalStrut(12));
        methdsb.add(sadb);
        methdsb.add(Box.createVerticalGlue());
        methdsb.add(ssdb);
        methdsb.add(Box.createVerticalGlue());
        methdsb.add(nccb);
        methdsb.add(Box.createVerticalGlue());
        methdsb.add(sccb);
        methdsb.add(Box.createVerticalGlue());
        methdsb.add(kccb);
        methdsb.add(Box.createVerticalStrut(6));


        zero.add(Box.createHorizontalGlue());
        zero.add(SelectLeftImage);
        zero.add(Box.createHorizontalGlue());
        zero.add(SelectRightImage);
        zero.add(Box.createHorizontalGlue());

        first.add(Box.createHorizontalGlue());
        first.add(Operation);
        first.add(Box.createHorizontalGlue());
        first.add(ApplyOperation);
        first.add(Box.createHorizontalGlue());
        first.add(UndoOperation);
        first.add(Box.createHorizontalGlue());

        second.add(Box.createHorizontalGlue());
        second.add(text);
        second.add(Box.createHorizontalGlue());
        second.add(FilterSizeTF);
        second.add(Box.createHorizontalGlue());
        second.add(AutoScaleCB);
        second.add(Box.createHorizontalGlue());
        second.add(AutoSaveCB);
        second.add(Box.createHorizontalGlue());


        third.add(Box.createHorizontalGlue());
        third.add(AdaptiveSizeCB);
        third.add(Box.createHorizontalGlue());
        third.add(NSLabel);
        third.add(Box.createHorizontalGlue());
        third.add(NSegmentsTF);
        //third.add(ConvolutionApproximation);
        third.add(Box.createHorizontalGlue());
        third.add(ApprxAlgsCB);
        third.add(Box.createHorizontalGlue());
        third.add(StrideLabel);
        third.add(Box.createHorizontalGlue());
        third.add(StrideTF);
        third.add(Box.createHorizontalGlue());
//        third.add(AutoSaveCB);
//        third.add(Box.createHorizontalGlue());

        // HERE
        fourth.add(Box.createHorizontalGlue());
        fourth.add(VdevLabel);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(VdevTF);
        fourth.add(Box.createHorizontalGlue());
//        fourth.add(Box.createHorizontalStrut(8));
        fourth.add(ECLabel);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(ECoefTF);
        fourth.add(Box.createHorizontalGlue());
//        fourth.add(Box.createHorizontalStrut(9));
        fourth.add(TimeLabel);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(TimeTF);
        fourth.add(Box.createHorizontalGlue());

        JPB.setPreferredSize(TimeTF.getPreferredSize());
        JPB.setStringPainted(true);
        JPB.setFont(FONT);

        fifth.add(Box.createHorizontalGlue());
        fifth.add(WindowSizeLabel);
        fifth.add(Box.createHorizontalGlue());
        fifth.add(WindowSizeTF);
        fifth.add(Box.createHorizontalGlue());
        fifth.add(IterLabel);
        fifth.add(Box.createHorizontalGlue());
        fifth.add(JPB);
        fifth.add(Box.createHorizontalGlue());

        sixth.add(Box.createHorizontalGlue());
        sixth.add(GoMakeSomeMagic);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(LoadDM);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(GetMetrics);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(ShowLogs);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(Save);
        sixth.add(Box.createHorizontalGlue());//чуть позже вернемся к графическому интерфейсу, для начала нужно раздобыть немного информации

        sad.setSelected(true);
        AutoSaveCB.setSelected(true);
        AutoScaleCB.setSelected(true);
        GoMakeSomeMagic.setEnabled(false);
        LoadDM.setEnabled(true);
        GetMetrics.setEnabled(false);
        ShowLogs.setEnabled(false);
        Save.setEnabled(false);

        ApplyOperation.setEnabled(false);
        UndoOperation.setEnabled(false);


      /*  width = image1.getWidth() >= image2.getWidth() ? image2.getWidth() : image1.getWidth();
        height = image1.getHeight() >= image2.getHeight() ? image2.getHeight() : image1.getHeight();*/
        // System.out.println("Разрешение фоток " + width + " на " + height);

        firstvert.add(Box.createVerticalGlue());
        firstvert.add(first);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(second);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(third);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(fourth);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(fifth);
        firstvert.add(Box.createVerticalGlue());

        parambox.add(Box.createHorizontalGlue());
        parambox.add(methdsb);
        parambox.add(Box.createHorizontalGlue());
        parambox.add(firstvert);
        parambox.add(Box.createHorizontalGlue());


        finalvert.add(Box.createVerticalGlue());
        finalvert.add(zero);
        finalvert.add(Box.createVerticalGlue());
        finalvert.add(parambox);
        finalvert.add(Box.createVerticalGlue());
        finalvert.add(sixth);
        finalvert.add(Box.createVerticalGlue());

        deepmap.add(Box.createHorizontalGlue());
        deepmap.add(BottomImageLabel);
        deepmap.add(Box.createHorizontalGlue());
        //deepmap.add(GradientOfColors);
        deepmap.add(Box.createHorizontalGlue());
        frame.getContentPane();
        panel.setLayout(new GridLayout(2, 2, 2, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        //image1 = ImageIO.read(LI);
        LeftImageLabel.setIcon(null /*new ImageIcon(ichange.SizeDecreaser(image1,2))*/);
        panel.add(LeftImageLabel);
        //image2 = ImageIO.read(RI);
        RightImageLabel.setIcon(null /*new ImageIcon(ichange.SizeDecreaser(image2,2))*/);
        panel.add(RightImageLabel);
        /* TopImageLabel.setIcon(null*//*new ImageIcon(ichange.SizeDecreaser(image1,2))*//*);
        panel.add(TopImageLabel);*/
        panel.add(finalvert);
        BottomImageLabel.setIcon(null);
        panel.add(deepmap);

        frame.add(panel);
//        frame.setFont(new Font("TimesRoman", Font.BOLD, 22));
        frame.setVisible(true);

        // link actions to corresponding buttons
        changeFont(frame, FONT);
//        frame.pack();


        ConfigureAllActions();
        ConfigureKeyBindings();
        ConfigureAllTips();


        //получение из матрицы смещений матрицу для карты глубины
//    double f=0.025; //фокусное расстояние в метрах
//  double base=0.3; //база съёмки - расстояние между фотиками
//  double realPixelSize=0.000006041; //как-то посчитанный реальный размер пикселя для снимка 1024*768
//  //double matrL[5][5];
//  for(int row=0; row<width; row++)
//      for(int col=0; col<height; col++)
//          if(matrix3[row][col]>0)
//              matrix3[row][col]=((f*base)/(realPixelSize*matrix3[row][col]));
//          else matrix3[row][col]=0;

        // !!! }

    }
    public void SetCompareMethod(){
        method = new SAD();
        if (ssd.isSelected())
            method = new SSD();
        else if (ncc.isSelected())
            method = new NCC();
        else if (scc.isSelected())
            method = new SCC();
        else if (kcc.isSelected())
            method = new KCC();
    }
    public JFileChooser getImageLoader(){
        return loadimage;
    }
    // to make them stationary
    public void SecureAllParameters(){
        adaptive_mode = AdaptiveSizeCB.isSelected();
        autosave_mode = AutoSaveCB.isSelected();
        approximate_mode = ApprxAlgsCB.isSelected();
        localized_mode = AdaptiveSizeCB.isSelected();

        window_size = parseInt(WindowSizeTF, WS);
        n_segments = parseInt(NSegmentsTF, NSEG);
        stride = parseInt(StrideTF, STRIDE);
        ext_coef = parseDouble(ECoefTF, EC);
        vdev = parseInt(VdevTF, VDEV);

        SetCompareMethod();
        method.setStride(stride);

//        gen_params.put("AdaptiveMode", AdaptiveSizeCB.isSelected());
//        gen_params.put("AutoSaveMode", AutoSaveCB.isSelected());
//        gen_params.put("ApproximateMode", ApprxAlgsCB.isSelected());
//        gen_params.put("LocalizedMode", AdaptiveSizeCB.isSelected());
    }

    //    public int GetWindowSize(){
//        try {
//            return Integer.valueOf(WindowSizeTF.getText());
//        }catch(Exception e){
//            WindowSizeTF.setText("5");
//            JOptionPane.showMessageDialog(MainFrame.this, "The value in the scan_screen_size field" +
//                    "must be positive. Setting to 5");
//            return Integer.valueOf(WindowSizeTF.getText());
//        }
//    }

    public void ResetProgress(){
        progress = 0;
    }
    public void UpdateProgress(){
        JPB.setValue((int)(100*progress));
    }

    class DMGenerator extends SwingWorker<Void, Integer> {


        public DMGenerator() {
            GoMakeSomeMagic.setEnabled(false);
            if (image1.getHeight() != image2.getHeight() || image1.getWidth() != image2.getWidth()){
                image2 = improc.SizeChangerS(image2, image1.getWidth(), image1.getHeight(), interpol_choice);
            }

            ClearWindows();
            SecureAllParameters();
            ResetProgress();
        }

        @Override
        protected void process(List<Integer> chunks) {
            UpdateProgress(); // The last value in this array is all we care about.
        }

        @Override
        protected Void doInBackground() throws Exception {
            start = (int) System.currentTimeMillis();
            GenerateDepthMap();
            finish = (int)System.currentTimeMillis();
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                long timeElapsed = finish - start;
                TimeTF.setText(Long.toString(timeElapsed));
                IterTF.setText(Long.toString(itercounter));

                // Сохранение
                if (AutoSaveCB.isSelected())
                    SaveResults();
                progress = 1;
                UpdateProgress();
                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(DepthMap_full, guiImageWidth, guiImageHeight, interpol_choice)));
                current_state = new GenState(DepthMap, logs, correlation_m, window_size, vdev);
                LogsStack.push(current_state);
                //BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)window_size*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
//            BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerDistanceBased(improc.SizeChanger(DepthMap, Math.round(((double)window_size*guiImageWidth/ iwidth))), guiImageWidth, guiImageHeight)));

                //GradientOfColors.setIcon(new ImageIcon(improc.SizeChangerLinear(gradientstripe, guiImageWidth, guiImageHeight)));
                if (LogsStack.size() > 1) {
                    UndoOperation.setEnabled(true);
                }
                ApplyOperation.setEnabled(true);
                if (buff != null)
                    GetMetrics.setEnabled(true);
                ShowLogs.setEnabled(true);
                Save.setEnabled(true);
//                frame.pack();
                //panel.add(BottomImageLabel, SOUTH);
                //panel.add(GradientOfColors, SOUTH);
                //frame.add(panel);
                GoMakeSomeMagic.setEnabled(true);
                frame.setVisible(true);
                frame.setEnabled(true);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void GenerateDepthMap(){
            matrix1 = new byte[iwidth][iheight][3]; //матрица для первого снимка
            matrix2 = new byte[iwidth][iheight][3]; //матрица для второго снимка
            //преобразование изображения в чб, конфликтует с некоторыми цветами
//        if (bw.isSelected()) {
//            improc.loadFull(image1);
//            image1 = improc.ImageCopy(improc.BW());
//            improc.loadFull(image2);
//            image2 = improc.ImageCopy(improc.BW());
//            LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(image1, guiImageWidth, guiImageHeight, interpol_choice)));
//            RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(image2, guiImageWidth, guiImageHeight, interpol_choice)));
//        }

            for (int i = 0; i < iwidth; i++) {
                for (int j = 0; j < iheight; j++) {
                    // matrix1[i][j] = Math.abs(image1.getRGB(i, j));
                    matrix1[i][j][0] = (byte)(Math.abs(new Color(image1.getRGB(i, j)).getRed()) - 128);
                    matrix1[i][j][1] = (byte)(Math.abs(new Color(image1.getRGB(i, j)).getGreen()) - 128);
                    matrix1[i][j][2] = (byte)(Math.abs(new Color(image1.getRGB(i, j)).getBlue()) - 128);
                    //System.out.print(matrix1[i][j] + " ");
                    // matrix2[i][j] = Math.abs(image2.getRGB(i, j));
                    matrix2[i][j][0] = (byte)(Math.abs(new Color(image2.getRGB(i, j)).getRed()) - 128);
                    matrix2[i][j][1] = (byte)(Math.abs(new Color(image2.getRGB(i, j)).getGreen()) - 128);
                    matrix2[i][j][2] = (byte)(Math.abs(new Color(image2.getRGB(i, j)).getBlue()) - 128);
                }
            }
            //Пожалуй, самая трудоемкая функция в данной программе, сложность - порядка O(n^3), но т.к. число n - далеко не маленькое, зачастую приходится подождать
            DepthMap_full = CalculateDepthMap();
        }

        public BufferedImage CalculateDepthMap() {
            int width = matrix1.length; // 500
            int height = matrix1[0].length; // 400

            int locale_w = width / n_segments;
            int locale_h = height / n_segments;
            double[][] thresh_matrix = new double[n_segments][n_segments];
            double[][] deviations = null;
            double[][] correlations = null;

//        double EC = Double.parseDouble(ECoefTF.getText());
            int opt_deviation, corrected_width;

            if (!adaptive_mode) {
                double[] devInfo = getDeviation(matrix1, matrix2, approximate_mode);
                double light_coef = ext_coef / devInfo[1];
                opt_deviation = (int) (devInfo[0]);
                max_deviation = (int) (opt_deviation * light_coef);

            } else {
                double[][][] devsInfo = getDeviations(matrix1, matrix2, n_segments);
                deviations = devsInfo[0];
                correlations = devsInfo[1];

                double max_dev = 0;
                double md_cor = 1;
                for (int i = 0; i < deviations.length; i++){
                    for (int j = 0; j < deviations[0].length; j++){
                        if (Math.abs(max_dev) < Math.abs(deviations[i][j])){
                            max_dev = deviations[i][j];
                            md_cor = correlations[i][j];
                        }
                        deviations[i][j] *= (ext_coef*(0.85)/correlations[i][j]);
                    }
                }
                opt_deviation = (int) (max_dev);
                double light_coef = ext_coef *(0.85)/ md_cor;
                max_deviation = (int) (opt_deviation * light_coef);
            }


            corrected_width = (width - Math.abs(opt_deviation));


            System.out.println("\nOPTIMAL DEVIATION: " + opt_deviation + " pixels");
            System.out.println("\nMAX DEVIATION: " + max_deviation + " pixels");
            //max_deviation = -100;
            System.out.println("\nMatrix size: " + (int) Math.ceil((double) corrected_width / window_size) + ' ' + (int) Math.ceil((double) height / window_size) + " pixels");
            matrix3 = new double[(int) Math.ceil((double) corrected_width / window_size)][(int) Math.ceil((double) height / window_size)]; //матрица смещений
            double[][] m3_upd = new double[corrected_width][height];
            logs = new int[(int) Math.ceil((double) corrected_width / window_size)][(int) Math.ceil((double) height / window_size)][];
            correlation_m = new double[(int) Math.ceil((double) corrected_width / window_size)][(int) Math.ceil((double) height / window_size)][];

            double best_correlation;
            int coincidentx;
            int coincidenty;

            int tempsizeadd;
            itercounter = 0;
            progress = 0;

            int sc_height, sc_width;
            double std1, std2 = 0;
            byte[][][] tempmatrix1, tempmatrix2;
            int comp_counter;
            double std_thresh; // Std(matrix1)/6
            //double AC = Double.parseDouble(ACoefTF.getText());
            double AC = 2.15;



            int w = (int) (2.6*Math.sqrt(Math.abs(max_deviation)));
            System.out.println("W: " + w);
            double c_thresh = 0.85;

            if (adaptive_mode) {
                for (int i = 0; i < n_segments; i++) {
                    for (int j = 0; j < n_segments; j++) {
                        double temp = Std(getPart(matrix1, locale_w * i, locale_h * j, Math.min(width - locale_w * i, locale_w), Math.min(height - locale_h * j, locale_h), 0));
                        thresh_matrix[i][j] = AC * Math.pow(temp, 0.5);
                    }
                }
                for (int i = 0; i < n_segments; i++) {
                    for (int j = 0; j < n_segments; j++) {
                        System.out.print((int) thresh_matrix[j][i] + " ");
                    }
                    System.out.println();
                }

                System.out.println("Adaptive Areas: " + locale_w + " " + locale_h);
            }


            double[][] devs_upd = new double[corrected_width][height];

            //double [][] thresh_matrix = new double[(int)Math.ceil((double)corrected_width / window_size)][(int)Math.ceil((double)height / window_size)];
            double[][] tm_upd = new double[corrected_width][height];

            int iterations_total = ((int) Math.ceil((double) corrected_width / window_size) * (int) Math.ceil((double) height / window_size));
            //System.out.println("START STD THRESH: " + std_thresh);
            for (int col_image1 = -Math.min(opt_deviation, 0); col_image1 < width - Math.max(opt_deviation, 0) - 1; col_image1 += window_size) {
                sc_width = window_size - Math.max((col_image1 + window_size) - (width - Math.max(opt_deviation, 0)) + 1, 0);
                //System.out.println("###1#### " + col_image1 + ' '+ width +' '+sc_width);
                for (int row_image1 = 0; row_image1 < height; row_image1 += window_size) {

                    sc_height = window_size - Math.max((row_image1 + window_size) - height + 1, 0);
                    //System.out.println("###2#### " + row_image1 + ' '+ height +' '+sc_height);
                    tempsizeadd = 0;
                    //System.out.println("!!!!!!!!!!!!!!!!!!!" + col_image1 + " "+ row_image1 + " "+width+"");
                    best_correlation  = 0;
                    tempmatrix1 = getPart(matrix1, col_image1, row_image1, sc_width, sc_height, tempsizeadd);
                    std1 = Std(tempmatrix1);
//                if ((col_image1 % locale_w < sc_width) && (row_image1 % locale_h < sc_height)) {
//                    System.out.println("IS THE START OF AREA: " + col_image1 + " " + row_image1);
//                    std_thresh = improc.Std(getPart(matrix1, col_image1, row_image1, locale_w - sc_width, locale_h - sc_height, 0)) / 6;
//                    System.out.println("NEW THRESH: " + std_thresh);
//                }
                    //System.out.println(col_image1 + " " + row_image1 + " " +  col_image1/locale_w + " " + row_image1/locale_h);
                    std_thresh = thresh_matrix[(Math.min(col_image1/locale_w, n_segments-1))][Math.min((row_image1/locale_h), n_segments-1)];

                    if (adaptive_mode) { // local
                        max_deviation = (int) (deviations[(Math.min(col_image1 / locale_w, n_segments - 1))][Math.min((row_image1 / locale_h), n_segments - 1)]);
//                    System.out.println("Max deviation: " + max_deviation);
                    }
                    //std_thresh = (std_thresh + std1/10)/1.1;
                    //System.out.println("NEW STD THRESH: " + std_thresh);


                    if(adaptive_mode && std1 < std_thresh && std1 > 0) {
                        tempsizeadd = 0;
                        int[] eP = extendPart(col_image1, row_image1, sc_width, sc_height, tempsizeadd);
                        byte[][][] asmatrix;
                        //System.out.println("STD " + improc.Std(tempmatrix1));
                        // && tempsizeadd < 2*sc_width
                        double start_std = std1;
                        if(eP[0] >= -max_deviation && (width - eP[2] - eP[0]) > 0 && eP[1] >= 0 && (height - eP[3] - eP[1]) > 0) {
                            while (std1 <= Math.min(std_thresh, AC*start_std) && eP[0] >= -max_deviation && (width - eP[2] - eP[0]) > 0 && eP[1] >= 0 && (height - eP[3] - eP[2]) > 0) {
                                asmatrix = getPart(matrix1, col_image1, row_image1, sc_width, sc_height, tempsizeadd);
                                //System.out.println("&&&&&&&&&&&&&&&&& " + tempsizeadd + " " + col_image1 + " " + row_image1);
                                std1 = Std(asmatrix);
                                if (std1 < Math.min(std_thresh, AC*start_std))
                                    tempmatrix1 = asmatrix;
                                tempsizeadd += 1;
                                eP = extendPart(col_image1, row_image1, sc_width, sc_height, tempsizeadd);
                            }
                            tempsizeadd -= 1;
                        }
                    }
                    std1 = Std(tempmatrix1);
                    coincidentx = col_image1;
                    coincidenty = row_image1;
                    //System.out.println("Top: " + Math.max(tempsizeadd,row_image1-vdev) + " Bottom:" + (Math.min(height- sc_height - tempsizeadd + 1,row_image1 + vdev + 1)));
                    //System.out.println("Left: " + tempsizeadd + " Right:" + (width - tempsizeadd - sc_width));
                    //System.out.println("TEMPSIZEADD: " + tempsizeadd);



                    int peak_b = 0;
                    int peak_f = 0;

                    //System.out.println(Math.min(Math.abs(max_deviation), width - sc_width - col_image1 - tempsizeadd));
                    //for (int deviation = 0; (col_image1 - deviation) >= tempsizeadd && deviation <= Math.abs(max_deviation); deviation++){

                    comp_counter = 0;
                    correlation_m[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ window_size)][(int)Math.ceil((double)row_image1 / window_size)] = new double[Math.min(col_image1 - tempsizeadd, Math.abs(max_deviation))+1];
                    for (int deviation = 0;  deviation <= Math.min(col_image1 - tempsizeadd, Math.abs(max_deviation)); deviation++){
                        //for (int col_image2 = tempsizeadd; col_image2 < width - sc_width; col_image2++) {
                        for(int row_image2 = Math.max(0,row_image1-vdev); row_image2 < Math.min(height-sc_height+1,row_image1 + vdev + 1); row_image2++) {

                            int col_image2 = (opt_deviation <= 0)? (col_image1-deviation):(col_image1+deviation);
                            tempmatrix2 = getPart(matrix2, col_image2, row_image2, sc_width, sc_height, tempsizeadd);

                            double correlation = Compare(method, tempmatrix1, tempmatrix2, ConvApprxCB.isSelected());
                            correlation_m[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ window_size)][(int)Math.ceil((double)row_image1 / window_size)][deviation] = correlation;
                            comp_counter++;

                            if (correlation > best_correlation) {
                                best_correlation = correlation;
                                coincidentx = col_image2;
                                coincidenty = row_image2;
                                std2 = Std(tempmatrix2);
                                //System.out.print("New Best: " + method.bestTotal + " ");
                                if (peak_b >= 1)
                                    peak_f = 0;
                                peak_f++;
                                peak_b = 0;
                            }
                            else{
                                if (peak_f >= w)
                                    peak_b++;
                            }

                            if (approximate_mode && peak_b >= w && best_correlation > c_thresh)
                                break;
                        }
                        if (approximate_mode && peak_b >= w && best_correlation > c_thresh){
                            break;
                        }
                    }
                    //hdprob += Math.abs(coincidenty - row_image1);
                    double disparity = Math.hypot(coincidentx - col_image1, coincidenty - row_image1);
                    //System.out.println("DIST: " + distance);
                    int[] eP1 = extendPart(col_image1, row_image1, sc_width, sc_height, tempsizeadd);
                    int[] eP2 = extendPart(coincidentx, coincidenty, sc_width, sc_height, tempsizeadd);
                    //System.out.println(tempsizeadd);
                    //col_image1, row_image1, coincidentx, coincidenty, sc_width, sc_height, metrics, (int)distance, std1, std2
                    int id1 = (int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ window_size);
                    int id2 = (int)Math.ceil((double)row_image1 / window_size);
                    logs[id1][id2] = new int[]{eP1[0], eP1[1], eP2[0], eP2[1], eP1[2], eP1[3],
                            (int)(dtis*best_correlation),  (int)((dtis*disparity)/Math.hypot(max_deviation, 2*vdev)),
                            (int)(dtis*std1), (int)(dtis*std2), (dtis*tempsizeadd/width), comp_counter, max_deviation};
                    //System.out.println("&&&&& " + col_image1 + ' ' + sc_width);
                    matrix3[id1][id2] = disparity;

                    for (int i = 0; i < sc_width; i++) {
                        for (int j = 0; j < sc_height; j++) {
                            m3_upd[col_image1 + Math.min(opt_deviation, 0) + i][row_image1 + j] = disparity;
                            tm_upd[col_image1 + Math.min(opt_deviation, 0) + i][row_image1 + j] = std_thresh;
                            devs_upd[col_image1 + Math.min(opt_deviation, 0) + i][row_image1 + j] = max_deviation;
                        }
                    }
                    itercounter++;
                    progress = 0.5+(double)itercounter/(iterations_total*2);
                    publish();
                }
            }

            itercounter = (int)progress;



            gradientstripe = new BufferedImage(20, height, BufferedImage.TYPE_INT_RGB);
            Color mycolor;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < 20; j++) {
                    mycolor = new Color(255 * i / height, 255 * i / height, 255 * i / height);
                    gradientstripe.setRGB(j, height - i - 1, mycolor.getRGB());
                }
            }

            // Low value (near 0) - distant object, high value (up to 255) - close one
            DepthMap = MatrixToImage(matrix3);
            BufferedImage DepthMap_full = MatrixToImage(m3_upd);
            THImage = MatrixToImage(tm_upd);
            DevsImage = MatrixToImage(devs_upd);
            ShiftedImage = getShiftedImage(matrix1, Math.abs(opt_deviation));
            return DepthMap_full;
        }

        public double[] getDeviation(byte[][][] matrix1, byte[][][] matrix2, boolean use_approx) {
            int width = matrix1.length;
            int height = matrix1[0].length;
            double best_correlation = 0;
            int opt_deviation = width/5;
            double area = width/4;
            int stripe = Math.max((int)area/75, 1);
            byte[][][] temp_matrix1, temp_matrix2, best_matrix1 = null, best_matrix2 = null;

            //int n_rnd = width/5;
            //int size = width/20;
            int n_rnd = width / 18;
            int size = width / 16;

            int ls, rs;
            ls = (int) (-area / 2);
            rs = -5; // (int) (area / 2)

            double[][] plot_data = new double[(rs-ls)/stripe+1][2];

            CompareMethod ncccm = new NCC();

            double[][] c_r = new double[n_rnd][];
            if (use_approx) {
                Random rand = new Random();
                for (int i = 0; i < n_rnd; i++) {
                    c_r[i] = new double[]{rand.nextDouble(), rand.nextDouble()};
                    //new double[]{rand.nextInt(width - Math.abs(deviation) - size + 1), rand.nextInt(height - size + 1)}
                }
            }

            int iterations_total = (int)((double)(rs-ls)/stripe);

            for (int deviation = ls; deviation < rs; deviation += stripe) {
                temp_matrix1 = new byte[width - Math.abs(deviation)][height][3];
                temp_matrix2 = new byte[width - Math.abs(deviation)][height][3];
                for (int i = 0; i < width - Math.abs(deviation); i++) {
                    for (int j = 0; j < height; j++) {
                        for (int k = 0; k < 3; k++) {
                            if (deviation < 0) {
                                temp_matrix1[i][j][k] = matrix1[i - deviation][j][k];
                                temp_matrix2[i][j][k] = matrix2[i][j][k];
                            } else {
                                temp_matrix1[i][j][k] = matrix1[i][j][k];
                                temp_matrix2[i][j][k] = matrix2[i + deviation][j][k];
                            }
                        }
                    }
                }
                double correlation = 0;
                double counter = 0;
                if (use_approx){
                    for (int i = 0; i < n_rnd; i++){
                        byte[][][] rbatch1 = new byte[size][size][3];
                        byte[][][] rbatch2 = new byte[size][size][3];
                        int x_r = (int)(c_r[i][0] * (width - Math.abs(deviation) - size + 1));
                        int y_r = (int)(c_r[i][1] * (height - size + 1));
                        for (int n = 0; n < size; n++){
                            for (int m = 0; m < size; m++){
                                for (int k = 0; k < 3; k++){
                                    rbatch1[n][m][k] = temp_matrix1[x_r + n][y_r + m][k];
                                    rbatch2[n][m][k] = temp_matrix2[x_r + n][y_r + m][k];
                                }
                            }
                        }
                        double temp = ncccm.get_similarity(rbatch1, rbatch2);
                        if(!Double.isNaN(temp)) {
                            correlation += temp;
                            counter++;
                        }
                    }
                    correlation /= counter;
                }
                else {
                    correlation = ncccm.get_similarity(temp_matrix1, temp_matrix2);
                }
                if (correlation > best_correlation) {
                    best_matrix1 = MCopy(temp_matrix1);
                    best_matrix2 = MCopy(temp_matrix2);
                    best_correlation = correlation;
                    opt_deviation = deviation;
                }
                plot_data[(deviation - ls)/stripe] = new double[]{deviation, 100*correlation};
                itercounter++;
                progress = (double)itercounter/(iterations_total*2);
                publish();
//            System.out.println(" " + correlation +" "+ deviation);
            }
//        if (verbose){
//            pf = new PlotFrame(MainFrame.this, MatrixToImage(best_matrix1), MatrixToImage(best_matrix2),
//                               opt_deviation, best_correlation, plot_data);
//        }


            // writing to file
//        try{
//            counter4saving = 0;
//            File outputfile;
//            do {
//                counter4saving++;
//                outputfile = new File("Maps\\DepthMap" + counter4saving + ".png");
//            } while (outputfile.exists());А
//            BufferedWriter writer = new BufferedWriter(new FileWriter("Correlations\\Correlation_data" + counter4saving + ".txt"));
//            for (int i = 0; i < plot_data.length; i++) {
//                writer.append(plot_data[i][0] +","+plot_data[i][1] + "\n");
//            }
//            writer.close();
//        }catch (Exception e){
//
//        }
            return new double[]{opt_deviation, best_correlation};
        }

        public double[][][] getDeviations(byte[][][] matrix1, byte[][][] matrix2, int n_segments) {
            int width = matrix1.length;
            int height = matrix1[0].length;
            double best_correlation = 0;
            int opt_deviation = width/5;
            double area = width/4;
            int stripe = Math.max((int)area/75, 1);
            byte[][][] temp_matrix1, temp_matrix2;

            int ls, rs;
            ls = (int) (-area / 2);
            rs = -5; // (int) (area / 2)

            CompareMethod ncccm = new NCC();
            double[][] d_matrix = new double[n_segments][n_segments];
            double[][] c_matrix = new double[n_segments][n_segments];
            for (int i = 0; i < n_segments; i++) {
                for (int j = 0; j < n_segments; j++) {
                    c_matrix[i][j] = 0;
                }
            }
            double correlation = 0;
            int seg_w, seg_h;
            int iterations_total = (int)((double)(rs-ls)/stripe);
            itercounter = 0;
            for (int deviation = ls; deviation < rs; deviation += stripe) {
                seg_w = (width-Math.abs(deviation))/n_segments;
                seg_h = height/n_segments;
                for (int i = 0; i < n_segments; i++) {
                    for (int j = 0; j < n_segments; j++) {
                        int temph = Math.min(height - seg_h * j, seg_h);
                        int tempw = Math.min(width - Math.abs(deviation) - seg_w * i, seg_w);
                        //System.out.println(i + " "+ j +" " + deviation + " "+ tempw + " "+ temph);
                        if (deviation < 0) {
                            temp_matrix1 = getPart(matrix1, seg_w * i - deviation, seg_h * j, tempw, temph, 0);
                            temp_matrix2 = getPart(matrix2, seg_w * i, seg_h * j, tempw, temph, 0);
                        } else {
                            temp_matrix1 = getPart(matrix1, seg_w * i, seg_h * j, tempw, temph, 0);
                            temp_matrix2 = getPart(matrix2, seg_w * i + deviation, seg_h * j, tempw, temph, 0);
                        }
                        correlation = ncccm.get_similarity(temp_matrix1, temp_matrix2);
                        if (correlation > c_matrix[i][j]) {
                            c_matrix[i][j] = correlation;
                            d_matrix[i][j] = deviation;
                        }
                    }
                }
                itercounter++;
                progress = (double)itercounter/(iterations_total*2);
                publish();
                System.out.println(deviation + " " + correlation);
            }

//        System.out.println("DEVIATIONS BEFORE");
//        for (int i = 0; i < n_segments; i++) {
//            for (int j = 0; j < n_segments; j++) {
//                System.out.print((int)Math.abs(d_matrix[i][j]) + " ");
//            }
//            System.out.println();
//        }
//        System.out.println();
            d_matrix = Median(d_matrix, 3);
            c_matrix = Median(c_matrix, 3);
//        for (int i = 0; i < n_segments; i++) {
//            for (int j = 0; j < n_segments; j++) {
////                d_matrix[i][j] /= Math.sqrt(c_matrix[i][j]);
//                d_matrix[i][j] /= c_matrix[i][j];
//            }
//            System.out.println();
//        }
            System.out.println("DEVIATIONS");
            for (int i = 0; i < n_segments; i++) {
                for (int j = 0; j < n_segments; j++) {
                    System.out.print((int)Math.abs(d_matrix[i][j]) + " ");
                }
                System.out.println();
            }
            System.out.println("CORRELATIONS");
            for (int i = 0; i < n_segments; i++) {
                for (int j = 0; j < n_segments; j++) {
                    System.out.print(c_matrix[i][j] + " ");
                }
                System.out.println();
            }
            return new double[][][]{d_matrix,c_matrix};
        }



    }


    public void ClearWindows(){
//        setEnabled(false);
        if(pf != null) {
            pf.dispose();
            pf = null;
        }
        if(lv != null) {
            lv.dispose();
            lv = null;
        }
        if(dmc != null) {
            dmc.dispose();
            dmc = null;
        }
        System.out.flush();
    }

    public void CreateDirectories() throws IOException {
        Files.createDirectories(Paths.get(DataPath));
        Files.createDirectories(Paths.get(MapsPath));
        Files.createDirectories(Paths.get(DeviationsPath));
        Files.createDirectories(Paths.get(ShiftedIPath));
        Files.createDirectories(Paths.get(ThresholdsPath));
    }
    public void SaveResults(){
        try{
            counter4saving = 0;
            File outputfile;
            do {
                counter4saving++;
                outputfile = new File(MapsPath+"DepthMap"+counter4saving+".png");
            } while (outputfile.exists());
        }catch (Exception e){}

        try {
            if (DepthMap == null) {
                throw new IOException();
            }
            File outputfile;
            outputfile = new File(MapsPath+"DepthMap"+counter4saving+".png");
            ImageIO.write(DepthMap_full, "png", outputfile);
            //JOptionPane.showMessageDialog(MainFrame.this, "Saved");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong, try again");
        }

        try {
            if (THImage == null) {
                throw new IOException();
            }
            File outputfile;
            outputfile = new File(ThresholdsPath+"Thresholds"+counter4saving+".png");
            ImageIO.write(THImage, "png", outputfile);
            //JOptionPane.showMessageDialog(MainFrame.this, "Saved");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong, try again");
        }

        try {
            if (ShiftedImage == null) {
                throw new IOException();
            }
            File outputfile;
            outputfile = new File(ShiftedIPath+"Shifted_Image"+counter4saving+".png");
            ImageIO.write(ShiftedImage, "png", outputfile);
            //JOptionPane.showMessageDialog(MainFrame.this, "Saved");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong, try again");
        }

        try {
            if (DevsImage == null) {
                throw new IOException();
            }
            File outputfile;
            outputfile = new File(DeviationsPath+"Deviations"+counter4saving+".png");
            ImageIO.write(DevsImage, "png", outputfile);
            //JOptionPane.showMessageDialog(MainFrame.this, "Saved");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong, try again");
        }
    }
    public int[][] GenerateGKernel(double dispersion, int size) {
        int[][] mask = new int[2 * size + 1][2 * size + 1];
        double min = ((double)1/((2*Math.PI*dispersion)*Math.exp(Math.hypot((-size),(-size))/(2*dispersion))));
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                mask[i][j] = (int)(((double)1/((2*Math.PI*dispersion)*Math.exp(Math.hypot((i-size),(j-size))/(2*dispersion))))/min);
            }
        }
        return mask;
    }
    // NOT WORKING, CHANGE TYPE OF RESULT FROM BYTE TO INT
    public byte[][][] Convolve(byte[][][] matrix, int[][] kernel){
        int width = matrix.length;
        int height = matrix[0].length;
        int size = (kernel.length-1)/2;
        byte[][][] result = new byte[width-2*size][height-2*size][3];
        for (int i = size; i < width - size; i++) {
            for (int j = size; j < height - size; j++) {
                for (int k = 0; k < 3; k++) {
                    for (int m = -size; m <= size; m++) {
                        for (int n = -size; n <= size; n++) {
                            result[i-size][j-size][k] += (matrix[i + m][j + n][k] * kernel[m + size][n + size]);
                        }
                    }
                }
            }
        }
        return result;
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
    public int[] extendPart(int col, int row, int width, int height, int tempsizeadd){
        return new int[]{col - tempsizeadd, row, width + tempsizeadd, height};
    }
    public byte[][][] getPart(byte[][][] matrix, int col, int row, int width, int height, int tempsizeadd){
        if (tempsizeadd != 0) {
            int[] eP = extendPart(col, row, width, height, tempsizeadd);
            col = eP[0];
            row = eP[1];
            width = eP[2];
            height = eP[3];
        }
        byte[][][] part = new byte[width][height][3];
        // 500 rows and 400 columns

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    //System.out.println("Height: " + matrix.length  + ' ' + (col+i) + " Width: " + matrix[0].length + ' '+ (row+j));
                    part[i][j][k] = (byte) matrix[col + i][row + j][k];
                }
            }
        }
        return part;
    }
    //    public int[][][] getPartU(int[][][] matrix, int col, int row, int width, int height, int tempsizeadd){
//        return getPart(matrix,col - tempsizeadd, row - tempsizeadd,width + 2*tempsizeadd, height + 2*tempsizeadd);
//    }
    public double Compare(CompareMethod method, byte[][][] part1, byte[][][] part2, boolean capprx) {
        if (capprx) {
            //int size = Math.min(tempmatrix1.length, tempmatrix1[0].length) / 20;
            int size = 1;
            int[][] kernel = GenerateGKernel(1, size);
            return method.get_similarity(Convolve(part1, kernel), Convolve(part2, kernel));
        }
        else
            return method.get_similarity(part1, part2);

    }

    public byte[][][] MCopy(byte[][][] matrix){
        byte[][][] temp = new byte[matrix.length][matrix[0].length][matrix[0][0].length];
        for (int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[0].length; j++){
                for(int k = 0; k < matrix[0][0].length; k++)
                    temp[i][j][k] = matrix[i][j][k];
            }
        }
        return temp;
    }

    public BufferedImage MatrixToImage(byte[][][] matrix){
        int width = matrix.length;
        int height = matrix[0].length;
        BufferedImage tempimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
            {
                Color MyColor = new Color(matrix[i][j][0] + 128, matrix[i][j][1] + 128, matrix[i][j][2] + 128);
                tempimg.setRGB(i, j, MyColor.getRGB());
            }
        }
        return tempimg;
    }

    public int getInterpChoice(){
        return this.interpol_choice;
    }
    public BufferedImage getShiftedImage(byte[][][] matrix, int shift){
        int width = matrix.length;
        int height = matrix[0].length;
        int corrected_width = width - shift;
        byte[][][] cutted_matrix = new byte[corrected_width][height][3];
        for (int i = 0; i < corrected_width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    //System.out.println(j + " " + (i + deviation) + " "+ matrix1.length + " " + matrix1[0].length);
                    cutted_matrix[i][j][k] = matrix[i+shift][j][k];
                }
            }
        }
        return MatrixToImage(cutted_matrix);
    }


    public double[][] Median(double[][] mat, int size) {
        size = Math.min(mat.length-1, size);
        int width = mat.length + 2*size;
        int height = mat[0].length + 2*size;

        double[][] emat = new double[width][height];

        for (int i = 0; i < width - 2*size; i++) {
            for (int j = 0; j < height - 2*size; j++) {
                emat[i+size][j+size] = mat[i][j];
            }
        }


        for (int i = size; i < width-size; i++) {
            for (int j = 0; j < size; j++) {
                emat[i][j] =  emat[i][size + j];
                emat[i][height - j - 1]  = emat[i][height - size - 1 - j];
            }
        }


        for (int i = 0; i < size; i++) {
            for (int j = size; j < height-size; j++) {
                emat[i][j] =  emat[size + i][j];
                emat[width - i - 1][j] =  emat[width - size - 1 - i][j];
            }
        }


        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {
                //filling left-top area
                emat[size - i][size - j] = (emat[size - i + 1][size - j] + emat[size - i][size - j + 1]) / 2;
                //filling right-top area
                emat[width - size + i - 1][size - j] = (emat[width - size + i - 2][size - j] + emat[width - size + i - 1][size - j + 1]) / 2;
                //filling left-bot area
                emat[size - i][height - size + j - 1] = (emat[size - i + 1][height - size + j - 1] + emat[size - i][height - size + j - 2]) / 2;
                //filling right-bot area
                emat[width - size + i - 1][height - size + j - 1] = (emat[width - size + i - 2][height - size + j - 1] + emat[width - size + i - 1][height - size + j - 2]) / 2;

            }
        }

        System.out.println("MEDIAN MATRIX");
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                System.out.print(emat[i][j] + " ");
            }
            System.out.println();
        }

        double[] sorted = new double[(int) Math.pow(2 * size + 1, 2)];
        double[][] fmat = new double[width - 2*size][height - 2*size];
        for (int i = size; i < width-size; i++) {
            for (int j = size; j < height-size; j++) {
                for (int m = -size; m <= size; m++) {
                    for (int n = -size; n <= size; n++) {
                        sorted[n + size + (m + size) * (2 * size + 1)] = emat[i + m][j + n];
                    }
                }
                Arrays.sort(sorted);
                fmat[i-size][j-size] = sorted[sorted.length / 2];
            }
        }
        return fmat;
    }


    //    public double[] getMapPSNR(int[][][] matrix1, int[][][] matrix2, boolean use_approx){
//        // matrix2 is our map and is smaller
//        int width = matrix2[0].length;
//
//        int height = matrix2.length;
//        double best_metrics = 1000;
//        int opt_deviation = width/5;
//        int[][][] temp_matrix1, temp_matrix2;
//
//        int n_rnd = width/15;
//        int size = width/15;
//        for (int deviation = 0; deviation <= matrix1[0].length - matrix2[0].length; deviation += 1) {
//            temp_matrix1 = new int[height][width][3];
//            temp_matrix2 = new int[height][width][3];
//            for (int i = 0; i < width; i++) {
//                for (int j = 0; j < height; j++) {
//                    for (int k = 0; k < 3; k++) {
//                        temp_matrix1[j][i][k] = matrix1[j][i + deviation][k];
//                        temp_matrix2[j][i][k] = matrix2[j][i][k];
//                    }
//                }
//            }
//            double metrics = 0;
//            double counter = 0;
//            if (use_approx){
//                Random rand = new Random();
//                for (int i = 0; i < n_rnd; i++){
//                    int[][][] rbatch1 = new int[size][size][3];
//                    int[][][] rbatch2 = new int[size][size][3];
//                    int y_r = rand.nextInt(width - size + 1);
//                    int x_r = rand.nextInt(height - size + 1);
//                    for (int n = 0; n < size; n++){
//                        for (int m = 0; m < size; m++){
//                            for (int k = 0; k < 3; k++){
//                                rbatch1[n][m][k] = temp_matrix1[x_r + n][y_r + m][k];
//                                rbatch2[n][m][k] = temp_matrix2[x_r + n][y_r + m][k];
//                            }
//                        }
//                    }
//                    double temp = SCC.get_similarity(rbatch1, rbatch2);
//                    if(!Double.isNaN(temp)) {
//                        metrics += temp;
//                        counter++;
//                    }
//                }
//                metrics /= counter;
//            }
//            else {
//                metrics = SCC.get_similarity(temp_matrix1, temp_matrix2);
//            }
//            if (metrics < best_metrics) {
//                best_metrics = metrics;
//                opt_deviation = deviation;
//            }
//
//            System.out.println(" " + metrics +" "+ deviation);
//        }
//        return new double[]{opt_deviation, best_metrics};
//    }
    public int[][] getCompressedMap(int[][] map){
        int height = map.length;
        int width = map[0].length;

//        System.out.println("WDYM???" + width +" "+ height);
        int ws = 1;
        boolean stop=false;
        for(int i=1; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[i][j] != map[0][j]) {
                    stop = true;
                    break;
                }
            }
            if (stop)
                break;
            else
                ws += 1;
        }
        current_state.window_size = ws;
        int cwidth = (int) Math.ceil((double)width/current_state.window_size);
        int cheight = (int) Math.ceil((double)height/current_state.window_size);
        int[][] tempmap = new int[cheight][cwidth];
        for(int i=0; i < cheight; i++){
            for(int j=0; j < cwidth; j++){
                tempmap[i][j] = map[i*current_state.window_size][j*current_state.window_size];
            }
        }
        return tempmap;
    }
    public double[][] getFullMap(int[][] map, int width, int height){
        double[][] tempmap = new double[width][height];
        for(int i=0; i < width; i++){
            for(int j=0; j < height; j++){
                //System.out.println("********** " + ((int)Math.ceil((double)(i+1)/window_size) - 1) + " " + ((int)Math.ceil((double)(j+1)/window_size) - 1) + " " + i + " " + j);
                tempmap[i][j] = map[(int)Math.ceil((double)(j+1)/current_state.window_size) - 1][(int)Math.ceil((double)(i+1)/current_state.window_size) - 1];
            }
        }
        return tempmap;
    }
    public double Std(byte[][][] matrix) {
        double[] std = {0,0,0};
        double[] avg = Average(matrix);
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[0].length; j++) {
                    std[k] += Math.pow((matrix[i][j][k]-avg[k]),2);
                }
            }
            std[k] = Math.pow(std[k] / (matrix.length*matrix[0].length-1), 0.5);
        }
        return (std[0]+std[1]+std[2])/3;
    }
    public double[] Average(byte[][][] matrix) {
        double[] average_c = {0,0,0};
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[0].length; j++) {
                    average_c[k] += matrix[i][j][k];
                }
            }
            average_c[k] = average_c[k] / (matrix.length*matrix[0].length);
        }
        return average_c;
    }


    public BufferedImage MatrixToImage(double[][] matrix){
        int width = matrix.length;
        int height = matrix[0].length;
        double max = 0;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                if (Math.abs(matrix[i][j]) > Math.abs(max)) {
                    max = matrix[i][j];
                }

            }
        BufferedImage Result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
            {
                int v = Math.max(0, (int)((matrix[i][j] / max) * 255));
                Color MyColor = new Color(v, v, v);
                Result.setRGB(i, j, MyColor.getRGB());
            }
        }
        return Result;
    }

    public BufferedImage MatrixToImage(int[][] matrix){
        int width = matrix.length;
        int height = matrix[0].length;
        double max = 0;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                if (Math.abs(matrix[i][j]) > Math.abs(max)) {
                    max = matrix[i][j];
                }

            }
        BufferedImage Result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
            {
                int v = Math.max(0, (int)((matrix[i][j] / max) * 255));
                Color MyColor = new Color(v, v, v);
                Result.setRGB(i, j, MyColor.getRGB());
            }
        }
        return Result;
    }

    public static void main(String[] args) throws IOException {
        MainFrame fr = new MainFrame();

    }
}