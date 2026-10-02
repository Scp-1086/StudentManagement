package com.ndky.studentmanagement.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private StudentPanel studentPanel;
    private CoursePanel coursePanel;
    private ScorePanel scorePanel;

    public MainFrame() {
        setTitle("学生成绩管理系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        studentPanel = new StudentPanel();
        coursePanel = new CoursePanel();
        scorePanel = new ScorePanel();

        tabbedPane.addTab("学生管理", studentPanel);
        tabbedPane.addTab("课程管理", coursePanel);
        tabbedPane.addTab("成绩管理", scorePanel);

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }
} 