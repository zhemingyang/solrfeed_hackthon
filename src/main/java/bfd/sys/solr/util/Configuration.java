package bfd.sys.solr.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public interface Configuration {

	String getString(String key);
	boolean getBoolean(String key);
	boolean containsKey(String key);
	Map<String,Set<String>> methodMap = new HashMap<String,Set<String>>() ;
	Map<String,Set<String>> fieldMap = new HashMap<String,Set<String>>() ;
}
