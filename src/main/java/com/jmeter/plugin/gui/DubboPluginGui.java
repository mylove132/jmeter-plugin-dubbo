package com.jmeter.plugin.gui;

import com.jmeter.plugin.dubbo.DubboPlugin;
import com.jmeter.plugin.util.JAutoCompleteComboBox;
import com.jmeter.plugin.util.DubboUtil;
import com.jmeter.plugin.util.ZkServiceUtil;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.Set;

/**
 * @Author: liuzhanhui
 * @Decription:
 * @Date: Created in 2019-01-03:17:05
 * Modify date: 2019-01-03:17:05
 */
public class DubboPluginGui extends AbstractSamplerGui {

    private JTextField address;
    private JComboBox<String> registryProtocol;
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    private JComboBox service;
    DefaultComboBoxModel methodModle = new DefaultComboBoxModel();
    private JComboBox method;
    private Map<String, String[]> serviceMethods;
    private JTextField requestBean;
    private JTextArea params;

    public DubboPluginGui() {
        init();
    }

    public JPanel settingPanel() {

        //Registry Settings
        JPanel settingPanel = new VerticalPanel();
        settingPanel.setBorder(BorderFactory.createTitledBorder("Registry Settings"));

        //Protocol
        JPanel ph = new HorizontalPanel();
        JLabel protocolLable = new JLabel("Protocol:", SwingConstants.RIGHT);
        registryProtocol = new JComboBox<String>(new String[]{"none", "zookeeper", "multicast", "redis", "simple"});
        protocolLable.setLabelFor(registryProtocol);
        ph.add(protocolLable);
        ph.add(registryProtocol);
        settingPanel.add(ph);

        //address
        JPanel adpanel = new HorizontalPanel();
        JLabel adLable = new JLabel("Address:", SwingConstants.RIGHT);
        address = new JTextField(2);
        adLable.setLabelFor(address);
        adpanel.add(adLable);
        adpanel.add(address);
        settingPanel.add(adpanel);

        //services
        JPanel servicesPanel = new HorizontalPanel();
        JButton gBtn = new JButton("获取注册服务");
        JLabel serviceLabel = new JLabel("Service list:", SwingConstants.RIGHT);
        model.addElement("none");
        methodModle.addElement("method none");
        service = new JAutoCompleteComboBox(model);
        Dimension dimension = new Dimension();
        dimension.setSize(300, 1);
        service.setPreferredSize(dimension);
        JLabel methodLabel = new JLabel("Method list:", SwingConstants.RIGHT);
        method = new JAutoCompleteComboBox(methodModle);
        Dimension methodSimension = new Dimension();
        methodSimension.setSize(240, 1);
        method.setPreferredSize(methodSimension);
        methodLabel.setLabelFor(method);
        serviceLabel.setLabelFor(service);
        servicesPanel.add(gBtn);
        servicesPanel.add(serviceLabel);
        servicesPanel.add(service);
        servicesPanel.add(methodLabel);
        servicesPanel.add(method);
        settingPanel.add(servicesPanel);

        gBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getDubboRegistryServices();
            }
        });

        service.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                getMethods(e);
            }
        });

        return settingPanel;
    }

    public JPanel paramPanel() {

        JPanel paramPanel = new VerticalPanel();
        paramPanel.setBorder(BorderFactory.createTitledBorder("Interface Param"));

        JPanel rb = new HorizontalPanel();
        JLabel rbLabel = new JLabel("Request:", SwingConstants.RIGHT);
        requestBean = new JTextField(2);
        rbLabel.setLabelFor(requestBean);
        rb.add(rbLabel);
        rb.add(requestBean);
        paramPanel.add(rb);

        //Param
        JPanel pp = new HorizontalPanel();
        JLabel protocolLable = new JLabel("Param:", SwingConstants.RIGHT);
        params = new JTextArea();
        params.setColumns(12);
        params.setRows(20);
        protocolLable.setLabelFor(params);
        JScrollPane jsp = new JScrollPane(params);
        pp.add(protocolLable);
        pp.add(params);
        pp.add(jsp);
        paramPanel.add(pp);


        return paramPanel;
    }

    /**
     * 创建面板
     */
    public void init() {
        JPanel settingPanel = new VerticalPanel(5, 0);
        settingPanel.setBorder(makeBorder());
        Container container = makeTitlePanel();
        settingPanel.add(container);
        settingPanel.add(settingPanel());
        settingPanel.add(paramPanel());
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(settingPanel, BorderLayout.CENTER);
    }

    @Override
    public String getStaticLabel() {
        return "dubbo Case";
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public TestElement createTestElement() {
        DubboPlugin dubboPlugin = new DubboPlugin();
        modifyTestElement(dubboPlugin);
        return dubboPlugin;
    }

    @Override
    public void clearGui() {
        super.clearGui();
        address.setText("");
        registryProtocol.setSelectedIndex(0);
        service.setSelectedIndex(0);
        method.setSelectedIndex(0);
        requestBean.setText("");
        params.setText("");
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        testElement.clear();
        configureTestElement(testElement);
        testElement.setProperty(DubboPlugin.ADDRESS, address.getText());
        testElement.setProperty(DubboPlugin.REGISTRY_PROTOCOL, registryProtocol.getSelectedItem().toString());
        testElement.setProperty(DubboPlugin.DUBBO_REGISTRY_SERVICE, service.getSelectedItem().toString());
        testElement.setProperty(DubboPlugin.DUBBO_REGISTRY_METHOD, method.getSelectedItem().toString());
        testElement.setProperty(DubboPlugin.REQUEST_BEAN, requestBean.getText());
        testElement.setProperty(DubboPlugin.DUBBO_PARAMS, params.getText());
    }

    /**
     * sample传值给gui
     *
     * @param element
     */
    @Override
    public void configure(TestElement element) {
        address.setText(element.getPropertyAsString(DubboPlugin.ADDRESS));
        registryProtocol.setSelectedItem(element.getPropertyAsString(DubboPlugin.REGISTRY_PROTOCOL));
        service.setSelectedItem(element.getPropertyAsString(DubboPlugin.DUBBO_REGISTRY_SERVICE));
        method.setSelectedItem(element.getPropertyAsString(DubboPlugin.DUBBO_REGISTRY_METHOD));
        requestBean.setText(element.getPropertyAsString(DubboPlugin.REQUEST_BEAN));
        params.setText(element.getPropertyAsString(DubboPlugin.DUBBO_PARAMS));
        super.configure(element);
    }

    //获取dubbo接口列表
    public void getDubboRegistryServices() {
        String protocolText = registryProtocol.getSelectedItem().toString();
        String addressText = address.getText();
        if (protocolText.equals("none") || protocolText.equals("")) {
            JOptionPane.showMessageDialog(this.getParent(), "注册协议不能为空!", "error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (addressText == null || addressText.equals("")) {
            JOptionPane.showMessageDialog(this.getParent(), "注册地址不能为空!", "error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int result = JOptionPane.showConfirmDialog(this.getParent(), "确定拉取dubbo注册服务？", "warn", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                serviceMethods = ZkServiceUtil.getInterfaceMethods(addressText.trim());
            }catch (RuntimeException e){
                super.clearGui();
                JOptionPane.showMessageDialog(this.getParent(), "zookeeper连接失败", "error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this.getParent(), "请检查zookeeper配置地址", "error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this.getParent(), "获取注册列表完成", "info", JOptionPane.INFORMATION_MESSAGE);
            Set<String> services = serviceMethods.keySet();
            for (String str : services) {
                model.addElement(str);
            }
        }
    }

    //获取接口方法列表
    public void getMethods(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            String serverName = e.getItem().toString();
            System.out.println(serverName);
            String[] methods = serviceMethods.get(serverName);
            methodModle.removeAllElements();
            methodModle.addElement("method none");
            if (methods == null) {
                methodModle.addElement("method none");
            } else {
                for (String method : methods) {
                    methodModle.addElement(method);
                }
            }

        }
    }
}

