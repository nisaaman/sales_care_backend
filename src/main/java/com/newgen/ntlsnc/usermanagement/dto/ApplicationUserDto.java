package com.newgen.ntlsnc.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * @author anika
 * @Date ২/৬/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUserDto {

    private Long id;

    @NotNull(message = "Name is required.")
    private String name;

    @NotNull(message = "Email is required.")
    private String email;

    @NotNull(message = "Password is required.")
    private String password;

    @NotNull(message = "Mobile is required.")
    private String mobile;

    @NotNull(message = "Department is required.")
    private Long departmentId;

    @NotNull(message = "Designation is required.")
    private Long designationId;

    private String referenceNo;

    private Boolean isActive;

    private MultipartFile profileImage;

    private String filePath;
    private String fcmId;

    private boolean isAccountNonExpired = true;
    private boolean isAccountNonLocked = true;
    private boolean isCredentialsNonExpired = true;
    private boolean isEnabled = true;
}
