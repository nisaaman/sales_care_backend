package com.newgen.ntlsnc.salesandcollection.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.AbstractMethodInvokingDelegator;
import org.springframework.batch.item.adapter.DynamicMethodInvocationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MethodInvoker;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Newaz Sharif
 * @since 20th Aug,22
 */
public class RepositoryListItemWriter <T> implements ItemWriter<T>, InitializingBean {

    protected static final Log logger = LogFactory.getLog(RepositoryListItemWriter.class);
    private CrudRepository<?, ?> repository;
    private String methodName;

    public RepositoryListItemWriter() {
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setRepository(CrudRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void write(List<? extends T> items) throws Exception {
        if(!CollectionUtils.isEmpty(items)) {
            this.doWrite(items);
        }

    }

    protected void doWrite(List<? extends T> items) throws Exception {
        if(logger.isDebugEnabled()) {
            logger.debug("Writing to the repository with " + items.size() + " items.");
        }

        MethodInvoker invoker = this.createMethodInvoker(this.repository, this.methodName);
        Iterator i$ = items.iterator();

        while(i$.hasNext()) {
            Object object = i$.next();
            invoker.setArguments(new Object[]{object});
            this.doInvoke(invoker);
        }

    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(this.repository != null, "A CrudRepository implementation is required");
    }

    private Object doInvoke(MethodInvoker invoker) throws Exception {
        try {
            invoker.prepare();
        } catch (ClassNotFoundException var3) {
            throw new DynamicMethodInvocationException(var3);
        } catch (NoSuchMethodException var4) {
            throw new DynamicMethodInvocationException(var4);
        }

        try {
            return invoker.invoke();
        } catch (InvocationTargetException var5) {
            if(var5.getCause() instanceof Exception) {
                throw (Exception)var5.getCause();
            } else {
                throw new AbstractMethodInvokingDelegator.InvocationTargetThrowableWrapper(var5.getCause());
            }
        } catch (IllegalAccessException var6) {
            throw new DynamicMethodInvocationException(var6);
        }
    }

    private MethodInvoker createMethodInvoker(Object targetObject, String targetMethod) {
        MethodInvoker invoker = new MethodInvoker();
        invoker.setTargetObject(targetObject);
        invoker.setTargetMethod(targetMethod);
        return invoker;
    }
}
