package com.pet.fresh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class BatchInventoryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 核心算法：先进先出(FIFO)与近效期优先出库
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized Map<String, Object> deductStockFIFO(Long goodsId, int quantity) {
        LocalDate today = LocalDate.now();

        // 查询该商品尚未过期且拥有可用库存的所有批次，按到期日期由近到远(ASC)排序
        String selectSql = "SELECT id, batch_no, current_stock, expire_date FROM batch_inventory " +
                           "WHERE goods_id = ? AND current_stock > 0 AND expire_date >= ? " +
                           "ORDER BY expire_date ASC FOR UPDATE";
        List<Map<String, Object>> batches = jdbcTemplate.queryForList(selectSql, goodsId, today);

        int totalAvailable = batches.stream().mapToInt(b -> ((Number) b.get("current_stock")).intValue()).sum();
        if (totalAvailable < quantity) {
            throw new RuntimeException("可用鲜食库存不足！剩余有效批次库存合计: " + totalAvailable + "，请求出库: " + quantity);
        }

        int remaining = quantity;
        List<Map<String, Object>> deductedDetails = new ArrayList<>();

        for (Map<String, Object> batch : batches) {
            Long batchId = ((Number) batch.get("id")).longValue();
            String batchNo = (String) batch.get("batch_no");
            int stock = ((Number) batch.get("current_stock")).intValue();

            int deductThisBatch;
            if (stock >= remaining) {
                deductThisBatch = remaining;
                jdbcTemplate.update("UPDATE batch_inventory SET current_stock = current_stock - ? WHERE id = ?",
                        deductThisBatch, batchId);
                remaining = 0;
            } else {
                deductThisBatch = stock;
                jdbcTemplate.update("UPDATE batch_inventory SET current_stock = 0 WHERE id = ?", batchId);
                remaining -= stock;
            }

            Map<String, Object> record = new HashMap<>();
            record.put("batchId", batchId);
            record.put("batchNo", batchNo);
            record.put("deductedQty", deductThisBatch);
            record.put("expireDate", batch.get("expire_date"));
            deductedDetails.add(record);

            if (remaining == 0) break;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deductedTotal", quantity);
        response.put("batchDetails", deductedDetails);
        return response;
    }

    /**
     * 自动定时巡检扫描：每小时执行一次，自动划分效期状态(正常/临期≤3天/已过期)
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void autoCheckExpiryTask() {
        LocalDate today = LocalDate.now();
        LocalDate warningThreshold = today.plusDays(3);

        // 1. 已到期置为2
        jdbcTemplate.update("UPDATE batch_inventory SET warning_status = 2 WHERE expire_date < ? AND warning_status != 2", today);

        // 2. 临期但未过期置为1
        jdbcTemplate.update("UPDATE batch_inventory SET warning_status = 1 WHERE expire_date >= ? AND expire_date <= ? AND warning_status = 0",
                today, warningThreshold);
    }

    /**
     * 获取库存大屏汇总指标
     */
    public Map<String, Object> getInventoryDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        Integer normalCount = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(current_stock),0) FROM batch_inventory WHERE warning_status = 0", Integer.class);
        Integer warningCount = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(current_stock),0) FROM batch_inventory WHERE warning_status = 1", Integer.class);
        Integer expiredCount = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(current_stock),0) FROM batch_inventory WHERE warning_status = 2", Integer.class);

        stats.put("normalStock", normalCount);
        stats.put("warningStock", warningCount);
        stats.put("expiredStock", expiredCount);

        // 获取各商品批次明细
        String listSql = "SELECT b.id, b.batch_no, b.produce_date, b.expire_date, b.current_stock, b.storage_location, " +
                         "b.warning_status, g.goods_name, g.category " +
                         "FROM batch_inventory b JOIN fresh_goods g ON b.goods_id = g.id " +
                         "ORDER BY b.expire_date ASC";
        stats.put("batchList", jdbcTemplate.queryForList(listSql));
        return stats;
    }
}
