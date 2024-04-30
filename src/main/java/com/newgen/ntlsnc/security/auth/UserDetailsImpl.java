package com.newgen.ntlsnc.security.auth;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.Permission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * @author nisa
 * @date 6/9/22
 * @time 5:48 PM
 */
@Data
@EqualsAndHashCode
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String password;

    private String username;
    private final String email;
    private final Organization organization;
    private final boolean isAccountNonExpired;
    private final boolean isAccountNonLocked;
    private final boolean isCredentialsNonExpired;
    private final boolean isEnabled;
    private final Set<? extends GrantedAuthority> grantedAuthorities;
    private final Set<Permission> permissions;

    public UserDetailsImpl(Long id,
                           String email,
                           String password,
                           Organization organization,
                           boolean isAccountNonExpired,
                           boolean isAccountNonLocked,
                           boolean isCredentialsNonExpired,
                           boolean isEnabled,
                           Set<? extends GrantedAuthority> grantedAuthorities,
                           Set<Permission> permissions) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.organization = organization;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
        this.grantedAuthorities = grantedAuthorities;
        this.permissions = permissions;
    }

    public static UserDetailsImpl build(ApplicationUser applicationUser, Set<GrantedAuthority> grantedAuthorities, Set<Permission> permissions) {
        return new UserDetailsImpl(
                applicationUser.getId(),
                applicationUser.getEmail(),
                applicationUser.getPassword(),
                applicationUser.getOrganization(),
                applicationUser.isAccountNonExpired(),
                applicationUser.isAccountNonLocked(),
                applicationUser.isCredentialsNonExpired(),
                applicationUser.isEnabled(),
                grantedAuthorities,
                permissions);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return null;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    public Set<Permission> getPermission() {
        return permissions;
    }
}
