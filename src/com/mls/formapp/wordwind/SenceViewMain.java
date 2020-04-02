package com.mls.formapp.wordwind;

import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

public class SenceViewMain extends ApplicationTemplate{    
    //��ʼ������
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
		private static final long serialVersionUID = -130932605398355602L;

		public AppFrame()
        {
        	/********************************��ά��ͼ********************************/
        	//��ά��ͼ��ʼ��
        	super(true,false,false);        	
	    	//��ͼ��Դ����
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            Thread t = new Thread(new Runnable() {
                public void run() {
                	//��ʼ������ͼ
                	InitMainView(getWwjPanel());
                	
                	/**************ͼ�����**************/
                	SenceLayerOperation sence = new SenceLayerOperation((WorldWindowGLCanvas)getWwd());
        	        //��ͼ��
        	    	sence.AddWedgeLayer();
        	        //������Ƭͼ��
        	    	sence.AddImageryCacheLayer();
        	        //����WMSͼ��
        	    	//sence.AddWMSLayre();	        
        	        //ͼ��ͼ��
        	    	sence.AddIconLayer();
        	    	//��ͼ��
        	    	sence.AddEntityLayer();
                	//�ļ�Ӱ��
                	//sence.installImagery();
                	//�ļ�����
                	//sence.installElevations();
                	
                    //������ϣ��ָ�״̬
                    setCursor(Cursor.getDefaultCursor());
                }
            });
            t.start();			
        }
        
        /********************************���������ͼ********************************/	
        public void InitMainView(JPanel paneMap) {
        	//�˵����
        	JPanel panMenu = CreateMenuPanel();
			//�ֲ㸡��
			JLayeredPane layeredPane = new JLayeredPane();
			layeredPane.add(panMenu,100);
			layeredPane.add(paneMap,10);
			
			this.setContentPane(layeredPane);
		
			//�����С�ı�
			this.addComponentListener(new ComponentAdapter(){
				@Override 
				public void componentResized(ComponentEvent e){
					//����
					Frame fram = AppFrame.getFrames()[0];
					//��Ӧ
					paneMap.setBounds(0, 0, fram.getWidth(),fram.getHeight()-48);
					paneMap.getCursor();
					//�ػ棬��Ȼ��bug
					fram.validate();
					fram.repaint();
				}
			});
        }
        
        //�����˵����
        protected JPanel CreateMenuPanel() 
        {
        	//�ؼ�
			JPanel pan = new JPanel();
			pan.setBounds(20, 200, 200, 400);
			JLabel label1 = new JLabel("����1��");
			JTextField textField1 = new JTextField(16);
			JLabel label2 = new JLabel("����2��");
			JTextField textField2 = new JTextField(16);
			JLabel label3 = new JLabel("����3��");
			JTextField textField3 = new JTextField(16);
			JButton button = new JButton("ȷ��");
			pan.add(label1);
			pan.add(textField1);
			pan.add(label2);
			pan.add(textField2);
			pan.add(label3);
			pan.add(textField3);
			pan.add(button);
			
			return pan;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("ģ�����", SenceViewMain.AppFrame.class);
    }
}
