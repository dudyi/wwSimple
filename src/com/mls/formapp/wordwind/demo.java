package com.mls.formapp.wordwind;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;


import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.w3c.dom.Document;
import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwindx.examples.dataimport.DataInstallUtil;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

public class demo {
	public static class WorldWindFrame extends JFrame {
		protected static final String BASE_CACHE_PATH = "Examples/";
		protected static final String ELEVATIONS_PATH_IMAGERY = "com/mls/formapp/data/craterlake-imagery-30m.tif";
	    protected static final String ELEVATIONS_PATH_ELEVATIONS = "com/mls/formapp/data/craterlake-elev-16bit-30m.tif";
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
			
			installElevations(windowCanvas);
			
			//窗口放大事件
//			this.addWindowListener(new WindowListener() {
//				public void windowOpened(WindowEvent e) {  
//	                System.out.println("window opened");  
//	            }
//			});
		}
		
		
		/*****************************影像*****************************/
        protected void installImagery(WorldWindowGLCanvas windowCanvas)
        {
            File sourceFile = ExampleUtil.saveResourceToTempFile(ELEVATIONS_PATH_IMAGERY, ".tif");
            FileStore fileStore = WorldWind.getDataFileStore();
            final Layer layer = installSurfaceImage("Crater Lake Imagery 30m", sourceFile, fileStore);
            if (layer == null) return;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //insertBeforePlacenames(AppFrame.this.getWwd(), layer);
                	
                	int compassPosition = 0;
                    LayerList layers = windowCanvas.getModel().getLayers();
                    for (Layer l : layers)
                    {
                        if (l instanceof PlaceNameLayer)
                            compassPosition = layers.indexOf(l);
                    }
                    layers.add(compassPosition, layer);
                	
                    AVList params = (AVList) layer.getValue(AVKey.CONSTRUCTION_PARAMETERS);
                    Sector sector = (Sector) params.getValue(AVKey.SECTOR);
                    ExampleUtil.goTo(windowCanvas, sector);
                }
            });
        }

        protected Layer installSurfaceImage(String displayName, Object imageSource, FileStore fileStore)
        {
            File fileStoreLocation = DataInstallUtil.getDefaultInstallLocation(fileStore);
            String cacheName = BASE_CACHE_PATH + WWIO.replaceIllegalFileNameCharacters(displayName);

            AVList params = new AVListImpl();
            params.setValue(AVKey.FILE_STORE_LOCATION, fileStoreLocation.getAbsolutePath());
            params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
            params.setValue(AVKey.DATASET_NAME, displayName);

            TiledImageProducer producer = new TiledImageProducer();
            try {
                producer.setStoreParameters(params);
                producer.offerDataSource(imageSource, null);

                producer.startProduction();
            }
            catch (Exception e)
            {
                producer.removeProductionState(); 
                e.printStackTrace();
                return null;
            }

            Iterable<?> results = producer.getProductionResults();
            if (results == null || results.iterator() == null || !results.iterator().hasNext()) return null;

            Object o = results.iterator().next();
            if (o == null || !(o instanceof Document)) return null;

            Layer layer = (Layer) BasicFactory.create(AVKey.LAYER_FACTORY, ((Document) o).getDocumentElement());
            layer.setEnabled(true);

            return layer;
        }
	
        
        /*****************************地形*****************************/
        protected void installElevations(WorldWindowGLCanvas windowCanvas)
        {
            //组装资源
            File sourceFile = ExampleUtil.saveResourceToTempFile(ELEVATIONS_PATH_ELEVATIONS, ".tif");
            //获取地形参考空间
            FileStore fileStore = WorldWind.getDataFileStore();
            //地形实体构建
            final ElevationModel em = installElevations("Elevations", sourceFile, fileStore);
            if (em == null) return;
            //添加地形
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    CompoundElevationModel model = (CompoundElevationModel) windowCanvas.getModel().getGlobe().getElevationModel();
                    model.addElevationModel(em);
                    //缩放至
                    AVList params = (AVList) em.getValue(AVKey.CONSTRUCTION_PARAMETERS);
                    Sector sector = (Sector) params.getValue(AVKey.SECTOR);
                    ExampleUtil.goTo(windowCanvas, sector);
                }
            });
        }

        protected ElevationModel installElevations(String displayName, Object elevationSource, FileStore fileStore)
        {
            File fileStoreLocation = DataInstallUtil.getDefaultInstallLocation(fileStore);
            //创建缓存
            String cacheName = BASE_CACHE_PATH + WWIO.replaceIllegalFileNameCharacters(displayName);
            //创建属性
            AVList params = new AVListImpl();
            params.setValue(AVKey.FILE_STORE_LOCATION, fileStoreLocation.getAbsolutePath());
            params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
            params.setValue(AVKey.DATASET_NAME, displayName);
            // 创建地形切片
            TiledElevationProducer producer = new TiledElevationProducer();
            try {
                //配置数据源
                producer.setStoreParameters(params);
                producer.offerDataSource(elevationSource, null);
                //生成操作
                producer.startProduction();
            }
            catch (Exception e) {
                producer.removeProductionState();
                e.printStackTrace();
                return null;
            }

            //获取结果集
            Iterable<?> results = producer.getProductionResults();
            if (results == null || results.iterator() == null || !results.iterator().hasNext()) return null;

            Object o = results.iterator().next();
            if (o == null || !(o instanceof Document)) return null;

            //ElevationModel对象
            return (ElevationModel) BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY,
                ((Document) o).getDocumentElement());
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
