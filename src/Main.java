
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Random;

class ImageChanger {
    //Уменьшает изображение в натуральное количество раз
    public BufferedImage SizeDecreaser(BufferedImage source, int decreasingcoefficient) {

        if (source.getWidth() / decreasingcoefficient == (source.getWidth() / decreasingcoefficient) && source.getHeight() / decreasingcoefficient == (source.getHeight() / decreasingcoefficient)) {
            BufferedImage result = new BufferedImage(source.getWidth() / decreasingcoefficient, source.getHeight() / decreasingcoefficient, BufferedImage.TYPE_INT_RGB);
            int tempblue;
            int tempred;
            int tempgreen;
            for (int i = 0; i < source.getWidth() / decreasingcoefficient; i++) {
                for (int j = 0; j < source.getHeight() / decreasingcoefficient; j++) {
                    tempblue = 0;
                    tempred = 0;
                    tempgreen = 0;
                    for (int k = 0; k < decreasingcoefficient; k++) {
                        for (int h = 0; h < decreasingcoefficient; h++) {
                            Color color = new Color(source.getRGB(i * decreasingcoefficient + k, j * decreasingcoefficient + h));
                            tempblue += color.getBlue();
                            tempred += color.getRed();
                            tempgreen += color.getGreen();
                        }
                    }
                    tempblue /= Math.pow(decreasingcoefficient, 2);
                    tempred /= Math.pow(decreasingcoefficient, 2);
                    tempgreen /= Math.pow(decreasingcoefficient, 2);
                    Color tempcolor = new Color(tempred, tempgreen, tempblue);
                    //System.out.println(tempred+" "+tempgreen+" "+tempblue+" "+tempcolor);
                    result.setRGB(i, j, tempcolor.getRGB());
                }
            }
            return result;
        }
        return null;
    }

    //Увеличивает изображение в натуральное количество раз
    public BufferedImage SizeIncreaser(BufferedImage source, int increasingcoefficient) {
        BufferedImage result = new BufferedImage(source.getWidth() * increasingcoefficient, source.getHeight() * increasingcoefficient, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < source.getWidth() * increasingcoefficient; i++) {
            for (int j = 0; j < source.getHeight() * increasingcoefficient; j++) {
                Color tempcolor = new Color(source.getRGB(i / increasingcoefficient, j / increasingcoefficient));
                result.setRGB(i, j, tempcolor.getRGB());
            }
        }
        return result;
    }

    //Изменяет размер изображения, при этом необязательно в натуральное количество раз
    public BufferedImage SizeChanger(BufferedImage source, double changingcoefficient) {
        if (source.getWidth() / changingcoefficient == (int) (source.getWidth() / changingcoefficient) && source.getHeight() / changingcoefficient == (int) (source.getHeight() / changingcoefficient)) {
            int denominator = 1;
            do {
                changingcoefficient *= 10;
                denominator *= 10;
            } while (changingcoefficient != (int) changingcoefficient);
            int numerator = (int) changingcoefficient;
            //System.out.println("NUMERATOR "+numerator+" DENOMINATOR "+denominator);
            //максимально упрощаем дробь
            for (int i = 2; i <= (Math.min(numerator, denominator)); i++) {
                if (numerator % i == 0 && denominator % i == 0) {
                    numerator /= i;
                    denominator /= i;
                }
            }
            // System.out.println("NUMERATOR "+numerator+" DENOMINATOR "+denominator);
            BufferedImage temp = new BufferedImage(source.getWidth() * numerator, source.getHeight() * numerator, BufferedImage.TYPE_INT_RGB);
            temp = SizeIncreaser(source, numerator);
            BufferedImage result = new BufferedImage((int) (source.getWidth() * changingcoefficient), (int) (source.getHeight() * changingcoefficient), BufferedImage.TYPE_INT_RGB);
            result = SizeDecreaser(temp, denominator);
            return result;
        }
        return source;
    }

    public BufferedImage SizeChanger(BufferedImage source, int increasingcoefficient, int decreasingcoefficient) {
        if (source.getWidth() * increasingcoefficient / decreasingcoefficient == (int) (source.getWidth() * increasingcoefficient / decreasingcoefficient) && source.getHeight() * increasingcoefficient / decreasingcoefficient == (int) (source.getHeight() * increasingcoefficient / decreasingcoefficient)) {
            // System.out.println("NUMERATOR "+numerator+" DENOMINATOR "+denominator);
            BufferedImage temp = new BufferedImage(source.getWidth() * increasingcoefficient, source.getHeight() * increasingcoefficient, BufferedImage.TYPE_INT_RGB);
            temp = SizeIncreaser(source, increasingcoefficient);
            BufferedImage result = new BufferedImage((int) (source.getWidth() * increasingcoefficient / decreasingcoefficient), (int) (source.getHeight() * increasingcoefficient / decreasingcoefficient), BufferedImage.TYPE_INT_RGB);
            result = SizeDecreaser(temp, decreasingcoefficient);
            return result;
        }
        return source;
    }

