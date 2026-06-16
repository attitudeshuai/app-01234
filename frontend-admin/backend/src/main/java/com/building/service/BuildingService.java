package com.building.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.building.dto.BuildingDTO;
import com.building.entity.Building;

import java.util.List;

/**
 * 楼宇管理服务接口
 * <p>
 * 提供楼宇的增删改查业务逻辑，包括数据权限控制。
 * 非管理员用户只能查看和操作本单位的楼宇数据。
 * </p>
 *
 * @author system
 * @since 1.0
 */
public interface BuildingService extends IService<Building> {

    /**
     * 分页查询楼宇列表
     *
     * @param current      当前页码
     * @param size         每页条数
     * @param name         楼宇名称（模糊查询）
     * @param departmentId 所属单位ID
     * @param status       状态
     * @return 分页结果，包含单位名称和房屋数量
     */
    Page<Building> pageList(int current, int size, String name, Long departmentId, Integer status);

    /**
     * 查询所有正常状态的楼宇（不分页）
     *
     * @return 楼宇列表
     */
    List<Building> listAll();

    /**
     * 新增楼宇
     *
     * @param dto 楼宇信息
     * @throws com.building.common.BusinessException 楼宇编号已存在时抛出异常
     */
    void createBuilding(BuildingDTO dto);

    /**
     * 修改楼宇信息
     *
     * @param dto 楼宇信息（包含ID）
     * @throws com.building.common.BusinessException 楼宇不存在时抛出异常
     */
    void updateBuilding(BuildingDTO dto);

    /**
     * 变更楼宇状态
     *
     * @param id     楼宇ID
     * @param status 目标状态：1-正常，0-封存
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除楼宇
     *
     * @param id 楼宇ID
     * @throws com.building.common.BusinessException 楼宇下存在房屋时无法删除
     */
    void deleteBuilding(Long id);
}
