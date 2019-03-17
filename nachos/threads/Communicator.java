package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
    private Lock comLock;      // communicator lock
    private int message;   // integer to be returned

    private boolean messageReady;   // flag to check if message is ready

    //conditions for speaking/listening/when they should connect
    private Condition2 speakCondition;
    private Condition2 listenCondition;
    private Condition2 connectCondition;
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        comLock = new Lock();
        messageReady = false;
        speakCondition = new Condition2(comLock);
        listenCondition = new Condition2(comLock);
        connectCondition = new Condition2(comLock);
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
    	//acquire lock, subsequently sleep other threads on ready queue
        comLock.acquire();

        // sleep speaking condition while message is ready
        while(messageReady) {
            speakCondition.sleep();
        }

        // store word as message
        message = word;
       
        //set messageReady flag to true
        messageReady = true;

        // wake sleeping listener
        listenCondition.wake();
        
        //not ok to connect after listeners listen
        connectCondition.sleep();

        //release lock
        comLock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
    	//acquire lock
        comLock.acquire();
        
        // sleep listening while message is not ready
        while(!messageReady) {
            listenCondition.sleep();
        }
        
        //set messageReady back to false
        messageReady = false;
        
        // wake a sleeping speaker and pair(connect)
        speakCondition.wake();
        connectCondition.wake();
        
        comLock.release();
        
        //return message from speaker
	    return message;
    }

}
