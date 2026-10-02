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

public class StudentPanel extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;
    private JButton searchButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    private static final String BASE_URL = "http://localhost:8080/api/students";

    public StudentPanel() {
        setLayout(new BorderLayout());
        initComponents();
        loadStudents();
    }

    private void initComponents() {
        // 创建工具栏
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchTypeCombo = new JComboBox<>(new String[]{"学号", "姓名", "系别"});
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
        String[] columnNames = {"学号", "姓名", "性别", "年龄", "系别"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(27);

        JScrollPane scrollPane = new JScrollPane(studentTable);

        // 添加组件到面板
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 添加事件监听器
        searchButton.addActionListener(e -> searchStudents());
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteStudent());
        refreshButton.addActionListener(e -> loadStudents());
    }

    private void loadStudents() {
        try {
            String response = sendGetRequest(BASE_URL);
            JSONArray students = new JSONArray(response);
            tableModel.setRowCount(0);

            for (int i = 0; i < students.length(); i++) {
                JSONObject student = students.getJSONObject(i);
                tableModel.addRow(new Object[]{
                    student.getString("sno"),
                    student.getString("sname"),
                    student.getString("ssex"),
                    student.getInt("sage"),
                    student.getString("sdept")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载学生数据失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchStudents() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadStudents();
            return;
        }

        String searchType = (String) searchTypeCombo.getSelectedItem();
        String searchUrl = BASE_URL + "/search?";
        try {
            switch (searchType) {
                case "学号":
                    searchUrl += "sno=" + java.net.URLEncoder.encode(searchText, "UTF-8");
                    break;
                case "姓名":
                    searchUrl += "name=" + java.net.URLEncoder.encode(searchText, "UTF-8");
                    break;
                case "系别":
                    searchUrl += "dept=" + java.net.URLEncoder.encode(searchText, "UTF-8");
                    break;
            }

            String response = sendGetRequest(searchUrl);
            JSONArray students = new JSONArray(response);
            tableModel.setRowCount(0);

            if (students.length() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "未找到符合条件的" + searchType + "为 \"" + searchText + "\" 的学生，请重新输入",
                    "搜索提示",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (int i = 0; i < students.length(); i++) {
                JSONObject student = students.getJSONObject(i);
                tableModel.addRow(new Object[]{
                    student.getString("sno"),
                    student.getString("sname"),
                    student.getString("ssex"),
                    student.getInt("sage"),
                    student.getString("sdept")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "搜索学生失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAddDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, "添加学生", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new GridLayout(6, 2, 5, 5));

        JTextField snoField = new JTextField();
        JTextField nameField = new JTextField();
        JComboBox<String> sexComboBox = new JComboBox<>(new String[]{"男", "女"});
        JTextField ageField = new JTextField();
        JTextField deptField = new JTextField();

        dialog.add(new JLabel("学号:"));
        dialog.add(snoField);
        dialog.add(new JLabel("姓名:"));
        dialog.add(nameField);
        dialog.add(new JLabel("性别:"));
        dialog.add(sexComboBox);
        dialog.add(new JLabel("年龄:"));
        dialog.add(ageField);
        dialog.add(new JLabel("系别:"));
        dialog.add(deptField);

        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            try {
                JSONObject student = new JSONObject();
                student.put("sno", snoField.getText());
                student.put("sname", nameField.getText());
                student.put("ssex", sexComboBox.getSelectedItem());
                student.put("sage", Integer.parseInt(ageField.getText()));
                student.put("sdept", deptField.getText());

                String response = sendPostRequest(BASE_URL, student.toString());
                if (response.contains("sno")) {
                    JOptionPane.showMessageDialog(dialog, "添加成功");
                    dialog.dispose();
                    loadStudents();
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
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的学生");
            return;
        }

        String sno = (String) tableModel.getValueAt(selectedRow, 0);

        try {
            String response = sendGetRequest(BASE_URL + "/" + sno);
            JSONObject student = new JSONObject(response);

            Window parent = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parent, "编辑学生", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setLayout(new GridLayout(6, 2, 5, 5));

            JTextField snoField = new JTextField(student.getString("sno"));
            snoField.setEditable(false);
            JTextField nameField = new JTextField(student.getString("sname"));
            JComboBox<String> sexComboBox = new JComboBox<>(new String[]{"男", "女"});
            sexComboBox.setSelectedItem(student.getString("ssex"));
            JTextField ageField = new JTextField(String.valueOf(student.getInt("sage")));
            JTextField deptField = new JTextField(student.getString("sdept"));

            dialog.add(new JLabel("学号:"));
            dialog.add(snoField);
            dialog.add(new JLabel("姓名:"));
            dialog.add(nameField);
            dialog.add(new JLabel("性别:"));
            dialog.add(sexComboBox);
            dialog.add(new JLabel("年龄:"));
            dialog.add(ageField);
            dialog.add(new JLabel("系别:"));
            dialog.add(deptField);

            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");

            saveButton.addActionListener(e -> {
                try {
                    JSONObject updatedStudent = new JSONObject();
                    updatedStudent.put("sno", snoField.getText());
                    updatedStudent.put("sname", nameField.getText());
                    updatedStudent.put("ssex", sexComboBox.getSelectedItem());
                    updatedStudent.put("sage", Integer.parseInt(ageField.getText()));
                    updatedStudent.put("sdept", deptField.getText());

                    String updateResponse = sendPutRequest(BASE_URL + "/" + sno, updatedStudent.toString());
                    if (updateResponse.contains("sno")) {
                        JOptionPane.showMessageDialog(dialog, "更新成功");
                        dialog.dispose();
                        loadStudents();
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
            JOptionPane.showMessageDialog(this, "获取学生信息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的学生");
            return;
        }

        String sno = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "确定要删除学号为 " + sno + " 的学生吗?", 
            "确认删除", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String response = sendDeleteRequest(BASE_URL + "/" + sno);
                if (response.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "删除成功");
                    loadStudents();
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