    public void BW(BufferedImage source) {
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                Color color = new Color(source.getRGB(x, y));
                int blue = color.getBlue();
                int red = color.getRed();
                int green = color.getGreen();
                // Применяем стандартный алгоритм для получения черно-белого изображения
                int grey = (int) (red * 0.299 + green * 0.587 + blue * 0.114);
                //все каналы серого имеют одно и то же значение, поэтому и делаем одно значение
                Color newColor = new Color(grey, grey, grey);
                source.setRGB(x, y, newColor.getRGB());
            }
        }
    }

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
                limit = average * 0.15; // Пределы подрибались исключительно вручную
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
    protected Double bestTotal;
    //Суть методов установления соотвествия довольно похожа, различаются в основном математические формулы и их смысл (вследствие чего и разные предельные функции)
    public static double get_similarity(byte[][][] scanim1, byte[][][] scanim2){return 0;}
    public abstract boolean DoMagic(byte[][][] scanim1, byte[][][] scanim2);
    public void setStartTotal(Double startTotal){
        this.bestTotal = startTotal;
    }
}

class NCC extends CompareMethod {

    public static double get_similarity(byte[][][] scanim1, byte[][][] scanim2){
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

    @Override
    public boolean DoMagic(byte[][][] scanim1, byte[][][] scanim2) {
        double total_avg = get_similarity(scanim1, scanim2);

        if (total_avg > bestTotal) {
            bestTotal = total_avg;
            //System.out.println("New besttotal " + besttotal);
            return true;
        } else
            return false;
    }
}
class SCC extends CompareMethod {
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
    public static double get_similarity(byte[][][] scanim1, byte[][][] scanim2){
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
    @Override
    public boolean DoMagic(byte[][][] scanim1, byte[][][] scanim2) {
        double total_avg = get_similarity(scanim1, scanim2);
        if (total_avg > bestTotal) {
            bestTotal = total_avg;
            //System.out.println("New besttotal " + besttotal);
            return true;
        } else
            return false;
    }
}
class KCC extends CompareMethod {
    public static int[] arrayRankTransform(byte[] arr) {
        int N = arr.length;
        //create result array and re-use it to store sorted elements of original array
        byte[] sorted = Arrays.copyOf(arr, N);
        int[] ranks = new int[N];
        Arrays.sort(sorted);
        //fill map of ranks based on sorted sequence of elements
        for (int i = 0; i < N; i++){
            for (int j = 0; j < N; j++){
                if (arr[i] == sorted[j]){
                    sorted[j] = -1;
                    ranks[i] = j;
                    break;
                }
            }
        }
        //fill result array with ranks, sequence of elements must be preserved from original array
        return ranks;
    }

    public static double get_similarity(byte[][][] scanim1, byte[][][] scanim2) {
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

    @Override
    public boolean DoMagic(byte[][][] scanim1, byte[][][] scanim2) {
        double total_avg = get_similarity(scanim1, scanim2);
        if (total_avg > bestTotal) {
            bestTotal = total_avg;
            //System.out.println("New besttotal " + besttotal);
            return true;
        } else
            return false;
    }
}
class SAD extends CompareMethod {
    public static double get_similarity(byte[][][] scanim1, byte[][][] scanim2) {
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
    @Override
    public boolean DoMagic(byte[][][] scanim1, byte[][][] scanim2) {
        double total_avg = get_similarity(scanim1, scanim2);
        if (total_avg > bestTotal) {//В данном случае также
            bestTotal = total_avg;
            //System.out.println("New besttotal " + besttotal);
            return true;
        } else
            return false;
    }
}

class SSD extends CompareMethod {

    public static double get_similarity(byte[][][] scanim1, byte[][][] scanim2) {
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

    @Override
    public boolean DoMagic(byte[][][] scanim1, byte[][][] scanim2) {
        double total_avg = get_similarity(scanim1, scanim2);
        if (total_avg > bestTotal/*Math.pow(average*Math.pow(0.2*size,2),2)*/) {//В данном случае также
            bestTotal = total_avg;
            //System.out.println("New besttotal " + besttotal);
            return true;
        } else
            return false;
    }
}



class MainFrame extends JFrame {
    int guiImageWidth = 280;
    int guiImageHeight = 240;

