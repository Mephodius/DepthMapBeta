

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.sql.SQLOutput;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.LinkedList;
import javax.swing.JPanel;

public class PlotVisualizator extends JPanel {
    // Список координат точек для построения графика
    private double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;
    //private boolean showNumeration = true;
    private boolean turnGraph = false;
    private boolean showIntegrals = false;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scale;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke lineStroke;
    // Различные шрифты отображения надписей
    private Font axisFont;
    private Font smallfont;

    public PlotVisualizator() {
// Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
// Сконструировать необходимые объекты, используемые в рисовании
// Перо для рисования графика
        lineStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, new float[]{10, 3}, 0.0f);
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
// Перо для рисования осей координат
        axisStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Шрифт для подписей осей координат
        axisFont = new Font("Serif", Font.BOLD, 36);
        smallfont = new Font("SansSerif", Font.ITALIC, 12);
    }

    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void showGraphics(double[][] graphicsData) {
// Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
        repaint();
    }

    // Методы-модификаторы для изменения параметров отображения графика
// Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    /* public void setNumeration(boolean showNumeration) {
         this.showNumeration = showNumeration;
         repaint();
     }*/
    public void setTurnGrid(boolean turnGraph) {
        this.turnGraph = turnGraph;
        System.out.println(turnGraph);
        repaint();
    }

    public void setShowIntegrals(boolean showIntegrals) {
        this.showIntegrals = showIntegrals;
        repaint();
    }

    // Метод отображения всего компонента, содержащего график
    public void paintComponent(Graphics g) {
        /* Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
         * Эта функциональность - единственное, что осталось в наследство от
         * paintComponent класса JPanel
         */
        super.paintComponent(g);
// Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
        if (graphicsData == null || graphicsData.length == 0) return;
// Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
// Это необходимо для определения области пространства, подлежащей отображению
// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;
// Найти минимальное и максимальное значение функции
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }
/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X
и Y - сколько пикселов
* приходится на единицу длины по X и по Y
*/

        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);
// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
// Выбираем за основу минимальный
        scale = Math.min(scaleX, scaleY);
// Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
        if (scale == scaleX) {
/* Если за основу был взят масштаб по оси X, значит по оси Y
делений меньше,
* т.е. подлежащий визуализации диапазон по Y будет меньше
высоты окна.
* Значит необходимо добавить делений, сделаем это так:
* 1) Вычислим, сколько делений влезет по Y при выбранном
масштабе - getSize().getHeight()/scale
* 2) Вычтем из этого сколько делений требовалось изначально
* 3) Набросим по половине недостающего расстояния на maxY и
minY
*/
            double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {
// Если за основу был взят масштаб по оси Y, действовать по аналогии
            double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }
// Шаг 7 - Сохранить текущие настройки холста
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
// Первыми (если нужно) отрисовываются оси координат.
        paintAxis(canvas);
// Затем отображается сам график
        paintGraphics(canvas);
// Затем (если нужно) отображаются маркеры точек, по которым строился график.
        //paintIntegrals(canvas);
        paintMarkers(canvas);
// Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    // Отрисовка графика по прочитанным координатам
    protected void paintGraphics(Graphics2D canvas) {
// Выбрать линию для рисования графика
        canvas.setStroke(graphicsStroke);
// Выбрать цвет линии
        canvas.setColor(Color.blue);
/* Будем рисовать линию графика как путь, состоящий из множества
сегментов (GeneralPath)
* Начало пути устанавливается в первую точку графика, после чего
прямой соединяется со
* следующими точками
*/
        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
// Преобразовать значения (x,y) в точку на экране point
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i > 0) {
// Не первая итерация цикла - вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else {
// Первая итерация цикла - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }
// Отобразить график
        canvas.draw(graphics);
    }

    // Отображение маркеров точек, по которым рисовался график
    protected void paintMarkers(Graphics2D canvas) {
// Шаг 1 - Установить специальное перо для черчения контуров маркеров
        canvas.setStroke(markerStroke);
// Выбрать красный цвета для контуров маркеров
        canvas.setColor(Color.ORANGE);
// Выбрать красный цвет для закрашивания маркеров внутри
        canvas.setPaint(Color.ORANGE);
        int size = 3;
        double max_correlation = 0;
        double eps = 0.0001;
// Шаг 2 - Организовать цикл по всем точкам графика
        for (double[] point: graphicsData){
            if (point[1] > max_correlation){
                max_correlation = point[1];
            }
        }
        for (double[] point : graphicsData) {
            size = 3;
            if (Math.abs(point[1] - max_correlation) < eps){
                size *= 2;

                canvas.setColor(Color.BLACK);
                canvas.setPaint(Color.BLACK);
                canvas.setStroke(lineStroke);

                DecimalFormat df = new DecimalFormat("#.##");
                FontRenderContext context = canvas.getFontRenderContext();
                Rectangle2D bounds = axisFont.getStringBounds(df.format((point[1])/100), context);
                Point2D.Double start = xyToPoint(0, 0);
                canvas.drawString(df.format((point[1])/100), (float) start.getX() + 10,
                        (float) (xyToPoint(point[0], point[1]).y - bounds.getY()/2 - 5));

                canvas.draw(new Line2D.Double(xyToPoint(point[0], point[1]),
                        xyToPoint(0, point[1])));

                bounds = axisFont.getStringBounds(Integer.toString((int)(point[0])), context);
                canvas.drawString(Integer.toString((int)(point[0])), (float) (xyToPoint(point[0], point[1]).x - 30),
                        (float) (start.getY() - bounds.getY() - 5));

                canvas.draw(new Line2D.Double(xyToPoint(point[0], point[1]),
                        xyToPoint(point[0], 0)));
            }
// Инициализировать эллипс как объект для представления маркера
            Ellipse2D.Double marker = new Ellipse2D.Double();
            Point2D.Double center = xyToPoint(point[0], point[1]);
            //line.setLine();
// Угол прямоугольника - отстоит на расстоянии (3,3)
            Point2D.Double corner = shiftPoint(center, size, size);
// Задать эллипс по центру и диагонали
            marker.setFrameFromCenter(center, corner);


            Line2D.Double line = new Line2D.Double(shiftPoint(center, -size, 0), shiftPoint(center, size, 0));
            canvas.draw(line);
            line.setLine(shiftPoint(center, 0, -size), shiftPoint(center, 0, size));
            canvas.draw(line);
            canvas.draw(marker); // Начертить контур маркера
            canvas.setColor(Color.ORANGE);
/* Эллипс будет задаваться посредством указания координат
его центра
и угла прямоугольника, в который он вписан */
// Центр - в точке (x,y)

        }
    }

    // Метод, обеспечивающий отображение осей координат
    protected void paintAxis(Graphics2D canvas) {
// Установить особое начертание для осей
        canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
        canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
        canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
// Определить, должна ли быть видна ось Y на графике
        if (minX <= 0.0 && maxX >= 0.0) {
// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
// а правая (maxX) >= 0.0
// Сама ось - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY),
                    xyToPoint(0, minY)));
// Стрелка оси Y
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на верхний конец оси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5,
                    arrow.getCurrentPoint().getY() + 20);
// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10,
                    arrow.getCurrentPoint().getY());
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи "y"
            Rectangle2D bounds = axisFont.getStringBounds("Correlation", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("Correlation", (float) labelPos.getX() + 10,
                    (float) (labelPos.getY() - bounds.getY()));
        }
// Определить, должна ли быть видна ось X на графике
        if (minY <= 0.0 && maxY >= 0.0) {
// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
// а нижняя (minY) <= 0.0
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                    xyToPoint(maxX, 0)));
// Стрелка оси X
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на правый конец оси X
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20,
                    arrow.getCurrentPoint().getY() - 5);
// Вести левую часть стрелки в точку с относительными координатами (0, 10)
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY() + 10);
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи "x"
            Rectangle2D bounds = axisFont.getStringBounds("Deviation", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("Deviation", (float) (labelPos.getX() -
                    bounds.getWidth() - 10), (float) (labelPos.getY() - bounds.getY()));
        }
    }

    /* Метод-помощник, осуществляющий преобразование координат.
    * Оно необходимо, т.к. верхнему левому углу холста с координатами
    * (0.0, 0.0) соответствует точка графика с координатами (minX, maxY),
    где
    * minX - это самое "левое" значение X, а
    * maxY - самое "верхнее" значение Y.
    */
    //замыкает области, пересекающие ось Ox, а также считает приблизительно значения площадей этих областей
    protected void paintIntegrals(Graphics2D canvas) {
        LinkedList<Integer> indexses = new LinkedList<>();
        Double domens = 0.0;
        GeneralPath path = new GeneralPath();
        for (int i = 0; i < graphicsData.length - 1; i++) {
            System.out.println("X: " + graphicsData[i][0] + " Y: " + graphicsData[i][1] + " i: " + i);
            if ((graphicsData[i][1] < 0 == graphicsData[i + 1][1] >= 0) || (graphicsData[i][1] == 0)) {
                if (domens != 0) {
                    domens += 1;
                    if (graphicsData[i][1] == 0)
                        indexses.add(i - 1);
                    else
                        indexses.add(i);
                    indexses.add(i);
                    System.out.println("End+Start");
                    if(graphicsData[i+1][1]==0) {
                        i++;
                    }
                    System.out.println("X: " + graphicsData[i][0] + " Y: " + graphicsData[i][1] + " i: " + i);
                    continue;
                } else {
                    indexses.add(i);
                    System.out.println("Start");
                    domens += 0.5;
                }

            }
            if (domens.intValue() == domens) {
                //  System.out.println("end");
            } else {
                //System.out.println("start");
            }
        }
        LinkedList<Double> xcoordinates = new LinkedList<>();
        for (int i = 0; i < 2 * domens.intValue(); i++) {
            //формулка для рассчета точки пересейчения прямой с осью координат по известным двум точкам
            xcoordinates.add(-graphicsData[indexses.get(i)][1] / (graphicsData[indexses.get(i) + 1][1] - graphicsData[indexses.get(i)][1]) * (graphicsData[indexses.get(i) + 1][0] - graphicsData[indexses.get(i)][0]) + graphicsData[indexses.get(i)][0]);
            System.out.println("Координата x пересечения c Ox с индексом " + i + " " + xcoordinates.get(i) + " на интервале от " + indexses.get(i) + " до " + (indexses.get(i) + 1));
        }

        int k = 0;
        Double[] integral = new Double[xcoordinates.size() / 2];
        for (int i = 0; i < xcoordinates.size() / 2; i++) {
            integral[i] = 0.0;
        }
        Double maxy = 0.0;
        Double miny = 0.0;
        Double[] averagey = new Double[xcoordinates.size() / 2];
        for (int i = 0; i < graphicsData.length; i++) {
            // System.out.println("INDEX: "+ i+ " left " +xcoordinates.get(k)+"<="+graphicsData[i][0]+"<"+xcoordinates.get(k+1)+ " ? ");
            if (graphicsData[i][0] >= xcoordinates.get(k) && graphicsData[i][0] < xcoordinates.get(k + 1)) {
// Преобразовать значения (x,y) в точку на экране point
                if (maxy < graphicsData[i][1]) {
                    maxy = graphicsData[i][1];
                }
                if (miny > graphicsData[i][1]) {
                    miny = graphicsData[i][1];
                }
                if (graphicsData[i - 1][0] <= xcoordinates.get(k)) {
// Первая итерация цикла - установить начало пути в точку point
                    integral[k / 2] += Math.abs((graphicsData[i][0] - xcoordinates.get(k)) * graphicsData[i][1] / 2);
                    canvas.setColor(Color.red);
                    Point2D.Double point = xyToPoint(xcoordinates.get(k), 0);
                    path.moveTo(point.getX(), point.getY());
                    System.out.println("The line moved to its initial position, x = " + point.getX() + " on the itaration i = " + i);
                    point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                    path.lineTo(point.getX(), point.getY());
                }
                if (graphicsData[i + 1][0] >= xcoordinates.get(k + 1)) {
// Не первая итерация цикла - вести линию в точку point
                    Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                    path.lineTo(point.getX(), point.getY());
                    point = xyToPoint(xcoordinates.get(k + 1), 0);
                    path.lineTo(point.getX(), point.getY());
                    integral[k / 2] += Math.abs(graphicsData[i][1] / 2 * (xcoordinates.get(k + 1) - graphicsData[i][0]));
                    path.closePath();
                    System.out.println("The line was closed , x = " + point.getX() + " on the itaration i = " + i);
                    canvas.fill(path);
                    canvas.draw(path);
                    if (maxy == 0.0)
                        averagey[k / 2] = miny;
                    else
                        averagey[k / 2] = maxy;
                    if (k >= xcoordinates.size() - 2) break;
                    k += 2;
                    maxy = 0.0;
                    miny = 0.0;
                }
                if(!(graphicsData[i + 1][0] >= xcoordinates.get(k + 1))&&!(graphicsData[i - 1][0] <= xcoordinates.get(k))) {
                    integral[k / 2] += Math.abs((graphicsData[i][0] - graphicsData[i - 1][0]) * (graphicsData[i][1] + graphicsData[i - 1][1]) / 2);
                    Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                    path.lineTo(point.getX(), point.getY());
                }

            }
        }
        System.out.println("Integral" + (int) (k / 2) + " = " + integral[k / 2]);

        canvas.setFont(smallfont);
        FontRenderContext context = canvas.getFontRenderContext();
        for (int i = 0; i < xcoordinates.size() / 2; i++) {
            canvas.setColor(Color.black);
            Rectangle2D bounds = smallfont.getStringBounds(String.format("%.3f", integral[i]), context);
            System.out.println(bounds.getX());
            //canvas.drawString(String.format("%.3f", integral[i]), (float) (xyToPoint(xcoordinates.get(2 * i) + (xcoordinates.get(2 * i + 1) - xcoordinates.get(2 * i)) / 2 - bounds.getX(), averagey[i] / 2).getX()), (float) xyToPoint(xcoordinates.get(2 * i) + (xcoordinates.get(2 * i + 1) - xcoordinates.get(2 * i)) / 2, averagey[i] / 2).getY());
        }
    }
// Отобразить график


    protected Point2D.Double xyToPoint(double x, double y) {
// Вычисляем смещение X от самой левой точки (minX)
        double deltaX = x - minX;
// Вычисляем смещение Y от точки верхней точки (maxY)
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    /* Метод-помощник, возвращающий экземпляр класса Point2D.Double
     * смещѐнный по отношению к исходному на deltaX, deltaY
     * К сожалению, стандартного метода, выполняющего такую задачу, нет.
     */
    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
// Инициализировать новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
// Задать еѐ координаты как координаты существующей точки + заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
}
