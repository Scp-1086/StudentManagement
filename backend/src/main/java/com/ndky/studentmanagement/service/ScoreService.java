package com.ndky.studentmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ndky.studentmanagement.entity.Score;
import java.util.List;
import java.util.Map;

public interface ScoreService extends IService<Score> {
    List<Score> findByStudent(String sno);
    List<Score> findByCourse(String cno);
    Map<String, Object> getCourseStatistics(String cno);
    Map<String, Object> getStudentStatistics(String sno);
} 