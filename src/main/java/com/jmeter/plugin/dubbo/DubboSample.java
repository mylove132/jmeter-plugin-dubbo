/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jmeter.plugin.dubbo;

import com.jmeter.plugin.util.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache.KeyGenerator;
import com.alibaba.dubbo.rpc.service.GenericService;

import javax.swing.*;

/**
 * DubboSample
 */
public class DubboSample extends AbstractSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();


    /**
     *
     */
    private static final long serialVersionUID = -6794913295411458705L;

    public static String FIELD_DUBBO_REGISTRY_PROTOCOL = "FIELD_DUBBO_REGISTRY_PROTOCOL";
    public static String FIELD_DUBBO_RPC_PROTOCOL = "FIELD_DUBBO_RPC_PROTOCOL";
    public static String FIELD_DUBBO_ADDRESS = "FIELD_DUBBO_ADDRESS";
    public static String FIELD_DUBBO_TIMEOUT = "FIELD_DUBBO_TIMEOUT";
    public static String FIELD_DUBBO_INTERFACE = "FIELD_DUBBO_INTERFACE";
    public static String FIELD_DUBBO_METHOD = "FIELD_DUBBO_METHOD";
    public static String FIELD_DUBBO_METHOD_ARGS = "FIELD_DUBBO_METHOD_ARGS";
    public static String FIELD_DUBBO_METHOD_ARGS_SIZE = "FIELD_DUBBO_METHOD_ARGS_SIZE";
    public static String DEFAULT_TIMEOUT = "1000";
    public static String DEFAULT_VERSION = "1.0";
    public static String DEFAULT_RETRIES = "0";
    public static String DEFAULT_CLUSTER = "failfast";
    public static String DEFAULT_CONNECTIONS = "100";
    public static ApplicationConfig application = new ApplicationConfig("DubboSample");

    /**
     * get Registry Protocol
     * @return the protocol
     */
    public String getRegistryProtocol() {
        return this.getPropertyAsString(FIELD_DUBBO_REGISTRY_PROTOCOL);
    }

    /**
     * set Registry Protocol
     * @param registryProtocol the protocol to set
     */
    public void setRegistryProtocol(String registryProtocol) {
        this.setProperty(new StringProperty(FIELD_DUBBO_REGISTRY_PROTOCOL, org.springframework.util.StringUtils.trimAllWhitespace(registryProtocol)));
    }

    /**
     * get address
     * @return the address
     */
    public String getAddress() {
        return this.getPropertyAsString(FIELD_DUBBO_ADDRESS);
    }

    /**
     * set address
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.setProperty(new StringProperty(FIELD_DUBBO_ADDRESS, org.springframework.util.StringUtils.trimAllWhitespace(address)));
    }

    /**
     * get timeout
     * @return the timeout
     */
    public String getTimeout() {
        return this.getPropertyAsString(FIELD_DUBBO_TIMEOUT, DEFAULT_TIMEOUT);
    }

    /**
     * set timeout
     * @param timeout the timeout to set
     */
    public void setTimeout(String timeout) {
        this.setProperty(new StringProperty(FIELD_DUBBO_TIMEOUT, org.springframework.util.StringUtils.trimAllWhitespace(timeout)));
    }

    /**
     * get RPC protocol
     * @return the RPC protocol
     */
    public String getRpcProtocol() {
        return this.getPropertyAsString(FIELD_DUBBO_RPC_PROTOCOL);
    }

    /**
     * set RPC protocol
     * @param rpcProtocol the protocol to set
     */
    public void setRpcProtocol(String rpcProtocol) {
        this.setProperty(new StringProperty(FIELD_DUBBO_RPC_PROTOCOL, org.springframework.util.StringUtils.trimAllWhitespace(rpcProtocol)));
    }



    /**
     * get interfaceName
     * @return the interfaceName
     */
    public String getInterface() {
        return this.getPropertyAsString(FIELD_DUBBO_INTERFACE);
    }

    /**
     * set interfaceName
     * @param interfaceName the interfaceName to set
     */
    public void setInterfaceName(String interfaceName) {
        this.setProperty(new StringProperty(FIELD_DUBBO_INTERFACE, org.springframework.util.StringUtils.trimAllWhitespace(interfaceName)));
    }

    /**
     * get method
     * @return the method
     */
    public String getMethod() {
        return this.getPropertyAsString(FIELD_DUBBO_METHOD);
    }

    /**
     * set method
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.setProperty(new StringProperty(FIELD_DUBBO_METHOD, org.springframework.util.StringUtils.trimAllWhitespace(method)));
    }

    /**
     * get methodArgs
     * @return the methodArgs
     */
    public List<MethodArgument> getMethodArgs() {
    	int paramsSize = this.getPropertyAsInt(FIELD_DUBBO_METHOD_ARGS_SIZE, 0);
    	List<MethodArgument> list = new ArrayList<MethodArgument>();
		for (int i = 1; i <= paramsSize; i++) {
			String paramType = this.getPropertyAsString(FIELD_DUBBO_METHOD_ARGS + "_PARAM_TYPE" + i);
			String paramValue = this.getPropertyAsString(FIELD_DUBBO_METHOD_ARGS + "_PARAM_VALUE" + i);
			MethodArgument args = new MethodArgument(paramType, paramValue);
			list.add(args);
		}
    	return list;
    }

    /**
     * set methodArgs
     * @param methodArgs the methodArgs to set
     */
    public void setMethodArgs(List<MethodArgument> methodArgs) {
    	int size = methodArgs == null ? 0 : methodArgs.size();
    	this.setProperty(new IntegerProperty(FIELD_DUBBO_METHOD_ARGS_SIZE, size));
    	if (size > 0) {
    		for (int i = 1; i <= methodArgs.size(); i++) {
    			this.setProperty(new StringProperty(FIELD_DUBBO_METHOD_ARGS + "_PARAM_TYPE" + i, methodArgs.get(i-1).getParamType()));
    			this.setProperty(new StringProperty(FIELD_DUBBO_METHOD_ARGS + "_PARAM_VALUE" + i, methodArgs.get(i-1).getParamValue()));
    		}
    	}
    }

    @SuppressWarnings("deprecation")
	@Override
    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.sampleStart();
        //构造请求数据
        res.setSamplerData(getSampleData());
        //调用dubbo
        res.setResponseData(JsonUtils.toJson(callDubbo(res)));
        //构造响应数据
        res.setDataType(SampleResult.TEXT);
        res.setResponseCodeOK();
        res.setResponseMessageOK();
        res.sampleEnd();
        return res;
    }

    /**
     * Construct request data
     */
    private String getSampleData() {
    	StringBuilder sb = new StringBuilder();
        sb.append("Registry Protocol: ").append(getRegistryProtocol()).append("\n");
        sb.append("Address: ").append(getAddress()).append("\n");
        sb.append("Timeout: ").append(getTimeout()).append("\n");
        sb.append("Interface: ").append(getInterface()).append("\n");
        sb.append("Method: ").append(getMethod()).append("\n");
        sb.append("Method Args: ").append(getMethodArgs().toString());
        return sb.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object callDubbo(SampleResult res) {
        // This instance is heavy, encapsulating the connection to the registry and the connection to the provider,
        // so please cache yourself, otherwise memory and connection leaks may occur.
        ReferenceConfig reference = new ReferenceConfig();
        // set application
        reference.setApplication(application);
        RegistryConfig registry = null;
        // check address
        String address = getAddress();
        if (StringUtils.isBlank(address)) {
            res.setSuccessful(false);
            return ErrorCode.MISS_ADDRESS.getMessage();
        }
        String rpcProtocol = getRpcProtocol().replaceAll("://", "");
        String protocol = getRegistryProtocol();
		switch (protocol) {
		case Constants.REGISTRY_ZOOKEEPER:
			registry = new RegistryConfig();
			registry.setProtocol(Constants.REGISTRY_ZOOKEEPER);
			registry.setAddress(address);
			reference.setRegistry(registry);
			reference.setProtocol(rpcProtocol);
			break;
		case Constants.REGISTRY_MULTICAST:
			registry = new RegistryConfig();
			registry.setProtocol(Constants.REGISTRY_MULTICAST);
			registry.setAddress(address);
			reference.setRegistry(registry);
			reference.setProtocol(rpcProtocol);
			break;
		case Constants.REGISTRY_REDIS:
			registry = new RegistryConfig();
			registry.setProtocol(Constants.REGISTRY_REDIS);
			registry.setAddress(address);
			reference.setRegistry(registry);
			reference.setProtocol(rpcProtocol);
			break;
		case Constants.REGISTRY_SIMPLE:
			registry = new RegistryConfig();
			registry.setAddress(address);
			reference.setRegistry(registry);
			reference.setProtocol(rpcProtocol);
			break;
		default:
			// direct invoke provider
			StringBuffer sb = new StringBuffer();
			sb.append(getRpcProtocol()).append(getAddress()).append("/").append(getInterface());
			log.debug("rpc invoker url : " + sb.toString());
			reference.setUrl(sb.toString());
		}
        try {
		    // set interface
		    String interfaceName = getInterface();
		    if (StringUtils.isBlank(interfaceName)) {
                res.setSuccessful(false);
                return ErrorCode.MISS_INTERFACE.getMessage();
            }
            reference.setInterface(interfaceName);

            // set timeout
            Integer timeout = null;
            try {
                if (!StringUtils.isBlank(getTimeout())) {
                    timeout = Integer.valueOf(getTimeout());
                }
            } catch (NumberFormatException e) {
                res.setSuccessful(false);
                return ErrorCode.TIMEOUT_ERROR.getMessage();
            }
            if (timeout != null) {
                reference.setTimeout(timeout);
            }
            reference.setVersion("3.0.0");
            reference.setGroup(null);
            // set generic
            reference.setGeneric(true);
            String methodName = getMethod();
            if (StringUtils.isBlank(methodName)) {
                res.setSuccessful(false);
                return ErrorCode.MISS_METHOD.getMessage();
            }

            ReferenceConfigCache cache = ReferenceConfigCache.getCache(getAddress(), new KeyGenerator() {
				public String generateKey(ReferenceConfig<?> referenceConfig) {
					return referenceConfig.toString();
				}
			});
            GenericService genericService = (GenericService) cache.get(reference);
            if (genericService == null) {
                res.setSuccessful(false);
                return MessageFormat.format(ErrorCode.GENERIC_SERVICE_IS_NULL.getMessage(), interfaceName);
            }
            String[] parameterTypes = null;
            Object[] parameterValues = null;
            List<MethodArgument> args = getMethodArgs();
            List<String> paramterTypeList =  new ArrayList<String>();;
            List<Object> parameterValuesList = new ArrayList<Object>();;
            for(MethodArgument arg : args) {
            	ClassUtils.parseParameter(paramterTypeList, parameterValuesList, arg);
            }
            parameterTypes = paramterTypeList.toArray(new String[paramterTypeList.size()]);
            parameterValues = parameterValuesList.toArray(new Object[parameterValuesList.size()]);
            Object result = null;
			try {
				result = genericService.$invoke(methodName, parameterTypes, parameterValues);
				res.setSuccessful(true);
			} catch (Exception e) {
				log.error("RpcException：", e);
				//TODO
				//当接口返回异常时，sample标识为successful，通过响应内容做断言来判断是否标识sample错误，因为sample的错误会统计到用例的error百分比内。
				//比如接口有一些校验性质的异常，不代表这个操作是错误的，这样就可以灵活的判断，不至于正常的校验返回导致测试用例error百分比的不真实
				res.setSuccessful(true);
				result = e;
			}
            return result;
        } catch (Exception e) {
            log.error("UnknownException：", e);
            res.setSuccessful(false);
            return e;
        } finally {
        	//TODO 不能在sample结束时destroy
//            if (registry != null) {
//                registry.destroyAll();
//            }
//            reference.destroy();
        }
    }
}
