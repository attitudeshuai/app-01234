package com.building.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.building.dto.HealthDTO;
import com.building.entity.RoomHealth;

public interface RoomHealthService extends IService<RoomHealth> {

    Page<RoomHealth> pageList(int current, int size, Long roomId, String checkType, String result);

    void createHealth(HealthDTO dto);

    void updateHealth(HealthDTO dto);
}
