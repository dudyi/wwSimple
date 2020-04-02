package com.mls.formapp.wordwind;

import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

public class SenceViewMain extends ApplicationTemplate{    
    //初始化窗体
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
		private static final long serialVersionUID = -130932605398355602L;

		public AppFrame()
        {
        	/********************************三维视图********************************/
        	//三维视图初始化
        	super(true,false,false);        	
	    	//地图资源加载
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            Thread t = new Thread(new Runnable() {
                public void run() {
                	//初始化主视图
                	InitMainView();
                	//图层叠加
                	SenceLayerOperation sence = new SenceLayerOperation((WorldWindowGLCanvas)getWwd());
        	        //体图层
        	    	sence.AddWedgeLayer();
        	        //缓存切片图层
        	    	sence.AddImageryCacheLayer();
        	        //新增WMS图层
        	    	//sence.AddWMSLayre();	        
        	        //图标图层
        	    	sence.AddIconLayer();
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
        public void InitMainView() {
        	//控件
			JPanel pan = new JPanel();
			pan.setBounds(20, 200, 200, 400);
			JLabel label1 = new JLabel("参数1：");
			JTextField textField1 = new JTextField(16);
			JLabel label2 = new JLabel("参数2：");
			JTextField textField2 = new JTextField(16);
			JLabel label3 = new JLabel("参数3：");
			JTextField textField3 = new JTextField(16);
			JButton button = new JButton("确定");
			pan.add(label1);
			pan.add(textField1);
			pan.add(label2);
			pan.add(textField2);
			pan.add(label3);
			pan.add(textField3);
			pan.add(button);

			//分层浮动
			JLayeredPane layeredPane = new JLayeredPane();
			layeredPane.add(pan,100);
			JPanel paneMap = this.getWwjPanel();
			layeredPane.add(paneMap,10);
			
			this.setContentPane(layeredPane);
		
			//窗体大小改变
			this.addComponentListener(new ComponentAdapter(){
				@Override 
				public void componentResized(ComponentEvent e){
					//获取窗体大小
					int w = AppFrame.getFrames()[0].getWidth();
					int h = AppFrame.getFrames()[0].getHeight();
					//适应
					paneMap.setBounds(0, 0, w,h);
				}
			});
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("模拟程序", SenceViewMain.AppFrame.class);
    }
}
