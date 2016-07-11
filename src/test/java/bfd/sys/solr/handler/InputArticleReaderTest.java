package bfd.sys.solr.handler;

import bfd.sys.solr.util.DefaultConfig;
import kafka.examples.KafkaProperties;

public class InputArticleReaderTest {

  public static void main(String[] args) {

    String group = KafkaProperties.groupId;
    String inputTopic = KafkaProperties.topic;
    String kafcaFile = System.getProperty("user.dir")+"/kafka.properties";
    DefaultConfig.setKafkaProp(kafcaFile);
    InputArticleReader reader = new InputArticleReader(inputTopic, group, null, true,null);
    reader.run();
    
  }

}
