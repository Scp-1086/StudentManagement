package com.ndky.studentmanagement.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray;

public class StatisticsPanel extends JPanel {
    private JTable statisticsTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;
    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;
    private static final String BASE_URL = "http://localhost:8080/api/scores";

    public StatisticsPanel() {
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // 创建工具栏
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        searchTypeCombo = new JComboBox<>(new String[]{"学生统计", "课程统计"});
        searchField = new JTextField(20);
        JButton searchButton = new JButton("查询");
        JButton refreshButton = new JButton("刷新");

        toolBar.add(new JLabel("查询类型："));
        toolBar.add(searchTypeCombo);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(new JLabel("输入ID："));
        toolBar.add(searchField);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(searchButton);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(refreshButton);

        // 创建统计表格
        String[] columnNames = {"统计项", "数值"};
        tableModel = new DefaultTableModel(columnNames, 0);
        statisticsTable = new JTable(tableModel);
        statisticsTable.setRowHeight(25);
        JScrollPane tableScrollPane = new JScrollPane(statisticsTable);

        // 创建详细信息区域
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("宋体", Font.PLAIN, 14));
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);

        // 创建分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailsScrollPane);
        splitPane.setDividerLocation(265);

        // 添加组件到面板
        add(toolBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // 添加事件监听器
        searchButton.addActionListener(e -> searchStatistics());
        refreshButton.addActionListener(e -> clearDisplay());
    }

    private void searchStatistics() {
        String searchType = (String) searchTypeCombo.getSelectedItem();
        String searchId = searchField.getText().trim();

        if (searchId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入查询ID", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String url = BASE_URL + "/statistics/" + (searchType.equals("学生统计") ? "student/" : "course/") + searchId;
            String response = sendGetRequest(url);
            displayStatistics(response, searchType);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "查询失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayStatistics(String response, String searchType) {
        JSONObject json = new JSONObject(response);
        tableModel.setRowCount(0);

        // 显示基本信息
        if (searchType.equals("学生统计")) {
            JSONObject studentInfo = json.getJSONObject("studentInfo");
            tableModel.addRow(new Object[]{"学号", studentInfo.getString("sno")});
            tableModel.addRow(new Object[]{"姓名", studentInfo.getString("sname")});
            tableModel.addRow(new Object[]{"性别", studentInfo.getString("ssex")});
            tableModel.addRow(new Object[]{"年龄", studentInfo.getInt("sage")});
            tableModel.addRow(new Object[]{"院系", studentInfo.getString("sdept")});
        } else {
            JSONObject courseInfo = json.getJSONObject("courseInfo");
            tableModel.addRow(new Object[]{"课程编号", courseInfo.getString("cno")});
            tableModel.addRow(new Object[]{"课程名称", courseInfo.getString("cname")});
            tableModel.addRow(new Object[]{"学分", courseInfo.getInt("ccredit")});
            tableModel.addRow(new Object[]{"开课院系", courseInfo.getString("cdept")});
        }

        // 显示统计信息
        tableModel.addRow(new Object[]{"平均分", json.getDouble("average")});
        tableModel.addRow(new Object[]{"最高分", json.getInt("max")});
        tableModel.addRow(new Object[]{"最低分", json.getInt("min")});
        tableModel.addRow(new Object[]{"总数量", json.getInt("count")});

        // 显示详细信息
        StringBuilder details = new StringBuilder();
        if (searchType.equals("学生统计")) {
            details.append("课程详情：\n\n");
            JSONArray courses = json.getJSONArray("courses");
            for (int i = 0; i < courses.length(); i++) {
                JSONObject course = courses.getJSONObject(i);
                details.append(String.format("课程编号：%s\n", course.getString("cno")));
                details.append(String.format("课程名称：%s\n", course.getString("cname")));
                details.append(String.format("学分：%d\n", course.getInt("ccredit")));
                details.append(String.format("开课院系：%s\n", course.getString("cdept")));
                details.append(String.format("成绩：%d\n", course.getInt("grade")));
                details.append("------------------------\n");
            }
        } else {
            details.append("学生详情：\n\n");
            JSONArray students = json.getJSONArray("students");
            for (int i = 0; i < students.length(); i++) {
                JSONObject student = students.getJSONObject(i);
                details.append(String.format("学号：%s\n", student.getString("sno")));
                details.append(String.format("姓名：%s\n", student.getString("sname")));
                details.append(String.format("性别：%s\n", student.getString("ssex")));
                details.append(String.format("年龄：%d\n", student.getInt("sage")));
                details.append(String.format("院系：%s\n", student.getString("sdept")));
                details.append(String.format("成绩：%d\n", student.getInt("grade")));
                details.append("------------------------\n");
            }
        }
        detailsArea.setText(details.toString());
    }

    private void clearDisplay() {
        tableModel.setRowCount(0);
        detailsArea.setText("");
        searchField.setText("");
    }

    private String sendGetRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("HTTP GET Request Failed with Error code : " + conn.getResponseCode());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        return response.toString();
    }
} 