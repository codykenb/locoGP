package de.uka.ipd.sdq.ByCounter.test.helpers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class ProxyLogger implements InvocationHandler {

	private Object obj;

	public ProxyLogger(Object obj) {
		this.obj = obj;
	}

    public static Object newInstance(Object obj) {
		return Proxy.newProxyInstance(
		    obj.getClass().getClassLoader(),
		    obj.getClass().getInterfaces(),
		    new ProxyLogger(obj));
    }

	public Object invoke(Object proxy, Method m, Object[] args)
			throws Throwable {
		Object result;
		try {
		    System.out.println("before method " + m.getName());
		    result = m.invoke(obj, args);
        } catch (InvocationTargetException e) {
		    throw e.getTargetException();
        } catch (Exception e) {
		    throw new RuntimeException(
		    		"unexpected invocation exception: " + e.getMessage());
		} finally {
		    System.out.println("after method " + m.getName());
		}
		return result;
	}

}