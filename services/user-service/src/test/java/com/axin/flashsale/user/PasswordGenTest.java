package com.axin.flashsale.user;

import com.axin.flashsale.user.entity.User;
import com.axin.flashsale.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class PasswordGenTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void initAdminUser(){
        // 1. 检查是否存在
        // (简单起见，这里先不做复杂判断，你可以手动清空表再跑，或者利用 Unique Key 报错忽略)

        // 2. 生成加密密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("123456");
        System.out.println("Encoded Password: " + encodedPassword);

        // 3. 插入用户
        User user = new User();
        user.setUsername("admin");
        user.setPassword(encodedPassword);
        user.setRoles("ADMIN,USER");
        user.setEnabled(true);

        try {
            userMapper.insert(user);
            System.out.println("Admin user created successfully!");
        } catch (Exception e) {
            System.out.println("User might already exist: " + e.getMessage());
        }
    }

}
