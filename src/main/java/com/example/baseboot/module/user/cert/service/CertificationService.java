package com.example.baseboot.module.user.cert.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.user.cert.dto.*;
import com.example.baseboot.module.user.cert.entity.UserCertification;
import com.example.baseboot.module.user.cert.vo.UserCertVO;

/**
 * 用户主体实名认证业务接口
 */
public interface CertificationService extends IService<UserCertification> {

    /**
     * 提交个人实名认证
     */
    UserCertVO submitPersonalCert(Long userId, PersonalCertDTO certDTO);

    /**
     * 提交个体工商户认证
     */
    UserCertVO submitIndividualCert(Long userId, IndividualCertDTO certDTO);

    /**
     * 提交企业实名认证
     */
    UserCertVO submitEnterpriseCert(Long userId, EnterpriseCertDTO certDTO);

    /**
     * 查询指定用户的认证信息
     */
    UserCertVO getCertificationByUserId(Long userId);

    /**
     * 审核实名认证申请
     */
    UserCertVO auditCertification(CertAuditDTO auditDTO);

    /**
     * 分页查询认证审核列表
     */
    CommonPage<UserCertVO> pageCertifications(CertQueryDTO queryDTO);
}
