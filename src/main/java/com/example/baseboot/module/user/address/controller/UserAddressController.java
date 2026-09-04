package com.example.baseboot.module.user.address.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.module.user.address.dto.AddressCreateDTO;
import com.example.baseboot.module.user.address.dto.AddressUpdateDTO;
import com.example.baseboot.module.user.address.service.UserAddressService;
import com.example.baseboot.module.user.address.vo.AddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收货地址管理控制器
 */
@Tag(name = "19. 买家收货地址 (UserAddressController)", description = "买家收货地址添加、修改、删除、详情、多地址列表与设为默认地址")
@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
@LoginRequired
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 新增收货地址 (若设为默认则自动重置其他地址)
     */
    @Operation(summary = "新增收货地址", description = "买家录入新收货人姓名、手机号、省市区编码及详细门牌地址，支持设为默认")
    @PostMapping
    public CommonResult<AddressVO> createAddress(@Valid @RequestBody AddressCreateDTO createDTO) {
        Long userId = UserContext.getUserId();
        AddressVO vo = userAddressService.createAddress(userId, createDTO);
        return CommonResult.success(vo, "收货地址添加成功");
    }

    /**
     * 修改收货地址
     */
    @Operation(summary = "修改收货地址", description = "修改指定收货地址的联系人、电话与配送位置")
    @PutMapping("/{id}")
    public CommonResult<AddressVO> updateAddress(@PathVariable("id") Long id,
                                                 @Valid @RequestBody AddressUpdateDTO updateDTO) {
        Long userId = UserContext.getUserId();
        AddressVO vo = userAddressService.updateAddress(userId, id, updateDTO);
        return CommonResult.success(vo, "收货地址修改成功");
    }

    /**
     * 删除收货地址
     */
    @Operation(summary = "删除收货地址", description = "删除指定的收货地址记录")
    @DeleteMapping("/{id}")
    public CommonResult<Void> deleteAddress(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        boolean success = userAddressService.deleteAddress(userId, id);
        if (success) {
            return CommonResult.success(null, "收货地址删除成功");
        }
        return CommonResult.failed("收货地址删除失败");
    }

    /**
     * 获取单个收货地址详情
     */
    @Operation(summary = "获取收货地址详情", description = "按地址 ID 获取收货地址详细字段")
    @GetMapping("/{id}")
    public CommonResult<AddressVO> getAddressById(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        AddressVO vo = userAddressService.getAddressById(userId, id);
        return CommonResult.success(vo);
    }

    /**
     * 获取当前用户的所有收货地址列表 (默认地址排在最前)
     */
    @Operation(summary = "获取我的全部收货地址", description = "返回当前买家名下所有的收货地址列表，默认地址置顶排序")
    @GetMapping("/list")
    public CommonResult<List<AddressVO>> listUserAddresses() {
        Long userId = UserContext.getUserId();
        List<AddressVO> list = userAddressService.listUserAddresses(userId);
        return CommonResult.success(list);
    }

    /**
     * 获取当前用户的默认收货地址
     */
    @Operation(summary = "获取默认收货地址", description = "快速获取当前买家配置的默认收货地址，用于下单时快速预填")
    @GetMapping("/default")
    public CommonResult<AddressVO> getDefaultAddress() {
        Long userId = UserContext.getUserId();
        AddressVO vo = userAddressService.getDefaultAddress(userId);
        return CommonResult.success(vo);
    }

    /**
     * 设为默认收货地址
     */
    @Operation(summary = "设为默认收货地址", description = "将指定地址设为默认地址，并自动取消其它地址的默认标识")
    @PutMapping("/{id}/default")
    public CommonResult<Void> setDefaultAddress(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        boolean success = userAddressService.setDefaultAddress(userId, id);
        if (success) {
            return CommonResult.success(null, "默认地址设置成功");
        }
        return CommonResult.failed("默认地址设置失败");
    }
}
