package com.newgen.ntlsnc.salesandcollection.dto;

import com.newgen.ntlsnc.salesandcollection.service.LocationTreeData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 14th June,22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesOverViewDto extends  SalesDataDto {

    private List<LocationTreeData> locationTreeData;

}
