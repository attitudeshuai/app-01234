package com.building.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.building.dto.RoomDTO;
import com.building.entity.Room;

import java.util.List;

public interface RoomService extends IService<Room> {

    Page<Room> pageList(int current, int size, Long buildingId, Long departmentId,
                        String roomNumber, String purpose, String status);

    List<Room> listByBuilding(Long buildingId);

    void createRoom(RoomDTO dto);

    void updateRoom(RoomDTO dto);

    void updateStatus(Long id, String status);

    void deleteRoom(Long id);
}
