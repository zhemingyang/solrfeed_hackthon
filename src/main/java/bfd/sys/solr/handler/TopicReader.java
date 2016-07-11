package bfd.sys.solr.handler;

public interface TopicReader extends Runnable{
	
	void setTopic(String topic);
	
	void setGroup(String group);
	
	void readTopic();
	
	void cancelTopic();
	
	String filter(String msg);
	
	void debug(String msg);
	
	void exit();
	
}
