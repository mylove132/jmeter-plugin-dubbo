package com.jmeter.plugin.dubbo;

import com.alibaba.dubbo.rpc.service.GenericService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmeter.plugin.util.DubboUtil;
import com.jmeter.plugin.util.JacksonUtil;
import com.jmeter.plugin.util.ZkServiceUtil;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.StringProperty;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    public static String DUBBO_PARAMS = "DUBBO_PARAMS";
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
        String address = getPropertyAsString(ADDRESS);
        String method = getPropertyAsString(DUBBO_REGISTRY_METHOD);
        String requestBean = getPropertyAsString(REQUEST_BEAN);
        String params = getPropertyAsString(DUBBO_PARAMS).trim();
        String reqId = "qa_"+uuid();
        Map<String, Object> paramsMap = JacksonUtil.json2map(params);
        paramsMap.put("reqId",reqId);
        String service = getPropertyAsString(DUBBO_REGISTRY_SERVICE).split(":")[0];
        GenericService genericService = DubboUtil.getGenericService(address, service);
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("method", method);
        requestMap.put("RequestBean", requestBean);
        requestMap.put("param",paramsMap);
        try {
            sampleResult.setRequestHeaders(objectMapper.writeValueAsString(requestMap));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        sampleResult.sampleStart();
        System.out.println("开始时间:"+sampleResult.getStartTime());
        Object result = genericService.$invoke(method,new String[]{requestBean},new Object[]{paramsMap});
        sampleResult.sampleEnd();
        System.out.println("结束时间:"+sampleResult.getEndTime());
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        System.out.println(result);
        if (result.toString().contains("success")){
            sampleResult.setResponseOK();
            sampleResult.setSuccessful(true);
            try {
                sampleResult.setResponseData(objectMapper.writeValueAsString(result),"utf-8");
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }else{
            sampleResult.setSuccessful(false);
            try {
                sampleResult.setResponseData(objectMapper.writeValueAsString(result),"utf-8");
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

    private String uuid(){
        String id = UUID.randomUUID().toString().replace("-","").substring(7,15);
        return id;
    }

}
