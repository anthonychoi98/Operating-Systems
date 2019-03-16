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
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        lock = new Lock();
        wordReady = false;
        okToSpeak = new Condition2(lock);
        okToListen = new Condition2(lock);
        okToConnect = new Condition2(lock);
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
        lock.acquire();

        // while a word is already ready
        while(wordReady) {
            okToSpeak.sleep();
        }

        // set the word to be spoken
        this.word = word;

        wordReady = true;

        // wake sleeping listener
        okToListen.wake();
        okToConnect.sleep();

        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
        lock.acquire();

        // while a word is not ready to be heard
        while(!wordReady) {
            okToListen.sleep();
        }

        // heard word, so set wordReady back to false
        wordReady = false;

        // wake a sleeping speaker
        okToSpeak.wake();
        okToConnect.wake();

        lock.release();

	    return word;
    }

    private Lock lock;      // lock
    private int word = 0;   // word to be spoken

    private boolean wordReady;   // check if word is ready

    private Condition2 okToSpeak;
    private Condition2 okToListen;
    private Condition2 okToConnect;
}
