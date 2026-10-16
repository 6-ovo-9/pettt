-- ==============================================================
-- 数据库初始化脚本: pet_fresh_db
-- ==============================================================
CREATE DATABASE IF NOT EXISTS `pet_fresh_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `pet_fresh_db`;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名/账号',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `role` VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER' COMMENT '角色: ROLE_USER-宠主, ROLE_ADMIN-系统管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与管理员基础表';

INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `phone`, `role`, `status`) VALUES
(1, 'admin', '123456', '系统管理员', '13800000000', 'ROLE_ADMIN', 1),
(2, 'petlover', '123456', '铲屎官 (宠主用户)', '13900001111', 'ROLE_USER', 1);

DROP TABLE IF EXISTS `pet_profile`;
CREATE TABLE `pet_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '宠物档案ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `pet_name` VARCHAR(50) NOT NULL COMMENT '宠物名字',
  `species` VARCHAR(20) NOT NULL COMMENT '物种: CAT-猫, DOG-狗',
  `breed` VARCHAR(50) DEFAULT '中华田园犬/猫' COMMENT '品种',
  `age_months` INT NOT NULL COMMENT '年龄(月)',
  `weight` DECIMAL(5,2) NOT NULL COMMENT '体重(kg)',
  `activity_level` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '日常运动量: LOW-低, NORMAL-适中, HIGH-高',
  `is_neutered` TINYINT(1) DEFAULT 0 COMMENT '是否绝育: 0-未绝育, 1-已绝育',
  `allergens` VARCHAR(255) DEFAULT NULL COMMENT '过敏食材',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建档时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_pet` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物健康档案表';

INSERT INTO `pet_profile` (`id`, `user_id`, `pet_name`, `species`, `breed`, `age_months`, `weight`, `activity_level`, `is_neutered`, `allergens`) VALUES
(1, 2, '乐乐', 'DOG', '柯基', 24, 11.50, 'HIGH', 1, '谷物'),
(2, 2, '咪咪', 'CAT', '英短蓝猫', 14, 4.20, 'NORMAL', 1, '海鲜');

DROP TABLE IF EXISTS `fresh_goods`;
CREATE TABLE `fresh_goods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '鲜食商品ID',
  `goods_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category` VARCHAR(50) NOT NULL COMMENT '分类: 成犬鲜食/幼猫鲜食/生骨肉配方',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价(元)',
  `shelf_life_days` INT NOT NULL DEFAULT 7 COMMENT '保质期天数',
  `target_species` VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '适用物种',
  `protein_ratio` DECIMAL(5,2) DEFAULT NULL COMMENT '粗蛋白(%)',
  `fat_ratio` DECIMAL(5,2) DEFAULT NULL COMMENT '粗脂肪(%)',
  `ingredients` TEXT COMMENT '配料说明',
  `status` TINYINT DEFAULT 1 COMMENT '1-上架, 0-下架',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鲜食商品表';

INSERT INTO `fresh_goods` (`id`, `goods_name`, `category`, `price`, `shelf_life_days`, `target_species`, `protein_ratio`, `fat_ratio`, `ingredients`) VALUES
(1, '草饲牛肉南瓜高能鲜肉餐包', '成犬鲜食', 28.50, 7, 'DOG', 32.00, 16.50, '鲜牛肉、牛心、南瓜、菠菜、胡萝卜、深海鱼油'),
(2, '三文鱼鸡肉低敏全价生鲜泥', '幼猫鲜食', 25.00, 5, 'CAT', 42.00, 20.00, '去骨三文鱼肉、鸡胸肉、牛磺酸、南极磷虾油'),
(3, '鸭肉梨清热降火去泪痕鲜食', '成犬鲜食', 22.00, 7, 'DOG', 29.50, 14.00, '鸭肉、鸭心、雪梨、西兰花、亚麻籽粉'),
(4, '兔肉蔓越莓泌尿呵护猫用鲜食', '成猫鲜食', 30.00, 5, 'CAT', 44.00, 18.00, '鲜兔肉、鸡肝、蔓越莓提取物、益生元');

DROP TABLE IF EXISTS `batch_inventory`;
CREATE TABLE `batch_inventory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '批次ID',
  `goods_id` BIGINT NOT NULL COMMENT '关联商品ID',
  `batch_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '生产批次编号',
  `produce_date` DATE NOT NULL COMMENT '生产日期',
  `expire_date` DATE NOT NULL COMMENT '到期日期',
  `initial_stock` INT NOT NULL DEFAULT 0 COMMENT '初始入库数量',
  `current_stock` INT NOT NULL DEFAULT 0 COMMENT '当前可用库存数量',
  `storage_location` VARCHAR(50) DEFAULT '冷冻库A区-01架' COMMENT '存储货位',
  `warning_status` TINYINT DEFAULT 0 COMMENT '效期状态: 0-正常, 1-临期预警, 2-过期锁定',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  PRIMARY KEY (`id`),
  INDEX `idx_goods_expire` (`goods_id`, `expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='鲜食批次库存与效期表';

INSERT INTO `batch_inventory` (`goods_id`, `batch_no`, `produce_date`, `expire_date`, `initial_stock`, `current_stock`, `storage_location`, `warning_status`) VALUES
(1, 'B2026101501', '2026-10-15', '2026-10-22', 100, 68, '冷藏库B区-02', 0),
(1, 'B2026101201', '2026-10-12', '2026-10-19', 50, 12, '冷藏库B区-01', 1),
(2, 'B2026101401', '2026-10-14', '2026-10-19', 80, 18, '冷藏库A区-01', 1),
(3, 'B2026101601', '2026-10-16', '2026-10-23', 120, 120, '冷藏库B区-03', 0),
(4, 'B2026100901', '2026-10-09', '2026-10-14', 40, 0, '冷藏库A区-03', 2);
