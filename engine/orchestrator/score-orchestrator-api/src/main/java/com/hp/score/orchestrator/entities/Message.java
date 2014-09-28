package com.hp.score.orchestrator.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 12/3/13
 *
 * @author Dima Rassin
 */
//TODO: Add Javadoc
public interface Message extends Serializable{
	String getId();
	int getWeight();
	List<Message> shrink(List<Message> messages);
}
