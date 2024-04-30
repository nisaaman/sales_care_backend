package com.newgen.ntlsnc.supplychainmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 26th Sept, 22
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepotDetailsDto {

    private String depotName;
    private String depotAddress;
    private Map depotStock;
}
