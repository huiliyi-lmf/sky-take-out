package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     *动态查询购物车列表
     *
     * @param shoppingCart 购物车实体对象
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);
    /**
     * 新增购物车
     *
     * @param shoppingCart 购物车实体对象
     */
    @Insert("insert into shopping_cart(user_id,dish_id,setmeal_id,name,image,amount,number,create_time,dish_flavor) " +
            "values(#{userId},#{dishId},#{setmealId},#{name},#{image},#{amount},#{number},#{createTime},#{dishFlavor})")
    void insert(ShoppingCart shoppingCart);
}
