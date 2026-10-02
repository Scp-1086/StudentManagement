package com.ndky.studentmanagement.controller;

import com.ndky.studentmanagement.entity.Score;
import com.ndky.studentmanagement.entity.Student;
import com.ndky.studentmanagement.entity.Course;
import com.ndky.studentmanagement.exception.ResourceNotFoundException;
import com.ndky.studentmanagement.service.ScoreService;
import com.ndky.studentmanagement.service.StudentService;
import com.ndky.studentmanagement.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scores")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ScoreController {
    @Autowired
    private ScoreService scoreService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @GetMapping
    public List<Map<String, Object>> getAllScores() {
        List<Score> scores = scoreService.list();
        return enrichScoresWithDetails(scores);
    }

    @GetMapping("/student/{sno}")
    public List<Map<String, Object>> getScoresByStudent(@PathVariable String sno) {
        List<Score> scores = scoreService.findByStudent(sno);
        if (scores.isEmpty()) {
            throw new ResourceNotFoundException("学号 " + sno + " 不存在或没有成绩记录");
        }
        return enrichScoresWithDetails(scores);
    }

    @GetMapping("/course/{cno}")
    public List<Map<String, Object>> getScoresByCourse(@PathVariable String cno) {
        List<Score> scores = scoreService.findByCourse(cno);
        if (scores.isEmpty()) {
            throw new ResourceNotFoundException("课程号 " + cno + " 不存在或没有成绩记录");
        }
        return enrichScoresWithDetails(scores);
    }

    @GetMapping("/statistics/course/{cno}")
    public Map<String, Object> getCourseStatistics(@PathVariable String cno) {
        Map<String, Object> statistics = scoreService.getCourseStatistics(cno);
        if (statistics == null) {
            throw new ResourceNotFoundException("课程号 " + cno + " 不存在或没有成绩记录");
        }
        return statistics;
    }

    @GetMapping("/statistics/student/{sno}")
    public Map<String, Object> getStudentStatistics(@PathVariable String sno) {
        Map<String, Object> statistics = scoreService.getStudentStatistics(sno);
        if (statistics == null) {
            throw new ResourceNotFoundException("学号 " + sno + " 不存在或没有成绩记录");
        }
        return statistics;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createScore(@RequestBody Score score) {
        scoreService.save(score);
        List<Map<String, Object>> enrichedScores = enrichScoresWithDetails(Arrays.asList(score));
        return ResponseEntity.ok(enrichedScores.get(0));
    }

    @PutMapping("/{sno}/{cno}")
    public ResponseEntity<Map<String, Object>> updateScore(
            @PathVariable String sno,
            @PathVariable String cno,
            @RequestBody Score score) {
        score.setSno(sno);
        score.setCno(cno);
        scoreService.updateById(score);
        List<Map<String, Object>> enrichedScores = enrichScoresWithDetails(Arrays.asList(score));
        return ResponseEntity.ok(enrichedScores.get(0));
    }

    @DeleteMapping("/{sno}/{cno}")
    public ResponseEntity<Void> deleteScore(@PathVariable String sno, @PathVariable String cno) {
        scoreService.removeById(new Score(sno, cno, null));
        return ResponseEntity.ok().build();
    }

    private List<Map<String, Object>> enrichScoresWithDetails(List<Score> scores) {
        return scores.stream().map(score -> {
            Map<String, Object> enrichedScore = new HashMap<>();
            Student student = studentService.getById(score.getSno());
            Course course = courseService.getById(score.getCno());
            
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("sno", student.getSno());
            studentMap.put("name", student.getSname());
            
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("cno", course.getCno());
            courseMap.put("name", course.getCname());
            
            enrichedScore.put("student", studentMap);
            enrichedScore.put("course", courseMap);
            enrichedScore.put("grade", score.getGrade());
            
            return enrichedScore;
        }).collect(Collectors.toList());
    }
} 