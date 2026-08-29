package com.example.baseboot.module.user.cert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.user.cert.entity.UserCertification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户主体实名认证 Mapper 接口
 */
@Mapper
public interface UserCertificationMapper extends BaseMapper<UserCertification> {
}