    //new File("D:\\Images\\left.jpg");
    File RI = null;
    // new File("D:\\Images\\right.jpg");
    BufferedImage image1 = null;
    BufferedImage image2 = null;
    BufferedImage gradientstripe;
    BufferedImage buff;
    BufferedImage DepthMap;
    BufferedImage DepthMap_full;
    int scanscreensize;
    byte[][][] matrix1;//first image
    byte[][][] matrix2;//second image
    public double[][] matrix3;//deviation matrix
    public int[][][] logs;
    public LogsVisualizator lv;
    public DMComparator dmc;
    public PlotFrame pf;
    int width, height; //ширина и высота фотки
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
    JLabel ScanScreenLabel = new JLabel("Scan screen size");
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
    JCheckBox ApplyForDepthMap = new JCheckBox("Apply to DM");
    JCheckBox AdaptiveSize = new JCheckBox("AdaptSize");
    JCheckBox ConvolutionApproximation = new JCheckBox("ConvApprx");
    JTextField FiltersizeTF = new JTextField("3", 3);
    JCheckBox UseOptimCB = new JCheckBox("AX");
    JCheckBox VerboseCB = new JCheckBox("VB");
    JTextField ECoefTF = new JTextField("2.2", 3);
    JTextField NSegmentsTF = new JTextField("5", 3);

    JLabel text = new JLabel("Filter size");
    JButton ApplyFunction = new JButton("Apply");
    //JComboBox Function = new JComboBox(new String[]{"amedian", "wmedian","prewitt","sobel","median", "avg", "min", "max", "gamma", "clarity", "equalize"});
    JComboBox Function = new JComboBox(new String[]{"amedian", "wmedian", "median"});

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

    int start, finish;

