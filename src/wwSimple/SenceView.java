package wwSimple;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.Wedge;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.dataimport.DataInstallUtil;
import gov.nasa.worldwindx.examples.dataimport.InstallElevations;
import gov.nasa.worldwindx.examples.dataimport.InstallElevations.AppFrame;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.w3c.dom.Document;

public class SenceView extends ApplicationTemplate{
    protected static final String BASE_CACHE_PATH = "Examples/";
    protected static final String ELEVATIONS_PATH_IMAGERY = "./data/craterlake-imagery-30m.tif";
    protected static final String ELEVATIONS_PATH_ELEVATIONS = "./data/craterlake-elev-16bit-30m.tif";

    //切片缓存地址
    protected static final String USER_TITLECACHE_PATH = "E:\\Work\\WordWind\\wwSimple\\src\\data\\TitleCacheData";
    
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
                	//installImagery();
                	//地形
                    //installElevations();
                    //加载完毕，恢复状态
                    setCursor(Cursor.getDefaultCursor());
                }
            });
            t.start();
            
            //体图层
            RenderableLayer layer = new RenderableLayer();
            int compassPosition = 0;
            LayerList layers = getWwd().getModel().getLayers();
            for (Layer l : layers)
            {
                if (l instanceof CompassLayer)
                    compassPosition = layers.indexOf(l);
            }
            layers.add(compassPosition, layer);
            //添加体
            AddWedge(layer);
            
            //缓存切片图层
            AddImageryCacheLayer();
            
            //新增WMS图层
            //AddWMSLayre();
            
            //图标图层
            AddIconLayer();
            
