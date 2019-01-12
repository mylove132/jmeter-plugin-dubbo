package com.jmeter.plugin.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
 * @Date: Created in 2019-01-11:11:02
 * Modify date: 2019-01-11:11:02
 */
public class TableTest extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    //选中的行数
    private int selectRowNum;
    //参数类型
    private JComboBox paramType;
    //参数名
    private JTextField paramName;
    //参数值
    private JTextField paramValue;
    private String[] types = new String[]{"java.lang.String","java.lang.Integter","java.lang.Double","编辑"};
    private static Map<String,Map<String,String>> maps = new HashMap<>();

    public TableTest(){
        super();
        setTitle("表格");
        setBounds(100, 100, 500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String[] columnNames = {"参数类型","参数名", "参数值"};   //列名
        String[][] tableVales = {{}}; //数据
        model = new DefaultTableModel(tableVales, columnNames);
        table = new JTable(model);
        table.setBackground(Color.GREEN);
        JScrollPane scrollPane = new JScrollPane(table);   //支持滚动
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  //单选
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectRowNum = table.getSelectedRow();
            }
        });
        scrollPane.setViewportView(table);
        final JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.SOUTH);
        panel.add(new JLabel("参数类型: "));
        paramType = new JComboBox(types);
        paramType.addItemListener(e -> {
            Object itemvalue = paramType.getSelectedItem();
            if (itemvalue instanceof String && itemvalue.equals("编辑")){
                paramType.setEditable(true);
            }else {
                paramType.setEditable(false);
            }
        });
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
                Object[] rowValues = {paramType.getSelectedItem(),paramName.getText(), paramValue.getText()};
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
                }else {
                    JOptionPane.showMessageDialog(panel.getParent(), "选择的行数"+selectedRow+"不存在!", "error", JOptionPane.ERROR_MESSAGE);

                }
            }
        });
        panel.add(delButton);
        JButton close =  new JButton("完成");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maps.clear();
                int countNum = table.getRowCount();
                for(int i=0;i<countNum;i++){
                    String type = (String)model.getValueAt(i,0);
                    String name = (String)model.getValueAt(i,1);
                    String value = (String)model.getValueAt(i,2);
                    if (name == null || name.equals("")){
                        JOptionPane.showMessageDialog(panel.getParent(), "行数"+(i+1)+"参数名为空!", "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (type == null || type.equals("")){
                        JOptionPane.showMessageDialog(panel.getParent(), "行数"+(i+1)+"参数类型为空!", "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (value == null || value.equals("")){
                        JOptionPane.showMessageDialog(panel.getParent(), "行数"+(i+1)+"参数值为空!", "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Map<String, String> typeValueMap = new HashMap<>();
                    typeValueMap.put(type, value);
                    maps.put(name,typeValueMap);
                }
            }
        });
        panel.add(close);
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        TableTest tableTest = new TableTest();
        tableTest.setVisible(true);


    }
}


class FontCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);
        Font font = new Font((String) value, Font.BOLD, 10);
        label.setFont(font);
        return label;
    }
}