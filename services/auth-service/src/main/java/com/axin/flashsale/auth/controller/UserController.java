package com.axin.flashsale.auth.controller;

import com.axin.flashsale.auth.dto.RegisterRequest;
import com.axin.flashsale.auth.dto.UserVO;
import com.axin.flashsale.auth.entity.User;
import com.axin.flashsale.auth.service.UserService;
import com.axin.flashsale.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterRequest request) {
        User user = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getPhone());
        return Result.success(toVO(user));
    }

    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return Result.fail(401, "未登录");
        }
        User user = userService.getByUsername(userDetails.getUsername());
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.success(toVO(user));
    }

    @PutMapping("/me")
    public Result<UserVO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {
        if (userDetails == null) {
            return Result.fail(401, "未登录");
        }
        User user = userService.getByUsername(userDetails.getUsername());
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        User updated = userService.updateProfile(user.getId(), email, phone);
        return Result.success(toVO(updated));
    }

    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.success(toVO(user));
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRoles(user.getRoles());
        vo.setEnabled(user.getEnabled());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
