package com.ndky.studentmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ndky.studentmanagement.entity.Student;
import com.ndky.studentmanagement.mapper.StudentMapper;
import com.ndky.studentmanagement.service.StudentService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    @Override
    public List<Student> findBySno(String sno) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("sno", sno);
        return list(queryWrapper);
    }

    @Override
    public List<Student> findByName(String name) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("sname", name);
        return list(queryWrapper);
    }

    @Override
    public List<Student> findByDept(String dept) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("sdept", dept);
        return list(queryWrapper);
    }
}