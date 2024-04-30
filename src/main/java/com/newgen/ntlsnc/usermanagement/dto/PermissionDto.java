package com.newgen.ntlsnc.usermanagement.dto;

import com.newgen.ntlsnc.common.enums.ActivityFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author anika
 * @Date ১৯/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {

    private Long id;
    private Long roleId;
    private ActivityFeature activityFeature;
    private Boolean isCreate;
    private Boolean isUpdate;
    private Boolean isDelete;
    private Boolean isView;
    private Boolean isAllView;
}
