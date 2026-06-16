-- 楼宇管理系统数据库初始化脚本
-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection=utf8mb4;

CREATE DATABASE IF NOT EXISTS building_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE building_management;

-- 单位/部门表
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '单位名称',
    code VARCHAR(50) NOT NULL COMMENT '单位编码',
    parent_id BIGINT DEFAULT 0 COMMENT '上级单位ID',
    level INT DEFAULT 1 COMMENT '层级(1集团/2子单位)',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态(1启用/0禁用)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单位部门表';

-- 系统用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '登录名',
    password VARCHAR(200) NOT NULL COMMENT '密码(MD5加密)',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'MANAGER' COMMENT '角色(ADMIN/MANAGER)',
    department_id BIGINT NOT NULL COMMENT '所属单位ID',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态(1启用/0禁用)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    KEY idx_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 楼宇表
DROP TABLE IF EXISTS building;
CREATE TABLE building (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '楼宇名称',
    code VARCHAR(50) NOT NULL COMMENT '楼宇编号',
    department_id BIGINT NOT NULL COMMENT '所属单位ID',
    address VARCHAR(200) DEFAULT NULL COMMENT '地址',
    floors INT DEFAULT 1 COMMENT '楼层数',
    total_area DECIMAL(12,2) DEFAULT 0.00 COMMENT '总面积(平方米)',
    structure_type VARCHAR(50) DEFAULT NULL COMMENT '结构类型',
    build_year INT DEFAULT NULL COMMENT '建造年份',
    status TINYINT DEFAULT 1 COMMENT '状态(1正常/0封存)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code),
    KEY idx_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼宇表';

-- 房屋表
DROP TABLE IF EXISTS room;
CREATE TABLE room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building_id BIGINT NOT NULL COMMENT '所属楼宇ID',
    department_id BIGINT NOT NULL COMMENT '所属单位ID',
    room_number VARCHAR(50) NOT NULL COMMENT '房间号',
    floor INT DEFAULT 1 COMMENT '所在楼层',
    area DECIMAL(10,2) DEFAULT 0.00 COMMENT '面积(平方米)',
    purpose VARCHAR(50) DEFAULT NULL COMMENT '用途(办公/宿舍/公寓/教学楼/病房/其他)',
    status VARCHAR(20) DEFAULT '空闲' COMMENT '状态(空闲/使用中/出租/封存)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_building (building_id),
    KEY idx_department (department_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房屋表';

-- 房屋资产表
DROP TABLE IF EXISTS room_asset;
CREATE TABLE room_asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL COMMENT '房间ID',
    asset_name VARCHAR(100) NOT NULL COMMENT '资产名称',
    asset_type VARCHAR(50) DEFAULT NULL COMMENT '资产类型(家具/电器/办公设备/其他)',
    quantity INT DEFAULT 1 COMMENT '数量',
    status VARCHAR(20) DEFAULT '正常' COMMENT '状态(正常/损坏/报废)',
    purchase_date DATE DEFAULT NULL COMMENT '购入日期',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房屋资产表';

-- 房屋健康记录表
DROP TABLE IF EXISTS room_health;
CREATE TABLE room_health (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL COMMENT '房间ID',
    check_date DATE NOT NULL COMMENT '检查日期',
    check_type VARCHAR(50) NOT NULL COMMENT '检查类型(日常巡检/专项检查/安全检查)',
    result VARCHAR(20) NOT NULL COMMENT '检查结果(良好/一般/较差)',
    description VARCHAR(1000) DEFAULT NULL COMMENT '问题描述',
    handler VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    next_check_date DATE DEFAULT NULL COMMENT '下次检查日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_room (room_id),
    KEY idx_check_date (check_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房屋健康记录表';

-- 内部使用审批表
DROP TABLE IF EXISTS usage_approval;
CREATE TABLE usage_approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL COMMENT '房间ID',
    department_id BIGINT NOT NULL COMMENT '申请单位ID',
    applicant VARCHAR(50) NOT NULL COMMENT '申请人',
    usage_type VARCHAR(50) NOT NULL COMMENT '用途类型(办公/宿舍/公寓/教学/病房/其他)',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE DEFAULT NULL COMMENT '结束日期',
    status VARCHAR(20) DEFAULT '待审批' COMMENT '状态(待审批/已通过/已驳回)',
    reason VARCHAR(500) DEFAULT NULL COMMENT '申请原因',
    approver VARCHAR(50) DEFAULT NULL COMMENT '审批人',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    approve_remark VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_room (room_id),
    KEY idx_department (department_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内部使用审批表';

-- 租赁管理表
DROP TABLE IF EXISTS rental;
CREATE TABLE rental (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL COMMENT '房间ID',
    tenant_name VARCHAR(100) NOT NULL COMMENT '租赁方名称',
    tenant_contact VARCHAR(50) DEFAULT NULL COMMENT '联系方式',
    tenant_company VARCHAR(200) DEFAULT NULL COMMENT '租赁公司',
    start_date DATE NOT NULL COMMENT '起始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    rent_amount DECIMAL(12,2) NOT NULL COMMENT '月租金(元)',
    payment_cycle VARCHAR(20) DEFAULT '月付' COMMENT '支付周期(月付/季付/半年付/年付)',
    status VARCHAR(20) DEFAULT '有效' COMMENT '状态(有效/到期/终止)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_room (room_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租赁管理表';

-- 租赁合约表
DROP TABLE IF EXISTS rental_contract;
CREATE TABLE rental_contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rental_id BIGINT NOT NULL COMMENT '租赁ID',
    contract_number VARCHAR(50) NOT NULL COMMENT '合同编号',
    sign_date DATE NOT NULL COMMENT '签约日期',
    start_date DATE NOT NULL COMMENT '合同起始日期',
    end_date DATE NOT NULL COMMENT '合同终止日期',
    total_amount DECIMAL(14,2) NOT NULL COMMENT '合同总额(元)',
    status VARCHAR(20) DEFAULT '生效' COMMENT '状态(生效/到期/终止)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contract_number (contract_number),
    KEY idx_rental (rental_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租赁合约表';

-- 操作日志表
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    module VARCHAR(50) DEFAULT NULL COMMENT '模块',
    action VARCHAR(50) DEFAULT NULL COMMENT '操作',
    target VARCHAR(200) DEFAULT NULL COMMENT '操作对象',
    detail TEXT DEFAULT NULL COMMENT '详情',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ===== 初始数据 =====

-- 单位数据
INSERT INTO sys_department (id, name, code, parent_id, level, sort, status) VALUES
(1, '集团总部', 'HQ', 0, 1, 1, 1),
(2, '分公司A', 'BRANCH_A', 1, 2, 2, 1),
(3, '分公司B', 'BRANCH_B', 1, 2, 3, 1),
(4, '分公司C', 'BRANCH_C', 1, 2, 4, 1);

-- 用户数据 (密码均为 MD5 加密，初始密码为 123456)
INSERT INTO sys_user (id, username, password, real_name, role, department_id, phone, status) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN', 1, '13800000001', 1),
(2, 'manager', 'e10adc3949ba59abbe56e057f20f883e', '张经理', 'MANAGER', 2, '13800000002', 1);

-- 楼宇数据
INSERT INTO building (id, name, code, department_id, address, floors, total_area, structure_type, build_year, status) VALUES
(1, '总部大厦', 'BLD-HQ-001', 1, '北京市朝阳区建国路88号', 25, 50000.00, '钢筋混凝土', 2015, 1),
(2, 'A区办公楼', 'BLD-A-001', 2, '北京市海淀区中关村大街1号', 12, 18000.00, '框架结构', 2018, 1),
(3, 'A区宿舍楼', 'BLD-A-002', 2, '北京市海淀区中关村大街3号', 8, 6000.00, '框架结构', 2019, 1),
(4, 'B区综合楼', 'BLD-B-001', 3, '上海市浦东新区陆家嘴路100号', 18, 32000.00, '钢结构', 2020, 1),
(5, 'C区教学楼', 'BLD-C-001', 4, '广州市天河区天河路200号', 6, 8000.00, '框架结构', 2017, 1);

-- 房屋数据
INSERT INTO room (id, building_id, department_id, room_number, floor, area, purpose, status) VALUES
(1, 1, 1, '101', 1, 120.00, '办公', '使用中'),
(2, 1, 1, '102', 1, 80.00, '办公', '空闲'),
(3, 1, 1, '201', 2, 200.00, '办公', '出租'),
(4, 1, 1, '301', 3, 60.00, '办公', '封存'),
(5, 2, 2, 'A-101', 1, 150.00, '办公', '使用中'),
(6, 2, 2, 'A-201', 2, 100.00, '办公', '空闲'),
(7, 3, 2, 'S-101', 1, 30.00, '宿舍', '使用中'),
(8, 3, 2, 'S-102', 1, 30.00, '宿舍', '空闲'),
(9, 4, 3, 'B-501', 5, 180.00, '办公', '使用中'),
(10, 4, 3, 'B-601', 6, 250.00, '办公', '出租'),
(11, 5, 4, 'C-101', 1, 200.00, '教学楼', '使用中'),
(12, 5, 4, 'C-201', 2, 200.00, '教学楼', '空闲');

-- 资产数据
INSERT INTO room_asset (id, room_id, asset_name, asset_type, quantity, status, purchase_date) VALUES
(1, 1, '办公桌', '家具', 6, '正常', '2022-03-15'),
(2, 1, '办公椅', '家具', 6, '正常', '2022-03-15'),
(3, 1, '台式电脑', '电器', 6, '正常', '2022-05-20'),
(4, 1, '空调', '电器', 2, '正常', '2022-01-10'),
(5, 5, '会议桌', '家具', 1, '正常', '2023-01-08'),
(6, 5, '投影仪', '办公设备', 1, '正常', '2023-02-15'),
(7, 7, '床铺', '家具', 4, '正常', '2023-06-01'),
(8, 9, '工位桌椅', '家具', 10, '正常', '2023-08-10');

-- 健康记录数据
INSERT INTO room_health (id, room_id, check_date, check_type, result, description, handler, next_check_date) VALUES
(1, 1, '2024-01-15', '日常巡检', '良好', '各项设施运行正常', '李维护', '2024-04-15'),
(2, 5, '2024-02-20', '安全检查', '良好', '消防设施完好，电路正常', '王安全', '2024-08-20'),
(3, 7, '2024-01-10', '日常巡检', '一般', '墙面有轻微裂缝需修补', '李维护', '2024-03-10'),
(4, 9, '2024-03-05', '专项检查', '良好', '空调系统运行正常', '赵工程', '2024-09-05');

-- 使用审批数据
INSERT INTO usage_approval (id, room_id, department_id, applicant, usage_type, start_date, end_date, status, reason, approver, approve_time) VALUES
(1, 2, 1, '王主任', '办公', '2024-04-01', '2025-03-31', '已通过', '部门扩编需要新办公室', 'admin', '2024-03-20 10:00:00'),
(2, 6, 2, '李经理', '办公', '2024-05-01', '2025-04-30', '待审批', '项目组临时办公需要', NULL, NULL),
(3, 8, 2, '赵主管', '宿舍', '2024-06-01', '2025-05-31', '已通过', '新员工入职住宿', 'manager', '2024-05-25 14:30:00'),
(4, 12, 4, '陈老师', '教学', '2024-09-01', '2025-01-15', '已驳回', '培训课程使用', 'admin', '2024-08-15 09:00:00');

-- 租赁数据
INSERT INTO rental (id, room_id, tenant_name, tenant_contact, tenant_company, start_date, end_date, rent_amount, payment_cycle, status) VALUES
(1, 3, '张先生', '13900001111', '北京创新科技有限公司', '2024-01-01', '2025-12-31', 15000.00, '月付', '有效'),
(2, 10, '李女士', '13900002222', '上海智联网络科技公司', '2024-03-01', '2025-02-28', 22000.00, '季付', '有效'),
(3, 3, '王经理', '13900003333', '深圳大数据有限公司', '2022-01-01', '2023-12-31', 12000.00, '月付', '到期');

-- 租赁合约数据
INSERT INTO rental_contract (id, rental_id, contract_number, sign_date, start_date, end_date, total_amount, status) VALUES
(1, 1, 'CT-2024-001', '2023-12-20', '2024-01-01', '2025-12-31', 360000.00, '生效'),
(2, 2, 'CT-2024-002', '2024-02-15', '2024-03-01', '2025-02-28', 264000.00, '生效'),
(3, 3, 'CT-2022-001', '2021-12-15', '2022-01-01', '2023-12-31', 288000.00, '到期');

-- 操作日志数据
INSERT INTO operation_log (id, user_id, username, module, action, target, detail, ip, created_at) VALUES
(1, 1, 'admin', '楼宇管理', '新增', '总部大厦', '新增楼宇: 总部大厦(BLD-HQ-001)', '192.168.1.100', '2024-01-01 09:00:00'),
(2, 1, 'admin', '房屋管理', '新增', '101', '新增房屋: 总部大厦-101', '192.168.1.100', '2024-01-02 10:30:00'),
(3, 2, 'manager', '使用审批', '提交', '审批单#1', '提交房屋使用申请', '192.168.1.101', '2024-03-18 15:20:00');
