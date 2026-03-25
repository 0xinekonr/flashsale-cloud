package com.axin.flashsale.auth.service;

import com.axin.flashsale.auth.entity.User;
import com.axin.flashsale.auth.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    public User getByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Transactional
    public User register(String username, String password, String email, String phone) {
        // 检查用户名是否已存在
        User existing = getByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setRoles("ROLE_USER");
        user.setEnabled(true);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功, userId={}, username={}", user.getId(), username);
        return user;
    }

    @Transactional
    public User updateProfile(Long userId, String email, String phone) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户信息更新成功, userId={}", userId);
        return user;
    }
}
