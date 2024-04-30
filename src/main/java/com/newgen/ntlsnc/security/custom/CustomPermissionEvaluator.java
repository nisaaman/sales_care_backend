package com.newgen.ntlsnc.security.custom;

import com.newgen.ntlsnc.common.enums.ActivityFeature;
import com.newgen.ntlsnc.security.auth.UserDetailsImpl;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author nisa
 * @date 6/12/22
 * @time 9:56 AM
 */
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(@NotNull Authentication auth, @NotNull Object targetDomainObject, @NotNull Object permissionName) {
        if (!(permissionName instanceof String) || permissionName.equals("")) {
            return false;
        }
        return hasPrivilege(auth, targetDomainObject.toString().toUpperCase(), permissionName.toString().toUpperCase());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
        return false;
    }

    private boolean hasPrivilege(@NotNull Authentication auth, @NotNull String targetType, @NotNull String permissionName) {

        boolean isHasPrivilege = false;

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        switch (permissionName) {
            case "CREATE":
                isHasPrivilege = userDetails.getPermission().stream().anyMatch(permission ->
                        permission.getActivityFeature().equals(ActivityFeature.valueOf((targetType))) &&
                                permission.getIsCreate());
                break;
            case "UPDATE":
                isHasPrivilege = userDetails.getPermission().stream().anyMatch(permission ->
                        permission.getActivityFeature().equals(ActivityFeature.valueOf((targetType))) &&
                                permission.getIsUpdate());
                break;
            case "DELETE":
                isHasPrivilege = userDetails.getPermission().stream().anyMatch(permission ->
                        permission.getActivityFeature().equals(ActivityFeature.valueOf((targetType))) &&
                                permission.getIsDelete());
                break;
            case "VIEW":
                isHasPrivilege = userDetails.getPermission().stream().anyMatch(permission ->
                        permission.getActivityFeature().equals(ActivityFeature.valueOf((targetType))) &&
                                permission.getIsView());
                break;
            case "LISTVIEW":
                isHasPrivilege = userDetails.getPermission().stream().anyMatch(permission ->
                        permission.getActivityFeature().equals(ActivityFeature.valueOf((targetType))) &&
                                permission.getIsAllView());
                break;
            default:
                break;
        }
        return isHasPrivilege;
    }
}
