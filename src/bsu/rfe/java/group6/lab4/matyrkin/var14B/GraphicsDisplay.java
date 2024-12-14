package bsu.rfe.java.group6.lab4.matyrkin.var14B;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    // Список координат точек для построения графика
    private ArrayList<Double[]> graphicsData;// Данные графика
    private ArrayList<Double[]> originalData;// Исходные данные
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private int selectedMarker = -1;// Индекс выбранной точки
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Текущая область видимости графика
    private double[][] viewport = new double[2][2];
    private ArrayList<double[][]> undoHistory = new ArrayList(5);// История изменений масштаба

    private double scaleX;
    private double scaleY;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke selectionStroke;// Толщина линии для выделения области
    private Font labelsFont;// Шрифт для подписей чисел на осях
    // Различные шрифты отображения надписей
    private Font axisFont;
    // Формат чисел (для отображения координат)
    private static DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance();
    private boolean scaleMode = false;// Режим масштабирования
    private boolean changeMode = false;// Режим изменения точки
    private double[] originalPoint = new double[2];// Начальная точка для масштабирования
    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();

    public GraphicsDisplay() {
// Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
// Сконструировать необходимые объекты, используемые в рисовании
// Перо для рисования графика
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
// Перо для рисования осей координат
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        this.selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[]{10.0F, 10.0F}, 0.0F);

        // Шрифт для подписей осей координат

        axisFont = new Font("Serif", Font.BOLD, 36);
        this.labelsFont = new Font("Serif", 0, 10);
        this.addMouseListener(new MouseHandler());
        this.addMouseMotionListener(new MouseMotionHandler());
    }

    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void displayGraphics(ArrayList<Double[]> graphicsData) {
// Сохраняем оригинальные данные для возможности сброса
        this.graphicsData = graphicsData;
        this.originalData = new ArrayList(graphicsData.size());
        Iterator var3 = graphicsData.iterator();

        while(var3.hasNext()) {
            Double[] point = (Double[])var3.next();
            Double[] newPoint = new Double[]{new Double(point[0]), new Double(point[1])};
            this.originalData.add(newPoint);
        }
        // Вычисляем минимальные и максимальные значения по осям
        this.minX = ((Double[])graphicsData.get(0))[0];
        this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0];
        this.minY = ((Double[])graphicsData.get(0))[1];
        this.maxY = this.minY;

        for(int i = 1; i < graphicsData.size(); ++i) {
            if (((Double[])graphicsData.get(i))[1] < this.minY) {
                this.minY = ((Double[])graphicsData.get(i))[1];
            }

            if (((Double[])graphicsData.get(i))[1] > this.maxY) {
                this.maxY = ((Double[])graphicsData.get(i))[1];
            }
        }
        // Устанавливаем начальную область видимости (весь график)
        this.zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }

    // Устанавливаем новую область отображения
    public void zoomToRegion(double x1, double y1, double x2, double y2) {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        this.repaint(); // Перерисовываем компонент
    }
    // Метод для рисования графика
    public void paintComponent(Graphics g) {
        // Очищаем область рисования
        super.paintComponent(g);
        // Вычисляем коэффициенты масштабирования
        this.scaleX = this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        this.scaleY = this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
        // Если данные для графика существуют, начинаем отрисовку
        if (this.graphicsData != null && this.graphicsData.size() != 0) {
            Graphics2D canvas = (Graphics2D)g;
            this.paintAxis(canvas); // Рисуем оси координат
            this.paintGraphics(canvas);
            this.paintMarkers(canvas);
            this.paintLabels(canvas); // Рисуем подписи
            this.paintSelection(canvas); // Рисуем выделение (если есть)
        }
    }
    private void paintSelection(Graphics2D canvas) {
        if (this.scaleMode) {
            canvas.setStroke(this.selectionStroke);
            canvas.setColor(Color.BLACK);
            canvas.draw(this.selectionRect);
        }
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

    // Метод отображения всего компонента, содержащего график


    // Рисуем сам график, соединяя точки линиями.
    private void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);// Устанавливаем стиль линии для графика
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        Iterator var5 = this.graphicsData.iterator();
        // Перебираем все точки графика
        while(var5.hasNext()) {
            Double[] point = (Double[])var5.next();
            // Проверяем, находится ли точка в пределах текущей области видимости (viewport)
            if (!(point[0] < this.viewport[0][0]) && !(point[1] > this.viewport[0][1]) && !(point[0] > this.viewport[1][0]) && !(point[1] < this.viewport[1][1])) {
                // Если есть предыдущая точка, соединяем её с текущей линией
                if (currentX != null && currentY != null) {
                    canvas.draw(new Line2D.Double(this.translateXYtoPoint(currentX, currentY), this.translateXYtoPoint(point[0], point[1])));
                }
                // Обновляем текущую точку
                currentX = point[0];
                currentY = point[1];
            }
        }

    }
    protected Point2D.Double translateXYtoPoint(double x, double y) {
        double deltaX = x - this.viewport[0][0];// Смещение по X
        double deltaY = this.viewport[0][1] - y;// Смещение по Y
        return new Point2D.Double(deltaX * this.scaleX, deltaY * this.scaleY);// Возвращаем координаты в пикселях
    }

    protected double[] translatePointToXY(int x, int y) {
        // Переводим координаты пикселей в координаты в области просмотра
        return new double[]{this.viewport[0][0] + (double)x / this.scaleX, this.viewport[0][1] - (double)y / this.scaleY};
    }
    // Отображение маркеров точек, по которым рисовался график
    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);

        for (Double[] point : graphicsData) {
            Point2D center = this.translateXYtoPoint(point[0], point[1]);

            // Проверка условия для выделения маркера другим цветом
            int integerPart = (int) Math.floor(point[1]);
            int digitSum = sumOfDigits(integerPart);

            if (digitSum < 10) {
                canvas.setColor(Color.GREEN); // Цвет маркера при выполнении условия
            } else {
                canvas.setColor(Color.RED); // Обычный цвет маркера
            }


            int x = (int) center.getX(); // Центр маркера по X
            int y = (int) center.getY(); // Центр маркера по Y

            // Рисуем пиксели, соответствующие фигуре маркера

            // 6-я строка, 1 и 11 столбцы
            canvas.fillRect(x - 5, y, 1, 1);
            canvas.fillRect(x + 5, y, 1, 1);

            // 5-я и 7-я строки, 2-3 и 9-10 столбцы
            canvas.fillRect(x - 4, y - 1, 1, 1);
            canvas.fillRect(x - 3, y - 1, 1, 1);
            canvas.fillRect(x + 3, y - 1, 1, 1);
            canvas.fillRect(x + 4, y - 1, 1, 1);

            canvas.fillRect(x - 4, y + 1, 1, 1);
            canvas.fillRect(x - 3, y + 1, 1, 1);
            canvas.fillRect(x + 3, y + 1, 1, 1);
            canvas.fillRect(x + 4, y + 1, 1, 1);

            // 4-я и 8-я строки, 4-5 и 7-8 столбцы
            canvas.fillRect(x - 2, y - 2, 1, 1);
            canvas.fillRect(x - 1, y - 2, 1, 1);
            canvas.fillRect(x + 1, y - 2, 1, 1);
            canvas.fillRect(x + 2, y - 2, 1, 1);

            canvas.fillRect(x - 2, y + 2, 1, 1);
            canvas.fillRect(x - 1, y + 2, 1, 1);
            canvas.fillRect(x + 1, y + 2, 1, 1);
            canvas.fillRect(x + 2, y + 2, 1, 1);

            // 2-я и 10-я строки, 5 и 7 столбцы
            canvas.fillRect(x - 1, y - 4, 1, 1);
            canvas.fillRect(x + 1, y - 4, 1, 1);

            canvas.fillRect(x - 1, y + 4, 1, 1);
            canvas.fillRect(x + 1, y + 4, 1, 1);

            // 1-я и 11-я строки, 6-й столбец
            canvas.fillRect(x, y - 5, 1, 1);
            canvas.fillRect(x, y + 5, 1, 1);

            // 3-я и 11-я строки, 5 и 7 столбцы
            canvas.fillRect(x - 1, y - 3, 1, 1);
            canvas.fillRect(x + 1, y - 3, 1, 1);

            canvas.fillRect(x - 1, y + 3, 1, 1);
            canvas.fillRect(x + 1, y + 3, 1, 1);
        }
    }
    private void paintLabels(Graphics2D canvas) {
        canvas.setColor(Color.BLACK);// Цвет меток — чёрный
        canvas.setFont(this.labelsFont);// Устанавливаем шрифт для меток
        FontRenderContext context = canvas.getFontRenderContext();
        // Определяем положение меток на осях
        double labelYPos;
        if (this.viewport[1][1] < 0.0 && this.viewport[0][1] > 0.0) {
            labelYPos = 0.0;
        } else {
            labelYPos = this.viewport[1][1];
        }

        double labelXPos;
        if (this.viewport[0][0] < 0.0 && this.viewport[1][0] > 0.0) {
            labelXPos = 0.0;
        } else {
            labelXPos = this.viewport[0][0];
        }

        // Рисуем метки на оси X
        double pos = this.viewport[0][0];

        double step;
        Point2D.Double point;
        String label;
        Rectangle2D bounds;
        for(step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0; pos < this.viewport[1][0]; pos += step) {
            point = this.translateXYtoPoint(pos, labelYPos);
            label = formatter.format(pos);// Форматируем значение
            bounds = this.labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() - bounds.getHeight()));
        }

        // Рисуем метки на оси Y
        pos = this.viewport[1][1];

        for(step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0; pos < this.viewport[0][1]; pos += step) {
            point = this.translateXYtoPoint(labelXPos, pos);
            label = formatter.format(pos);
            bounds = this.labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() - bounds.getHeight()));
        }
        // Рисуем координаты выделенной точки
        if (this.selectedMarker >= 0) {
            point = this.translateXYtoPoint(((Double[])this.graphicsData.get(this.selectedMarker))[0], ((Double[])this.graphicsData.get(this.selectedMarker))[1]);
            label = "X=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[0]) + ", Y=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[1]);
            bounds = this.labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLUE);
            canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() - bounds.getHeight()));
        }

    }
    // Метод для вычисления суммы цифр целой части числа
    private int sumOfDigits(int number) {
        number = Math.abs(number);
        int sum = 0;
        while (number > 0) {
            sum += number % 10;
            number /= 10;
        }
        return sum;
    }


    protected int findSelectedPoint(int x, int y) {
        if (this.graphicsData == null) {
            return -1;// Если данных нет, возвращаем -1
        } else {
            int pos = 0;

            for(Iterator var5 = this.graphicsData.iterator(); var5.hasNext(); ++pos) {
                Double[] point = (Double[])var5.next();// Получаем следующую точку
                // Переводим в экранные координаты
                Point2D.Double screenPoint = this.translateXYtoPoint(point[0], point[1]);
                // Вычисляем квадрат расстояния до курсора
                double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + (screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);
                if (distance < 100.0) {// Если курсор близко к точке
                    return pos;// Возвращаем индекс точки
                }
            }

            return -1;// Если не нашли, возвращаем -1
        }
    }
    public void reset() {
        this.displayGraphics(this.originalData);// Сброс отображаемых данных
    }

    private boolean showIntegerPart = false; // Флаг отображения графика "целая часть"

    public void setShowIntegerPart(boolean showIntegerPart) {
        this.showIntegerPart = showIntegerPart;
        repaint();
    }
    // Метод, обеспечивающий отображение осей координат
    private void paintAxis(Graphics2D canvas) {
        canvas.setStroke(this.axisStroke);// Установка стиля обводки для осей
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.axisFont);// Установка шрифта для подписей осей
        FontRenderContext context = canvas.getFontRenderContext();// Получение контекста для рендеринга шрифта
        Rectangle2D bounds;// Для хранения границ текста
        Point2D.Double labelPos;// Позиция для подписи
        // Рисуем ось Y, если она пересекает область просмотра
        if (this.viewport[0][0] <= 0.0 && this.viewport[1][0] >= 0.0) {
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(0.0, this.viewport[0][1]), this.translateXYtoPoint(0.0, this.viewport[1][1])));
            // Рисуем стрелки на оси Y
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(-(this.viewport[1][0] - this.viewport[0][0]) * 0.0025, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015), this.translateXYtoPoint(0.0, this.viewport[0][1])));
            canvas.draw(new Line2D.Double(this.translateXYtoPoint((this.viewport[1][0] - this.viewport[0][0]) * 0.0025, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015), this.translateXYtoPoint(0.0, this.viewport[0][1])));
            bounds = this.axisFont.getStringBounds("y", context);// Получаем границы для подписи
            labelPos = this.translateXYtoPoint(0.0, this.viewport[0][1]);// Позиция подписи
            // Рисуем подпись
            canvas.drawString("y", (float)labelPos.x + 10.0F, (float)(labelPos.y + bounds.getHeight() / 2.0));
        }

        // Рисуем ось X, если она пересекает область просмотра
        if (this.viewport[1][1] <= 0.0 && this.viewport[0][1] >= 0.0) {
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], 0.0), this.translateXYtoPoint(this.viewport[1][0], 0.0)));
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01, (this.viewport[0][1] - this.viewport[1][1]) * 0.005), this.translateXYtoPoint(this.viewport[1][0], 0.0)));
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01, -(this.viewport[0][1] - this.viewport[1][1]) * 0.005), this.translateXYtoPoint(this.viewport[1][0], 0.0)));
            bounds = this.axisFont.getStringBounds("x", context);// Получаем границы для подписи
            labelPos = this.translateXYtoPoint(this.viewport[1][0], 0.0);// Позиция подписи
            canvas.drawString("x", (float)(labelPos.x - bounds.getWidth() - 10.0), (float)(labelPos.y - bounds.getHeight() / 2.0));// Рисуем подпись
        }

    }

    /* Метод-помощник, осуществляющий преобразование координат.
    * Оно необходимо, т.к. верхнему левому углу холста с координатами
    * (0.0, 0.0) соответствует точка графика с координатами (minX, maxY),
    где
    * minX - это самое "левое" значение X, а
    * maxY - самое "верхнее" значение Y.
    */


    /* Метод-помощник, возвращающий экземпляр класса Point2D.Double
     * смещѐнный по отношению к исходному на deltaX, deltaY
     * К сожалению, стандартного метода, выполняющего такую задачу, нет.
     */

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX,
                                        double deltaY) {
// Инициализировать новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
// Задать еѐ координаты как координаты существующей точки + заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
    // Обработчик событий мыши
    public class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }

        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == 3) {// Если нажата правая кнопка мыши
                if (GraphicsDisplay.this.undoHistory.size() > 0) {
                    // Восстанавливаем последнее состояние области просмотра
                    GraphicsDisplay.this.viewport = (double[][])GraphicsDisplay.this.undoHistory.get(GraphicsDisplay.this.undoHistory.size() - 1);
                    GraphicsDisplay.this.undoHistory.remove(GraphicsDisplay.this.undoHistory.size() - 1);
                } else {
                    // Если нет истории, выполняем зум на определённую область
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.minX, GraphicsDisplay.this.maxY, GraphicsDisplay.this.maxX, GraphicsDisplay.this.minY);
                }

                GraphicsDisplay.this.repaint();// Перерисовываем график
            }

        }

        public void mousePressed(MouseEvent ev) {
            if (ev.getButton() == 1) {// Если нажата левая кнопка мыши
                GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());// Находим выбранную точку
                GraphicsDisplay.this.originalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());// Запоминаем исходную точку
                if (GraphicsDisplay.this.selectedMarker >= 0) {// Если точка выбрана
                    GraphicsDisplay.this.changeMode = true;// Переключаем режим изменения
                    GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));// Устанавливаем курсор для изменения
                } else {
                    GraphicsDisplay.this.scaleMode = true;// Включаем режим масштабирования
                    GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));// Устанавливаем курсор для масштабирования
                    GraphicsDisplay.this.selectionRect.setFrame((double)ev.getX(), (double)ev.getY(), 1.0, 1.0);// Устанавливаем прямоугольник выделения
                }

            }
        }

        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() == 1) {// Если левая кнопка мыши отпущена
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));// Сбрасываем курсор
                if (GraphicsDisplay.this.changeMode) {// Если в режиме изменения
                    GraphicsDisplay.this.changeMode = false;
                } else {
                    GraphicsDisplay.this.scaleMode = false;
                    double[] finalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());// Получаем конечную точку
                    GraphicsDisplay.this.undoHistory.add(GraphicsDisplay.this.viewport);// Сохраняем текущее состояние в историю
                    GraphicsDisplay.this.viewport = new double[2][2];// Создаём новую область просмотра
                    // Зумируем на выделенную область
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.originalPoint[0], GraphicsDisplay.this.originalPoint[1], finalPoint[0], finalPoint[1]);
                    GraphicsDisplay.this.repaint();// Перерисовываем график
                }

            }
        }
    }
    // Обработчик движения мыши
    public class MouseMotionHandler implements MouseMotionListener {
        public MouseMotionHandler() {
        }

        public void mouseMoved(MouseEvent ev) {
            // Находим выбранную точку
            GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
            if (GraphicsDisplay.this.selectedMarker >= 0) {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));// Если выбрана точка, устанавливаем курсор изменения
            } else {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));// В противном случае сбрасываем курсор
            }

            GraphicsDisplay.this.repaint();// Перерисовываем график
        }

        public void mouseDragged(MouseEvent ev) {
            if (GraphicsDisplay.this.changeMode) {// Если в режиме изменения
                double[] currentPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());// Получаем текущую точку
                // Изменяем координату Y
                double newY = ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1] + (currentPoint[1] - ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1]);
                if (newY > GraphicsDisplay.this.viewport[0][1]) {
                    newY = GraphicsDisplay.this.viewport[0][1];
                }
                // Проверяем границы по Y
                if (newY < GraphicsDisplay.this.viewport[1][1]) {
                    newY = GraphicsDisplay.this.viewport[1][1];
                }

                ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1] = newY;// Обновляем координаты точки
                GraphicsDisplay.this.repaint();// Перерисовываем график
            } else {
                // Обновляем размеры прямоугольника выделения
                double width = (double)ev.getX() - GraphicsDisplay.this.selectionRect.getX();
                if (width < 5.0) {
                    width = 5.0;
                }

                double height = (double)ev.getY() - GraphicsDisplay.this.selectionRect.getY();
                if (height < 5.0) {
                    height = 5.0;
                }

                GraphicsDisplay.this.selectionRect.setFrame(GraphicsDisplay.this.selectionRect.getX(), GraphicsDisplay.this.selectionRect.getY(), width, height);// Устанавливаем новые размеры
                GraphicsDisplay.this.repaint();// Перерисовываем график
            }

        }
    }
}