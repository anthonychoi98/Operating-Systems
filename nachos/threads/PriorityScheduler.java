package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

import nachos.machine.Machine;
import nachos.machine.Lib;
import java.util.LinkedList;


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
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
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
    	if (thread.schedulingState == null) {
    		thread.schedulingState = new ThreadState(thread);
    	}
		return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
    	PriorityQueue(boolean transferPriority) {
	    this.swapPriority = transferPriority;
	}

	/*thread will go from a running to waiting state*/
	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    /*waitQueue is undefined for PriorityScheduler.ThreadState
	     * so we need to include here inorder to be able to add the threads through
	     * waitQueue so the rest of the code can do it's magic*/
	   // waitQueue.add(thread);
	    getThreadState(thread).waitForAccess(this);
	}

	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).acquire(this);
	}

	public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());

	    //check if there are waiting threads or threads next in line
	  //  if ( waitQueue.isEmpty() || pickNextThread() == null ) {
	  //  	return null;	//return null if nothing is left to pick
	   // }
/*Note: We will need to call acquire() for the highest priority in thread
 * and give it to the resource*/
	//    KThread nextThread = pickNextThread().thread; //if not pick the next thread

	    /*In order to choose the next thread, we need to remove it from the waitlist
	     * in Queue*/
	   // this.waitQueue.remove(nextThread);
	    
	    /*Now make the thread the resource holder*/
	   // resourceHolder = nextThread;
	    
	    /*acquire sets the polled thread to be the owner of the resource*/
	    //this.acquire(resourceHolder);
	    
	    /*finally, return the resource holder thread*/
	    //return resourceHolder;
	    
	    if (lock != null) { //check to see if list is empty
	    	//remove from queue and donate priority
	    	lock.waitQueue.remove(this);
	    	lock.revise(); //update the values
	    }
	  //the next state of the thread will be the next thread from pickNextThead
	    ThreadState threadState = pickNextThread();
	    
	    if (threadState != null) {
	    	threadState.acquire(this);
	    		return threadState.thread; //return the resource holder
	    		//which would be this thread
	    }
	    else {
	    	return null;
	    }
	    
	} //end up nextThread()
	
	    // implement me
    /*be sure to not return anything, only if you're
     * especially meaning to
/*	    if(!waitQueue.isEmpty()) {
	 
	    //call acquire() on the highest priority thread
	    //to give it the resource
	    acquire(waitQueue.poll().aThread);
	    //Acquire now sets the polled thread to be the owner of 
	    //this resource under "owningThread"
	    }
	    else{
	    
	    return null;
	    } */ 
	    

	/**
	 * Return the next thread that <tt>nextThread()</tt> would return,
	 * without modifying the state of this queue.
	 *
	 * @return	the next thread that <tt>nextThread()</tt> would
	 *		return.
	 */
	protected ThreadState pickNextThread() {
	    // implement me
		//returns the next ThreadState in the line of succession
		//while not alternating the queue or the Threadstate
		// this is where we have to display the thread with the high priority
		KThread product = null;
/*The goal in pickNextThread is to return the highestPriority thread. Will need 
 * to initialize 2 variables to help assist with the comparison between threads */
		int mostPriority = -1;
		//int minPriority = 0; initialized for us previously
//		mostPriority = priorityMinimum;
		
	    //KThread thread = null; CHECK THIS
		
//		int temp; 	//create a 'temp' variable to help assist with swaps
		
/*Use a for loop to interate through the linkedlist in waitQueue, and then
 * locate the thread of highestPriority within the list*/
		
		for (KThread thread : waitQueue) 	
			//temp = getThreadState(waitQueue.get(i)).getEffectivePriority();	
/*set temp == to first thread in waitQueue*/
			
			if ( product == null || getEffectivePriority(thread) > mostPriority) {
				//thread = waitQueue.get(i);	//set that thread
				product = thread;
				//mostPriority = temp;	//set temp as the new highestPriority 
				mostPriority = getEffectivePriority(thread);
			} //end of if statement
			
			if (product == null) {  //if there is no result of priority from threads
				return null;	//tell the user it's empty
			} //end of if statement
			
		//remember to notify getThreadState of the next thread chosen
			return getThreadState(product); //return that thread
		 //end of for loop
		
		
		//return getThreadState(thread);	//return that thread
		/*return waitQueue.peek(); */
	    //return null;
	} // end of pickNextThread()
    
    public void print_Result() {
    /*a process will continue to run until it is interrupted, inorder to
     * guarantee mutual exclusion, it is in our best interest to prevent it
     * from being interrupted*/
    	Lib.assertTrue(Machine.interrupt().disabled());
    	
    	//print
    	System.out.print("PriorityQueue: ");
    	
    	for (KThread thread: waitQueue) {
    		System.out.print(" " + thread);
    	}
    	
    	System.out.println();
    } // end of print_Result()

    
    /*We need to implement a boolean for the transfer of priorities
     * transfer priority from threads in waitQueue to the owning thread(resource)*/
	/*Create Helper Method*/
    public boolean swapPriority;
    LinkedList<KThread> waitQueue = new LinkedList<KThread> ();
    ThreadState lock = null;
