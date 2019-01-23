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
import com.jmeter.plugin.dubbo.DubboSample;
import com.jmeter.plugin.dubbo.JAutoCompleteComboBox;
import com.jmeter.plugin.gui.ProviderService;
import com.jmeter.plugin.util.MethodArgument;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * DubboSampleGui </br>
 * invoke sequence**clearGui()->createTestElement()->modifyTestElement()->configure()**
 */
public class DubboSampleGui extends AbstractSamplerGui {

    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     */
    private static final long serialVersionUID = -3248204995359935007L;

    private JComboBox<String> registryProtocolText;
    private JComboBox<String> rpcProtocolText;
    private JTextField addressText;
    private JTextField timeoutText;
    private JTextField retriesText;
    private JTextField interfaceText;
    private JTextField methodText;
    private DefaultTableModel model;
    private String[] columnNames = {"paramType", "paramValue"};
    private String[] tmpRow = {"", ""};
    private int textColumns = 2;
    private JAutoCompleteComboBox<String> interfaceList;
    private JAutoCompleteComboBox<String> methodList;

    public DubboSampleGui() {
        super();
        init();
    }

    /**
     * Initialize the interface layout and elements
     */
    private void init() {
        //所有设置panel，垂直布局
        JPanel settingPanel = new VerticalPanel(5, 0);
        settingPanel.setBorder(makeBorder());
        Container container = makeTitlePanel();
        settingPanel.add(container);

        //Registry Settings
        JPanel registrySettings = new VerticalPanel();
        registrySettings.setBorder(BorderFactory.createTitledBorder("Registry Settings"));
        //Protocol
        JPanel ph = new HorizontalPanel();
        JLabel protocolLable = new JLabel("Protocol:", SwingConstants.RIGHT);
        registryProtocolText = new JComboBox<String>(new String[]{"none", "zookeeper", "multicast", "redis", "simple"});
        protocolLable.setLabelFor(registryProtocolText);
        ph.add(protocolLable);
        ph.add(registryProtocolText);
        registrySettings.add(ph);
        //Address
        JPanel ah = new HorizontalPanel();
        JLabel addressLable = new JLabel("Address:", SwingConstants.RIGHT);
        addressText = new JTextField(textColumns);
        addressLable.setLabelFor(addressText);
        ah.add(addressLable);
        ah.add(addressText);

        registrySettings.add(ah);
        //Selection Interface
        JPanel sh = new HorizontalPanel();
        JButton jButton = new JButton("Get Provider List");
        interfaceList = new JAutoCompleteComboBox<String>(new DefaultComboBoxModel<String>(new String[]{}), new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    doChange(e.getItem().toString());
                }
            }
        });
        interfaceList.addPropertyChangeListener("model", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (interfaceList.getSelectedItem() != null) {
                    doChange(interfaceList.getSelectedItem().toString());
                } else {
                    methodList.setModel(new DefaultComboBoxModel<String>(new String[]{}));
                }
            }
        });
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doConfirm(e, interfaceList);
            }
        });
        sh.add(jButton);
        sh.add(new JLabel("Interfaces:", SwingConstants.RIGHT));
        sh.add(interfaceList);
        sh.add(new JLabel("Methods:", SwingConstants.RIGHT));
        methodList = new JAutoCompleteComboBox<String>(new DefaultComboBoxModel<String>(new String[]{}), new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    methodText.setText(e.getItem().toString());
                }
            }
        });
        methodList.addPropertyChangeListener("model", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (methodList.getSelectedItem() != null) {
                    methodText.setText(methodList.getSelectedItem().toString());
                }
            }
        });
        sh.add(methodList);
        registrySettings.add(sh);


        //RPC Protocol Settings
        JPanel protocolSettings = new VerticalPanel();
        protocolSettings.setBorder(BorderFactory.createTitledBorder("RPC Protocol Settings"));
        //RPC Protocol
        JPanel rpcPh = new HorizontalPanel();
        JLabel rpcProtocolLable = new JLabel("Protocol:", SwingConstants.RIGHT);
        rpcProtocolText = new JComboBox<String>(new String[]{"dubbo://", "rmi://", "hessian://", "webservice://", "memcached://", "redis://"});
        rpcProtocolLable.setLabelFor(rpcProtocolText);
        rpcPh.add(rpcProtocolLable);
        rpcPh.add(rpcProtocolText);
        protocolSettings.add(rpcPh);

        //Consumer Settings
        JPanel consumerSettings = new VerticalPanel();
        consumerSettings.setBorder(BorderFactory.createTitledBorder("Consumer&Service Settings"));
        JPanel h = new HorizontalPanel();
        //Timeout
        JLabel timeoutLable = new JLabel(" Timeout:", SwingConstants.RIGHT);
        timeoutText = new JTextField(textColumns);
        timeoutText.setText(DubboSample.DEFAULT_TIMEOUT);
        timeoutLable.setLabelFor(timeoutText);
        h.add(timeoutLable);
        h.add(timeoutText);

        //Retries
        JLabel retriesLable = new JLabel("Retries:", SwingConstants.RIGHT);
        retriesText = new JTextField(textColumns);
        retriesText.setText(DubboSample.DEFAULT_RETRIES);
        retriesLable.setLabelFor(retriesText);
        h.add(retriesLable);
        h.add(retriesText);

        //Interface Settings
        JPanel interfaceSettings = new VerticalPanel();
        interfaceSettings.setBorder(BorderFactory.createTitledBorder("Interface Settings"));
        //Interface
        JPanel ih = new HorizontalPanel();
        JLabel interfaceLable = new JLabel("Interface:", SwingConstants.RIGHT);
        interfaceText = new JTextField(textColumns);
        interfaceLable.setLabelFor(interfaceText);
        ih.add(interfaceLable);
        ih.add(interfaceText);
        interfaceSettings.add(ih);
        //Method
        JPanel mh = new HorizontalPanel();
        JLabel methodLable = new JLabel("   Method:", SwingConstants.RIGHT);
        methodText = new JTextField(textColumns);
        methodLable.setLabelFor(methodText);
        mh.add(methodLable);
        mh.add(methodText);
        interfaceSettings.add(mh);

        //表格panel
        JPanel tablePanel = new HorizontalPanel();
        //Args
        JLabel argsLable = new JLabel("        Args:", SwingConstants.RIGHT);
        model = new DefaultTableModel();
