
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
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
    public static double get_similarity(int[][][] scanim1, int[][][] scanim2){return 0;}
    public abstract boolean DoMagic(int[][][] scanim1, int[][][] scanim2);
    public void setStartTotal(Double startTotal){
        this.bestTotal = startTotal;
    }
}

class NCC extends CompareMethod {

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

    @Override
    public boolean DoMagic(int[][][] scanim1, int[][][] scanim2) {
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
    public static int[] arrayRankTransform(int[] arr) {
        int N = arr.length;
        //create result array and re-use it to store sorted elements of original array
        int[] sorted = Arrays.copyOf(arr, N);
        int[] ranks = Arrays.copyOf(arr, N);
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
    public static double get_similarity(int[][][] scanim1, int[][][] scanim2){
        int width = scanim1.length;
        int height = scanim1[0].length;
        int N = width*height;
        int[] array1 = new int[N];
        int[] array2 = new int[N];
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
    @Override
    public boolean DoMagic(int[][][] scanim1, int[][][] scanim2) {
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
    public static int[] arrayRankTransform(int[] arr) {
        int N = arr.length;
        //create result array and re-use it to store sorted elements of original array
        int[] sorted = Arrays.copyOf(arr, N);
        int[] ranks = Arrays.copyOf(arr, N);
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

    public static double get_similarity(int[][][] scanim1, int[][][] scanim2) {
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

    @Override
    public boolean DoMagic(int[][][] scanim1, int[][][] scanim2) {
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

    public static double get_similarity(int[][][] scanim1, int[][][] scanim2) {
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
        return (total[0] + total[1] + total[2])/3;
    }

    @Override
    public boolean DoMagic(int[][][] scanim1, int[][][] scanim2) {
        double total_avg = get_similarity(scanim1, scanim2);
        if (total_avg < bestTotal) {//В данном случае также
            bestTotal = total_avg;
            //System.out.println("New besttotal " + besttotal);
            return true;
        } else
            return false;
    }
}

class SSD extends CompareMethod {

    public static double get_similarity(int[][][] scanim1, int[][][] scanim2) {
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
        return (total[0] + total[1] + total[2])/3;
    }

    @Override
    public boolean DoMagic(int[][][] scanim1, int[][][] scanim2) {
        double total_avg = get_similarity(scanim1, scanim2);
        if (total_avg < bestTotal/*Math.pow(average*Math.pow(0.2*size,2),2)*/) {//В данном случае также
            bestTotal = total_avg;
            //System.out.println("New besttotal " + besttotal);
            return true;
        } else
            return false;
    }
}


class MainFrame extends JFrame {
    //В данном разделе, как уже понятно из класса-предка будет нудное создание интерфейса
    //Который, к слову, получился совсем не так, как задумывался
    int guiImageWidth = 250;
    int guiImageHeight = 200;

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
    int[][][] matrix1;//first image
    int[][][] matrix2;//second image
    public double[][] matrix3;//deviation matrix
    public int[][][] logs;
    public LogsVisualizator lv;
    public PlotFrame pf;
    int width, height; //ширина и высота фотки
    public double[] average4ssdandsad = new double[3];//средняя "яркость" на элемент матриц, похже используется в паре методов
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
    JLabel VdevLabel = new JLabel("Max vertical deviation");
    JTextField VdevTF = new JTextField("0", 4);
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
    JButton LoadDM = new JButton("LoadDM");
    JButton GetMetrics = new JButton("GetM");
    JButton GetLogs = new JButton("GetL");
    JButton SelectLeftImage = new JButton("Load Left Image");
    JButton SelectRightImage = new JButton("Load Right Image");
    ButtonGroup methods = new ButtonGroup();
    ButtonGroup filtration = new ButtonGroup();
    JFileChooser loadleftimage = new JFileChooser();
    JFileChooser loadrightimage = new JFileChooser();
    JTextField UserSize = new JTextField("10", 4);
    Box zero = Box.createHorizontalBox();
    Box zero_first = Box.createHorizontalBox();
    Box first = Box.createHorizontalBox();
    Box second = Box.createHorizontalBox();
    Box third = Box.createHorizontalBox();
    Box third2 = Box.createHorizontalBox();
    Box fourth = Box.createHorizontalBox();
    Box fifth = Box.createHorizontalBox();
    Box sixth = Box.createHorizontalBox();
    Box firstvert = Box.createVerticalBox();
    Box deepmap = Box.createHorizontalBox();
    JCheckBox ApplyForDepthMap = new JCheckBox("Apply to depth map");
    JCheckBox AdaptiveSize = new JCheckBox("AS");
    JTextField Filtersize = new JTextField("3", 5);
    JPanel filtersize_panel = new JPanel();
    JLabel text = new JLabel("size");
    JButton ApplyFunction = new JButton("Apply Function");
    JComboBox Function = new JComboBox(new String[]{"wmedian","amedian","prewitt","sobel","median", "avg", "min", "max", "gamma", "clarity", "equalize"});
    ImageProcessor improc = new ImageProcessor();
    int[] size_adjustment = {0, 0};
    int filtersize = 9;
    private int max_deviation;
    private int vdev;
    private int counter4saving = 0;
    public static void main(String[] args) throws IOException {
        MainFrame fr = new MainFrame();


    }

    public MainFrame() throws IOException {

        ncc.setSelected(true);

        loadleftimage.setCurrentDirectory(new File("D:\\Images\\"));
        loadrightimage.setCurrentDirectory(new File("D:\\Images\\"));

        frame.setLayout(new BorderLayout());
        frame.setSize(600, 500); //размер фрейма
        frame.setTitle("Depth map by Kirill Kolesnikov, inspired by Oleg Kovalev & Mikalai Yatskou");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        UserSize.setMaximumSize(UserSize.getPreferredSize());
        VdevTF.setMaximumSize(UserSize.getPreferredSize());
        methods.add(ncc);
        methods.add(scc);
        methods.add(kcc);
        methods.add(sad);
        methods.add(ssd);
        filtration.add(Sobel);
        filtration.add(Previtt);
        filtration.add(none);
        zero.add(Box.createHorizontalGlue());
        zero.add(SelectLeftImage);
        zero.add(Box.createHorizontalGlue());
        zero.add(SelectRightImage);
        zero.add(Box.createHorizontalGlue());
        first.add(Box.createHorizontalGlue());
        first.add(Ncc);
        first.add(Box.createHorizontalGlue());
        first.add(ncc);
        first.add(Box.createHorizontalGlue());
        first.add(ApplyFunction);
        first.add(Box.createHorizontalGlue());
        second.add(Box.createHorizontalGlue());
        second.add(Sad);
        second.add(Box.createHorizontalGlue());
        second.add(sad);
        second.add(Box.createHorizontalGlue());
        second.add(Function);
        second.add(Box.createHorizontalGlue());
        third.add(Box.createHorizontalGlue());
        third.add(Ssd);
        third.add(Box.createHorizontalGlue());
        third.add(ssd);
        third.add(Box.createHorizontalGlue());
        third.add(ApplyForDepthMap);
        third.add(Box.createHorizontalGlue());
        third2.add(Box.createHorizontalGlue());
        third2.add(Scc);
        third2.add(Box.createHorizontalGlue());
        third2.add(scc);
        third2.add(Box.createHorizontalGlue());
        third2.add(AdaptiveSize);
        third2.add(Box.createHorizontalGlue());
        third2.add(text);
        third2.add(Box.createHorizontalGlue());
        filtersize_panel.add(Filtersize);
        filtersize_panel.setPreferredSize(new Dimension(30, 30));
        third2.add(filtersize_panel);
        third2.add(Box.createHorizontalGlue());
        fourth.add(Box.createHorizontalGlue());
        fourth.add(Kcc);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(kcc);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(VdevLabel);
        fourth.add(Box.createHorizontalGlue());
        fourth.add(VdevTF);
        fourth.add(Box.createHorizontalGlue());
        sixth.add(Box.createHorizontalGlue());
        sixth.add(GoMakeSomeMagic);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(LoadDM);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(GetMetrics);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(GetLogs);
        sixth.add(Box.createHorizontalGlue());//чуть позже вернемся к графическому интерфейсу, для начала нужно раздобыть немного информации
        LoadDM.setEnabled(true);
        GetMetrics.setEnabled(false);
        GetLogs.setEnabled(false);
        SelectLeftImage.addActionListener(actionEvent -> {
            try {
                File LI = null;
                int leftret = loadleftimage.showDialog(null, "Загрузите левое изображение формата jpg");
                if (leftret == JFileChooser.APPROVE_OPTION) {
                    LI = loadleftimage.getSelectedFile();
                }
                image1 = improc.SizeChangerLinear(ImageIO.read(LI), guiImageWidth*2, guiImageHeight*2);
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
                int rightret = loadrightimage.showDialog(null, "Загрузите правое изображение формата jpg");
                if (rightret == JFileChooser.APPROVE_OPTION) {
                    RI = loadrightimage.getSelectedFile();
                }
                image2 = improc.SizeChangerLinear(ImageIO.read(RI), guiImageWidth*2, guiImageHeight*2);
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
                    improc.setSize((int)Double.parseDouble(Filtersize.getText()));
                    if (!ApplyForDepthMap.isSelected()) {
                        switch (str) {
                            case "gamma":
                                improc.setSize(0);
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.applyFunction(2, Double.parseDouble(Filtersize.getText())));
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.applyFunction(2, Double.parseDouble(Filtersize.getText())));
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "min":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.OrderStatFiltration("min"));
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.OrderStatFiltration("min"));
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "max":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.OrderStatFiltration("max"));
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.OrderStatFiltration("max"));
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "median":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.OrderStatFiltration("median"));
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.OrderStatFiltration("median"));
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "avg":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.OrderStatFiltration("avg"));
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.OrderStatFiltration("avg"));
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "sobel":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.Sobel());
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.Sobel());
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "prewitt":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.Prewitt());
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.Prewitt());
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "clarity":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.Clarity());
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.Clarity());
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "equalize":
                                improc.loadFull(image1);
                                image1 = improc.ImageContrastIncrease();
                                improc.loadFull(image2);
                                image2 = improc.ImageContrastIncrease();
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "amedian":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.AdaptiveMedianFiltration());
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.AdaptiveMedianFiltration());
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                            case "wmedian":
                                improc.loadFull(image1);
                                image1 = ImageCopy(improc.WeightedMedian());
                                improc.loadFull(image2);
                                image2 = ImageCopy(improc.WeightedMedian());
                                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                                break;
                        }

                    }
                    else{
                        switch (str) {
                            case "gamma":
                                improc.setSize(0);
                                improc.loadFull(DepthMap);
                                //DepthMap = improc.ImageSubstitution(DepthMap, ImageCopy(improc.applyFunction(2, Double.parseDouble(Filtersize.getText()))), 1);
                                DepthMap = ImageCopy(improc.applyFunction(2, Double.parseDouble(Filtersize.getText())));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "min":
                                improc.loadFull(DepthMap);
                                DepthMap = ImageCopy(improc.OrderStatFiltration("min"));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "max":
                                improc.loadFull(DepthMap);
                                DepthMap = ImageCopy(improc.OrderStatFiltration("max"));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "median":
                                improc.loadFull(DepthMap);
                                DepthMap = ImageCopy(improc.OrderStatFiltration("median"));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "avg":
                                improc.loadFull(DepthMap);
                                DepthMap = ImageCopy(improc.OrderStatFiltration("avg"));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "sobel":
                                improc.loadFull(DepthMap);
                                DepthMap = ImageCopy(improc.Sobel());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "prewitt":
                                improc.loadFull(DepthMap);
                                DepthMap = ImageCopy(improc.Prewitt());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "clarity":
                                improc.loadFull(DepthMap);
                                DepthMap = ImageCopy(improc.Clarity());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "equalize":
                                improc.loadFull(DepthMap);
                                DepthMap = improc.ImageContrastIncrease();
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "amedian":
                                improc.loadFull(DepthMap);
                                DepthMap = ImageCopy(improc.AdaptiveMedianFiltration());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "wmedian":
                                improc.loadFull(DepthMap);
                                DepthMap = ImageCopy(improc.WeightedMedian());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                        }
                        DepthMap_full = MatrixToImage(getFullMap(improc.BWImageToMatrix(DepthMap), DepthMap_full.getWidth(), DepthMap_full.getHeight()));
                    }
                }
            }
        });

      /*  width = image1.getWidth() >= image2.getWidth() ? image2.getWidth() : image1.getWidth();
        height = image1.getHeight() >= image2.getHeight() ? image2.getHeight() : image1.getHeight();*/
        // System.out.println("Разрешение фоток " + width + " на " + height);

        //подсчет допустимых размеров сканирования, таких, чтобы изображение делилось без остатка
        fifth.add(Box.createHorizontalGlue());
        fifth.add(ScanScreenLabel);
        fifth.add(Box.createHorizontalGlue());
        fifth.add(UserSize);
        fifth.add(Box.createHorizontalGlue());
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(zero);
        firstvert.add(Box.createVerticalGlue());
        //firstvert.add(zero_first);
        //firstvert.add(Box.createVerticalGlue());
        firstvert.add(first);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(second);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(third);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(third2);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(fourth);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(fifth);
        firstvert.add(Box.createVerticalGlue());
        firstvert.add(sixth);
        firstvert.add(Box.createVerticalGlue());
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
        panel.add(firstvert);
        BottomImageLabel.setIcon(null);
        panel.add(deepmap);
        // panel.add(GradientOfColors);
        frame.add(panel);
        //frame.pack();
        frame.setVisible(true);

        LoadDM.addActionListener(actionEvent -> {
            try {
                int dmret = loadleftimage.showDialog(null, "Загрузите левое изображение формата jpg");
                File temp = null;
                if (dmret == JFileChooser.APPROVE_OPTION) {
                    temp = loadleftimage.getSelectedFile();
                }
                GetMetrics.setEnabled(true);
                buff = improc.SizeChangerLinear(ImageIO.read(temp), guiImageWidth*2, guiImageHeight*2);
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong while reading, try again");
            }
        });
        GetMetrics.addActionListener(actionEvent -> {
            double[] metrics = getMapMetrics(improc.ImageToMatrix(buff), improc.ImageToMatrix(DepthMap_full), true);
            DMComparator dmComparator = new DMComparator(this, buff, DepthMap_full, metrics[0], metrics[1]);
        });
        GetLogs.addActionListener(actionEvent -> {
            lv = new LogsVisualizator(this, matrix1, matrix2, logs, max_deviation, vdev);
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
            System.out.flush();
            matrix1 = new int[width][height][3]; //матрица для первого снимка
            matrix2 = new int[width][height][3]; //матрица для второго снимка
            //преобразование изображения в чб, конфликтует с некоторыми цветами
            if (bw.isSelected()) {
                improc.loadFull(image1);
                image1 = ImageCopy(improc.BW());
                improc.loadFull(image2);
                image2 = ImageCopy(improc.BW());
                // TopImageLabel.setIcon(new ImageIcon(image1));
                // panel.add(TopImageLabel, NORTH);
                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
            }
            //Средняя "яркость" будет позже использоваться в некоторых методах
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    // matrix1[i][j] = Math.abs(image1.getRGB(i, j));
                    matrix1[i][j][0] = Math.abs(new Color(image1.getRGB(i, j)).getRed());
                    matrix1[i][j][1] = Math.abs(new Color(image1.getRGB(i, j)).getGreen());
                    matrix1[i][j][2] = Math.abs(new Color(image1.getRGB(i, j)).getBlue());
                    //System.out.print(matrix1[i][j] + " ");
                    // matrix2[i][j] = Math.abs(image2.getRGB(i, j));
                    matrix2[i][j][0] = Math.abs(new Color(image2.getRGB(i, j)).getRed());
                    matrix2[i][j][1] = Math.abs(new Color(image2.getRGB(i, j)).getGreen());
                    matrix2[i][j][2] = Math.abs(new Color(image2.getRGB(i, j)).getBlue());
                    for (int k = 0; k < 3; k++){
                        average4ssdandsad[k] += matrix1[i][j][k] + matrix2[i][j][k];
                    }
                }
                //System.out.println();
            }
            //System.out.print("@@@@@@@@@" + matrix1[0][0][0]);
            for(int k = 0; k < 3; k++) {
                average4ssdandsad[k] /= width * height * 2;
            }

