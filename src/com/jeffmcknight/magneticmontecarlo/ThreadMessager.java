package com.jeffmcknight.magneticmontecarlo;

import java.util.concurrent.LinkedBlockingQueue;
/**
 * Message queue to communicate between threads.
 * @author jeffmcknight
 *
 * @param <N>
 */
public class ThreadMessager<N> {
	public static final int DEFAULT_CAPACITY = 5;
	private LinkedBlockingQueue<N> mLinkedBlockingQueue;
	
	public ThreadMessager (){
		mLinkedBlockingQueue = new LinkedBlockingQueue<N>(DEFAULT_CAPACITY);
	}
	
	public LinkedBlockingQueue<N> getLinkedBlockingQueue() {
		return mLinkedBlockingQueue;
	}
	
	public void setLinkedBlockingQueue(LinkedBlockingQueue<N> linkedBlockingQueue) {
		mLinkedBlockingQueue = linkedBlockingQueue;
	}
}
