package com.newgen.ntlsnc.security.auth;

import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUserRoleMap;
import com.newgen.ntlsnc.usermanagement.entity.Permission;
import com.newgen.ntlsnc.usermanagement.entity.Role;
import com.newgen.ntlsnc.usermanagement.repository.ApplicationUserRepository;
import com.newgen.ntlsnc.usermanagement.repository.PermissionRepository;
import com.newgen.ntlsnc.usermanagement.service.ApplicationUserRoleMapService;
import com.newgen.ntlsnc.usermanagement.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author nisa
 * @date 6/9/22
 * @time 5:46 PM
 */
@Service
@RequiredArgsConstructor
public class ApplicationUserServiceDAO implements UserDetailsService {
    private final ApplicationUserRepository applicationUserRepository;
    private final ApplicationUserRoleMapService applicationUserRoleMapService;
    private final PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ApplicationUser applicationUser =
                applicationUserRepository.findByEmailAndIsAccountNonExpiredTrueAndIsAccountNonLockedTrueAndIsCredentialsNonExpiredTrueAndIsEnabledTrueAndIsActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        List<Role> roleList = applicationUserRoleMapService.findAllByApplicationUser(applicationUser).stream().filter(Objects::nonNull)
                .map(ApplicationUserRoleMap::getRole)
                .collect(Collectors.toList());

        Set<GrantedAuthority> roles = roleList.stream().filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        Set<Permission> permissions = new HashSet<>();
        roleList.forEach(role -> {
            permissions.addAll(permissionService.findAllByRole(role));
        });

        return UserDetailsImpl.build(applicationUser, roles, permissions);
    }
}