//	public void add(PriorityQueue waitQueue2) {
		// TODO Auto-generated method stub
	@Override
	public void print() {
		// TODO Auto-generated method stub
		
	}

public void add(PriorityQueue waitQueue2) {
	// TODO Auto-generated method stub
	
}
		
//	}

//	@Override
//	public void print() {
		// TODO Auto-generated method stub
		
//	}
    
} // end of Priorty extends ThreadQueue
  
    
//	public int getEffectivePriority() {
			//int minPriority;
//		if (swapPriority == false) {
//			return priorityMinimum;	//already initialized variable set to 0
//		}	
//			int temp;
//			int highestPriority = priorityMinimum;
	/*Now for locate the highestPriority and set effectivePriority equal to that*/
//		for (int i = 0; i < waitQueue.size(); i++) {	//iterate through linklist in waitQueue
	//		temp = getThreadState(waitQueue.get(i)).getEffectivePriority();
			
/* set each thread equal to the temp variable and check each thread if they are greater than
 * the current effective priority. If temp is greater than effective, then set effective
 * equal to the temp*/			
//			if (temp > highestPriority) {
//				highestPriority = temp;
//			}
//		}	//end of loop locating
/* We are searching through owned resources, if any have been marked to donate priority,
 * then taking the priority of the next thread scheduled to be executed */	
//		return highestPriority;		//return thread of highestPriority	
//	}
	
	/*Create New Helper Method swapPriority(): 
	 * will assist with setting the boolean helper variable 
 *  to true if and only if a transfer/donation has been made*/
//public void swapPriority() {
//
//	if (swapPriority == false) {	//if donation hasn't occurred
//		return;
//	}
/*if no donation has been made that means that there has been a donation made
 * and should return true for transferPriority*/
//	priorityChanged = true;	//return true and let the know
	
//	return effectivePriority;
//}

//	public void print() {
//	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
//	    System.out.println("Name");
//	}

	/**
	 * <tt>true</tt> if this queue should transfer priority from waiting
	 * threads to the owning thread.
	 */
//	public boolean swapPriority;
/*Because priorities will be donated from the threads 
 * waiting in linkedlist, we need to include all methods above that are needed
 * to assist wit the donations/labeling of priority, as well as letting them
 * have access to the CPU. This includes the queue that waits for the threads,
 * the thread chosen of highestPriority that has gained access  to the source,
 * and the boolean method to check is there was a donation made*/
	
