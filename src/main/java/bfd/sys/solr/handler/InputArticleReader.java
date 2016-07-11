package bfd.sys.solr.handler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Exchanger;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import bfd.sys.solr.util.Configuration;

public class InputArticleReader extends BaseItemReader {
	
	public InputArticleReader(String topic,String group,Set<String> cids,Exchanger<List<String>> exchanger) {
		super(topic,group,cids,exchanger);
		logger = Logger.getLogger(InputArticleReader.class);
	}
	
	public InputArticleReader(String topic,String group,Set<String> cids,boolean isDebug,Exchanger<List<String>> exchanger) {
		super(topic,group,cids,isDebug,exchanger);
		logger = Logger.getLogger(InputArticleReader.class);
	}

	public void debug(String msg) {
		logger.info(msg);
	}

	@Override
	String getReaderName() {
		return InputArticleReader.class.getSimpleName();
	}

	@Override
	boolean contain(JSONObject json) {
		return json.has("html");
	}

}
