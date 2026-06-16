package com.building.controller;

import com.building.aspect.OperLog;
import com.building.common.PageResult;
import com.building.common.Result;
import com.building.dto.ContractDTO;
import com.building.dto.RentalDTO;
import com.building.entity.Rental;
import com.building.entity.RentalContract;
import com.building.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    @Autowired
    private RentalService rentalService;

    @GetMapping
    public Result<PageResult<Rental>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tenantName,
            @RequestParam(required = false) String status) {
        return Result.success(PageResult.of(rentalService.pageList(current, size, tenantName, status)));
    }

    @PostMapping
    @OperLog(module = "租赁管理", action = "新增")
    public Result<Void> create(@Valid @RequestBody RentalDTO dto) {
        rentalService.createRental(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @OperLog(module = "租赁管理", action = "修改")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RentalDTO dto) {
        dto.setId(id);
        rentalService.updateRental(dto);
        return Result.success();
    }

    @PutMapping("/{id}/terminate")
    @OperLog(module = "租赁管理", action = "终止")
    public Result<Void> terminate(@PathVariable Long id) {
        rentalService.terminateRental(id);
        return Result.success();
    }

    @GetMapping("/contracts")
    public Result<List<RentalContract>> allContracts() {
        return Result.success(rentalService.getAllContracts());
    }

    @GetMapping("/{id}/contracts")
    public Result<List<RentalContract>> contracts(@PathVariable Long id) {
        return Result.success(rentalService.getContracts(id));
    }

    @PostMapping("/{id}/contracts")
    @OperLog(module = "租赁管理", action = "新增合约")
    public Result<Void> createContract(@PathVariable Long id, @Valid @RequestBody ContractDTO dto) {
        rentalService.createContract(id, dto);
        return Result.success();
    }
}
