package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */

public class Communicator {
	private Lock commLock;
	private Condition2 listenerCond;
	private Condition2 speakerCond;
	// third cond variable helps us in linking speaker to listener
	private Condition2 currSpeakerCond;
	private int numListening;
	private int word = 0;
	private boolean msgReady;
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	commLock = new Lock();
    	speakerCond = new Condition2(commLock);
    	listenerCond = new Condition2(commLock);
    	currSpeakerCond = new Condition2(commLock);
    	numListening = 0;
    	msgReady = false;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	commLock.acquire();
    	
    	while (msgReady) {
    		speakerCond.sleep();
    	}
    	//modify message instance variable
    	this.word = word;
    	msgReady = true;
    	listenerCond.wake();
    	currSpeakerCond.sleep();
    	commLock.release();
    	return;
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	commLock.acquire();
    	numListening += 1;
    	while (!msgReady) {
    		listenerCond.sleep();
    	}
    	// receive the message
    	int rcvdMessage = this.word;
    	//reset variables for next listener/speaker combo
    	this.msgReady = false;
    	numListening -= 1;
    	
    	//wake certain threads (current speaker and general speakers)
    	currSpeakerCond.wake();
    	speakerCond.wake();
    	commLock.release();
    	return rcvdMessage;
    }
    
    
}
