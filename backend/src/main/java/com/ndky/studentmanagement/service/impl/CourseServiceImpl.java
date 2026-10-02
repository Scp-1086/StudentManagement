package com.ndky.studentmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ndky.studentmanagement.entity.Course;
import com.ndky.studentmanagement.mapper.CourseMapper;
import com.ndky.studentmanagement.service.CourseService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Override
    public List<Course> findByCno(String cno) {
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("cno", cno);
        return list(queryWrapper);
    }

    @Override
    public List<Course> findByCname(String cname) {
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("cname", cname);
        return list(queryWrapper);
    }
} 