//            filtersize = Integer.parseInt(Filtersize.getText());
//
//            BufferedImage FiltratedImage1;
//            BufferedImage FiltratedImage2;
//            //фильтрация
//            if (Previtt.isSelected()) {
//                improc.setSize(filtersize);
//                improc.loadFull(image1);
//                FiltratedImage1 = ImageCopy(improc.Prewitt());
//                improc.loadFull(image2);
//                FiltratedImage2 = ImageCopy(improc.Prewitt());
//                //FiltratedImage1 = ichange.Filtration(matrix1, height, width, average4ssdandsad, 2);
//                //FiltratedImage2 = ichange.Filtration(matrix2, height, width, average4ssdandsad, 2);
//            } else {
//                if (Sobel.isSelected()) {
//                    improc.setSize(filtersize);
//                    improc.loadFull(image1);
//                    FiltratedImage1 = ImageCopy(improc.Sobel());
//                    improc.loadFull(image2);
//                    FiltratedImage2 = ImageCopy(improc.Sobel());
//                    //FiltratedImage1 = ichange.Filtration(matrix1, height, width, average4ssdandsad, 3);
//                    //FiltratedImage2 = ichange.Filtration(matrix2, height, width, average4ssdandsad, 3);
//                } else {
//                    FiltratedImage1 = image1;
//                    FiltratedImage2 = image2;
//                }
//            }
//            RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(FiltratedImage1, guiImageWidth, guiImageHeight)));
//            // panel.add(firstvert, CENTER);
//            LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(FiltratedImage2, guiImageWidth, guiImageHeight)));
//            // panel.add(RightImageLabel, WEST);
//            // panel.add(RightImageLabel, EAST);

            for (int k = 0; k < 3; k++) {
                average4ssdandsad[k] = 0;
            }
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    // matrix1[i][j] = Math.abs(image1.getRGB(i, j));
                    matrix1[i][j][0] = Math.abs(new Color(image1.getRGB(i, j)).getRed());
                    matrix1[i][j][1] = Math.abs(new Color(image1.getRGB(i, j)).getGreen());
                    matrix1[i][j][2] = Math.abs(new Color(image1.getRGB(i, j)).getBlue());
                    //System.out.print(matrix1[i][j] + " ");
                    // matrix2[i][j] = Math.abs(image2.getRGB(i, j));
                    matrix2[i][j][0] = Math.abs(new Color(image2.getRGB(i, j)).getRed());
                    matrix2[i][j][1] = Math.abs(new Color(image2.getRGB(i, j)).getGreen());
                    matrix2[i][j][2] = Math.abs(new Color(image2.getRGB(i, j)).getBlue());
                    for (int k = 0; k < 3; k++){
                        average4ssdandsad[k] += matrix1[i][j][k] + matrix2[i][j][k];
                    }
                }
            }
            for(int k = 0; k < 3; k++) {
                average4ssdandsad[k] /= width * height * 2;
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
            DepthMap_full = CalculateDeepMap(scanscreensize, userchoise);

            // Сохранение

            try {
                if (DepthMap == null) {
                    throw new IOException();
                }

                File outputfile;
                outputfile = new File("DepthMap" + counter4saving + ".png");
                ImageIO.write(DepthMap_full, "png", outputfile);
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
            System.out.println("!!!!!!!!!!!!! "+((double)scanscreensize*guiImageWidth/width) +" "+ DepthMap.getWidth() +" "+ DepthMap.getWidth()*scanscreensize*guiImageWidth/width);
            System.out.println("!!!!!!!!!!!!! "+(double)scanscreensize*guiImageHeight/height +" "+ DepthMap.getHeight() +" "+ DepthMap.getHeight()*scanscreensize*guiImageHeight/height);
            System.out.println(improc.SizeChanger(DepthMap, ((double)scanscreensize*guiImageWidth/width + (double)scanscreensize*guiImageHeight/height)/2).getWidth()+" "+improc.SizeChanger(DepthMap, ((double)scanscreensize*guiImageWidth/width + (double)scanscreensize*guiImageHeight/height)/2).getHeight());
            BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DepthMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
            //GradientOfColors.setIcon(new ImageIcon(improc.SizeChangerLinear(gradientstripe, guiImageWidth, guiImageHeight)));
            LoadDM.setEnabled(true);
            GetLogs.setEnabled(true);
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


    public BufferedImage ImageCopy(BufferedImage img) {
        BufferedImage temp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                temp.setRGB(i, j, img.getRGB(i, j));
            }
        }
        return temp;
    }

    public boolean Compare(CompareMethod method, int row1, int col1, int row2, int col2, int height, int width, int tempsizeadd) {
        //чтобы не таскать дальше эти большие фотки, создаем копии только нужных областей
        int[][][] tempmatrix1 = new int[width+2*tempsizeadd][height+2*tempsizeadd][3];
        int[][][] tempmatrix2 = new int[width+2*tempsizeadd][height+2*tempsizeadd][3];
        for (int i = 0; i < width+2*tempsizeadd; i++) {
            for (int j = 0; j < height+2*tempsizeadd; j++) {
                for (int k = 0; k < 3; k++) {
                    tempmatrix1[i][j][k] = matrix1[row1 + i - tempsizeadd][col1 + j - tempsizeadd][k];
                    tempmatrix2[i][j][k] = matrix2[row2 + i - tempsizeadd][col2 + j - tempsizeadd][k];
                }
            }
        }
        return method.DoMagic(tempmatrix1, tempmatrix2);
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
    public double[] getDeviation(int[][][] matrix1, int[][][] matrix2, boolean use_approx, boolean verbose) {
        int width = matrix1.length;
        int height = matrix1[0].length;
        double best_correlation = 0;
        int opt_deviation = width/5;
        double area = width/3.5;
        int stripe = Math.max((int)area/75, 1);
        int[][][] temp_matrix1, temp_matrix2, best_matrix1 = null, best_matrix2 = null;

        int n_rnd = width/2;
        int size = width/30;
        int ls, rs;
        ls = (int) (-area / 2);
        rs = (int) (area / 2);

        double[][] logs = new double[rs-ls][2];

        for (int deviation = ls; deviation < rs; deviation += stripe) {
            temp_matrix1 = new int[width - Math.abs(deviation)][height][3];
            temp_matrix2 = new int[width - Math.abs(deviation)][height][3];
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
                Random rand = new Random();
                for (int i = 0; i < n_rnd; i++){
                    int[][][] rbatch1 = new int[size][size][3];
                    int[][][] rbatch2 = new int[size][size][3];
                    int x_r = rand.nextInt(width - Math.abs(deviation) - size + 1);
                    int y_r = rand.nextInt(height - size + 1);
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
                        correlation += temp;
                        counter++;
                    }
                }
                correlation /= counter;
            }
            else {
                correlation = SCC.get_similarity(temp_matrix1, temp_matrix2);
            }
            if (correlation > best_correlation) {
                best_matrix1 = MCopy(temp_matrix1);
                best_matrix2 = MCopy(temp_matrix2);
                best_correlation = correlation;
                opt_deviation = deviation;
            }
            logs[deviation - ls] = new double[]{deviation, correlation*100};
            System.out.println(" " + correlation +" "+ deviation);
        }
        if (verbose){
            pf = new PlotFrame(MainFrame.this, improc.MatrixToImage(best_matrix1), improc.MatrixToImage(best_matrix2), opt_deviation, best_correlation, logs);
        }
        // writing to file
        try{
            counter4saving = 0;
            File outputfile;
            do {
                counter4saving++;
                outputfile = new File("DepthMap" + counter4saving + ".png");
            } while (outputfile.exists());
            BufferedWriter writer = new BufferedWriter(new FileWriter("Correlation_data" + counter4saving + ".txt"));
            for (int i = 0; i < logs.length; i++) {
                writer.append(logs[i][0] +","+logs[i][1] + "\n");
            }
            writer.close();
        }catch (IOException e){
            
        }
        return new double[]{opt_deviation, best_correlation};
    }
    public double[] getMapMetrics(int[][][] matrix1, int[][][] matrix2, boolean use_approx){
        // matrix2 is our map and is smaller
        int width = matrix2[0].length;

        int height = matrix2.length;
        double best_correlation = 0;
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
            double correlation = 0;
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
                        correlation += temp;
                        counter++;
                    }
                }
                correlation /= counter;
            }
            else {
                correlation = SCC.get_similarity(temp_matrix1, temp_matrix2);
            }
            if (correlation > best_correlation) {
                best_correlation = correlation;
                opt_deviation = deviation;
            }

            System.out.println(" " + correlation +" "+ deviation);
        }
        return new double[]{opt_deviation, best_correlation};
    }
    public double[][] getFullMap(int[][] map, int width, int height){
        double[][] tempmap = new double[width][height];
        for(int i=0; i < width; i++){
            for(int j=0; j < height; j++){
                System.out.println("********** " + ((int)Math.ceil((double)(i+1)/scanscreensize) - 1) + " " + ((int)Math.ceil((double)(j+1)/scanscreensize) - 1) + " " + i + " " + j);
                tempmap[i][j] = map[(int)Math.ceil((double)(j+1)/scanscreensize) - 1][(int)Math.ceil((double)(i+1)/scanscreensize) - 1];
            }
        }
        return tempmap;
    }
    public BufferedImage CalculateDeepMap(int scanscreensize, int userchoise) {
        int width = matrix1.length;
        int height = matrix1[0].length;
        double[] devInfo = getDeviation(matrix1, matrix2, true, true);
        int opt_deviation = (int)(devInfo[0]);
        double light_coef = 2/devInfo[1];
        this.max_deviation = (int)(opt_deviation * light_coef);
        int corrected_width = (width - Math.abs(opt_deviation));
        System.out.println("\nOPTIMAL DEVIATION: " + Integer.toString(opt_deviation) + " pixels");
        System.out.println("\nMatrix size: " + (int)Math.ceil((double)corrected_width / scanscreensize) + ' ' + (int)Math.ceil((double)height / scanscreensize) + " pixels");
        matrix3 = new double[(int)Math.ceil((double)corrected_width / scanscreensize)][(int)Math.ceil((double)height / scanscreensize)]; //матрица смещений
        double[][] m3_upd = new double[corrected_width][height];
        logs = new int[(int)Math.ceil((double)corrected_width / scanscreensize)][(int)Math.ceil((double)height / scanscreensize)][8];
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
        this.vdev = Integer.valueOf(VdevTF.getText());
        int tempsizeadd;

        int sc_height, sc_width;
        for (int col_image1 = -Math.min(opt_deviation, 0); col_image1 < width - Math.max(opt_deviation, 0) - 1; col_image1 += scanscreensize) {
            sc_width = scanscreensize - Math.max((col_image1 + scanscreensize) - (width - Math.max(opt_deviation, 0)) + 1, 0);
            System.out.println("###1#### " + col_image1 + ' '+ width +' '+sc_width);
            for (int row_image1 = 0; row_image1 < height; row_image1 += scanscreensize) {
                sc_height = scanscreensize - Math.max((row_image1 + scanscreensize) - height + 1, 0);
                System.out.println("###2#### " + row_image1 + ' '+ height +' '+sc_height);
                tempsizeadd = 0;
                //System.out.println("!!!!!!!!!!!!!!!!!!!" + col_image1 + " "+ row_image1 + " "+width+"");
                switch (userchoise) {
                    case 1, 2, 3 -> starttotal = 0.0;
                    case 4 -> starttotal = 255 * Math.pow(scanscreensize, 2);
                    case 5 -> starttotal = Math.pow(255, 2) * Math.pow(scanscreensize, 2);
                }
                int[][][] tempmatrix1 = new int[sc_width][sc_height][3];
                for (int i = 0; i < sc_width; i++) {
                    for (int j = 0; j < sc_height; j++) {
                        for (int k = 0; k < 3; k++) {
                            //System.out.println(row_image1 +" "+ i+" "+col_image1 + " "+j);
                            tempmatrix1[i][j][k] = matrix1[col_image1 + i][row_image1 + j][k];
                        }
                    }
                }
                if(AdaptiveSize.isSelected()) {
                    int counter = 0;
                    System.out.println("STD" + improc.Std(tempmatrix1));
                    while (improc.Std(tempmatrix1) < 0.3 && (col_image1 - 2 * tempsizeadd) > 0 && (width - scanscreensize - 2 * tempsizeadd - col_image1) > 0 && (row_image1 - 2 * tempsizeadd) > 0 && (height - scanscreensize - 2 * tempsizeadd - row_image1) > 0) {
                        counter += 1;
                        tempsizeadd += 2*counter;
                        tempmatrix1 = new int[scanscreensize + 2 * tempsizeadd][scanscreensize + 2 * tempsizeadd][3];
                        for (int i = 0; i < scanscreensize + 2 * tempsizeadd; i++) {
                            for (int j = 0; j < scanscreensize + 2 * tempsizeadd; j++) {
                                for (int k = 0; k < 3; k++) {
                                    //System.out.println(Double.toString((col_image1 - 2*tempsizeadd))+" "+Double.toString((width-scanscreensize-2*tempsizeadd-col_image1))+" "+Double.toString(row_image1 - 2*tempsizeadd)+ " " +Double.toString(height-scanscreensize-2*tempsizeadd-row_image1)+" "+improc.Std(tempmatrix1));
                                    tempmatrix1[i][j][k] = matrix1[col_image1 + i - tempsizeadd][row_image1 + j - tempsizeadd][k];
                                }
                            }
                        }
                        System.out.println("&&&&&&&&&&&&&&&&& " + tempsizeadd + " " + col_image1 + " " + row_image1);
                    }
                }
                method.setStartTotal(starttotal);
                coincidentx = col_image1;
                coincidenty = row_image1;
                for(int row_image2 = Math.max(0,row_image1-vdev); row_image2 < Math.min(height-sc_height+1,row_image1 + vdev + 1); row_image2++) {
                    for (int col_image2 = tempsizeadd; col_image2 < width - tempsizeadd - sc_width + 1; col_image2++) {
                        if (opt_deviation <= 0) {
                            if ((col_image2 - col_image1) <= 0 && (col_image2 - col_image1) >= this.max_deviation) {
                                //На самом деле ограничение области поиска (гипотенуза) должно быть намного уже, но программа просто-напросто отказывается адекватно работать
                                if (Compare(method, col_image1, row_image1, col_image2, row_image2, sc_height, sc_width, tempsizeadd)) {
                                    coincidentx = col_image2;
                                    coincidenty = row_image2;
                                    System.out.print("New Best: " + method.bestTotal + " ");
                                }
                            }
                        } else {
                            if ((col_image2 - col_image1) >= 0 && (col_image2 - col_image1) <= this.max_deviation) {
                                //На самом деле ограничение области поиска (гипотенуза) должно быть намного уже, но программа просто-напросто отказывается адекватно работать
                                if (Compare(method, col_image1, row_image1, col_image2, row_image2, sc_height, sc_width, tempsizeadd)) {
                                    coincidentx = col_image2;
                                    coincidenty = row_image2;
                                    System.out.print("New Best: " + method.bestTotal + " ");
                                }
                            }
                        }
                    }
                }
                //hdprob += Math.abs(coincidenty - row_image1);
                //
                double distance = Math.hypot(coincidentx - col_image1, coincidenty - row_image1);
                logs[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ scanscreensize)][(int)Math.ceil((double)row_image1 / scanscreensize)] = new int[]{col_image1, row_image1, coincidentx, coincidenty, (int)distance, sc_width, sc_height, (int)(10000*method.bestTotal)};
                System.out.println("&&&&& " + col_image1 + ' ' + sc_width);
                matrix3[(int)Math.ceil((double)(col_image1 + Math.min(opt_deviation, 0))/ scanscreensize)][(int)Math.ceil((double)row_image1 / scanscreensize)] = distance;
                for (int i = 0; i < sc_width; i++) {
                    for (int j = 0; j < sc_height; j++) {
                            //System.out.println(row_image1 +" "+ i+" "+col_image1 + " "+j);
                            m3_upd[col_image1 + Math.min(opt_deviation, 0) + i][row_image1 + j] = distance;
                    }
                }

            }
            //hdprob /= (corrected_width / scanscreensize * height / scanscreensize*eps);
            //System.out.println("HDPROB: " + hdprob);
        }

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
                System.out.print((int)matrix[i][j]+" ");
                Color MyColor = new Color((int) ((matrix[i][j] / matrix[i_max][j_max]) * 255), (int) (matrix[i][j] / matrix[i_max][j_max] * 255), (int) (matrix[i][j] / matrix[i_max][j_max] * 255));
                Result.setRGB(i, j, MyColor.getRGB()); //установка цвета
                // Рисование закрашенного прямоугольника с началом координам x=i*w, y=j*w. Ширина и длина w.
            }
            System.out.println();
        }
        return Result;
    }
}