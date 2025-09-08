package org.aione.plugin.sqlmarking.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.aione.plugin.sqlmarking.example.entity.User;
import org.aione.plugin.sqlmarking.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务类
 * 
 * @author Billy
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 根据姓名查询用户
     */
    public List<User> findByName(String name) {
        return baseMapper.findByName(name);
    }

    /**
     * 根据邮箱查询用户
     */
    public User findByEmail(String email) {
        return baseMapper.findByEmail(email);
    }

    /**
     * 更新用户状态
     */
    public int updateStatus(Long id, String status) {
        return baseMapper.updateStatus(id, status);
    }

    /**
     * 根据状态删除用户
     */
    public int deleteByStatus(String status) {
        return baseMapper.deleteByStatus(status);
    }

    /**
     * 插入用户
     */
    public int insertUser(User user) {
        return baseMapper.insertUser(user);
    }

    /**
     * 根据年龄范围查询用户
     */
    public List<User> findByAgeRange(Integer minAge, Integer maxAge) {
        return baseMapper.findByAgeRange(minAge, maxAge);
    }

    /**
     * 条件查询活跃用户
     */
    public List<User> findActiveUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "ACTIVE");
        return list(queryWrapper);
    }

    /**
     * 批量更新用户状态
     */
    public boolean batchUpdateStatus(String oldStatus, String newStatus) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("status", oldStatus)
                     .set("status", newStatus);
        return update(null, updateWrapper);
    }
}