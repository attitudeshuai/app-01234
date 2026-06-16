package com.building.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.building.common.BusinessException;
import com.building.dto.AssetDTO;
import com.building.entity.Building;
import com.building.entity.Room;
import com.building.entity.RoomAsset;
import com.building.mapper.RoomAssetMapper;
import com.building.service.BuildingService;
import com.building.service.RoomAssetService;
import com.building.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class RoomAssetServiceImpl extends ServiceImpl<RoomAssetMapper, RoomAsset> implements RoomAssetService {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BuildingService buildingService;

    @Override
    public Page<RoomAsset> pageList(int current, int size, Long roomId, String assetType) {
        LambdaQueryWrapper<RoomAsset> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(RoomAsset::getRoomId, roomId);
        }
        if (StringUtils.hasText(assetType)) {
            wrapper.eq(RoomAsset::getAssetType, assetType);
        }
        wrapper.orderByDesc(RoomAsset::getCreatedAt);

        Page<RoomAsset> page = page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(a -> {
            Room room = roomService.getById(a.getRoomId());
            if (room != null) {
                a.setRoomNumber(room.getRoomNumber());
                Building building = buildingService.getById(room.getBuildingId());
                a.setBuildingName(building != null ? building.getName() : "");
            }
        });
        return page;
    }

    @Override
    public void createAsset(AssetDTO dto) {
        // 业务校验：检查房屋是否存在
        Room room = roomService.getById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("房屋不存在");
        }

        // 业务校验：检查同一房间内是否已有同名资产
        long count = lambdaQuery()
                .eq(RoomAsset::getRoomId, dto.getRoomId())
                .eq(RoomAsset::getAssetName, dto.getAssetName())
                .count();
        if (count > 0) {
            throw new BusinessException("该房间内已存在同名资产【" + dto.getAssetName() + "】");
        }

        RoomAsset asset = new RoomAsset();
        BeanUtils.copyProperties(dto, asset);
        save(asset);
        log.info("新增资产: {} (房间ID: {})", asset.getAssetName(), asset.getRoomId());
    }

    @Override
    public void updateAsset(AssetDTO dto) {
        RoomAsset asset = getById(dto.getId());
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }

        // 业务校验：检查同一房间内是否已有同名资产（排除自身）
        long count = lambdaQuery()
                .eq(RoomAsset::getRoomId, dto.getRoomId())
                .eq(RoomAsset::getAssetName, dto.getAssetName())
                .ne(RoomAsset::getId, dto.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("该房间内已存在同名资产【" + dto.getAssetName() + "】");
        }

        BeanUtils.copyProperties(dto, asset);
        updateById(asset);
        log.info("修改资产: {}", asset.getAssetName());
    }

    @Override
    public void deleteAsset(Long id) {
        RoomAsset asset = getById(id);
        if (asset == null) {
            throw new BusinessException("资产不存在");
        }
        removeById(id);
        log.info("删除资产: {}", asset.getAssetName());
    }
}
