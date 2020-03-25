package wwSimple;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
 
import javax.swing.JFrame;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

public class demo {
public static class WorldWindFrame extends JFrame {
		
		private static final long serialVersionUID = -130932605398355602L;
		
		private WorldWindowGLCanvas windowCanvas;
 
		public WorldWindFrame() {
			windowCanvas = new WorldWindowGLCanvas();
			Dimension frameSize = new Dimension(600,600);
			windowCanvas.setPreferredSize(frameSize);
			this.setTitle("WorldWindDemo");
			this.getContentPane().add(windowCanvas,BorderLayout.CENTER);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation((screenSize.width - frameSize.width ) / 2, (screenSize.height - frameSize.height) / 2);
			this.pack();
			windowCanvas.setModel(new BasicModel());
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
