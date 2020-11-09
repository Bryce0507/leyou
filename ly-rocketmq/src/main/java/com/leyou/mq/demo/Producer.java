package com.leyou.mq.demo;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;

/**
 * @author zxb
 * @date 2020/11/9 14:06
 */
public class Producer {

    public static void main(String[] args) throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException, MQBrokerException {
        //创建DefaultMQProducer
        DefaultMQProducer producer = new DefaultMQProducer("demo-producer_group");
        //设置nameSrv地址
        producer.setNamesrvAddr("47.112.167.185:9876");
        //开启DefaultMQProducer
        producer.start();
        //创建消息message  参数String topic（主题）, String tags(用于消息过滤), String keys（消息唯一值）, byte[] body(消息主题)
        Message message = new Message("topic_demo","tags","keys_1","hello,你好啊中国！！！！".getBytes(RemotingHelper.DEFAULT_CHARSET));
        //消息发送
        SendResult result = producer.send(message);
        System.out.println("result = " + result);
        //关闭DefaultMQProducer
        producer.shutdown();
    }
}
