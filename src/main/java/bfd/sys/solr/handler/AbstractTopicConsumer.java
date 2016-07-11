package bfd.sys.solr.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Exchanger;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

public abstract class AbstractTopicConsumer implements TopicConsumer {
	
	protected List<String> buffer = new ArrayList<String>();
	protected Exchanger<List<String>> exchanger;
	protected String topic;
	protected String group;
	protected boolean isDebug = false;
	protected static Logger logger = Logger.getLogger(AbstractTopicConsumer.class);

	public void run() {
		ArrayList<SolrInputDocument> docs = new ArrayList<>();
		while(true){
			try {
				buffer = exchanger.exchange(buffer);
				int size = buffer.size();
				for (int i = 0; i < size; i++) {
					logger.info("before parseItem.");
					ArrayList<SolrInputDocument> inputDocuments = parseItem(buffer.remove(0));
					if(inputDocuments !=null)
						docs.addAll(inputDocuments);
						logger.info("add doc success!");
				}
				process(docs);
				logger.info("process doc success!");
				docs.clear();
			} catch (InterruptedException e) {
				info(e.getMessage());
				e.printStackTrace();
			} catch (Throwable e) {
				info(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	

}
