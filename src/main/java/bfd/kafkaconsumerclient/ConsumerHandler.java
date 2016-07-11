package bfd.kafkaconsumerclient;

import java.util.Properties;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;

import org.apache.log4j.Logger;


public class ConsumerHandler {
	
	// Kafka配置
	private Properties kfk_props = new Properties();
	
	public static final Logger logger = Logger.getLogger(ConsumerHandler.class);
	
	public void init(Properties properties) {
		logger.info("Initing...");
		
		// kafka配置
		kfk_props.put("zookeeper.connect", properties.getProperty("kafka.zk.connect",
				"bjlg-zk1:2882,bjlg-zk2:2882,bjlg-zk3:2882,bjlg-zk4:2882,bjlg-zk5:2882,bjlg-zk6:2882"));
		kfk_props.put("zookeeper.session.timeout.ms",
				properties.getProperty("kafka.zk.sessiontimeout.ms", "300000"));
		kfk_props.put("zookeeper.connection.timeout.ms",
				properties.getProperty("kafka.zk.connectiontimeout.ms", "1000000"));
		// 自动提交偏移量的时间间隔，默认60s
		kfk_props.put("auto.commit.interval.ms",
				properties.getProperty("kafka.autocommit.interval.ms", "60000"));
		// 从上次到offset续读，如果是新的group则从最老的内容开始读（offset设置为smallest，若设置为largest表示从最新的内容开始读）
		kfk_props.put("auto.offset.reset",
				properties.getProperty("kafka.autooffset.reset", "smallest"));
		logger.info("Kafka connection properties are: " + kfk_props.toString());
	
	}
	

	public ConsumerConnector getConnector(String group) {
		kfk_props.put("group.id", group);
		ConsumerConfig consumerConfig = new ConsumerConfig(kfk_props);
		ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
		return consumerConnector;
	}

}
