package com.jmeter.plugin.util;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryService;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.jmeter.plugin.dubbo.DubboPlugin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Author: liuzhanhui
 * @Decription:
 * @Date: Created in 2019-01-09:10:48
 * Modify date: 2019-01-09:10:48
 */
public class DubboUtil {

    public static Map<String, String[]> getServiceMethodMapper(String address, String protocol) {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(DubboPlugin.APPLICATION_NAME);
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress(address);
        registry.setProtocol(protocol);
        registry.setGroup(null);
        ReferenceConfig referenceConfig = new ReferenceConfig();
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setRegistry(registry);
        referenceConfig.setInterface("com.alibaba.dubbo.registry.RegistryService");
        ReferenceConfigCache cache = ReferenceConfigCache.getCache();

        RegistryService registryService = (RegistryService) cache.get(referenceConfig);
        String url = URL.encode(Constants.ADMIN_PROTOCOL+"://" + NetUtils.getLocalHost() + "/" + "com.alibaba.dubbo.registry.RegistryService");
        URL ul = URL.valueOf(url);
        ul = ul.addParameter(Constants.INTERFACE_KEY, Constants.ANY_VALUE).
                addParameter(Constants.METHODS_KEY, Constants.ANY_VALUE)
                .addParameter(Constants.VERSION_KEY, Constants.ANY_VALUE)
                .addParameter(Constants.CHECK_KEY, String.valueOf(true))
                .addParameter(Constants.CLASSIFIER_KEY, Constants.ANY_VALUE)
                .addParameter(Constants.ENABLED_KEY, Constants.ANY_VALUE)
                .addParameter(Constants.CATEGORY_KEY, Constants.PROVIDERS_CATEGORY);
        Map<String, String[]> serviceMethodMapper = new HashMap<>();
        registryService.subscribe(ul, new NotifyListener() {
            @Override
            public void notify(List<URL> urls) {
                if (urls != null) {
                    for (URL ul1 : urls) {
                        String service = ul1.getServiceInterface();
                        String methods = ul1.getParameter(Constants.METHODS_KEY);
                        if (methods.equals("*")){
                            continue;
                        }
                        serviceMethodMapper.put(service, methods.split(","));
                    }
                }
            }
        });
        return serviceMethodMapper;
    }

    public static GenericService getGenericService(String address, String interfaceName) {
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress(address);
        registry.setProtocol("zookeeper");
        registry.setGroup(null);
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        reference.setApplication(new ApplicationConfig(DubboPlugin.APPLICATION_NAME));
        reference.setInterface(interfaceName);
        reference.setProtocol("dubbo");
        reference.setTimeout(5000);
        reference.setVersion("3.0.0");
        reference.setGroup(null);
        reference.setRegistry(registry);
        reference.setGeneric(true);
        GenericService genericService = reference.get();
        return genericService;
    }

    public static void main(String[] args) {
        GenericService service = getGenericService("172.18.4.48:2181","com.noriental.usersvr.service.okuser.KlassUserService");
        Map<String,Object> map = new HashMap<>();
        map.put("id","10000001");
        Object result = service.$invoke("findStudentClassInfo",new String[]{"com.noriental.usersvr.bean.request.RequestLong"},new Object[]{map});
        System.out.println(result);
    }
}
