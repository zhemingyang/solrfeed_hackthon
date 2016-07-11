package bfd.sys.solr.handler;

import org.apache.log4j.Logger;

/**
 * 读取kafka数据线程：将读取到的数据放到Exchanger，供消费者线程ConsumeThread使用
 */
public class ReadThread extends Thread {
	
	private static Logger logger = Logger.getLogger(ReadThread.class);
	private TopicReader reader;
	private Thread thread;

	public ReadThread(TopicReader reader,String name) {
		this.reader = reader;
		this.thread = new Thread(reader,name);
	}

	@Override
	public synchronized void start() {
		reader.readTopic();
		thread.start();
		logger.info(this.thread.getName() + " has started.");
	}
	
	public synchronized void cancel() {
		reader.cancelTopic();
		logger.info(this.thread.getName() + " has canceled.");
	}
	
	public synchronized void exit() {
		reader.exit();
		logger.info(this.thread.getName() + " has exit.");
	}
	
	
}
