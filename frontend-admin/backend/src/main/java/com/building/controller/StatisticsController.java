package com.building.controller;

import com.building.common.Result;
import com.building.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(statisticsService.getDashboard());
    }

    @GetMapping("/rooms")
    public Result<Map<String, Object>> roomStats(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String status) {
        return Result.success(statisticsService.getRoomStats(departmentId, buildingId, purpose, status));
    }

    @GetMapping("/usage")
    public Result<Map<String, Object>> usageStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long departmentId) {
        return Result.success(statisticsService.getUsageStats(year, month, departmentId));
    }

    @GetMapping("/rental")
    public Result<Map<String, Object>> rentalStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long departmentId) {
        return Result.success(statisticsService.getRentalStats(year, month, departmentId));
    }
}
