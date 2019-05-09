package nachos.threads;

import nachos.machine.*;
/**
 * A KThread is a thread that can be used to execute Nachos kernel code. Nachos
 * allows multiple threads to run concurrently.
 *
 * To create a new thread of execution, first declare a class that implements
 * the <tt>Runnable</tt> interface. That class then implements the <tt>run</tt>
 * method. An instance of the class can then be allocated, passed as an
 * argument when creating <tt>KThread</tt>, and forked. For example, a thread
 * that computes pi could be written as follows:
 *
 * <p><blockquote><pre>
 * class PiRun implements Runnable {
 *     public void run() {
 *         // compute pi
 *         ...
 *     }
 * }
 * </pre></blockquote>
 * <p>The following code would then create a thread and start it running:
 *
 * <p><blockquote><pre>
 * PiRun p = new PiRun();
 * new KThread(p).fork();
 * </pre></blockquote>
 */
public class KThread {
    /**
     * Get the current thread.
     *
     * @return	the current thread.
     */
    public static KThread currentThread() {
		Lib.assertTrue(currentThread != null);
		return currentThread;
    }
    
    /**
     * Allocate a new <tt>KThread</tt>. If this is the first <tt>KThread</tt>,
     * create an idle thread as well.
     */
    public KThread() {
		if (currentThread != null) {
		    tcb = new TCB();
		}	    
		else {
			//means this is the main thread, kernel startup
		    readyQueue = ThreadedKernel.scheduler.newThreadQueue(false);
		    readyQueue.acquire(this);
	
		    currentThread = this;
		    tcb = TCB.currentTCB();
		    name = "main";
		    restoreState();
	
		    createIdleThread();
		}
    }
    public int getStatus() {
    	return this.status;
    }

    /**
     * Allocate a new KThread.
     *
     * @param	target	the object whose <tt>run</tt> method is called.
     */
    public KThread(Runnable target) {
		this();
		this.target = target;
    }

    /**
     * Set the target of this thread.
     *
     * @param	target	the object whose <tt>run</tt> method is called.
     * @return	this thread.
     */
    public KThread setTarget(Runnable target) {
		Lib.assertTrue(status == statusNew);
		
		this.target = target;
		return this;
    }

    /**
     * Set the name of this thread. This name is used for debugging purposes
     * only.
     *
     * @param	name	the name to give to this thread.
     * @return	this thread.
     */
    public KThread setName(String name) {
		this.name = name;
		return this;
    }

    /**
     * Get the name of this thread. This name is used for debugging purposes
     * only.
     *
     * @return	the name given to this thread.
     */     
    public String getName() {
    	return name;
    }

    /**
     * Get the full name of this thread. This includes its name along with its
     * numerical ID. This name is used for debugging purposes only.
     *
     * @return	the full name given to this thread.
     */
    public String toString() {
    	return (name + " (#" + id + ")");
    }

    /**
     * Deterministically and consistently compare this thread to another
     * thread.
     */
    public int compareTo(Object o) {
		KThread thread = (KThread) o;
	
		if (id < thread.id)
		    return -1;
		else if (id > thread.id)
		    return 1;
		else
		    return 0;
    }

    /**
     * Causes this thread to begin execution. The result is that two threads
     * are running concurrently: the current thread (which returns from the
     * call to the <tt>fork</tt> method) and the other thread (which executes
     * its target's <tt>run</tt> method).
     */
    public void fork() {
		Lib.assertTrue(status == statusNew);
		Lib.assertTrue(target != null);
		
		Lib.debug(dbgThread,
			  "Forking thread: " + toString() + " Runnable: " + target);
	
		boolean intStatus = Machine.interrupt().disable();
	
		tcb.start(new Runnable() {
			public void run() {
			    runThread();
			}
		});
	
		ready();
		
		Machine.interrupt().restore(intStatus);
    }

    private void runThread() {
		begin();
		target.run();
		finish();
    }

