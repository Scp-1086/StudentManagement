package com.ndky.studentmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ndky.studentmanagement.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}