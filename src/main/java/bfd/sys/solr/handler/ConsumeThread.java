package bfd.sys.solr.handler;

import org.apache.log4j.Logger;


/**
 * 生产者&消费者：
 * 读取ReadThread生产的数据（在Exchanger中），
 * 同时作为生产者将数据放到队列中供消费线程SolrThread（将数据写到solr索引库）使用
 */
public class ConsumeThread extends Thread {
	
	private static Logger logger = Logger.getLogger(ConsumeThread.class);
	
	private TopicConsumer consumer;
	private Thread thread;

	public ConsumeThread(TopicConsumer consumer,String name) {
		this.consumer = consumer;
		this.thread = new Thread(consumer,name);
	}

	
	@Override
	public synchronized void start() {
		thread.start();
		logger.info(this.thread.getName() + " has started.");
	}

}
