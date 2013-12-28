/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.message.ScheduleMsgInfo;
import com.alibaba.rocketmq.store.schedule.ScheduleMessageService;


/**
 * 消费队列实现
 * 
 * @author guanghao.rb
 * @since 2013-7-21
 */
public class ScheduleConsumeQueue extends ConsumeQueue {

	private static final Logger log = LoggerFactory.getLogger(LoggerName.StoreLoggerName);
	
	private ScheduleMessageService scheduleMessageService;

    public ScheduleConsumeQueue(//
            final String topic,//
            final int queueId,//
            final String storePath,//
            final int mapedFileSize,//
            final DefaultMessageStore defaultMessageStore) {
       super(topic, queueId, storePath, mapedFileSize, defaultMessageStore);
       scheduleMessageService = defaultMessageStore.getScheduleMessageService();
    }


    public boolean load() {
        boolean result = this.getMapedFileQueue().load();
        return result;
    }

    public int deleteExpiredFile(long offset) {
        int cnt = super.deleteExpiredFile(offset);
        scheduleMessageService.deleteExpireScheduleMsgs();
        return cnt;
    }


    public void putMessagePostionInfoWrapper(long offset, int size, long tagsCode, long storeTimestamp,
            long logicOffset) {
    	super.putMessagePostionInfoWrapper(offset, size, tagsCode, storeTimestamp, logicOffset);
    	ScheduleMsgInfo msg = new ScheduleMsgInfo();
    	msg.setCommitOffset(offset);
    	msg.setSize(size);
    	scheduleMessageService.setScheduleMsg(tagsCode, msg);
    }

}
