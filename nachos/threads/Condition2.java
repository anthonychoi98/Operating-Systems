package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

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

		sleepQ = new LinkedList<KThread>();
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The
	 * current thread must hold the associated lock. The thread will
	 * automatically reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		//release lock
		conditionLock.release();
		//disable interrupts
		boolean status = Machine.interrupt().disable();
		//add thread to sleep queue
		sleepQ.add(KThread.currentThread());
		//sleep
		KThread.sleep();
		//enable interrupts
		Machine.interrupt().restore(status);
		//reclaim lock
		conditionLock.acquire();
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		if(!sleepQ.isEmpty()) {
			//disable interrupts
			boolean status = Machine.interrupt().disable();
			//remove thread from sleep queue and wake up
			sleepQ.removeFirst().ready();
			//enable interrupts
			Machine.interrupt().restore(status);
		}
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	while(!waitQueue.isEmpty()){
        wake();
    }
    }

	private Lock conditionLock;
	private LinkedList<KThread> sleepQ;