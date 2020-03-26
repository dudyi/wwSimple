package wwSimple;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.dataimport.DataInstallUtil;
import gov.nasa.worldwindx.examples.dataimport.InstallElevations;
import gov.nasa.worldwindx.examples.dataimport.InstallElevations.AppFrame;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.w3c.dom.Document;

public class SenceView extends ApplicationTemplate{
    protected static final String BASE_CACHE_PATH = "Examples/";
    //protected static final String ELEVATIONS_PATH_IMAGERY = "./data/craterlake-imagery-30m.tif";
    //protected static final String ELEVATIONS_PATH_ELEVATIONS = "./data/craterlake-elev-16bit-30m.tif";
    protected static final String ELEVATIONS_PATH_IMAGERY = "./data/our/gg.tif";
    protected static final String ELEVATIONS_PATH_ELEVATIONS = "./data/our/DEM.tif";

    //初始化窗体
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
        	super(true,false,false);
        	//等待状态
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            //开一个线程处理
            Thread t = new Thread(new Runnable() {
                public void run() {
                	//影像
                	installImagery();
                	//地形
                    installElevations();
                    //加载完毕，恢复状态
                    setCursor(Cursor.getDefaultCursor());
                }
            });
            t.start();
            
            //屏幕默认最大化
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
            //屏幕大小
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = new Dimension(screenSize.width,screenSize.height);
			//地图视图大小
			((Component)getWwd()).setPreferredSize(frameSize);
			
			//面板
            JPanel paneMap = new JPanel();
            paneMap.setBounds(0, 0, (int)frameSize.getWidth(),(int)frameSize.getHeight());
			//paneMap.setBounds(0, 0, this.getWidth(),this.getHeight());
			paneMap.add((Component)getWwd(),BorderLayout.CENTER);
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
			
			//窗口事件
			this.addWindowListener(new WindowListener(){
				@Override
				public void windowActivated(WindowEvent arg0) {
					// TODO 自动生成的方法存根
					
				}

				@Override
				public void windowClosed(WindowEvent arg0) {
					// TODO 自动生成的方法存根
					
				}

				@Override
				public void windowClosing(WindowEvent arg0) {
					// TODO 自动生成的方法存根
					
				}

				@Override
				public void windowDeactivated(WindowEvent arg0) {
					// TODO 自动生成的方法存根
					
				}

				@Override
				public void windowDeiconified(WindowEvent arg0) {
					// TODO 自动生成的方法存根
					
				}

				@Override
				public void windowIconified(WindowEvent arg0) {
					// TODO 自动生成的方法存根
					
				}

				@Override
				public void windowOpened(WindowEvent arg0) {
					// TODO 自动生成的方法存根
					
				}
			});
			//窗口状态事件
			this.addWindowStateListener(new WindowStateListener(){
				@Override
				public void windowStateChanged(WindowEvent e) {					
					//判断状态
					if(e.getOldState() != e.getNewState()){
			            switch (e.getNewState()) {
				            case 6:
//				            	//屏幕大小
//					            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//								Dimension frameSize = new Dimension(screenSize.width,screenSize.height);
//								//地图视图大小
//								((Component)getWwd()).setPreferredSize(frameSize);
//								paneMap.setBounds(0, 0, (int)frameSize.getWidth(),(int)frameSize.getHeight());
				                break;
				            case 1:
				            case 7:
				                // 最小化 
				                break;
				            case 0:
				                // 恢复 
				                break;
				            default:
				                break;
			            }
			        }
				}});
        }

        /*****************************影像*****************************/
        protected void installImagery()
        {
            File sourceFile = ExampleUtil.saveResourceToTempFile(ELEVATIONS_PATH_IMAGERY, ".tif");
            FileStore fileStore = WorldWind.getDataFileStore();
            final Layer layer = installSurfaceImage("Crater Lake Imagery 30m", sourceFile, fileStore);
            if (layer == null) return;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    insertBeforePlacenames(AppFrame.this.getWwd(), layer);
                    AVList params = (AVList) layer.getValue(AVKey.CONSTRUCTION_PARAMETERS);
                    Sector sector = (Sector) params.getValue(AVKey.SECTOR);
                    ExampleUtil.goTo(getWwd(), sector);
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
        protected void installElevations()
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
                    CompoundElevationModel model = (CompoundElevationModel) AppFrame.this.getWwd().getModel().getGlobe().getElevationModel();
                    model.addElevationModel(em);
                    //缩放至
                    AVList params = (AVList) em.getValue(AVKey.CONSTRUCTION_PARAMETERS);
                    Sector sector = (Sector) params.getValue(AVKey.SECTOR);
                    ExampleUtil.goTo(getWwd(), sector);
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

    public static void main(String[] args)
    {
        ApplicationTemplate.start("模拟程序", SenceView.AppFrame.class);
    }
}
