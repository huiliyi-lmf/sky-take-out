package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
    /**
     * 分页查询用户订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQueryUserOrders(Integer page, Integer pageSize, Integer status);
    /**
     * 查询订单详情
     * @param id
     * @return
     */
    OrderVO getOrderDetailById(Long id);
    /**
     * 取消订单
     * @param id
     */
    void userCancelById(Long id);
    /**
     * 再来一单
     * @param id
     */
    void repetition(Long id);
    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);
    /**
     * 统计订单数据
     * @return
     */
    OrderStatisticsVO statistics();
    /**
     * 接单确认
     * @param orderConfirmDTO
     */
    void confirm(OrdersConfirmDTO orderConfirmDTO);
    /**
     * 拒单
     * @param orderConfirmDTO
     */
    void rejection(OrdersRejectionDTO orderConfirmDTO);
    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);
    /**
     * 订单派送
     * @param id
     */
    void delivery(Long id);
    /**
     * 订单完成
     * @param id
     */
    void complete(Long id);
}
