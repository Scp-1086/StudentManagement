package com.ndky.studentmanagement.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ndky.studentmanagement.entity.Student;
import com.ndky.studentmanagement.exception.DuplicateKeyException;
import com.ndky.studentmanagement.exception.ResourceNotFoundException;
import com.ndky.studentmanagement.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.list();
    }

    @GetMapping("/{sno}")
    public ResponseEntity<Student> getStudentBySno(@PathVariable String sno) {
        List<Student> students = studentService.findBySno(sno);
        if (students.isEmpty()) {
            throw new ResourceNotFoundException("学号 " + sno + " 不存在");
        }
        return ResponseEntity.ok(students.get(0));
    }

    @GetMapping("/search")
    public List<Student> searchStudents(
            @RequestParam(required = false) String sno,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String dept) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        if (sno != null && !sno.isEmpty()) {
            queryWrapper.like("sno", sno);
        }
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("sname", name);
        }
        if (dept != null && !dept.isEmpty()) {
            queryWrapper.eq("sdept", dept);
        }
        return studentService.list(queryWrapper);
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        // 检查学号是否已存在
        List<Student> existingStudents = studentService.findBySno(student.getSno());
        if (!existingStudents.isEmpty()) {
            throw new DuplicateKeyException("学号 " + student.getSno() + " 已存在");
        }
        studentService.save(student);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{sno}")
    public ResponseEntity<Student> updateStudent(@PathVariable String sno, @RequestBody Student student) {
        // 检查学号是否存在
        List<Student> existingStudents = studentService.findBySno(sno);
        if (existingStudents.isEmpty()) {
            throw new ResourceNotFoundException("学号 " + sno + " 不存在");
        }
        student.setSno(sno);
        studentService.updateById(student);
        return ResponseEntity.ok(student);
    }

    @DeleteMapping("/{sno}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String sno) {
        // 检查学号是否存在
        List<Student> existingStudents = studentService.findBySno(sno);
        if (existingStudents.isEmpty()) {
            throw new ResourceNotFoundException("学号 " + sno + " 不存在");
        }
        studentService.removeById(sno);
        return ResponseEntity.ok().build();
    }

    // 分页查询
    @GetMapping("/page")
    public Page<Student> pageStudents(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Student> page = new Page<>(pageNum, pageSize);
        return studentService.page(page);
    }
}
