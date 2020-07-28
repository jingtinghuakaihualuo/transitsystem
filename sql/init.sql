-- 设备信息表
create table t_equipment_info(id int auto_increment primary key comment '表id',
sno varchar(30) not null comment '设备编号',
mac varchar(20) not null comment '设备mac地址',
token_id bigint default null comment '设备当前提供的连接id',
status tinyint  default 0 comment '0:激活导入状态;1:连接状态;2:关闭状态;3:禁用状态',
create_time bigint comment '记录创建时间',
update_time bigint comment '设备最后一次修改时间',
key idx_t_equipment_info_sno_mac (sno, mac) using BTREE,
key idx_t_equipment_info_token_id (token_id) using BTREE,
key idx_t_equipment_info_update_time (update_time) using BTREE
);

-- 设备流量统计表
create table t_equipment_flow(id int auto_increment primary key,
sno varchar(30) not null comment '设备编号',
mac varchar(20) not null comment '设备mac地址',
size float not null default 0.0 comment '上传文件的大小，单位（KB）',
create_time bigint comment '记录创建时间',
update_time bigint comment '设备最后一次修改时间',
key idx_t_equipment_info_sno_mac (sno, mac) using BTREE,
key idx_t_equipment_info_update_time (update_time) using BTREE
);