    private void begin() {
		Lib.debug(dbgThread, "Beginning thread: " + toString());
		
		Lib.assertTrue(this == currentThread);
	
		restoreState();
	
		Machine.interrupt().enable();
    }

    /**
     * Finish the current thread and schedule it to be destroyed when it is
     * safe to do so. This method is automatically called when a thread's
     * <tt>run</tt> method returns, but it may also be called directly.
     *
     * The current thread cannot be immediately destroyed because its stack and
     * other execution state are still in use. Instead, this thread will be
     * destroyed automatically by the next thread to run, when it is safe to
     * delete this thread.
     */
    //automatically called from machine
    public static void finish() {
		Lib.debug(dbgThread, "Finishing thread: " + currentThread.toString());
		
		Machine.interrupt().disable();
<<<<<<< Updated upstream
		
		//wake up other threads that were waiting on this thread to be finished.
		ThreadQueue joiningQueue = currentThread.getJoinQueue();
		if(joiningQueue != null) {
			KThread nextJoinedThread = joiningQueue.nextThread();
			while ( nextJoinedThread != null ) {
			    nextJoinedThread.ready();
			    joiningQueue.acquire(nextJoinedThread);
			    nextJoinedThread = joiningQueue.nextThread();
			}
		}
	
		
	
=======
		/*--> inocrrect REDO
>>>>>>> Stashed changes
		Machine.autoGrader().finishingCurrentThread();
	
		Lib.assertTrue(toBeDestroyed == null);
		toBeDestroyed = currentThread;
	
	
		currentThread.status = statusFinished;
		
		sleep();
<<<<<<< Updated upstream
    }
    
    public ThreadQueue getJoinQueue() {
    	return this.joiningQueue;
=======
	}*/
		
		//we need to ensure that the other threads will be waiting for the thread
		//to be finished before joining or gaining access to resource
		ThreadQueue joiningQueue = currentThread.getJoinQueue();
		if(joiningQueue != null) {
			KThread nextJoinedThread = joiningQueue.nextThread();
			// 	while ((waitThread = currentThread.waitForJoin.nextThread()) != null) {
			while ( nextJoinedThread != null ) {
			    nextJoinedThread.ready();
			    joiningQueue.acquire(nextJoinedThread);
			    nextJoinedThread = joiningQueue.nextThread();
			}
			//sleep();
		}
	
		
	
		Machine.autoGrader().finishingCurrentThread();
	
		Lib.assertTrue(toBeDestroyed == null); //if there is nothing to be destroyed
		toBeDestroyed = currentThread;
		//then the next thread will need to be after gaining access to resource
	
		currentThread.status = statusFinished;	//thread is finished
		
		sleep();	//call sleep method
    }
    
    //create getJoinQueue() for ThreadQueue class
    public ThreadQueue getJoinQueue() {
    	return this.joiningQueue;	//return thread from joiningQueue
>>>>>>> Stashed changes
    }

    /**
     * Relinquish the CPU if any other thread is ready to run. If so, put the
     * current thread on the ready queue, so that it will eventually be
     * rescheuled.
     *
     * <p>
     * Returns immediately if no other thread is ready to run. Otherwise
     * returns when the current thread is chosen to run again by
     * <tt>readyQueue.nextThread()</tt>.
     *
     * <p>
     * Interrupts are disabled, so that the current thread can atomically add
     * itself to the ready queue and switch to the next thread. On return,
     * restores interrupts to the previous state, in case <tt>yield()</tt> was
     * called with interrupts disabled.
     */
    public static void yield() {
		Lib.debug(dbgThread, "Yielding thread: " + currentThread.toString());
		
		Lib.assertTrue(currentThread.status == statusRunning);
		
		boolean intStatus = Machine.interrupt().disable();
	
		currentThread.ready();
	
		runNextThread();
		
		Machine.interrupt().restore(intStatus);
    }

