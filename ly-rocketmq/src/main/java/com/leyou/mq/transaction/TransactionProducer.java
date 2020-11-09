package com.leyou.mq.transaction;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author zxb
 * @date 2020/11/9 14:06
 */
public class TransactionProducer {

    public static void main(String[] args) throws MQClientException, UnsupportedEncodingException, RemotingException, InterruptedException, MQBrokerException {

        //创建TransactionMQProducer
        TransactionMQProducer producer = new TransactionMQProducer("demo-producer_group");
        //设置nameSrv地址
        producer.setNamesrvAddr("47.112.167.185:9876");

        //指定消息监听对象，用于执行本地事务和消息回查
        TransactionListener transactionListener = new TransactionListenerImpl();
        producer.setTransactionListener(transactionListener);

        ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("client-transaction-msg-check-thread");
                        return thread;
                    }
                });

        producer.setExecutorService(executorService);


        //开启DefaultMQProducer
        producer.start();

        //创建消息message  参数String topic（主题）, String tags(用于消息过滤), String keys（消息唯一值）, byte[] body(消息主题)
        Message message = new Message("topic_demo", "tags", "keys_1", "hello,tranaction！！！！".getBytes(RemotingHelper.DEFAULT_CHARSET));

        TransactionSendResult result = producer.sendMessageInTransaction(message, "hello-transaction");
        System.out.println("result = " + result);

        //关闭DefaultMQProducer
        producer.shutdown();
    }
}
