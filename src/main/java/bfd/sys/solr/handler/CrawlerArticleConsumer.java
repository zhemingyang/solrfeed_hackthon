package bfd.sys.solr.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public class CrawlerArticleConsumer extends BaseItemConsumer {
	

	public CrawlerArticleConsumer(String topic, String group,Exchanger<List<String>> exchanger,boolean isDebug) {
		super(topic, group,exchanger,isDebug);
		this.logger = Logger.getLogger(CrawlerArticleConsumer.class);
	}

	public ArrayList<SolrInputDocument> parseItem(String msg) {
		return updateItem(msg);
	}

	public void info(String msg) {
		logger.info(msg);
	}

}
