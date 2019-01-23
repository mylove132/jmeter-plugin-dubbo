package com.jmeter.plugin.util;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.RegistryConfig;

/**
 * @Author: liuzhanhui
 * @Decription:
 * @Date: Created in 2019-01-15:10:07
 * Modify date: 2019-01-15:10:07
 */
public class Test {

    public static void main(String[] args) {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("dubbosample");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("10.10.6.1:2181");
    }
}
