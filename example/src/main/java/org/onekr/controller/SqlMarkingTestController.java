package org.onekr.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.onekr.entity.User;
import org.onekr.plugin.sqlmarking.mybatis.SqlMarkingConfig;
import org.onekr.plugin.sqlmarking.mybatis.SqlMarkingContext;
import org.onekr.plugin.sqlmarking.mybatis.SqlMarkingInterceptor;
import org.onekr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis Plus SQL标记功能验证Controller
 * 将原来的单元测试转换为REST API接口
 *
 * @author Billy
 */
@RestController
@RequestMapping("/api/sql-marking/test")
@Slf4j
public class SqlMarkingTestController {

    @Autowired
    private UserService userService;

    @Autowired
    private SqlMarkingInterceptor sqlMarkingInterceptor;

    /**
     * 测试INSERT操作的SQL标记功能
     */
    @GetMapping("/insert")
    public Map<String, Object> testInsertSqlMarking() {
        log.info("=== 开始测试INSERT操作的SQL标记功能 ===");

        Map<String, Object> result = new HashMap<>();

        // 设置自定义标记信息
        ConcurrentHashMap<String, Object> customInfo = new ConcurrentHashMap<>();
        customInfo.put("operation", "insert_test");
        customInfo.put("testCase", "testInsertSqlMarking");

        SqlMarkingContext context = new SqlMarkingContext();
        context.setUserId("test_user_001");
        context.setCustomInfo(customInfo);
        SqlMarkingContext.setCurrentContext(context);

        try {
            long initialExecutionCount = sqlMarkingInterceptor.getExecutionCount();

            // 执行INSERT操作 - 使用MyBatis Plus的insert方法
            User newUser = new User("测试用户", "test@example.com", 25);
            int insertResult = userService.save(newUser) ? 1 : 0;

            // 验证插入成功
            result.put("insertResult", insertResult);
            result.put("newUserId", newUser.getId());

            // 验证SQL标记拦截器被调用
            long currentExecutionCount = sqlMarkingInterceptor.getExecutionCount();
            boolean interceptorCalled = currentExecutionCount > initialExecutionCount;
            result.put("interceptorCalled", interceptorCalled);
            result.put("executionCountIncrease", currentExecutionCount - initialExecutionCount);

            // 执行自定义INSERT方法
            User customUser = new User("自定义插入用户", "custom@example.com", 30);
            int customResult = userService.insertUser(customUser);
            result.put("customInsertResult", customResult);

            // 验证执行计数再次增加
            long finalExecutionCount = sqlMarkingInterceptor.getExecutionCount();
            boolean customInterceptorCalled = finalExecutionCount > currentExecutionCount;
            result.put("customInterceptorCalled", customInterceptorCalled);
            result.put("totalExecutionIncrease", finalExecutionCount - initialExecutionCount);

            result.put("success", true);
            result.put("message", "INSERT操作SQL标记测试完成");

            log.info("INSERT操作SQL标记测试结果: {}", result);

        } catch (Exception e) {
            log.error("INSERT操作SQL标记测试异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        } finally {
            SqlMarkingContext.clearCurrentContext();
        }

        log.info("=== INSERT操作SQL标记测试结束 ===");
        return result;
    }

    /**
     * 测试SELECT操作的SQL标记功能
     */
    @GetMapping("/select")
    public Map<String, Object> testSelectSqlMarking() {
        log.info("=== 开始测试SELECT操作的SQL标记功能 ===");

        Map<String, Object> result = new HashMap<>();

        // 设置自定义标记信息
        ConcurrentHashMap<String, Object> customInfo = new ConcurrentHashMap<>();
        customInfo.put("operation", "select_test");
        customInfo.put("testCase", "testSelectSqlMarking");

        SqlMarkingContext context = new SqlMarkingContext();
        context.setUserId("test_user_002");
        context.setCustomInfo(customInfo);
        SqlMarkingContext.setCurrentContext(context);

        try {
            long beforeCount = sqlMarkingInterceptor.getExecutionCount();

            // 执行SELECT操作 - 使用MyBatis Plus的list方法
            List<User> allUsers = userService.list();
            result.put("allUsersCount", allUsers.size());

            // 验证SQL标记拦截器被调用
            long afterSelectAllCount = sqlMarkingInterceptor.getExecutionCount();
            boolean selectAllIntercepted = afterSelectAllCount > beforeCount;
            result.put("selectAllIntercepted", selectAllIntercepted);

            // 执行条件查询
            List<User> activeUsers = userService.findActiveUsers();
            result.put("activeUsersCount", activeUsers.size());

            // 验证执行计数增加
            long afterConditionalCount = sqlMarkingInterceptor.getExecutionCount();
            boolean conditionalIntercepted = afterConditionalCount > afterSelectAllCount;
            result.put("conditionalIntercepted", conditionalIntercepted);

            // 执行自定义SELECT方法
            List<User> usersByName = userService.findByName("测试用户");
            result.put("usersByNameCount", usersByName.size());

            // 验证自定义查询也被标记
            long afterCustomCount = sqlMarkingInterceptor.getExecutionCount();
            boolean customSelectIntercepted = afterCustomCount > afterConditionalCount;
            result.put("customSelectIntercepted", customSelectIntercepted);

            // 执行复杂查询
            List<User> usersByAge = userService.findByAgeRange(20, 35);
            result.put("usersByAgeCount", usersByAge.size());

            long finalCount = sqlMarkingInterceptor.getExecutionCount();
            boolean complexQueryIntercepted = finalCount > afterCustomCount;
            result.put("complexQueryIntercepted", complexQueryIntercepted);
            result.put("totalExecutionIncrease", finalCount - beforeCount);

            result.put("success", true);
            result.put("message", "SELECT操作SQL标记测试完成");

            log.info("SELECT操作SQL标记测试结果: {}", result);

        } catch (Exception e) {
            log.error("SELECT操作SQL标记测试异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        } finally {
            SqlMarkingContext.clearCurrentContext();
        }

        log.info("=== SELECT操作SQL标记测试结束 ===");
        return result;
    }

    /**
     * 测试UPDATE操作的SQL标记功能
     */
    @GetMapping("/update")
    public Map<String, Object> testUpdateSqlMarking() {
        log.info("=== 开始测试UPDATE操作的SQL标记功能 ===");

        Map<String, Object> result = new HashMap<>();

        // 设置自定义标记信息
        ConcurrentHashMap<String, Object> customInfo = new ConcurrentHashMap<>();
        customInfo.put("operation", "update_test");
        customInfo.put("testCase", "testUpdateSqlMarking");

        SqlMarkingContext context = new SqlMarkingContext();
        context.setUserId("test_user_003");
        context.setCustomInfo(customInfo);
        SqlMarkingContext.setCurrentContext(context);

        try {
            long beforeCount = sqlMarkingInterceptor.getExecutionCount();

            // 先查询一个用户
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", "测试用户").last("LIMIT 1");
            User user = userService.getOne(queryWrapper);

            long afterSelectCount = sqlMarkingInterceptor.getExecutionCount();
            boolean selectIntercepted = afterSelectCount > beforeCount;
            result.put("selectIntercepted", selectIntercepted);

            if (user != null) {
                // 执行UPDATE操作 - 使用MyBatis Plus的updateById方法
                user.setAge(26);
                user.setEmail("updated_" + System.currentTimeMillis() + "@test.com");
                boolean updateResult = userService.updateById(user);
                result.put("updateResult", updateResult);

                // 验证UPDATE操作触发SQL标记
                long afterUpdateCount = sqlMarkingInterceptor.getExecutionCount();
                boolean updateIntercepted = afterUpdateCount > afterSelectCount;
                result.put("updateIntercepted", updateIntercepted);

                // 执行批量更新
                boolean batchUpdateResult = userService.batchUpdateStatus("ACTIVE", "UPDATED");
                result.put("batchUpdateResult", batchUpdateResult);

                // 验证批量更新也被标记
                long afterBatchUpdateCount = sqlMarkingInterceptor.getExecutionCount();
                boolean batchUpdateIntercepted = afterBatchUpdateCount > afterUpdateCount;
                result.put("batchUpdateIntercepted", batchUpdateIntercepted);

                // 执行自定义UPDATE方法
                int customUpdateResult = userService.updateStatus(user.getId(), "CUSTOM_UPDATED");
                result.put("customUpdateResult", customUpdateResult);

                // 验证自定义更新也被标记
                long finalCount = sqlMarkingInterceptor.getExecutionCount();
                boolean customUpdateIntercepted = finalCount > afterBatchUpdateCount;
                result.put("customUpdateIntercepted", customUpdateIntercepted);
                result.put("totalExecutionIncrease", finalCount - beforeCount);

            } else {
                result.put("message", "未找到测试用户，请先执行INSERT测试");
            }

            result.put("success", true);
            result.put("message", "UPDATE操作SQL标记测试完成");

            log.info("UPDATE操作SQL标记测试结果: {}", result);

        } catch (Exception e) {
            log.error("UPDATE操作SQL标记测试异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        } finally {
            SqlMarkingContext.clearCurrentContext();
        }

        log.info("=== UPDATE操作SQL标记测试结束 ===");
        return result;
    }

    /**
     * 测试DELETE操作的SQL标记功能
     */
    @GetMapping("/delete")
    public Map<String, Object> testDeleteSqlMarking() {
        log.info("=== 开始测试DELETE操作的SQL标记功能 ===");

        Map<String, Object> result = new HashMap<>();

        // 设置自定义标记信息
        ConcurrentHashMap<String, Object> customInfo = new ConcurrentHashMap<>();
        customInfo.put("operation", "delete_test");
        customInfo.put("testCase", "testDeleteSqlMarking");

        SqlMarkingContext context = new SqlMarkingContext();
        context.setUserId("test_user_004");
        context.setCustomInfo(customInfo);
        SqlMarkingContext.setCurrentContext(context);

        try {
            long beforeCount = sqlMarkingInterceptor.getExecutionCount();

            // 先插入一个测试用户用于删除
            User testUser = new User("待删除用户", "todelete@test.com", 99);
            boolean insertResult = userService.save(testUser);
            result.put("insertResult", insertResult);

            long afterInsertCount = sqlMarkingInterceptor.getExecutionCount();
            boolean insertIntercepted = afterInsertCount > beforeCount;
            result.put("insertIntercepted", insertIntercepted);

            // 执行DELETE操作 - 使用MyBatis Plus的removeById方法
            boolean deleteResult = userService.removeById(testUser.getId());
            result.put("deleteResult", deleteResult);

            // 验证DELETE操作触发SQL标记
            long afterDeleteCount = sqlMarkingInterceptor.getExecutionCount();
            boolean deleteIntercepted = afterDeleteCount > afterInsertCount;
            result.put("deleteIntercepted", deleteIntercepted);

            // 执行条件删除
            QueryWrapper<User> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("status", "INACTIVE");
            int batchDeleteResult = userService.remove(deleteWrapper) ? 1 : 0;
            result.put("batchDeleteResult", batchDeleteResult);

            // 验证条件删除也被标记
            long afterBatchDeleteCount = sqlMarkingInterceptor.getExecutionCount();
            boolean batchDeleteIntercepted = afterBatchDeleteCount > afterDeleteCount;
            result.put("batchDeleteIntercepted", batchDeleteIntercepted);

            // 执行自定义DELETE方法
            int customDeleteResult = userService.deleteByStatus("CUSTOM_UPDATED");
            result.put("customDeleteResult", customDeleteResult);

            // 验证自定义删除也被标记
            long finalCount = sqlMarkingInterceptor.getExecutionCount();
            boolean customDeleteIntercepted = finalCount > afterBatchDeleteCount;
            result.put("customDeleteIntercepted", customDeleteIntercepted);
            result.put("totalExecutionIncrease", finalCount - beforeCount);

            result.put("success", true);
            result.put("message", "DELETE操作SQL标记测试完成");

            log.info("DELETE操作SQL标记测试结果: {}", result);

        } catch (Exception e) {
            log.error("DELETE操作SQL标记测试异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        } finally {
            SqlMarkingContext.clearCurrentContext();
        }

        log.info("=== DELETE操作SQL标记测试结束 ===");
        return result;
    }

    /**
     * 测试SQL标记配置功能
     */
    @GetMapping("/config")
    public Map<String, Object> testSqlMarkingConfiguration() {
        log.info("=== 开始测试SQL标记配置功能 ===");

        Map<String, Object> result = new HashMap<>();

        try {
            SqlMarkingConfig config = sqlMarkingInterceptor.getConfig();

            // 验证配置的有效性
            result.put("configValid", config.isValid());

            // 测试配置摘要
            String summary = config.getConfigSummary();
            result.put("configSummary", summary);

            // 获取当前配置状态
            result.put("enabled", config.isEnabled());
            result.put("markSelect", config.isMarkSelect());
            result.put("markInsert", config.isMarkInsert());
            result.put("markUpdate", config.isMarkUpdate());
            result.put("markDelete", config.isMarkDelete());
            result.put("debugEnabled", config.isDebugEnabled());

            // 测试禁用特定操作的标记
            boolean originalSelectSetting = config.isMarkSelect();
            config.setMarkSelect(false);

            long beforeCount = sqlMarkingInterceptor.getExecutionCount();

            // 执行SELECT操作，应该不会触发标记（取决于实现）
            List<User> users = userService.list();
            result.put("usersCountWhenSelectDisabled", users.size());

            long afterCount = sqlMarkingInterceptor.getExecutionCount();
            result.put("executionCountChangeWhenSelectDisabled", afterCount - beforeCount);

            // 恢复配置
            config.setMarkSelect(originalSelectSetting);

            // 再次执行SELECT操作，应该触发标记
            users = userService.list();
            result.put("usersCountAfterRestore", users.size());

            long finalCount = sqlMarkingInterceptor.getExecutionCount();
            boolean restoredSelectWorking = finalCount > afterCount;
            result.put("restoredSelectWorking", restoredSelectWorking);

            result.put("success", true);
            result.put("message", "SQL标记配置测试完成");

            log.info("SQL标记配置测试结果: {}", result);

        } catch (Exception e) {
            log.error("SQL标记配置测试异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        log.info("=== SQL标记配置测试结束 ===");
        return result;
    }

    /**
     * 综合测试：验证所有CRUD操作的SQL标记
     */
    @GetMapping("/comprehensive")
    public Map<String, Object> testComprehensiveSqlMarking() {
        log.info("=== 开始综合测试：验证所有CRUD操作的SQL标记 ===");

        Map<String, Object> result = new HashMap<>();

        // 设置综合测试的自定义标记信息
        ConcurrentHashMap<String, Object> customInfo = new ConcurrentHashMap<>();
        customInfo.put("operation", "comprehensive_test");
        customInfo.put("testCase", "testComprehensiveSqlMarking");
        customInfo.put("batchId", "BATCH_" + System.currentTimeMillis());

        SqlMarkingContext context = new SqlMarkingContext();
        context.setUserId("test_user_comprehensive");
        context.setCustomInfo(customInfo);
        SqlMarkingContext.setCurrentContext(context);

        try {
            long initialCount = sqlMarkingInterceptor.getExecutionCount();

            // 1. INSERT操作
            User newUser = new User("综合测试用户", "comprehensive@test.com", 28);
            boolean insertResult = userService.save(newUser);
            result.put("insertResult", insertResult);
            result.put("newUserId", newUser.getId());

            long afterInsert = sqlMarkingInterceptor.getExecutionCount();
            boolean insertIntercepted = afterInsert > initialCount;
            result.put("insertIntercepted", insertIntercepted);

            // 2. SELECT操作
            User foundUser = userService.getById(newUser.getId());
            result.put("foundUser", foundUser != null);
            if (foundUser != null) {
                result.put("foundUserName", foundUser.getName());
            }

            long afterSelect = sqlMarkingInterceptor.getExecutionCount();
            boolean selectIntercepted = afterSelect > afterInsert;
            result.put("selectIntercepted", selectIntercepted);

            // 3. UPDATE操作
            if (foundUser != null) {
                foundUser.setAge(29);
                foundUser.setStatus("COMPREHENSIVE_TESTED");
                boolean updateResult = userService.updateById(foundUser);
                result.put("updateResult", updateResult);
            }

            long afterUpdate = sqlMarkingInterceptor.getExecutionCount();
            boolean updateIntercepted = afterUpdate > afterSelect;
            result.put("updateIntercepted", updateIntercepted);

            // 4. 验证更新结果
            User updatedUser = userService.getById(newUser.getId());
            if (updatedUser != null) {
                result.put("updatedAge", updatedUser.getAge());
                result.put("updatedStatus", updatedUser.getStatus());
            }

            long afterVerifySelect = sqlMarkingInterceptor.getExecutionCount();
            boolean verifySelectIntercepted = afterVerifySelect > afterUpdate;
            result.put("verifySelectIntercepted", verifySelectIntercepted);

            // 5. DELETE操作
            boolean deleteResult = userService.removeById(newUser.getId());
            result.put("deleteResult", deleteResult);

            long afterDelete = sqlMarkingInterceptor.getExecutionCount();
            boolean deleteIntercepted = afterDelete > afterVerifySelect;
            result.put("deleteIntercepted", deleteIntercepted);

            // 6. 验证删除结果
            User deletedUser = userService.getById(newUser.getId());
            result.put("userDeleted", deletedUser == null);

            long finalCount = sqlMarkingInterceptor.getExecutionCount();
            boolean finalSelectIntercepted = finalCount > afterDelete;
            result.put("finalSelectIntercepted", finalSelectIntercepted);

            // 验证总的执行次数符合预期（至少6次操作）
            long totalOperations = finalCount - initialCount;
            result.put("totalOperations", totalOperations);
            result.put("expectedMinOperations", 6);
            result.put("operationsCountValid", totalOperations >= 6);

            result.put("success", true);
            result.put("message", "综合SQL标记测试完成，共执行" + totalOperations + "次SQL操作");

            log.info("综合SQL标记测试结果: {}", result);

        } catch (Exception e) {
            log.error("综合SQL标记测试异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        } finally {
            SqlMarkingContext.clearCurrentContext();
        }

        log.info("=== 综合SQL标记测试结束 ===");
        return result;
    }

    /**
     * 测试SQL标记拦截器的异常处理
     */
    @GetMapping("/exception-handling")
    public Map<String, Object> testSqlMarkingExceptionHandling() {
        log.info("=== 开始测试SQL标记拦截器的异常处理 ===");

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 测试空的自定义信息
            SqlMarkingContext context = new SqlMarkingContext();
            context.setUserId(null);
            context.setCustomInfo(null);
            SqlMarkingContext.setCurrentContext(context);

            // 即使上下文信息不完整，SQL操作也应该正常执行
            List<User> users1 = userService.list();
            result.put("emptyContextTest", users1 != null);
            result.put("emptyContextUserCount", users1.size());

            SqlMarkingContext.clearCurrentContext();

            // 2. 测试没有上下文的情况
            List<User> users2 = userService.list();
            result.put("noContextTest", users2 != null);
            result.put("noContextUserCount", users2.size());

            // 3. 测试配置被禁用的情况
            SqlMarkingConfig config = sqlMarkingInterceptor.getConfig();
            boolean originalEnabled = config.isEnabled();

            config.setEnabled(false);
            List<User> users3 = userService.list();
            result.put("disabledConfigTest", users3 != null);
            result.put("disabledConfigUserCount", users3.size());

            // 恢复配置
            config.setEnabled(originalEnabled);

            result.put("success", true);
            result.put("message", "SQL标记异常处理测试完成");

            log.info("SQL标记异常处理测试结果: {}", result);

        } catch (Exception e) {
            log.error("SQL标记异常处理测试异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        log.info("=== SQL标记异常处理测试结束 ===");
        return result;
    }

    /**
     * 获取当前SQL标记拦截器状态
     */
    @GetMapping("/status")
    public Map<String, Object> getSqlMarkingStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            SqlMarkingConfig config = sqlMarkingInterceptor.getConfig();

            status.put("interceptorClass", sqlMarkingInterceptor.getClass().getSimpleName());
            status.put("executionCount", sqlMarkingInterceptor.getExecutionCount());
            status.put("configSummary", config.getConfigSummary());
            status.put("enabled", config.isEnabled());
            status.put("debugEnabled", config.isDebugEnabled());

            // 当前上下文信息
            SqlMarkingContext currentContext = SqlMarkingContext.getCurrentContext();
            if (currentContext != null) {
                status.put("currentContext", "存在");
                status.put("currentUserId", currentContext.getUserId());
                status.put("currentThreadId", currentContext.getThreadId());
            } else {
                status.put("currentContext", "无");
            }

            status.put("success", true);

        } catch (Exception e) {
            status.put("success", false);
            status.put("error", e.getMessage());
        }

        return status;
    }

    /**
     * 重置SQL标记拦截器执行计数
     */
    @GetMapping("/reset-count")
    public Map<String, Object> resetExecutionCount() {
        Map<String, Object> result = new HashMap<>();

        try {
            long beforeReset = sqlMarkingInterceptor.getExecutionCount();
            sqlMarkingInterceptor.resetExecutionCount();
            long afterReset = sqlMarkingInterceptor.getExecutionCount();

            result.put("beforeReset", beforeReset);
            result.put("afterReset", afterReset);
            result.put("success", true);
            result.put("message", "执行计数已重置");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}