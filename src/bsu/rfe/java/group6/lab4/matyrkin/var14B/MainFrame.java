package bsu.rfe.java.group6.lab4.matyrkin.var14B;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    // Начальные размеры окна приложения
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
// Объект диалогового окна для выбора файлов
private JFileChooser fileChooser = null;
    // Пункты меню
    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;
    // Компонент-отображатель графика
    private GraphicsDisplay display = new GraphicsDisplay();
    // Флаг, указывающий на загруженность данных графика
    private boolean fileLoaded = false;
    public MainFrame() {
// Вызов конструктора предка Frame
        super("Построение графиков функций на основе заранее подготовленных файлов");
// Установка размеров окна
                setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
// Отцентрировать окно приложения на экране
        setLocation((kit.getScreenSize().width - WIDTH)/2,
                (kit.getScreenSize().height - HEIGHT)/2);
// Развѐртывание окна на весь экран
        setExtendedState(MAXIMIZED_BOTH);
// Создать и установить полосу меню
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
// Добавить пункт меню "Файл"
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
// Создать действие по открытию файла
        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") {
        public void actionPerformed(ActionEvent event) {
            if (fileChooser==null) {
                fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
            }
            if (fileChooser.showOpenDialog(MainFrame.this) ==
                    JFileChooser.APPROVE_OPTION)
                openGraphics(fileChooser.getSelectedFile());
        }
    };
// Добавить соответствующий элемент меню
fileMenu.add(openGraphicsAction);
    // Создать пункт меню "График"
    JMenu graphicsMenu = new JMenu("График");
menuBar.add(graphicsMenu);
    // Создать действие для реакции на активацию элемента "Показыватьоси координат"
    Action showAxisAction = new AbstractAction("Показывать оси координат") {
    public void actionPerformed(ActionEvent event) {
// свойство showAxis класса GraphicsDisplay истина, если элемент меню
// showAxisMenuItem отмечен флажком, и ложь - в противном случае
        display.setShowAxis(showAxisMenuItem.isSelected());
    }
};
        // Создать действие для отображения графика "целая часть f(x)"
        Action showIntegerPartAction = new AbstractAction("Показывать график \"целая часть f(x)\"") {
            public void actionPerformed(ActionEvent event) {
                display.setShowIntegerPart(((JCheckBoxMenuItem) event.getSource()).isSelected());
            }
        };
        JCheckBoxMenuItem showIntegerPartMenuItem = new JCheckBoxMenuItem(showIntegerPartAction);
        graphicsMenu.add(showIntegerPartMenuItem);
        showIntegerPartMenuItem.setSelected(false);
showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
// Добавить соответствующий элемент в меню
graphicsMenu.add(showAxisMenuItem);
// Элемент по умолчанию включен (отмечен флажком)
showAxisMenuItem.setSelected(true);
// Повторить действия для элемента "Показывать маркеры точек"
Action showMarkersAction = new AbstractAction("Показывать маркеры  точек") {
public void actionPerformed(ActionEvent event) {
// по аналогии с showAxisMenuItem
    display.setShowMarkers(showMarkersMenuItem.isSelected());
}
};
showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
graphicsMenu.add(showMarkersMenuItem);
// Элемент по умолчанию включен (отмечен флажком)
showMarkersMenuItem.setSelected(true);
// Зарегистрировать обработчик событий, связанных с меню "График"
graphicsMenu.addMenuListener(new GraphicsMenuListener());
// Установить GraphicsDisplay в цент граничной компоновки
getContentPane().add(display, BorderLayout.CENTER);
}
// Считывание данных графика из существующего файла
// Метод для открытия и чтения данных из файла
protected void openGraphics(File selectedFile) {
    try {
        // Открываем файл для чтения
        DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
        ArrayList<Double[]> graphicsData = new ArrayList(50);

        // Читаем данные из файла
        while(in.available() > 0) {
            Double x = in.readDouble();
            Double y = in.readDouble();
            graphicsData.add(new Double[]{x, y});// Добавляем точку в список
        }

        // Если данные успешно считаны
        if (graphicsData.size() > 0) {
            this.fileLoaded = true;
            this.display.displayGraphics(graphicsData);// Передаем данные в панель для отображения
        }

    } catch (FileNotFoundException var6) {

        JOptionPane.showMessageDialog(this, "Указанный файл не найден", "Ошибка загрузки данных", JOptionPane.ERROR_MESSAGE);
    } catch (IOException var7) {

        JOptionPane.showMessageDialog(this, "Ошибка чтения координат точек из файла", "Ошибка загрузки данных", JOptionPane.ERROR_MESSAGE);
    }
}
public static void main(String[] args) {
// Создать и показать экземпляр главного окна приложения
    MainFrame frame = new MainFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
}

// Класс-слушатель событий, связанных с отображением меню
private class GraphicsMenuListener implements MenuListener {
    // Обработчик, вызываемый перед показом меню
    public void menuSelected(MenuEvent e) {
// Доступность или недоступность элементов меню "График" определяется загруженностью данных
        showAxisMenuItem.setEnabled(fileLoaded);
        showMarkersMenuItem.setEnabled(fileLoaded);
    }

    // Обработчик, вызываемый после того, как меню исчезло с экрана
    public void menuDeselected(MenuEvent e) {
    }
// Обработчик, вызываемый в случае отмены выбора пункта меню (очень редкая ситуация)
    public void menuCanceled(MenuEvent e) {
    }
}
}