//            //屏幕默认最大化
//			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
//            //屏幕大小
//            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//			Dimension frameSize = new Dimension(screenSize.width,screenSize.height);
//			//地图视图大小
//			((Component)getWwd()).setPreferredSize(frameSize);
//			
//			//面板
//            JPanel paneMap = new JPanel();
//            paneMap.setBounds(0, 0, (int)frameSize.getWidth(),(int)frameSize.getHeight());
//			//paneMap.setBounds(0, 0, this.getWidth(),this.getHeight());
//			paneMap.add((Component)getWwd(),BorderLayout.CENTER);
//			//控件
//			JPanel pan = new JPanel();
//			pan.setBounds(20, 200, 200, 400);
//			JLabel label1 = new JLabel("参数1：");
//			JTextField textField1 = new JTextField(16);
//			JLabel label2 = new JLabel("参数2：");
//			JTextField textField2 = new JTextField(16);
//			JLabel label3 = new JLabel("参数3：");
//			JTextField textField3 = new JTextField(16);
//			JButton button = new JButton("确定");
//			pan.add(label1);
//			pan.add(textField1);
//			pan.add(label2);
//			pan.add(textField2);
//			pan.add(label3);
//			pan.add(textField3);
//			pan.add(button);
//
//			JLayeredPane layeredPane = new JLayeredPane();
//			layeredPane.add(pan,100);
//			layeredPane.add(paneMap,10);
//			
//			this.setContentPane(layeredPane);
			
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

        /*****************************体*****************************/
        protected void AddWedge(RenderableLayer layer){
        	//属性
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.YELLOW);
            attrs.setInteriorOpacity(0.7);
            attrs.setEnableLighting(true);
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(2d);
            attrs.setDrawInterior(true);
            attrs.setDrawOutline(false);
        	//几何体
            Wedge wedge4 = new Wedge(Position.fromDegrees(31.2, 101, 50000), Angle.POS90, 50000, 70000, 50000);
            wedge4.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            wedge4.setAttributes(attrs);
            wedge4.setVisible(true);
            wedge4.setValue(AVKey.DISPLAY_NAME, "Wedge with equal axes, RELATIVE_TO_GROUND altitude mode");
            layer.addRenderable(wedge4);
        }
        
        /*****************************切片缓存图层*****************************/
        protected void AddImageryCacheLayer(){
        	// 加载缓存数据
			LoadCacheData loadCacheData = new LoadCacheData((WorldWindowGLCanvas)getWwd(),USER_TITLECACHE_PATH);
			loadCacheData.loadPreviouslyInstalledData();
        }

        /*****************************WMS图层*****************************/
        protected void AddWMSLayre(){
        	try {
        		//请求地图的URL
                String uri = "http://localhost:8080/geoserver/mvtRoute/wms";
                WMSCapabilities caps;
                URI serverURI = new URI(uri);

                //获得WMSCapabilities对象
                caps = WMSCapabilities.retrieve(serverURI);
                //解析WMSCapabilities数据
                caps.parse();

                AVList params = new AVListImpl();

                //图层的名称
                params.setValue(AVKey.LAYER_NAMES, "gis_osm_roads_free_1");
                //地图服务的协议，这里是OGC:WMS
                params.setValue(AVKey.SERVICE_NAME, "OGC:WMS");
                //获得地图的uri，也就是上面定义的uri
                params.setValue(AVKey.GET_MAP_URL, uri);
                //在本地缓存文件的名称
                params.setValue(AVKey.DATA_CACHE_NAME, "geoserver wms");
                params.setValue(AVKey.TILE_URL_BUILDER, new WMSTiledImageLayer.URLBuilder(params));
            	
            	Object component = createComponent(caps,params);
            	
            	Layer layer = (Layer) component;
                LayerList layers = getWwd().getModel().getLayers();

                layer.setEnabled(true);

                if (true)
                {
                    if (!layers.contains(layer))
                    {
                        ApplicationTemplate.insertBeforePlacenames(getWwd(), layer);
                        this.firePropertyChange("LayersPanelUpdated", null, layer);
                    }
                }
 
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }   
        }
        
        protected static Object createComponent(WMSCapabilities caps, AVList params)
        {
            AVList configParams = params.copy(); // Copy to insulate changes from the caller.

            // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
            configParams.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
            configParams.setValue(AVKey.URL_READ_TIMEOUT, 30000);
            configParams.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

            try
            {
                String factoryKey = getFactoryKeyForCapabilities(caps);
                Factory factory = (Factory) WorldWind.createConfigurationComponent(factoryKey);
                return factory.createFromConfigSource(caps, configParams);
            }
            catch (Exception e)
            {
                // Ignore the exception, and just return null.
            }

            return null;
        }
        
        protected static String getFactoryKeyForCapabilities(WMSCapabilities caps)
        {
            boolean hasApplicationBilFormat = false;

            Set<String> formats = caps.getImageFormats();
            for (String s : formats)
            {
                if (s.contains("application/bil"))
                {
                    hasApplicationBilFormat = true;
                    break;
                }
            }

            return hasApplicationBilFormat ? AVKey.ELEVATION_MODEL_FACTORY : AVKey.LAYER_FACTORY;
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
    
        /*****************************IconLayer*****************************/
        private UserFacingIcon icon;
        protected void AddIconLayer(){
        	IconLayer layer = new IconLayer();
        	icon = new UserFacingIcon("./data/pic/alarm.png", new Position(Angle.fromDegrees(30.19), Angle.fromDegrees(104.05), 100));
            icon.setSize(new Dimension(16, 16));
            layer.addIcon(icon);
            ApplicationTemplate.insertAfterPlacenames(this.getWwd(), layer);
        	
            BufferedImage circleRed = createBitmap(PatternFactory.PATTERN_CIRCLE, Color.RED);
            //动画
            (new PulsingAlarmAction("Pulsing Red Circle", circleRed, 100)).actionPerformed(null);

        }
        
        private BufferedImage createBitmap(String pattern, Color color)
        {
            // Create bitmap with pattern
            BufferedImage image = PatternFactory.createPattern(pattern, new Dimension(128, 128), 0.7f,
                color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
            // Blur a lot to get a fuzzy edge
            image = PatternFactory.blur(image, 13);
            image = PatternFactory.blur(image, 13);
            image = PatternFactory.blur(image, 13);
            image = PatternFactory.blur(image, 13);
            return image;
        }
        
        private class PulsingAlarmAction extends AbstractAction
        {
            protected final Object bgIconPath;
            protected int frequency;
            protected int scaleIndex = 0;
            protected double[] scales = new double[] {1.25, 1.5, 1.75, 2, 2.25, 2.5, 2.75, 3, 3.25, 3.5, 3.25, 3,
                2.75, 2.5, 2.25, 2, 1.75, 1.5};
            protected Timer timer;

            private PulsingAlarmAction(String name, Object bgp, int frequency)
            {
                super(name);
                this.bgIconPath = bgp;
                this.frequency = frequency;
            }

            private PulsingAlarmAction(String name, Object bgp, int frequency, double[] scales)
            {
                this(name, bgp, frequency);
                this.scales = scales;
            }

            public void actionPerformed(ActionEvent e)
            {
                if (timer == null)
                {
                    timer = new Timer(frequency, new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            icon.setBackgroundScale(scales[++scaleIndex % scales.length]);
                            getWwd().redraw();
                        }
                    });
                }
                icon.setBackgroundImage(bgIconPath);
                scaleIndex = 0;
                timer.start();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("模拟程序", SenceView.AppFrame.class);
    }
}
