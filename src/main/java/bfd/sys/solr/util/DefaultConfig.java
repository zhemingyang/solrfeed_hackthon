package bfd.sys.solr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;




public class DefaultConfig implements Configuration {
	
	private static String kafkaProp;
	private Properties prop = new Properties();
	
	public static Properties getKafkaProp(){
		return getProp(kafkaProp);
	}
	
	public static void setKafkaProp(String file){
		kafkaProp = file;
	}
	
	public static Properties getProp(String file){
		Properties p = new Properties();
		File f = new File(file);
		try {
			p.load(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	public DefaultConfig(String configFile) {
		File f = new File(configFile);
		try {
			prop.load(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isDebug() {
		return false;
	}

	public String getString(String key) {
		return prop.getProperty(key);
	}

	public boolean getBoolean(String key) {
		return getString(key)==null?false:Boolean.valueOf(getString(key));
	}

	@Override
	public boolean containsKey(String key) {
		return prop.containsKey(key);
	}


}
