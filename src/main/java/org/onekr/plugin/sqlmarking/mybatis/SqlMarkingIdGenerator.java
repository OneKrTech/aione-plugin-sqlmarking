package org.onekr.plugin.sqlmarking.mybatis;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SQL染色标识生成器
 * 用于生成分布式环境下的唯一追踪标识
 * 
 * @author Billy
 */
@Slf4j
public class SqlMarkingIdGenerator {

    /**
     * 机器标识（基于IP地址生成）
     */
    private final String machineId;
    
    /**
     * 进程标识（基于进程ID生成）
     */
    private final String processId;

    /**
     * PFinderId计数器
     */
    private final AtomicLong pFinderIdCounter = new AtomicLong(0);

    /**
     * TraceId计数器
     */
    private final AtomicLong traceIdCounter = new AtomicLong(0);
    
    /**
     * 启动时间戳
     */
    private final long startupTime;

    public SqlMarkingIdGenerator() {
        this.startupTime = System.currentTimeMillis();
        this.machineId = generateMachineId();
        this.processId = generateProcessId();
        
        log.info("SQL染色ID生成器初始化完成 - MachineId: {}, ProcessId: {}", machineId, processId);
    }

    /**
     * 生成PFinderId
     * 格式: {machineId}-{processId}-{timestamp}-{counter}
     */
    public String generatePFinderId() {
        long counter = pFinderIdCounter.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        
        return String.format("%s-%s-%d-%d", 
            machineId, processId, timestamp, counter);
    }

    /**
     * 生成TraceId
     * 格式: {machineId}{processId}{timestamp}{counter}
     */
    public String generateTraceId() {
        long counter = traceIdCounter.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        
        return String.format("%s%s%d%06d", 
            machineId, processId, timestamp, counter % 1000000);
    }

    /**
     * 生成短格式的PFinderId（用于简化显示）
     * 格式: {machineId}-{counter}
     */
    public String generateShortPFinderId() {
        long counter = pFinderIdCounter.incrementAndGet();
        return String.format("%s-%d", machineId, counter);
    }

    /**
     * 生成机器标识
     * 基于本机IP地址的最后两段生成4位十六进制字符串
     */
    private String generateMachineId() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            byte[] address = localHost.getAddress();
            
            // 使用IP地址的最后两个字节生成机器ID
            int machineHash = ((address[address.length - 2] & 0xFF) << 8) | 
                             (address[address.length - 1] & 0xFF);
            
            return String.format("%04X", machineHash & 0xFFFF);
            
        } catch (UnknownHostException e) {
            log.warn("无法获取本机IP地址，使用随机机器ID", e);
            // 使用当前时间戳的低16位作为机器ID
            return String.format("%04X", (int)(System.currentTimeMillis() & 0xFFFF));
        }
    }

    /**
     * 生成进程标识
     * 基于进程ID和启动时间生成3位十六进制字符串
     */
    private String generateProcessId() {
        try {
            // 获取进程ID
            String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            String pid = processName.split("@")[0];
            
            // 结合进程ID和启动时间生成进程标识
            int processHash = (Integer.parseInt(pid) * 31 + (int)(startupTime & 0xFFFF)) & 0xFFF;
            
            return String.format("%03X", processHash);
            
        } catch (Exception e) {
            log.warn("无法获取进程ID，使用随机进程ID", e);
            // 使用启动时间的低12位作为进程ID
            return String.format("%03X", (int)(startupTime & 0xFFF));
        }
    }

    /**
     * 获取生成器统计信息
     */
    public String getStatistics() {
        return String.format("SqlMarkingIdGenerator[machineId=%s, processId=%s, pFinderCount=%d, traceCount=%d]",
            machineId, processId, pFinderIdCounter.get(), traceIdCounter.get());
    }

    /**
     * 重置计数器（主要用于测试）
     */
    public void resetCounters() {
        pFinderIdCounter.set(0);
        traceIdCounter.set(0);
    }

    /**
     * 获取机器标识
     */
    public String getMachineId() {
        return machineId;
    }

    /**
     * 获取进程标识
     */
    public String getProcessId() {
        return processId;
    }
}