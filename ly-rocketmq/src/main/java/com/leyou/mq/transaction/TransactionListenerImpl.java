package com.leyou.mq.transaction;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author zxb
 * @date 2020/11/9 16:31
 */
public class TransactionListenerImpl implements TransactionListener {

    //执行本地事务
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        return null;
    }

    //消息回查
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return null;
    }
}
