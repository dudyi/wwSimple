package com.mls.formapp.wordwind;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.w3c.dom.Document;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.data.TiledImageProducer;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.Wedge;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.dataimport.DataInstallUtil;
import gov.nasa.worldwindx.examples.util.DirectedPath;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

public class SenceLayerOperation {

	protected static final String BASE_CACHE_PATH = "Examples/";
    protected static final String ELEVATIONS_PATH_IMAGERY = "com/mls/formapp/data/craterlake-imagery-30m.tif";
    protected static final String ELEVATIONS_PATH_ELEVATIONS = "com/mls/formapp/data/craterlake-elev-16bit-30m.tif";

    //����ͼ��
    RenderableLayer entityLayer=null;
    MarkerLayer markerLayer = null;
    
    //��Ƭ�����ַ
    protected static final String USER_TITLECACHE_PATH = "E:\\Work\\WordWind\\TitleCacheData";
	
    private static WorldWindowGLCanvas worldWindowGLCanvas;
	
	public SenceLayerOperation(WorldWindowGLCanvas worldWindowGLCanvas) 
	{
		SenceLayerOperation.setWorldWindowGLCanvas(worldWindowGLCanvas);
	}
	
	/*****************************��ͼ��*****************************/
    protected void AddWedgeLayer() {
        RenderableLayer layer = new RenderableLayer();
        int compassPosition = 0;
        LayerList layers = getWorldWindowGLCanvas().getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof CompassLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
        //�����
        AddWedge(layer);
    }
    
    protected void AddWedge(RenderableLayer layer){
    	//����
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setInteriorMaterial(Material.YELLOW);
        attrs.setInteriorOpacity(0.7);
        attrs.setEnableLighting(true);
        attrs.setOutlineMaterial(Material.RED);
        attrs.setOutlineWidth(2d);
        attrs.setDrawInterior(true);
        attrs.setDrawOutline(false);
    	//������
        Wedge wedge4 = new Wedge(Position.fromDegrees(31.2, 101, 50000), Angle.POS90, 50000, 70000, 50000);
        wedge4.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        wedge4.setAttributes(attrs);
        wedge4.setVisible(true);
        wedge4.setValue(AVKey.DISPLAY_NAME, "Wedge with equal axes, RELATIVE_TO_GROUND altitude mode");
        layer.addRenderable(wedge4);
    }
    
    /*****************************��Ƭ����ͼ��*****************************/
    protected void AddImageryCacheLayer(){
    	// ���ػ�������
		LoadCacheData loadCacheData = new LoadCacheData(getWorldWindowGLCanvas(),USER_TITLECACHE_PATH);
		loadCacheData.loadPreviouslyInstalledData();
		
		//������(32.44829108,118.08144093)(32.46786118,118.12199625)
        Sector sector = Sector.fromDegrees(32.44829108, 32.46786118, 118.08144093, 118.12199625);
        ExampleUtil.goTo(getWorldWindowGLCanvas(), sector);
        
    }

