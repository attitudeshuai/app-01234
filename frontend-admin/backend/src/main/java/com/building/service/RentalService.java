package com.building.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.building.dto.ContractDTO;
import com.building.dto.RentalDTO;
import com.building.entity.Rental;
import com.building.entity.RentalContract;

import java.util.List;

public interface RentalService extends IService<Rental> {

    Page<Rental> pageList(int current, int size, String tenantName, String status);

    void createRental(RentalDTO dto);

    void updateRental(RentalDTO dto);

    void terminateRental(Long id);

    List<RentalContract> getContracts(Long rentalId);

    List<RentalContract> getAllContracts();

    void createContract(Long rentalId, ContractDTO dto);
}
