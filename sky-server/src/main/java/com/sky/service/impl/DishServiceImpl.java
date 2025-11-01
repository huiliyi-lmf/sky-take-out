package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDTo
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTo) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTo,dish);
        //保存一条菜品数据
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        //保存n条口味数据到口味表
        List<DishFlavor> flavors = dishDTo.getFlavors();
        if(flavors!=null && flavors.size()>0){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
