package com.hp.oo.internal.sdk.execution.events;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.lang.Long;import java.lang.Object;import java.lang.Override;import java.lang.String;import java.lang.SuppressWarnings;import java.util.Date;

/**
 * Date: 3/6/12
 *
 * @author Dima Rassin
 */
public class ExecutionEvent implements Serializable{
	public static final String TABLE_NAME="OO_EXECUTION_EVENTS";

	private Long id;

	private String executionId;

	private ExecutionEnums.Event type;

	private Date publishTime;

	@SuppressWarnings("unused")
    private String sequenceOrder;   // used for ordering result from database

    private String path;

	private String data1;

	private String data2;

	private Long data3;

	private String data4;

    private long index;

    private boolean isDebuggerMode;

    @SuppressWarnings("unused")
	public ExecutionEvent() {/* used by JPA */}

	ExecutionEvent(String executionId, ExecutionEnums.Event type, String sequenceOrder, String path) {
		this.executionId = executionId;
		this.type = type;
		this.publishTime = new Date();
        this.sequenceOrder =  sequenceOrder;
        this.path = path;
	}

    ExecutionEvent(String executionId, ExecutionEnums.Event type, String sequenceOrder, String path, boolean isDebuggerMode) {
	    this( executionId,  type,  sequenceOrder,  path);
        this.isDebuggerMode = isDebuggerMode;
	}

	public Long getId() {
		return id;
	}

	public ExecutionEvent setId(Long id) {
		this.id = id;
		return this;
	}

	public String getExecutionId() {
		return executionId;
	}

	public ExecutionEvent setExecutionId(String executionId){
		this.executionId = executionId;
		return this;
	}

	public ExecutionEnums.Event getType() {
		return type;
	}

	public ExecutionEvent setType(ExecutionEnums.Event type) {
		this.type = type;
		return this;
	}

	public Date getPublishTime() {
		return publishTime;
	}

	public ExecutionEvent setPublishTime(Date publishTime) {
		this.publishTime = publishTime;
		return this;
	}

	public String getData1() {
		return data1;
	}

	public ExecutionEvent setData1(String data1) {
		if (data1 != null) {
			if (data1.length() > 255) {
				data1 = data1.substring(0, 255);
			}
		}
		this.data1 = data1;
		return this;
	}

	public String getData2() {
		return data2;
	}

	public ExecutionEvent setData2(String data2) {
		if (data2 != null) {
			if (data2.length() > 2048) {
				data2 = data2.substring(0, 2048);
			}
		}
		this.data2 = data2;
		return this;
	}

	public Long getData3() {
		return data3;
	}

	public ExecutionEvent setData3(Long data3) {
		this.data3 = data3;
		return this;
	}

	public String getData4() {
		return data4;
	}

	public ExecutionEvent setData4(String data4) {
		this.data4 = data4;
		return this;
	}

    public String getSequenceOrder() {
   		return sequenceOrder;
   	}

   	public ExecutionEvent setSequenceOrder(String sequenceOrder) {
   		this.sequenceOrder = sequenceOrder;
   		return this;
   	}

    public String getPath() {
        return path;
    }

    public ExecutionEvent setPath(String path) {
        this.path = path;
        return this;
    }

    public long getIndex() {
        return index;
    }

    public ExecutionEvent setIndex(long index) {
        this.index = index;
        return this;
    }

    public boolean isDebuggerMode() {
        return isDebuggerMode;
    }

    public ExecutionEvent setDebuggerMode(boolean debuggerMode) {
        isDebuggerMode = debuggerMode;
        return this;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ExecutionEvent)) return false;

		ExecutionEvent that = (ExecutionEvent) o;
		return new EqualsBuilder()
				.append(this.executionId, that.executionId)
				.append(this.type, that.type)
				.append(this.publishTime, that.publishTime)
                .append(this.sequenceOrder, that.sequenceOrder)
                .append(this.path, that.path)
				.append(this.data1, that.data1)
				.append(this.data2, that.data2)
				.append(this.data3, that.data3)
				.append(this.data4, that.data4)
                .append(this.isDebuggerMode, that.isDebuggerMode)
                .append(this.index, that.index)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(executionId)
				.append(type)
				.append(publishTime)
				.append(data1)
				.append(data2)
				.toHashCode();
	}
}
