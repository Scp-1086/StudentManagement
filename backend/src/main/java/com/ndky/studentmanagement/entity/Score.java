package com.ndky.studentmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sc")
public class Score {
    @TableId(type = IdType.INPUT)
    private String sno;
    private String cno;
    private Integer grade;
} 