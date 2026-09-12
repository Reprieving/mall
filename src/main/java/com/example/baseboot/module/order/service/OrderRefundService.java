package com.example.baseboot.module.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.order.dto.OrderRefundApplyDTO;
import com.example.baseboot.module.order.dto.OrderRefundAuditDTO;
import com.example.baseboot.module.order.dto.OrderRefundQueryDTO;
import com.example.baseboot.module.order.entity.OrderRefund;
import com.example.baseboot.module.order.vo.OrderRefundVO;

/**
 * 订单退款申请与审批业务接口
 */
public interface OrderRefundService extends IService<OrderRefund> {

    /**
     * 买家发起订单退款申请
     *
     * @param userId   买家用户ID
     * @param orderId  订单ID
     * @param applyDTO 申请参数 (退款原因、说明、金额、类型)
     * @return 退款记录视图对象
     */
    OrderRefundVO applyRefund(Long userId, Long orderId, OrderRefundApplyDTO applyDTO);

    /**
     * 运营端/卖家审批退款申请 (基于退款记录ID)
     *
     * @param auditUserId   审批人ID
     * @param auditUserName 审批人姓名
     * @param refundId      退款记录ID
     * @param auditDTO      审批参数 (通过/驳回、原因备注)
     * @return 审批后的退款视图对象
     */
    OrderRefundVO auditRefund(Long auditUserId, String auditUserName, Long refundId, OrderRefundAuditDTO auditDTO);

    /**
     * 运营端/卖家审批退款申请 (基于订单ID)
     *
     * @param auditUserId   审批人ID
     * @param auditUserName 审批人姓名
     * @param orderId       订单ID
     * @param auditDTO      审批参数 (通过/驳回、原因备注)
     * @return 审批后的退款视图对象
     */
    OrderRefundVO auditRefundByOrderId(Long auditUserId, String auditUserName, Long orderId, OrderRefundAuditDTO auditDTO);

    /**
     * 获取指定订单的最新退款记录
     *
     * @param orderId 订单ID
     * @return 退款记录视图对象 (若无则返回 null)
     */
    OrderRefundVO getRefundByOrderId(Long orderId);

    /**
     * 根据退款ID获取退款详情
     *
     * @param refundId 退款记录ID
     * @return 退款记录视图对象
     */
    OrderRefundVO getRefundById(Long refundId);

    /**
     * 分页查询全平台退款申请列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果集
     */
    CommonPage<OrderRefundVO> pageRefunds(OrderRefundQueryDTO queryDTO);
}
