package com.newgen.ntlsnc.security.jwt;

import lombok.*;

/**
 * @author nisa
 * @date 6/9/22
 * @time 6:18 PM
 */

@Data
public class UsernameAndPasswordAuthenticationRequest {
    private String email;
    private String password;
}
