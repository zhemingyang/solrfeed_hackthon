package bfd.sys.solr.handler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Exchanger;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;


public class RemoveArticleReader extends BaseItemReader {

	public RemoveArticleReader(String topic, String group, Set<String> cids,Exchanger<List<String>> exchanger) {
		super(topic, group, cids,exchanger);
		logger = Logger.getLogger(RemoveArticleReader.class);
	}
	
	public RemoveArticleReader(String topic, String group, Set<String> cids, boolean isDebug,Exchanger<List<String>> exchanger) {
		super(topic, group, cids, isDebug,exchanger);
		logger = Logger.getLogger(RemoveArticleReader.class);
	}

	@Override
	String getReaderName() {
		return RemoveArticleReader.class.getSimpleName();
	}

	@Override
	boolean contain(JSONObject json) {
		return true;
	}
	
	public void debug(String msg) {
		logger.info(msg);
	}


}