    public static void main(String[] args) throws IOException {
        MainFrame fr = new MainFrame();


    }
    public int[][][] BtoIMatrix(byte[][][] matrix) {
        width = matrix.length;
        height = matrix[0].length;
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
        frame.setSize(600, 500); //размер фрейма
        frame.setTitle("Depth map by Kirill Kolesnikov, inspired by Oleg Kovalev & Mikalai Yatskou");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        UserSize.setMaximumSize(UserSize.getPreferredSize());
        VdevTF.setMaximumSize(VdevTF.getPreferredSize());
        FiltersizeTF.setMaximumSize(FiltersizeTF.getPreferredSize());
        TimeTF.setMaximumSize(TimeTF.getPreferredSize());
        IterTF.setMaximumSize(IterTF.getPreferredSize());
        NSegmentsTF.setMaximumSize(NSegmentsTF.getPreferredSize());
        ECoefTF.setMaximumSize(ECoefTF.getPreferredSize());
        UserSize.setHorizontalAlignment(JTextField.CENTER);
        VdevTF.setHorizontalAlignment(JTextField.CENTER);
        FiltersizeTF.setHorizontalAlignment(JTextField.CENTER);
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
        second.add(FiltersizeTF);
        second.add(Box.createHorizontalGlue());
        second.add(ApplyForDepthMap);
        second.add(Box.createHorizontalGlue());


        third.add(Box.createHorizontalGlue());
        third.add(AdaptiveSize);
        third.add(Box.createHorizontalGlue());
        third.add(UseOptimCB);
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
        fifth.add(ScanScreenLabel);
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
        ApplyForDepthMap.setSelected(true);
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
                image1 = improc.SizeChangerLinear(ImageIO.read(LI), guiImageWidth*2, guiImageHeight*2);
                //image1 = ImageIO.read(LI);
                width = image1.getWidth();
                height = image1.getHeight();
//                StringBuilder SIZES = new StringBuilder();
//                for (int i = 1; i < Math.min(height,width) / 2; i++) {
//                    if (height % i == 0 && width % i == 0) {
//                        System.out.print(i + " ");
//                        if (i == 1)
//                            SIZES.append(Integer.toString(i));
//                        else
//                            SIZES.append("/").append(i);
//                    }
//                }
//                Selections.setText(SIZES.toString());
                frame.getContentPane();
                //TopImageLabel.setIcon(new ImageIcon(ichange.SizeChanger(image1,2,3)));
                //panel.add(TopImageLabel, NORTH);
                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                //panel.add(LeftImageLabel, WEST);
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
                image2 = improc.SizeChangerLinear(ImageIO.read(RI), guiImageWidth*2, guiImageHeight*2);
                //image2 = ImageIO.read(RI);
                width = image2.getWidth();
                height = image2.getHeight();
                StringBuilder SIZES = new StringBuilder();
//                for (int i = 1; i < Math.min(height,width) / 2; i++) {
//                    if (height % i == 0 && width % i == 0) {
//                        System.out.print(i + " ");
//                        if (i == 1)
//                            SIZES.append(i);
//                        else
//                            SIZES.append("/").append(i);
//                    }
//                }
//                Selections.setText(SIZES.toString());
                frame.getContentPane();
                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
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
                    improc.setSize((int)Double.parseDouble(FiltersizeTF.getText()));
                    if (!ApplyForDepthMap.isSelected()) {
                        switch (str) {
                            case "median":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.OrderStatFiltration("median"));
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.OrderStatFiltration("median"));
                                break;
                            case "amedian":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.AdaptiveMedianFiltration());
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.AdaptiveMedianFiltration());
                                break;
                            case "wmedian":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.WeightedMedian());
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.WeightedMedian());
                                break;
//                            case "gamma":
//                                improc.setSize(0);
//                                improc.loadFull(image1);
//                                image1 = ImageCopy(improc.applyFunction(2, Double.parseDouble(FiltersizeTF.getText())));
//                                improc.loadFull(image2);
//                                image2 = ImageCopy(improc.applyFunction(2, Double.parseDouble(FiltersizeTF.getText())));
//                                break;
//                            case "min":
//                                improc.loadFull(image1);
//                                image1 = ImageCopy(improc.OrderStatFiltration("min"));
//                                improc.loadFull(image2);
//                                image2 = ImageCopy(improc.OrderStatFiltration("min"));
//                                break;
//                            case "max":
//                                improc.loadFull(image1);
//                                image1 = ImageCopy(improc.OrderStatFiltration("max"));
//                                improc.loadFull(image2);
//                                image2 = ImageCopy(improc.OrderStatFiltration("max"));
//                                break;
//                            case "avg":
//                                improc.loadFull(image1);
//                                image1 = ImageCopy(improc.OrderStatFiltration("avg"));
//                                improc.loadFull(image2);
//                                image2 = ImageCopy(improc.OrderStatFiltration("avg"));
//                                break;
//                            case "sobel":
//                                improc.loadFull(image1);
//                                image1 = ImageCopy(improc.Sobel());
//                                improc.loadFull(image2);
//                                image2 = ImageCopy(improc.Sobel());
//                                break;
//                            case "prewitt":
//                                improc.loadFull(image1);
//                                image1 = ImageCopy(improc.Prewitt());
//                                improc.loadFull(image2);
//                                image2 = ImageCopy(improc.Prewitt());
//                                break;
//                            case "clarity":
//                                improc.loadFull(image1);
//                                image1 = ImageCopy(improc.Clarity());
//                                improc.loadFull(image2);
//                                image2 = ImageCopy(improc.Clarity());
//                                break;
//                            case "equalize":
//                                improc.loadFull(image1);
//                                image1 = improc.ImageContrastIncrease();
//                                improc.loadFull(image2);
//                                image2 = improc.ImageContrastIncrease();
//                                break;
                        }
                        LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                        RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));

                    }
                    else{
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
//                            case "equalize":
//                                improc.loadFull(DepthMap);
//                                DepthMap = improc.ImageContrastIncrease();
//                                break;
                        }
                        BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                        DepthMap_full = MatrixToImage(getFullMap(improc.BWImageToMatrix(DepthMap), DepthMap_full.getWidth(), DepthMap_full.getHeight()));
                    }
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
                buff = improc.SizeChangerLinear(ImageIO.read(temp), guiImageWidth*2, guiImageHeight*2);
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong while reading, try again");
            }
        });
        GetMetrics.addActionListener(actionEvent -> {
            double[] metrics = getMapMetrics(improc.ImageToMatrix(buff), improc.ImageToMatrix(DepthMap_full), true);
            dmc = new DMComparator(this, buff, DepthMap_full, metrics[0], metrics[1]);
        });
        GetLogs.addActionListener(actionEvent -> {
            lv = new LogsVisualizator(this, BtoIMatrix(matrix1), BtoIMatrix(matrix2), logs, max_deviation, vdev, dtis);
        });
        Save.addActionListener(actionEvent -> {
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
                JOptionPane.showMessageDialog(MainFrame.this, "Saved");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong, try again");
            }
        });
        GoMakeSomeMagic.addActionListener(actionEvent -> {
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
            matrix1 = new byte[width][height][3]; //матрица для первого снимка
            matrix2 = new byte[width][height][3]; //матрица для второго снимка
            use_opt = UseOptimCB.isSelected();
            //преобразование изображения в чб, конфликтует с некоторыми цветами
            if (bw.isSelected()) {
                improc.loadFull(image1);
                image1 = ImageCopy(improc.BW());
                improc.loadFull(image2);
                image2 = ImageCopy(improc.BW());
                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
            }

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
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

            scanscreensize = Integer.valueOf(UserSize.getText());
            int userchoise = 0;
            if (ncc.isSelected())
                userchoise = 1;
            else if (scc.isSelected())
                userchoise = 2;
            else if (kcc.isSelected())
                userchoise = 3;
            else if (sad.isSelected())
                userchoise = 4;
            else if (ssd.isSelected())
                userchoise = 5;

            //Пожалуй, самая трудоемкая функция в данной программе, сложность - порядка O(n^3), но т.к. число n - далеко не такое маленькое, зачастую приходится подождать
            start = (int) System.currentTimeMillis();
            DepthMap_full = CalculateDeepMap(scanscreensize, userchoise);
            finish = (int)System.currentTimeMillis();
            long timeElapsed = finish - start;

            TimeTF.setText(Long.toString(timeElapsed));
            IterTF.setText(Long.toString(itercounter));

            // Сохранение
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

            //тест на круг, можно удалить
            /* BufferedImage Test = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            for(int i=0; i<width; i++){
                for(int j=0; j<height;j++){
                    if(Math.hypot(width/2-i, height/2-j)<100)
                        Test.setRGB(i,j,new Color(255,0,0).getRGB());
                    else
                        Test.setRGB(i,j, new Color(255,255,255).getRGB());

                }
            }*/
            //System.out.println("!!!!!!!!!!!!! "+((double)scanscreensize*guiImageWidth/width) +" "+ DepthMap.getWidth() +" "+ DepthMap.getWidth()*scanscreensize*guiImageWidth/width);
            //System.out.println("!!!!!!!!!!!!! "+(double)scanscreensize*guiImageHeight/height +" "+ DepthMap.getHeight() +" "+ DepthMap.getHeight()*scanscreensize*guiImageHeight/height);
            //System.out.println(improc.SizeChanger(DepthMap, ((double)scanscreensize*guiImageWidth/width + (double)scanscreensize*guiImageHeight/height)/2).getWidth()+" "+improc.SizeChanger(DepthMap, ((double)scanscreensize*guiImageWidth/width + (double)scanscreensize*guiImageHeight/height)/2).getHeight());
            //BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
            BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));

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
    public boolean Compare(CompareMethod method, byte[][][] part1, byte[][][] part2, boolean capprx) {
        if (capprx) {
            //int size = Math.min(tempmatrix1.length, tempmatrix1[0].length) / 20;
            int size = 1;
            int[][] kernel = GenerateGKernel(1, size);
            return method.DoMagic(Convolve(part1, kernel), Convolve(part2, kernel));
        }
        else
            return method.DoMagic(part1, part2);

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
    public double[] getDeviation(byte[][][] matrix1, byte[][][] matrix2, boolean use_approx, boolean verbose) {
        int width = matrix1.length;
        int height = matrix1[0].length;
        double best_correlation = 0;
        int opt_deviation = width/5;
        double area = width/3.5;
        int stripe = Math.max((int)area/75, 1);
        byte[][][] temp_matrix1, temp_matrix2, best_matrix1 = null, best_matrix2 = null;

        //int n_rnd = width/5;
        //int size = width/20;
        int n_rnd = width / 18;
        int size = width / 16;

        int ls, rs;
        ls = (int) (-area / 2);
        rs = (int) (area / 2);

        double[][] logs = new double[(rs-ls)/stripe][2];

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
                    double temp = NCC.get_similarity(rbatch1, rbatch2);
                    if(!Double.isNaN(temp)) {
                        correlation += temp;
                        counter++;
                    }
                }
                correlation /= counter;
            }
            else {
                correlation = NCC.get_similarity(temp_matrix1, temp_matrix2);
            }
            if (correlation > best_correlation) {
                best_matrix1 = MCopy(temp_matrix1);
                best_matrix2 = MCopy(temp_matrix2);
                best_correlation = correlation;
                opt_deviation = deviation;
            }
            logs[(deviation - ls)/stripe] = new double[]{deviation, 100*correlation};
            System.out.println(" " + correlation +" "+ deviation);
        }
        if (verbose){
            pf = new PlotFrame(MainFrame.this, MatrixToImage(best_matrix1), MatrixToImage(best_matrix2), opt_deviation, best_correlation, logs);
        }
        // writing to file
