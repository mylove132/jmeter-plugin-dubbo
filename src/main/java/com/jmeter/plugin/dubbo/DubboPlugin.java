package com.jmeter.plugin.dubbo;

import com.alibaba.dubbo.rpc.service.GenericService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmeter.plugin.gui.DubboPluginGui;
import com.jmeter.plugin.util.DubboUtil;
import com.jmeter.plugin.util.JacksonUtil;
import com.jmeter.plugin.util.ZkServiceUtil;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: liuzhanhui
 * @Decription:
 * @Date: Created in 2019-01-03:17:06
 * Modify date: 2019-01-03:17:06
 */
public class DubboPlugin extends AbstractSampler {

    public static String ADDRESS = "ADDRESS";
    public static String REGISTRY_PROTOCOL = "REGISTRY_PROTOCOL";
    public static String DUBBO_REGISTRY_SERVICE = "DUBBO_REGISTRY_SERVICE";
    public static String DUBBO_REGISTRY_METHOD = "DUBBO_REGISTRY_METHOD";
    public static String REQUEST_BEAN = "REQUEST_BEAN";
    public static String DUBBO_PARAMS = "";
    public static final String APPLICATION_NAME = "dubboSample";
    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    private long start = 0;
    private long end = 0;

    public DubboPlugin() {
        classCount.incrementAndGet();
        trace("DubboPlugin()");
    }

