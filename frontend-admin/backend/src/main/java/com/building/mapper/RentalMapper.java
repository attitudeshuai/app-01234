package com.building.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.building.entity.Rental;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface RentalMapper extends BaseMapper<Rental> {

    @Select("SELECT COALESCE(SUM(rent_amount), 0) FROM rental WHERE status = '有效'")
    BigDecimal sumActiveRentAmount();

    @Select("SELECT DATE_FORMAT(r.start_date, '%Y-%m') as month, SUM(r.rent_amount) as amount " +
            "FROM rental r WHERE r.status != '终止' AND YEAR(r.start_date) = #{year} " +
            "GROUP BY DATE_FORMAT(r.start_date, '%Y-%m') ORDER BY month")
    List<Map<String, Object>> monthlyRentalIncome(int year);
}
