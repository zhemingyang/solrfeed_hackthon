package bfd.sys.solr.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public class RemoveArticleConsumer extends BaseItemConsumer {
	
	public RemoveArticleConsumer(String topic, String group,Exchanger<List<String>> exchanger) {
		super(topic, group,exchanger);
		this.logger = Logger.getLogger(RemoveArticleConsumer.class);
	}

	public ArrayList<SolrInputDocument> parseItem(String msg) {
		return rmItem(msg);
	}

	public void info(String msg) {
		logger.info(msg);
	}

}
