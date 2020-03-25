package wwSimple;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;


import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

public class demo {
	public static class WorldWindFrame extends JFrame {
		
		private static final long serialVersionUID = -130932605398355602L;
		
		private WorldWindowGLCanvas windowCanvas;
 
		public WorldWindFrame() {	
			this.setTitle("模拟程序");
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = new Dimension(screenSize.width,screenSize.height);
			
			this.setSize(frameSize.width/2,frameSize.height/2);
			this.setLocation(frameSize.width/4,frameSize.height/4);
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
			
			windowCanvas = new WorldWindowGLCanvas();
			windowCanvas.setPreferredSize(frameSize);
			
			JPanel paneMap = new JPanel();
			paneMap.setBounds(0, 0, frameSize.width,frameSize.height);
			paneMap.add(windowCanvas,BorderLayout.CENTER);

			windowCanvas.setModel(new BasicModel());
			
			
//			this.setTitle("模拟程序");			
//			windowCanvas = new WorldWindowGLCanvas();
//			Dimension frameSize = new Dimension(800,600);
//			windowCanvas.setPreferredSize(frameSize);
//			this.getContentPane().add(windowCanvas,BorderLayout.CENTER);
//			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//			this.setLocation((screenSize.width - frameSize.width ) / 2, (screenSize.height - frameSize.height) / 2);
//			this.pack();
//			windowCanvas.setModel(new BasicModel());
			
			
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

			JLayeredPane layeredPane = new JLayeredPane();
			layeredPane.add(pan,100);
			layeredPane.add(paneMap,10);
			this.setContentPane(layeredPane);
			
			//窗口放大事件
//			this.addWindowListener(new WindowListener() {
//				public void windowOpened(WindowEvent e) {  
//	                System.out.println("window opened");  
//	            }
//			});
		}
	}
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {			
			@Override
			public void run() {
				new WorldWindFrame().setVisible(true);				
			}
		});
	}
}
