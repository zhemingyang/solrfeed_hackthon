package bfd.sys.solr.handler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Exchanger;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

public class CrawlerArticleReader extends BaseItemReader {
	
	public CrawlerArticleReader(String topic,String group,Set<String> cids,Exchanger<List<String>> exchanger) {
		super(topic,group,cids,exchanger);
		logger = Logger.getLogger(CrawlerArticleReader.class);
	}
	
	public CrawlerArticleReader(String topic,String group,Set<String> cids,boolean isDebug,Exchanger<List<String>> exchanger) {
		super(topic,group,cids,isDebug,exchanger);
		logger = Logger.getLogger(CrawlerArticleReader.class);
	}

	public void debug(String msg) {
		logger.info(msg);
	}

	@Override
	String getReaderName() {
		return CrawlerArticleReader.class.getSimpleName();
	}

	@Override
	boolean contain(JSONObject json) {
		try {
			if(json.has("method")&&"RmItem".equals(json.getString("method"))){
				return false;
			}
			if(json.has("attr")&&json.getJSONObject("attr").has("onshelf")&&"off".equals(json.getJSONObject("attr").getString("onshelf"))){
				return false;
			}
			if(json.has("timestamp")){
				System.out.println(json.get("timestamp").toString());
				double timestamp = Double.valueOf(json.getLong("timestamp")/1000);
				json.put("timestamp", timestamp);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
