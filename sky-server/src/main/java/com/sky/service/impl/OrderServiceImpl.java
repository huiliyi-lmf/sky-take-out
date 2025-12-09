package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Value("${sky.shop.address}")
    private String shopAddress;
    @Value("${sky.baidi.ak}")
    private String ak;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种业务异常（地址簿为空，购物车数据为空）
        AddressBook addressBook= addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            //地址簿为空，抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //检查用户地址是否超出配送范围
        checkOutOfRange(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
        ShoppingCart shoppingCartCondition=new ShoppingCart();
        Long userId= BaseContext.getCurrentId();
        shoppingCartCondition.setUserId(userId);
        //查询当前用户的购物车数据
        List<ShoppingCart> shoppingCartList=shoppingCartMapper.list(shoppingCartCondition);
        if(shoppingCartList==null|| shoppingCartList.isEmpty()){
            //购物车数据为空，抛出业务异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        //生成订单号
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);
        //向订单明细表插入n条数据
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for(ShoppingCart shoppingCart:shoppingCartList){
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        //封装VO对象并返回
        OrderSubmitVO orderSubmitVO=OrderSubmitVO.builder().id(orders.getId()).orderNumber(orders.getNumber())
                               .orderTime(orders.getOrderTime()).orderAmount(orders.getAmount()).build();
        return orderSubmitVO;
    }

    @Override
    public PageResult pageQueryUserOrders(Integer pageNum, Integer pageSize, Integer status) {
        //设置分页参数
        PageHelper.startPage(pageNum,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO=new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        //执行分页查询
        Page<Orders> page= orderMapper.pageQueryUserOrders(ordersPageQueryDTO);
        List<OrderVO> orderVOList=new ArrayList<>();
        //封装订单VO对象
        if(page!=null&&page.getTotal()>0){
            for(Orders orders:page.getResult()){
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                //查询订单明细列表
                List<OrderDetail> orderDetailList= orderDetailMapper.getByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetailList);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(),orderVOList);
    }
    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderDetailById(Long id) {
       OrderVO orderVO=new OrderVO();
       //查询订单数据
       Orders orders= orderMapper.selectById(id);
         if(orders!=null){
              BeanUtils.copyProperties(orders,orderVO);
              //查询订单明细列表
              List<OrderDetail> orderDetailList= orderDetailMapper.getByOrderId(orders.getId());
              orderVO.setOrderDetailList(orderDetailList);
         }
         return orderVO;
    }
    /**
     * 取消订单
     * @param id
     */
    @Override
    public void userCancelById(Long id) {
        Orders ordersDB= orderMapper.selectById(id);
       if(ordersDB==null){
           throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
       }
       //订单状态 1-待付款 2-待接单 3-已接单 4-派送中 5-已完成 6-已取消
        if(ordersDB.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        //订单处于待接单状态下取消，需要进行退款处理
        if(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //TODO 退款处理
//            WeChatPayUtil.refund(
//                    ordersDB.getNumber(),//商户订单号
//                    ordersDB.getNumber(),//商户退款单号
//                    new BigDecimal(0.01),//退款金额
//                    new BigDecimal(0.01));//订单金额
//            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
    /**
     * 再来一单
     * @param id
     * @return
     **/
    @Override
    public void repetition(Long id) {
        Long userId= BaseContext.getCurrentId();
        //查询订单明细数据
        List<OrderDetail> orderDetailList= orderDetailMapper.getByOrderId(id);
        //将原订单明细数据转换为购物车数据
        List<ShoppingCart>shoppingCartList=orderDetailList.stream().map(x->{
                ShoppingCart shoppingCart=new ShoppingCart();
                //将原订单明细数据复制到购物车对象，忽略id字段
                BeanUtils.copyProperties(x,shoppingCart,"id");
                shoppingCart.setUserId(userId);
                shoppingCart.setCreateTime(LocalDateTime.now());
                return shoppingCart;
                }).collect(Collectors.toList());
        //插入到购物车数据
        shoppingCartMapper.insertBatch(shoppingCartList);

     }
     /**
      * 订单搜索
      * @param ordersPageQueryDTO
      * @return
      */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        //执行分页查询
        Page<Orders> page = orderMapper.pageQueryUserOrders(ordersPageQueryDTO);
        List<OrderVO> orderVOList=getOrderVOList(page.getResult());
        return new PageResult(page.getTotal(),orderVOList);
    }
    /**
     * 统计格格状态订单数据
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed= orderMapper.coutStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed= orderMapper.coutStatus(Orders.CONFIRMED);
        Integer deliveryInProgress= orderMapper.coutStatus(Orders.DELIVERY_IN_PROGRESS);

        //将查询结果封装到VO对象并返回
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }
    /**
     * 接单确认
     * @param orderConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO orderConfirmDTO) {
        Orders orders=Orders.builder().id(orderConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }
    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersDB= orderMapper.selectById(ordersRejectionDTO.getId());
        if(ordersDB==null||!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Integer paystatus=ordersDB.getPayStatus();
        //如果订单已支付，则需要进行退款处理
        if(paystatus.equals(Orders.PAID)){
            //TODO 退款处理
        }
        Orders orders=Orders.builder().id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }
    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB= orderMapper.selectById(ordersCancelDTO.getId());
        if(ordersDB==null||ordersDB.getStatus()>Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Integer paystatus=ordersDB.getPayStatus();
        //如果订单已支付，则需要进行退款处理
        if(paystatus.equals(Orders.PAID)){
            //TODO 退款处理
        }
        Orders orders=Orders.builder().id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     *  订单派送
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders ordersDB=new Orders();
        if(ordersDB==null||!ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders=Orders.builder().id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }
    /**
     * 订单完成
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders ordersDB=new Orders();
        if(ordersDB==null||!ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders=Orders.builder().id(ordersDB.getId())
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }
    /**
     * 封装订单VO对象
     * @param ordersList
     * @return
     */
    private List<OrderVO> getOrderVOList(List<Orders> ordersList){
        List<OrderVO> orderVOList=new ArrayList<>();
        //封装订单VO对象
        if(ordersList!=null&&!ordersList.isEmpty()){
            for(Orders orders:ordersList){
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                String orderDishes=getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }
    /**
     * 获取订单菜品字符串
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders){
        List<OrderDetail> orderDetailList= orderDetailMapper.getByOrderId(orders.getId());
        List<String> orderDishList=orderDetailList.stream().map(x->{
            String orderDish=x.getName()+"*"+x.getNumber()+";";
            return orderDish;
        }).collect(Collectors.toList());
        return String.join("",orderDishList);//拼接字符串,形式：菜品名称*数量;菜品名称*数量;
    }
    /**
     * 检查用户地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address){
        Map map= new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);
        //发起请求，获取店铺的经纬度
        String shopCoordinate= HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        JSONObject jsonObject= JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("获取店铺坐标失败，无法下单");
        }
        //数据解析
        JSONObject location =jsonObject.getJSONObject("result").getJSONObject("location"); //店铺经纬度
        String lat=location.getString("lat");
        String lng=location.getString("lng");
        String shopLngLat=lat+","+lng;
        map.put("address",address);
        //发起请求，获取用户地址的经纬度
        String userCoordinate= HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        jsonObject= JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("获取用户坐标失败，无法下单");
        }
        //数据解析
        location =jsonObject.getJSONObject("result").getJSONObject("location"); //用户经纬
        lat=location.getString("lat");
        lng=location.getString("lng");
        String userLngLat=lat+","+lng;
        map.put("origins",shopLngLat);
        map.put("destinations",userLngLat);
        map.put("output","json");
        //路线规划
        String json= HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);
        jsonObject= JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败，无法下单");
        }
        JSONObject result=jsonObject.getJSONObject("result");
        JSONArray jsonArray=(JSONArray) result.get("routes");
        Integer distance=(Integer) ((JSONObject)jsonArray.get(0)).get("distance");
        if(distance>5000){
            throw new OrderBusinessException("超出配送范围，无法下单");
        }
    }

}
