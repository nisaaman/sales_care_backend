package com.newgen.ntlsnc.salesandcollection.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Newaz Sharif
 * @since 31st May, 22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationTreeData {

    private Long id;
    private Long parentId;
    private String locationName;
    private Integer locationTypeLevel;
    private List<LocationTreeData> children;

    public LocationTreeData(Long id, Long parentId, String locationName, Integer locationTypeLevel) {
        this.id = id;
        this.parentId = parentId;
        this.locationName = locationName;
        this.locationTypeLevel = locationTypeLevel;
    }
}
