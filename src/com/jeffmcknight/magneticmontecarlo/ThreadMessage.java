package com.jeffmcknight.magneticmontecarlo;

public class ThreadMessage<T> {
	public enum Signal{
		SIGNAL_CONTAINS_DATA, 
		SIGNAL_READY_FOR_DATA, 
		SIGNAL_END_OF_DATA}
	
	private Signal mSignal;
	private T mDataChunk;
	
	public ThreadMessage(Signal signal){
		mSignal = signal;
	}
	
	public ThreadMessage(Signal signal, T dataChunk){
		this(signal);
		mDataChunk = dataChunk;
	}
	
	public T getDataChunk() {
		return mDataChunk;
	}

	public void setDataChunk(T dataChunk) {
		mDataChunk = dataChunk;
	}

	public Signal getSignal() {
		return mSignal;
	}
	
	public void setSignal(Signal signal) {
		mSignal = signal;
	}

}
