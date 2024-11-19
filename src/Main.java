
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class ImageChanger {
    //Уменьшает изображение в натуральное количество раз
    public BufferedImage Filtration(int[][] source, int height, int width, double average, int typeofmask) {
        int size = 3;
        //Маски стандартные, легко гуглятся
        int[][] mask;
        double Gx, Gy, Grad;
        int[][] tempmatrix = new int[width][height];
        double limit = 0;
        switch (typeofmask) {
            case 1 -> { //Roberts
                mask = new int[][]{{1, 0}, {0, -1}};
                size = 2;
                limit = average * 0.15; // Пределы подбирались исключительно вручную
            }
            case 2 -> { //Previtt
                mask = new int[][]{{-1, -1, -1}, {0, 0, 0}, {1, 1, 1}};
                limit = average * 0.42; // Для большей точности, конечно, лучше, чтобы функцию подбирала машина (можно сделать с помощью машинного обучения и т.п)
            }
            case 3 -> { //Sobel
                mask = new int[][]{{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
                limit = average * 0.42; // Это скорее любительская lite-версия
            }
            default -> mask = new int[][]{{0, 0, 0}, {0, 1, 0}, {0, 0, 0}};
        }

        for (int i = 0; i < width - 2; i++) {
            for (int j = 0; j < height - 2; j++) {
                Gx = 0;
                Gy = 0;
                for (int k = 0; k < size; k++) {
                    for (int h = 0; h < size; h++) {
                        Gx += source[i + k][j + h] * mask[k][h];
                        Gy += source[i + k][j + h] * mask[size - 1 - k][size - 1 - h];
                    }
                }
                Grad = Math.hypot(Gx, Gy);
                tempmatrix[i + 1][j + 1] = (int) Grad;
                if (Grad > limit) {
                    tempmatrix[i + 1][j + 1] = 255; //белый цвет для краевых точек
                } else {
                    tempmatrix[i + 1][j + 1] = 0; //черный для внутренних
                }
            }
        }
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                Color pixelcolor = new Color(tempmatrix[i][j], tempmatrix[i][j], tempmatrix[i][j]);
                result.setRGB(i, j, pixelcolor.getRGB());
            }
        }
        //раскраска оставшихся пикселей
        for (int i = 0; i < width; i++) {
            Color pixelcolor = new Color(0, 0, 0);
            result.setRGB(i, 0, pixelcolor.getRGB());
            if (size != 2)
                result.setRGB(i, height - 1, pixelcolor.getRGB());
        }

        for (int j = 1; j < height - 1; j++) {
            Color pixelcolor = new Color(0, 0, 0);
            result.setRGB(0, j, pixelcolor.getRGB());
            if (size != 2)
                result.setRGB(width - 1, j, pixelcolor.getRGB());
        }

           /*
           Для записи фильтрованных изображений в файл
           try {
                String fileName = "Deepmaptest.jpg";
                File file = new File(fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                ImageIO.write(result, "png", file);
            }catch(Exception e){}*/
        return result;
    }
}

//    public static int[] arrayRankTransform(byte[] arr) {
//        int N = arr.length;
//        //create result array and re-use it to store sorted elements of original array
//        byte[] sorted = Arrays.copyOf(arr, N);
//        int[] ranks = new int[N];
//        Arrays.sort(sorted);
//        //fill map of ranks based on sorted sequence of elements
//        for (int i = 0; i < N; i++){
//            for (int j = 0; j < N; j++){
//                if (arr[i] == sorted[j]){
//                    sorted[j] = -1;
//                    ranks[i] = j;
//                    break;
//                }
//            }
//        }
//        //fill result array with ranks, sequence of elements must be preserved from original array
//        return ranks;
//    }
abstract class CompareMethod {
    public int[][][] Transpose(int[][][] scanim){
        int width = scanim.length;
        int height = scanim[0].length;
        int[][][] transposed = new int[height][width][3];
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    transposed[j][i][k] = scanim[i][j][k];
                }
            }
        }
        return transposed;
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
    public static double get_similarity(int[][][] scanim1, int[][][] scanim2){
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
        int N = width*height;
        byte[] array1 = new byte[N];
        byte[] array2 = new byte[N];
        int[] ranks1;
        int[] ranks2;
        long[] d = {0,0,0};
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
                d[k] += Math.pow(ranks1[i]-ranks2[i],2);
            }
            total[k] = (1 - (((double) d[k] / N) * 6) / (Math.pow(N,2) - 1));
        }
        return (total[0] + total[1] + total[2])/3;
    }

    public static double get_similarity(int[][][] scanim1, int[][][] scanim2) {
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
        int N = width*height;
        byte[] array1 = new byte[N];
        byte[] array2 = new byte[N];
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
        double[] total = {0, 0, 0};
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    total[k] += Math.abs(scanim1[i][j][k] - scanim2[i][j][k]);
                }
            }
        }

        return 1 - (total[0] + total[1] + total[2])/(3*255*width*height);
    }

}

