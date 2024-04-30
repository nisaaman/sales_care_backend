package com.newgen.ntlsnc.common;

import java.util.List;

/**
 * @author liton
 * Created on 4/3/22 10:26 AM
 */

public interface IService<T> {

    public T create(Object object);

    public T update(Long id, Object object);

    public boolean delete(Long id);

    public T findById(Long id);

    public List<T> findAll();

    public boolean validate(Object object);
}
