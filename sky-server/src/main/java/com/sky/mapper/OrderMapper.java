package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);
    /**
     * 分页查询用户订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQueryUserOrders(OrdersPageQueryDTO ordersPageQueryDTO);
    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders selectById(Long id);
    /**
     * 更新订单数据
     * @param orders
     */
    void update(Orders orders);
    /**
     * 统计订单状态数量
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer coutStatus(Integer status);
}