    /*****************************WMSͼ��*****************************/
    protected void AddWMSLayre(){
    	try {
    		//�����ͼ��URL
            String uri = "http://localhost:8080/geoserver/mvtRoute/wms";
            WMSCapabilities caps;
            URI serverURI = new URI(uri);

            //���WMSCapabilities����
            caps = WMSCapabilities.retrieve(serverURI);
            //����WMSCapabilities����
            caps.parse();

            AVList params = new AVListImpl();

            //ͼ�������
            params.setValue(AVKey.LAYER_NAMES, "gis_osm_roads_free_1");
            //��ͼ�����Э�飬������OGC:WMS
            params.setValue(AVKey.SERVICE_NAME, "OGC:WMS");
            //��õ�ͼ��uri��Ҳ�������涨���uri
            params.setValue(AVKey.GET_MAP_URL, uri);
            //�ڱ��ػ����ļ�������
            params.setValue(AVKey.DATA_CACHE_NAME, "geoserver wms");
            params.setValue(AVKey.TILE_URL_BUILDER, new WMSTiledImageLayer.URLBuilder(params));
        	
        	Object component = createComponent(caps,params);
        	
        	Layer layer = (Layer) component;
            LayerList layers = getWorldWindowGLCanvas().getModel().getLayers();

            layer.setEnabled(true);

            if (true)
            {
                if (!layers.contains(layer))
                {
                    ApplicationTemplate.insertBeforePlacenames(getWorldWindowGLCanvas(), layer);
                    getWorldWindowGLCanvas().firePropertyChange("LayersPanelUpdated", null, layer);//(Component)
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
    
    /*****************************Ӱ��*****************************/
    protected void installImagery()
    {
        File sourceFile = ExampleUtil.saveResourceToTempFile(ELEVATIONS_PATH_IMAGERY, ".tif");
        FileStore fileStore = WorldWind.getDataFileStore();
        final Layer layer = installSurfaceImage("Crater Lake Imagery 30m", sourceFile, fileStore);
        if (layer == null) return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	ApplicationTemplate.insertBeforePlacenames(getWorldWindowGLCanvas(), layer);
                AVList params = (AVList) layer.getValue(AVKey.CONSTRUCTION_PARAMETERS);
                Sector sector = (Sector) params.getValue(AVKey.SECTOR);
                ExampleUtil.goTo(getWorldWindowGLCanvas(), sector);
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
   
    /*****************************����*****************************/
    protected void installElevations()
    {
        //��װ��Դ
        File sourceFile = ExampleUtil.saveResourceToTempFile(ELEVATIONS_PATH_ELEVATIONS, ".tif");
        //��ȡ���βο��ռ�
        FileStore fileStore = WorldWind.getDataFileStore();
        //����ʵ�幹��
        final ElevationModel em = installElevations("Elevations", sourceFile, fileStore);
        if (em == null) return;
        //��ӵ���
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CompoundElevationModel model = (CompoundElevationModel) getWorldWindowGLCanvas().getModel().getGlobe().getElevationModel();
                model.addElevationModel(em);
                //������
                AVList params = (AVList) em.getValue(AVKey.CONSTRUCTION_PARAMETERS);
                Sector sector = (Sector) params.getValue(AVKey.SECTOR);
                ExampleUtil.goTo(getWorldWindowGLCanvas(), sector);
            }
        });
    }

    protected ElevationModel installElevations(String displayName, Object elevationSource, FileStore fileStore)
    {
        File fileStoreLocation = DataInstallUtil.getDefaultInstallLocation(fileStore);
        //��������
        String cacheName = BASE_CACHE_PATH + WWIO.replaceIllegalFileNameCharacters(displayName);
        //��������
        AVList params = new AVListImpl();
        params.setValue(AVKey.FILE_STORE_LOCATION, fileStoreLocation.getAbsolutePath());
        params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
        params.setValue(AVKey.DATASET_NAME, displayName);
        // ����������Ƭ
        TiledElevationProducer producer = new TiledElevationProducer();
        try {
            //��������Դ
            producer.setStoreParameters(params);
            producer.offerDataSource(elevationSource, null);
            //���ɲ���
            producer.startProduction();
        }
        catch (Exception e) {
            producer.removeProductionState();
            e.printStackTrace();
            return null;
        }

        //��ȡ�����
        Iterable<?> results = producer.getProductionResults();
        if (results == null || results.iterator() == null || !results.iterator().hasNext()) return null;

        Object o = results.iterator().next();
        if (o == null || !(o instanceof Document)) return null;

        //ElevationModel����
        return (ElevationModel) BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY,
            ((Document) o).getDocumentElement());
    }

    /*****************************IconLayer*****************************/
    private UserFacingIcon icon;
    protected void AddIconLayer(){
    	IconLayer layer = new IconLayer();
    	icon = new UserFacingIcon("com/mls/formapp/images/alarm.png", new Position(Angle.fromDegrees(30.19), Angle.fromDegrees(104.05), 100));
        icon.setSize(new Dimension(16, 16));
        layer.addIcon(icon);
        
        ApplicationTemplate.insertAfterPlacenames(getWorldWindowGLCanvas(), layer);
    	
        BufferedImage circleRed = createBitmap(PatternFactory.PATTERN_CIRCLE, Color.RED);
        //����
        (new PulsingAlarmAction("���е�", circleRed, 100)).actionPerformed(null);

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
		private static final long serialVersionUID = 1L;
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
                        getWorldWindowGLCanvas().redraw();
                    }
                });
            }
            icon.setBackgroundImage(bgIconPath);
            scaleIndex = 0;
            timer.start();
        }
    }
	
    /*****************************������ͼ��*****************************/
    protected void RemoveEntityLayer() {
    	if(entityLayer != null) 
    		entityLayer.removeAllRenderables();
    }
    
    protected void AddEntityLayer() 
    {
    	if(entityLayer == null) entityLayer = new RenderableLayer();
    	else entityLayer.removeAllRenderables();
    	//Polyline
    	ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(Material.RED);
        attrs.setOutlineWidth(3);
        //attrs.setOutlineStippleFactor(2);

        Iterable<LatLon> locations = Arrays.asList(
            LatLon.fromDegrees(31, 118),
            LatLon.fromDegrees(32, 112),
            LatLon.fromDegrees(36, 115));
        SurfaceShape shape = new SurfacePolyline(locations);
        shape.setAttributes(attrs);
        ((SurfacePolyline) shape).setClosed(false);
        entityLayer.addRenderable(shape);
        
        //��ǩ(����?)
        GlobeAnnotation ga = new GlobeAnnotation("�������test", Position.fromDegrees(32, 110.9, 300));
        ga.setAlwaysOnTop(true);
        ga.getAttributes().setFont(Font.decode("UTF-8"));
        entityLayer.addRenderable(ga);
        
        //�߶���
        ShapeAttributes attrsPath = new BasicShapeAttributes();
        attrsPath.setOutlineMaterial(Material.YELLOW);
        attrsPath.setOutlineWidth(2d);
        
        ArrayList<Position> pathPositions = new ArrayList<Position>();
        for(int i=0;i<100000;i++) {
        	if(i<=50000) pathPositions.add(Position.fromDegrees(32.45, 118.09+i*0.0000001, i/100));
        	else pathPositions.add(Position.fromDegrees(32.45, 118.09+i*0.0000001, (100000-i)/100));
        }
         
        Path path = new Path(pathPositions);
        path.setAttributes(attrsPath);
        entityLayer.addRenderable(path);
               
        ApplicationTemplate.insertBeforeCompass(getWorldWindowGLCanvas(), entityLayer); 
    }
    
    //��̬�켣
    protected void HistoryTrack() {
    	ArrayList<Position> pathPositions = new ArrayList<Position>();
        for(int i=0;i<100000;i++) {
        	if(i<=50000) pathPositions.add(Position.fromDegrees(32.45, 118.09+i*0.0000001, i/100));
        	else pathPositions.add(Position.fromDegrees(32.45, 118.09+i*0.0000001, (100000-i)/100));
        }
        
        //�����
        MarkerLayer mLayer = new MarkerLayer();
        ArrayList<Marker> markers = new ArrayList<Marker>();
        BasicMarkerAttributes attrs = new BasicMarkerAttributes(Material.RED, BasicMarkerShape.SPHERE, 0.5);
        BasicMarker marker= new BasicMarker(pathPositions.get(50000), attrs);
        markers.add(marker);
        mLayer.setMarkers(markers);
        mLayer.setOverrideMarkerElevation(false);
        mLayer.setElevation(0);
        mLayer.setEnablePickSizeReturn(true);

        ApplicationTemplate.insertBeforeCompass(getWorldWindowGLCanvas(), mLayer);
        
        //���߳�
        RunnerHistoryTrack r1 = new RunnerHistoryTrack(marker,pathPositions,getWorldWindowGLCanvas());		
		Thread t = new Thread(r1);		
		t.start();
    }
    
    protected void RemoveMarkersLayer() {
    	if(markerLayer != null) 
    		markerLayer.setMarkers(null);
    }
    
    protected void AddMarkersLayer() {
    	if(markerLayer == null) markerLayer = new MarkerLayer();
    	else markerLayer.setMarkers(null);
    	//�켣��
        List<Position> positions =  Arrays.asList(
        		Position.fromDegrees(30, 120.9, 1000),
        		Position.fromDegrees(30, 121.9, 2000),
        		Position.fromDegrees(30, 122.9, 30000),
        		Position.fromDegrees(30, 123.9, 40000),
        		Position.fromDegrees(30, 124.9, 50000),
        		Position.fromDegrees(30, 125.9, 60000),
        		Position.fromDegrees(30, 126.9, 50000),
        		Position.fromDegrees(30, 127.9, 40000),
        		Position.fromDegrees(30, 128.9, 3000),
        		Position.fromDegrees(30, 129.9, 2000),
        		Position.fromDegrees(30, 130.9, 1000));        
        BasicMarkerAttributes attrs = new BasicMarkerAttributes(Material.RED, BasicMarkerShape.SPHERE, 0.5);
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for(Position p:positions) 
        	markers.add(new BasicMarker(p, attrs));

        //markerLayer = new MarkerLayer(markers);
        markerLayer.setMarkers(markers);
        markerLayer.setOverrideMarkerElevation(false);
        markerLayer.setElevation(0);
        markerLayer.setEnablePickSizeReturn(true);

        ApplicationTemplate.insertBeforeCompass(getWorldWindowGLCanvas(), markerLayer);

    }
    
	//���û����ؼ�����
	public static void setWorldWindowGLCanvas(WorldWindowGLCanvas worldWindowGLCanvas)
	{
		SenceLayerOperation.worldWindowGLCanvas = worldWindowGLCanvas;
	}
		
	//��ȡ�����ؼ�����
	public static WorldWindowGLCanvas getWorldWindowGLCanvas()
	{
		return worldWindowGLCanvas;
	}
	 
}

class RunnerHistoryTrack implements Runnable{
	private BasicMarker marker;
	private ArrayList<Position> pathPositions;
	private WorldWindowGLCanvas canvas;
	
	public RunnerHistoryTrack(BasicMarker marker,ArrayList<Position> pathPositions,WorldWindowGLCanvas canvas) {
		this.marker = marker;
		this.pathPositions = pathPositions;
		this.canvas = canvas;
	}
	@Override
	public void run() {		
        try {
        	for(int i=0;i<pathPositions.size();i++) {
        		marker.setPosition(pathPositions.get(i));
            	Thread.sleep(100);
            	canvas.redraw();
            	System.out.println(marker.getPosition());
            }
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}	
}
