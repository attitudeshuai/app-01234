package com.building.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.building.entity.SysDepartment;
import com.building.mapper.SysDepartmentMapper;
import com.building.service.SysDepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartment> implements SysDepartmentService {

    @Override
    public List<SysDepartment> listAll() {
        return lambdaQuery().eq(SysDepartment::getStatus, 1).orderByAsc(SysDepartment::getSort).list();
    }

    @Override
    public String getNameById(Long id) {
        if (id == null) return "";
        SysDepartment dept = getById(id);
        return dept != null ? dept.getName() : "";
    }
}