class SSD extends CompareMethod {

    public double get_similarity(byte[][][] scanim1, byte[][][] scanim2) {
        int width = scanim1.length;
        int height = scanim1[0].length;
        double[] total = {0, 0, 0};
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    total[k] += Math.pow(scanim1[i][j][k] - scanim2[i][j][k], 2);
                }
            }
        }
        return 1 - (total[0] + total[1] + total[2])/(3*255*255*width*height);
    }

}



class MainFrame extends JFrame {
    int guiImageWidth = 400; //540, 280
    int guiImageHeight = 300; //360, 240

    //new File("D:\\Images\\left.jpg");
    File RI = null;
    // new File("D:\\Images\\right.jpg");
    BufferedImage image1 = null;
    BufferedImage image2 = null;
    BufferedImage gradientstripe;
    BufferedImage buff;
    BufferedImage DepthMap;
    BufferedImage DepthMap_full;
    BufferedImage ShiftedImage;
    int window_size;

    Map<String, Boolean> gen_params = new HashMap<String,Boolean>();
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
    JLabel WindowSizeLabel = new JLabel("Scan screen size");
    JLabel VdevLabel = new JLabel("MVDev");
    JLabel TimeLabel = new JLabel("Time");
    JLabel IterLabel = new JLabel("Iters");
    JLabel NSLabel = new JLabel("NS");
    JLabel ECLabel = new JLabel("EC");
    JTextField VdevTF = new JTextField("0", 3);
    JTextField TimeTF = new JTextField("0", 4);
    JTextField IterTF = new JTextField("0", 4);
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
    JButton GoMakeSomeMagic = new JButton("Go");
    JButton LoadDM = new JButton("LDM");
    JButton GetMetrics = new JButton("GM");
    JButton GetLogs = new JButton("GL");
    JButton Save = new JButton("Save");
    JButton SelectLeftImage = new JButton("Load Left Image");
    JButton SelectRightImage = new JButton("Load Right Image");
    ButtonGroup methods = new ButtonGroup();
    ButtonGroup filtration = new ButtonGroup();
    JFileChooser loadimage = new JFileChooser();
    JTextField UserSize = new JTextField("10", 4);
    Box zero = Box.createHorizontalBox();
    Box first = Box.createHorizontalBox();
    Box second = Box.createHorizontalBox();
    Box third = Box.createHorizontalBox();
    Box third2 = Box.createHorizontalBox();
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
    JTextField FilterSizeTF = new JTextField("3", 3);
    JCheckBox ApprxAlgsCB = new JCheckBox("AX");
    JCheckBox VerboseCB = new JCheckBox("VB");

    JCheckBox LocDevsCB = new JCheckBox("LD");
    JTextField ECoefTF = new JTextField("2.2", 3);
    JTextField NSegmentsTF = new JTextField("5", 3);

    JLabel text = new JLabel("Filter size");
    JButton ApplyFunction = new JButton("Apply");
    //JComboBox Function = new JComboBox(new String[]{"amedian", "wmedian","prewitt","sobel","median", "avg", "min", "max", "gamma", "clarity", "equalize"});
    JComboBox Function = new JComboBox(new String[]{"amedian", "median", "wmedian", "equalize"});


    ImageProcessor improc = new ImageProcessor();
    int[] size_adjustment = {0, 0};
    int filtersize = 9;
    private int max_deviation;
    private int vdev;
    private int counter4saving = 0;
    private boolean use_opt = false;

    private int itercounter;
    public static int dtis = 10000; // scale for converting double to int and then backwards

    public BufferedImage THImage;
    public BufferedImage DevsImage;

    int start, finish;

    int apprx_choice = 2;

