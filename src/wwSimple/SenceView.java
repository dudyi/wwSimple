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

    //��ʼ������
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
        	super(true,false,false);
        	//�ȴ�״̬
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            //��һ���̴߳���
            Thread t = new Thread(new Runnable() {
                public void run() {
                	//Ӱ��
                	installImagery();
                	//����
                    installElevations();
                    //������ϣ��ָ�״̬
                    setCursor(Cursor.getDefaultCursor());
                }
            });
            t.start();
            
            //��ĻĬ�����
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
            //��Ļ��С
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = new Dimension(screenSize.width,screenSize.height);
			//��ͼ��ͼ��С
			((Component)getWwd()).setPreferredSize(frameSize);
			
			//���
            JPanel paneMap = new JPanel();
            paneMap.setBounds(0, 0, (int)frameSize.getWidth(),(int)frameSize.getHeight());
			//paneMap.setBounds(0, 0, this.getWidth(),this.getHeight());
			paneMap.add((Component)getWwd(),BorderLayout.CENTER);
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

			JLayeredPane layeredPane = new JLayeredPane();
			layeredPane.add(pan,100);
			layeredPane.add(paneMap,10);
			
			this.setContentPane(layeredPane);
			
			//�����¼�
			this.addWindowListener(new WindowListener(){
				@Override
				public void windowActivated(WindowEvent arg0) {
					// TODO �Զ����ɵķ������
					
				}

				@Override
				public void windowClosed(WindowEvent arg0) {
					// TODO �Զ����ɵķ������
					
				}

				@Override
				public void windowClosing(WindowEvent arg0) {
					// TODO �Զ����ɵķ������
					
				}

				@Override
				public void windowDeactivated(WindowEvent arg0) {
					// TODO �Զ����ɵķ������
					
				}

				@Override
				public void windowDeiconified(WindowEvent arg0) {
					// TODO �Զ����ɵķ������
					
				}

				@Override
				public void windowIconified(WindowEvent arg0) {
					// TODO �Զ����ɵķ������
					
				}

				@Override
				public void windowOpened(WindowEvent arg0) {
					// TODO �Զ����ɵķ������
					
				}
			});
			//����״̬�¼�
			this.addWindowStateListener(new WindowStateListener(){
				@Override
				public void windowStateChanged(WindowEvent e) {					
					//�ж�״̬
					if(e.getOldState() != e.getNewState()){
			            switch (e.getNewState()) {
				            case 6:
//				            	//��Ļ��С
//					            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//								Dimension frameSize = new Dimension(screenSize.width,screenSize.height);
//								//��ͼ��ͼ��С
//								((Component)getWwd()).setPreferredSize(frameSize);
//								paneMap.setBounds(0, 0, (int)frameSize.getWidth(),(int)frameSize.getHeight());
				                break;
				            case 1:
				            case 7:
				                // ��С�� 
				                break;
				            case 0:
				                // �ָ� 
				                break;
				            default:
				                break;
			            }
			        }
				}});
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
                    CompoundElevationModel model = (CompoundElevationModel) AppFrame.this.getWwd().getModel().getGlobe().getElevationModel();
                    model.addElevationModel(em);
                    //������
                    AVList params = (AVList) em.getValue(AVKey.CONSTRUCTION_PARAMETERS);
                    Sector sector = (Sector) params.getValue(AVKey.SECTOR);
                    ExampleUtil.goTo(getWwd(), sector);
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
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("ģ�����", SenceView.AppFrame.class);
    }
}
