package com.building.controller;

import com.building.aspect.OperLog;
import com.building.common.PageResult;
import com.building.common.Result;
import com.building.dto.RoomDTO;
import com.building.entity.Room;
import com.building.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 房屋管理控制器
 * <p>
 * 提供房屋的增删改查接口，包括分页查询、状态变更等功能。
 * 房屋状态包括：空闲、使用中、出租、封存。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    /**
     * 分页查询房屋列表
     *
     * @param current      当前页码，默认1
     * @param size         每页条数，默认10
     * @param buildingId   所属楼宇ID
     * @param departmentId 所属单位ID
     * @param roomNumber   房间号（模糊查询）
     * @param purpose      用途
     * @param status       状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<Room>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String status) {
        return Result.success(PageResult.of(roomService.pageList(current, size, buildingId, departmentId, roomNumber, purpose, status)));
    }

    /**
     * 根据楼宇ID查询房屋列表（不分页，用于下拉选择）
     *
     * @param buildingId 楼宇ID
     * @return 房屋列表
     */
    @GetMapping("/by-building/{buildingId}")
    public Result<List<Room>> listByBuilding(@PathVariable Long buildingId) {
        return Result.success(roomService.listByBuilding(buildingId));
    }

    /**
     * 新增房屋（房屋登记）
     *
     * @param dto 房屋信息
     * @return 操作结果
     */
    @PostMapping
    @OperLog(module = "房屋管理", action = "新增")
    public Result<Void> create(@Valid @RequestBody RoomDTO dto) {
        roomService.createRoom(dto);
        return Result.success();
    }

    /**
     * 修改房屋信息
     *
     * @param id  房屋ID
     * @param dto 房屋信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @OperLog(module = "房屋管理", action = "修改")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoomDTO dto) {
        dto.setId(id);
        roomService.updateRoom(dto);
        return Result.success();
    }

    /**
     * 变更房屋状态
     *
     * @param id   房屋ID
     * @param body 包含status字段的请求体（空闲/使用中/出租/封存）
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @OperLog(module = "房屋管理", action = "变更状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        roomService.updateStatus(id, body.get("status"));
        return Result.success();
    }

    /**
     * 删除房屋
     *
     * @param id 房屋ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @OperLog(module = "房屋管理", action = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return Result.success();
    }
}
