package nachos.threads;

import nachos.machine.*;
import nachos.threads.PriorityScheduler.ThreadState;


import java.util.TreeSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
//import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.Comparator;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve  priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
class threadY{
	ThreadState thread;
	int time;
	int effectivePriority;
	KThread kthread;
}
public class PriorityScheduler extends Scheduler {//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<PRIORITY SCHEDULER<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }

    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());

	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());

	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());

	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);

	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();

	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();

	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<PRIORITY QUEUE<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    	//public SortedSet<ThreadState> ready_queue = new TreeSet<ThreadState>();
    	//public LinkedList<ThreadState> wait_queue = new LinkedList<ThreadState>();
    	//public LinkedList<KThread> ready_queue = new LinkedList<KThread>();

    	   java.util.PriorityQueue<threadY> waitQ = new java.util.PriorityQueue<threadY>(1000, new Comparator<threadY>(){
    		@Override
    		public int compare(threadY thread1, threadY thread2){
    			if(thread1.effectivePriority > thread2.effectivePriority) return -1;//make switch, thread1 should be before thread2
    		    else if(thread1.effectivePriority < thread2.effectivePriority) return 1; //correct spots thread1 should be after thread2
    		    else { //priorities equal
    		    			if(thread1.time > thread2.time) return 1; //thread with longest time goes up in priority
    		    			else if(thread1.time < thread2.time) return -1; //in this sense, longest time will be the smallest integer so when we have a tie in effectivePriority, we want smallest times at top
    		    			else {}
    		    			return 0;

    		    }

    	}
    		});

    	   PriorityQueue(boolean transferPriority){
    		   this.transferPriority = transferPriority;
    	   }

    	   //check if waitQ contains threadY Object
    	   boolean contains(threadY threads) {
    		   if(threads == null) return false;
    			Iterator<threadY> itr = waitQ.iterator();

    			while(itr.hasNext()) {

    				if(itr.next().thread == threads.thread && itr.next().kthread == threads.kthread) {

    					return true;
    				}
    			}
    			return false;
    		}
	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    Lib.assertTrue(thread != null);
	    /**threadY newThread = new threadY();
	    newThread.thread = getThreadState(thread); //get current threadstate to associated thread
	    newThread.time =  this.time;
	    newThread.effectivePriority = newThread.thread.getEffectivePriority();
	    newThread.kthread =  thread;
	    Lib.assertTrue(!contains(newThread)); //make sure thread not already in
	    waitQ.add(newThread);


	    this.time++;//update time

	    updateMaxPriority();**/
	    getThreadState(thread).waitForAccess(this);
	}
	/**void updateMaxPriority() {//get maxPriority of threadStates in waitQ

			if(!waitQ.isEmpty()) {
				// TODO
//				this.maxPriorityinQ = waitQ.peek().effectivePriority;
				//this.maxPriorityinQ = waitQ.peek().thread.getNewEffectivePriority();
				maxPriorityinQ = findMaxEffectivePriority();
				if(currentThreadState != null) {

					this.currentThreadState.updateMaxEffective();
				}
			}else { //empty Q reset max
				this.maxPriorityinQ = 0;
				this.time = 0;
				this.currentThreadState = null;
			}

	}**/
	/**int findMaxEffectivePriority() {
		if(!transferPriority) { // no need to transfer priority return to minimm priority
			return priorityMinimum;

		}
		if(updateMaxFlag) {
			maxPriorityinQ = priorityMinimum;
			Iterator<threadY> itr = waitQ.iterator();
			while(itr.hasNext()) {
				int newPriority =itr.next().thread.getEffectivePriority();
				if(newPriority > maxPriorityinQ) {
					maxPriorityinQ = newPriority;
				}


			}
			updateMaxFlag = false;
		}
		return maxPriorityinQ;
	}**/
	// TODO
	/**public void updatePriorities(ThreadState stateThread) {
		Lib.assertTrue(contains(findThreadY(stateThread)));
		//threadY updateThread = findThreadY(stateThread);

		//waitQ.remove(findThreadY(stateThread));
		/**if(stateThread.getEffectivePriority() != updateThread.effectivePriority) {
			waitQ.remove(findThreadY(stateThread));
			updateThread.effectivePriority = stateThread.getEffectivePriority();
			updateThread.time = findThreadY(stateThread).time;
			updateThread.thread = stateThread;
			updateThread.kthread = stateThread.thread;
			waitQ.add(updateThread);
			/**threadY newThread = new threadY();
			newThread.thread = stateThread;
			newThread.kthread = stateThread.thread;
			newThread.time = updateThread.time;
			newThread.effectivePriority = stateThread.getEffectivePriority();

			//waitQ.add(newThread);
			updateMaxPriority();
		}


	}**/
	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());

	    ThreadState newThread = getThreadState(thread);
	    //newThread.thread = getThreadState(thread);
	    //newThread.effectivePriority = newThread.thread.getEffectivePriority();
	    //newThread.kthread = thread;
	    //if there is a current resource and Thread needs to transferPriority then we release resource trying to be used
	    if(this.currentThreadState != null && this.transferPriority) { //release resource
	    		this.currentThreadState.releaseResource(this);
	    }

	    if(newThread != null) {
	    		this.currentThreadState = newThread;
		    newThread.acquire(this);
		    //getThreadState(thread).acquire(this);
	    		//remove from waitQ
	    		//waitQ.remove(newThread);
	    		//updateMaxPriority(); //update current max
	    		//this.currentThreadState = newThread.thread; //current thread state

	    }

	    //newThread.thread.acquire(this); //acquire next resource for threadState of thread

	   //

	}
	threadY findThreadY(ThreadState thread) {//return threaY object in waitQ if found
		Iterator<threadY> itr = waitQ.iterator();

		while(itr.hasNext()) {

			threadY threadObj = itr.next();
			if(threadObj.thread == thread) {

				return threadObj;
			}
		}
		return null;
	}
	public KThread nextThread() {
		//implement me
	    Lib.assertTrue(Machine.interrupt().disabled());
	    ThreadState threadST = pickNextThread();
	    threadY removeResource = findThreadY(threadST);//find threadY object for threadST so we can get the assoicated KThread

	    if(!waitQ.isEmpty()) {
	    		if(this.currentThreadState != null && this.transferPriority) { //if currentThreadState and transferPriority is true meaning we must transferPriority and release resource is my guess
	    			this.currentThreadState.releaseResource(this);//releaseResource of PriorityQueue for threadState
	    		}


	    		if(threadST != null) {
	    			 //next threadstate
	    			//threadY removeResource = findThreadY(threadST);//find threadY object for threadST so we can get the assoicated KThread

	    				waitQ.remove(removeResource); //not waiting for it no more
	    				//updateMaxPriority();
	    				getThreadState(removeResource.kthread).acquire(this); //add resource to associated thread
	    				//acquire(threadST.thread);

	    		}else {
	    			//threadST == null thus we reset currentThreadState to null
	    			this.currentThreadState = null;
	    			return null;

	    		}

	    }


	    if (threadST == null) {
	    	return null;
	    }
	    return threadST.thread;

	    /**ThreadState nextThread = null;
	    if(!ready_queue.isEmpty()) {
	    	nextThread = ready_queue.first();
	    	ready_queue.remove(ready_queue.first());
	    }

	    return nextThread.thread;**/
	}
	public int getEffectivePriority() {
		if(!transferPriority) return priorityMinimum;

		if(updateMaxFlag) {
			maxPriorityinQ = priorityMinimum;
			Iterator<threadY> itr = waitQ.iterator();

			while(itr.hasNext()) {
				threadY newThread = itr.next();
				int priority = newThread.thread.getEffectivePriority();
				if(priority > maxPriorityinQ) {
					maxPriorityinQ = priority;
				}
			}
			updateMaxFlag = false;

		}
		return maxPriorityinQ;


	}
	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {
		//implement me
		if(waitQ.isEmpty()) {
			time = 0;
			return null;
			}

		ThreadState maxStatePri = null;

		Iterator<threadY> itr = waitQ.iterator();
		while(itr.hasNext()) {
			threadY newThread = itr.next();
			int pri = newThread.thread.getEffectivePriority();
			if(maxStatePri == null || pri > maxStatePri.getEffectivePriority()) {
				maxStatePri = newThread.thread;
			}
		}
		return maxStatePri;

		/**
		ThreadState nextThread = null;
	    if(!ready_queue.isEmpty()) {
	    	nextThread = ready_queue.first();
	    }

	    return nextThread;
	   **/
	}
	// TODO
	int getPriorityMax() {
		//updateMaxPriority();
		return getEffectivePriority();
	}
	public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	    Iterator<threadY> itr = waitQ.iterator();
	    while(itr.hasNext()) {
	    threadY thread = itr.next();

	    	System.out.println("KThread: " + 	thread.kthread + " ThreadState: "+ thread.thread + " Time: " + thread.time + " Priority: " + thread.effectivePriority);
	    }
	}

	void setFlag() {
		if(!transferPriority) { //if false no need to transfer priority
			return;
		}
		updateMaxFlag = true;

		if(currentThreadState != null) {
			currentThreadState.setFlag();
		}


	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
	public boolean transferPriority;
	private ThreadState currentThreadState =  null;
	int maxPriorityinQ = 0;//effectivePriority of highest priority in waitQ cached
	int time = 0;

	boolean updateMaxFlag;
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<THREAD STATE<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;

	    setEffectivePriority(priorityDefault);
	    setPriority(priorityDefault);

	}

	/**
	 * Return the priority of the associated thread.
	 *
	 * @return	the priority of the associated thread.
	 */
	public int getPriority() {
	    return priority;
	}

	/**
	 * Return the effective priority of the associated thread.
	 *
	 * @return	the effective priority of the associated thread.
	 */
	//mine
	/**public int getNewEffectivePriority() {

		// TODO
	    int max_priority = getPriority();
	    if(!resources_waiting_for.isEmpty()) {
	    	for(PriorityQueue queues : resources_waiting_for) {
	    		//System.out.println("max_priority: " + max_priority + ", queues.getPriorityMax(): " + queues.getPriorityMax());
	    		if(max_priority < queues.getPriorityMax()) {
	    			max_priority = queues.getPriorityMax();
	    		}
	    	}
	    }
	    for (PriorityQueue queue : resources_waiting_for) {
	    	//queue.print();
	    }
	   // System.out.println("max prioirty is " + max_priority);
	    return max_priority;
	}**/

	// TODO
	/*public int getEffectivePriority() {
		int effective = this.effectivePriority;
		if(updateMaxFlag) {
			effective = getNewEffectivePriority();
		}
		return effective;
		//return this.effectivePriority;
		//return this.getNewEffectivePriority();
	}
	*/
	public int getEffectivePriority() {
		if(!updateMaxFlag) return this.priority;

		//updateMaxFlag == true
		int max = this.priority;
		Iterator<PriorityQueue> itr = current_resources.iterator();
		while(itr.hasNext()) {
			PriorityQueue Q = itr.next();
			int Qeffective = Q.getEffectivePriority();
			if(max < Qeffective) {
				max = Qeffective;
			}
		}
		return max;
	}

	//mine
	/**public void updateMaxEffective() {
		int newMaxEffective = getNewEffectivePriority();
		if(newMaxEffective != effectivePriority) {
			effectivePriority = newMaxEffective;
			for(PriorityQueue Q: resources_waiting_for) {
				Q.updatePriorities(this);//update priorites
			}
		}
	}**/
	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {


	    this.priority = priority;
	    //updateMaxEffective();

	    // implement me
	    //update flag
	    setFlag();
	}
	void setEffectivePriority(int effectivePriority) {
		this.effectivePriority = effectivePriority;
		//updateMaxEffective();
	}

	/**
	 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
	 * the associated thread) is invoked on the specified priority queue.
	 * The associated thread is therefore waiting for access to the
	 * resource guarded by <tt>waitQueue</tt>. This method is only called
	 * if the associated thread cannot immediately obtain access.
	 *
	 * @param	waitQueue	the queue that the associated thread is
	 *				now waiting on.
	 *
	 * @see	nachos.threads.ThreadQueue#waitForAccess
	 */
	public void waitForAccess(PriorityQueue waitQueue) {
	    // implement me
		//Lib.assertTrue(Machine.interrupt().disable());
		//Lib.assertTrue(waitQueue.waitQ.contai);
		//Lib.assertTrue(waitQueue.findThreadY(getThreadState(thread)) != null);
		resources_waiting_for.add(waitQueue); //resources we want

		threadY newThread = new threadY();
		newThread.thread = this;
		newThread.kthread = this.thread;
		newThread.effectivePriority = this.effectivePriority;
		newThread.time = this.time;
		time++;
		waitQueue.waitQ.add(newThread);

		waitQueue.setFlag();//set flag of priorityQueue

		this.mostRecentResource = waitQueue;



		if(current_resources.indexOf(waitQueue) != -1) { //take out of current resources
			current_resources.remove(current_resources.indexOf(waitQueue) );
			waitQueue.currentThreadState = null;


		}
	}

	void resetRecentResource() {
		mostRecentResource = null;
	}
	ThreadQueue getRecentResource() {

		return mostRecentResource;
	}
	/**
	 * Called when the associated thread has acquired access to whatever is
	 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
	 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
	 * <tt>thread</tt> is the associated thread), or as a result of
	 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
	 *
	 * @see	nachos.threads.ThreadQueue#acquire
	 * @see	nachos.threads.ThreadQueue#nextThread
	 */

	public void acquire(PriorityQueue waitQueue) {
	    // implement me
		//something about this is wrong. move where it adds it inside the if??????
		if(resources_waiting_for.indexOf(waitQueue) != -1) {

			resources_waiting_for.remove(resources_waiting_for.indexOf(waitQueue));
		}
		current_resources.add(waitQueue);
		if(mostRecentResource == waitQueue) resetRecentResource();



		//update flag
		setFlag();

		//updateMaxEffective();//new PQ find new max
	}

	public void releaseResource(PriorityQueue resource) {//release resource we no longer need to allow others to use it
		if(current_resources.indexOf(resource) != 1) {
			current_resources.remove(current_resources.indexOf(resource));
		}
		//updateMaxEffective();//changed so update
	}

	void setFlag() {
		if(updateMaxFlag ) {
			return;
		}

		updateMaxFlag = true;

		PriorityQueue Q = (PriorityQueue)getRecentResource();
		if(Q != null) Q.setFlag();

	}





	/** The thread with which this object is associated. */
	protected KThread thread;
	protected LinkedList<PriorityQueue> current_resources = new LinkedList<PriorityQueue>();
	protected LinkedList<PriorityQueue> resources_waiting_for = new LinkedList<PriorityQueue>();
	/** The priority of the associated thread. */
	protected int priority;
	protected int effectivePriority;
	ThreadQueue mostRecentResource;
	//set to true when threads priority is changed or PriorityQueue in current_resources needs to updateMax
	boolean updateMaxFlag =  false;
	int time = 0;
    }

    /**
     * Tests whether this module is working.
     */
    public static void selfTest() {

		System.out.println("Inside priority schedule");



//		KThread thread1 = new KThread(new PingTest(1)).setName("1st forked thread");
//
//		thread1.fork();
//
//		Machine.interrupt().disable();
//		readyQueue.print();
//		Machine.interrupt().enable();
//
//		thread1.join(); // prevents alternating between parent thread, and forked thread from parent.
//
//		Machine.interrupt().disable();
//		readyQueue.print();
//		Machine.interrupt().enable();
//
//		//NOTE: machine interrupts allow us to share the CPU?
//
////		Machine.interrupt().disable();
////		readyQueue.print();
////		Machine.interrupt().enable();
//
//
//		Machine.interrupt().disable();
//		readyQueue.print();
//		Machine.interrupt().enable();

	}


}
