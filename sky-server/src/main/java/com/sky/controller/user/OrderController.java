package com.sky.controller.user;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户订单相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户端提交订单
     *
     * @param ordersSubmitDTO 提交订单信息
     * @return 提交订单结果
     */
    @PostMapping("/submit")
    @ApiOperation("用户端提交订单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户端提交订单: {}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 查询历史订单
     *
     * @param page     当前页
     * @param pageSize 每页显示条数
     * @param status   订单状态
     * @return 历史订单分页结果
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    public Result<PageResult> page(Integer page, Integer pageSize, Integer status) {
        {
            log.info("查询历史订单: page={}, pageSize={}, status={}", page, pageSize, status);
            PageResult pageResult = orderService.pageQueryUserOrders(page, pageSize, status);
            return Result.success(pageResult);
        }
    }
    /**
     * 查询订单详情
     * @return 订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> detail(@PathVariable Long id) {
        log.info("查询订单详情: id={}", id);
        OrderVO orderVO = orderService.getOrderDetailById(id);
        return Result.success(orderVO);
    }
    /**
     * 取消订单
     * @param id 订单id
     * @return 订单取消结果
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable("id") Long id) {
        log.info("取消订单: id={}", id);
        orderService.userCancelById(id);
        return Result.success();
    }
    /**
     * 再来一单
     * @param id 订单id
     * @return 再来一单结果
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id) {
        log.info("再来一单: id={}", id);
        orderService.repetition(id);
        return Result.success();
    }
}
