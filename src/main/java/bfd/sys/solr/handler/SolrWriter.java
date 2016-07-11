package bfd.sys.solr.handler;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Exchanger;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

// import scala.actors.threadpool.Arrays;
import bfd.sys.solr.ZkManager;
import bfd.sys.solr.util.Configuration;
import bfd.sys.solr.util.Constants;
import bfd.sys.solr.util.DefaultConfig;

import com.google.common.collect.Sets;

public class SolrWriter {

	private static Logger logger = Logger.getLogger(SolrWriter.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("start read config ...");
		//读配置文件
		String configFile = System.getProperty("config.file.path","../conf/config.properties");
		Configuration config = new DefaultConfig(configFile);
		ZkManager.init(config);
		Set<String> cids = new HashSet<String>();//存储所有solr客户唯一标识
		String[] crawlers = null,inputs = null;
		if(config.containsKey(Constants.CRAWLER_TOPIC)){
			crawlers = config.getString(Constants.CRAWLER_TOPIC).split(",");
			for(String topic:crawlers){
				cids.addAll(Arrays.asList(config.getString(topic).split(",")));
			}
		}
		if(config.containsKey(Constants.INPUT_TOPIC)){
			inputs = config.getString(Constants.INPUT_TOPIC).split(",");
			for(String topic:inputs){
				cids.addAll(Arrays.asList(config.getString(topic).split(",")));
			}
		}
		if(cids.size()==0){
			logger.error("no cid has been set.");
			System.exit(-1);
		}
		String group = config.getString(Constants.TOPIC_GROUP);
		for(String cid:cids){
			Constants.data.put(cid,new ConcurrentLinkedQueue<SolrInputDocument>());
		}
		logger.info("cids contains:" + cids.toString());
		if(!ZkManager.refreshConfig(cids)){
			logger.error("failed to read from zookeeper .");
			System.exit(-1);
		}
		Set<String> m2 = new HashSet<String>();
//		#method处理方法,m2中存储的值：AddItem,RmItem,UpdateItem
		Collections.addAll(m2, config.getString(Constants.METHOD_INPUT).split(","));
		Configuration.methodMap.put(Constants.METHOD_INPUT, m2);
//		m3中存储的值：RmItem
		Set<String> m3 = new HashSet<String>();
		Collections.addAll(m3, config.getString(Constants.METHOD_REMOVE).split(","));
		Configuration.methodMap.put(Constants.METHOD_REMOVE, m3);
		
		String kafcaFile = System.getProperty("kafca.file.path","../conf/kafca.properties");
		DefaultConfig.setKafkaProp(kafcaFile);
		
		logger.info("start create thread ...");
		
		//创建消费kafca队列的线程
		final List<ReadThread> readers = new ArrayList<ReadThread>();
		final List<ConsumeThread> threads = new ArrayList<ConsumeThread>();
		if(crawlers!=null){
			for(String crawlerTopic:crawlers){
				Exchanger<List<String>> crawlerExchanger = new Exchanger<List<String>>();
				ReadThread kafcaCrawler = new ReadThread(new CrawlerArticleReader(
						crawlerTopic, group, Sets.newHashSet(config.getString(crawlerTopic).split(",")), config.getBoolean(Constants.CRAWLER_DEBUG),crawlerExchanger),crawlerTopic+"-"+group+"-input");
				readers.add(kafcaCrawler);
				ConsumeThread solrCrawler = new ConsumeThread(new CrawlerArticleConsumer(crawlerTopic, group,crawlerExchanger,config.getBoolean(Constants.CRAWLER_DEBUG)),crawlerTopic+"-"+group+"-solr");
				threads.add(solrCrawler);
			}
			logger.info("create thread for crawler topic :" + crawlers);
		}
		if(inputs!=null){
			for(String inputTopic:inputs){//对应config.properties中的数据为input.topic:DS.Input.All.Gmedia_6,DS.Input.All.Gfengniao,DS.Input.All.Gmingchengsuzhou,DS.Input.All.Gzgc_pc,DS.Input.All.Gwanweijiadian
				Exchanger<List<String>> inputExchanger = new Exchanger<List<String>>();
				ReadThread kafkaInput = new ReadThread(new InputArticleReader(
						inputTopic, group, Sets.newHashSet(config.getString(inputTopic).split(",")), config.getBoolean(Constants.INPUT_DEBUG),inputExchanger),inputTopic+"-"+group+"-input");
				readers.add(kafkaInput);
				ConsumeThread solrInput = new ConsumeThread(new InputArticleConsumer(inputTopic, group,inputExchanger),inputTopic+"-"+group+"-solr");
				threads.add(solrInput);
			}
			logger.info("create thread for input topic :" + inputs);
		}
		if(config.containsKey(Constants.REMOVE_TOPIC)){
			String removeTopic = config.getString(Constants.REMOVE_TOPIC);
			Exchanger<List<String>> removeExchanger = new Exchanger<List<String>>();
			ReadThread kafcaRemove = new ReadThread(new RemoveArticleReader(
					removeTopic, group, cids, config.getBoolean(Constants.REMOVE_DEBUG),removeExchanger),removeTopic+"-"+group+"-input");
			readers.add(kafcaRemove);
			ConsumeThread solrRemove = new ConsumeThread(new RemoveArticleConsumer(removeTopic, group,removeExchanger),removeTopic+"-"+group+"-solr");
			threads.add(solrRemove);
		}
		logger.info("threads has created over.");
		
		logger.info("start all threads ...");
		for (Thread t : threads) {
			t.start();
		}
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		for (ReadThread s : readers){
			s.start();
		}
		logger.info("all threads start over.");
		
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				logger.info("schedule time is :"+System.currentTimeMillis());
				doSchedule();
			}
		}, 1000 * 10 * 1, 1000 * 10 * 1);
		
		//注册钩子  
		Runtime.getRuntime().addShutdownHook(new Thread() {  
			
			public void run() {  
				for (ReadThread t : readers) {
					t.exit();
				}
				System.out.println("read kafka success stopped.");
				try {
					Thread.currentThread().sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				timer.cancel();
				System.out.println("timer success exit."); 
				try {
					Thread.currentThread().sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("task success exit.");  
			}  
		});  
		
	}
	
	public static void doSchedule() {
		for (String cid : Constants.data.keySet()) {
			new SolrThread(cid).start();
		}
	}

}
