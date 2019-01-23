package com.jmeter.plugin.util;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.registry.RegistryService;
import com.alibaba.dubbo.rpc.service.GenericService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: liuzhanhui
 * @Decription:
 * @Date: Created in 2019-01-02:10:56
 * Modify date: 2019-01-02:10:56
 */
public class ZkServiceUtil {


    public static Map<String, String[]> getInterfaceMethods(String address){
        Map<String, String[]> serviceMap = new HashMap<>();
        try {
            ApplicationConfig applicationConfig = new ApplicationConfig();
            applicationConfig.setName("dubboSample");

            RegistryConfig registry = new RegistryConfig();
            registry.setAddress(address);
            registry.setProtocol("zookeeper");
            registry.setClient("curator");
            registry.setGroup(null);

            ReferenceConfig referenceConfig = new ReferenceConfig();
            referenceConfig.setApplication(applicationConfig);
            referenceConfig.setRegistry(registry);
            referenceConfig.setInterface("com.alibaba.dubbo.registry.RegistryService");
            ReferenceConfigCache cache = ReferenceConfigCache.getCache();
            RegistryService registryService = (RegistryService) cache.get(referenceConfig);

            RegistryServerSync registryServerSync = RegistryServerSync.get(address + "_");
            registryService.subscribe(RegistryServerSync.SUBSCRIBE, registryServerSync);
            ConcurrentMap<String, ConcurrentMap<String, Map<String, URL>>> map = registryServerSync.getRegistryCache();
            ConcurrentMap<String, Map<String, URL>> providers = map.get(Constants.PROVIDERS_CATEGORY);
            if (providers == null || providers.isEmpty()) {
                throw new RuntimeException("zookeeper连接失败");
            } else {
                for (String url : providers.keySet()) {
                    Map<String, URL> provider = providers.get(url);
                    for (String str : provider.keySet()) {
                        String methodName = provider.get(str).getParameter(Constants.METHODS_KEY);
                        String[] methods = methodName.split(",");
                        serviceMap.put(url, methods);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("zk连接失败");
            return null;
        }
        return serviceMap;
    }

    public static GenericService getGenericService(String address, String interfaceName) {
        GenericService genericService = null;
        try {
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress(address);
        registry.setClient("curator");
        registry.setProtocol("zookeeper");
        registry.setCheck(true);
        registry.setGroup(null);
        registry.setTimeout(10000);
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        reference.setApplication(new ApplicationConfig("dubboSample"));
        reference.setInterface(interfaceName);
        reference.setProtocol("dubbo");
        reference.setTimeout(5000);
        reference.setVersion("3.0.0");
        reference.setGroup(null);
        reference.setRegistry(registry);
        reference.setGeneric(true);
        genericService = reference.get();
        }catch (Exception e){
            System.out.println("zk连接异常，请检查zk地址！");
            return null;
        }
        return genericService;
    }

    public static void main(String[] args) {
        GenericService genericService = getGenericService("172.18.4.48:2181", "com.noriental.adminsvr.service.teaching.ChapterService");
        Map<String,Object> map = new HashMap();
        map.put("entity","200,201,202");
        Object result = genericService.$invoke("findByIds", new String[]{"com.noriental.adminsvr.request.RequestEntity"}, new Object[]{map});
        System.out.println(result);
    }
}
