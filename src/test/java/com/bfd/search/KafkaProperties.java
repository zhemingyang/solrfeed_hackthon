package com.bfd.search;

public interface KafkaProperties
{
//  final static String zkConnect = "127.0.0.1:2181/kafka08";
  final static String zkConnect = "172.18.1.41:2181/kafka08";
  final static  String groupId = "solr_multishard_1";
//  final static String topic = "Input.All.Gxicihutong";
  final static String topic = "DS.Input.All.Gxicihutong";
  final static String kafkaServerURL = "localhost";
  final static int kafkaServerPort = 9092;
  final static int kafkaProducerBufferSize = 64*1024;
  final static int connectionTimeOut = 100000;
  final static int reconnectInterval = 10000;
  final static String topic2 = "topic2";
  final static String topic3 = "topic3";
  final static String clientId = "SimpleConsumerDemoClient";
}
