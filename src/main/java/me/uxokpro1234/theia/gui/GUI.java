package me.uxokpro1234.theia.gui;

import li.flor.nativejfilechooser.NativeJFileChooser;
import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Theia;
import me.uxokpro1234.theia.checks.AbstractCheck;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Converted from Kotlin GUI object
 * Author: GiantNuker
 */

public class GUI extends JFrame {

    private static GUI instance;

    private final List<String> exclusions = new ArrayList<>();
    private JLabel fileIndicator;
    private JButton fileSelectButton;
    private JButton runButton;
    private JTabbedPane tabs;
    private JTextArea logPanel;
    private JTextArea oldOutputPanel;
    private JPanel tableOutputPanel;
    private JCheckBox excludeLibraries;
    private JTextArea exclusionsBox;
    private File file;
    private boolean runOnce = false;
    private final Executor cachedExec = Executors.newCachedThreadPool();
    private int lastLogHeight = 0;

    private GUI() {
        super("Theia");
        exclusions.add("org/reflections/");
        exclusions.add("javassist/");
        exclusions.add("com/sun/jna/");
        exclusions.add("org/spongepowered/");
        exclusions.add("net/jodah/typetools");
    }

    public static GUI getInstance() {
        if (instance == null) {
            instance = new GUI();
        }
        return instance;
    }

    public void open() {
        addElements();
        setIconImage(new ImageIcon(getClass().getClassLoader().getResource("icon.png")).getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addElements() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

        ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("theia.png"));
        Image img = icon.getImage().getScaledInstance(500, 250, Image.SCALE_DEFAULT);
        JLabel label = new JLabel(new ImageIcon(img));
        header.add(label);

        JPanel fileBox = new JPanel();
        fileBox.setLayout(new BoxLayout(fileBox, BoxLayout.Y_AXIS));

        fileIndicator = new JLabel("File: NONE");
        fileBox.add(fileIndicator);

        fileSelectButton = new JButton("Select File");
        fileSelectButton.addActionListener(e -> {
            NativeJFileChooser fileChooser = new NativeJFileChooser();
            if (fileChooser.showOpenDialog(fileSelectButton) == NativeJFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                fileIndicator.setText("File: " + file.getName());
                runButton.setEnabled(true);
            }
        });
        fileBox.add(fileSelectButton);

        runButton = new JButton("Run Theia");
        runButton.setEnabled(false);
        runButton.addActionListener(e -> runTheia());
        fileBox.add(runButton);

        excludeLibraries = new JCheckBox("Exclude Libraries", true);
        fileBox.add(excludeLibraries);

        exclusionsBox = new JTextArea();
        for (String ex : exclusions) {
            exclusionsBox.append(ex + "\n");
        }
        fileBox.add(scrollPanel(exclusionsBox));

        header.add(fileBox);
        add(header, BorderLayout.NORTH);

        logPanel = new JTextArea();
        logPanel.setEditable(false);
        oldOutputPanel = new JTextArea();
        oldOutputPanel.setEditable(false);
        tableOutputPanel = new JPanel();
        tableOutputPanel.setLayout(new BoxLayout(tableOutputPanel, BoxLayout.Y_AXIS));

        tabs = new JTabbedPane();
        tabs.add("Log", scrollPanel(logPanel));
        add(tabs, BorderLayout.CENTER);
    }

    private JScrollPane scrollPanel(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAutoscrolls(true);
        return scrollPane;
    }

    private void runTheia() {
        if ("Running...".equals(runButton.getText())) return;

        if (file == null) {
            JOptionPane.showMessageDialog(null, "Please select a file to run with!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        runButton.setText("Running...");
        runButton.setEnabled(false);

        cachedExec.execute(() -> {
            // Update GUI safely in EDT
            SwingUtilities.invokeLater(() -> {
                oldOutputPanel.setText("Running...");
                tableOutputPanel.removeAll();
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(new JLabel("Running..."), BorderLayout.WEST);
                tableOutputPanel.add(panel);
                tabs.setSelectedIndex(0);
            });

            // Update exclusions from text area
            exclusions.clear();
            String[] lines = exclusionsBox.getText().split("\\R");
            for (String line : lines) {
                if (!line.isEmpty()) exclusions.add(line.trim());
            }

            // Run Theia
            Theia.getInstance().run(file, excludeLibraries.isSelected() ? new ArrayList<>(exclusions) : new ArrayList<>());

            // Update GUI after run
            SwingUtilities.invokeLater(() -> {
                runButton.setEnabled(true);
                runButton.setText("Run Theia");
                finish();
            });
        });
    }

    private void finish() {
        if (!runOnce) {
            runOnce = true;
            tabs.add("Table", scrollPanel(tableOutputPanel));
            tabs.add("Text", scrollPanel(oldOutputPanel));
        }
        finishTypeTable();
        oldOutputPanel.setText(Theia.getInstance().log);
        oldOutputPanel.scrollRectToVisible(new Rectangle(0, 0));
        tabs.setSelectedIndex(1);
    }

    private void finishTypeTable() {
        tableOutputPanel.removeAll();
        int[] widthArray = new int[]{0, 0, 0};
        DefaultTableModel[] tableModels = new DefaultTableModel[Theia.getInstance().getChecks().length];

        for (int i = 0; i < Theia.getInstance().getChecks().length; i++) {
            AbstractCheck check = Theia.getInstance().getChecks()[i];
            DefaultTableModel model = new DefaultTableModel(0, 3);

            for (Possible possible : check.getPossibles()) {
                model.addRow(new Object[]{
                        possible.getSeverity().name(),
                        possible.getClazz(),
                        possible.getDescription()
                });

                widthArray[0] = Math.max(widthArray[0], new JLabel(possible.getSeverity().name()).getPreferredSize().width);
                widthArray[1] = Math.max(widthArray[1], new JLabel(possible.getClazz()).getPreferredSize().width);
                widthArray[2] = Math.max(widthArray[2], new JLabel(possible.getDescription()).getPreferredSize().width);
            }

            tableModels[i] = model;
        }

        for (int i = 0; i < Theia.getInstance().getChecks().length; i++) {
            DefaultTableModel model = tableModels[i];
            JTable table = new JTable(model) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            int j = 0;
            Enumeration<?> columns = table.getColumnModel().getColumns();
            while (columns.hasMoreElements()) {
                TableColumn tc = (TableColumn) columns.nextElement();
                tc.setMinWidth(widthArray[j] + 20);
                j++;
            }

            JPanel panel = new JPanel(new BorderLayout());
            AbstractCheck check = Theia.getInstance().getChecks()[i];
            panel.add(new JLabel(check.getName() + " - " + check.getDesc()), BorderLayout.WEST);
            tableOutputPanel.add(panel);

            if (check.getPossibles().isEmpty()) {
                panel.add(new JLabel(" - Passed check"), BorderLayout.SOUTH);
            } else {
                tableOutputPanel.add(table);
            }
        }
    }

    public void log(String text) {
        if (text.startsWith("\r")) {
            logPanel.setText(logPanel.getText().substring(0, logPanel.getText().lastIndexOf('\n')) + "\n" + text);
        } else {
            logPanel.append("\n" + text);
        }
        if (logPanel.getHeight() > lastLogHeight && logPanel.getVisibleRect().y + logPanel.getVisibleRect().height > lastLogHeight - 80) {
            logPanel.scrollRectToVisible(new Rectangle(0, logPanel.getHeight(), 0, 0));
        }
        lastLogHeight = logPanel.getHeight();
    }
}