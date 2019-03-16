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
	Lock communicatorLock;
	int message;
	int listeners;
	Condition2 listeningCondition;
	Condition2 speakingCondition;
	Integer buffer;
	boolean messageReady;
	
    /**
     * Allocate a new communicator.
     */
	/*
	 * Semaphore mutex = new Semaphore(1); Semaphore fullslots = new Semaphore(0);
	 * Semaphore emptyslots = new Semaphore(1); Queue<Integer> words = new
	 * LinkedList<>();
	 */
	/*
	 * Semaphore mutex = new Semaphore(1); Semaphore fullslots = new Semaphore(0);
	 * Semaphore emptyslots = new Semaphore(1); Queue<Integer> words = new
	 * LinkedList<>();
	 */
	
    public Communicator() {
    	communicatorLock = new Lock();
    	message = 0;
    	listeners = 0;
    	listeningCondition = new Condition2(communicatorLock);
    	speakingCondition = new Condition2(communicatorLock);
    	buffer = null;
    	messageReady = false;
    }

    /**
@ -35,11 +51,24 @@ public class Communicator {
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
		/*
		 * emptyslots.P(); mutex.P(); words.add(word); mutex.V(); fullslots.V();
		 */
    	communicatorLock.acquire();
    	
    	while(listeners == 0 || buffer == null) {
    		speakingCondition.sleep();
    	}
    	
    	message = word;
    	buffer = 1;
    	
    	listeningCondition.wake();
    	
    	
    	communicatorLock.release();
    	
		/*
		 * emptyslots.P(); mutex.P(); words.add(word); mutex.V(); fullslots.V();
		 */
		
    }

    /**
@ -49,11 +78,30 @@ public class Communicator {
     * @return	the integer transferred.
     */    
    public int listen() {
		/*
		 * fullslots.P(); mutex.P(); int word = words.remove(); emptyslots.V();
		 * System.out.println("here"); return word;
		 */
    	communicatorLock.acquire();
    	
    	listeners++;
    	
    	while(buffer == null) {
    		listeningCondition.sleep();
    		speakingCondition.wake();
    	}
    	
    	int msg = message;
    	buffer = null;
    	
    	listeners--;
    	
    	communicatorLock.release();
    	
    	return msg;
		
		/*
		 * fullslots.P(); mutex.P(); int word = words.remove(); emptyslots.V();
		 * 
		 * return word;
		 */
		 
    }
    
}