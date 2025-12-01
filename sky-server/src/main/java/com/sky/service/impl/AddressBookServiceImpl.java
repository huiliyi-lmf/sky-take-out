package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 根据用户id查询地址簿列表
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }
    /**
     * 新增地址簿
     * @param addressBook
     */
    @Override
    public void save(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }
    /**
     * 根据id查询地址簿
     * @param id
     * @return
     */
    @Override
    public AddressBook selectById(Long id) {
        return addressBookMapper.selectById(id);
    }

    @Override
    public void updateById(AddressBook addressBook) {
        addressBookMapper.updateById(addressBook);
    }
    /**
     * 删除地址簿
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
    /**
     * 设置默认地址
     * @param addressBook
     */
    @Transactional
    @Override
    public void setDefault(AddressBook addressBook) {
        // 1.先将该用户的所有地址设置为非默认地址
        AddressBook updateAddressBook = new AddressBook();
        updateAddressBook.setUserId(BaseContext.getCurrentId());
        updateAddressBook.setIsDefault(0);
        addressBookMapper.updateIsDefaultByUserId(updateAddressBook);
        // 2.再将指定地址设置为默认地址
        addressBook.setIsDefault(1);
        addressBookMapper.updateById(addressBook);
    }

}
