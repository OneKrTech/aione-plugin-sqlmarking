package org.onekr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 测试用户实体类
 * 
 * @author Billy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("test_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String email;

    private Integer age;

    private String status;

    public User(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.status = "ACTIVE";
    }
}