//        try{
//            counter4saving = 0;
//            File outputfile;
//            do {
//                counter4saving++;
//                outputfile = new File("Maps\\DepthMap" + counter4saving + ".png");
//            } while (outputfile.exists());
//            BufferedWriter writer = new BufferedWriter(new FileWriter("Correlations\\Correlation_data" + counter4saving + ".txt"));
//            for (int i = 0; i < logs.length; i++) {
//                writer.append(logs[i][0] +","+logs[i][1] + "\n");
//            }
//            writer.close();
//        }catch (Exception e){
//
//        }
        return new double[]{opt_deviation, best_correlation};
    }
    public double[][] getDeviations(byte[][][] matrix1, byte[][][] matrix2, int n_segments) {
        int width = matrix1.length;
        int height = matrix1[0].length;
        double best_correlation = 0;
        int opt_deviation = width/5;
        double area = width/3.5;
        int stripe = Math.max((int)area/75, 1);
        byte[][][] temp_matrix1, temp_matrix2;

        int ls, rs;
        ls = (int) (-area / 2);
        rs = (int) (area / 2);

        double[][] d_matrix = new double[n_segments][n_segments];
        double[][] c_matrix = new double[n_segments][n_segments];
        for (int i = 0; i < n_segments; i++) {
            for (int j = 0; j < n_segments; j++) {
                c_matrix[i][j] = 0;
            }
        }
        double correlation;
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
                    correlation = NCC.get_similarity(temp_matrix1, temp_matrix2);
                    if (correlation > c_matrix[i][j]) {
                        c_matrix[i][j] = correlation;
                        d_matrix[i][j] = deviation;
                    }
                }
            }
        }

        double avg_d = 0;
        double avg_c = 0;
        for (int i = 0; i < n_segments; i++) {
            for (int j = 0; j < n_segments; j++) {
                avg_d += d_matrix[i][j];
                avg_c += c_matrix[i][j];
            }
        }
        avg_d = avg_d / (n_segments*n_segments);
        avg_c = avg_c / (n_segments*n_segments);
        System.out.println("AVG "+avg_d + " "+ avg_c);
        for (int i = 0; i < n_segments; i++) {
            for (int j = 0; j < n_segments; j++) {
                if (Math.abs(Math.signum(avg_d) - Math.signum(d_matrix[i][j])) >= 1){
                    d_matrix[i][j] = avg_d;
                    c_matrix[i][j] = avg_c;
                }
                if (Math.abs(avg_d)/3 > Math.abs(d_matrix[i][j])){
                    d_matrix[i][j] = avg_d/3;
                    c_matrix[i][j] = avg_c;
                }
                if (Math.abs(avg_d)*3 < Math.abs(d_matrix[i][j])){
                    d_matrix[i][j] = avg_d*3;
                    c_matrix[i][j] = avg_c;
                }
            }
        }
        for (int i = 0; i < n_segments; i++) {
            for (int j = 0; j < n_segments; j++) {
                avg_d += d_matrix[i][j];
                avg_c += c_matrix[i][j];
            }
        }
        avg_d = avg_d / (n_segments*n_segments);
        avg_c = avg_c / (n_segments*n_segments);
        System.out.println("AVG "+avg_d + " "+ avg_c);

        for (int i = 0; i < n_segments; i++) {
            for (int j = 0; j < n_segments; j++) {
                d_matrix[i][j] /= c_matrix[i][j];
            }
            System.out.println();
        }

        for (int i = 0; i < n_segments; i++) {
            for (int j = 0; j < n_segments; j++) {
                System.out.print((int)Math.abs(d_matrix[i][j]) + " ");
            }
            System.out.println();
        }
        System.out.println();
        for (int i = 0; i < n_segments; i++) {
            for (int j = 0; j < n_segments; j++) {
                System.out.print(c_matrix[i][j] + " ");
            }
            System.out.println();
        }
        BufferedImage DevsImage = MatrixToImage(d_matrix);
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
        return d_matrix;
    }
    public double[] getMapMetrics(int[][][] matrix1, int[][][] matrix2, boolean use_approx) {
        // matrix2 is our map and is smaller
        int width = matrix2[0].length;

        int height = matrix2.length;
        double best_correlation = 0;
        int opt_deviation = width / 5;
        int[][][] temp_matrix1, temp_matrix2;

        int n_rnd = width / 15;
        int size = width / 15;

        double area = width/3.5;
        int stripe = Math.max((int)area/75, 1);
        System.out.println("Metrics calculation");
        for (int deviation = 0; deviation <= matrix1[0].length - matrix2[0].length; deviation += stripe) {
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
                    double temp = SCC.get_similarity(rbatch1, rbatch2);
                    //double temp = improc.PSNR(rbatch1, rbatch2);
                    if (!Double.isNaN(temp)) {
                        correlation += temp;
                        counter++;
                    }
                }
                correlation /= counter;
            } else {
                correlation = SCC.get_similarity(temp_matrix1, temp_matrix2);
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
                //System.out.println("********** " + ((int)Math.ceil((double)(i+1)/scanscreensize) - 1) + " " + ((int)Math.ceil((double)(j+1)/scanscreensize) - 1) + " " + i + " " + j);
                tempmap[i][j] = map[(int)Math.ceil((double)(j+1)/scanscreensize) - 1][(int)Math.ceil((double)(i+1)/scanscreensize) - 1];
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
    public BufferedImage CalculateDeepMap(int scanscreensize, int userchoise) {
        int width = matrix1.length; // 500
        int height = matrix1[0].length; // 400

        double[] devInfo = getDeviation(matrix1, matrix2, use_opt, VerboseCB.isSelected());
        int opt_deviation = (int) (devInfo[0]);
        double EC = Double.parseDouble(ECoefTF.getText());
        double light_coef = EC / devInfo[1];
        this.max_deviation = (int) (opt_deviation * light_coef);
        int corrected_width = (width - Math.abs(opt_deviation));
        System.out.println("\nOPTIMAL DEVIATION: " + Integer.toString(opt_deviation) + " pixels");
        System.out.println("\nMAX DEVIATION: " + Integer.toString(max_deviation) + " pixels");
        //max_deviation = -100;
        System.out.println("\nMatrix size: " + (int) Math.ceil((double) corrected_width / scanscreensize) + ' ' + (int) Math.ceil((double) height / scanscreensize) + " pixels");
        matrix3 = new double[(int) Math.ceil((double) corrected_width / scanscreensize)][(int) Math.ceil((double) height / scanscreensize)]; //матрица смещений
        double[][] m3_upd = new double[corrected_width][height];
        logs = new int[(int) Math.ceil((double) corrected_width / scanscreensize)][(int) Math.ceil((double) height / scanscreensize)][];
        //процесс нахождение точно значения пикселя первой матрицы во второй
        CompareMethod method = switch (userchoise) {
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
        double starttotal = 0.0;
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
        int locale_c = Integer.parseInt(NSegmentsTF.getText());
        int locale_w = width / locale_c;
        int locale_h = height / locale_c;
        double[][] thresh_matrix = new double[locale_c][locale_c];

        //double AC = Double.parseDouble(ACoefTF.getText());
        double AC = 2.15;



        int w = (int) (2.6*Math.sqrt(Math.abs(max_deviation)));
        System.out.println("W: " + w);
        double c_thresh = 0.85;

        if (AdaptiveSize.isSelected()) {
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
        //double[][] deviations = getDeviations(matrix1,matrix2,locale_c);
        //double [][] thresh_matrix = new double[(int)Math.ceil((double)corrected_width / scanscreensize)][(int)Math.ceil((double)height / scanscreensize)];
        double[][] tm_upd = new double[corrected_width][height];
        //System.out.println("START STD THRESH: " + std_thresh);
        for (int col_image1 = -Math.min(opt_deviation, 0); col_image1 < width - Math.max(opt_deviation, 0) - 1; col_image1 += scanscreensize) {
            sc_width = scanscreensize - Math.max((col_image1 + scanscreensize) - (width - Math.max(opt_deviation, 0)) + 1, 0);
            //System.out.println("###1#### " + col_image1 + ' '+ width +' '+sc_width);
            for (int row_image1 = 0; row_image1 < height; row_image1 += scanscreensize) {
                comp_counter = 0;
                sc_height = scanscreensize - Math.max((row_image1 + scanscreensize) - height + 1, 0);
                //System.out.println("###2#### " + row_image1 + ' '+ height +' '+sc_height);
                tempsizeadd = 0;
                //System.out.println("!!!!!!!!!!!!!!!!!!!" + col_image1 + " "+ row_image1 + " "+width+"");
                starttotal  = 0;
                tempmatrix1 = getPart(matrix1, col_image1, row_image1, sc_width, sc_height, tempsizeadd);
                std1 = Std(tempmatrix1);
//                if ((col_image1 % locale_w < sc_width) && (row_image1 % locale_h < sc_height)) {
//                    System.out.println("IS THE START OF AREA: " + col_image1 + " " + row_image1);
//                    std_thresh = improc.Std(getPart(matrix1, col_image1, row_image1, locale_w - sc_width, locale_h - sc_height, 0)) / 6;
//                    System.out.println("NEW THRESH: " + std_thresh);
//                }
                //System.out.println(col_image1 + " " + row_image1 + " " +  col_image1/locale_w + " " + row_image1/locale_h);
                std_thresh = thresh_matrix[(Math.min(col_image1/locale_w, locale_c-1))][Math.min((row_image1/locale_h), locale_c-1)];

                //max_deviation = (int) (EC * 0.5 * deviations[(Math.min(col_image1/locale_w, locale_c-1))][Math.min((row_image1/locale_h), locale_c-1)]);
                //std_thresh = (std_thresh + std1/10)/1.1;
                //System.out.println("NEW STD THRESH: " + std_thresh);


                if(AdaptiveSize.isSelected() && std1 < std_thresh) {
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
                method.setStartTotal(starttotal);
                coincidentx = col_image1;
                coincidenty = row_image1;
                //System.out.println("Top: " + Math.max(tempsizeadd,row_image1-vdev) + " Bottom:" + (Math.min(height- sc_height - tempsizeadd + 1,row_image1 + vdev + 1)));
                //System.out.println("Left: " + tempsizeadd + " Right:" + (width - tempsizeadd - sc_width));
                //System.out.println("TEMPSIZEADD: " + tempsizeadd);



                int peak_b = 0;
                int peak_f = 0;

                //System.out.println(Math.min(Math.abs(max_deviation), width - sc_width - col_image1 - tempsizeadd));
                for (int deviation = 0; (col_image1 - deviation) >= tempsizeadd && deviation <= Math.abs(max_deviation); deviation++){
                //for (int col_image2 = tempsizeadd; col_image2 < width - sc_width; col_image2++) {
                    for(int row_image2 = Math.max(0,row_image1-vdev); row_image2 < Math.min(height-sc_height+1,row_image1 + vdev + 1); row_image2++) {
                        if (opt_deviation <= 0) {
                            int col_image2 = col_image1 - deviation;
                            //System.out.println(col_image1 + " " + col_image2);
                            //if ((col_image2 - col_image1) <= 0 && (col_image2 - col_image1) >= this.max_deviation) {
                            //System.out.println("XD " + col_image2 +' '+ row_image2 + ' ' + sc_width +' '+ sc_height);
                            tempmatrix2 = getPart(matrix2, col_image2, row_image2, sc_width, sc_height, tempsizeadd);
                            //На самом деле ограничение области поиска (гипотенуза) должно быть намного уже, но программа просто-напросто отказывается адекватно работать
                            if (Compare(method, tempmatrix1, tempmatrix2, ConvolutionApproximation.isSelected())) {
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
                        } else {
                            int col_image2 = col_image1 + deviation;
                            //if ((col_image2 - col_image1) >= 0 && (col_image2 - col_image1) <= this.max_deviation) {
                            //System.out.println("XD " + col_image2 +' '+ row_image2 + ' ' + sc_width +' '+ sc_height);
                            tempmatrix2 = getPart(matrix2, col_image2, row_image2, sc_width, sc_height, tempsizeadd);
                            //На самом деле ограничение области поиска (гипотенуза) должно быть намного уже, но программа просто-напросто отказывается адекватно работать
                            if (Compare(method, tempmatrix1, tempmatrix2, ConvolutionApproximation.isSelected())) {
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
                        }
                        if (use_opt && peak_b >= w && method.bestTotal > c_thresh)
                            break;
                        itercounter++;
                        comp_counter++;
                    }
                    if (use_opt && peak_b >= w && method.bestTotal > c_thresh){
                        break;
                    }
                }

                //hdprob += Math.abs(coincidenty - row_image1);
                double distance = Math.hypot(coincidentx - col_image1, coincidenty - row_image1);
                //System.out.println("DIST: " + distance);
                int[] eP1 = extendPart(col_image1, row_image1, sc_width, sc_height, tempsizeadd);
                int[] eP2 = extendPart(coincidentx, coincidenty, sc_width, sc_height, tempsizeadd);
                //System.out.println(tempsizeadd);
                //col_image1, row_image1, coincidentx, coincidenty, sc_width, sc_height, metrics, (int)distance, std1, std2
                logs[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ scanscreensize)][(int)Math.ceil((double)row_image1 / scanscreensize)] = new int[]{eP1[0], eP1[1], eP2[0], eP2[1], eP1[2], eP1[3], (int)(dtis*method.bestTotal),  (int)((dtis*distance)/Math.hypot(max_deviation, 2*vdev)), (int)(dtis*std1), (int)(dtis*std2), (int)(dtis*tempsizeadd/width), comp_counter};
                //System.out.println("&&&&& " + col_image1 + ' ' + sc_width);
                matrix3[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ scanscreensize)][(int)Math.ceil((double)row_image1 / scanscreensize)] = distance;
                //thresh_matrix[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ scanscreensize)][(int)Math.ceil((double)row_image1 / scanscreensize)] = std_thresh;
                for (int i = 0; i < sc_width; i++) {
                    for (int j = 0; j < sc_height; j++) {
                        //System.out.println(row_image1 +" "+ i+" "+col_image1 + " "+j);
                        m3_upd[col_image1 + Math.min(opt_deviation, 0) + i][row_image1 + j] = distance;
                        tm_upd[col_image1 + Math.min(opt_deviation, 0) + i][row_image1 + j] = std_thresh;
                    }
                }

            }
            //hdprob /= (corrected_width / scanscreensize * height / scanscreensize*eps);
            //System.out.println("HDPROB: " + hdprob);
        }

        itercounter = (int)((double) itercounter/(((int) Math.ceil((double) corrected_width / scanscreensize) * (int) Math.ceil((double) height / scanscreensize))));
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

        // После этого нужно выбрать нужные данные и нажать на кнопку снизу менюшки, потом подождать, пока кнопка не станет вновь доступна и снова растянуть/сжать окно
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
         /*   if (min > matrix3[i][j]){
                min=matrix3[i][j];
            }*/
            }
        BufferedImage Result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)//максимальное количество пикселей в строке
            {
                //System.out.print((int)matrix[i][j]+" ");
                Color MyColor = new Color((int) ((matrix[i][j] / matrix[i_max][j_max]) * 255), (int) (matrix[i][j] / matrix[i_max][j_max] * 255), (int) (matrix[i][j] / matrix[i_max][j_max] * 255));
                Result.setRGB(i, j, MyColor.getRGB()); //установка цвета
                // Рисование закрашенного прямоугольника с началом координам x=i*w, y=j*w. Ширина и длина w.
            }
            //System.out.println();
        }
        return Result;
    }
}