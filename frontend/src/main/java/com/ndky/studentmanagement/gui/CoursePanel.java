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

public class CoursePanel extends JPanel {
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;
    private JButton searchButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    private static final String BASE_URL = "http://localhost:8080/api/courses";

    public CoursePanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadCourses();
    }

    private void initComponents() {
        // 创建工具栏
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchTypeCombo = new JComboBox<>(new String[]{"课程号", "课程名"});
        searchButton = new JButton("搜索");
        addButton = new JButton("添加");
        editButton = new JButton("编辑");
        deleteButton = new JButton("删除");
        refreshButton = new JButton("刷新");

        toolBar.add(new JLabel("搜索条件："));
        toolBar.add(searchTypeCombo);
        toolBar.add(searchField);
        toolBar.add(searchButton);
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(refreshButton);

        // 创建表格
        String[] columnNames = {"课程号", "课程名", "学分", "开课院系"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable = new JTable(tableModel);
        // 设置所有行高为25像素
        courseTable.setRowHeight(27);
        JScrollPane scrollPane = new JScrollPane(courseTable);

        // 添加组件到面板
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 添加事件监听器
        searchButton.addActionListener(e -> searchCourses());
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteCourse());
        refreshButton.addActionListener(e -> loadCourses());
    }

    private void loadCourses() {
        try {
            String response = sendGetRequest(BASE_URL);
            JSONArray courses = new JSONArray(response);
            tableModel.setRowCount(0);

            for (int i = 0; i < courses.length(); i++) {
                JSONObject course = courses.getJSONObject(i);
                tableModel.addRow(new Object[]{
                    course.getString("cno"),
                    course.getString("cname"),
                    course.getInt("ccredit"),
                    course.getString("cdept")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载课程数据失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchCourses() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadCourses();
            return;
        }

        String searchType = (String) searchTypeCombo.getSelectedItem();
        String searchUrl = BASE_URL + "/search?";
        try {
            switch (searchType) {
                case "课程号":
                    searchUrl += "cno=" + java.net.URLEncoder.encode(searchText, "UTF-8");
                    break;
                case "课程名":
                    searchUrl += "cname=" + java.net.URLEncoder.encode(searchText, "UTF-8");
                    break;
            }

            String response = sendGetRequest(searchUrl);
            JSONArray courses = new JSONArray(response);
            tableModel.setRowCount(0);

            if (courses.length() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "未找到符合条件的" + searchType + "为 \"" + searchText + "\" 的课程，请重新输入",
                    "搜索提示",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (int i = 0; i < courses.length(); i++) {
                JSONObject course = courses.getJSONObject(i);
                tableModel.addRow(new Object[]{
                    course.getString("cno"),
                    course.getString("cname"),
                    course.getInt("ccredit"),
                    course.getString("cdept")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "搜索课程失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAddDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, "添加课程", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new GridLayout(5, 2, 5, 5));

        JTextField cnoField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField creditField = new JTextField();
        JTextField deptField = new JTextField();

        dialog.add(new JLabel("课程号:"));
        dialog.add(cnoField);
        dialog.add(new JLabel("课程名:"));
        dialog.add(nameField);
        dialog.add(new JLabel("学分:"));
        dialog.add(creditField);
        dialog.add(new JLabel("开课院系:"));
        dialog.add(deptField);

        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            try {
                JSONObject course = new JSONObject();
                course.put("cno", cnoField.getText());
                course.put("cname", nameField.getText());
                course.put("ccredit", Integer.parseInt(creditField.getText()));
                course.put("cdept", deptField.getText());

                String response = sendPostRequest(BASE_URL, course.toString());
                if (response.contains("cno")) {
                    JOptionPane.showMessageDialog(dialog, "添加成功");
                    dialog.dispose();
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(dialog, "添加失败");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "添加失败: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的课程");
            return;
        }

        String cno = (String) tableModel.getValueAt(selectedRow, 0);

        try {
            String response = sendGetRequest(BASE_URL + "/" + cno);
            JSONObject course = new JSONObject(response);

            Window parent = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parent, "编辑课程", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setLayout(new GridLayout(5, 2, 5, 5));

            JTextField cnoField = new JTextField(course.getString("cno"));
            cnoField.setEditable(false);
            JTextField nameField = new JTextField(course.getString("cname"));
            JTextField creditField = new JTextField(String.valueOf(course.getInt("ccredit")));
            JTextField deptField = new JTextField(course.getString("cdept"));

            dialog.add(new JLabel("课程号:"));
            dialog.add(cnoField);
            dialog.add(new JLabel("课程名:"));
            dialog.add(nameField);
            dialog.add(new JLabel("学分:"));
            dialog.add(creditField);
            dialog.add(new JLabel("开课院系:"));
            dialog.add(deptField);

            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");

            saveButton.addActionListener(e -> {
                try {
                    JSONObject updatedCourse = new JSONObject();
                    updatedCourse.put("cno", cnoField.getText());
                    updatedCourse.put("cname", nameField.getText());
                    updatedCourse.put("ccredit", Integer.parseInt(creditField.getText()));
                    updatedCourse.put("cdept", deptField.getText());

                    String updateResponse = sendPutRequest(BASE_URL + "/" + cno, updatedCourse.toString());
                    if (updateResponse.contains("cno")) {
                        JOptionPane.showMessageDialog(dialog, "更新成功");
                        dialog.dispose();
                        loadCourses();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "更新失败");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "更新失败: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            cancelButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            dialog.add(buttonPanel);

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "获取课程信息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的课程");
            return;
        }

        String cno = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "确定要删除课程号为 " + cno + " 的课程吗?", 
            "确认删除", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String response = sendDeleteRequest(BASE_URL + "/" + cno);
                if (response.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "删除成功");
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "删除失败: " + e.getMessage());
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

    private String sendPostRequest(String urlStr, String jsonInput) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
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

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    private String sendPutRequest(String urlStr, String jsonInput) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
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

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    private String sendDeleteRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
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
} 