package com.example.baseboot.module.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.admin.system.entity.SysAdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统管理员 Mapper 接口
 */
@Mapper
public interface SysAdminUserMapper extends BaseMapper<SysAdminUser> {
}
