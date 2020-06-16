package com.minis.web.servlet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import com.minis.beans.BeansException;
import com.minis.web.RequestMapping;
import com.minis.web.WebApplicationContext;

public class RequestMappingHandlerMapping implements HandlerMapping {
	WebApplicationContext webApplicationContext;
	private final MappingRegistry mappingRegistry = new MappingRegistry();
	
	public RequestMappingHandlerMapping(WebApplicationContext webApplicationContext) {
		this.webApplicationContext = webApplicationContext;
		
		initMapping();
	}
	
    protected void initMapping() {
    	Class<?> clz = null;
    	Object obj = null;
    	String[] controllerNames = this.webApplicationContext.getBeanDefinitionNames();
    	for (String controllerName : controllerNames) {
			try {
				clz = Class.forName(controllerName);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			try {
				obj = this.webApplicationContext.getBean(controllerName);
			} catch (BeansException e) {
				e.printStackTrace();
			}
    		Method[] methods = clz.getDeclaredMethods();
    		if(methods!=null){
    			for(Method method : methods){
    				boolean isRequestMapping = method.isAnnotationPresent(RequestMapping.class);
    				if (isRequestMapping){
    					String methodName = method.getName();
    					String urlmapping = method.getAnnotation(RequestMapping.class).value();
    					this.mappingRegistry.getUrlMappingNames().add(urlmapping);
    					this.mappingRegistry.getMappingObjs().put(urlmapping, obj);
    					this.mappingRegistry.getMappingMethods().put(urlmapping, method);
    					this.mappingRegistry.getMappingMethodNames().put(urlmapping, methodName);
    					this.mappingRegistry.getMappingClasses().put(urlmapping, clz);
    				}
    			}
    		}
    	}

    }


	@Override
	public HandlerMethod getHandler(HttpServletRequest request) throws Exception {
		String sPath = request.getServletPath();
	
		if (!this.mappingRegistry.getUrlMappingNames().contains(sPath)) {
			return null;
		}

		Method method = this.mappingRegistry.getMappingMethods().get(sPath);
		Object obj = this.mappingRegistry.getMappingObjs().get(sPath);
		Class<?> clz = this.mappingRegistry.getMappingClasses().get(sPath);
		String methodName = this.mappingRegistry.getMappingMethodNames().get(sPath);
	
		HandlerMethod handlerMethod = new HandlerMethod(method, obj, clz, methodName);
		
		return handlerMethod;
	}

}
