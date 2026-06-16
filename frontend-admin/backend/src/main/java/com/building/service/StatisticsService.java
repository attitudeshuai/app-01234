package com.building.service;

import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getDashboard();

    Map<String, Object> getRoomStats(Long departmentId, Long buildingId, String purpose, String status);

    Map<String, Object> getUsageStats(Integer year, Integer month, Long departmentId);

    Map<String, Object> getRentalStats(Integer year, Integer month, Long departmentId);
}
