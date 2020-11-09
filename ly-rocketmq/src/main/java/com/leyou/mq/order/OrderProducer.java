package com.leyou.mq.order;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author zxb
 * @date 2020/11/9 14:06
 */
public class OrderProducer {

    public static void main(String[] args) throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException, MQBrokerException {
        //创建DefaultMQProducer
        DefaultMQProducer producer = new DefaultMQProducer("demo-producer_group");
        //设置nameSrv地址
        producer.setNamesrvAddr("47.112.167.185:9876");
        //开启DefaultMQProducer
        producer.start();
        //创建消息message  参数String topic（主题）, String tags(用于消息过滤), String keys（消息唯一值）, byte[] body(消息主题)
//        Message message = new Message("topic_demo","tags","keys_1","hello,你好啊中国！！！！".getBytes(RemotingHelper.DEFAULT_CHARSET));
        //消息发送
        // 参数：
        // 1.消息信息，
        // 2.选择指定的消息队列对象
        // 3.指定对应的队列下标
        for (int i = 0; i < 5; i++) {

            //创建消息message  参数String topic（主题）, String tags(用于消息过滤), String keys（消息唯一值）, byte[] body(消息主题)
            Message message = new Message("topic_demo", "tags", "keys_1", ("hello,你好啊中国！！！！" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));

            SendResult result = producer.send(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    //获取队列的下标
                    Integer index = (Integer) arg;

                    return mqs.get(index);
                }
            }, 0);
            System.out.println("result = " + result);
        }
        //关闭DefaultMQProducer
        producer.shutdown();
    }
}
