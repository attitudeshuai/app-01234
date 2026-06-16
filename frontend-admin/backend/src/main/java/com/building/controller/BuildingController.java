package com.building.controller;

import com.building.aspect.OperLog;
import com.building.common.PageResult;
import com.building.common.Result;
import com.building.dto.BuildingDTO;
import com.building.entity.Building;
import com.building.service.BuildingService;
import com.building.service.SysDepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 楼宇管理控制器
 * <p>
 * 提供楼宇的增删改查接口，包括分页查询、状态变更等功能。
 * 所有写操作都会记录操作日志。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@RestController
@RequestMapping("/buildings")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private SysDepartmentService departmentService;

    /**
     * 分页查询楼宇列表
     *
     * @param current      当前页码，默认1
     * @param size         每页条数，默认10
     * @param name         楼宇名称（模糊查询）
     * @param departmentId 所属单位ID
     * @param status       状态：1-正常，0-封存
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<Building>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer status) {
        return Result.success(PageResult.of(buildingService.pageList(current, size, name, departmentId, status)));
    }

    /**
     * 查询所有楼宇（不分页，用于下拉选择）
     *
     * @return 楼宇列表
     */
    @GetMapping("/all")
    public Result<List<Building>> listAll() {
        return Result.success(buildingService.listAll());
    }

    /**
     * 查询所有单位（用于下拉选择）
     *
     * @return 单位列表
     */
    @GetMapping("/departments")
    public Result<?> departments() {
        return Result.success(departmentService.listAll());
    }

    /**
     * 新增楼宇
     *
     * @param dto 楼宇信息
     * @return 操作结果
     */
    @PostMapping
    @OperLog(module = "楼宇管理", action = "新增")
    public Result<Void> create(@Valid @RequestBody BuildingDTO dto) {
        buildingService.createBuilding(dto);
        return Result.success();
    }

    /**
     * 修改楼宇信息
     *
     * @param id  楼宇ID
     * @param dto 楼宇信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @OperLog(module = "楼宇管理", action = "修改")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BuildingDTO dto) {
        dto.setId(id);
        buildingService.updateBuilding(dto);
        return Result.success();
    }

    /**
     * 变更楼宇状态（正常/封存）
     *
     * @param id   楼宇ID
     * @param body 包含status字段的请求体
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @OperLog(module = "楼宇管理", action = "变更状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        buildingService.updateStatus(id, body.get("status"));
        return Result.success();
    }

    /**
     * 删除楼宇
     * <p>注意：如果楼宇下存在房屋，则无法删除</p>
     *
     * @param id 楼宇ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @OperLog(module = "楼宇管理", action = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return Result.success();
    }
}
