package com.building.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.building.entity.*;
import com.building.mapper.*;
import com.building.security.UserContext;
import com.building.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private BuildingMapper buildingMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private RentalMapper rentalMapper;
    @Autowired
    private UsageApprovalMapper approvalMapper;
    @Autowired
    private SysDepartmentMapper departmentMapper;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();

        // 楼宇总数
        result.put("buildingCount", buildingMapper.selectCount(null));
        // 房屋总数
        result.put("roomCount", roomMapper.selectCount(null));
        // 各状态房屋数
        List<Room> rooms = roomMapper.selectList(null);
        Map<String, Long> statusCount = rooms.stream()
                .collect(Collectors.groupingBy(Room::getStatus, Collectors.counting()));
        result.put("roomStatusCount", statusCount);

        // 出租中房屋数
        long rentedCount = rooms.stream().filter(r -> "出租".equals(r.getStatus())).count();
        result.put("rentedCount", rentedCount);

        // 当月租金收入
        BigDecimal monthlyRent = rentalMapper.sumActiveRentAmount();
        result.put("monthlyRentIncome", monthlyRent);

        // 待审批数
        long pendingApproval = approvalMapper.selectCount(
                new LambdaQueryWrapper<UsageApproval>().eq(UsageApproval::getStatus, "待审批"));
        result.put("pendingApprovalCount", pendingApproval);

        // 各用途房屋分布
        Map<String, Long> purposeCount = rooms.stream()
                .filter(r -> r.getPurpose() != null)
                .collect(Collectors.groupingBy(Room::getPurpose, Collectors.counting()));
        result.put("purposeDistribution", purposeCount);

        return result;
    }

    @Override
    public Map<String, Object> getRoomStats(Long departmentId, Long buildingId, String purpose, String status) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();

        UserContext ctx = UserContext.get();
        if (ctx != null && !ctx.isAdmin()) {
            wrapper.eq(Room::getDepartmentId, ctx.getDepartmentId());
        }
        if (departmentId != null) {
            wrapper.eq(Room::getDepartmentId, departmentId);
        }
        if (buildingId != null) {
            wrapper.eq(Room::getBuildingId, buildingId);
        }
        if (purpose != null && !purpose.isEmpty()) {
            wrapper.eq(Room::getPurpose, purpose);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Room::getStatus, status);
        }

        List<Room> rooms = roomMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", rooms.size());
        result.put("totalArea", rooms.stream()
                .map(Room::getArea)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        result.put("statusDistribution", rooms.stream()
                .collect(Collectors.groupingBy(Room::getStatus, Collectors.counting())));
        result.put("purposeDistribution", rooms.stream()
                .filter(r -> r.getPurpose() != null)
                .collect(Collectors.groupingBy(Room::getPurpose, Collectors.counting())));
        result.put("rooms", rooms);

        return result;
    }

    @Override
    public Map<String, Object> getUsageStats(Integer year, Integer month, Long departmentId) {
        LambdaQueryWrapper<UsageApproval> wrapper = new LambdaQueryWrapper<>();

        UserContext ctx = UserContext.get();
        if (ctx != null && !ctx.isAdmin()) {
            wrapper.eq(UsageApproval::getDepartmentId, ctx.getDepartmentId());
        }
        if (departmentId != null) {
            wrapper.eq(UsageApproval::getDepartmentId, departmentId);
        }
        // 支持年度和月度查询：如果只传month则使用当前年份
        if (year != null || month != null) {
            int targetYear = year != null ? year : LocalDate.now().getYear();
            int targetMonth = month != null ? month : 1;
            LocalDate start = LocalDate.of(targetYear, targetMonth, 1);
            LocalDate end = month != null ? start.plusMonths(1) : start.plusYears(1);
            wrapper.ge(UsageApproval::getStartDate, start);
            wrapper.lt(UsageApproval::getStartDate, end);
        }

        List<UsageApproval> approvals = approvalMapper.selectList(wrapper);

        // 房屋总数（用于计算利用率）
        long totalRooms = roomMapper.selectCount(null);

        Map<String, Object> result = new HashMap<>();
        result.put("totalApprovals", approvals.size());
        result.put("approvedCount", approvals.stream().filter(a -> "已通过".equals(a.getStatus())).count());
        result.put("rejectedCount", approvals.stream().filter(a -> "已驳回".equals(a.getStatus())).count());
        result.put("pendingCount", approvals.stream().filter(a -> "待审批".equals(a.getStatus())).count());

        // 按用途类型统计
        result.put("usageTypeDistribution", approvals.stream()
                .collect(Collectors.groupingBy(UsageApproval::getUsageType, Collectors.counting())));

        // 利用率 = 使用中房屋数 / 总房屋数
        long inUseCount = roomMapper.selectCount(
                new LambdaQueryWrapper<Room>().eq(Room::getStatus, "使用中"));
        long rentedCount = roomMapper.selectCount(
                new LambdaQueryWrapper<Room>().eq(Room::getStatus, "出租"));
        double utilizationRate = totalRooms > 0 ? (double) (inUseCount + rentedCount) / totalRooms * 100 : 0;
        result.put("utilizationRate", Math.round(utilizationRate * 100.0) / 100.0);
        result.put("totalRooms", totalRooms);
        result.put("inUseCount", inUseCount);
        result.put("rentedCount", rentedCount);

        return result;
    }

    @Override
    public Map<String, Object> getRentalStats(Integer year, Integer month, Long departmentId) {
        int targetYear = year != null ? year : LocalDate.now().getYear();

        Map<String, Object> result = new HashMap<>();

        // 构建时间范围查询条件
        LambdaQueryWrapper<Rental> timeWrapper = new LambdaQueryWrapper<>();
        if (month != null) {
            // 按月度查询
            LocalDate start = LocalDate.of(targetYear, month, 1);
            LocalDate end = start.plusMonths(1);
            timeWrapper.ge(Rental::getStartDate, start).lt(Rental::getStartDate, end);
        } else {
            // 按年度查询
            LocalDate start = LocalDate.of(targetYear, 1, 1);
            LocalDate end = start.plusYears(1);
            timeWrapper.ge(Rental::getStartDate, start).lt(Rental::getStartDate, end);
        }

        // 月度租金收入趋势（仅年度查询时展示）
        if (month == null) {
            List<Map<String, Object>> monthlyIncome = rentalMapper.monthlyRentalIncome(targetYear);
            result.put("monthlyIncome", monthlyIncome);
        } else {
            // 月度查询时返回该月的租金详情
            result.put("monthlyIncome", new ArrayList<>());
        }

        // 获取时间范围内的租赁记录
        List<Rental> rentalsInPeriod = rentalMapper.selectList(timeWrapper);

        // 时间范围内有效租赁数
        long activeCount = rentalsInPeriod.stream()
                .filter(r -> "有效".equals(r.getStatus()))
                .count();
        result.put("activeRentalCount", activeCount);

        // 时间范围内总租金
        BigDecimal totalAmount = rentalsInPeriod.stream()
                .filter(r -> "有效".equals(r.getStatus()))
                .map(Rental::getRentAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalRentAmount", totalAmount);

        // 按状态统计（时间范围内）
        result.put("statusDistribution", rentalsInPeriod.stream()
                .collect(Collectors.groupingBy(Rental::getStatus, Collectors.counting())));

        // 各单位租赁数量（时间范围内）
        Map<Long, Long> deptRentalCount = rentalsInPeriod.stream()
                .map(r -> {
                    Room room = roomMapper.selectById(r.getRoomId());
                    return room != null ? room.getDepartmentId() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        List<Map<String, Object>> deptStats = new ArrayList<>();
        deptRentalCount.forEach((deptId, count) -> {
            SysDepartment dept = departmentMapper.selectById(deptId);
            Map<String, Object> item = new HashMap<>();
            item.put("departmentId", deptId);
            item.put("departmentName", dept != null ? dept.getName() : "未知");
            item.put("count", count);
            deptStats.add(item);
        });
        result.put("departmentStats", deptStats);

        // 返回查询的时间范围信息
        result.put("queryYear", targetYear);
        result.put("queryMonth", month);

        return result;
    }
}
