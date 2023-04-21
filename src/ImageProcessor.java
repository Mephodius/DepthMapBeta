import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Mephodius
 * This class in the one with the hugest amount of mathematics here, so its basicly more server-orientieted
 */
public class ImageProcessor {
    private MainFrame mf;
    private final double e = Math.pow(10,-6);
    private final int L = 256; //квантование по яркости
    private Double[][] mask;
    private Integer[][][] tempMat;
    private Integer size;
    private BufferedImage TempImage = null;
    private BufferedImage Result = null;
    private Integer width;
    private Integer height;
    private Double[] average;
    private Double div = 1.0;
    private Boolean full = true;
    private Integer[] points;
    private int top, bot, left, right;

    /**
     * Typical constructor
     *
     * @param size to regulate the basic size of a filter
     */
    ImageProcessor(int size) {
        this.size = size;
        this.average = new Double[]{0.0, 0.0, 0.0};
    }

    /**
     * Typical constructor
     */
    ImageProcessor() {
        this.size = 0;
        this.average = new Double[]{0.0, 0.0, 0.0};
    }

    /**
     * Common setting method
     *
     * @param size a parameter to set
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * Usual getting method
     *
     * @return result image after applying any filtration
     */
    public BufferedImage getResult() {
        return Result;
    }

    /**
     * Method, that is used to load the full image for further processing
     *
     * @param LoadedImage image to process
     */
    public void loadFull(BufferedImage LoadedImage) {
        full = true;
        Result = new BufferedImage(LoadedImage.getWidth(), LoadedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < Result.getWidth(); i++) {
            for (int j = 0; j < Result.getHeight(); j++) {
                Result.setRGB(i, j, new Color(LoadedImage.getRGB(i, j)).getRGB());
            }
        }
        width = Result.getWidth() + 2 * size;
        height = Result.getHeight() + 2 * size;
        TempImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < Result.getWidth(); i++) {
            for (int j = 0; j < Result.getHeight(); j++) {
                if (i >= 0 && i < Result.getWidth() && j >= 0 && j < Result.getHeight())
                    TempImage.setRGB(i + size, j + size, new Color(Result.getRGB(i, j)).getRGB());
            }
        }
    }
    public void setMainFrame(MainFrame mainFrame){
        this.mf = mainFrame;
    }
    /**
     * Method, that is used to load any part of original image for further processing
     *
     * @param LoadedImage image to process
     * @param x           top left x
     * @param y           top left y
     * @param x1          bot right x
     * @param y1          bot right y
     */
    //copy needed part of the image, extending it
    public void loadPart(BufferedImage LoadedImage, int x, int y, int x1, int y1) {
        points = new Integer[]{x, y, x1, y1};
        full = false;
        Result = new BufferedImage(x1 - x + 1, y1 - y + 1, BufferedImage.TYPE_INT_RGB);
        for (int i = x; i <= x1; i++) {
            for (int j = y; j <= y1; j++) {
                Result.setRGB(i - x, j - y, new Color(LoadedImage.getRGB(i, j)).getRGB());
            }
        }
        width = Result.getWidth() + 2 * size;
        height = Result.getHeight() + 2 * size;
        TempImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = x - size; i <= x1 + size; i++) {
            for (int j = y - size; j <= y1 + size; j++) {
                if (i >= 0 && i < LoadedImage.getWidth() && j >= 0 && j < LoadedImage.getHeight())
                    TempImage.setRGB(i - x + size, j - y + size, new Color(LoadedImage.getRGB(i, j)).getRGB());
            }
        }
        top = Math.max(size - points[1], 0);
        bot = size - Math.min((LoadedImage.getHeight() - 1 - points[3]), size);
        left = Math.max(size - points[0], 0);
        right = size - Math.min((LoadedImage.getWidth() - 1 - points[2]), size);

    }

    /**
     * Private method, that makes it real to process corners of the image
     */
    //fill the extended parts of the image
    private void ImageExtension() {
        if (full) {
            for (int i = size; i < width - size; i++) {
                for (int j = 0; j < size; j++) {
                    TempImage.setRGB(i, j, new Color(TempImage.getRGB(i, size + j)).getRGB());
                    TempImage.setRGB(i, height - j - 1, new Color(TempImage.getRGB(i, height - size - 1 - j)).getRGB());
                }
            }
            for (int i = 0; i < size; i++) {
                for (int j = size; j < height - size; j++) {
                    TempImage.setRGB(i, j, new Color(TempImage.getRGB(size + i, j)).getRGB());
                    TempImage.setRGB(width - i - 1, j, new Color(TempImage.getRGB(width - size - 1 - i, j)).getRGB());
                }
            }
            Color tempColor;
            for (int i = 1; i <= size; i++) {
                for (int j = 1; j <= size; j++) {
                    //filling left-top area
                    tempColor = AverageColor(new Color[]{new Color(TempImage.getRGB(size - i + 1, size - j)), new Color(TempImage.getRGB(size - i, size - j + 1))});
                    TempImage.setRGB(size - i, size - j, tempColor.getRGB());
                    //filling right-top area
                    tempColor = AverageColor(new Color[]{new Color(TempImage.getRGB(width - size + i - 2, size - j)), new Color(TempImage.getRGB(width - size + i - 1, size - j + 1))});
                    TempImage.setRGB(width - size + i - 1, size - j, tempColor.getRGB());
                    //filling left-bot area
                    tempColor = AverageColor(new Color[]{new Color(TempImage.getRGB(size - i + 1, height - size + j - 1)), new Color(TempImage.getRGB(size - i, height - size + j - 2))});
                    TempImage.setRGB(size - i, height - size + j - 1, tempColor.getRGB());
                    //filling right-bot area
                    tempColor = AverageColor(new Color[]{new Color(TempImage.getRGB(width - size + i - 2, height - size + j - 1)), new Color(TempImage.getRGB(width - size + i - 1, height - size + j - 2))});
                    TempImage.setRGB(width - size + i - 1, height - size + j - 1, tempColor.getRGB());
                }
            }
        } else {

            for (int i = left; i < width - right; i++) {
                for (int j = 0; j < top; j++) {
                    TempImage.setRGB(i, j, new Color(TempImage.getRGB(i, top + (top - j))).getRGB());
                }
            }
            for (int i = left; i < width - right; i++) {
                for (int j = 0; j < bot; j++) {
                    TempImage.setRGB(i, height - j - 1, new Color(TempImage.getRGB(i, height - bot - 1 - (bot - j))).getRGB());
                }
            }
            for (int i = 0; i < left; i++) {
                for (int j = top; j < height - bot; j++) {
                    TempImage.setRGB(i, j, new Color(TempImage.getRGB(left + (left - i), j)).getRGB());
                }
            }
            for (int i = 0; i < right; i++) {
                for (int j = top; j < height - bot; j++) {
                    TempImage.setRGB(width - i - 1, j, new Color(TempImage.getRGB(width - right - 1 - (right - i), j)).getRGB());
                }
            }
            Color tempColor;
            for (int i = 1; i <= size; i++) {
                for (int j = 1; j <= size; j++) {
                    //filling left-top area
                    if (i - 1 < left && j - 1 < top) {
                        tempColor = AverageColor(new Color[]{new Color(TempImage.getRGB(left - i + 1, top - j)), new Color(TempImage.getRGB(left - i, top - j + 1))});
                        TempImage.setRGB(left - i, top - j, tempColor.getRGB());
                    }
                    //filling right-top area
                    if (i - 1 < right && j - 1 < top) {
                        tempColor = AverageColor(new Color[]{new Color(TempImage.getRGB(width - right + i - 2, top - j)), new Color(TempImage.getRGB(width - right + i - 1, top - j + 1))});
                        TempImage.setRGB(width - right + i - 1, top - j, tempColor.getRGB());
                    }
                    //filling left-bot area
                    if (i - 1 < left && j - 1 < bot) {
                        tempColor = AverageColor(new Color[]{new Color(TempImage.getRGB(left - i + 1, height - bot + j - 1)), new Color(TempImage.getRGB(left - i, height - bot + j - 2))});
                        TempImage.setRGB(left - i, height - bot + j - 1, tempColor.getRGB());
                    }
                    //filling right-bot area
                    if (i - 1 < right && j - 1 < bot) {
                        tempColor = AverageColor(new Color[]{new Color(TempImage.getRGB(width - right + i - 2, height - bot + j - 1)), new Color(TempImage.getRGB(width - right + i - 1, height - bot + j - 2))});
                        TempImage.setRGB(width - right + i - 1, height - bot + j - 1, tempColor.getRGB());
                    }
                }
            }
        }

    }

    /**
     * Method to calculate average RGB color
     * @param colors colors to mix
     * @return average color
     */
    private Color AverageColor(Color[] colors) {
        int tempred = 0;
        int tempgreen = 0;
        int tempblue = 0;
        for (Color color : colors) {
            tempred += color.getRed();
            tempgreen += color.getGreen();
            tempblue += color.getBlue();
        }
        return new Color(tempred / colors.length, tempgreen / colors.length, tempblue / colors.length);
    }

    /**
     * Method to calculate an average RGB color of whole image
     * @used in borderline methods
     */
    private void Average() {
        syncMatrix();
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    average[k] += tempMat[i][j][k];
                }
            }
            average[k] = average[k] / (height * width);
        }
    }
    /**
     * Method to calculate an average RGB color of whole image
     * @used in borderline methods
     */
    public double[] Average(int[][][] matrix) {
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


    public double Std(int[][][] matrix) {
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

    /**
     * Method to synchronize temp matrix with temp image
     */
    //synchronises matrix with image
    private void syncMatrix() {
        tempMat = new Integer[width][height][3];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tempMat[i][j][0] = new Color(TempImage.getRGB(i, j)).getRed();
                tempMat[i][j][1] = new Color(TempImage.getRGB(i, j)).getGreen();
                tempMat[i][j][2] = new Color(TempImage.getRGB(i, j)).getBlue();
            }
        }
    }


    /**
     * Method to synchronize temp image with temp matrix
     */
    //synchronises image with matrix
    private void syncImage() {
        TempImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                TempImage.setRGB(i, j, new Color(tempMat[i][j][0], tempMat[i][j][1], tempMat[i][j][2]).getRGB());
            }
        }
    }

    private void UpdateTemp(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        TempImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                TempImage.setRGB(i, j, new Color(image.getRGB(i, j)).getRGB());
            }
        }
    }

    /**
     * Method to correct out-of-bounds values
     * @param value a value to correct
     * @return corrected value
     */
    //method is correcting out of limit elements
    private Double elementCorrect(Double value) {
        if (value > (L - 1))
            return (double) (L - 1);
        if (value < 0)
            return 0.0;
        return value;
    }

    private void minMaxScaler(){
        int[] min = {0, 0, 0};
        int[] max = {tempMat[0][0][0], tempMat[0][0][1], tempMat[0][0][2]};
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    if(min[k] > tempMat[i][j][k])
                        min[k]=tempMat[i][j][k];
                    if(max[k] < tempMat[i][j][k])
                        max[k]=tempMat[i][j][k];
                }
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    tempMat[i][j][k]=(tempMat[i][j][k]-min[k])*255/(max[k]-min[k]);
                }
            }
        }
    }

    public BufferedImage applyFunction(int type, double param){
        syncMatrix();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 3; k++) {
                    switch(type){
                        case 1:
                            tempMat[i][j][k] = (int)(Math.log(tempMat[i][j][k]+1)/Math.log(param));
                            break;
                        case 2:
                            tempMat[i][j][k] = (int)Math.pow(tempMat[i][j][k], param);
                            break;
                    }
                    System.out.print(tempMat[i][j][k]+" ");
                }
                System.out.println();
            }
            System.out.println();
        }
        minMaxScaler();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Result.setRGB(i, j, new Color(tempMat[i][j][0], tempMat[i][j][1], tempMat[i][j][2]).getRGB());
            }
        }
        return Result;
    }
    public BufferedImage ImageSubstitution(BufferedImage first, BufferedImage second, double alpha){
        int width = first.getWidth();
        int height = second.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color color1 = new Color(first.getRGB(i, j));
                Color color2 = new Color(second.getRGB(i, j));
                int blue = Math.max((int)(color1.getBlue() - alpha*color2.getBlue()),0);
                int red = Math.max((int)(color1.getRed() - alpha*color2.getRed()),0);
                int green = Math.max((int)(color1.getGreen() - alpha*color2.getGreen()), 0);
                first.setRGB(i, j, new Color(red, green, blue).getRGB());
            }
        }
        return first;
    }
    /**
     * Standard factorial function
     * @param f a number, factorial of that do you need
     * @return factorial of f
     */
    private int getFactorial(int f) {
        if (f > 0) {
            int result = 1;
            for (int i = 1; i <= f; i++) {
                result = result * i;
            }
            return result;
        } else {
            return 0;
        }
    }

    /**
     * Median matrix filter, used to get rid of salt&pepper noise and make image mush more smooth
     */
    public BufferedImage Median() {
        ImageExtension();
        syncMatrix();
        int[] sorted = new int[(int) Math.pow(2 * size + 1, 2)];
        int[] temprgb = new int[3];
        for (int i = size; i < width - size; i++) {
            for (int j = size; j < height - size; j++) {
                for (int k = 0; k < 3; k++) {
                    for (int m = -size; m <= size; m++) {
                        for (int n = -size; n <= size; n++) {
                            sorted[n + size + (m + size) * (2 * size + 1)] = tempMat[i + m][j + n][k];
                        }
                    }
                    Arrays.sort(sorted);
                    temprgb[k] = sorted[sorted.length / 2];
                }
                Result.setRGB(i - size, j - size, new Color(temprgb[0], temprgb[1], temprgb[2]).getRGB());
            }
        }
        return Result;
    }

    /**
     * Clarity matrix filter, makes image colors more "sharp"
     */
    public BufferedImage Clarity() {
        div = 0.0;
        mask = new Double[2 * size + 1][2 * size + 1];
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                if (i + j < mask.length) {
                    mask[i][j] = (double) (-Math.min(i, j) - 1);
                } else {
                    mask[i][j] = (double) (-(mask.length - Math.max(i, j)));
                }
                if (i == mask.length / 2 && j == mask.length / 2) {
                    mask[i][j] = 2 * Math.abs(div) + 1;
                }
                div += mask[i][j];
            }
        }
        div = 1.0;
        return MatrixFiltration();
    }

    /**
     * Erosion matrix filter (diamond locality)
     */
    public BufferedImage Erosion() {
        div = 0.0;
        mask = new Double[2 * size + 1][2 * size + 1];
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                if (i > j + mask.length / 2 || i < j - mask.length / 2 || i + j < mask.length / 2 || i + j > mask.length + mask.length / 2 - 1) {
                    mask[i][j] = 0.0;
                } else {
                    mask[i][j] = 1.0;
                }
                div += mask[i][j];
            }
        }
        return MatrixFiltration();
    }

    /**
     * Round erosion matrix filter
     */
    public BufferedImage RoundEro() {
        div = 0.0;
        mask = new Double[2 * size + 1][2 * size + 1];
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                if (Math.hypot(i - mask.length / 2, j - mask.length / 2) > mask.length / 2) {
                    mask[i][j] = 0.0;
                } else {
                    mask[i][j] = 1.0;
                }
                div += mask[i][j];
            }
        }
        return MatrixFiltration();
    }
    /**
     * Gaussian matrix filter (blur)
     */
    public BufferedImage Gaussian(double dispersion) {
        ImageExtension();
        syncMatrix();
        div = 0.0;
        mask = new Double[2 * size + 1][2 * size + 1];
        double min = 10;
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                mask[i][j] = ((double)1/((2*Math.PI*dispersion)*Math.exp(Math.hypot((i-size),(j-size))/(2*dispersion))));
                if (min > mask[i][j]){
                    min=mask[i][j];
                }
            }
        }
        System.out.println();
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                mask[i][j]/=min;
                mask[i][j] = (double)Math.round(mask[i][j]);
                div += mask[i][j];
            }
        }
        return MatrixFiltration();
    }
    public BufferedImage WeightedMedian() {
        ImageExtension();
        syncMatrix();
        Gaussian(1);
        int[] temprgb = new int[3];
        for (int i = size; i < width - size; i++) {
            for (int j = size; j < height - size; j++) {
                for (int k = 0; k < 3; k++) {
                    ArrayList<Integer> sorted = new ArrayList<Integer>();
                    for (int m = -size; m <= size; m++) {
                        for (int n = -size; n <= size; n++) {
                            for (int c = 0; c < mask[m+size][n+size]; c++) {
                                sorted.add(tempMat[i + m][j + n][k]);
                            }
                        }
                    }
                    Collections.sort(sorted);
                    temprgb[k] = sorted.get(sorted.size()/2);
                }
                Result.setRGB(i - size, j - size, new Color(temprgb[0], temprgb[1], temprgb[2]).getRGB());
            }
        }
        return Result;
    }

    /**
     * Random matrix filter, randomly generates lineal matrix filter
     */
    public BufferedImage Random() {
        div = 0.0;
        mask = new Double[2 * size + 1][2 * size + 1];
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                mask[i][j] = (double) (int) (Math.random() * 10 - 5);
                div += mask[i][j];
            }
        }
        if (div == 0)
            div = 1.0;
        return MatrixFiltration();
    }

    /**
     * Sobel matrix filter (kind of borderline filters)
     */
    public BufferedImage Sobel() {
        div = 1.0;
        mask = new Double[2 * size + 1][2 * size + 1];
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                if (j < mask.length / 2 && i <= mask.length / 2) {
                    mask[i][j] = (double) (getFactorial(i + 1) * (j + 1));
                    mask[mask.length - 1 - i][j] = (double) (getFactorial(i + 1) * (j + 1));
                    mask[i][mask.length - 1 - j] = -(double) (getFactorial(i + 1) * (j + 1));
                    mask[mask.length - 1 - i][mask.length - 1 - j] = -(double) (getFactorial(i + 1) * (j + 1));
                    if (div < mask[i][j])
                        div = mask[i][j];
                }
                if (j == mask.length / 2) {
                    mask[i][j] = 0.0;
                }

            }
        }
        Borderline();
        return Result;
    }

    /**
     * Prewitt matrix filter (kind of borderline filters)
     */
    public BufferedImage Prewitt() {
        mask = new Double[2 * size + 1][2 * size + 1];
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                if (j < mask.length / 2) {
                    mask[i][j] = -1.0;
                }
                if (j == mask.length / 2) {
                    mask[i][j] = 0.0;
                }
                if (j > mask.length / 2) {
                    mask[i][j] = 1.0;
                }
            }
        }
        div = 1.0;
        Borderline();
        return Result;
    }

    /**
     * Private method of borderline processing
     * @used in Sobel borderline method
     * @used in Prewitt borderline method
     */
    private void Borderline() {

        ImageExtension();
        Average();
        syncMatrix();
        //Маски стандартные, легко гуглятся
        Double[] Gx = new Double[3];
        Double[] Gy = new Double[3];
        Double[] Grad = new Double[3];
        Double[] limit = new Double[]{average[0]/3, average[1]/3, average[2]/3};

        for (int i = size; i < width - size; i++) {
            for (int j = size; j < height - size; j++) {
                for (int k = 0; k < 3; k++) {
                    Gx[k] = 0.0;
                    Gy[k] = 0.0;
                    for (int m = -size; m <= size; m++) {
                        for (int n = -size; n <= size; n++) {
                            Gx[k] += (tempMat[i + m][j + n][k] * mask[m + size][n + size]) / div;
                            Gy[k] += (tempMat[i + m][j + n][k] * mask[n + size][m + size]) / div;
                        }
                    }
                    Grad[k] = Math.hypot(Gx[k], Gy[k]);
                    if (Grad[k] > limit[k]) {
                        //белый цвет для краевых точек
                        Grad[k] = (double) (L - 1);

                    } else {
                        //черный для внутренних
                        Grad[k] = 0.0;
                    }
                }
                Result.setRGB(i - size, j - size, new Color(Grad[0].intValue(), Grad[1].intValue(), Grad[2].intValue()).getRGB());

            }
        }
        syncImage();
    }

    /**
     * Black & White converting
     */
    public BufferedImage BW() {
        ImageExtension();
        for (int i = size; i < width - size; i++) {
            for (int j = size; j < height - size; j++) {
                Color color = new Color(TempImage.getRGB(i, j));
                int blue = color.getBlue();
                int red = color.getRed();
                int green = color.getGreen();
                // Применяем стандартный алгоритм для получения черно-белого изображения
                int grey = (int) (red * 0.299 + green * 0.587 + blue * 0.114);
                //все каналы серого имеют одно и то же значение, поэтому и делаем одно значение
                Color newcolor = new Color(grey, grey, grey);
                Result.setRGB(i - size, j - size, newcolor.getRGB());
            }
        }
        return Result;
    }

    /**
     * Matrix filter with user matrix
     * @param mask user matrix
     */
    public BufferedImage UserFilter(Double[][] mask) {
        this.mask = mask;
        size = mask.length / 2;
        div = 0.0;
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                div += mask[i][j];
            }
        }
        if (div == 0)
            div = 1.0;
        return MatrixFiltration();
    }

    /**
     * Private method of standard matrix processing, used in many others lineal matrix filters
     * @used in Clarity filter method
     * @used in Erosion filter methods
     * @used in Random filter method
     * @used in User filter method
     */
    private BufferedImage MatrixFiltration() {
        ImageExtension();
        syncMatrix();
        Double[] temprgb = new Double[3];
        for (int i = size; i < width - size; i++) {
            for (int j = size; j < height - size; j++) {
                temprgb[0] = 0.0;
                temprgb[1] = 0.0;
                temprgb[2] = 0.0;
                for (int k = 0; k < 3; k++) {
                    for (int m = -size; m <= size; m++) {
                        for (int n = -size; n <= size; n++) {
                            temprgb[k] += (int) (tempMat[i + m][j + n][k] * mask[m + size][n + size]);
                        }
                    }
                    temprgb[k] /= div;
                    temprgb[k] = elementCorrect(temprgb[k]);
                }
                Result.setRGB(i - size, j - size, new Color(temprgb[0].intValue(), temprgb[1].intValue(), temprgb[2].intValue()).getRGB());
            }
        }
        syncImage();
        return Result;
    }

    /**
     * Method to increase contrast globally, based on histogram equalization
     * @return
     */
    public BufferedImage ImageContrastIncrease() {
        UpdateTemp(Result);
        syncMatrix();
        Double[][] BrightnessDensity = new Double[3][L];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j <= (L - 1); j++) {
                BrightnessDensity[i][j] = 0.0;
            }
        }
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    BrightnessDensity[k][tempMat[i][j][k]]++;
                }
            }
        }
        Double[][] BrightnessDistribution = new Double[3][L];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j <= (L - 1); j++) {
                BrightnessDensity[i][j] /= Result.getHeight() * Result.getWidth();
                if (j > 0)
                    BrightnessDistribution[i][j] = BrightnessDensity[i][j] + BrightnessDistribution[i][j - 1];
                else
                    BrightnessDistribution[i][j] = BrightnessDensity[i][j];
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Integer red = (int) ((L - 1) * BrightnessDistribution[0][tempMat[i][j][0]]);
                Integer green = (int) ((L - 1) * BrightnessDistribution[1][tempMat[i][j][1]]);
                Integer blue = (int) ((L - 1) * BrightnessDistribution[2][tempMat[i][j][2]]);
                Result.setRGB(i, j, new Color(red, green, blue).getRGB());
            }
        }
        syncImage();
        return Result;
    }

    /**
     * @deprecated
     * Locally increases contrast, but works strange, that's why deprecated
     * @return
     */
    public BufferedImage LocalContrastIncrease() {
        int length = 1;
        for (int i = 1; i < Math.min(Result.getWidth(), Result.getHeight()) / 2; i++) {
            if (Result.getHeight() % i == 0 && Result.getHeight() == 0) {
                length = i;
            }
        }
        if (length > 10) {
            UpdateTemp(Result);
            syncMatrix();
            Double[][] BrightnessDensity = new Double[3][L];
            for (int m = 0; m < width; m += length) {
                for (int n = 0; n < height; n += length) {
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j <= (L - 1); j++) {
                            BrightnessDensity[i][j] = 0.0;
                        }
                    }
                    for (int k = 0; k < 3; k++) {
                        for (int i = 0; i < length; i++) {
                            for (int j = 0; j < length; j++) {
                                BrightnessDensity[k][tempMat[m + i][n + j][k]]++;
                            }
                        }
                    }
                    Double[][] BrightnessDistribution = new Double[3][L];
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j <= (L - 1); j++) {
                            BrightnessDensity[i][j] /= Math.pow(length, 2);
                            if (j > 0)
                                BrightnessDistribution[i][j] = BrightnessDensity[i][j] + BrightnessDistribution[i][j - 1];
                            else
                                BrightnessDistribution[i][j] = BrightnessDensity[i][j];

                        }
                    }
                    for (int i = 0; i < length; i++) {
                        for (int j = 0; j < length; j++) {
                            Integer red = (int) ((L - 1) * BrightnessDistribution[0][tempMat[m + i][n + j][0]]);
                            Integer green = (int) ((L - 1) * BrightnessDistribution[1][tempMat[m + i][n + j][1]]);
                            Integer blue = (int) ((L - 1) * BrightnessDistribution[2][tempMat[m + i][n + j][2]]);
                            Result.setRGB(m + i, n + j, new Color(red, green, blue).getRGB());
                        }
                    }
                }
            }
        }
        syncImage();
        return Result;
    }

    /**
     * Cut down some even pixels in case size could be changed one more time
     * @param source image to cut
     * @return cutted image
     */
    //cutting even pixels for size decreasing
    public BufferedImage SizeCutter(BufferedImage source) {

        int width = source.getWidth();
        int height = source.getHeight();
        if (source.getWidth() % 2 == 1) {
            width -= 1;
        }
        if (source.getHeight() % 2 == 1) {
            height -= 1;
        }
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if (height != source.getHeight() || width != source.getWidth()) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    result.setRGB(i, j, new Color(source.getRGB(i, j)).getRGB());
                }
            }
            return result;
        } else {
            return source;
        }

    }


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
                    result.setRGB(i, j, tempcolor.getRGB());
                }
            }
            return result;
        }
        return null;
    }

    //Увеличивает изображение в натуральное количество раз
    public BufferedImage SizeIncreaser(BufferedImage source, int increasingcoefficient) {
        Result = new BufferedImage(source.getWidth() * increasingcoefficient, source.getHeight() * increasingcoefficient, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < source.getWidth() * increasingcoefficient; i++) {
            for (int j = 0; j < source.getHeight() * increasingcoefficient; j++) {
                Color tempcolor = new Color(source.getRGB(i / increasingcoefficient, j / increasingcoefficient));
                Result.setRGB(i, j, tempcolor.getRGB());
            }
        }
        return Result;
    }

    //Изменяет размер изображения, при этом не обязательно в натуральное количество раз
    public BufferedImage SizeChanger(BufferedImage source, double changingcoefficient) {
        if (source.getWidth() * changingcoefficient == (int) (source.getWidth() * changingcoefficient) && source.getHeight() * changingcoefficient == (int) (source.getHeight() * changingcoefficient)) {
            int denominator = 1;
            do {
                changingcoefficient *= 10;
                denominator *= 10;
            } while (changingcoefficient != (int) changingcoefficient);
            int numerator = (int) changingcoefficient;

//максимально упрощаем дробь
            for (int i = 2; i <= (numerator > denominator ? denominator : numerator); i++) {
                if (numerator % i == 0 && denominator % i == 0) {
                    numerator /= i;
                    denominator /= i;
                }
            }
            System.out.println(numerator + " " + denominator);
            BufferedImage temp;
            temp = SizeIncreaser(source, numerator);
            Result = SizeDecreaser(temp, denominator);
            return Result;
        }
        return source;
    }
    // Перегрузка под уже заданные отдельно коэффициенты
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
    //использует линейную интерполяцию для передискретизации размеров изображения
    public BufferedImage SizeChangerLinear(BufferedImage source, int newWidth, int newHeight) {
        if(source.getWidth()==newWidth && source.getHeight()==newHeight){
            return source;
        }
        else {
            Result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            UpdateTemp(source);
            syncMatrix();
            double dx = (double) (source.getWidth() - 1) / (newWidth - 1);
            double dy = (double) (source.getHeight() - 1) / (newHeight - 1);
            Integer[] temprgb = new Integer[3];
            int x;
            int y;
            double realx;
            double realy;
            double temprgbx;
            double temprgby;
            double coefx;
            double coefy;
            for (int i = 0; i < newWidth; i++) {
                for (int j = 0; j < newHeight; j++) {
                    temprgb[0] = 0;
                    temprgb[1] = 0;
                    temprgb[2] = 0;
                    for (int k = 0; k < 3; k++) {
                        x = (int) (i * dx);
                        y = (int) (j * dy);
                        realx = i * dx;
                        realy = j * dy;
                        if (i < newWidth - 1) {
                            temprgbx = Math.min(tempMat[x + 1][y][k], tempMat[x][y][k]) + (realx - x) * Math.abs(tempMat[x + 1][y][k] - tempMat[x][y][k]);
                        } else {
                            temprgbx = 0;
                        }
                        if (j < newHeight - 1) {
                            temprgby = Math.min(tempMat[x][y + 1][k], tempMat[x][y][k]) + (realy - y) * Math.abs(tempMat[x][y + 1][k] - tempMat[x][y][k]);
                        } else {
                            temprgby = 0;
                        }
                        if (Math.abs(realx - x) > e) {
                            coefx = (realx - x) / (realx - x + realy - y);
                        } else {
                            coefx = 0;
                        }
                        if (Math.abs(realy - y) > e) {
                            coefy = (realy - y) / (realx - x + realy - y);
                        } else {
                            coefy = 0;
                        }
                        if (Math.abs(realx - x) < e && Math.abs(realy - y) < e) {
                            coefx = 0.5;
                            coefy = 0.5;
                        }
                        if (i >= newWidth - 1 && j < newHeight - 1) {
                            coefy = 1;
                        }
                        if (i < newWidth - 1 && j >= newHeight - 1) {
                            coefx = 1;
                        }
                        temprgb[k] = (int) (coefx * temprgbx + coefy * temprgby);
                    }
                    Result.setRGB(i, j, new Color(temprgb[0], temprgb[1], temprgb[2]).getRGB());
                }
                Result.setRGB(newWidth - 1, newHeight - 1, new Color(tempMat[width - 1][height - 1][0], tempMat[width - 1][height - 1][1], tempMat[width - 1][height - 1][2]).getRGB());
            }
            return Result;
        }
    }

    /**
     *
     * @param source Image needed to process
     * @param bitconfig configuration of each bit in the image (on/off). As an example {0, 1, 0, 0, 0, 1, 0, 1}
     * @return image with cut down "off" color bits
     */
    public BufferedImage BitCutter(BufferedImage source, Byte[] bitconfig){
        Result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        UpdateTemp(source);
        syncMatrix();
        Byte[] tempbits;
        Integer[] temprgb = new Integer[3];
        for(int i = 0; i< source.getWidth(); i++){
            for(int j = 0; j< source.getHeight(); j++){
                for(int k = 0; k<3; k++){
                    tempbits = toBinary(tempMat[i][j][k]);
                    for(int l = 0; l<8; l++){
                        tempbits[l] = (byte)Math.min(tempbits[l],bitconfig[l]);
                    }
                    temprgb[k]=toDecent(tempbits);
                }
                Result.setRGB(i,j,new Color(temprgb[0],temprgb[1],temprgb[2]).getRGB());
            }
        }
        return Result;
    }
    //метод для перевода числа в двоичный вид
    private Byte[] toBinary(int number){
        Byte [] bits = {0, 0, 0, 0, 0, 0, 0, 0};
        int temp=128;
        int tempnumber = number;
        for(int i = 0; i<8; i++){
            if(tempnumber-temp>=0){
                bits[i]=1;
                tempnumber-=temp;
            }
            temp/=2;
        }

        return bits;
    }
    private int toDecent(Byte[] bits){
        int temp=0;
        for(int i = 0; i<bits.length; i++){
            temp+=bits[i]*Math.pow(2,7-i);
        }
        return temp;
    }
    public BufferedImage ColorCutter(BufferedImage source, Byte[] colorconfig){
        Result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        UpdateTemp(source);
        syncMatrix();
        Integer[] temprgb = new Integer[3];
        for(int i = 0; i< source.getWidth(); i++){
            for(int j = 0; j< source.getHeight(); j++){
                for(int k = 0; k<3; k++){
                    if(colorconfig[k]==0){
                        temprgb[k]=0;
                    }
                    else{
                        temprgb[k]=tempMat[i][j][k];
                    }
                }
                Result.setRGB(i,j,new Color(temprgb[0],temprgb[1],temprgb[2]).getRGB());
            }
        }
        return Result;
    }
    public double PSNR(int[][][] mat1, int[][][] mat2){
        double psnr = -1;
        double temp = 0;
        for (int i = 0; i < mat1.length; i++) {
            for (int j = 0; j < mat1[0].length; j++) {
                for (int k = 0; k < 3; k++) {
                    temp += Math.pow(mat1[i][j][k] - mat2[i][j][k], 2);
                }
            }
        }
        psnr = 10*Math.log10((double)255*255*mat1.length*mat1[0].length/(Math.sqrt(temp)));
        return psnr;
    }
    public int[][][] ImageToMatrix(BufferedImage image){
        int[][][] matrix = new int[image.getHeight()][image.getWidth()][3];
        for(int i = 0; i< image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color color = new Color(image.getRGB(j,i));
                matrix[i][j][0] = color.getRed();
                matrix[i][j][1] = color.getGreen();
                matrix[i][j][2] = color.getBlue();
            }
        }
        return matrix;
    }
    public int[][] BWImageToMatrix(BufferedImage image){
        int[][] matrix = new int[image.getHeight()][image.getWidth()];
        for(int i = 0; i< image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color color = new Color(image.getRGB(j,i));
                matrix[i][j] = color.getRed();
            }
        }
        return matrix;
    }
    public BufferedImage MatrixToImage(int[][][] matrix){
        int width = matrix.length;
        int height = matrix[0].length;
        BufferedImage tempimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
            {
                Color MyColor = new Color(matrix[i][j][0], matrix[i][j][1], matrix[i][j][2]);
                tempimg.setRGB(i, j, MyColor.getRGB());
            }
        }
        return tempimg;
    }
    public double[] getMetrics(BufferedImage im1, BufferedImage im2){
        double[] metrics = {0, 0, 0};
        int temp_width = im1.getWidth();
        int temp_height = im1.getHeight();
        System.out.println("IM1: "+im1.getWidth()+" "+im1.getHeight() +" IM2: "+im2.getWidth()+" "+im2.getHeight());
        int[][][] matrix1 = ImageToMatrix(im1);
        int[][][] matrix2 = ImageToMatrix(im2);
        double[] averageim1 = {0, 0, 0};
        double[] averageim2 = {0, 0, 0};
        double numerator;
        double denominator;
        double temp;
        double[] total = {0, 0, 0};
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < temp_height; i++) {
                for (int j = 0; j < temp_width; j++) {
                    averageim1[k] += matrix1[i][j][k];
                    averageim2[k] += matrix2[i][j][k];
                }
            }
            averageim1[k] /= (temp_width*temp_height);
            averageim2[k] /= (temp_width*temp_height);
            numerator = 0;
            denominator = 0;
            temp = 0;
            for (int i = 0; i < temp_height; i++) {
                for (int j = 0; j < temp_width; j++) {
                    numerator += (matrix1[i][j][k] - averageim1[k]) * (matrix2[i][j][k] - averageim2[k]);
                    denominator += Math.pow((matrix1[i][j][k] - averageim1[k]), 2);
                    temp += Math.pow((matrix2[i][j][k] - averageim2[k]), 2);
                }
            }
            denominator = Math.sqrt(denominator) * Math.sqrt(temp);

            total[k] = numerator / denominator;
        }
        metrics[0] = (total[0] + total[1] + total[2])/3;

        total = new double[]{0, 0, 0};

        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < temp_height; i++) {
                for (int j = 0; j < temp_width; j++) {
                    total[k] += Math.abs(matrix1[i][j][k] - matrix2[i][j][k]);
                }
            }
            total[k]/=(temp_height*temp_width);
        }
        metrics[1] = (total[0] + total[1] + total[2])/3;
        total = new double[]{0, 0, 0};
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < temp_height; i++) {
                for (int j = 0; j < temp_width; j++) {
                    total[k] += Math.pow(matrix1[i][j][k] - matrix2[i][j][k],2);
                }
            }
            total[k]/=(temp_height*temp_width);
        }
        metrics[2] = (total[0] + total[1] + total[2])/3;
        return metrics;
    }

    public BufferedImage OrderStatFiltration(String type) {
        ImageExtension();
        syncMatrix();
        int[] sorted = new int[(int) Math.pow(2 * size + 1, 2)];
        int[] temprgb = new int[3];
        for (int i = size; i < width - size; i++) {
            for (int j = size; j < height - size; j++) {
                for (int k = 0; k < 3; k++) {
                    for (int m = -size; m <= size; m++) {
                        for (int n = -size; n <= size; n++) {
                            sorted[n + size + (m + size) * (2 * size + 1)] = tempMat[i + m][j + n][k];
                        }
                    }
                    Arrays.sort(sorted);
                    if (type.equals("min")) {
                        temprgb[k] = sorted[0];
                    }
                    if (type.equals("max")) {
                        temprgb[k] = sorted[sorted.length - 1];
                    }
                    if (type.equals("avg")) {
                        temprgb[k] = (sorted[0] + sorted[sorted.length - 1]) / 2;
                    }
                    if (type.equals("median")) {
                        temprgb[k] = sorted[sorted.length / 2];
                    }


                }
                Result.setRGB(i - size, j - size, new Color(temprgb[0], temprgb[1], temprgb[2]).getRGB());
            }
        }
        return Result;
    }
    public BufferedImage AdaptiveMedianFiltration() {
        ImageExtension();
        syncMatrix();
        int[] temprgb = new int[3];
        int adaptive_size;
        int A1, A2, B1, B2;
        for (int i = size; i < width - size; i++) {
            for (int j = size; j < height - size; j++) {
                for (int k = 0; k < 3; k++) {
                    adaptive_size = 1;
                    while (true) {
                        int[] sorted = new int[(int) Math.pow(2 * adaptive_size + 1, 2)];
                        for (int m = -adaptive_size; m <= adaptive_size; m++) {
                            for (int n = -adaptive_size; n <= adaptive_size; n++) {
                                sorted[n + adaptive_size + (m + adaptive_size) * (2 * adaptive_size + 1)] = tempMat[i + m][j + n][k];
                            }
                        }
                        Arrays.sort(sorted);
                        A1 = sorted[sorted.length / 2] - sorted[0];
                        A2 = sorted[sorted.length / 2] - sorted[sorted.length - 1];
                        if (A1 > 0 && A2 < 0) {
                            B1 = tempMat[i][j][k]-sorted[0];
                            B2 = tempMat[i][j][k]-sorted[sorted.length - 1];
                            if (B1 > 0 && B2 < 0){
                                temprgb[k] = tempMat[i][j][k];
                            }else{
                                temprgb[k] = sorted[sorted.length/2];
                            }
                            break;
                        } else {
                            adaptive_size += 1;
                            if(adaptive_size > size){
                                temprgb[k] = sorted[sorted.length/2];
                                break;
                            }
                            continue;
                        }
                    }
                }
                Result.setRGB(i - size, j - size, new Color(temprgb[0], temprgb[1], temprgb[2]).getRGB());
            }
        }
        return Result;
    }

}
