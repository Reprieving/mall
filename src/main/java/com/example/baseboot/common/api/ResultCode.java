package com.example.baseboot.common.api;

import lombok.Getter;

/**
 * 通用响应状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已过期"),
    FORBIDDEN(403, "没有相关权限"),
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "邮箱已被注册"),
    PASSWORD_ERROR(1003, "密码错误"),
    VERIFY_CODE_ERROR(1004, "验证码错误或已过期"),
    USER_DISABLED(1005, "用户已被禁用"),
    CERT_ALREADY_APPROVED(2001, "您已完成实名认证，无需重复提交"),
    CERT_IN_REVIEW(2002, "实名认证正在审核中，请耐心等待"),
    ID_CARD_INVALID(2003, "身份证号码格式不合法"),
    ID_CARD_OCCUPIED(2004, "该身份证号码已被其他账号绑定"),
    CERT_RECORD_NOT_FOUND(2005, "实名认证记录不存在"),
    CATEGORY_NOT_EXIST(3001, "商品分类不存在"),
    CATEGORY_HAS_CHILDREN(3002, "该分类下存在子分类，无法直接删除"),
    CATEGORY_HAS_PRODUCTS(3003, "该分类下已关联商品，无法直接删除"),
    BRAND_NOT_EXIST(3011, "商品品牌不存在"),
    BRAND_NAME_EXISTS(3012, "品牌名称已存在"),
    BRAND_HAS_PRODUCTS(3013, "该品牌下已关联商品，无法直接删除"),
    SPU_NOT_EXIST(3021, "商品SPU不存在"),
    SPU_CODE_EXISTS(3022, "商品货号已存在"),
    SKU_NOT_EXIST(3031, "商品SKU不存在"),
    SKU_CODE_EXISTS(3032, "SKU编码已存在"),
    SKU_STOCK_NOT_ENOUGH(3033, "商品SKU库存不足"),
    SKU_SPEC_INVALID(3034, "多规格参数配置不合法"),
    SPEC_KEY_NOT_EXIST(3041, "规格项不存在"),
    SPEC_KEY_NAME_EXISTS(3042, "规格项名称已存在"),
    SPEC_KEY_HAS_VALUES(3043, "该规格项下存在规格值，无法直接删除"),
    SPEC_KEY_IN_USE(3044, "该规格项已被商品引用，无法删除"),
    SPEC_VALUE_NOT_EXIST(3045, "规格值不存在"),
    SPEC_VALUE_EXISTS(3046, "规格值已存在"),
    SPEC_VALUE_IN_USE(3047, "该规格值已被商品SKU引用，无法删除"),
    ORDER_NOT_EXIST(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态不符合操作要求"),
    ORDER_PAY_FAILED(4003, "订单支付失败"),
    ORDER_CANCEL_FAILED(4004, "当前状态订单无法取消"),
    ORDER_CANNOT_DELETE(4005, "只能删除已完成或已取消的订单"),
    ORDER_FORBIDDEN(4006, "无权访问该订单"),
    ORDER_ITEM_EMPTY(4007, "订单商品条目不能为空"),
    RECEIVER_INFO_EMPTY(4008, "收货人信息不完整"),
    PRODUCT_OFF_SHELF(4009, "商品已下架或不可售"),
    ADDRESS_NOT_EXIST(4011, "收货地址不存在"),
    LICENSE_NO_INVALID(2006, "统一社会信用代码格式不合法"),
    SHOP_NOT_EXIST(5001, "店铺不存在"),
    SHOP_NAME_EXISTS(5002, "店铺名称已被占用"),
    SHOP_ALREADY_OPENED(5003, "您已提交过开店申请或已拥有店铺"),
    SHOP_CERT_REQUIRED(5004, "开店前请先完成对应主体实名认证"),
    SHOP_STATUS_ERROR(5005, "店铺状态不符合当前操作要求"),
    ADMIN_NOT_EXIST(6001, "管理员账号不存在"),
    ADMIN_USERNAME_EXISTS(6002, "管理员账号已存在"),
    ADMIN_PASSWORD_ERROR(6003, "管理员账号或密码错误"),
    ADMIN_DISABLED(6004, "管理员账号已被禁用"),
    ROLE_NOT_EXIST(6011, "角色不存在"),
    ROLE_CODE_EXISTS(6012, "角色编码已存在"),
    ROLE_IN_USE(6013, "角色已被管理员使用，无法删除");

    private final long code;
    private final String message;

    ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }
}
