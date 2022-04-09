import com.sun.source.tree.WhileLoopTree;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

class ImageChanger {
    //Уменьшает изображение в натуральное количество раз
    public BufferedImage SizeDecreaser(BufferedImage source, int decreasingcoefficient) {

        if (source.getWidth() / decreasingcoefficient == (int) (source.getWidth() / decreasingcoefficient) && source.getHeight() / decreasingcoefficient == (int) (source.getHeight() / decreasingcoefficient)) {
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

    //Изменяет размер изображения, при этом не обязательно в натуральное количество раз
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
            for (int i = 2; i <= (numerator > denominator ? denominator : numerator); i++) {
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
            case 1: //Roberts
                mask = new int[][]{{1, 0}, {0, -1}};
                size = 2;
                limit = average * 0.15; // Пределы подрибались исключительно вручную
                break;
            case 2: //Previtt
                mask = new int[][]{{-1, -1, -1}, {0, 0, 0}, {1, 1, 1}};
                limit = average * 0.42; // Для большей точности, конечно, лучше, чтобы функцию подбирала машина (можно сделать с помощью машинного обучения и т.п)
                break;
            case 3: //Sobel
                mask = new int[][]{{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
                limit = average * 0.42; // Это скорее любительская lite-версия
                break;
            default:
                mask = new int[][]{{0, 0, 0}, {0, 1, 0}, {0, 0, 0}};
                break;
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

class CompareMethod {
    protected Double bestTotal;
    //Суть методов установления соотвествия довольно похожа, различаются в основном математические формулы и их смысл (в следствие чего и разные предельные функции)
    public boolean DoMagic(int[][][] scanim1, int[][][] scanim2, int scanscreensize, double[] average) {
        return true;
    }
    public void setStartTotal(Double startTotal){
        this.bestTotal = startTotal;
    }
}

class NCC extends CompareMethod {

    @Override
    public boolean DoMagic(int[][][] scanim1, int[][][] scanim2, int scanscreensize, double[] average) {
        double[] averageim1 = {0, 0, 0};
        double[] averageim2 = {0, 0, 0};
        double numerator;
        double denominator;
        double temp;
        double[] total = {0, 0, 0};
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < scanscreensize; i++) {
                for (int j = 0; j < scanscreensize; j++) {
                    averageim1[k] += scanim1[i][j][k];
                    averageim2[k] += scanim2[i][j][k];
                }
            }
            averageim1[k] /= Math.pow(scanscreensize, 2);
            averageim2[k] /= Math.pow(scanscreensize, 2);
            numerator = 0;
            denominator = 0;
            temp = 0;
            for (int i = 0; i < scanscreensize; i++) {
                for (int j = 0; j < scanscreensize; j++) {
                    numerator += (scanim1[i][j][k] - averageim1[k]) * (scanim2[i][j][k] - averageim2[k]);
                    denominator += Math.pow((scanim1[i][j][k] - averageim1[k]), 2);
                    temp += Math.pow((scanim2[i][j][k] - averageim2[k]), 2);
                }
            }
            denominator = Math.sqrt(denominator) * Math.sqrt(temp);

            total[k] = numerator / denominator;
        }
        double total_avg = (total[0] + total[1] + total[2])/3;
        //предел как всегда подбирался вручную
        if (total_avg > bestTotal) {//чем больше total, тем более схожи части изображений, но не стоит ставить слишком высокий, иначе просто не найдет совпадений
            bestTotal = total_avg;
            //System.out.println("New besttotal " + besttotal);
            return true;
        } else
            return false;
    }
}

class SAD extends CompareMethod {
    @Override
    public boolean DoMagic(int[][][] scanim1, int[][][] scanim2, int scanscreensize, double[] average) {
        double[] total = {0, 0, 0};
        for (int i = 0; i < scanscreensize; i++) {
            for (int j = 0; j < scanscreensize; j++) {
                for (int k = 0; k < 3; k++) {
                    total[k] += Math.abs(scanim1[i][j][k] - scanim2[i][j][k]);
                }
            }
        }
        double total_avg = (total[0] + total[1] + total[2])/3;
        if (total_avg < bestTotal/*Math.pow(average*Math.pow(0.2*scanscreensize,2),2)*/) {//В данном случае также
            bestTotal = total_avg;
            //System.out.println("New besttotal " + besttotal);
            return true;
        } else
            return false;
    }
}

class SSD extends CompareMethod {
    @Override
    public boolean DoMagic(int[][][] scanim1, int[][][] scanim2, int scanscreensize, double[] average) {
        double[] total = {0, 0, 0};
        for (int i = 0; i < scanscreensize; i++) {
            for (int j = 0; j < scanscreensize; j++) {
                for (int k = 0; k < 3; k++) {
                    total[k] += Math.pow(scanim1[i][j][k] - scanim2[i][j][k], 2);
                }
            }
        }
        double total_avg = (total[0] + total[1] + total[2])/3;
        if (total_avg < bestTotal/*Math.pow(average*Math.pow(0.2*scanscreensize,2),2)*/) {//В данном случае также
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
    File LI = null;
    //new File("D:\\Images\\left.jpg");
    File RI = null;
    // new File("D:\\Images\\right.jpg");
    BufferedImage image1 = null;
    BufferedImage image2 = null;
    BufferedImage gradientstripe;
    BufferedImage buff;
    BufferedImage DeepMap;
    int scanscreensize;
    int matrix1[][][];//first image
    int matrix2[][][];//second image
    public double matrix3[][];//disperyty`s matrix
    int width, height; //ширина и высота фотки
    public double[] average4ssdandsad = new double[3];//средняя "яркость" на элемент матриц, похже используется в паре методов
    JFrame frame = new JFrame();
    JPanel panel = new JPanel();
    JLabel NCC = new JLabel("NCC");
    JLabel SAD = new JLabel("SAD");
    JLabel SSD = new JLabel("SSD");
    JLabel BW = new JLabel("BW");
    JLabel SOBEL = new JLabel("SOBEL");
    JLabel PREVITT = new JLabel("PREVITT");
    JLabel NONE = new JLabel("NONE");
    JLabel ScanScreen = new JLabel("Write the size of a scanning screen");
    JLabel Selections = new JLabel("");
    //JLabel TopImageLabel = new JLabel("Main image");
    JLabel LeftImageLabel = new JLabel("Left image");
    JLabel RightImageLabel = new JLabel("Right image");
    JLabel BottomImageLabel = new JLabel("Deep map");
    JLabel GradientOfColors = new JLabel();
    JRadioButton ncc = new JRadioButton("", false);
    JRadioButton sad = new JRadioButton("", false);
    JRadioButton ssd = new JRadioButton("", false);
    JCheckBox bw = new JCheckBox("BW", false);
    JRadioButton Sobel = new JRadioButton("", false);
    JRadioButton Previtt = new JRadioButton("", false);
    JRadioButton none = new JRadioButton("", true);
    JButton GoMakeSomeMagic = new JButton("Go");
    JButton Buff = new JButton("Buff");
    JButton GetMetrics = new JButton("GetStat");
    JButton SelectLeftImage = new JButton("Load Left Image");
    JButton SelectRightImage = new JButton("Load Right Image");
    ButtonGroup methods = new ButtonGroup();
    ButtonGroup filtration = new ButtonGroup();
    JFileChooser loadleftimage = new JFileChooser();
    JFileChooser loadrightimage = new JFileChooser();
    JTextField UserSize = new JTextField("", 4);
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

    public static void main(String[] args) throws IOException {
        MainFrame fr = new MainFrame();

    }

    public MainFrame() throws IOException {

//http://software-testing.ru/forum/index.php?/topic/19084-sravnenie-dvuh-izobrazhenii/
//http://sbp-program.ru/java/sbp-bufferedimage.htm
        loadleftimage.setCurrentDirectory(new File("D:\\Images\\"));
        loadrightimage.setCurrentDirectory(new File("D:\\Images\\"));

        frame.setLayout(new BorderLayout());
        frame.setSize(650, 500); //размер фрейма
        frame.setTitle("Depth map by Kirill Kolesnikov, inspired and supported by Oleg Kovalev");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        UserSize.setMaximumSize(UserSize.getPreferredSize());
        methods.add(ncc);
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
        first.add(NCC);
        first.add(Box.createHorizontalGlue());
        first.add(ncc);
        first.add(Box.createHorizontalGlue());
        first.add(ApplyFunction);
        first.add(Box.createHorizontalGlue());
        second.add(Box.createHorizontalGlue());
        second.add(SAD);
        second.add(Box.createHorizontalGlue());
        second.add(sad);
        second.add(Box.createHorizontalGlue());
        second.add(Function);
        second.add(Box.createHorizontalGlue());
        third.add(Box.createHorizontalGlue());
        third.add(SSD);
        third.add(Box.createHorizontalGlue());
        third.add(ssd);
        third.add(Box.createHorizontalGlue());
        third.add(ApplyForDepthMap);
        third.add(Box.createHorizontalGlue());
        third2.add(Box.createHorizontalGlue());
        third2.add(bw);
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
        fourth.add(ScanScreen);
        fourth.add(Box.createHorizontalGlue());
        sixth.add(Box.createHorizontalGlue());
        sixth.add(GoMakeSomeMagic);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(Buff);
        sixth.add(Box.createHorizontalGlue());
        sixth.add(GetMetrics);
        sixth.add(Box.createHorizontalGlue());//чуть позже вернемся к графическому интерфейсу, для начала нужно раздобыть немного информации
        Buff.setEnabled(false);
        GetMetrics.setEnabled(false);
        SelectLeftImage.addActionListener(actionEvent -> {
            try {
                if (LI == null || RI == null) {
                    int leftret = loadleftimage.showDialog(null, "Загрузите левое изображение формата jpg");
                    if (leftret == JFileChooser.APPROVE_OPTION) {
                        LI = loadleftimage.getSelectedFile();
                    }
                }
                image1 = ImageIO.read(LI);
                image1 = improc.SizeChangerLinear(image1, 384, 288);
                LI = null;
                width = image1.getWidth();
                height = image1.getHeight();
                String SIZES = "";
                for (Integer i = 1; i < Math.min(height,width) / 2; i++) {
                    if (height % i == 0 && width % i == 0) {
                        System.out.print(i + " ");
                        if (i == 1)
                            SIZES += i.toString();
                        else
                            SIZES += "/" + i.toString();
                    }
                }
                Selections.setText(SIZES);
                frame.getContentPane();
                //TopImageLabel.setIcon(new ImageIcon(ichange.SizeChanger(image1,2,3)));
                //panel.add(TopImageLabel, NORTH);
                LeftImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image1, guiImageWidth, guiImageHeight)));
                //panel.add(LeftImageLabel, WEST);
                frame.setVisible(true);
            } catch (Exception e) {
            }
        });
        SelectRightImage.addActionListener(actionEvent -> {
            try {
                if (LI == null || RI == null) {
                    int rightret = loadrightimage.showDialog(null, "Загрузите правое изображение формата jpg");
                    if (rightret == JFileChooser.APPROVE_OPTION) {
                        RI = loadrightimage.getSelectedFile();
                    }
                }
                image2 = ImageIO.read(RI);
                image2 = improc.SizeChangerLinear(image2, 384, 288);
                RI = null;
                width = image2.getWidth();
                height = image2.getHeight();
                String SIZES = "";
                for (Integer i = 1; i < Math.min(height,width) / 2; i++) {
                    if (height % i == 0 && width % i == 0) {
                        System.out.print(i + " ");
                        if (i == 1)
                            SIZES += i.toString();
                        else
                            SIZES += "/" + i.toString();
                    }
                }
                frame.getContentPane();
                Selections.setText(SIZES);
                RightImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(image2, guiImageWidth, guiImageHeight)));
                //panel.add(RightImageLabel);
                frame.setVisible(true);
            } catch (Exception e) {
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
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.applyFunction(2, Double.parseDouble(Filtersize.getText())));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "min":
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.OrderStatFiltration("min"));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "max":
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.OrderStatFiltration("max"));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "median":
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.OrderStatFiltration("median"));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "avg":
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.OrderStatFiltration("avg"));
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "sobel":
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.Sobel());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "prewitt":
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.Prewitt());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "clarity":
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.Clarity());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "equalize":
                                improc.loadFull(DeepMap);
                                DeepMap = improc.ImageContrastIncrease();
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "amedian":
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.AdaptiveMedianFiltration());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                            case "wmedian":
                                improc.loadFull(DeepMap);
                                DeepMap = ImageCopy(improc.WeightedMedian());
                                BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
                                break;
                        }
                    }
                }
            }
        });

      /*  width = image1.getWidth() >= image2.getWidth() ? image2.getWidth() : image1.getWidth();
        height = image1.getHeight() >= image2.getHeight() ? image2.getHeight() : image1.getHeight();*/
        // System.out.println("Разрешение фоток " + width + " на " + height);

        //подсчет допустимых размеров сканирования, таких, чтобы изображение делилось без остатка
        fifth.add(Box.createHorizontalGlue());
        fifth.add(Selections);
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

        Buff.addActionListener(actionEvent -> buff = ImageCopy(DeepMap));
        GetMetrics.addActionListener(actionEvent -> {
            double[] metrics = improc.getMetrics(buff, DeepMap);
            JOptionPane.showMessageDialog(this,"Metrics \n Correlation: "+metrics[0]+" \n St. abs. deviation: "+ metrics[1]+"\n St. squared. deviation: "+metrics[2]);
        });
        GoMakeSomeMagic.addActionListener(actionEvent -> {

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
            else if (sad.isSelected())
                userchoise = 2;
            else if (ssd.isSelected())
                userchoise = 3;
            //Пожалуй, самая трудоемкая функция в данной программе, сложность - порядка O(n^3), но т.к число n - далеко не такое маленькое, зачастую приходится подождать
            DeepMap = CalculateDeepMap(width, height, scanscreensize, userchoise);

            // Сохранение

            try {
                int counter4savedimage = 0;
                if (DeepMap == null) {
                    throw new IOException();
                }
                File outputfile;
                do {
                    counter4savedimage++;
                    outputfile = new File("DepthMap" + counter4savedimage + ".png");
                } while (outputfile.exists());
                ImageIO.write(DeepMap, "png", outputfile);
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
            System.out.println("!!!!!!!!!!!!! "+((double)scanscreensize*guiImageWidth/width) +" "+ DeepMap.getWidth() +" "+ DeepMap.getWidth()*scanscreensize*guiImageWidth/width);
            System.out.println("!!!!!!!!!!!!! "+(double)scanscreensize*guiImageHeight/height +" "+ DeepMap.getHeight() +" "+ DeepMap.getHeight()*scanscreensize*guiImageHeight/height);
            System.out.println(improc.SizeChanger(DeepMap, ((double)scanscreensize*guiImageWidth/width + (double)scanscreensize*guiImageHeight/height)/2).getWidth()+" "+improc.SizeChanger(DeepMap, ((double)scanscreensize*guiImageWidth/width + (double)scanscreensize*guiImageHeight/height)/2).getHeight());
            BottomImageLabel.setIcon(new ImageIcon(improc.SizeChangerLinear(improc.SizeChanger(DeepMap, Math.round(((double)scanscreensize*guiImageWidth/width))), guiImageWidth, guiImageHeight)));
            //GradientOfColors.setIcon(new ImageIcon(improc.SizeChangerLinear(gradientstripe, guiImageWidth, guiImageHeight)));
            Buff.setEnabled(true);
            GetMetrics.setEnabled(true);
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

    public boolean Functions(CompareMethod method, int row1, int col1, int row2, int col2, int tempsizeadd) {
        //чтобы не таскать дальше эти большие фотки, создаем копии только нужных областей
        int[][][] tempmatrix1 = new int[scanscreensize+2*tempsizeadd][scanscreensize+2*tempsizeadd][3];
        int[][][] tempmatrix2 = new int[scanscreensize+2*tempsizeadd][scanscreensize+2*tempsizeadd][3];
        for (int i = 0; i < scanscreensize+2*tempsizeadd; i++) {
            for (int j = 0; j < scanscreensize+2*tempsizeadd; j++) {
                for (int k = 0; k < 3; k++) {
                    tempmatrix1[i][j][k] = matrix1[row1 + i - tempsizeadd][col1 + j - tempsizeadd][k];
                    tempmatrix2[i][j][k] = matrix2[row2 + i - tempsizeadd][col2 + j - tempsizeadd][k];
                }
            }
        }
        return method.DoMagic(tempmatrix1, tempmatrix2, scanscreensize, average4ssdandsad);
    }

    public BufferedImage CalculateDeepMap(int width, int height, int scanscreensize, int userchoise) {
        if (width % scanscreensize == 0 && height % scanscreensize == 0) {
            matrix3 = new double[width / scanscreensize][height / scanscreensize]; //матрица смещений
            //процесс нахождение точно значения пикселя первой матрицы во второй
            CompareMethod method = null;
            switch (userchoise) {
                //Пирсона (NСС)
                case 1:
                    method = new NCC();
                    break;
                //SAD
                case 2:
                    method = new SAD();
                    break;
                //SSD
                case 3:
                    method = new SSD();
                    break;
            }
            Double starttotal = 0.0;
            int coincidentx = 0;
            int coinncidenty = 0;
            int eps = 2;
            int tempsizeadd;
            double avg_average = (average4ssdandsad[0]+ average4ssdandsad[1] + average4ssdandsad[2])/3;
            for (int col_image1 = 0; col_image1 < width - scanscreensize+1; col_image1 += scanscreensize) {
                for (int row_image1 = 0; row_image1 < height - scanscreensize+1; row_image1 += scanscreensize) {
                    tempsizeadd = 0;
                    //System.out.println("!!!!!!!!!!!!!!!!!!!" + col_image1 + " "+ row_image1 + " "+width+"");
                    switch (userchoise) {
                        case 1:
                            starttotal = 0.1;
                            break;
                        case 2:
                            starttotal = avg_average * 0.2 * Math.pow(scanscreensize, 2)*5;
                            break;
                        case 3:
                            starttotal = Math.pow(avg_average * Math.pow(0.2 * scanscreensize, 2), 2)*5;
                            break;
                    }
                    int[][][] tempmatrix1 = new int[scanscreensize][scanscreensize][3];
                    for (int i = 0; i < scanscreensize; i++) {
                        for (int j = 0; j < scanscreensize; j++) {
                            for (int k = 0; k < 3; k++) {
                                //System.out.println(row_image1 +" "+ i+" "+col_image1 + " "+j);
                                tempmatrix1[i][j][k] = matrix1[col_image1 + i][row_image1 + j][k];
                            }
                        }
                    }
                    if(AdaptiveSize.isSelected()) {
                        int counter = 0;
                        while (improc.Std(tempmatrix1) < 0.4 && (col_image1 - 2 * tempsizeadd) > 0 && (width - scanscreensize - 2 * tempsizeadd - col_image1) > 0 && (row_image1 - 2 * tempsizeadd) > 0 && (height - scanscreensize - 2 * tempsizeadd - row_image1) > 0) {
                            counter += 1;
                            tempsizeadd += counter;
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
                    //for(int row_image2 = Math.max(0,row_image1-eps); row_image2 < Math.min(height-scanscreensize+1,row_image1+eps); row_image2++) {
                        for (int col_image2 = tempsizeadd; col_image2 < width - tempsizeadd - scanscreensize + 1; col_image2++) {
                            if (Math.abs(col_image1 - col_image2) < 50) {
                                //На самом деле ограничение области поиска (гипотенуза) должно быть намного уже, но программа просто напросто отказывается адекватно работать
                                if (Functions(method, col_image1, row_image1, col_image2, row_image1, tempsizeadd)) {
                                    coincidentx = col_image2;
                                    coinncidenty = row_image1;
                                    System.out.print("New Best: " + method.bestTotal + " ");
                                }
                            }
                        }
                    System.out.println();
                    matrix3[col_image1 / scanscreensize][row_image1 / scanscreensize] = Math.hypot(coincidentx - col_image1, coinncidenty - row_image1);
                }
            }
        }
        double max = 0;
        // double min = matrix3[0][0];
        int i_max = 0;
        int y_max = 0;
        BufferedImage DeepMap = new BufferedImage(width/scanscreensize, height/scanscreensize, BufferedImage.TYPE_INT_RGB);
        /*поиск максимума в матрице.
        RGB это три числа, каждое от 0 до 255. (0,0,0) - чёрный
        (255,255,255) - абсолютно белый цвет.
        Максимальное число в матрице считаем как бы за максимальную удалённость, это будет чисто белый цвет - 255.
        Далее каждый элемент матрицы закрашиваем цветом, равным (текущий элемент/максимальный элемент)*255.*/
        for (int i = 0; i < width / scanscreensize; i++)
            for (int j = 0; j < height / scanscreensize; j++) {
                if (Math.abs(matrix3[i][j]) > Math.abs(max)) {
                    max = matrix3[i][j];
                    //System.out.print("max= "+ max);
                    i_max = i;
                    y_max = j;
                }
         /*   if (min > matrix3[i][j]){
                min=matrix3[i][j];
            }*/
            }
        //System.out.print("max= " + max+" min=" + min);
        //System.out.print(i_max + "   " + y_max);
        gradientstripe = new BufferedImage(20, height, BufferedImage.TYPE_INT_RGB);
        Color mycolor;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < 20; j++) {
                mycolor = new Color((int) (255 * i / height), (int) (255 * i / height), (int) (255 * i / height));
                gradientstripe.setRGB(j, height - i - 1, mycolor.getRGB());
            }
        }
        for (int i = 0; i < width/scanscreensize; i++) {
            for (int j = 0; j < height/scanscreensize; j++)//максимальное количество пикселей в строке
            {
                System.out.print((int)matrix3[i][j]+" ");
                Color MyColor = new Color((int) ((matrix3[i][j] / matrix3[i_max][y_max]) * 255), (int) (matrix3[i][j] / matrix3[i_max][y_max] * 255), (int) (matrix3[i][j] / matrix3[i_max][y_max] * 255));
                DeepMap.setRGB(i, j, MyColor.getRGB()); //установка цвета
                //рисование закрашенного прямоугольника с началом координам x=i*w, y=j*w. Ширина и длина w.
            }
            System.out.println();
        }
        // После этого нужно выбрать нужные данные и нажать на кнопку снизу менюшки, потом подождать, пока кнопка не станет вновь доступна и снова растянуть/сжать окно
        return DeepMap;
    }
}