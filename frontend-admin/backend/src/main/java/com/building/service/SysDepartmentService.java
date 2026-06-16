package com.building.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.building.entity.SysDepartment;

import java.util.List;

public interface SysDepartmentService extends IService<SysDepartment> {

    List<SysDepartment> listAll();

    String getNameById(Long id);
}
