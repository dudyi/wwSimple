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
 
	//构造函数
	public LoadCacheData(WorldWindowGLCanvas worWindowGLCanvas,String tileCachePath)
	{
		LoadCacheData.setWorldWindowGLCanvas(worWindowGLCanvas);
		LoadCacheData.setTileCachePath(tileCachePath);
	}
	
	//线程读取设置缓存站点信息
	public void loadPreviouslyInstalledData()
	{ 
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				//新增缓存站点
				File file = new File(tileCachePath);
				if(file.getParentFile().exists()) 
					WorldWind.getDataFileStore().addLocation(tileCachePath, true);

				//加载默认缓存站点
				loadInstalledDataFromFileStore(WorldWind.getDataFileStore());
			}
		});
		thread.start();
	}
 
	//遍历缓存站点
	protected void loadInstalledDataFromFileStore(FileStore fileStore)
	{
		// 遍历已有的缓存文件
		for (File file : fileStore.getLocations())
		{
			// 文件存在并且是缓存文件目录
			if (file.exists() && fileStore.isInstallLocation(file.getPath()))
			{
				System.out.println(file.getPath());
				loadInstalledDataFromDirectory(file);
			}
 
		}
	}
 
	//从文件目录加载缓存数据
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
				// 根据缓存文件XML描述文件创建Document对象
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
 
			// 由于数据配置文件来自于已有的文件，因此不能保证它是由当前版本WW's Installer
			// 产生的。可能是由之前版本或其他应用程序产生的，因此要为可能缺失的参数设置备用值（这些参数需要用来构建图层或高程模拟）
			AVList params = new AVListImpl();
			setFallbackParams(doc, filename, params);
			// 添加数据
			addInstalledData(doc, params);
		}
	}
 
	//设置备用参数值
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
 
	//添加缓存数据
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
 
	//添加已有缓存数据
	public void addInstalledCacheData(final Element domElement,
			final AVList params)
	{
		if (domElement == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
 
		String description = getDescription(domElement); // 图层名称
		Sector sector = getSector(domElement); // 图层范围
		System.out.println(description);
		System.out.println(sector);
		addToWorldWindow(domElement, params);
	}
 
	//将缓存文件加入WW
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
 
	//向WW中添加图层
	private void addLayerToWorldWindow(Element domElement, AVList params)
	{
		Layer layer = null;
		try
		{
			// Factory创建的图层默认是不可见的
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
		layer.setEnabled(true); // 设置图层可见
 
		// 添加至WW
		if (!getWorldWindowGLCanvas().getModel().getLayers().contains(layer))
		{
			getWorldWindowGLCanvas().getModel().getLayers().add(layer); 
		}
	}
 
	//添加高程图层
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
 
	//获取缓存文件类型 获取缓存配置文件描述：是Layer或者是Elevation
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
 
	//获取图层范围
	protected static Sector getSector(Element domElement)
	{
		return WWXML.getSector(domElement, "Sector", null);
	}
	 
	//获取画布控件对象
	public static WorldWindowGLCanvas getWorldWindowGLCanvas()
	{
		return worldWindowGLCanvas;
	}
 
	//设置画布控件对象
	public static void setWorldWindowGLCanvas(WorldWindowGLCanvas worldWindowGLCanvas)
	{
		LoadCacheData.worldWindowGLCanvas = worldWindowGLCanvas;
	}
	
	//设置用户指定的缓存站点
	public static void setTileCachePath(String tileCachePath) 
	{
		LoadCacheData.tileCachePath = tileCachePath;
	}

}
