import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import static java.awt.datatransfer.DataFlavor.javaFileListFlavor;

//final int[][] ic = new int[][]{}


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
    BufferedImage icon = null;
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

    int opt_deviation, corrected_width;

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

    // yep, a hardcoded icon so there is no need for external files
    public final int[][] ico_matrix = new int[][]{
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13933642, 13999433, 13012319, 13736269, 13473898, 10050724, 9722293, 4398591, 2952959, 1180671, 16776960, 16761344, 16762112, 16762112, 16762112, 16762112, 16762112, 16762624, 16753664, 16711680, 16711680, 16711680, 16711680, 16711680, 16711680, 16711680, 16711680, 16711680, 16711680, 16711680, 16711680, 6029253, 8650627, 11075466, 15448473, 11169443, 10182822, 6826411, 7680180, 10111927, 9913531, 9913531, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 13933642, 14064959, 13343113, 13671237, 15115597, 14787667, 14590535, 14919491, 15246654, 15179584, 15310654, 15442238, 16761659, 16763706, 16761913, 16759619, 16488537, 16287589, 16153713, 16150655, 16148868, 16743018, 16742257, 16740983, 16739707, 16737157, 16273317, 15942835, 15547072, 14823618, 12525009, 11408596, 11409349, 11873668, 11284615, 11547273, 12861078, 10827437, 10237108, 11945147, 11025603, 7874239, 5381065, 10109853, 10241983, 9913531, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 9919398, 16776960, 13210991, 14525244, 28475706, 416847957, 1020960848, 1524277581, 1759158094, 1926731086, 2077592910, 2094369869, 2094369869, 2127857997, 2144635213, 2144634957, -2133555888, -2032895656, -2016185764, -2016252319, -2016253082, -2016320150, -2100602254, -2117380235, -2117446026, -2117446281, -2100735110, -2016916601, -2016982901, -2017180528, -2034155119, -2135802987, 2142058646, 2142058646, 2108241559, 2108110491, 2108044955, 2057516446, 1872311981, 1838757809, 1637825718, 1015887548, 377565881, 21903812, 8860353, 188, 16766580, 9919398, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 12551786, 255, 16765709, 16763928, 634953803, 2145100620, -857955760, -287465136, -136536241, -52716722, -2516915, -2583476, -2584245, -2650548, -2651316, -2717365, -2784178, -2784941, -2720679, -2853539, -2920352, -2921625, -2988437, -3186832, -3122567, -3189123, -3386752, -3518843, -3585398, -3717488, -3915118, -4244078, -4703853, -5163629, -5557358, -6410607, -7197549, -7263083, -6803299, -23448148, -23185227, -90359624, -325765187, -963823937, 2140026813, 579613877, 9713100, 2031844, 16776960, 9915572, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 9919398, 11302024, 16765440, 16769551, 1037673037, -857954995, -2251443, -2251443, -2252465, -2319025, -2451379, -2517684, -2583989, -2584757, -2651316, -2717621, -2784434, -2785197, -2720167, -2721187, -2788255, -19632283, -19699607, -2989201, -3121804, -3122820, -3254911, -3453053, -20296824, -20428659, -37403759, -4178287, -4572526, -5163631, -5754997, -6542712, -7527295, -8905350, -9167232, -7919471, -6999382, -6473801, -6801219, -6998082, -7194688, -7587902, -981388350, 996815049, 7209187, 247, 10377646, 9919398, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 8274124, 16768256, 15184716, 735880529, -824400563, -2251187, -2251443, -2251698, -2252976, -2385330, -2517428, -2583733, -2650549, -2717109, -2783925, -2784690, -2785454, -2786218, -2786982, -2787747, -19632028, -19698839, -2988691, -2989963, -3122310, -3189121, -3255420, -3453049, -20362613, -20626546, -20889712, -4703857, -5295218, -6017656, -7002754, -8118925, -9365910, -10415514, -10612121, -8969846, -7458903, -6735943, -6801219, -7063361, -7522366, -7784508, -8309305, -847957820, 694101697, 5184968, 12452001, 12222368, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 12617574, 15250246, 80516186, -1864522159, -2250930, -19028403, -2251443, -2252465, -2319281, -2517172, -2583733, -2650549, -2717877, -2849973, -2916534, -2983092, -2983600, -2918825, -19631013, -19697312, -19698586, -2922388, -2923659, -3056264, -3123075, -3255165, -3387259, -20362615, -20560501, -4178289, -4638580, -5361015, -6214781, -7397002, -8644243, -9825693, -10481565, -10415513, -10217873, -9297522, -7589974, -6932548, -6932289, -7325502, -7587901, -7981370, -8374841, -8834362, -1939329593, 89203643, 4922840, 7221678, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 13342585, 14789442, 568108886, -673405362, -2250931, -19028403, -2251698, -2253233, -2451122, -2583477, -2585013, -2783670, -2916280, -3114683, -3246525, -3313340, -3182258, -3182765, -19894950, -19698588, -19699351, -2923153, -2924423, -3056773, -3189121, -3321211, -20296313, -20494455, -4046711, -4572790, -5426811, -6543495, -7790992, -9038748, -9694881, -9890970, -8905350, -7525224, -6802260, -7196244, -7655507, -7063619, -7194431, -7522365, -7784508, -8177977, -8571706, -8768569, -630509880, 576070593, 5646032, 7154600, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 13670745, 14657867, 1323017551, -220354739, -19028147, -19028659, -2252465, -2319281, -2517172, -2584501, -2717621, -3048122, -3246781, -3510976, -3643074, -3775171, -3644347, -3513260, -3447716, -3185308, -2988947, -2923915, -3056518, -3123074, -3255164, -3453050, -20428663, -4046457, -4638590, -5493125, -6609549, -8054171, -9235875, -9367203, -8840591, -7131755, -5883474, -5948489, -6210887, -6735944, -7786317, -7326017, -7390781, -7653436, -7981370, -8243513, -8637498, -8965176, -193908279, 1331965128, 6960345, 6695086, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 6495407, 14788928, 1910285651, -35805362, -19028403, -2251699, -2252977, -2385074, -2517940, -2651317, -2916280, -3312830, -3709123, -4170953, -4435149, -4500686, -4369352, -3974836, -3711657, -3448989, -3186833, -3056265, -3122564, -3189374, -3321467, -3650937, -4046457, -4638846, -5558663, -6806933, -8054950, -9039272, -9169822, -7658110, -5883738, -5555021, -5751627, -6079303, -6276166, -6932292, -8048458, -7457089, -7456573, -7850043, -8177977, -8440377, -8834104, -9227574, -43044406, 1834953418, 6171089, 11499172, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 48, 15050496, 2145364316, -2250930, -2251443, -2252209, -2253232, -2451123, -2584501, -2783670, -3180476, -3775172, -4435149, -5159894, -5555676, -5621723, -5292245, -4634562, -4108203, -3647647, -3319183, -3122566, -3123074, -3255162, -3453306, -3848824, -4507005, -5558663, -6806933, -8251816, -8908461, -8382103, -6541421, -5358420, -5423694, -5620556, -5882440, -6144581, -6406979, -6997827, -8245322, -7719487, -7653179, -8112441, -8243512, -8702262, -9030454, -9358646, -9752116, 2103060425, 6633177, 16777063, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777111, 14649344, -2116048292, -2250930, -2251698, -2252720, -2318769, -2451379, -2650549, -2981816, -3445184, -4369100, -5358042, -5950946, -6083042, -6281182, -6215388, -5622992, -4570548, -4108966, -3583375, -3188867, -3189375, -3321208, -20362359, -4112506, -5098883, -6741140, -8251817, -8646060, -7528593, -5556582, -5095763, -5161040, -5554764, -5817161, -6013510, -6275652, -6603328, -7129155, -8507720, -7850560, -7784250, -8177721, -8570934, -8767797, -9161782, -9489716, -9948722, -2125060665, 13134079, 184, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14582528, -2116048548, -2251441, -2252465, -2252720, -2319024, -2451890, -2651061, -3114170, -3840965, -5028309, -5951202, -6082788, -6148833, -6281180, -6412765, -6346970, -5360581, -4373674, -3715728, -3320705, -3255417, -3321715, -3717238, -4638590, -6347152, -8251817, -8448940, -6740358, -5031004, -4964434, -5095503, -5291852, -5620043, -5947975, -6079045, -6537793, -6734656, -7260225, -8638790, -8112700, -8046649, -8309048, -8767797, -8964661, -9358389, -9620788, -10080052, -2125060665, 14315519, 165, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14582272, -2116048548, -2251952, -2252720, -2253232, -2253998, -2386608, -2717622, -3246524, -4170953, -5555676, -5885153, -5226197, -4831683, -4766143, -5161923, -5689035, -5952464, -4769200, -3913875, -3387007, -3255669, -3387503, -3783030, -4901505, -7135386, -8383145, -6411648, -4702811, -4767572, -5095504, -5292109, -5488714, -5816648, -6013509, -6341186, -6603328, -6865470, -7522111, -8901446, -8375099, -8243255, -8636726, -8767797, -9161267, -9489459, -9817138, -10342193, -2125191993, 13528319, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14580992, -2116048803, -2252463, -2252720, -2253488, -2188716, -2321325, -2717621, -3246525, -4369101, -5291992, -3974077, -3579308, -3777450, -3711910, -3844518, -4109223, -5032116, -5032624, -4045714, -3453052, -3321713, -3387757, -20494704, -5098623, -7529632, -6149249, -4637017, -4636501, -4964177, -5161039, -5357387, -5619785, -5947717, -6209601, -6472000, -6668863, -7062076, -7522110, -8901446, -8506171, -8440119, -8767797, -8964404, -9357874, -9751601, -9948208, -10473264, -2125191993, 13593599, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14580736, -2116048803, -2252463, -2253231, -2253998, -2188971, -2256043, -2652083, -3180986, -4171209, -3511990, -2985636, -3513257, -3447972, -3317147, -3318165, -3517075, -4111510, -4704927, -4177554, -3519096, -3321966, -3388008, -20494440, -5032827, -6083714, -4702812, -4701781, -4832595, -5095247, -5291852, -5488457, -5750597, -6012995, -6340929, -6603328, -6931005, -7193147, -7718973, -9163588, -8768570, -8636726, -8833333, -9292338, -9489202, -9817137, -10144815, -10670127, -2125191993, 13331455, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14580480, -2116048803, -2252719, -2253744, -2188717, -2189226, -2190503, -2520492, -3180729, -3379381, -2390933, -19565469, -3250592, -3317147, -3252371, -3187341, -3452042, -3913874, -4573338, -4375435, -3453299, -3387755, -3454054, -3716449, -4769394, -5031782, -4504916, -4767060, -4898129, -5160524, -5422922, -5619527, -5947203, -6144065, -6537535, -6668606, -7062075, -7390010, -7915323, -9229123, -8899641, -8767797, -9161267, -9423410, -9685809, -9948208, -10407214, -10735919, -2125323064, 12872191, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14513920, -2116049059, -2253231, -2253998, -2189226, -2189736, -2190758, -2389159, -2852015, -2520461, -2125175, -2590355, -2987927, -3121042, -3187342, -3122821, -3386756, -3782535, -4507020, -4638342, -3453550, -3453800, -3585123, -3781980, -4440164, -4702558, -4570195, -4766801, -4963404, -5226058, -5488199, -5750339, -6012738, -6340415, -6602814, -6931004, -7193146, -7520824, -8046394, -9491522, -9161783, -8964404, -9292338, -9620273, -9817137, -10079279, -10538542, -10932527, -2109266996, 10970367, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14578944, -2116049059, -2253742, -2188972, -2189481, -2190247, -2191014, -2257826, -2457250, -2190723, -2057839, -2392197, -2790799, -2857610, -2857857, -2990206, -3255163, -3716986, -4309886, -4572799, -3519595, -3519588, -3716446, -3913306, -4307803, -4702814, -4569680, -4832076, -5094474, -5422664, -5619011, -5946946, -6143808, -6537022, -6733885, -7127355, -7323961, -7717431, -8308793, -9753663, -9292854, -9226803, -9357874, -9751601, -9948208, -10341678, -10669870, -10998319, -2109398324, 10511103, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14644224, -2116049059, -2188459, -2189226, -2189992, -2191014, -2257315, -2258336, -2325405, -2124930, -1991019, -2325885, -2659207, -2659199, -2592371, -2659188, -3122548, -3585397, -4244091, -4375416, -3519077, -3650657, -3847515, -3979096, -4307541, -4833367, -4766800, -4897611, -5291080, -5487941, -5750082, -6078016, -6340415, -6602557, -6864956, -7192633, -7520824, -7717431, -8439864, -9884734, -9424182, -9357874, -9554738, -9882672, -10144814, -10473006, -10669870, -11129392, -2109398324, 10380031, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14641408, -2116050080, -2188714, -2189481, -2190503, -2191269, -2257826, -2259101, -2391451, -2257797, -2123888, -2392187, -2658944, -2592374, -2459761, -2592625, -3056494, -3585392, -4244084, -3980141, -3584866, -3781981, -3913306, -4110422, -4438867, -4701523, -5029713, -5028682, -5421893, -5619011, -5815617, -6208831, -6536764, -6667836, -7127097, -7323960, -7717431, -7848502, -8636213, -10147132, -9489717, -9488945, -9751345, -10079279, -10275886, -10604334, -10735406, -11325998, -2109463602, 10052863, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14573824, -2099273631, -2189224, -2190247, -2191014, -2257571, -2258591, -2325403, -2458263, -2458769, -2325120, -2459251, -2659192, -2659187, -2658674, -2857583, -3254634, -20494444, -20823661, -3650660, -3716191, -3847515, -4044631, -4307027, -4569681, -4897870, -5226060, -5356615, -5486913, -5684289, -6077759, -6339901, -6602300, -6929977, -7192632, -7586103, -7782966, -8176180, -8767285, -10146876, -9686323, -9685552, -9948208, -10144814, -10341678, -10669870, -10932270, -11522604, -2109463602, 9987327, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14571264, -2099274653, -2189735, -2190758, -2257315, -2258336, -2324892, -2391961, -2459028, -2591633, -2658189, -2592121, -2725493, -2924146, -2990703, -3254890, -3453800, -20429162, -20362855, -3650657, -3781981, -3978841, -4110165, -4372818, -4700750, -4963403, -5225803, -5554249, -5552448, -5749054, -6142781, -6470972, -6733371, -7061048, -7257911, -7717174, -7979316, -8307251, -8898612, -10277946, -9882674, -9816624, -10079279, -10275885, -10604077, -10735405, -11259691, -11522604, -2109463602, 9987327, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14569984, -2099274908, -2190246, -2191014, -2257826, -2259101, -2391451, -2458262, -2525585, -2658190, -2791306, -2858118, -2924412, -3056501, -3255151, -3387755, -3388010, -3453800, -3519588, -3781982, -3913306, -4044630, -4307027, -4503888, -4766540, -5094731, -5554249, -6079561, -6210120, -6010939, -6273595, -6602043, -6929464, -7192119, -7585589, -7847732, -8175666, -8503858, -8964148, -10343225, -10014001, -9947951, -10210350, -10538028, -10669612, -11062827, -11325227, -11522604, -2109463858, 9921791, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14569216, -2099274908, -2190757, -2257315, -2258336, -2325403, -2458008, -2459283, -2591888, -2724749, -2857609, -2924679, -3122819, -3189118, -3255415, -3321712, -3387755, -3454054, -3650657, -3847516, -3978840, -4175956, -4438353, -4700749, -4963403, -5357386, -6014026, -6802001, -7852890, -7325515, -6273080, -6732857, -7126327, -7257398, -7716403, -8044338, -8306738, -8634672, -9160754, -10540088, -10210608, -10078766, -10341165, -10538027, -10734891, -11193899, -11456554, -11588139, -2109463859, 9790463, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14502912, -2099275164, -2257059, -2258081, -2259101, -2391706, -2458518, -2525841, -2592651, -2791048, -2858375, -3056516, -3188862, -3255162, -3321460, -3321966, -3453800, -3585122, -3781981, -3913306, -4110165, -4372818, -4569678, -4897612, -5291594, -5817163, -6736465, -7918428, -9428587, -10741374, -7652683, -6797621, -7126326, -7454261, -7847475, -8175666, -8438065, -8700464, -9357361, -10802486, -10276143, -10210093, -10472492, -10669098, -10931498, -11324970, -11456554, -11522347, -2109463602, 9987327, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14436352, -2099340699, -2257569, -2258846, -2325659, -2458007, -2525075, -2592142, -2659208, -2792070, -2924418, -3056768, -3189371, -3255671, -3321713, -3387755, -3519588, -3716191, -3913306, -4044631, -4241492, -4503888, -4832077, -5291851, -5817164, -6736465, -7984220, -9625454, -11923087, -13760935, -9557348, -6994740, -7257398, -7716403, -7913267, -8241202, -8569137, -8831535, -9488432, -10868022, -10473007, -10340908, -10538027, -10800170, -11128106, -11390762, -11456554, -11522347, -2109463602, 9987327, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14434048, -2099341465, -2258334, -2325148, -2391961, -2459028, -2525840, -2658442, -2791302, -2858373, -2990719, -3122809, -3255160, -3321459, -3387501, -3453799, -3650656, -3781981, -3978840, -4175956, -4372818, -4766542, -5226317, -5883215, -6736722, -8049757, -10019186, -12185748, -14286000, -12972439, -8177991, -7126069, -7519540, -7781939, -8109874, -8372529, -8700464, -8962606, -9619504, -10998836, -10603821, -10472235, -10669098, -10996777, -11259177, -11390762, -11456554, -11522603, -2109463602, 9987327, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14299136, -2099408277, -2259100, -2391450, -2458263, -2525585, -2592141, -2724999, -2857606, -2924416, -3056762, -3189113, -3255669, -3321967, -3453289, -3584867, -3716191, -3913306, -4110165, -4307283, -4635473, -5095506, -5751890, -6868058, -8246883, -10150517, -12185749, -14220207, -12972439, -8507218, -7126583, -7322677, -7716147, -7847475, -8306481, -8569136, -8831535, -9159212, -9750575, -11129650, -10669613, -10603050, -10734377, -11127849, -11259177, -11259177, -11324969, -11522347, -2109463858, 9987327, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777107, 14098944, -2099409553, -2325402, -2392217, -2459284, -2526095, -2658697, -2791302, -2858372, -2990460, -3122552, -3254646, -3321456, -3387754, -3584356, -3716191, -3847515, -4044631, -4241492, -4635731, -5095508, -5817948, -6737249, -8247405, -10676101, -12776866, -13564332, -11659655, -8112970, -7061047, -7191605, -7519283, -7781682, -8109617, -8437552, -8700207, -9093677, -9290284, -10078509, -11326769, -10735148, -10734121, -10930985, -11193385, -11259177, -11259177, -11259177, -11391018, -2109398066, 10118911, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777106, 13899264, -2099475855, -2391705, -2458518, -2525585, -2592396, -2724999, -2857347, -2924671, -3056761, -3188597, -3255153, -3387244, -3518565, -3650145, -3781981, -3913305, -4175957, -4569939, -4964181, -5817948, -7000167, -8247925, -10545290, -13039269, -13170600, -10215286, -7128131, -6798134, -7125813, -7322163, -7584306, -7912753, -8306481, -8568622, -8765229, -9159212, -9355819, -10209581, -11457841, -10865963, -10799657, -11062056, -11259177, -11259177, -11259177, -11259177, -11390762, -2109398066, 10250495, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777124, 14034176, -2099476110, -2458006, -2459284, -2591887, -2658696, -2791299, -2923645, -2990459, -3122550, -3188848, -3320940, -3452775, -3584099, -3715935, -3847259, -4110166, -4438612, -4964181, -5817948, -7000168, -8904574, -11071129, -12842922, -12382618, -8836193, -6602301, -6732343, -6863157, -7190835, -7387698, -7715377, -7978289, -8437295, -8633902, -8962092, -9224748, -9552426, -10209581, -11523377, -10996778, -10930728, -11062056, -11193128, -11193384, -11258921, -11258921, -11324970, -2109398066, 10250751, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777141, 14234624, -2099542157, -2458516, -2525841, -2592651, -2724998, -2857599, -2924413, -3056246, -3188594, -3254636, -3386728, -3583843, -3649632, -3781468, -3913048, -4307028, -4898389, -5817948, -7000168, -8970110, -11595934, -13171117, -11398033, -7588434, -6404921, -6469944, -6797621, -6994228, -7387442, -7584048, -7846447, -8305710, -8568109, -8699437, -9158956, -9421099, -9683498, -10209581, -11654448, -11127849, -11061543, -11127592, -11193128, -11193128, -11193128, -11193128, -11324713, -2109397810, 10316799, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777140, 14101760, -2099542412, -2525073, -2592141, -2658951, -2791298, -2923644, -2990456, -3122291, -3254383, -3320426, -3518051, -3583842, -3715164, -3847001, -4109909, -4569940, -5423961, -6934631, -8970110, -11661470, -12908459, -10216322, -6800972, -6273594, -6404408, -6601014, -6863157, -7190835, -7452978, -7715119, -8043055, -8502317, -8699437, -8830765, -9290027, -9617962, -9749034, -10471723, -11851055, -11258921, -11061543, -11127336, -11127336, -11193128, -11193128, -11193128, -11324713, -2109397810, 10316799, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777140, 14034944, -2099542668, -2525838, -2592906, -2725252, -2857597, -2924154, -3056242, -3188334, -3254634, -3386213, -3518050, -3715164, -3780697, -3978069, -4241236, -4898389, -6277728, -8510585, -11661470, -12711592, -9165944, -6275398, -6076731, -6273080, -6469174, -6731316, -7059250, -7321650, -7649584, -7846191, -8239918, -8568109, -8699437, -9027628, -9355306, -9683498, -9880105, -10602794, -11851055, -11258921, -11061543, -11061543, -11061543, -11061543, -11127335, -11127336, -11258921, -2109332274, 10448127, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777140, 13832971, -2099609989, -2592394, -2658951, -2791553, -2857851, -2990197, -3122289, -3254379, -3320167, -3451745, -3583583, -3780698, -3911767, -4108882, -4306770, -5226839, -7262826, -10480274, -12580263, -8640627, -6012483, -5945403, -6075704, -6403125, -6599731, -6927922, -7190065, -7452721, -7780655, -8108333, -8370989, -8633644, -8895787, -9289257, -9486121, -9749034, -10076712, -10602794, -11851055, -11258921, -11061543, -11061543, -11061543, -11061543, -11061543, -11061543, -11193128, -2109332017, 10579967, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777140, 13631518, -2099677310, -2658439, -2791042, -2857596, -2924150, -3056242, -3122542, -3254376, -3385955, -3583327, -3714907, -3846232, -4042834, -4108624, -4503374, -5555031, -8116597, -11530397, -8640368, -5618755, -5879355, -6009912, -6140982, -6534195, -6665266, -7058480, -7255601, -7649327, -7911726, -8239404, -8370731, -8698923, -9092394, -9289257, -9551913, -9814570, -10142247, -10602794, -11851055, -11193128, -10995751, -10995494, -10995751, -10995494, -10995751, -11061287, -11193128, -2109134639, 11040767, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777140, 13500453, -2099743357, -2724740, -2857341, -2857848, -2924657, -3056495, -3188330, -3320164, -3451744, -3648859, -3780440, -3977300, -4108368, -4239437, -4568651, -5554773, -8378999, -8903286, -5684292, -5617212, -5944376, -6075448, -6403124, -6534195, -6927409, -7124016, -7517486, -7780398, -8042797, -8239404, -8567082, -8829738, -9289000, -9420328, -9682984, -10010920, -10207783, -10799401, -11851055, -11192872, -10929702, -10929702, -10929702, -10929702, -10929702, -10929702, -11127335, -2109134639, 11172351, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777140, 13369383, -2099743356, -2791297, -2857595, -2858356, -3056240, -3122027, -3253862, -3385697, -3517277, -3648855, -3845974, -4108368, -4108367, -4304712, -4567623, -5422410, -7065181, -5946952, -5485628, -5813048, -5944119, -6271540, -6468403, -6665266, -7058480, -7255087, -7648557, -7845677, -8173868, -8501546, -8698153, -9026344, -9354536, -9551399, -9879078, -10076455, -10338854, -11061544, -11916591, -11127336, -10929702, -10929702, -10929702, -10929702, -10929702, -10929702, -11061287, -2109068847, 11304191, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777139, 13303858, -2099743611, -2857340, -2858102, -2924657, -3122027, -3187814, -3254115, -3451486, -3582809, -3714390, -3976786, -4108110, -4304714, -4370247, -4632386, -5092161, -5684291, -5485884, -5615928, -5812535, -6074421, -6271027, -6533682, -6927409, -7058480, -7451693, -7648813, -8042284, -8239147, -8632617, -8829225, -9091880, -9420072, -9682471, -9944870, -10207783, -10469669, -11061544, -11851055, -11127079, -10863909, -10863909, -10863910, -10863910, -10929702, -10929702, -11061287, -2109068846, 11304447, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777162, 13500463, -2099809656, -2857594, -2858611, -2990446, -3122024, -3188066, -3319904, -3517018, -3648343, -3779667, -4042319, -4239178, -4370247, -4501317, -4697921, -4828991, -5288765, -5550393, -5615414, -5812021, -6205235, -6271026, -6664497, -6992432, -7254830, -7648300, -7845164, -8173355, -8370475, -8698153, -9026088, -9222952, -9485864, -9813542, -10010406, -10273062, -10600740, -11061544, -11785262, -11061287, -10798117, -10798117, -10798117, -10798117, -10798117, -10798117, -10995494, -2109068846, 11370239, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777184, 13698875, -2099876465, -2858100, -2924400, -3122027, -3188068, -3253856, -3450972, -3648342, -3713876, -3910735, -4107596, -4304456, -4435782, -4697921, -4828991, -5156411, -5550136, -5550393, -5745717, -5942580, -6270770, -6532913, -6861104, -7189039, -7451437, -7648300, -7976235, -8304426, -8632361, -8763689, -9091623, -9288743, -9682214, -9879078, -10141477, -10469412, -10534948, -11061288, -11785262, -10995494, -10732325, -10732325, -10798117, -10798117, -10798117, -10798117, -10929702, -2109068846, 11501567, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777183, 13500496, -2116720491, -2858608, -3055979, -3122023, -3253857, -3319390, -3516760, -3648085, -3779409, -4042060, -4238921, -4369990, -4566594, -4763456, -4959805, -5353017, -5615671, -5681463, -5811765, -6007346, -6401329, -6663728, -6925870, -7254061, -7582508, -7779371, -8107306, -8369962, -8632617, -8894760, -9157159, -9419815, -9813286, -9944614, -10272292, -10534691, -10534948, -10995751, -11653933, -10929702, -10666533, -10666533, -10666533, -10666533, -10732325, -10732325, -10864166, -2109068846, 11501567, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777183, 13238357, -2116786537, -2990445, -3122024, -3188068, -3253855, -3385177, -3582549, -3713875, -3910478, -4041802, -4304455, -4501060, -4697665, -4894270, -5156154, -5549879, -5615671, -5943092, -6271027, -6204466, -6334512, -6728751, -7056941, -7384876, -7713323, -7975722, -8172329, -8500520, -8763432, -9026087, -9222695, -9616165, -9813285, -10075685, -10337827, -10469155, -10469155, -10929959, -11588140, -10798374, -10600741, -10600741, -10666533, -10666533, -10666533, -10666533, -10798374, -2109003054, 11633151, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777183, 13172826, -2116786536, -3056234, -3187814, -3253856, -3385178, -3516502, -3648083, -3844943, -3910476, -4107079, -4369733, -4697408, -4763199, -5090620, -5353017, -5615415, -5811765, -6204979, -6401841, -6598705, -6729520, -6925101, -7055916, -7450155, -7647018, -8041001, -8303400, -8696870, -8893991, -9222438, -9484581, -9747493, -9878821, -10272036, -10403363, -10469155, -10469155, -10929703, -11587884, -10798117, -10534948, -10534948, -10534948, -10534948, -10600740, -10600741, -10798118, -2109003054, 11633407, 166, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777182, 13107297, -2116786535, -3121767, -3188066, -3319389, -3450711, -3516757, -3779151, -3910477, -4041288, -4172614, -4500290, -4696894, -5024828, -5155899, -5483832, -5746229, -6073907, -6270770, -6532913, -6926127, -7188525, -7385389, -7253292, -7449130, -7908136, -8105768, -8434215, -8696870, -8959269, -9287717, -9549860, -9746724, -10009636, -10272035, -10337571, -10337571, -10337571, -10864167, -11522348, -10666533, -10469155, -10469155, -10469155, -10534948, -10534948, -10534948, -10732325, -2109003054, 11896319, 165, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 16777201, 13041772, -2116786534, -3187811, -3319390, -3450712, -3516245, -20359249, -3779149, -3976011, -4106822, -4369219, -4565824, -4827965, -5090363, -5286969, -5680181, -5942580, -6204979, -6401841, -6729520, -7057454, -7385132, -7647788, -7910186, -7975722, -8039720, -8039207, -8367655, -8696358, -9090085, -9352740, -9615395, -9746467, -10140194, -10271522, -10271779, -10337571, -10337315, -10863910, -11456299, -10600740, -10403363, -10469155, -10469155, -10469155, -10469155, -10469155, -10666789, -2109003054, 10714623, 184, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 100, 13445764, 2094163100, -3253600, -3385179, -3450711, -20358994, -20424782, -3910219, -4106823, -4237892, -4500032, -4631102, -5024828, -5155898, -5483319, -5745717, -6008116, -6270770, -6598192, -6860335, -7188269, -7516203, -7713579, -7975465, -8238121, -8566055, -8565543, -8433447, -8695077, -9089060, -9221156, -9549603, -9812002, -10139938, -10205730, -10205730, -10205730, -10205730, -10732325, -11192873, -10403363, -10337571, -10337571, -10337571, -10403363, -10403363, -10469155, -10666533, 2102406355, 5260015, 16777063, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 8537248, 14112936, 1808358315, -36873821, -3450712, -3516244, -20359248, -20556108, -4041288, -4106821, -4434497, -4500032, -4762429, -5090363, -5417784, -5614390, -5877044, -6139443, -6532657, -6729264, -6991405, -7384876, -7581995, -7975466, -8106793, -8500007, -8696870, -8959269, -9156390, -9286692, -9220131, -9088548, -9351459, -9810978, -10008098, -10073889, -10139682, -10139937, -10139937, -10666533, -10929703, -10271779, -10271779, -10337571, -10337571, -10337571, -10337571, -10337571, -44089637, 1834561231, 5517531, 11499172, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 9919398, 9914804, 14704038, 1321620910, -187934554, -3516245, -20359249, -20424782, -3975496, -4106822, -4237635, -4565567, -4696638, -5024571, -5286456, -5483062, -5745717, -6073395, -6270258, -6663728, -6860335, -7188012, -7515947, -7713066, -8106537, -8172328, -8631334, -8762406, -9155876, -9352484, -9615395, -9746467, -9811747, -9613858, -9547810, -9613346, -9744930, -9876514, -9942305, -9942305, -10534949, -10600740, -10205730, -10205730, -10205730, -10271522, -10271522, -10271779, -10271779, -195084581, 1331441870, 5783017, 6695086, 9919398, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 11892899, 13716657, 566975405, -657827669, -3582034, -20424783, -20555850, -4041030, -4171844, -4434240, -4565567, -4827708, -5155129, -5351991, -5679925, -5876532, -6138417, -6466608, -6663472, -6991405, -7319340, -7581483, -7909418, -8172073, -8434215, -8696870, -8893477, -9286948, -9352484, -9680931, -9942818, -10139681, -10205730, -10139938, -9942562, -9613602, -9547554, -9416226, -9547810, -10403364, -10337315, -10205730, -10205730, -10205730, -10205730, -10205730, -10205730, -10205730, -631292711, 575809224, 4992735, 7154600, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 9849269, 15361958, 79124907, -1815521106, -3647567, -20490316, -3974983, -4106308, -4237121, -4499775, -4630589, -5089337, -5220407, -5417270, -5745204, -6072882, -6269232, -6532144, -6859822, -7122477, -7450411, -7647018, -7974953, -8237352, -8565031, -8696614, -9090085, -9286948, -9483812, -9746467, -10008353, -10139681, -10139681, -10073889, -10139681, -10139938, -10074146, -10008354, -10008354, -10205730, -10073889, -10139681, -10139938, -10139682, -10139681, -10139938, -10205474, -10205987, -1939982893, 90248617, 5053397, 7221678, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 9921693, 5455413, 14243519, 784684467, -809085260, -3843912, -3974725, -4105794, -4368448, -4564797, -4826682, -5154359, -5285686, -5482549, -5941554, -6138161, -6269232, -6597423, -6990893, -7188012, -7581227, -7843113, -8106024, -8433703, -8565031, -8827430, -9155365, -9286948, -9549347, -9877026, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10073889, -10008097, -832290086, 693775563, 4401885, 12452001, 12222368, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 9919398, 9522617, 12531377, 16748463, 1035882935, -859744582, -4105795, -4171329, -4499005, -4760890, -4891961, -5219894, -5350965, -5678387, -6071856, -6269232, -6465583, -6858797, -6990124, -7383851, -7711785, -7908649, -8039976, -8433703, -8696102, -9023781, -9089573, -9286692, -9614371, -9876514, -9942306, -10008098, -10008098, -10008098, -9942306, -9942306, -9942306, -9942306, -9942306, -9942306, -9876514, -9942306, -9942306, -10007841, -9876513, -9810465, -865581092, 1029978322, 1988863, 327897, 10377646, 9919398, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 9915572, 60160, 12860616, 15951285, 616058551, 2126468794, -893757246, -306750778, -122201402, -5023031, -5415988, -5612594, -5808944, -5940272, -6137135, -6464813, -6727212, -6924076, -7383081, -7776296, -7973159, -8038951, -8301350, -8694564, -8891684, -9022756, -9285155, -9547810, -9678882, -9678881, -9678882, -9678882, -9678882, -9678882, -9678882, -9678881, -9678881, -9678881, -9679137, -9679137, -26521890, -110408226, -311669026, -915715108, 2103920605, 610286297, 4680191, 3998938, 16776960, 9915572, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 9919398, 16718079, 12275121, 13392061, 21692574, 396643523, 1001804478, 1522095296, 1907251915, 1990941389, 2024233421, 2041141709, 2057787598, 2140690132, 2140493269, 2140427733, -2137893674, -2121247529, -2121509672, -2122362405, -2122493477, -2122493477, -2122690084, -2122690084, -2123083811, -2123608609, -2123608865, -2123608865, -2123608865, -2123608865, -2123608865, -2123608865, -2123608865, -2123608865, -2140385826, 2137869790, 2121027038, 2003520734, 1970032094, 1835683039, 1583825624, 1013201875, 392313301, 20119699, 5848807, 4592822, 255, 9919398, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 9913531, 10241983, 6237865, 13581992, 12080332, 13263302, 12407241, 11688400, 12281045, 13728481, 12419315, 12420340, 14656232, 14853607, 14788839, 16763615, 16765409, 16044261, 13945579, 13617644, 13486317, 12502770, 12437234, 11453429, 10075641, 9747193, 9812729, 9812729, 9812729, 9812729, 9812729, 9812729, 9812985, 8889340, 8229375, 8162558, 7892989, 7298546, 6572774, 6244079, 6116854, 4929769, 251, 12143502, 10241983, 9913531, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9913531, 9913531, 9914806, 9847994, 11166892, 9853351, 11169699, 15448473, 10878860, 12189542, 16776960, 16776960, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 16776960, 16776960, 16768396, 12682656, 9787559, 8538535, 7614388, 10111927, 9913531, 9913531, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 9919398, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };

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
                if ((buff != null) && (buff.getWidth() >= DepthMap_full.getWidth()) && (buff.getHeight() == DepthMap_full.getHeight()))
                    dmc = new DMComparator(MainFrame.this, buff, DepthMap_full, false);
                else
                    GetMetrics.setEnabled(false);
            }
        };
        GetMetrics.addActionListener(getMetricsAction);

        showLogsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                lv = new LogsVisualizator(MainFrame.this, image1, image2, logs, correlation_m, vdev, opt_deviation, dtis);
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
                DMGen.cancel(true);
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
        GoMakeSomeMagic.setToolTipText("Click to start the generation process");
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

        try {
            icon = CreateIcon();
//            icon = ImageIO.read(new File("icon.png"));
//            printImage(icon);
            frame.setIconImage(icon);
        }
        catch (Exception exc) {
            icon = null;
//            exc.printStackTrace();
        }

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
                        boolean ldm = name.contains("gt") || name.contains("ground") ||
                                      name.contains("true") || name.contains("truth");  // check if there is a depth map
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
            publish();
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
            } catch (Exception ignored) {
//                e.printStackTrace();
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
                        double temp = Std(getPart(matrix1, locale_w*i, locale_h*j,
                                                  Math.min(width-locale_w*i, locale_w),
                                                  Math.min(height-locale_h*j, locale_h), 0));
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


                    if(adaptive_mode && std1 < std_thresh) { // std1 > 0 ???
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
                    for (int deviation = Math.min(col_image1 - tempsizeadd, Math.abs(max_deviation));  deviation >= 0; deviation--){
//                        for (int deviation = 0;  deviation <= Math.min(col_image1 - tempsizeadd, Math.abs(max_deviation)); deviation++){

                            //for (int col_image2 = tempsizeadd; col_image2 < width - sc_width; col_image2++) {
                        for(int row_image2 = Math.max(0,row_image1-vdev); row_image2 < Math.min(height-sc_height+1,row_image1 + vdev + 1); row_image2++) {

                            if (this.isCancelled())
                                return null;

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



//            gradientstripe = new BufferedImage(20, height, BufferedImage.TYPE_INT_RGB);
//            Color mycolor;
//            for (int i = 0; i < height; i++) {
//                for (int j = 0; j < 20; j++) {
//                    mycolor = new Color(255 * i / height, 255 * i / height, 255 * i / height);
//                    gradientstripe.setRGB(j, height - i - 1, mycolor.getRGB());
//                }
//            }

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
            rs = 0; // (int) (area / 2)

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
                if (this.isCancelled())
                    return null;
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
//                    best_matrix1 = MCopy(temp_matrix1);
//                    best_matrix2 = MCopy(temp_matrix2);
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
            rs = 0; // (int) (area / 2)

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
                if (this.isCancelled())
                    return null;
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
        ColorModel cm = img.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = img.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
//        BufferedImage temp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
//        for (int i = 0; i < img.getWidth(); i++) {
//            for (int j = 0; j < img.getHeight(); j++) {
//                temp.setRGB(i, j, img.getRGB(i, j));
//            }
//        }
//        return temp;
    }
    public void printImage(BufferedImage img){
        for (int i = 0; i < img.getWidth(); i++) {
            System.out.print("{");
            for (int j = 0; j < img.getHeight(); j++) {
                System.out.print(img.getRGB(i, j));
                if (j != (img.getHeight()-1))
                    System.out.print(", ");
            }
            System.out.print("},");
            System.out.println();
        }
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
//        byte[][][] temp = new byte[matrix.length][matrix[0].length][matrix[0][0].length];
//        for (int i = 0; i < matrix.length; i++){
//            for(int j = 0; j < matrix[0].length; j++){
//                for(int k = 0; k < matrix[0][0].length; k++)
//                    temp[i][j][k] = matrix[i][j][k];
//            }
//        }
//        return temp;
        return Arrays.stream(matrix).map(byte[][]::clone).toArray(byte[][][]::new);
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


    public BufferedImage CreateIcon() {
        int width = ico_matrix.length;
        int height = ico_matrix[0].length;
        BufferedImage Result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Result.setRGB(i, j, ico_matrix[i][j]);
            }
        }
        return Result;
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