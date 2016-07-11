package bfd.sys.solr.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Exchanger;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import bfd.sys.solr.util.DefaultConfig;

public abstract class BaseItemReader extends AbstractTopicReader {
	
	protected Set<String> cids = new HashSet<String>();
	protected Logger logger;
	abstract String getReaderName();
	abstract boolean contain(JSONObject json);
	
	public BaseItemReader(String topic,String group,Set<String> cids,Exchanger<List<String>> exchanger) {
		this(topic,group,cids,false,exchanger);
	}
	
	public BaseItemReader(String topic,String group,Set<String> cids,boolean isDebug,Exchanger<List<String>> exchanger) {
		setTopic(topic);
		setGroup(group);
		this.cids = cids;
		this.isDebug = isDebug;
		this.exchanger = exchanger;
		this.consumerHandler.init((Properties)DefaultConfig.getKafkaProp().clone());
	}

	public String filter(String msg) {
		return msg;
	}
	
}
