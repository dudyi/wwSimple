package com.mls.formapp.wordwind;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

public class SenceViewMain extends ApplicationTemplate{    
    //初始化窗体
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
		private static final long serialVersionUID = -130932605398355602L;
		SenceLayerOperation sence=null;
		
		public AppFrame()
        {
        	/********************************三维视图********************************/
        	//三维视图初始化
        	super(true,false,false); 
			//super(new Dimension(900, 800));
	    	//地图资源加载
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            Thread t = new Thread(new Runnable() {
                public void run() {
                	//初始化主视图
                	InitMainView(getWwjPanel());
                	
                	/**************图层叠加**************/
                	sence = new SenceLayerOperation((WorldWindowGLCanvas)getWwd());
        	        //体图层
        	    	//sence.AddWedgeLayer();
        	        //缓存切片图层
        	    	sence.AddImageryCacheLayer();
        	        //新增WMS图层
        	    	//sence.AddWMSLayre();	        
        	        //图标图层
        	    	//sence.AddIconLayer();
        	    	//线图层
        	    	//sence.AddEntityLayer();
                	//文件影像
                	//sence.installImagery();
                	//文件地形
                	//sence.installElevations();
                	
                    //加载完毕，恢复状态
                    setCursor(Cursor.getDefaultCursor());
                }
            });
            t.start();			
        }
        
        /********************************界面操作视图********************************/	
        public void InitMainView(JPanel paneMap) {
        	//菜单面板
        	JPanel panMenu = CreateMenuPanel();
			//分层浮动
			JLayeredPane layeredPane = new JLayeredPane();
			layeredPane.add(panMenu,100);
			layeredPane.add(paneMap,10);
			
			this.setContentPane(layeredPane);
		
			//窗体大小改变
			this.addComponentListener(new ComponentAdapter(){
				@Override 
				public void componentResized(ComponentEvent e){
					//窗体
					Frame fram = AppFrame.getFrames()[0];
					//适应
					paneMap.setBounds(0, 0, fram.getWidth(),fram.getHeight()-48);
					paneMap.getCursor();
					//重绘，不然有bug
					fram.validate();
					fram.repaint();
				}
			});
        }
        
        //创建菜单面板
        protected JPanel CreateMenuPanel() 
        {
        	//控件
			JPanel pan = new JPanel();
			pan.setBounds(20, 200, 320, 480);
			
			//初始值设置
			pan.setLayout(null);
			
			JLabel label1 = new JLabel("DY选型：");
			label1.setBounds(GetBoundsByType(1,0));
			//label1.setHorizontalAlignment(SwingConstants.RIGHT);
			String[] item = {"选型Ⅰ", "选型Ⅱ","选型三"};
			JComboBox<String> textField1 = new JComboBox<String>(item);
			textField1.setBounds(GetBoundsByType(1,1));
			
			JLabel label2 = new JLabel("投弹模式：");
			label2.setBounds(GetBoundsByType(2,0));
			String[] item1 = {"CCIP", "CCIP1","CCIP2"};
			JComboBox<String> textField2 = new JComboBox<String>(item1);
			textField2.setBounds(GetBoundsByType(2,1));
			
			JLabel label3 = new JLabel("初始速度(m/s)：");
			label3.setBounds(GetBoundsByType(3,0));
			JTextField textField3 = new JTextField();
			textField3.setBounds(GetBoundsByType(3,1));
			
			JLabel label4 = new JLabel("能见度系数：");
			label4.setBounds(GetBoundsByType(4,0));
			JTextField textField4 = new JTextField();
			textField4.setBounds(GetBoundsByType(4,1));
			
			JLabel label5 = new JLabel("航向角(十进制度)：");
			label5.setBounds(GetBoundsByType(5,0));
			JTextField textField5 = new JTextField();
			textField5.setBounds(GetBoundsByType(5,1));
			
			JLabel label6 = new JLabel("俯仰角(十进制度)：");
			label6.setBounds(GetBoundsByType(6,0));
			JTextField textField6 = new JTextField();
			textField6.setBounds(GetBoundsByType(6,1));
			
			JLabel label7 = new JLabel("相对高度(m)：");
			label7.setBounds(GetBoundsByType(7,0));
			JTextField textField7 = new JTextField();
			textField7.setBounds(GetBoundsByType(7,1));
			
			JLabel label8 = new JLabel("海拔(m)：");
			label8.setBounds(GetBoundsByType(8,0));
			JTextField textField8 = new JTextField();
			textField8.setBounds(GetBoundsByType(8,1));
			
			JLabel label9 = new JLabel("风速(m/s)：");
			label9.setBounds(GetBoundsByType(9,0));
			JTextField textField9 = new JTextField();
			textField9.setBounds(GetBoundsByType(9,1));
			
			JLabel label10 = new JLabel("风向：");
			label10.setBounds(GetBoundsByType(10,0));
			String[] item2 = {"北风", "东风","西风","南风"};
			JComboBox<String> textField10 = new JComboBox<String>(item2);
			textField10.setBounds(GetBoundsByType(10,1));
			
			Rectangle rec = GetBoundsByType(11,0);
			JButton button1 = new JButton("解算");
			button1.setBounds(340/2-80,rec.y,60,rec.height);
			button1.setContentAreaFilled(false);
			button1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	sence.AddEntityLayer();
                	sence.AddMarkersLayer();
                }
            });
			JButton button2 = new JButton("回放");
			button2.setBounds(340/2+20,rec.y,60,rec.height);
			button2.setContentAreaFilled(false);
			button2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
//                	sence.RemoveEntityLayer();
//                	sence.RemoveMarkersLayer();
//                	//重绘
//                	SenceLayerOperation.getWorldWindowGLCanvas().redraw();
                	
                	sence.HistoryTrack();
                }
            });
			
			pan.add(label1);
			pan.add(textField1);
			pan.add(label2);
			pan.add(textField2);
			pan.add(label3);
			pan.add(textField3);
			pan.add(label4);
			pan.add(textField4);
			pan.add(label5);
			pan.add(textField5);
			pan.add(label6);
			pan.add(textField6);
			pan.add(label7);
			pan.add(textField7);
			pan.add(label8);
			pan.add(textField8);
			pan.add(label9);
			pan.add(textField9);
			pan.add(label10);
			pan.add(textField10);

			pan.add(button1);	
			pan.add(button2);
			
			return pan;
        }
        
        protected Rectangle GetBoundsByType(int n,int type) 
        {
        	//水平间距、垂直间距
        	int horizontalInterval =10,verticalInterval=10;
        	//lable宽、input宽、高
			int lableW = 115,inputW =160,h=30;
			//lable初始左侧间距、input初始左侧间距、初始头部间距
			int lableLeftInterval =20,inputLeftInterval = lableLeftInterval + lableW + horizontalInterval,originTop=10;
			//类别判断
			if(type==0)
				return new Rectangle(lableLeftInterval,originTop+verticalInterval*n+h*(n-1),lableW,h);
			else if(type == 1)
				return new Rectangle(inputLeftInterval,originTop+verticalInterval*n+h*(n-1),inputW,h);
			else
				return new Rectangle(0,0,0,0);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("模拟程序", SenceViewMain.AppFrame.class);
    }
}
