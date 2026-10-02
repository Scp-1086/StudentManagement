package com.ndky.studentmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ndky.studentmanagement.entity.Score;
import com.ndky.studentmanagement.entity.Student;
import com.ndky.studentmanagement.entity.Course;
import com.ndky.studentmanagement.mapper.ScoreMapper;
import com.ndky.studentmanagement.service.ScoreService;
import com.ndky.studentmanagement.service.StudentService;
import com.ndky.studentmanagement.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {
    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Override
    public List<Score> findByStudent(String sno) {
        QueryWrapper<Score> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sno", sno);
        return list(queryWrapper);
    }

    @Override
    public List<Score> findByCourse(String cno) {
        QueryWrapper<Score> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cno", cno);
        return list(queryWrapper);
    }

    @Override
    public Map<String, Object> getCourseStatistics(String cno) {
        List<Score> scores = findByCourse(cno);
        Map<String, Object> statistics = new HashMap<>();

        Course course = courseService.getById(cno);
        if (course == null) {
            return null;
        }

        statistics.put("courseInfo", new HashMap<String, Object>() {{
            put("cno", course.getCno());
            put("cname", course.getCname());
            put("ccredit", course.getCcredit());
            put("cdept", course.getCdept());
        }});

        if (scores.isEmpty()) {
            statistics.put("average", 0.0);
            statistics.put("max", 0);
            statistics.put("min", 0);
            statistics.put("count", 0);
            statistics.put("students", new ArrayList<>());
            return statistics;
        }

        double sum = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        List<Map<String, Object>> studentDetails = new ArrayList<>();

        for (Score score : scores) {
            int grade = score.getGrade();
            sum += grade;
            max = Math.max(max, grade);
            min = Math.min(min, grade);

            Student student = studentService.getById(score.getSno());
            Map<String, Object> studentDetail = new HashMap<>();
            studentDetail.put("sno", student.getSno());
            studentDetail.put("sname", student.getSname());
            studentDetail.put("ssex", student.getSsex());
            studentDetail.put("sage", student.getSage());
            studentDetail.put("sdept", student.getSdept());
            studentDetail.put("grade", grade);
            studentDetails.add(studentDetail);
        }

        statistics.put("average", Math.round(sum / scores.size() * 100.0) / 100.0);
        statistics.put("max", max);
        statistics.put("min", min);
        statistics.put("count", scores.size());
        statistics.put("students", studentDetails);
        return statistics;
    }

    @Override
    public Map<String, Object> getStudentStatistics(String sno) {
        List<Score> scores = findByStudent(sno);
        Map<String, Object> statistics = new HashMap<>();

        Student student = studentService.getById(sno);
        if (student == null) {
            return null;
        }

        statistics.put("studentInfo", new HashMap<String, Object>() {{
            put("sno", student.getSno());
            put("sname", student.getSname());
            put("ssex", student.getSsex());
            put("sage", student.getSage());
            put("sdept", student.getSdept());
        }});

        if (scores.isEmpty()) {
            statistics.put("average", 0.0);
            statistics.put("max", 0);
            statistics.put("min", 0);
            statistics.put("count", 0);
            statistics.put("courses", new ArrayList<>());
            return statistics;
        }

        double sum = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        List<Map<String, Object>> courseDetails = new ArrayList<>();

        for (Score score : scores) {
            int grade = score.getGrade();
            sum += grade;
            max = Math.max(max, grade);
            min = Math.min(min, grade);

            Course course = courseService.getById(score.getCno());
            Map<String, Object> courseDetail = new HashMap<>();
            courseDetail.put("cno", course.getCno());
            courseDetail.put("cname", course.getCname());
            courseDetail.put("ccredit", course.getCcredit());
            courseDetail.put("cdept", course.getCdept());
            courseDetail.put("grade", grade);
            courseDetails.add(courseDetail);
        }

        statistics.put("average", Math.round(sum / scores.size() * 100.0) / 100.0);
        statistics.put("max", max);
        statistics.put("min", min);
        statistics.put("count", scores.size());
        statistics.put("courses", courseDetails);
        return statistics;
    }
} 