package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
        this.conditionLock = conditionLock;

        this.waitQueue = new LinkedList<KThread>();
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        conditionLock.release(); //releases lock
        boolean state = Machine.interrupt().disable(); // disables interrupts
        waitQueue.push(KThread.currentThread()); // adds the currentThread to waitQueue
        KThread.sleep(); //Sleeps the thread
        Machine.interrupt().restore(state); // enables interrupts
        conditionLock.acquire(); // restores lock
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {

        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        if (!waitQueue.isEmpty()){ // as long as the waitQueue is not empty
            boolean state = Machine.interrupt().disable(); // disables interrupts
            waitQueue.getFirst().ready(); // ready the top of waitQueue
            waitQueue.pop(); // removes the first entry
            Machine.interrupt().restore(state); // restores interrupts.
        }
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        while(!waitQueue.isEmpty()){ // as long as there are threads in the waitQueue
            this.wake(); // call wake()
        }
    }

    private Lock conditionLock;
    private LinkedList<KThread> waitQueue;

}