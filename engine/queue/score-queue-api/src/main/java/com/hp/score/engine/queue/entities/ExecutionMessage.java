package com.hp.score.engine.queue.entities;


import com.hp.oo.engine.node.entities.WorkerNode;
import com.hp.score.orchestrator.entities.Message;
import org.apache.commons.lang.builder.EqualsBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * User: Amit Levin
 * Date: 10/09/12
 * Time: 11:11
 */
public class ExecutionMessage implements Message, Cloneable {

    private static final long serialVersionUID = 3523623124812765964L;

	public static final long EMPTY_EXEC_STATE_ID = -1L;
	public static final String EMPTY_WORKER = "EMPTY";

	private long execStateId;
	private String workerId;
	private String workerGroup;
	private ExecStatus status;
	private Payload payload;
	private int msgSeqId;
	private String msgId;
    private Date createDate;

	private transient String workerKey;

	public ExecutionMessage() {
		execStateId = EMPTY_EXEC_STATE_ID;
		workerId = ExecutionMessage.EMPTY_WORKER;
		workerGroup = "";
		status = ExecStatus.INIT;
		payload = null;
		msgSeqId = -1;
		msgId = "";
        createDate = null;
	}

    public ExecutionMessage(String executionId, Payload payload) {
        this.execStateId = ExecutionMessage.EMPTY_EXEC_STATE_ID;
        this.workerId = ExecutionMessage.EMPTY_WORKER;
        this.workerGroup = WorkerNode.DEFAULT_WORKER_GROUPS[0];
        this.msgId = String.valueOf(executionId);
        this.status = ExecStatus.PENDING;
        this.payload = payload;
        this.msgSeqId = 0;
    }

    public ExecutionMessage(long execStateId,
    	                        String workerId,
    	                        String workerGroup,
    	                        String msgId,
    	                        ExecStatus status,
    	                        Payload payload,
    	                        int msgSeqId,
                                Date createDate) {
    		this.execStateId = execStateId;
    		this.workerId = workerId;
    		this.workerGroup = workerGroup;
    		this.msgId = msgId;
    		this.status = status;
    		this.payload = payload;
    		this.msgSeqId = msgSeqId;
            this.createDate = createDate;
   }

	public ExecutionMessage(long execStateId,
	                        String workerId,
	                        String workerGroup,
	                        String msgId,
	                        ExecStatus status,
	                        Payload payload,
	                        int msgSeqId) {
		this.execStateId = execStateId;
		this.workerId = workerId;
		this.workerGroup = workerGroup;
		this.msgId = msgId;
		this.status = status;
		this.payload = payload;
		this.msgSeqId = msgSeqId;
	}

    public Date getCreateDate() {
       return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

	public long getExecStateId() {
		return execStateId;
	}

	public void setExecStateId(long id) {
		this.execStateId = id;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msg_id) {
		this.msgId = msg_id;
	}

	public String getWorkerId() {
		return workerId;
	}

	public String getWorkerGroup() {
		return workerGroup;
	}

	@JsonIgnore
	public String getMsgUniqueId() {
		return msgId + ":" + msgSeqId;
	}

	public ExecStatus getStatus() {
		return status;
	}

	public Payload getPayload() {
		return payload;
	}

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public int getMsgSeqId() {
		return msgSeqId;
	}

	public void setStatus(ExecStatus status) {
		this.status = status;
	}

	synchronized public void incMsgSeqId() {
		this.msgSeqId = msgSeqId + 1;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	@Override
	public int getWeight() {
		return 1;
	}

	@Override
	public String getId() {
		return workerKey;
	}

	public String getWorkerKey() {
		return workerKey;
	}

	public ExecutionMessage setWorkerKey(String workerKey) {
		this.workerKey = workerKey;
		return this;
	}

	@Override
	public List<Message> shrink(List<Message> messages) {
		if (messages.size() > 2 && messages.get(0) instanceof ExecutionMessage) {
			if (((ExecutionMessage)messages.get(0)).getStatus().equals(ExecStatus.IN_PROGRESS)) {
				return Arrays.asList(messages.get(1), messages.get(messages.size()-1));
			} else {
				return Arrays.asList(messages.get(0), messages.get(messages.size()-1));
			}
		} else {
			return messages;
		}
	}

	@SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
	@Override
	public Object clone() {
		try {
			ExecutionMessage cloned = (ExecutionMessage) super.clone();
			if (payload != null) cloned.payload = (Payload) (payload.clone());
			return cloned;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException("Failed to clone message", ex);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ExecutionMessage that = (ExecutionMessage) o;
		return new EqualsBuilder()
				.append(this.execStateId, that.execStateId)
				.append(this.msgSeqId, that.msgSeqId)
				.append(this.msgId, that.msgId)
				.append(this.payload, that.payload)
				.append(this.status, that.status)
				.append(this.workerGroup, that.workerGroup)
				.append(this.workerId, that.workerId)
                .append(this.createDate, that.createDate)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				workerId,
				workerGroup,
				msgId,
				status,
				payload,
				msgSeqId,
				execStateId,
                createDate
		);
	}
}
