package com.jeffmcknight.magneticmontecarlo;

public class ChunkMakerRunnable<E,N> implements Runnable {
	private E mElement;
	private WorkerInterface<E,N> mWorkerInterface;
	private ThreadMessager<N> mThreadMessager;
	
	public interface WorkerInterface<E,N>{
		N doCalculation(E e);
		float publishResult();
		int size(Object object);
	}

	public ChunkMakerRunnable(E element, WorkerInterface<E,N> workerInterface, ThreadMessager<N> threadMessager) {
		mElement = element;
		mWorkerInterface = workerInterface;
		mThreadMessager = threadMessager;
	}
	
	@Override
	public void run() {
		N result = mWorkerInterface.doCalculation(mElement);
		try {
			mThreadMessager.getLinkedBlockingQueue().put(result);
		} catch (InterruptedException e) {
			System.err.print("Queueing operation interrupted!!  " + e.getCause() );
			e.printStackTrace();
		}
	}

}
