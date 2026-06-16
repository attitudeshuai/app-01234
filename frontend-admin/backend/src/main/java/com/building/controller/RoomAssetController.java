package com.building.controller;

import com.building.aspect.OperLog;
import com.building.common.PageResult;
import com.building.common.Result;
import com.building.dto.AssetDTO;
import com.building.entity.RoomAsset;
import com.building.service.RoomAssetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room-assets")
public class RoomAssetController {

    @Autowired
    private RoomAssetService assetService;

    @GetMapping
    public Result<PageResult<RoomAsset>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String assetType) {
        return Result.success(PageResult.of(assetService.pageList(current, size, roomId, assetType)));
    }

    @PostMapping
    @OperLog(module = "资产管理", action = "新增")
    public Result<Void> create(@Valid @RequestBody AssetDTO dto) {
        assetService.createAsset(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @OperLog(module = "资产管理", action = "修改")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AssetDTO dto) {
        dto.setId(id);
        assetService.updateAsset(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "资产管理", action = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return Result.success();
    }
}
