package com.ndky.studentmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ndky.studentmanagement.entity.Student;
import java.util.List;

public interface StudentService extends IService<Student> {
    List<Student> findBySno(String sno);
    List<Student> findByName(String name);
    List<Student> findByDept(String dept); 
}