    public static void main(String[] args) throws IOException {
        MainFrame fr = new MainFrame();


    }
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
    public MainFrame() throws IOException {

        loadimage.setCurrentDirectory(new File("D:\\Images\\"));
        loadimage.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"));

        frame.setLayout(new BorderLayout());
        frame.setSize(guiImageWidth*2+35, guiImageHeight*2+70); //размер фрейма
        frame.setTitle("DMGen by Kirill Kolesnikov, inspired by Oleg Kovalev & Mikalai Yatskou");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        UserSize.setMaximumSize(UserSize.getPreferredSize());
        VdevTF.setMaximumSize(VdevTF.getPreferredSize());
        FilterSizeTF.setMaximumSize(FilterSizeTF.getPreferredSize());
        TimeTF.setMaximumSize(TimeTF.getPreferredSize());
        IterTF.setMaximumSize(IterTF.getPreferredSize());
        NSegmentsTF.setMaximumSize(NSegmentsTF.getPreferredSize());
        ECoefTF.setMaximumSize(ECoefTF.getPreferredSize());
        UserSize.setHorizontalAlignment(JTextField.CENTER);
        VdevTF.setHorizontalAlignment(JTextField.CENTER);
        FilterSizeTF.setHorizontalAlignment(JTextField.CENTER);
        TimeTF.setHorizontalAlignment(JTextField.CENTER);
        IterTF.setHorizontalAlignment(JTextField.CENTER);
        NSegmentsTF.setHorizontalAlignment(JTextField.CENTER);
        ECoefTF.setHorizontalAlignment(JTextField.CENTER);


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

        methdsb.add(Box.createVerticalGlue());
        methdsb.add(nccb);
        methdsb.add(Box.createVerticalGlue());
        methdsb.add(sadb);
        methdsb.add(Box.createVerticalGlue());
        methdsb.add(ssdb);
        methdsb.add(Box.createVerticalGlue());
        methdsb.add(sccb);
        methdsb.add(Box.createVerticalGlue());
        methdsb.add(kccb);
        methdsb.add(Box.createVerticalGlue());

        zero.add(Box.createHorizontalGlue());
        zero.add(SelectLeftImage);
        zero.add(Box.createHorizontalGlue());
        zero.add(SelectRightImage);
        zero.add(Box.createHorizontalGlue());

        first.add(Box.createHorizontalGlue());
        first.add(Function);
        first.add(Box.createHorizontalGlue());
        first.add(ApplyFunction);
        first.add(Box.createHorizontalGlue());

        second.add(Box.createHorizontalGlue());
        second.add(text);
        second.add(Box.createHorizontalGlue());
        second.add(FilterSizeTF);
        second.add(Box.createHorizontalGlue());
        second.add(AutoScaleCB);
        second.add(Box.createHorizontalGlue());


        third.add(Box.createHorizontalGlue());
        third.add(AdaptiveSizeCB);
        third.add(Box.createHorizontalGlue());
        third.add(ApprxAlgsCB);
        third.add(Box.createHorizontalGlue());
        third.add(VerboseCB);
        third.add(Box.createHorizontalGlue());
        third.add(NSLabel);
        third.add(Box.createHorizontalGlue());
        third.add(NSegmentsTF);
        //third.add(ConvolutionApproximation);
        third.add(Box.createHorizontalGlue());

        // HERE
        fourth.add(Box.createHorizontalGlue());
        fourth.add(VdevLabel);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(VdevTF);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(Box.createHorizontalStrut(8));
        fourth.add(ECLabel);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(ECoefTF);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(Box.createHorizontalStrut(9));
        fourth.add(TimeLabel);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(TimeTF);
        fourth.add(Box.createHorizontalGlue());

        fifth.add(Box.createHorizontalGlue());
        fifth.add(WindowSizeLabel);
        fifth.add(Box.createHorizontalGlue());
        fifth.add(UserSize);
        fifth.add(Box.createHorizontalGlue());
        fifth.add(IterLabel);
        fifth.add(Box.createHorizontalGlue());
        fifth.add(IterTF);
        fifth.add(Box.createHorizontalGlue());

        sixth.add(Box.createHorizontalGlue());
        sixth.add(GoMakeSomeMagic);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(LoadDM);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(GetMetrics);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(GetLogs);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(Save);
        sixth.add(Box.createHorizontalGlue());//чуть позже вернемся к графическому интерфейсу, для начала нужно раздобыть немного информации


        ncc.setSelected(true);
        AutoScaleCB.setSelected(true);
        LoadDM.setEnabled(true);
        GetMetrics.setEnabled(false);
        GetLogs.setEnabled(false);
        Save.setEnabled(false);

        SelectLeftImage.addActionListener(actionEvent -> {
            try {
                File LI = null;
                int leftret = loadimage.showDialog(null, "Load left image");
                if (leftret == JFileChooser.APPROVE_OPTION) {
                    LI = loadimage.getSelectedFile();
                }
                //image1 = improc.SizeChangerLinear(ImageIO.read(LI), guiImageWidth*2, guiImageHeight*2);
                image1 = ImageIO.read(LI);
                iwidth = image1.getWidth();
                iheight = image1.getHeight();
                System.out.println("Relation: " + iheight + " " +iwidth + " " + guiImageWidth*2 + " " + guiImageHeight*2*iheight / iwidth);
                frame.getContentPane();
                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(image1, guiImageWidth, guiImageHeight, apprx_choice)));

//                image1 = improc.SizeChangerDistanceBased(image1, guiImageWidth*2, guiImageHeight*2*iheight / iwidth);
                iwidth = image1.getWidth();
                iheight = image1.getHeight();
                frame.setVisible(true);
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong while reading, try again");
            }
        });
        SelectRightImage.addActionListener(actionEvent -> {
            try {
                File RI = null;
                int rightret = loadimage.showDialog(null, "Load right image");
                if (rightret == JFileChooser.APPROVE_OPTION) {
                    RI = loadimage.getSelectedFile();
                }
                //image2 = improc.SizeChangerLinear(ImageIO.read(RI), guiImageWidth*2, guiImageHeight*2);
                image2 = ImageIO.read(RI);
                iwidth = image2.getWidth();
                iheight = image2.getHeight();

                frame.getContentPane();

                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(image2, guiImageWidth, guiImageHeight, apprx_choice)));

//                image2 = improc.SizeChangerDistanceBased(image2, guiImageWidth*2, guiImageHeight*2*iheight / iwidth);
                iwidth = image2.getWidth();
                iheight = image2.getHeight();
                //panel.add(RightImageLabel);
                frame.setVisible(true);
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong while reading, try again");
            }
        });

        ApplyFunction.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(image1 != null && image2 != null){
                    String str = Function.getSelectedItem().toString();
                    improc.setSize((int)Double.parseDouble(FilterSizeTF.getText()));

                    switch (str) {
                        case "median":
                            improc.loadFull(DepthMap);
                            DepthMap = ImageCopy(improc.OrderStatFiltration("median"));
                            break;
                        case "amedian":
                            improc.loadFull(DepthMap);
                            DepthMap = ImageCopy(improc.AdaptiveMedianFiltration());
                            break;
                        case "wmedian":
                            improc.loadFull(DepthMap);
                            DepthMap = ImageCopy(improc.WeightedMedian());

//                            case "gamma":
//                                improc.setSize(0);
//                                improc.loadFull(DepthMap);
//                                //DepthMap = improc.ImageSubstitution(DepthMap, ImageCopy(improc.applyFunction(2, Double.parseDouble(Filtersize.getText()))), 1);
//                                DepthMap = ImageCopy(improc.applyFunction(2, Double.parseDouble(FiltersizeTF.getText())));
//                                break;
//                            case "min":
//                                improc.loadFull(DepthMap);
//                                DepthMap = ImageCopy(improc.OrderStatFiltration("min"));
//                                break;
//                            case "max":
//                                improc.loadFull(DepthMap);
//                                DepthMap = ImageCopy(improc.OrderStatFiltration("max"));
//                                break;
//                            case "avg":
//                                improc.loadFull(DepthMap);
//                                DepthMap = ImageCopy(improc.OrderStatFiltration("avg"));
//                                break;
//                            case "sobel":
//                                improc.loadFull(DepthMap);
//                                DepthMap = ImageCopy(improc.Sobel());
//                                break;
//                            case "prewitt":
//                                improc.loadFull(DepthMap);
//                                DepthMap = ImageCopy(improc.Prewitt());
//                                break;
//                            case "clarity":
//                                improc.loadFull(DepthMap);
//                                DepthMap = ImageCopy(improc.Clarity());
//                                break;
                        case "equalize":
                            improc.loadFull(DepthMap);
                            DepthMap = improc.ImageContrastIncrease();
                            break;
                    }
                    if (AutoScaleCB.isSelected())
                        DepthMap = ImageCopy(improc.ImageScaler(DepthMap));

//                    BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerDistanceBased(improc.SizeChanger(DepthMap, Math.round(((double)window_size*guiImageWidth/ iwidth))), guiImageWidth, guiImageHeight)));

                    DepthMap_full = MatrixToImage(getFullMap(improc.BWImageToMatrix(DepthMap), DepthMap_full.getWidth(), DepthMap_full.getHeight()));
                    BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(DepthMap_full, guiImageWidth, guiImageHeight, 3)));
                }
            }
        });

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
        //Вот тут главная претензия: какого черта центр это совсем не центр, что касается и остальных сторон света
        frame.getContentPane();
        panel.setLayout(new GridLayout(2, 2, 10, 10));
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
        //frame.pack();
        frame.setVisible(true);

        LoadDM.addActionListener(actionEvent -> {
            try {
                int dmret = loadimage.showDialog(null, "Load ground-true map");
                File temp = null;
                if (dmret == JFileChooser.APPROVE_OPTION) {
                    temp = loadimage.getSelectedFile();
                }
                GetMetrics.setEnabled(true);
                buff = improc.SizeChangerS(ImageIO.read(temp), iwidth, iheight, apprx_choice);
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong while reading, try again");
            }
        });
        GetMetrics.addActionListener(actionEvent -> {
            double[] metrics = getMapMetrics(improc.ImageToMatrix(buff), improc.ImageToMatrix(DepthMap_full), false);
            dmc = new DMComparator(this, buff, DepthMap_full, metrics[0], metrics[1]);
        });
        GetLogs.addActionListener(actionEvent -> {
            lv = new LogsVisualizator(this, image1, image2, logs, correlation_m, vdev, dtis);
        });
        Save.addActionListener(actionEvent -> {
            File outputfile;
            try{
                counter4saving = 0;
                do {
                    counter4saving++;
                    outputfile = new File("Maps\\DepthMap" + counter4saving + ".png");
                } while (outputfile.exists());

                if (DepthMap == null) {
                    throw new IOException();
                }

                //outputfile = new File("Maps\\DepthMap" + counter4saving + ".png");
                ImageIO.write(DepthMap_full, "png", outputfile);
                JOptionPane.showMessageDialog(MainFrame.this, "Depth map was saved");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong, try again");
            }
        });
        GoMakeSomeMagic.addActionListener(actionEvent -> {

            ClearWindows();
            SecureAllParameters();
            GenerateDepthMap();

            long timeElapsed = finish - start;
            TimeTF.setText(Long.toString(timeElapsed));
            IterTF.setText(Long.toString(itercounter));

            // Сохранение
            SaveResults();

            //BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)window_size*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
//            BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerDistanceBased(improc.SizeChanger(DepthMap, Math.round(((double)window_size*guiImageWidth/ iwidth))), guiImageWidth, guiImageHeight)));
            BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(DepthMap_full, guiImageWidth, guiImageHeight, apprx_choice)));

            //GradientOfColors.setIcon(new ImageIcon(improc.SizeChangerLinear(gradientstripe, guiImageWidth, guiImageHeight)));
            LoadDM.setEnabled(true);
            GetLogs.setEnabled(true);
            Save.setEnabled(true);
            //frame.pack();
            //panel.add(BottomImageLabel, SOUTH);
            //panel.add(GradientOfColors, SOUTH);
            //frame.add(panel);
            frame.setVisible(true);

        });

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
    public int GetCompareType(){
        int compare_type = 0;
        if (ncc.isSelected())
            compare_type = 1;
        else if (scc.isSelected())
            compare_type = 2;
        else if (kcc.isSelected())
            compare_type = 3;
        else if (sad.isSelected())
            compare_type = 4;
        else if (ssd.isSelected())
            compare_type = 5;
        return compare_type;
    }

    public void SecureAllParameters(){
        gen_params.put("AdaptiveMode", AdaptiveSizeCB.isSelected());
        gen_params.put("VerboseMode", VerboseCB.isSelected());
        gen_params.put("ApproximateMode", ApprxAlgsCB.isSelected());
        gen_params.put("LocalizedMode", AdaptiveSizeCB.isSelected());
//        gen_params.put("LocalDevsMode", Local);

    }
    
    public int GetWindowSize(){
        return Integer.valueOf(UserSize.getText());
    }
    public void GenerateDepthMap(){
        matrix1 = new byte[iwidth][iheight][3]; //матрица для первого снимка
        matrix2 = new byte[iwidth][iheight][3]; //матрица для второго снимка
        use_opt = gen_params.get("ApproximateMode");
        //преобразование изображения в чб, конфликтует с некоторыми цветами
        if (bw.isSelected()) {
            improc.loadFull(image1);
            image1 = ImageCopy(improc.BW());
            improc.loadFull(image2);
            image2 = ImageCopy(improc.BW());
            LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(image1, guiImageWidth, guiImageHeight, apprx_choice)));
            RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerS(image2, guiImageWidth, guiImageHeight, apprx_choice)));
        }

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
        window_size = GetWindowSize();
        int compare_type = GetCompareType();
        //Пожалуй, самая трудоемкая функция в данной программе, сложность - порядка O(n^3), но т.к. число n - далеко не маленькое, зачастую приходится подождать
        start = (int) System.currentTimeMillis();
        DepthMap_full = CalculateDepthMap(window_size, compare_type);
        finish = (int)System.currentTimeMillis();
    }
    public void ClearWindows(){
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
    public void SaveResults(){
        try{
            counter4saving = 0;
            File outputfile;
            do {
                counter4saving++;
                outputfile = new File("Maps\\DepthMap" + counter4saving + ".png");
            } while (outputfile.exists());
        }catch (Exception e){}

        try {
            if (DepthMap == null) {
                throw new IOException();
            }
            File outputfile;
            outputfile = new File("Maps\\DepthMap" + counter4saving + ".png");
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
            outputfile = new File("Thresholds\\Thresholds" + counter4saving + ".png");
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
            outputfile = new File("Shifted_Images\\Shifted_Image" + counter4saving + ".png");
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
            outputfile = new File("Deviations\\Deviations" + counter4saving + ".png");
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
    public double[] getDeviation(byte[][][] matrix1, byte[][][] matrix2, boolean use_approx, boolean verbose) {
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
        rs = (int) (area / 2);

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
//            System.out.println(" " + correlation +" "+ deviation);
        }
        if (verbose){
            pf = new PlotFrame(MainFrame.this, MatrixToImage(best_matrix1), MatrixToImage(best_matrix2), opt_deviation, best_correlation, plot_data);
        }
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
        rs = (int) (area / 2);

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
    public double[] getMapMetrics(int[][][] matrix1, int[][][] matrix2, boolean use_approx) {
        // matrix2 is our map and is smaller
        int width = matrix2[0].length;

        int height = matrix2.length;
        double best_correlation = 0;
        int opt_deviation = width / 4;
        int[][][] temp_matrix1, temp_matrix2;

        int n_rnd = width / 15;
        int size = width / 15;

        //double area = width/3.5;
        //int stripe = Math.max((int)area/75, 1);
        System.out.println("Metrics calculation");
        for (int deviation = 0; deviation <= matrix1[0].length - matrix2[0].length; deviation += 1) {
            temp_matrix1 = new int[height][width][3];
            temp_matrix2 = new int[height][width][3];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    for (int k = 0; k < 3; k++) {
                        //System.out.println(j + " " + (i + deviation) + " "+ matrix1.length + " " + matrix1[0].length);
                        temp_matrix1[j][i][k] = matrix1[j][i + deviation][k];
                        temp_matrix2[j][i][k] = matrix2[j][i][k];
                    }
                }
            }
            double correlation = 0;
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
                    double temp = NCC.get_similarity(rbatch1, rbatch2);
                    //double temp = improc.PSNR(rbatch1, rbatch2);
                    if (!Double.isNaN(temp)) {
                        correlation += temp;
                        counter++;
                    }
                }
                correlation /= counter;
            } else {
                correlation = NCC.get_similarity(temp_matrix1, temp_matrix2);
                //correlation = improc.PSNR(temp_matrix1, temp_matrix2);
            }
            if (correlation > best_correlation) {
                best_correlation = correlation;
                opt_deviation = deviation;
            }

            System.out.println(" " + correlation + " " + deviation);
        }
        return new double[]{opt_deviation, best_correlation};
    }
    public double[] getMapPSNR(int[][][] matrix1, int[][][] matrix2, boolean use_approx){
        // matrix2 is our map and is smaller
        int width = matrix2[0].length;

        int height = matrix2.length;
        double best_metrics = 1000;
        int opt_deviation = width/5;
        int[][][] temp_matrix1, temp_matrix2;

        int n_rnd = width/15;
        int size = width/15;
        for (int deviation = 0; deviation <= matrix1[0].length - matrix2[0].length; deviation += 1) {
            temp_matrix1 = new int[height][width][3];
            temp_matrix2 = new int[height][width][3];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    for (int k = 0; k < 3; k++) {
                        temp_matrix1[j][i][k] = matrix1[j][i + deviation][k];
                        temp_matrix2[j][i][k] = matrix2[j][i][k];
                    }
                }
            }
            double metrics = 0;
            double counter = 0;
            if (use_approx){
                Random rand = new Random();
                for (int i = 0; i < n_rnd; i++){
                    int[][][] rbatch1 = new int[size][size][3];
                    int[][][] rbatch2 = new int[size][size][3];
                    int y_r = rand.nextInt(width - size + 1);
                    int x_r = rand.nextInt(height - size + 1);
                    for (int n = 0; n < size; n++){
                        for (int m = 0; m < size; m++){
                            for (int k = 0; k < 3; k++){
                                rbatch1[n][m][k] = temp_matrix1[x_r + n][y_r + m][k];
                                rbatch2[n][m][k] = temp_matrix2[x_r + n][y_r + m][k];
                            }
                        }
                    }
                    double temp = SCC.get_similarity(rbatch1, rbatch2);
                    if(!Double.isNaN(temp)) {
                        metrics += temp;
                        counter++;
                    }
                }
                metrics /= counter;
            }
            else {
                metrics = SCC.get_similarity(temp_matrix1, temp_matrix2);
            }
            if (metrics < best_metrics) {
                best_metrics = metrics;
                opt_deviation = deviation;
            }

            System.out.println(" " + metrics +" "+ deviation);
        }
        return new double[]{opt_deviation, best_metrics};
    }
    public double[][] getFullMap(int[][] map, int width, int height){
        double[][] tempmap = new double[width][height];
        for(int i=0; i < width; i++){
            for(int j=0; j < height; j++){
                //System.out.println("********** " + ((int)Math.ceil((double)(i+1)/window_size) - 1) + " " + ((int)Math.ceil((double)(j+1)/window_size) - 1) + " " + i + " " + j);
                tempmap[i][j] = map[(int)Math.ceil((double)(j+1)/window_size) - 1][(int)Math.ceil((double)(i+1)/window_size) - 1];
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
    public BufferedImage CalculateDepthMap(int window_size, int compare_type) {
        int width = matrix1.length; // 500
        int height = matrix1[0].length; // 400

        int locale_c = Integer.parseInt(NSegmentsTF.getText());
        int locale_w = width / locale_c;
        int locale_h = height / locale_c;
        double[][] thresh_matrix = new double[locale_c][locale_c];
        double[][] deviations = null;
        double[][] correlations = null;

        double EC = Double.parseDouble(ECoefTF.getText());
        int opt_deviation, corrected_width;

        if (!gen_params.get("LocalizedMode")) {
            double[] devInfo = getDeviation(matrix1, matrix2, use_opt, gen_params.get("VerboseMode"));
            double light_coef = EC / devInfo[1];
            opt_deviation = (int) (devInfo[0]);
            this.max_deviation = (int) (opt_deviation * light_coef);

        } else {
            double[][][] devsInfo = getDeviations(matrix1, matrix2, locale_c);
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
                    deviations[i][j] *= (EC*(0.85)/correlations[i][j]);
                }
            }
            opt_deviation = (int) (max_dev);
            double light_coef = EC *(0.85)/ md_cor;
            this.max_deviation = (int) (opt_deviation * light_coef);
        }


        corrected_width = (width - Math.abs(opt_deviation));




        System.out.println("\nOPTIMAL DEVIATION: " + Integer.toString(opt_deviation) + " pixels");
        System.out.println("\nMAX DEVIATION: " + Integer.toString(max_deviation) + " pixels");
        //max_deviation = -100;
        System.out.println("\nMatrix size: " + (int) Math.ceil((double) corrected_width / window_size) + ' ' + (int) Math.ceil((double) height / window_size) + " pixels");
        matrix3 = new double[(int) Math.ceil((double) corrected_width / window_size)][(int) Math.ceil((double) height / window_size)]; //матрица смещений
        double[][] m3_upd = new double[corrected_width][height];
        logs = new int[(int) Math.ceil((double) corrected_width / window_size)][(int) Math.ceil((double) height / window_size)][];
        correlation_m = new double[(int) Math.ceil((double) corrected_width / window_size)][(int) Math.ceil((double) height / window_size)][];

        CompareMethod method = switch (compare_type) {
            // Pearson (NСС)
            case 1 -> new NCC();
            // Spearman (SCC)
            case 2 -> new SCC();
            // Kendall (KCC)
            case 3 -> new KCC();
            // SAD
            case 4 -> new SAD();
            // SSD
            case 5 -> new SSD();
            default -> null;
        };
        double best_correlation;
        int coincidentx;
        int coincidenty;
        vdev = Integer.valueOf(VdevTF.getText());
        int tempsizeadd;
        itercounter = 0;

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

        if (gen_params.get("AdaptiveMode")) {
            for (int i = 0; i < locale_c; i++) {
                for (int j = 0; j < locale_c; j++) {
                    double temp = Std(getPart(matrix1, locale_w * i, locale_h * j, Math.min(width - locale_w * i, locale_w), Math.min(height - locale_h * j, locale_h), 0));
                    thresh_matrix[i][j] = AC * Math.pow(temp, 0.5);
                }
            }
            for (int i = 0; i < locale_c; i++) {
                for (int j = 0; j < locale_c; j++) {
                    System.out.print((int) thresh_matrix[j][i] + " ");
                }
                System.out.println();
            }

            System.out.println("Adaptive Areas: " + locale_w + " " + locale_h);
        }


        double[][] devs_upd = new double[corrected_width][height];

        //double [][] thresh_matrix = new double[(int)Math.ceil((double)corrected_width / window_size)][(int)Math.ceil((double)height / window_size)];
        double[][] tm_upd = new double[corrected_width][height];
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
                std_thresh = thresh_matrix[(Math.min(col_image1/locale_w, locale_c-1))][Math.min((row_image1/locale_h), locale_c-1)];

                if (gen_params.get("LocalizedMode")) {
                    max_deviation = (int) (deviations[(Math.min(col_image1 / locale_w, locale_c - 1))][Math.min((row_image1 / locale_h), locale_c - 1)]);
//                    System.out.println("Max deviation: " + max_deviation);
                }
                //std_thresh = (std_thresh + std1/10)/1.1;
                //System.out.println("NEW STD THRESH: " + std_thresh);


                if(gen_params.get("AdaptiveMode") && std1 < std_thresh && std1 > 0) {
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
                        itercounter++;
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

                        if (use_opt && peak_b >= w && best_correlation > c_thresh)
                            break;
                    }
                    if (use_opt && peak_b >= w && best_correlation > c_thresh){
                        break;
                    }
                }
//                System.out.println(">>> " + comp_counter + " "+ correlation_m[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ window_size)][(int)Math.ceil((double)row_image1 / window_size)].length);


                //hdprob += Math.abs(coincidenty - row_image1);
                double disparity = Math.hypot(coincidentx - col_image1, coincidenty - row_image1);
                //System.out.println("DIST: " + distance);
                int[] eP1 = extendPart(col_image1, row_image1, sc_width, sc_height, tempsizeadd);
                int[] eP2 = extendPart(coincidentx, coincidenty, sc_width, sc_height, tempsizeadd);
                //System.out.println(tempsizeadd);
                //col_image1, row_image1, coincidentx, coincidenty, sc_width, sc_height, metrics, (int)distance, std1, std2
                logs[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ window_size)][(int)Math.ceil((double)row_image1 / window_size)] = new int[]{eP1[0], eP1[1], eP2[0], eP2[1], eP1[2], eP1[3], (int)(dtis*best_correlation),  (int)((dtis*disparity)/Math.hypot(max_deviation, 2*vdev)), (int)(dtis*std1), (int)(dtis*std2), (int)(dtis*tempsizeadd/width), comp_counter, max_deviation};
                //System.out.println("&&&&& " + col_image1 + ' ' + sc_width);
                matrix3[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ window_size)][(int)Math.ceil((double)row_image1 / window_size)] = disparity;
                for (int i = 0; i < sc_width; i++) {
                    for (int j = 0; j < sc_height; j++) {
                        m3_upd[col_image1 + Math.min(opt_deviation, 0) + i][row_image1 + j] = disparity;
                        tm_upd[col_image1 + Math.min(opt_deviation, 0) + i][row_image1 + j] = std_thresh;
                        devs_upd[col_image1 + Math.min(opt_deviation, 0) + i][row_image1 + j] = max_deviation;
                    }
                }

            }
        }

        itercounter = (int)((double) itercounter/(((int) Math.ceil((double) corrected_width / window_size) * (int) Math.ceil((double) height / window_size))));
        double max = 0;
        // double min = matrix3[0][0];
        int i_max = 0;
        int j_max = 0;
        /* Поиск максимума в матрице.
        RGB это три числа, каждое от 0 до 255. (0,0,0) - чёрный
        (255,255,255) - абсолютно белый цвет.
        Максимальное число в матрице считаем как бы за максимальную удалённость, это будет чисто белый цвет - 255.
        Далее каждый элемент матрицы закрашиваем цветом, равным (текущий элемент/максимальный элемент)*255.*/
        gradientstripe = new BufferedImage(20, height, BufferedImage.TYPE_INT_RGB);
        Color mycolor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < 20; j++) {
                mycolor = new Color(255 * i / height, 255 * i / height, 255 * i / height);
                gradientstripe.setRGB(j, height - i - 1, mycolor.getRGB());
            }
        }

        DepthMap = MatrixToImage(matrix3);
        BufferedImage DepthMap_full = MatrixToImage(m3_upd);
        THImage = MatrixToImage(tm_upd);
        DevsImage = MatrixToImage(devs_upd);
        ShiftedImage = getShiftedImage(matrix1, Math.abs(opt_deviation));
        return DepthMap_full;
    }
    public BufferedImage MatrixToImage(double[][] matrix){
        int width = matrix.length;
        int height = matrix[0].length;
        double max = 0;
        int i_max = 0, j_max = 0;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                if (Math.abs(matrix[i][j]) > Math.abs(max)) {
                    max = matrix[i][j];
                    //System.out.print("max= "+ max);
                    i_max = i;
                    j_max = j;
                }
//            if (min > Math.abs(matrix[i][j])) {
//                min = matrix[i][j];
//            }
            }
        BufferedImage Result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)//максимальное количество пикселей в строке
            {
                //System.out.print((int)matrix[i][j]+" ");
//                System.out.println(matrix[i][j] + " " + (matrix[i][j] / max) * 255);
                int v = Math.max(0, (int)((matrix[i][j] / max) * 255));
                Color MyColor = new Color(v, v, v);
                Result.setRGB(i, j, MyColor.getRGB()); //установка цвета
                // Рисование закрашенного прямоугольника с началом координам x=i*w, y=j*w. Ширина и длина w.
            }
            //System.out.println();
        }
        return Result;
    }
}