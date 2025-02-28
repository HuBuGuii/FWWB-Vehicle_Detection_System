// File: src/main/java/com/fwwb/vehicledetection/controller/user/UserController.java
package com.fwwb.vehicledetection.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fwwb.vehicledetection.domain.model.User;
import com.fwwb.vehicledetection.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 仅管理员可以访问
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{pageNum}")
    public Page<User> listUsers(@PathVariable int pageNum) {
        return userService.page(new Page<>(pageNum, 10));
    }

    // 其他管理、修改、删除等接口也可以通过 @PreAuthorize 注解进行权限限制
    // 例如：
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Long userId) {
        return userService.removeById(userId) ? "删除成功" : "删除失败";
    }

    // 普通用户之间的接口可不加此注解或加上其他限制（如仅本人访问）
    @PutMapping("/updateProfile")
    public String updateProfile(@RequestBody User user) {
        return userService.updateById(user) ? "修改成功" : "修改失败";
    }
}