//		protected LinkedList<KThread> waitQueue = new LinkedList<KThread>();	//queue that waits for threads
//		protected KThread resourceHolder = null;	//thread with access to resource
//		private boolean priorityChanged = false;	//flag to check if the priority has been updated
  //  }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
	/**
	 * Allocate a new <tt>ThreadState</tt> object and associate it with the
	 * specified thread.
	 *
	 * @param	thread	the thread this state belongs to.
	 */
	public ThreadState(KThread thread) {
	    this.thread = thread;
	    
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
	    // implement me
	public int getEffectivePriority() {
		return getEffectivePriority(new HashSet<ThreadState>());
	} //end of getEffectivePriority()
	
	private int getEffectivePriority(HashSet<ThreadState> set) {
			if (set.contains(this)) {
					return priority;
			}
			resourcePriority = priority;
			
		for (PriorityQueue queue : waitQueue) {
			if (queue.swapPriority) {
					for (KThread thread : queue.waitQueue) {
						set.add(this);
						int x = getThreadState(thread).getEffectivePriority(set); 
//assign and initialize thread for swap
						set.remove(this); //clear out and remove thread 
						
						if (x > resourcePriority) {
							resourcePriority = x;
						}//end of second if statement
					}//end of second for loop
			} //end of if statement
		}//end of for loop
		
		PriorityQueue queue = (PriorityQueue) thread.waitForJoin;
		if (queue.swapPriority) {
		/* the getEffectivePriority methods duty is to seek for the thread with 
		 * the highestPriority in waitQueue by looping through the threads, and 
		 * keep track of donations if they were made */	
			for (KThread thread : queue.waitQueue) {
				set.add(this);
				int x  = getThreadState(thread).getEffectivePriority(set);
				set.remove(this); //remove thread from hash
				
				if (x > resourcePriority) {
					resourcePriority = x;
				} //end of second if statement
			}//end of for loop
		}//end of if statement
		return resourcePriority;
	} //end of getEffectivePriority()

		
		//initialize variables to assist with the donations
//		int highestPriority;
//		int temp;
//		highestPriority = priority;
	
	/*Now, we need to again go through all the threads in linklist Queue*/
//		for (int i = 0; i < waitQueue.size(); i++) {
//			temp = waitQueue.get(i).getEffectivePriority(); //set temp tp each val
/*check through each thread as temp to compare priorities as a "Comparator" */
//				if (temp > highestPriority) {
	//					highestPriority = temp;
/*check if the current thread priority is greater than effectivePriority, then
 * make that the new effectivePriority and swap threads*/
	//			}
//		}
/*now if this doesn't hold up then the temp < effectivePriority, and the temp value
 * is equal to the effectivePriority, now get the priority of that thread*/
		
//		if (highestPriority == priorityMinimum) {
//			highestPriority = getPriority();
//		}
//	    return highestPriority;	//return the new thread of effectivePriority
//	}
	
	/**
	 * Set the priority of the associated thread to the specified value.
	 *
	 * @param	priority	the new priority.
	 */
	public void setPriority(int priority) {
		// --> second try going back to the first
		//this.priority = priority;
	    /* --> first try
	     if (this.priority == priority)
		return;
	    
	    this.priority = priority; */
		if (this.priority == priority) {
			return;
		}
		this.priority = priority;
	    
	    // implement me
		revise(); //don't forget to create the revise method to update the priority vals
	} //end of setPriority()

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
		/*check and save the time the thread is being added, add that thread
		 * into waitQueue priority queue*/
		waitQueue.waitQueue.add(thread);
		
		if(waitQueue.lock == null) {
		//waitQueue.swapPriority();	
			return;
		} //end of if statement
		waitQueue.lock.revise();
		//because a thread was added into waitQueue, we need to notify priorityQueue
		
		/*
	    implement me
		Save the time of enter the line for potential tie-breaking
		waitTime = Machine.timer().getTime();
		Update the waitingFor object with the PriorityQueue that
		has the desired resource 
		waitingFor.add(waitQueue);
		Place this ThreadState inside the resources waitQueue in the 
		nachos.PriorityQueue
		waitQueue.waitQueue.add(this);
		
		Make sure that the current thread is owned, and if true, update the 
		effective priority of that thread. This will assist with mitigating
		the effects of priority.
		
		 * 
		 */
		
		/* --> first try
		 * if(waitQueue.owningThread != null){
		getThreadState(waitQueue.owningThread).updateEffectivePriority();
		} */
	} //end of waitForAccess()

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
		//Dealing with the situation where the waitQueue is owned by another thread
		
/* in acquire() if it has an owner, boot it off */	
	//	this.waitQueue.add(waitQueue); 
		waitQueue.waitQueue.remove(thread);
		waitQueue.lock = this;
		waitQueue.add(waitQueue);
	/*	--> incorrect attempt
	 * if (waitQueue.owningThread != null) { //remove thread by the state
			ThreadState previousResident = getThreadState(waitQueue.accessedThread);
 		
	 * When we have confirmed that the removal from it's
	 * owner [hashset]. accessedThread will reset so that effectivePriority
	 * of the previous owner is notified
	 *
			if(previousResident.accessed.remove(waitQueue)) {
				waitQueue.accessedThread = null;
				previousResident.updateEffectivePriority ();
			}
		} 
	*/
		revise();
	}	
//create revise() to keep track of the updates of priority values when swapping
	public void revise() {
		resourcePriority = expiredEffectivePriority;
		getEffectivePriority();
	} //end of revise()
	
	/* --> had a previous error
	 * private LinkedList<KThread> waitQueue() {
	   TODO Auto-generated method stub
		return null;
		}
	*/
	protected static final int expiredEffectivePriority = -1;
	protected int resourcePriority = expiredEffectivePriority;
	protected LinkedList<PriorityQueue> waitQueue = new LinkedList<PriorityQueue>();
	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority = priorityDefault;
    }
}