    @Override
    public SampleResult sample(Entry entry) {
        final ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        SampleResult sampleResult = new SampleResult();
        sampleResult.setSampleLabel(this.getTitle());
        String protocol = getPropertyAsString(REGISTRY_PROTOCOL);
        String address = getPropertyAsString(ADDRESS);
        String method = getPropertyAsString(DUBBO_REGISTRY_METHOD);
        String requestBean = getPropertyAsString(REQUEST_BEAN);
        String service = getPropertyAsString(DUBBO_REGISTRY_SERVICE).split(":")[0];

        if (protocol.equals("none") || protocol == null) {
            JOptionPane.showMessageDialog(new DubboPluginGui().getParent(), "注册协议不能为空!", "error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (address.equals("") || address == null) {
            JOptionPane.showMessageDialog(new DubboPluginGui().getParent(), "zk地址不能为空!", "error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (service.equals("") || service == null) {
            JOptionPane.showMessageDialog(new DubboPluginGui().getParent(), "服务接口不能为空!", "error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (method.equals("") || method == null) {
            JOptionPane.showMessageDialog(new DubboPluginGui().getParent(), "接口方法不能为空!", "error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        Map<String, Object> pamMap = new HashMap<>();
        Map<String, Map<String, String>> pMap = new HashMap<String, Map<String, String>>();
        String params = getPropertyAsString(DUBBO_PARAMS);
        System.out.println("@@@@@@"+params);
        pMap = JacksonUtil.json2Map(params);
        System.out.println("~~~~~~~~~~~~paramMap"+pMap);

        for (String name : pMap.keySet()) {
            Map<String, String> typeMap = pMap.get(name);
            for (String type : typeMap.keySet()) {
                switch (type) {
                    case "java.lang.String":
                        String strValue = typeMap.get(type);
                        if (strValue.contains(",")) {
                            List<String> strList = new ArrayList<>();
                            String s[] = strValue.split(",");
                            for (String str : s) {
                                strList.add(str);
                            }
                            pamMap.put(name, strList);
                        } else {
                            pamMap.put(name, new BigDecimal(typeMap.get(type)).toString());
                        }
                        break;
                    case "java.lang.Integter":
                        String intValue = typeMap.get(type);
                        if (intValue.contains(",")) {
                            List<Integer> intList = new ArrayList<>();
                            String s[] = intValue.split(",");
                            for (String str : s) {
                                int res = Math.round(Float.parseFloat(str));
                                intList.add(res);
                            }
                            pamMap.put(name, intList);
                        } else {
                            String value = typeMap.get(type);
                            int res = Math.round(Float.parseFloat(value));
                            pamMap.put(name, res);
                        }
                        break;
                    case "java.lang.Double":
                        String doubleValue = typeMap.get(type);
                        if (doubleValue.contains(",")) {
                            List<Double> doubleList = new ArrayList<>();
                            String s[] = doubleValue.split(",");
                            for (String str : s) {
                                double res = Double.parseDouble(str);
                                doubleList.add(res);
                            }
                            pamMap.put(name, doubleList);
                        } else {
                            pamMap.put(name, Double.parseDouble(typeMap.get(type)));
                        }
                        break;
                    case "java.lang.Byte":
                        String byteValue = typeMap.get(type);
                        if (byteValue.contains(",")) {
                            List<Byte> byteList = new ArrayList<>();
                            String s[] = byteValue.split(",");
                            for (String str : s) {
                                Byte res = Byte.parseByte(str);
                                byteList.add(res);
                            }
                            pamMap.put(name, byteList);
                        } else {
                            pamMap.put(name, Byte.parseByte(typeMap.get(type)));
                        }
                        break;
                    case "java.lang.Short":
                        String shortValue = typeMap.get(type);
                        if (shortValue.contains(",")) {
                            List<Short> shortList = new ArrayList<>();
                            String s[] = shortValue.split(",");
                            for (String str : s) {
                                Short res = Short.parseShort(str);
                                shortList.add(res);
                            }
                            pamMap.put(name, shortList);
                        } else {
                            pamMap.put(name, Short.parseShort(typeMap.get(type)));
                        }
                        break;
                    case "java.lang.Char":
                        pamMap.put(name, typeMap.get(type).toCharArray());
                        break;
                    case "java.lang.Float":
                        String floatValue = typeMap.get(type);
                        if (floatValue.contains(",")) {
                            List<Float> floatList = new ArrayList<>();
                            String s[] = floatValue.split(",");
                            for (String str : s) {
                                Float res = Float.parseFloat(str);
                                floatList.add(res);
                            }
                            pamMap.put(name, floatList);
                        } else {
                            pamMap.put(name, Float.parseFloat(typeMap.get(type)));
                        }
                        break;
                    case "java.lang.Long":
                        String longValue = typeMap.get(type);
                        if (longValue.contains(",")) {
                            List<Long> longList = new ArrayList<>();
                            String s[] = longValue.split(",");
                            for (String str : s) {
                                Long lValue = new BigDecimal(str).longValue();
                                longList.add(lValue);
                            }
                            pamMap.put(name, longList);
                        } else {
                            Long lValue = new BigDecimal(typeMap.get(type)).longValue();
                            pamMap.put(name, lValue);
                        }
                        break;
                    default:
                        pamMap.put(name, typeMap.get(type));
                        break;
                }
            }
        }
        String reqId = "qa_" + uuid();
        pamMap.put("reqId", reqId);
        System.out.println("~~~~~~~~~~~~~~~~~~~~" + pamMap);

        GenericService genericService = DubboUtil.getGenericService(address, service);
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("method", method);
        requestMap.put("RequestBean", requestBean);
        requestMap.put("param", pamMap);
        try {
            sampleResult.setRequestHeaders(objectMapper.writeValueAsString(requestMap));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        sampleResult.sampleStart();
        System.out.println("开始时间:" + sampleResult.getStartTime());
        Object result = null;try {
            genericService.$invoke(method, new String[]{requestBean}, new Object[]{pamMap});
        }catch (Exception e){
            JOptionPane.showMessageDialog(new DubboPluginGui().getParent(), "zk连接异常!", "error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        sampleResult.sampleEnd();
        System.out.println("结束时间:" + sampleResult.getEndTime());
        System.out.println(result);
        if (result.toString().contains("success")) {
            sampleResult.setResponseOK();
            sampleResult.setSuccessful(true);
            try {
                sampleResult.setResponseData(objectMapper.writeValueAsString(result), "utf-8");
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            sampleResult.setSuccessful(false);
            try {
                sampleResult.setResponseData(objectMapper.writeValueAsString(result), "utf-8");
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return sampleResult;
    }

    /**
     * @return a string for the sampleResult Title
     */
    private String getTitle() {
        return this.getName();
    }

    /*
    * Helper method
    */
    private void trace(String s) {
        String tl = getTitle();
        String tn = Thread.currentThread().getName();
        String th = this.toString();
    }

    private String uuid() {
        String id = UUID.randomUUID().toString().replace("-", "").substring(7, 15);
        return id;
    }
}