//        model.setDataVector(new String[][]{{"", ""}}, columnNames);
        model.setDataVector(null, columnNames);
        final JTable table = new JTable(model);
        table.setRowHeight(40);
        //失去光标退出编辑
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        //添加按钮
        JButton addBtn = new JButton("增加");
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                model.addRow(tmpRow);
            }
        });
        JButton delBtn = new JButton("删除");
        delBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                int rowIndex = table.getSelectedRow();
                if(rowIndex != -1) {
                	model.removeRow(rowIndex);
                }
            }
        });
        //表格滚动条
        JScrollPane scrollpane = new JScrollPane(table);
        tablePanel.add(argsLable);
        tablePanel.add(scrollpane);
        tablePanel.add(addBtn);
        tablePanel.add(delBtn);
        interfaceSettings.add(tablePanel);

        //所有设置panel
        settingPanel.add(registrySettings);
        settingPanel.add(protocolSettings);
        settingPanel.add(consumerSettings);
        settingPanel.add(interfaceSettings);

        //全局布局设置
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(settingPanel,BorderLayout.CENTER);
    }


    /**
     * this method sets the Sample's data into the gui
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        log.debug("sample赋值给gui");
        DubboSample sample = (DubboSample) element;
        registryProtocolText.setSelectedItem(sample.getRegistryProtocol());
        rpcProtocolText.setSelectedItem(sample.getRpcProtocol());
        addressText.setText(sample.getAddress());
        timeoutText.setText(sample.getTimeout());
        interfaceText.setText(sample.getInterface());
        methodText.setText(sample.getMethod());
        Vector<String> columnNames = new Vector<String>();
        columnNames.add("paramType");
        columnNames.add("paramValue");
        model.setDataVector(paserMethodArgsData(sample.getMethodArgs()), columnNames);
    }

    /**
     * Create a new sampler. And pass it to the modifyTestElement(TestElement) method.
     */
    @Override
    public TestElement createTestElement() {
        log.debug("创建sample对象");
        //创建sample对象
        DubboSample sample = new DubboSample();
        modifyTestElement(sample);
        return sample;
    }

    /**
     * component title/name
     */
    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    /**
     * this method sets the Gui's data into the sample
     */
    @SuppressWarnings("unchecked")
    @Override
    public void modifyTestElement(TestElement element) {
        log.debug("gui数据赋值给sample");
        //给sample赋值
        super.configureTestElement(element);
        DubboSample sample = (DubboSample) element;
        sample.setRegistryProtocol(registryProtocolText.getSelectedItem().toString());
        sample.setRpcProtocol(rpcProtocolText.getSelectedItem().toString());
        sample.setAddress(addressText.getText());
        sample.setTimeout(timeoutText.getText());
        sample.setInterfaceName(interfaceText.getText());
        sample.setMethod(methodText.getText());
        sample.setMethodArgs(getMethodArgsData(model.getDataVector()));
    }

    private Vector<Vector<String>> paserMethodArgsData(List<MethodArgument> list) {
    	Vector<Vector<String>> res = new Vector<Vector<String>>();
    	for (MethodArgument args : list) {
    		Vector<String> v = new Vector<String>();
    		v.add(args.getParamType());
    		v.add(args.getParamValue());
    		res.add(v);
    	}
    	return res;
    }

    private List<MethodArgument> getMethodArgsData(Vector<Vector<String>> data) {
    	List<MethodArgument> params = new ArrayList<MethodArgument>();
    	if (!data.isEmpty()) {
    		 //处理参数
            Iterator<Vector<String>> it = data.iterator();
            while(it.hasNext()) {
                Vector<String> param = it.next();
                if (!param.isEmpty()) {
                	params.add(new MethodArgument(param.get(0), param.get(1)));
                }
            }
    	}
    	return params;
    }

    /**
     * sample's name
     */
    @Override
    public String getStaticLabel() {
        return "Dubbo Sample";
    }

    /**
     * clear gui's data
     */
    @Override
    public void clearGui() {
        log.debug("清空gui数据");
        super.clearGui();
        registryProtocolText.setSelectedIndex(0);
        rpcProtocolText.setSelectedIndex(0);
        addressText.setText("");
        timeoutText.setText(DubboSample.DEFAULT_TIMEOUT);
        retriesText.setText(DubboSample.DEFAULT_RETRIES);
        interfaceText.setText("");
        methodText.setText("");
        model.setDataVector(null, columnNames);
    }

    private void doChange(String key) {
        ProviderService providerService = ProviderService.get(addressText.getText());
        Map<String, URL> provider = providerService.findByService(key);
        if (provider != null && !provider.isEmpty()) {
            URL url = new ArrayList<URL>(provider.values()).get(0);
            String timeout = url.getParameter(com.alibaba.dubbo.common.Constants.TIMEOUT_KEY);
            String protocol = url.getProtocol() + "://";
            String interfaceName = url.getServiceInterface();
            String method = url.getParameter(com.alibaba.dubbo.common.Constants.METHODS_KEY);
            timeoutText.setText(timeout);
            rpcProtocolText.setSelectedItem(protocol);
            interfaceText.setText(interfaceName);
            //set method
            String[] items = method.split(",");
            methodList.setModel(new DefaultComboBoxModel<String>(items));
        } else {
            methodList.setModel(new DefaultComboBoxModel<String>(new String[]{}));
        }
    }

    private void doConfirm(ActionEvent event, JAutoCompleteComboBox<String> interfaceList) {
        String protocol = registryProtocolText.getSelectedItem().toString();
        String address = addressText.getText();
        if (StringUtils.isBlank(address)) {
            JOptionPane.showMessageDialog(this.getParent(), "Address can't be empty!", "error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int result = JOptionPane.showConfirmDialog(this.getParent(), "Obtaining all the providers lists may cause jmeter to stop responding for a few seconds. Do you want to continue?", "warn", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            List<String> list = new ArrayList<String>();
            try {
                list = ProviderService.get(address).getProviders(protocol, address);
                JOptionPane.showMessageDialog(this.getParent(), "Get provider list to finish! Check if the log has errors.", "info", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this.getParent(), e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String[] items = list.toArray(new String[]{});
            interfaceList.setModel(new DefaultComboBoxModel<String>(items));
        }
    }

}


