package bfd.sys.solr.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.solr.common.SolrInputDocument;

public final class Constants {

	public static final String METHOD_INPUT = "InputArticleReader";
	public static final String METHOD_REMOVE = "RemoveArticleReader";
	public static final String INPUT_DEBUG = "input.debug";
	public static final String INPUT_TOPIC = "input.topic";
	public static final String CRAWLER_DEBUG = "crawler.debug";
	public static final String CRAWLER_TOPIC = "crawler.topic";
	public static final String REMOVE_DEBUG = "remove.debug";
	public static final String REMOVE_TOPIC = "remove.topic";
	public static final String TOPIC_GROUP = "topic.group";
	public static final ConcurrentHashMap<String, ConcurrentLinkedQueue<SolrInputDocument>> data = new ConcurrentHashMap<String, ConcurrentLinkedQueue<SolrInputDocument>>();

}
