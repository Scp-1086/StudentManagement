package com.ndky.studentmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ndky.studentmanagement.entity.Course;
import java.util.List;

public interface CourseService extends IService<Course> {
    List<Course> findByCno(String cno);
    List<Course> findByCname(String cname);
} 