    /**
     * Relinquish the CPU, because the current thread has either finished or it
     * is blocked. This thread must be the current thread.
     *
     * <p>
     * If the current thread is blocked (on a synchronization primitive, i.e.
     * a <tt>Semaphore</tt>, <tt>Lock</tt>, or <tt>Condition</tt>), eventually
     * some thread will wake this thread up, putting it back on the ready queue
     * so that it can be rescheduled. Otherwise, <tt>finish()</tt> should have
     * scheduled this thread to be destroyed by the next thread to run.
     */
    public static void sleep() {
		Lib.debug(dbgThread, "Sleeping thread: " + currentThread.toString());
		
		Lib.assertTrue(Machine.interrupt().disabled());
<<<<<<< Updated upstream
	
		if (currentThread.status != statusFinished)
		    currentThread.status = statusBlocked;
	
=======
	//Lib.assertTrue(status != statusReady);
		
		//status = statusReady; 
		
		/*--> REDO
		 * 	if (this != idleThread)
			readyQueue.waitForAccess(this);

		Machine.autoGrader().readyThread(this);
	}*/
		if (currentThread.status != statusFinished) //if not finished
		    currentThread.status = statusBlocked; //then don't allow next thread access yet
	//run
>>>>>>> Stashed changes
		runNextThread();
    }

    /**
     * Moves this thread to the ready state and adds this to the scheduler's
     * ready queue.
     */
    public void ready() {
		Lib.debug(dbgThread, "Ready thread: " + toString());
		
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(status != statusReady);
		
		status = statusReady;
		if (this != idleThread)
		    readyQueue.waitForAccess(this); // adds to ready queue
		
		Machine.autoGrader().readyThread(this);
    }

    /**
     * Waits for this thread to finish. If this thread is already finished,
     * return immediately. This method must only be called once; the second
     * call is not guaranteed to return. This thread must not be the current
     * thread.
     */
    public void join() {
    	
    	Lib.assertTrue(this != currentThread);
		Lib.debug(dbgThread, "Joining to thread: " + toString());
<<<<<<< Updated upstream
		
		
		Machine.interrupt().disable();
       if(joiningQueue == null){
        	joiningQueue = ThreadedKernel.scheduler.newThreadQueue(true);
    	}
	    
=======
		// TASK 1 --> REDO
	//			boolean intStatus = Machine.interrupt().disable();
		
		Machine.interrupt().disable();
		// so the current thread will wait for this thread
				// not need to wait if the thread is already dead
       if(joiningQueue == null){
        	joiningQueue = ThreadedKernel.scheduler.newThreadQueue(true);
    	}
	    /*
	     * 	if (status != statusFinished) {
			waitForJoin.waitForAccess(currentThread);
			KThread.sleep();
		}

		Machine.interrupt().restore(intStatus);
	}*/
>>>>>>> Stashed changes
        if (currentThread != this && status != statusFinished) {
       	joiningQueue.acquire(this);
        	joiningQueue.waitForAccess(currentThread);
        	currentThread.sleep();
    	}
    	Machine.interrupt().enable();

    }

    /**
     * Create the idle thread. Whenever there are no threads ready to be run,
     * and <tt>runNextThread()</tt> is called, it will run the idle thread. The
     * idle thread must never block, and it will only be allowed to run when
     * all other threads are blocked.
     *
     * <p>
     * Note that <tt>ready()</tt> never adds the idle thread to the ready set.
     */
    private static void createIdleThread() {
		Lib.assertTrue(idleThread == null);
		
		idleThread = new KThread(new Runnable() {
<<<<<<< Updated upstream
		    public void run() { while (true) yield(); }
=======
		    public void run() { 
		    	while (true) 
		    		yield(); 
		    	}
>>>>>>> Stashed changes
		});
		
		idleThread.setName("idle");
	
		Machine.autoGrader().setIdleThread(idleThread);
		
		idleThread.fork();
    }
    
