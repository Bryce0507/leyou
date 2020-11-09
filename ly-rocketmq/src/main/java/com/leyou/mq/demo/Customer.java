package com.leyou.mq.demo;

import lombok.SneakyThrows;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 实现消息消费
 * @author zxb
 * @date 2020/11/9 14:18
 */
public class Customer {

    public static void main(String[] args) throws MQClientException {

        //创建一个DefaultMQPushConsumer
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("demo_consumer_group");
        //设置nanmesrv地址
        consumer.setNamesrvAddr("47.112.167.185:9876");
        //设置消息拉去最大数
        consumer.setConsumeMessageBatchMaxSize(2);

        //设置subscribe，这里是要读取的主题信息 String topic（消费主题）, String subExpression（过滤规则）
        consumer.subscribe("topic_demo","*");
        //创建消息监听messageListener
        consumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //迭代消息信息
                for (MessageExt msg : msgs) {
                    try {
                        //获取消息
                        String topic = msg.getTopic();
                        //获取标签
                        String tags = msg.getTags();
                        //获取信息
                        String result = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);

                        System.out.println("consumer消费信息-----topic: " + topic + "-----tags:" + tags + "-----result:" + result);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        //消息重试
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        //开启消费者
        consumer.start();

    }
}
