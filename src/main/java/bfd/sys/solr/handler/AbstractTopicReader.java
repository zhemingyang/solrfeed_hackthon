package bfd.sys.solr.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Exchanger;

import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import org.apache.log4j.Logger;

import bfd.kafkaconsumerclient.ConsumerHandler;

import com.google.common.collect.ImmutableMap;

public abstract class AbstractTopicReader implements TopicReader {
  
  private static Logger logger = Logger.getLogger(AbstractTopicReader.class);
	
	protected List<String> buffer = new ArrayList<String>();
	protected Exchanger<List<String>> exchanger;
	protected String topic;
	protected String group;
	protected ConsumerHandler consumerHandler = new ConsumerHandler();	
	protected volatile boolean status = false;
	protected volatile boolean stopped = false;
	protected boolean isDebug = false;

	public void run() {
		String msg = null;
		ConsumerConnector connector = this.consumerHandler.getConnector(group);
		Map<String, List<KafkaStream<byte[], byte[]>>> topicMessageStreams = connector
		.createMessageStreams(ImmutableMap.of(topic, 1));
		List<KafkaStream<byte[], byte[]>> streams = topicMessageStreams.get(topic);	
		while(!stopped){
			try {
			  for (final KafkaStream<byte[], byte[]> stream : streams) {
	        for (MessageAndMetadata<byte[], byte[]> msgAndMetadata : stream) {
//	          Message message = (Message) msgAndMetadata.message();
//	          ByteBuffer byteBuffer = message.payload();
//	          byte[] bytes = new byte[byteBuffer.remaining()];
//	          byteBuffer.get(bytes);
	          msg = new String(msgAndMetadata.message());
	          logger.info("read kafka message success:===============>"+msg);
	          msg = filter(msg);
	          if(msg!=null){
	            try {
	              buffer.add(msg);
	              buffer = exchanger.exchange(buffer);
	            } catch (InterruptedException e) {
	              e.printStackTrace();
	            }
	          }
//	          try {
//	            Thread.currentThread().sleep(0, 1000);
//	          } catch (InterruptedException e) {
//	            e.printStackTrace();
//	          }
	          if(!status){
	            break ;
	          }
	        }
	        if(!status){
	          break ;
	        }
	      }
      } catch (Throwable e) {
        logger.error("读取kafka消息失败，原因："+e.getMessage());
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

	public void readTopic() {
		if(!this.status){
			this.status = true;
		}
		if(this.stopped){
			this.stopped = false;
		}
	}

	public void cancelTopic() {
		if(this.status){
			this.status = false;
		}
	}
	
	public void exit() {
		if(this.status){
			this.status = false;
		}
		if(!this.stopped){
			this.stopped = true;
		}
		
	}

}
