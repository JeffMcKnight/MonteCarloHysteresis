package com.jeffmcknight.magneticmontecarlo;

public class AgregatorRunnable<N> implements Runnable {
	private int mChunkCount;
	private ThreadMessager<N> mThreadMessager;
	private Agregator<N> mAgregator;
	private N mAgregateChunk;
	
	public AgregatorRunnable (ThreadMessager<N> threadMessager, Agregator<N> agregator, int count){
		mThreadMessager = threadMessager;
		mAgregator = agregator;
		mChunkCount = count;
	}
	
	@Override
	public void run() {
		int chunksReceived = 0;
		while (!doneWaiting(chunksReceived)){
			handleMessage(mThreadMessager);
			chunksReceived++ ;
		}
		mAgregator.publishResult(mAgregateChunk);
	}


	private void handleMessage(ThreadMessager<N> threadMessager){
		try {
			N chunk = threadMessager.getLinkedBlockingQueue().take();
			mAgregateChunk = mAgregator.aggregateChunk(chunk);
		} catch (InterruptedException e) { }
	}
	
	private boolean doneWaiting(int receivedCount) {
		return (receivedCount<mChunkCount);
	}

	public interface Agregator<E> {
		E aggregateChunk(E e);
		void publishResult(E e) ;
	}
}
