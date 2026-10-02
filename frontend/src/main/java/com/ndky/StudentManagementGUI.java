package com.ndky;

import com.ndky.studentmanagement.gui.StudentPanel;
import com.ndky.studentmanagement.gui.CoursePanel;
import com.ndky.studentmanagement.gui.ScorePanel;
import com.ndky.studentmanagement.gui.StatisticsPanel;

import javax.swing.*;
import java.awt.*;

public class StudentManagementGUI extends JFrame {
    private JTabbedPane tabbedPane;
    private StudentPanel studentPanel;
    private CoursePanel coursePanel;
    private ScorePanel scorePanel;
    private StatisticsPanel statisticsPanel;

    public StudentManagementGUI() {
        setTitle("学生成绩管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        studentPanel = new StudentPanel();
        coursePanel = new CoursePanel();
        scorePanel = new ScorePanel();
        statisticsPanel = new StatisticsPanel();

        tabbedPane.addTab("学生管理", studentPanel);
        tabbedPane.addTab("课程管理", coursePanel);
        tabbedPane.addTab("成绩管理", scorePanel);
        tabbedPane.addTab("统计查询", statisticsPanel);

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new StudentManagementGUI().setVisible(true);
        });
    }
}