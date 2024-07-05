/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.client.impl.consumer;

import org.apache.rocketmq.common.message.MessageQueue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Message lock,strictly ensure the single queue only one thread at a time consuming
 */
public class MessageQueueLock {
    private ConcurrentMap<MessageQueue, Object> mqLockTable =
        new ConcurrentHashMap<MessageQueue, Object>();

    /**
     * 根据消息队列获取对应的锁对象。
     * 此方法用于确保对特定消息队列的操作是线程安全的。它通过映射关系，为每个消息队列分配一个锁对象，
     * 当多个线程尝试操作同一个消息队列时，它们需要获取对应的锁才能进行操作，从而实现线程同步。
     *
     * @param mq 待获取锁的消息队列对象。每个消息队列对应一个锁对象，用于线程同步。
     * @return 返回与给定消息队列关联的锁对象。如果该消息队列之前没有关联的锁对象，则创建并返回一个新的锁对象；
     *         否则，返回之前已存在的锁对象。
     */
    public Object fetchLockObject(final MessageQueue mq) {
        // 尝试从映射表中直接获取锁对象。
        Object objLock = this.mqLockTable.get(mq);
        // 如果当前消息队列没有对应的锁对象，则创建一个新的锁对象。
        if (null == objLock) {
            objLock = new Object();
            // 使用原子操作尝试将新创建的锁对象放入映射表中，如果该消息队列已有锁对象，则替换为已存在的锁对象。
            Object prevLock = this.mqLockTable.putIfAbsent(mq, objLock);
            // 如果映射表中已存在锁对象，则更新本地变量objLock为已存在的锁对象。
            if (prevLock != null) {
                objLock = prevLock;
            }
        }

        // 返回与消息队列关联的锁对象。
        return objLock;
    }

}
