package com.building.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.building.dto.AssetDTO;
import com.building.entity.RoomAsset;

public interface RoomAssetService extends IService<RoomAsset> {

    Page<RoomAsset> pageList(int current, int size, Long roomId, String assetType);

    void createAsset(AssetDTO dto);

    void updateAsset(AssetDTO dto);

    void deleteAsset(Long id);
}
