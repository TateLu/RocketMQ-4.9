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
package org.apache.rocketmq.client.consumer.rebalance;

import org.apache.rocketmq.common.message.MessageQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Average Hashing queue algorithm
 */
public class AllocateMessageQueueAveragely extends AbstractAllocateMessageQueueStrategy {


    /**
     * 根据消费者组和当前消费者ID，从所有MessageQueue中分配指定数量的MessageQueue给当前消费者。
     * 分配逻辑确保每个消费者得到公平的份额，且同一消费者组内的消费者不会互相争夺相同的MessageQueue。
     *
     * @param consumerGroup 消费者组ID，用于标识同一组内的消费者。
     * @param currentCID 当前消费者ID，用于唯一标识当前消费者。
     * @param mqAll 所有可用的MessageQueue列表。
     * @param cidAll 所有消费者ID的列表，用于确定消费者顺序和分配逻辑。
     * @return 分配给当前消费者的MessageQueue列表。
     */
    @Override
    public List<MessageQueue> allocate(String consumerGroup, String currentCID, List<MessageQueue> mqAll, List<String> cidAll) {
        List<MessageQueue> result = new ArrayList<MessageQueue>();
        if (!check(consumerGroup, currentCID, mqAll, cidAll)) {
            return result;
        }
        /* 查找当前消费者ID在所有消费者ID列表中的索引，用于计算分配起点。 */
        /**implementation*/
        int index = cidAll.indexOf(currentCID);
        int mod = mqAll.size() % cidAll.size();
        int averageSize =
            mqAll.size() <= cidAll.size() ? 1 : (mod > 0 && index < mod ? mqAll.size() / cidAll.size()
                + 1 : mqAll.size() / cidAll.size());
        int startIndex = (mod > 0 && index < mod) ? index * averageSize : index * averageSize + mod;
        /* 计算当前消费者实际能分配到的MessageQueue数量。 */
        int range = Math.min(averageSize, mqAll.size() - startIndex);
        /* 根据起始索引和实际数量，从所有MessageQueue中分配给当前消费者。 */
        for (int i = 0; i < range; i++) {
            result.add(mqAll.get((startIndex + i) % mqAll.size()));
        }
        return result;
    }


    @Override
    public String getName() {
        return "AVG";
    }
}
