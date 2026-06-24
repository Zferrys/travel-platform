package com.travel.controller;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.travel.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        Map<String, Object> m = new HashMap<>();
        Runtime rt = Runtime.getRuntime();
        m.put("totalMemoryMB", rt.totalMemory() / 1024 / 1024);
        m.put("freeMemoryMB", rt.freeMemory() / 1024 / 1024);
        m.put("maxMemoryMB", rt.maxMemory() / 1024 / 1024);
        m.put("processors", rt.availableProcessors());

        if (dataSource instanceof DruidDataSource) {
            DruidDataSource ds = (DruidDataSource) dataSource;
            m.put("activeConnections", ds.getActiveCount());
            m.put("poolingCount", ds.getPoolingCount());
            m.put("maxActive", ds.getMaxActive());
        }
        return ApiResponse.success(m);
    }
}
