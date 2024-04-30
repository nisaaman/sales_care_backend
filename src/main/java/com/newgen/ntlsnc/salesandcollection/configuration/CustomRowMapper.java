package com.newgen.ntlsnc.salesandcollection.configuration;

import com.newgen.ntlsnc.salesandcollection.dto.SalesBudgetExcelDistributorWiseDto;
import org.springframework.batch.extensions.excel.RowMapper;
import org.springframework.batch.extensions.excel.support.rowset.RowSet;

public class CustomRowMapper implements RowMapper<SalesBudgetExcelDistributorWiseDto> {

    public SalesBudgetExcelDistributorWiseDto mapRow(RowSet column) {
        SalesBudgetExcelDistributorWiseDto salesBudgetExcelDistributorWiseDto = new SalesBudgetExcelDistributorWiseDto();

        //salesBudgetExcelDistributorWiseDto.setProductId(column.get);
        return salesBudgetExcelDistributorWiseDto;
    }
}
