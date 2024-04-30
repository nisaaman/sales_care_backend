package com.newgen.ntlsnc.common;

import com.newgen.ntlsnc.security.auth.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author nisa
 * @date 6/9/22
 * @time 5:14 PM
 */
@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        if ((SecurityContextHolder.getContext().getAuthentication() != null) && (!SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser"))) {
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Optional.of(userDetailsImpl.getId());
        }
        return Optional.of(0L);
    }
}
