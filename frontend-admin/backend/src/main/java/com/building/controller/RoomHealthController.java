package com.building.controller;

import com.building.aspect.OperLog;
import com.building.common.PageResult;
import com.building.common.Result;
import com.building.dto.HealthDTO;
import com.building.entity.RoomHealth;
import com.building.service.RoomHealthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room-health")
public class RoomHealthController {

    @Autowired
    private RoomHealthService healthService;

    @GetMapping
    public Result<PageResult<RoomHealth>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String checkType,
            @RequestParam(required = false) String result) {
        return Result.success(PageResult.of(healthService.pageList(current, size, roomId, checkType, result)));
    }

    @PostMapping
    @OperLog(module = "房屋健康", action = "新增检查")
    public Result<Void> create(@Valid @RequestBody HealthDTO dto) {
        healthService.createHealth(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @OperLog(module = "房屋健康", action = "修改记录")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody HealthDTO dto) {
        dto.setId(id);
        healthService.updateHealth(dto);
        return Result.success();
    }
}
