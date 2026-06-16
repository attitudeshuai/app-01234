package com.building.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.building.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    @Select("SELECT COUNT(*) FROM room WHERE building_id = #{buildingId}")
    int countByBuildingId(Long buildingId);
}
