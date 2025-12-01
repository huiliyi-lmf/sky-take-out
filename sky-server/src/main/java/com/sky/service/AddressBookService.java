package com.sky.service;

import com.sky.entity.AddressBook;
import java.util.List;

public interface AddressBookService {

    /**
     * 根据用户id查询地址簿列表
     * @param addressBook
     */
    List<AddressBook> list(AddressBook addressBook);
    /**
     * 新增地址簿
     * @param addressBook
     */
    void save(AddressBook addressBook);
    /**
     * 根据id查询地址簿
     * @param id
     */
    AddressBook selectById(Long id);
    /**
     * 修改地址簿
     * @param addressBook
     */
    void updateById(AddressBook addressBook);
    /**
     * 根据id删除地址簿
     * @param id
     */
    void deleteById(Long id);
    /**
     * 设置默认地址
     * @param addressBook
     */
    void setDefault(AddressBook addressBook);
}
