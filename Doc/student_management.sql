-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 使用数据库
USE student_management;

-- 创建学生表
CREATE TABLE IF NOT EXISTS student (
    sno VARCHAR(20) PRIMARY KEY COMMENT '学号',
    sname VARCHAR(50) NOT NULL COMMENT '姓名',
    ssex VARCHAR(10) NOT NULL COMMENT '性别',
    sage INT NOT NULL COMMENT '年龄',
    sdept VARCHAR(50) NOT NULL COMMENT '系别'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 创建课程表
CREATE TABLE IF NOT EXISTS course (
    cno VARCHAR(20) PRIMARY KEY COMMENT '课程号',
    cname VARCHAR(50) NOT NULL COMMENT '课程名',
    ccredit INT NOT NULL COMMENT '学分',
    cdept VARCHAR(50) NOT NULL COMMENT '开课系别'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程信息表';

-- 创建成绩表
CREATE TABLE IF NOT EXISTS sc (
    sno VARCHAR(20) COMMENT '学号',
    cno VARCHAR(20) COMMENT '课程号',
    grade INT COMMENT '成绩',
    PRIMARY KEY (sno, cno),
    FOREIGN KEY (sno) REFERENCES student(sno) ON DELETE CASCADE,
    FOREIGN KEY (cno) REFERENCES course(cno) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 插入示例数据
-- 学生数据
INSERT INTO student (sno, sname, ssex, sage, sdept) VALUES
('2024001', '张三', '男', 20, '计算机系'),
('2024002', '李四', '女', 19, '计算机系'),
('2024003', '王五', '男', 21, '数学系'),
('2024004', '赵六', '女', 20, '物理系'),
('2024005', '孙七', '男', 22, '化学系');

-- 课程数据
INSERT INTO course (cno, cname, ccredit, cdept) VALUES
('C001', 'Java程序设计', 4, '计算机系'),
('C002', '数据库原理', 3, '计算机系'),
('C003', '高等数学', 5, '数学系'),
('C004', '大学物理', 4, '物理系'),
('C005', '有机化学', 3, '化学系');

-- 成绩数据
INSERT INTO sc (sno, cno, grade) VALUES
('2024001', 'C001', 85),
('2024001', 'C002', 90),
('2024002', 'C001', 88),
('2024002', 'C002', 92),
('2024003', 'C003', 95),
('2024004', 'C004', 87),
('2024005', 'C005', 89),
('2024001', 'C003', 78),
('2024002', 'C003', 82),
('2024003', 'C001', 76); 