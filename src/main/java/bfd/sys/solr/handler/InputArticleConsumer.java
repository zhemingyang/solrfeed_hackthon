package bfd.sys.solr.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public class InputArticleConsumer extends BaseItemConsumer {
	

	public InputArticleConsumer(String topic, String group,Exchanger<List<String>> exchanger) {
		super(topic, group,exchanger);
		this.logger = Logger.getLogger(InputArticleConsumer.class);
	}

	public ArrayList<SolrInputDocument> parseItem(String msg) {
		try {
			logger.info("enter parseItem.");
			JSONObject json = JSONObject.fromObject(msg.trim());
			if(json.has("type") && "newscontent".equals(json.get("type").toString())){
				return updateItem(msg);
			}
			if(json.has("type") && "newslist".equals(json.get("type").toString())){
				return updateListItem(msg);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void info(String msg) {
		logger.info(msg);
	}

}
