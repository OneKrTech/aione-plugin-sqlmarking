package org.aione.plugin.sqlmarking.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.aione.plugin.sqlmarking.example.entity.User;

import java.util.List;

/**
 * 用户Mapper接口，用于测试SQL标记功能
 * 
 * @author Billy
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 自定义查询方法 - 根据姓名查询用户
     */
    @Select("SELECT * FROM test_user WHERE name = #{name}")
    List<User> findByName(String name);

    /**
     * 自定义查询方法 - 根据邮箱查询用户
     */
    @Select("SELECT * FROM test_user WHERE email = #{email}")
    User findByEmail(String email);

    /**
     * 自定义更新方法 - 更新用户状态
     */
    @Update("UPDATE test_user SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id")Long id,@Param("status") String status);

    /**
     * 自定义删除方法 - 根据状态删除用户
     */
    @Delete("DELETE FROM test_user WHERE status = #{status}")
    int deleteByStatus(String status);

    /**
     * 自定义插入方法 - 批量插入用户
     */
    @Insert("INSERT INTO test_user (name, email, age, status) VALUES (#{name}, #{email}, #{age}, #{status})")
    int insertUser(User user);

    /**
     * 复杂查询 - 根据年龄范围查询用户
     */
    @Select("SELECT * FROM test_user WHERE age BETWEEN #{minAge} AND #{maxAge} ORDER BY age")
    List<User> findByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    List<User> findByIds(@Param("ids") List<Long> ids);

}