    /**
     * Determine the next thread to run, then dispatch the CPU to the thread
     * using <tt>run()</tt>.
     */
    private static void runNextThread() {
		KThread nextThread = readyQueue.nextThread();
		if (nextThread == null)
		    nextThread = idleThread;
	
		nextThread.run();
    }

    /**
     * Dispatch the CPU to this thread. Save the state of the current thread,
     * switch to the new thread by calling <tt>TCB.contextSwitch()</tt>, and
     * load the state of the new thread. The new thread becomes the current
     * thread.
     *
     * <p>
     * If the new thread and the old thread are the same, this method must
     * still call <tt>saveState()</tt>, <tt>contextSwitch()</tt>, and
     * <tt>restoreState()</tt>.
     *
     * <p>
     * The state of the previously running thread must already have been
     * changed from running to blocked or ready (depending on whether the
     * thread is sleeping or yielding).
     *
     * @param	finishing	<tt>true</tt> if the current thread is
     *				finished, and should be destroyed by the new
     *				thread.
     */
    private void run() {
		Lib.assertTrue(Machine.interrupt().disabled());
	
		Machine.yield();
	
		currentThread.saveState();
	
		Lib.debug(dbgThread, "Switching from: " + currentThread.toString()
			  + " to: " + toString());
	
		currentThread = this;
	
		tcb.contextSwitch();
	
		currentThread.restoreState();
    }

    /**
     * Prepare this thread to be run. Set <tt>status</tt> to
     * <tt>statusRunning</tt> and check <tt>toBeDestroyed</tt>.
     */
    protected void restoreState() {
		Lib.debug(dbgThread, "Running thread: " + currentThread.toString());
		
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(this == currentThread);
		Lib.assertTrue(tcb == TCB.currentTCB());
	
		Machine.autoGrader().runningThread(this);
		
		status = statusRunning;
	
		if (toBeDestroyed != null) {
		    toBeDestroyed.tcb.destroy();
		    toBeDestroyed.tcb = null;
		    toBeDestroyed = null;
		}
    }

    /**
     * Prepare this thread to give up the processor. Kernel threads do not
     * need to do anything here.
     */
    protected void saveState() {
		Lib.assertTrue(Machine.interrupt().disabled());
		Lib.assertTrue(this == currentThread);
    }

    private static class PingTest implements Runnable {
		PingTest(int which) {
		    this.which = which;
		}
		
<<<<<<< Updated upstream
=======
		/*
		 * @Override
>>>>>>> Stashed changes
		public void run() {
		    for (int i=0; i<5; i++) {
			System.out.println("*** thread " + which + " looped "
					   + i + " times");
			currentThread.yield();
		
		    }
		}
		private int which;
<<<<<<< Updated upstream
=======
	}*/
		public void run() {
		    for (int i=0; i<5; i++) {
			System.out.println("*** thread " + which + " looped "
					   + i + " times");
			currentThread.yield();
		
		    }
		}
		private int which;
>>>>>>> Stashed changes
    }

