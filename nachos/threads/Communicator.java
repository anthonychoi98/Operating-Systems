package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList; 
import java.util.Queue; 


/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
<<<<<<< HEAD
	/*
	 * Lock communicatorLock; int message; int listeners; Condition2
	 * listeningCondition; Condition2 speakingCondition; Integer buffer; boolean
	 * messageReady;
	 */
	
    /**
     * Allocate a new communicator.
     */
	
	 Semaphore mutex = new Semaphore(1); 
	 Semaphore fullslots = new Semaphore(0);
	 Semaphore emptyslots = new Semaphore(1); 
	 Queue<Integer> words = new LinkedList<>();
	 
	
    public Communicator() {
		/*
		 communicatorLock = new Lock(); message = 0; listeners = 0; listeningCondition
		 = new Condition2(communicatorLock); speakingCondition = new
		 * Condition2(communicatorLock); buffer = null; messageReady = false;
		 */
=======
    /**
     * Allocate a new communicator.
     */
	Semaphore mutex = new Semaphore(1);
	Semaphore fullslots = new Semaphore(0);
	Semaphore emptyslots = new Semaphore(1);
	Queue<Integer> words = new LinkedList<>();
	
    public Communicator() {
>>>>>>> parent of 05ee204... using locks for Communicator
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
<<<<<<< HEAD
		/*
		 * communicatorLock.acquire();
		 * 
		 * while(listeners == 0 || buffer == null) { speakingCondition.sleep(); }
		 * 
		 * message = word; buffer = 1;
		 * 
		 * listeningCondition.wake();
		 * 
		 * 
		 * communicatorLock.release();
		 */
    	
		
		emptyslots.P(); 
		mutex.P(); 
		words.add(word); 
		mutex.V(); 
		fullslots.V();
		 
		
=======
    	emptyslots.P();
    	mutex.P();
    	words.add(word);
    	mutex.V();
    	fullslots.V();
>>>>>>> parent of 05ee204... using locks for Communicator
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
<<<<<<< HEAD
		/*
		 * communicatorLock.acquire();
		 * 
		 * listeners++;
		 * 
		 * while(buffer == null) { listeningCondition.sleep(); speakingCondition.wake();
		 * }
		 * 
		 * int msg = message; buffer = null;
		 * 
		 * listeners--;
		 * 
		 * communicatorLock.release();
		 * 
		 * return msg;
		 */
		
		
		 fullslots.P(); 
		 mutex.P(); 
		 int word = words.remove(); 
		 emptyslots.V();
		  
		 return word;
		 
		 
=======
    	fullslots.P();
    	mutex.P();
    	int word = words.remove();
    	emptyslots.V();
    	System.out.println("here"); 
	return word;
>>>>>>> parent of 05ee204... using locks for Communicator
    }
}
