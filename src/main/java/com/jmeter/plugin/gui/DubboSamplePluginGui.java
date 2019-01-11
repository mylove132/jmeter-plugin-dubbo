package com.jmeter.plugin.gui;

import com.google.gson.Gson;
import com.jmeter.plugin.dubbo.DubboPlugin;
import com.jmeter.plugin.util.JacksonUtil;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.AbstractProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: liuzhanhui
 * @Decription:
 * @Date: Created in 2019-01-03:17:05
 * Modify date: 2019-01-03:17:05
 */
public class DubboSamplePluginGui extends AbstractSamplerGui {

    private JComboBox address;
    private JComboBox<String> registryProtocol;
    private JTextField service;
    private JTextField method;
    private JTextField requestBean;
    private JTable table;
    private DefaultTableModel model;
    //参数类型
    private JComboBox paramType;
    //参数名
    private JTextField paramName;
    //参数值
    private JTextField paramValue;
    private String[] columnNames;
    private String[] types = new String[]{"java.lang.String", "java.lang.Integter", "java.lang.Double","java.lang.Long",
                                          "java.lang.Char","java.lang.Short","java.lang.Byte","java.lang.Boolean","java.lang.Float"};
    private static Map<String, Map<String, String>> maps = new HashMap<>();

    public DubboSamplePluginGui() {
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
        address = new JComboBox(new String[]{"172.18.4.48:2181", "10.10.6.3:2181"});
        adLable.setLabelFor(address);
        adpanel.add(adLable);
        adpanel.add(address);
        settingPanel.add(adpanel);

        //services
        JPanel servicesPanel = new HorizontalPanel();
        JLabel serviceLabel = new JLabel("Service", SwingConstants.RIGHT);
        service = new JTextField(2);
        JLabel methodLabel = new JLabel("Method", SwingConstants.RIGHT);
        method = new JTextField(2);
        methodLabel.setLabelFor(method);
        serviceLabel.setLabelFor(service);
        servicesPanel.add(serviceLabel);
        servicesPanel.add(service);
        servicesPanel.add(methodLabel);
        servicesPanel.add(method);
        settingPanel.add(servicesPanel);

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
        columnNames = new String[]{"参数类型", "参数名", "参数值"};   //列名
        String[][] tableVales = {{}}; //数据
        model = new DefaultTableModel(tableVales, columnNames);
        table = new JTable(model);
        table.setBackground(Color.CYAN);
        JScrollPane scrollPane = new JScrollPane(table);   //支持滚动
        pp.add(scrollPane, BorderLayout.CENTER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  //单选
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectRowNum = table.getSelectedRow();
            }
        });
        scrollPane.setViewportView(table);
        final JPanel panel = new JPanel();
        pp.add(panel, BorderLayout.SOUTH);
        panel.add(new JLabel("参数类型: "));
        paramType = new JComboBox(types);
        panel.add(paramType);
        panel.add(new JLabel("参数名: "));
        paramName = new JTextField(10);
        panel.add(paramName);
        panel.add(new JLabel("参数值: "));
        paramValue = new JTextField(10);
        panel.add(paramValue);
        final JButton addButton = new JButton("添加");   //添加按钮
        addButton.addActionListener(new ActionListener() {//添加事件
            public void actionPerformed(ActionEvent e) {
                Object[] rowValues = {paramType.getSelectedItem(), paramName.getText(), paramValue.getText()};
                model.addRow(rowValues);  //添加一行
                int rowCount = table.getRowCount() + 1;   //行数加上1
                paramType.setSelectedIndex(0);
                paramName.setText("");
                paramValue.setText("");
            }
        });
        panel.add(addButton);

        final JButton delButton = new JButton("删除");
        delButton.addActionListener(new ActionListener() {//添加事件
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();//获得选中行的索引
                if (selectedRow != -1)  //存在选中行
                {
                    model.removeRow(selectedRow);  //删除行
                } else {
                    JOptionPane.showMessageDialog(panel.getParent(), "选择的行数" + selectedRow + "不存在!", "error", JOptionPane.ERROR_MESSAGE);

                }
            }
        });
        panel.add(delButton);
        JButton close = new JButton("完成");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                maps.clear();
                int countNum = table.getRowCount();
                for (int i = 0; i < countNum; i++) {
                    String type = (String) model.getValueAt(i, 0);
                    String name = (String) model.getValueAt(i, 1);
                    String value = (String) model.getValueAt(i, 2);
                    if (name == null || name.equals("")) {
                        JOptionPane.showMessageDialog(panel.getParent(), "行数" + (i + 1) + "参数名为空!", "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (type == null || type.equals("")) {
                        JOptionPane.showMessageDialog(panel.getParent(), "行数" + (i + 1) + "参数类型为空!", "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (value == null || value.equals("")) {
                        JOptionPane.showMessageDialog(panel.getParent(), "行数" + (i + 1) + "参数值为空!", "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Map<String, String> typeValueMap = new HashMap<>();
                    typeValueMap.put(type, value);
                    maps.put(name, typeValueMap);

                }
                JOptionPane.showMessageDialog(panel.getParent(), "接口参数添加完成", "info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        panel.add(close);
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
        return "dubbo Sample Case";
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
        address.setSelectedIndex(0);
        registryProtocol.setSelectedIndex(0);
        service.setText("");
        method.setText("");
        requestBean.setText("");
        paramType.setSelectedIndex(0);
        paramValue.setText("");
        paramName.setText("");
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        testElement.clear();
        configureTestElement(testElement);
        testElement.setProperty(DubboPlugin.ADDRESS, address.getSelectedItem().toString());
        testElement.setProperty(DubboPlugin.REGISTRY_PROTOCOL, registryProtocol.getSelectedItem().toString());
        testElement.setProperty(DubboPlugin.DUBBO_REGISTRY_SERVICE, service.getText());
        testElement.setProperty(DubboPlugin.DUBBO_REGISTRY_METHOD, method.getText());
        testElement.setProperty(DubboPlugin.REQUEST_BEAN, requestBean.getText());
        System.out.println("传递的map值"+maps.toString());
        testElement.setProperty(DubboPlugin.DUBBO_PARAMS, maps.toString());
    }

    /**
     * sample传值给gui
     *
     * @param element
     */
    @Override
    public void configure(TestElement element) {
        address.setSelectedItem(element.getPropertyAsString(DubboPlugin.ADDRESS));
        registryProtocol.setSelectedItem(element.getPropertyAsString(DubboPlugin.REGISTRY_PROTOCOL));
        service.setText(element.getPropertyAsString(DubboPlugin.DUBBO_REGISTRY_SERVICE));
        method.setText(element.getPropertyAsString(DubboPlugin.DUBBO_REGISTRY_METHOD));
        requestBean.setText(element.getPropertyAsString(DubboPlugin.REQUEST_BEAN));
        //       String paramValues = element.getPropertyAsString(DubboPlugin.DUBBO_PARAMS);
//        Map<String,Object> maps = JacksonUtil.String2Map(paramValues);
//        Map<String,Map<String,String>> vals = new HashMap<String,Map<String,String>>();
//        for (String str:maps.keySet()){
//            Map<String,String> mp = (Map<String,String>)maps.get(str);
//            vals.put(str, mp);
//        }
//        model.setDataVector(objects, columnNames);
        super.configure(element);
    }

    /**
     * <teacherId>6106328</teacherId>
     <resPackageId>89610</resPackageId>
     <classId>1735</classId>
     *
     *
     *
     * com.noriental.lessonsvr.rservice.ResPackageService
     *
     * findPublishStudentByClass
     *
     * com.noriental.lessonsvr.entity.request.FindPublishStudentRequest
     *
     */

    public static void main(String[] args) {
        Map<String, Object> map = JacksonUtil.String2Map("{java={java.lang.String=\"pkp,plk,lkkk\"}}");
        System.out.println(map.get("java"));
    }
}

