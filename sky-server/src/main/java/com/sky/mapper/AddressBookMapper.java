package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AddressBookMapper {

    /**
     * 根据用户id查询地址簿列表
     * @param addressBook
     */
    List<AddressBook> list(AddressBook addressBook);
    /**
     * 新增地址簿
     * @param addressBook
     */
    @Insert("insert into address_book (user_id, consignee, sex, phone, province_code, province_name, city_code, " +
            "city_name, district_code, district_name, detail, label, is_default) " +
            "values (#{userId}, #{consignee}, #{sex}, #{phone}, #{provinceCode}, #{provinceName}, #{cityCode}, " +
            "#{cityName}, #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    void insert(AddressBook addressBook);
    /**
     * 根据id查询地址簿
     * @param id
     * @return
     */
    @Select("select * from address_book where id = #{id}")
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
    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);
    /**
     * 将该用户的所有地址设为非默认地址
     * @param updateAddressBook
     */
    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void updateIsDefaultByUserId(AddressBook updateAddressBook);
}
