package com.ustcinfo.ptp.yunting.service;

import org.apache.rocketmq.client.producer.DefaultMQProducer;

public interface IrocketMqProducer {
  public DefaultMQProducer  createProducter(String mqName);
  public DefaultMQProducer getcreateProducter(String mq) ;
}