    /**
     * Tests whether this module is working.
     */
    public static void selfTest() {
		Lib.debug(dbgThread, "Enter KThread.selfTest");
		
		KThread thread1 = new KThread(new PingTest(1)).setName("1st forked thread");
		
		thread1.fork();
		
		Machine.interrupt().disable();
		readyQueue.print();
		Machine.interrupt().enable();
<<<<<<< Updated upstream
		
		thread1.join(); // prevents alternating between parent thread, and forked thread from parent.
		
=======
		//need to stop alternating the parent/fork thread
		thread1.join(); 
>>>>>>> Stashed changes
		Machine.interrupt().disable();
		readyQueue.print();
		Machine.interrupt().enable();
		
		//NOTE: machine interrupts allow us to share the CPU?
		
//		Machine.interrupt().disable();
<<<<<<< Updated upstream
//		readyQueue.print();
//		Machine.interrupt().enable();
		
		new PingTest(0).run(); // called from main thread that was initialized by Kernel.
		
		KThread thread2 = new KThread(new PingTest(2)).setName("2nd forked thread");
		thread2.fork();
		//thread2.join();
		
		KThread thread3 = new KThread(new PingTest(3)).setName("3rd forked thread");
		thread3.fork();
		//thread3.join();
		
		KThread thread4 = new KThread(new PingTest(4)).setName("4th forked thread");
		thread4.fork();
		//thread4.join();
		
		KThread thread5 = new KThread(new PingTest(5)).setName("5th forked thread");
		thread5.fork();

		KThread thread6 = new KThread(new PingTest(6)).setName("6th forked thread");
		thread6.fork();
		
		
=======
		//call PingTest from main thread above
		//I believe it was implemented in Kernel
		new PingTest(0).run(); 
		//join thread 2
		KThread thread2 = new KThread(new PingTest(2)).setName("2nd forked thread");
		thread2.fork();
		
		
		//join thread 3
		KThread thread3 = new KThread(new PingTest(3)).setName("3rd forked thread");
		thread3.fork();
		
		//join thread 4
		KThread thread4 = new KThread(new PingTest(4)).setName("4th forked thread");
		thread4.fork();
		
		//join thread 5
		KThread thread5 = new KThread(new PingTest(5)).setName("5th forked thread");
		thread5.fork();
		
		//join thread 6
		KThread thread6 = new KThread(new PingTest(6)).setName("6th forked thread");
		thread6.fork();
		
		/**
		 * Tests whether this module is working.
		 */
	/*	public static void selfTest() {
			Lib.debug(dbgThread, "Enter KThread.selfTest");

			new KThread(new PingTest(1)).setName("forked thread").fork();
			new PingTest(0).run();
		}
		*/
>>>>>>> Stashed changes
		Machine.interrupt().disable();
		readyQueue.print();
		Machine.interrupt().enable();
		
	}

    private static final char dbgThread = 't';

    /**
     * Additional state used by schedulers.
     *
     * @see	nachos.threads.PriorityScheduler.ThreadState
     */
    public Object schedulingState = null;

    private static final int statusNew = 0;
    private static final int statusReady = 1;
    private static final int statusRunning = 2;
    private static final int statusBlocked = 3;
    private static final int statusFinished = 4;

    /**
     * The status of this thread. A thread can either be new (not yet forked),
     * ready (on the ready queue but not running), running, or blocked (not
     * on the ready queue and not running).
     */
    private int status = statusNew;
    private String name = "(unnamed thread)";
    private Runnable target;
    private TCB tcb;

<<<<<<< Updated upstream
    /**
     * Unique identifer for this thread. Used to deterministically compare
     * threads.
     */
=======
	/**
	 * Initialize some unique variable to help identifiers to assist with the
	 * implementations and uses of the threads. We need to do this to help compare threads
	 */
	
>>>>>>> Stashed changes
    private int id = numCreated++;
    /** Number of times the KThread constructor was called. */
    private static int numCreated = 0;

<<<<<<< Updated upstream
    private static ThreadQueue readyQueue = null;
    private static KThread currentThread = null;
    private static KThread toBeDestroyed = null;
    private static KThread idleThread = null;
    private ThreadQueue joiningQueue  = null;
}
=======
    /*public PriorityQueue waitForJoin() {
	// TODO Auto-generated method stub
	return null;
	}*/
    private static ThreadQueue readyQueue = null;
    private static KThread currentThread = null;
  //private static boolean isJoined = false; //added as wellf
    private static KThread toBeDestroyed = null;
    private static KThread idleThread = null;
//	private static boolean joinQueue = false;
    private ThreadQueue joiningQueue  = null;
	// TASK 1.1
//	ThreadQueue waitForJoin = ThreadedKernel.scheduler.newThreadQueue(true);
	//ThreadQueue waitForJoin = ThreadedKernel.scheduler.newThreadQueue(false);
	/*public PriorityQueue getJoinQueue() {
		// TODO Auto-generated method stub
		return null;
	}*/
}
>>>>>>> Stashed changes
