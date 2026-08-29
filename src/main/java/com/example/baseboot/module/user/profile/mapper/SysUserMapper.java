package com.example.baseboot.module.user.profile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.user.profile.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper 接口 (继承 MyBatis-Plus BaseMapper)
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 基础 CRUD 方法已由 MyBatis-Plus BaseMapper 提供
}
