package com.newgen.ntlsnc.security.config;

import com.newgen.ntlsnc.security.auth.ApplicationUserServiceDAO;
import com.newgen.ntlsnc.security.jwt.AuthEntryPointJwt;
import com.newgen.ntlsnc.security.jwt.JwtConfig;
import com.newgen.ntlsnc.security.jwt.JwtUsernameAndPasswordAuthenticationFilter;
import com.newgen.ntlsnc.security.jwt.JwtTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.util.Arrays;

/**
 * @author nisa
 * @date 6/9/22
 * @time 6:16 PM
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserServiceDAO applicationUserService;
    private final SecretKey secretKey;
    private final JwtConfig jwtConfig;
    private final AuthEntryPointJwt unauthorizedHandler;

    @Value("${cors.allow.origin}")
    private final String[] corsUrls;

    String[] pathArray = new String[]{"/v3/api-docs/**",
            "/swagger-ui.html", "/swagger-ui/**",
            "/api/login", "/api/organization/view"};

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwtConfig, secretKey))
                .addFilterAfter(new JwtTokenVerifier(secretKey, jwtConfig, applicationUserService), JwtUsernameAndPasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/resources/templates/**").permitAll()
                .antMatchers("/**/*.{js,html,css}").permitAll()
                .antMatchers(pathArray).permitAll()
                .anyRequest()
                .authenticated();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(applicationUserService);
        return provider;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(pathArray);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(corsUrls));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", jwtConfig.getAuthorizationHeader()));
        configuration.addAllowedHeader("Access-Control-Allow-Origin");
        configuration.addAllowedHeader("cache-control");
        configuration.addAllowedHeader(jwtConfig.getAuthorizationHeader());
        configuration.addAllowedHeader("access-control-allow-headers");
        configuration.addAllowedHeader("access-control-allow-methods");
        configuration.addAllowedHeader("credentials");
        configuration.addExposedHeader("Access-Control-Allow-Credentials");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
