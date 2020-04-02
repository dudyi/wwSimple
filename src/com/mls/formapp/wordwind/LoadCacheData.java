package com.mls.formapp.wordwind;

import java.io.File;

import javax.swing.SwingUtilities;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.DataConfigurationFilter;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

public class LoadCacheData {
	private static WorldWindowGLCanvas worldWindowGLCanvas;
	private static String tileCachePath;
 
	//���캯��
	public LoadCacheData(WorldWindowGLCanvas worWindowGLCanvas,String tileCachePath)
	{
		LoadCacheData.setWorldWindowGLCanvas(worWindowGLCanvas);
		LoadCacheData.setTileCachePath(tileCachePath);
	}
	
	//�̶߳�ȡ���û���վ����Ϣ
	public void loadPreviouslyInstalledData()
	{ 
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				//��������վ��
				File file = new File(tileCachePath);
				if(file.getParentFile().exists()) 
					WorldWind.getDataFileStore().addLocation(tileCachePath, true);

				//����Ĭ�ϻ���վ��
				loadInstalledDataFromFileStore(WorldWind.getDataFileStore());
			}
		});
		thread.start();
	}
 
	//��������վ��
	protected void loadInstalledDataFromFileStore(FileStore fileStore)
	{
		// �������еĻ����ļ�
		for (File file : fileStore.getLocations())
		{
			// �ļ����ڲ����ǻ����ļ�Ŀ¼
			if (file.exists() && fileStore.isInstallLocation(file.getPath()))
			{
				System.out.println(file.getPath());
				loadInstalledDataFromDirectory(file);
			}
 
		}
	}
 
	//���ļ�Ŀ¼���ػ�������
	private void loadInstalledDataFromDirectory(File dir)
	{
		String[] names = WWIO.listDescendantFilenames(dir,
				new DataConfigurationFilter(), false);
		if (names == null || names.length == 0)
			return;
 
		for (String filename : names)
		{
			Document doc = null;
 
			try
			{
				// ���ݻ����ļ�XML�����ļ�����Document����
				File dataConfigFile = new File(dir, filename);
				doc = WWXML.openDocument(dataConfigFile);
				doc = DataConfigurationUtils.convertToStandardDataConfigDocument(doc);
			}
			catch (WWRuntimeException e)
			{
				e.printStackTrace();
			}
 
			if (doc == null)
				continue;
 
			// �������������ļ����������е��ļ�����˲��ܱ�֤�����ɵ�ǰ�汾WW's Installer
			// �����ġ���������֮ǰ�汾������Ӧ�ó�������ģ����ҪΪ����ȱʧ�Ĳ������ñ���ֵ����Щ������Ҫ��������ͼ���߳�ģ�⣩
			AVList params = new AVListImpl();
			setFallbackParams(doc, filename, params);
			// �������
			addInstalledData(doc, params);
		}
	}
 
	//���ñ��ò���ֵ
	private void setFallbackParams(Document dataConfig, String filename, AVList params)
	{
		XPath xpath = WWXML.makeXPath();
		Element domElement = dataConfig.getDocumentElement();
 
		// If the data configuration document doesn't define a cache name, then
		// compute one using the file's path
		// relative to its file cache directory.
		String s = WWXML.getText(domElement, "DataCacheName", xpath);
		if (s == null || s.length() == 0)
			DataConfigurationUtils.getDataConfigCacheName(filename, params);
 
		// If the data configuration document doesn't define the data's extreme
		// elevations, provide default values using
		// the minimum and maximum elevations of Earth.
		String type = DataConfigurationUtils.getDataConfigType(domElement);
		if (type.equalsIgnoreCase("ElevationModel"))
		{
			if (WWXML.getDouble(domElement, "ExtremeElevations/@min", xpath) == null)
				params.setValue(AVKey.ELEVATION_MIN, Earth.ELEVATION_MIN);
			if (WWXML.getDouble(domElement, "ExtremeElevations/@max", xpath) == null)
				params.setValue(AVKey.ELEVATION_MAX, Earth.ELEVATION_MAX);
		}
	}
 
	//��ӻ�������
	private void addInstalledData(final Document dataConfig, final AVList params)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					addInstalledData(dataConfig, params);
				}
			});
		}
		else
		{
			addInstalledCacheData(dataConfig.getDocumentElement(), params);
		}
 
	}
 
	//������л�������
	public void addInstalledCacheData(final Element domElement,
			final AVList params)
	{
		if (domElement == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
 
		String description = getDescription(domElement); // ͼ������
		Sector sector = getSector(domElement); // ͼ�㷶Χ
		System.out.println(description);
		System.out.println(sector);
		addToWorldWindow(domElement, params);
	}
 
	//�������ļ�����WW
	private void addToWorldWindow(Element domElement, AVList params)
	{
		String type = DataConfigurationUtils.getDataConfigType(domElement);
		if (type == null)
			return;
 
		if (type.equalsIgnoreCase("Layer"))
		{
			addLayerToWorldWindow(domElement, params);
		}
		else if (type.equalsIgnoreCase("ElevationModel"))
		{
			addElevationModelToWorldWindow(domElement, params);
		}
	}
 
	//��WW�����ͼ��
	private void addLayerToWorldWindow(Element domElement, AVList params)
	{
		Layer layer = null;
		try
		{
			// Factory������ͼ��Ĭ���ǲ��ɼ���
			Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
			layer = (Layer) factory.createFromConfigSource(domElement, params);
		}
		catch (Exception e)
		{
			String message = Logging.getMessage(
					"generic.CreationFromConfigurationFailed",
					DataConfigurationUtils.getDataConfigDisplayName(domElement));
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
		}
 
		if (layer == null)
			return;
		layer.setEnabled(true); // ����ͼ��ɼ�
 
		// �����WW
		if (!getWorldWindowGLCanvas().getModel().getLayers().contains(layer))
		{
			getWorldWindowGLCanvas().getModel().getLayers().add(layer); 
		}
	}
 
	//��Ӹ߳�ͼ��
	private void addElevationModelToWorldWindow(Element domElement,
			AVList params)
	{
		ElevationModel em = null;
		try
		{
			Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
			em = (ElevationModel) factory.createFromConfigSource(domElement,
					params);
		}
		catch (Exception e)
		{
			String message = Logging.getMessage(
					"generic.CreationFromConfigurationFailed",
					DataConfigurationUtils.getDataConfigDisplayName(domElement));
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
		}
 
		if (em == null)
			return;
 
		ElevationModel defaultElevationModel = getWorldWindowGLCanvas().getModel().getGlobe().getElevationModel();
		if (defaultElevationModel instanceof CompoundElevationModel)
		{
			if (!((CompoundElevationModel) defaultElevationModel).containsElevationModel(em))
				((CompoundElevationModel) defaultElevationModel).addElevationModel(em);
		}
		else
		{
			CompoundElevationModel cm = new CompoundElevationModel();
			cm.addElevationModel(defaultElevationModel);
			cm.addElevationModel(em);
			getWorldWindowGLCanvas().getModel().getGlobe().setElevationModel(cm);
		}
	}
 
	//��ȡ�����ļ����� ��ȡ���������ļ���������Layer������Elevation
	private String getDescription(Element domElement)
	{
		String displayName = DataConfigurationUtils.getDataConfigDisplayName(domElement);
		String type = DataConfigurationUtils.getDataConfigType(domElement);
 
		StringBuilder sb = new StringBuilder(displayName);
 
		if (type.equalsIgnoreCase("Layer"))
		{
			sb.append(" (Layer)");
		}
		else if (type.equalsIgnoreCase("ElevationModel"))
		{
			sb.append(" (Elevations)");
		}
 
		return sb.toString();
	}
 
	//��ȡͼ�㷶Χ
	protected static Sector getSector(Element domElement)
	{
		return WWXML.getSector(domElement, "Sector", null);
	}
	 
	//��ȡ�����ؼ�����
	public static WorldWindowGLCanvas getWorldWindowGLCanvas()
	{
		return worldWindowGLCanvas;
	}
 
	//���û����ؼ�����
	public static void setWorldWindowGLCanvas(WorldWindowGLCanvas worldWindowGLCanvas)
	{
		LoadCacheData.worldWindowGLCanvas = worldWindowGLCanvas;
	}
	
	//�����û�ָ���Ļ���վ��
	public static void setTileCachePath(String tileCachePath) 
	{
		LoadCacheData.tileCachePath = tileCachePath;
	}

}
