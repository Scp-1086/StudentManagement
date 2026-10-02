package com.ndky.studentmanagement.controller;

import com.ndky.studentmanagement.entity.Course;
import com.ndky.studentmanagement.exception.DuplicateKeyException;
import com.ndky.studentmanagement.exception.ResourceNotFoundException;
import com.ndky.studentmanagement.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.list();
    }

    @GetMapping("/{cno}")
    public ResponseEntity<Course> getCourseByCno(@PathVariable String cno) {
        List<Course> courses = courseService.findByCno(cno);
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("课程号 " + cno + " 不存在");
        }
        return ResponseEntity.ok(courses.get(0));
    }

    @GetMapping("/search")
    public List<Course> searchCourses(
            @RequestParam(required = false) String cno,
            @RequestParam(required = false) String cname) {
        if (cno != null) return courseService.findByCno(cno);
        if (cname != null) return courseService.findByCname(cname);
        return courseService.list();
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        // 检查课程号是否已存在
        List<Course> existingCourses = courseService.findByCno(course.getCno());
        if (!existingCourses.isEmpty()) {
            throw new DuplicateKeyException("课程号 " + course.getCno() + " 已存在");
        }
        courseService.save(course);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{cno}")
    public ResponseEntity<Course> updateCourse(@PathVariable String cno, @RequestBody Course course) {
        // 检查课程号是否存在
        List<Course> existingCourses = courseService.findByCno(cno);
        if (existingCourses.isEmpty()) {
            throw new ResourceNotFoundException("课程号 " + cno + " 不存在");
        }
        course.setCno(cno);
        courseService.updateById(course);
        return ResponseEntity.ok(course);
    }

    @DeleteMapping("/{cno}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String cno) {
        // 检查课程号是否存在
        List<Course> existingCourses = courseService.findByCno(cno);
        if (existingCourses.isEmpty()) {
            throw new ResourceNotFoundException("课程号 " + cno + " 不存在");
        }
        courseService.removeById(cno);
        return ResponseEntity.ok().build();
    }
} 