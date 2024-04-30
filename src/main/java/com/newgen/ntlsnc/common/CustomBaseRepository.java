package com.newgen.ntlsnc.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CustomBaseRepository<T, ID> extends JpaRepository<T, ID> {

}