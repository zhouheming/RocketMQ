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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.help.ScheduleHelper;
import com.alibaba.rocketmq.common.message.ScheduleMsgInfo;


/**
 * 消费队列实现
 * 
 * @author guanghao.rb
 * @since 2013-7-21
 */
public class ScheduleConsumeQueue extends ConsumeQueue {

	private static final Logger log = LoggerFactory.getLogger(LoggerName.StoreLoggerName);
	
	public static Integer UNLOAD = 0; //未加载到内存
	public static Integer LOADING = 1; //加载中
	public static Integer LOADED = 2; //加载内存操作完成
	public static Integer DELETING = 3; //删除中
	
	public Integer status = UNLOAD;
	
	public long loadingOffsetFlag;
	
	// 内存存储
    private final ConcurrentHashMap<Long, List<ScheduleMsgInfo>> scheduleMsgTable = 
    		new ConcurrentHashMap<Long, List<ScheduleMsgInfo>>(1800);

    public ScheduleConsumeQueue(//
            final String topic,//
            final int queueId,//
            final String storePath,//
            final int mapedFileSize,//
            final DefaultMessageStore defaultMessageStore) {
       super(topic, queueId, storePath, mapedFileSize, defaultMessageStore);
    }


    public boolean storageLoad() {
    	//提前把List都new好，需要线程安全
    	status = LOADING;
    	
    	
    	status = LOADED;
    	return true;
    }

    public int deleteExpiredFile(long offset) {
    	//
        int cnt = super.deleteExpiredFile(offset);
        return cnt;
    }


    public void putMessagePostionInfoWrapper(long offset, int size, long tagsCode, long storeTimestamp,
            long logicOffset) {
    	super.putMessagePostionInfoWrapper(offset, size, tagsCode, storeTimestamp, logicOffset);
    	if(status.equals(LOADING)) {
    		if(loadingOffsetFlag==0) {
    			synchronized (LOADED) {
					if(loadingOffsetFlag==0) {
						loadingOffsetFlag = logicOffset;
					}
				}
    		}
    	}
    	if(status.equals(LOADING)||status.equals(LOADED)) {
    		ScheduleMsgInfo msg = new ScheduleMsgInfo();
        	msg.setCommitOffset(offset);
        	msg.setSize(size);
        	long timeKey = ScheduleHelper.getTimeKey(tagsCode);
        	List<ScheduleMsgInfo> scheduleList = scheduleMsgTable.get(timeKey);
        	scheduleList.add(msg);
    	}
    	
    }


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}
    

}
