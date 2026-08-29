package com.example.baseboot.module.user.cert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.user.cert.dto.*;
import com.example.baseboot.module.user.cert.entity.UserCertification;
import com.example.baseboot.module.user.cert.mapper.UserCertificationMapper;
import com.example.baseboot.module.user.cert.service.CertificationService;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户主体实名认证业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificationServiceImpl extends ServiceImpl<UserCertificationMapper, UserCertification> implements CertificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCertVO submitPersonalCert(Long userId, PersonalCertDTO certDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (certDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "个人认证参数不能为空");
        }

        UserCertification existing = this.getOne(new LambdaQueryWrapper<UserCertification>().eq(UserCertification::getUserId, userId));
        checkExistingCert(existing);

        String idCard = certDTO.getIdCard().trim().toUpperCase();
        checkIdCardOccupied(userId, idCard);

        UserCertification cert = existing != null ? existing : new UserCertification();
        cert.setUserId(userId);
        cert.setCertType(1); // 1-个人
        cert.setRealName(certDTO.getRealName().trim());
        cert.setIdCard(idCard);
        cert.setIdCardFrontPic(certDTO.getIdCardFrontPic().trim());
        cert.setIdCardBackPic(certDTO.getIdCardBackPic().trim());
        cert.setCompanyName(null);
        cert.setBusinessLicenseNo(null);
        cert.setBusinessLicensePic(null);
        cert.setCompanyAddress(null);
        cert.setStatus(0); // 0-待审核
        cert.setAuditRemark(null);
        cert.setAuditTime(null);
        cert.setUpdateTime(LocalDateTime.now());

        if (existing == null) {
            cert.setCreateTime(LocalDateTime.now());
            this.save(cert);
        } else {
            this.updateById(cert);
        }

        return UserCertVO.fromEntity(cert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCertVO submitIndividualCert(Long userId, IndividualCertDTO certDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (certDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "个体工商户认证参数不能为空");
        }

        UserCertification existing = this.getOne(new LambdaQueryWrapper<UserCertification>().eq(UserCertification::getUserId, userId));
        checkExistingCert(existing);

        String idCard = certDTO.getIdCard().trim().toUpperCase();
        checkIdCardOccupied(userId, idCard);

        UserCertification cert = existing != null ? existing : new UserCertification();
        cert.setUserId(userId);
        cert.setCertType(2); // 2-个体工商户
        cert.setRealName(certDTO.getRealName().trim());
        cert.setIdCard(idCard);
        cert.setIdCardFrontPic(certDTO.getIdCardFrontPic().trim());
        cert.setIdCardBackPic(certDTO.getIdCardBackPic().trim());
        cert.setCompanyName(certDTO.getCompanyName().trim());
        cert.setBusinessLicenseNo(certDTO.getBusinessLicenseNo().trim().toUpperCase());
        cert.setBusinessLicensePic(certDTO.getBusinessLicensePic().trim());
        cert.setCompanyAddress(certDTO.getCompanyAddress());
        cert.setStatus(0);
        cert.setAuditRemark(null);
        cert.setAuditTime(null);
        cert.setUpdateTime(LocalDateTime.now());

        if (existing == null) {
            cert.setCreateTime(LocalDateTime.now());
            this.save(cert);
        } else {
            this.updateById(cert);
        }

        return UserCertVO.fromEntity(cert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCertVO submitEnterpriseCert(Long userId, EnterpriseCertDTO certDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (certDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "企业认证参数不能为空");
        }

        UserCertification existing = this.getOne(new LambdaQueryWrapper<UserCertification>().eq(UserCertification::getUserId, userId));
        checkExistingCert(existing);

        String idCard = certDTO.getIdCard().trim().toUpperCase();
        checkIdCardOccupied(userId, idCard);

        UserCertification cert = existing != null ? existing : new UserCertification();
        cert.setUserId(userId);
        cert.setCertType(3); // 3-企业
        cert.setRealName(certDTO.getRealName().trim());
        cert.setIdCard(idCard);
        cert.setIdCardFrontPic(certDTO.getIdCardFrontPic().trim());
        cert.setIdCardBackPic(certDTO.getIdCardBackPic().trim());
        cert.setCompanyName(certDTO.getCompanyName().trim());
        cert.setBusinessLicenseNo(certDTO.getBusinessLicenseNo().trim().toUpperCase());
        cert.setBusinessLicensePic(certDTO.getBusinessLicensePic().trim());
        cert.setCompanyAddress(certDTO.getCompanyAddress().trim());
        cert.setStatus(0);
        cert.setAuditRemark(null);
        cert.setAuditTime(null);
        cert.setUpdateTime(LocalDateTime.now());

        if (existing == null) {
            cert.setCreateTime(LocalDateTime.now());
            this.save(cert);
        } else {
            this.updateById(cert);
        }

        return UserCertVO.fromEntity(cert);
    }

    @Override
    public UserCertVO getCertificationByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        UserCertification cert = this.getOne(new LambdaQueryWrapper<UserCertification>().eq(UserCertification::getUserId, userId));
        return UserCertVO.fromEntity(cert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCertVO auditCertification(CertAuditDTO auditDTO) {
        if (auditDTO == null || auditDTO.getId() == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "认证ID不能为空");
        }

        UserCertification cert = this.getById(auditDTO.getId());
        if (cert == null) {
            throw new BusinessException(ResultCode.CERT_RECORD_NOT_FOUND);
        }

        int status = auditDTO.getStatus();
        if (status != 1 && status != 2) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "审核状态非法 (1-通过, 2-驳回)");
        }

        if (status == 2 && !StringUtils.hasText(auditDTO.getAuditRemark())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "审核驳回时必须填写驳回原因");
        }

        cert.setStatus(status);
        cert.setAuditRemark(auditDTO.getAuditRemark());
        cert.setAuditTime(LocalDateTime.now());
        cert.setUpdateTime(LocalDateTime.now());

        this.updateById(cert);
        return UserCertVO.fromEntity(cert);
    }

    @Override
    public CommonPage<UserCertVO> pageCertifications(CertQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new CertQueryDTO();
        }

        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<UserCertification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserCertification> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getCertType() != null) {
            wrapper.eq(UserCertification::getCertType, queryDTO.getCertType());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(UserCertification::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String kw = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(UserCertification::getRealName, kw)
                    .or().like(UserCertification::getCompanyName, kw)
                    .or().like(UserCertification::getIdCard, kw));
        }

        wrapper.orderByDesc(UserCertification::getId);
        Page<UserCertification> resultPage = this.page(page, wrapper);

        if (CollectionUtils.isEmpty(resultPage.getRecords())) {
            return CommonPage.restPage(resultPage, Collections.emptyList());
        }

        List<UserCertVO> voList = resultPage.getRecords().stream()
                .map(UserCertVO::fromEntity)
                .collect(Collectors.toList());

        return CommonPage.restPage(resultPage, voList);
    }

    private void checkExistingCert(UserCertification existing) {
        if (existing != null) {
            if (existing.getStatus() == 1) {
                throw new BusinessException(ResultCode.CERT_ALREADY_APPROVED);
            }
            if (existing.getStatus() == 0) {
                throw new BusinessException(ResultCode.CERT_IN_REVIEW);
            }
        }
    }

    private void checkIdCardOccupied(Long userId, String idCard) {
        Long count = this.count(new LambdaQueryWrapper<UserCertification>()
                .eq(UserCertification::getIdCard, idCard)
                .eq(UserCertification::getStatus, 1)
                .ne(UserCertification::getUserId, userId));
        if (count > 0) {
            throw new BusinessException(ResultCode.ID_CARD_OCCUPIED);
        }
    }
}
