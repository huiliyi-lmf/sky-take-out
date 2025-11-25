package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);
    /**
     * 根据id查询套餐信息和对应的菜品信息
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);
    /**
     * 修改套餐信息，同时修改套餐和菜品的关联关系
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);
    /**
     * 批量删除套餐信息，同时删除套餐和菜品的关联关系
     * @param ids
     */
    void delete(List<Long> ids);
    /**
     * 根据条件查询套餐列表
     * @param setmeal
     * @return
     */
    void startOrStop(Integer status, Long id);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);
    /**
     * 根据套餐id查询对应的菜品信息
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
