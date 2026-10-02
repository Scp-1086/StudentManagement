package com.ndky.studentmanagement.gui;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ScorePanel extends JPanel {
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;
    private JButton searchButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    private static final String BASE_URL = "http://localhost:8080/api/scores";

    public ScorePanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // 创建工具栏
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // 创建搜索组件
        searchField = new JTextField(15);
        searchTypeCombo = new JComboBox<>(new String[]{"按学生查询", "按课程查询"});
        searchButton = new JButton("搜索");
        refreshButton = new JButton("刷新");

        // 添加搜索组件到工具栏
        toolBar.add(new JLabel("查询类型："));
        toolBar.add(searchTypeCombo);
        toolBar.add(new JLabel("学号/课程号："));
        toolBar.add(searchField);
        toolBar.add(searchButton);
        toolBar.addSeparator();
        toolBar.add(refreshButton);

        // 创建按钮
        addButton = new JButton("添加");
        editButton = new JButton("编辑");
        deleteButton = new JButton("删除");

        // 添加按钮到工具栏
        toolBar.addSeparator();
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);

        // 创建表格
        String[] columnNames = {"学号", "学生姓名", "课程号", "课程名称", "成绩"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scoreTable = new JTable(tableModel);
        scoreTable.setRowHeight(25);
        JScrollPane tableScrollPane = new JScrollPane(scoreTable);

        // 添加组件到面板
        add(toolBar, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        // 添加事件监听器
        searchButton.addActionListener(e -> searchScores());
        refreshButton.addActionListener(e -> loadScores());
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteScore());

        // 初始加载所有成绩
        loadScores();
    }

    private void loadScores() {
        try {
            String response = sendGetRequest(BASE_URL);
            JSONArray scores = new JSONArray(response);
            updateTable(scores);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载成绩失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchScores() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入学号或课程号");
            return;
        }

        String searchType = (String) searchTypeCombo.getSelectedItem();
        String searchUrl;
        try {
            if ("按学生查询".equals(searchType)) {
                searchUrl = BASE_URL + "/student/" + java.net.URLEncoder.encode(searchText, "UTF-8");
            } else {
                searchUrl = BASE_URL + "/course/" + java.net.URLEncoder.encode(searchText, "UTF-8");
            }

            String response = sendGetRequest(searchUrl);
            JSONArray scores = new JSONArray(response);
            
            if (scores.length() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "未找到" + searchType + "为 \"" + searchText + "\" 的成绩记录，请重新输入",
                    "搜索提示",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            updateTable(scores);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "查询失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTable(JSONArray scores) {
        tableModel.setRowCount(0);
        for (int i = 0; i < scores.length(); i++) {
            JSONObject score = scores.getJSONObject(i);
            JSONObject student = score.getJSONObject("student");
            JSONObject course = score.getJSONObject("course");
            tableModel.addRow(new Object[]{
                student.getString("sno"),
                student.getString("name"),
                course.getString("cno"),
                course.getString("name"),
                score.getInt("grade")
            });
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "添加成绩", true);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField snoField = new JTextField();
        JTextField cnoField = new JTextField();
        JTextField gradeField = new JTextField();

        formPanel.add(new JLabel("学号:"));
        formPanel.add(snoField);
        formPanel.add(new JLabel("课程号:"));
        formPanel.add(cnoField);
        formPanel.add(new JLabel("成绩:"));
        formPanel.add(gradeField);

        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            try {
                String sno = snoField.getText().trim();
                String cno = cnoField.getText().trim();
                String gradeStr = gradeField.getText().trim();

                if (sno.isEmpty() || cno.isEmpty() || gradeStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请填写所有字段");
                    return;
                }

                // 检查成绩是否已存在
                String checkUrl = BASE_URL + "/student/" + java.net.URLEncoder.encode(sno, "UTF-8");
                String response = sendGetRequest(checkUrl);
                JSONArray scores = new JSONArray(response);
                
                for (int i = 0; i < scores.length(); i++) {
                    JSONObject score = scores.getJSONObject(i);
                    JSONObject course = score.getJSONObject("course");
                    if (course.getString("cno").equals(cno)) {
                        JOptionPane.showMessageDialog(dialog, 
                            "该学生（学号：" + sno + "）的课程（课程号：" + cno + "）成绩已存在！",
                            "添加失败",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                int grade = Integer.parseInt(gradeStr);
                if (grade < 0 || grade > 100) {
                    JOptionPane.showMessageDialog(dialog, "成绩必须在0-100之间");
                    return;
                }

                JSONObject score = new JSONObject();
                score.put("sno", sno);
                score.put("cno", cno);
                score.put("grade", grade);

                sendPostRequest(BASE_URL, score.toString());
                JOptionPane.showMessageDialog(dialog, "添加成功");
                dialog.dispose();
                loadScores();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "成绩必须是数字");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加失败: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(270, 225);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = scoreTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的成绩记录");
            return;
        }

        String sno = (String) tableModel.getValueAt(selectedRow, 0);
        String cno = (String) tableModel.getValueAt(selectedRow, 2);
        int currentGrade = (int) tableModel.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "修改成绩", true);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField gradeField = new JTextField(String.valueOf(currentGrade));

        formPanel.add(new JLabel("学号："));
        formPanel.add(new JLabel(sno));
        formPanel.add(new JLabel("课程号："));
        formPanel.add(new JLabel(cno));
        formPanel.add(new JLabel("成绩："));
        formPanel.add(gradeField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            try {
                int grade = Integer.parseInt(gradeField.getText().trim());
                if (grade < 0 || grade > 100) {
                    JOptionPane.showMessageDialog(dialog, "成绩必须在0-100之间");
                    return;
                }

                JSONObject score = new JSONObject();
                score.put("sno", sno);
                score.put("cno", cno);
                score.put("grade", grade);

                sendPutRequest(BASE_URL + "/" + sno + "/" + cno, score.toString());
                dialog.dispose();
                loadScores();
                JOptionPane.showMessageDialog(this, "成绩修改成功");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "成绩必须是数字");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "保存失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteScore() {
        int selectedRow = scoreTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的成绩记录");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除这条成绩记录吗？", "确认删除", 
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sno = (String) tableModel.getValueAt(selectedRow, 0);
                String cno = (String) tableModel.getValueAt(selectedRow, 2);
                sendDeleteRequest(BASE_URL + "/" + sno + "/" + cno);
                loadScores();
                JOptionPane.showMessageDialog(this, "成绩删除成功");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "删除失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String sendGetRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                throw new Exception(response.toString());
            }
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    private void sendPostRequest(String urlStr, String jsonInput) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                throw new Exception(response.toString());
            }
        }
    }

    private void sendPutRequest(String urlStr, String jsonInput) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                throw new Exception(response.toString());
            }
        }
    }

    private void sendDeleteRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                throw new Exception(response.toString());
            }
        }
    }
} 
