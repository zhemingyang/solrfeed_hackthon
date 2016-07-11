package bfd.sys.solr.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;


public interface TopicConsumer extends Runnable {
	
	void setTopic(String topic);
	
	void setGroup(String group);
	
	/**
	 * 将solrDoc写入队列，作为数据源，供后续消费者写入索引库使用
	 * @param doc
	 */
	void process(ArrayList<SolrInputDocument> doc);
	
	ArrayList<SolrInputDocument> parseItem(String msg);
	
	void info(String msg);
	
}
