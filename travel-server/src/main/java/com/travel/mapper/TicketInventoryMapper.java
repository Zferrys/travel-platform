package com.travel.mapper;

import com.travel.entity.TicketInventory;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface TicketInventoryMapper {
    TicketInventory selectForUpdate(@Param("scenicId") Integer scenicId, @Param("useDate") Date useDate);
    int decreaseStockWithVersion(@Param("scenicId") Integer scenicId, @Param("useDate") Date useDate,
                                  @Param("version") Integer version);
    int restoreStock(@Param("scenicId") Integer scenicId, @Param("useDate") Date useDate,
                     @Param("version") Integer version);
    int insert(TicketInventory inventory);
}
