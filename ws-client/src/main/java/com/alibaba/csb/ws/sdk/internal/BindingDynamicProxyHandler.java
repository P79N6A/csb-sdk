package com.alibaba.csb.ws.sdk.internal;

import com.alibaba.csb.ws.sdk.WSParams;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dynamic Proxy Invocation Handler, set security related info to WS Client Proxy or Dispatch
 * @author liaotian.wq 2017年1月12日
 *
 */
public class BindingDynamicProxyHandler implements InvocationHandler {
	private BindingInterceptor interceptor = null;
	private Object proxy1;
	private AtomicBoolean isBound = new AtomicBoolean(false);

	public BindingDynamicProxyHandler() {
		interceptor = new BindingInterceptor();
	}

	public <T> T bind(T business) {
		if (isBound.get())
			return business;

		this.proxy1 = business;
		isBound.set(true);
		Class<?>[] ics = null;
		if (business.getClass().getInterfaces().length == 0 && business instanceof javax.xml.ws.Dispatch) {
			ics = new Class<?>[] { javax.xml.ws.Dispatch.class };
		} else
			ics = business.getClass().getInterfaces();

		return (T) Proxy.newProxyInstance(business.getClass().getClassLoader(), ics, this);
	}
	
	public void setMock(boolean isMock) {
		interceptor.setMock(isMock);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		if(!"getRequestContext".equals(method.getName())) {
			interceptor.before(proxy1);
		}
		try {
			result = method.invoke(proxy1, args);
		} finally {
			if(!"getRequestContext".equals(method.getName())) {
				interceptor.after(proxy1);
			}
		}
		return result;
	}

	public void setParams(WSParams params) {
		interceptor.setWSParams(params);
	}
}
