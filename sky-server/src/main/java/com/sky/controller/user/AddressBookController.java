package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 根据用户id查询地址簿列表
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据用户id查询地址簿列表")
    public Result<List<AddressBook>> list() {
        AddressBook addressBook = new AddressBook();
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        List<AddressBook> addressBooks = addressBookService.list(addressBook);
        return Result.success(addressBooks);
    }
    /**
     * 新增地址簿
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址簿")
    public Result save(@RequestBody AddressBook addressBook) {
        addressBookService.save(addressBook);
        return Result.success();
    }
    /**
     * 根据id查询地址簿
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址簿")
    public Result<AddressBook> selectById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.selectById(id);
        return Result.success(addressBook);
    }
    /**
     * 修改地址簿
     * @param addressBook
     * @return
     */
    @PutMapping
    @ApiOperation("修改地址簿")
    public Result updateById(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return Result.success();
    }
    /**
     * 根据id删除地址簿
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址簿")
    public Result deleteById(Long id) {
        addressBookService.deleteById(id);
        return Result.success();
    }
    /**
     * 获取默认地址
     * @return
     */
    @GetMapping("/default")
    @ApiOperation("获取默认地址")
    public Result<AddressBook> getDefault() {
       AddressBook addressBook = new AddressBook();
       Long userId = BaseContext.getCurrentId();
       addressBook.setUserId(userId);
       addressBook.setIsDefault(1);
       List<AddressBook> addressBooks = addressBookService.list(addressBook);
       if (addressBooks != null && !addressBooks.isEmpty()) {
           return Result.success(addressBooks.get(0));
       } else {
           return Result.error("没有找到默认地址");
       }
    }
    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook) {
        addressBookService.setDefault(addressBook);
        return  Result.success();
    }

}
