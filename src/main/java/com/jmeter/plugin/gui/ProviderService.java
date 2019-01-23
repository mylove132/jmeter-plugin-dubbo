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

package com.jmeter.plugin.gui;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.registry.RegistryService;

import com.jmeter.plugin.dubbo.DubboSample;
import com.jmeter.plugin.util.Constants;
import com.jmeter.plugin.util.RegistryServerSync;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ProviderService
 */
public class ProviderService implements Serializable {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = -750353929981409079L;
    ConcurrentMap<String, Map<String, URL>> providerUrls = null;

    private static ConcurrentMap<String, ProviderService> cache = new ConcurrentHashMap<>();

    public static ProviderService get(String key) {
        ProviderService service = cache.get(key);
        if (service == null) {
            cache.putIfAbsent(key, new ProviderService());
            service = cache.get(key);
        }
        return service;
    }

    public Map<String, URL> findByService(String serviceName) {
        return providerUrls == null ? null : providerUrls.get(serviceName);
    }

    public List<String> getProviders(String protocol, String address) throws RuntimeException {
        if (protocol.equals("zookeeper") || protocol.equals("redis")){
            return executeRegistry(protocol, address, "");
//        } else if (protocol.equals("none")) {
//            return executeTelnet();
        } else {
            throw new RuntimeException("Registry Protocol please use zookeeper or redis!");
        }
    }

    private List<String> executeTelnet() throws RuntimeException {
        throw new RuntimeException();
    }

    private List<String> executeRegistry(String protocol, String address, String group) throws RuntimeException {
        ReferenceConfig reference = new ReferenceConfig();
        // set application
        reference.setApplication(DubboSample.application);
        RegistryConfig registry = null;
        switch (protocol) {
            case Constants.REGISTRY_ZOOKEEPER:
                registry = new RegistryConfig();
                registry.setProtocol(Constants.REGISTRY_ZOOKEEPER);
                registry.setGroup(group);
                registry.setAddress(address);
                reference.setRegistry(registry);
                break;
            case Constants.REGISTRY_REDIS:
                registry = new RegistryConfig();
                registry.setProtocol(Constants.REGISTRY_REDIS);
                registry.setGroup(group);
                registry.setAddress(address);
                reference.setRegistry(registry);
                break;
        }
        reference.setInterface("com.alibaba.dubbo.registry.RegistryService");
        try {
            ReferenceConfigCache cache = ReferenceConfigCache.getCache(address + "_" + group, new ReferenceConfigCache.KeyGenerator() {
                public String generateKey(ReferenceConfig<?> referenceConfig) {
                    return referenceConfig.toString();
                }
            });
            RegistryService registryService = (RegistryService) cache.get(reference);
            if (registryService == null) {
                throw new RuntimeException("Can't get the interface list, please check if the address is wrong!");
            }
            RegistryServerSync registryServerSync = RegistryServerSync.get(address + "_" + group);
            registryService.subscribe(RegistryServerSync.SUBSCRIBE, registryServerSync);
            List<String> ret = new ArrayList<String>();
            providerUrls = registryServerSync.getRegistryCache().get(com.alibaba.dubbo.common.Constants.PROVIDERS_CATEGORY);
            if (providerUrls != null) ret.addAll(providerUrls.keySet());
            return ret;
        } catch (Exception e) {
            log.error("get provider list is error!", e);
            throw new RuntimeException("Can't get the interface list, please check if the address is wrong!", e);
        }
    }
}
