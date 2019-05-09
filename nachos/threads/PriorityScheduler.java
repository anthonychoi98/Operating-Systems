package nachos.threads;

import nachos.machine.*;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.LinkedList;    
import java.util.Iterator;      


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
     * @param   transferPriority    <tt>true</tt> if this queue should
     *                  transfer priority from waiting threads
     *                  to the owning thread.
     * @return  a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean swapPriority) {
    	return new PriorityQueue(swapPriority);
    }

    public int getPriority(KThread thread) {
               
    return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {

    return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
    boolean intStatus = Machine.interrupt().disabled();
               
    Lib.assertTrue(priority >= priorityMinimum &&
                       priority <= priorityMaximum);
    
    getThreadState(thread).setPriority(priority);
    Machine.interrupt().restore(intStatus);
    }

    public boolean increasePriority() {
    boolean intStatus = Machine.interrupt().disable();
               
    KThread thread = KThread.currentThread();

    int priority = getPriority(thread);
    if (priority == priorityMaximum) {
        Machine.interrupt().restore(intStatus);
        return false;
    }

    setPriority(thread, priority+1);

    Machine.interrupt().restore(intStatus);
    return true;
    }

    public boolean decreasePriority() {
    boolean intStatus = Machine.interrupt().disable();
               
    KThread thread = KThread.currentThread();

    int priority = getPriority(thread);
    if (priority == priorityMinimum) {
        Machine.interrupt().restore(intStatus);
        return false;
    }

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

    public static final int priorityMaximum = Integer.MAX_VALUE;    //max 7           

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param   thread  the thread whose scheduling state to return.
     * @return  the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
    if (thread.schedulingState == null)
        thread.schedulingState = new ThreadState(thread);

    return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
        PriorityQueue(boolean swapPriority) {
            this.swapPriority = swapPriority;
        }

    	/*thread will go from a running to waiting state*/
    	//public void waitForAccess(KThread thread) {
    //	    Lib.assertTrue(Machine.interrupt().disabled());
    	    /*waitQueue is undefined for PriorityScheduler.ThreadState
    	     * so we need to include here inorder to be able to add the threads through
    	     * waitQueue so the rest of the code can do it's magic*/
    	   // waitQueue.add(thread);
 //   	    getThreadState(thread).waitForAccess(this);
  //  	}
        public void waitForAccess(KThread thread) {
            boolean intStatus = Machine.interrupt().disabled();
            getThreadState(thread).waitForAccess(this);
            // Machine.interrupt().restore(intStatus);
        }
//code below
        public void acquire(KThread thread){
            Lib.assertTrue(Machine.interrupt().disabled());
     //       Lib.assertTrue(Machine.interrupt().disabled());
    	//    getThreadState(thread).acquire(this);
            ThreadState state = getThreadState(thread); 
            stateOfThread = state;            
            state.acquire(this);
        }

        public KThread nextThread(){
            //check if there are waiting threads or threads next in line
      	  //  if ( waitQueue.isEmpty() || pickNextThread() == null ) {
      	  //  	return null;	//return null if nothing is left to pick
      	   // }
      /*Note: We will need to call acquire() for the highest priority in thread
       * and give it to the resource*/
      	//    KThread nextThread = pickNextThread().thread; //if not pick the next thread    
            Lib.assertTrue(Machine.interrupt().disabled());
            /*In order to choose the next thread, we need to remove it from the waitlist
    	     * in Queue*/
    	   // this.waitQueue.remove(nextThread);
            
            /*Now make the thread the resource holder*/
     	   // resourceHolder = nextThread;
     	    
     	    /*acquire sets the polled thread to be the owner of the resource*/
     	    //this.acquire(resourceHolder);
     	    
     	    /*finally, return the resource holder thread*/
     	    //return resourceHolder;
            if (waitingThreads.isEmpty()){
                return null;
            
            }else{
                KThread findThread = pickNextThread();
                if (swapPriority){
                    stateOfThread.data.remove(this);
                    stateOfThread = null;
                }
         /*
          *  if (lock != null) { //check to see if list is empty
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
	    
	} //end up nextThread()*/

                if (findThread != null){
                    waitingThreads.remove(findThread);
                    acquire(findThread);         
                }
                
                return findThread;
            }
/*
 * 
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
  }//end

        /**
         * Return the next thread that <tt>nextThread()</tt> would return,
         * without modifying the state of this queue.
         *
         * @return  the next thread that <tt>nextThread()</tt> would
         *      return.
         */
        
  //      protected ThreadState pickNextThread() {
        protected KThread pickNextThread() {
            KThread nextThread = null;
            // implement me
    		//returns the next ThreadState in the line of succession
    		//while not alternating the queue or the Threadstate
    		// this is where we have to display the thread with the high priority
    	//	KThread product = null;
            
            /*The goal in pickNextThread is to return the mostPriority thread. Will need 
             * to initialize 2 variables to help assist with the comparison between threads */

           Iterator<KThread> n = waitingThreads.iterator();
            while ( n.hasNext()) {  
                KThread thread = n.next(); 
             /*
              * int mostPriority = -1;
		//int minPriority = 0; initialized for us previously
//		mostPriority = priorityMinimum;
		
	    //KThread thread = null; CHECK THIS*/
//        		int temp; 	//create a 'temp' variable to help assist with swaps
                if (nextThread == null || getThreadState(thread).getEffectivePriority() > getThreadState(nextThread).getEffectivePriority()) { 
                    nextThread = thread;
                }
            }
            return nextThread;
        }//end of pickNextThread
        /*Use a for loop to interate through the linkedlist in waitQueue, and then
         * locate the thread of mostPriority within the list*/
        		
   //     		for (KThread thread : waitQueue) 	
        			//temp = getThreadState(waitQueue.get(i)).getEffectivePriority();	
        /*set temp == to first thread in waitQueue*/  
        
        
        
       /*
        * 
        * 	if ( product == null || getEffectivePriority(thread) > mostPriority) {
				//thread = waitQueue.get(i);	//set that thread
				product = thread;
				//mostPriority = temp;	//set temp as the new mostPriority 
				mostPriority = getEffectivePriority(thread);
			} //end of if statement
			
			if (product == null) {  //if there is no result of priority from threads
				return null;	//tell the user it's empty
			} //end of if statement
			
		//remember to notify getThreadState of the next thread chosen
			return getThreadState(product); //return that thread
		 //end of for loop*/
        
        
      //return getThreadState(thread);	//return that thread
      		/*return waitQueue.peek(); */
      	    //return null;
        public int getEffectivePriority() {

            if (swapPriority == false) {
                return priorityMinimum;
            }

            if (expired) {
                mostPriority = priorityMinimum; 
                Iterator<KThread> n = waitingThreads.iterator();
                while (n.hasNext()) {  
                    KThread thread = n.next(); 
                    if ( getThreadState(thread).getEffectivePriority() > mostPriority) { 
                        mostPriority = getThreadState(thread).getEffectivePriority();;
                    }
                }
                expired = false;
            }

            return mostPriority;
        }

        public void setP() {
            if (swapPriority) {
                expired = true;
                if (stateOfThread != null) {
                    stateOfThread.setP();
                }
            }
        }

        /*
         *  public void print_Result() {
    /*a process will continue to run until it is interrupted, inorder to
     * guarantee mutual exclusion, it is in our best interest to prevent it
     * from being interrupted*/
  //  	Lib.assertTrue(Machine.interrupt().disabled());
    	
    	//print
 //   	System.out.print("PriorityQueue: ");
    //	
    	//for (KThread thread: waitQueue) {
   // 		System.out.print(" " + thread);
    	//}
    	
    //	System.out.println();
   // } // end of print_Result()*/
        public void print() {
            // implement me (if you want)
            Lib.assertTrue(Machine.interrupt().disabled());

            
        }

        /**
         * <tt>true</tt> if this queue should transfer priority from waiting
         * threads to the owning thread.
         */
        public boolean swapPriority;
     
        /*
         *   
    /*We need to implement a boolean for the transfer of priorities
     * transfer priority from threads in waitQueue to the owning thread(resource)*/
	/*Create Helper Method*/
//    public boolean swapPriority;
 //   LinkedList<KThread> waitQueue = new LinkedList<KThread> ();
   // ThreadState lock = null;
//	public void add(PriorityQueue waitQueue2) {
		// TODO Auto-generated method stub
	//@Override
//	public void print() {
		// TODO Auto-generated method stub
		
//	}*/
        protected LinkedList<KThread> waitingThreads = new LinkedList<KThread>();         
        protected int mostPriority; 
        protected ThreadState stateOfThread = null;             
        protected boolean expired;                  

    } 


    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {

    /**
     * Allocate a new <tt>ThreadState</tt> object and associate it with the
     * specified thread.
     *
     * @param   thread  the thread this state belongs to.
     */
    public ThreadState(KThread thread) {
        this.thread = thread;
        
        setPriority(priorityDefault);
    }

    /**
     * Return the priority of the associated thread.
     *
     * @return  the priority of the associated thread.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Return the effective priority of the associated thread.
     *
     * @return  the effective priority of the associated thread.
     */
    public int getEffectivePriority() {
//    	public int getEffectivePriority() {
    			//int minPriority;
//    		if (swapPriority == false) {
//    			return priorityMinimum;	//already initialized variable set to 0
//    		}	
//    			int temp;
//    			int mostPriority = priorityMinimum;
    	/*Now for locate the mostPriority and set effectivePriority equal to that*/
//    		for (int i = 0; i < waitQueue.size(); i++) {	//iterate through linklist in waitQueue
    	//		temp = getThreadState(waitQueue.get(i)).getEffectivePriority();
    			
    /* set each thread equal to the temp variable and check each thread if they are greater than
     * the current effective priority. If temp is greater than effective, then set effective
     * equal to the temp*/			
//    			if (temp > mostPriority) {
//    				mostPriority = temp;
//    			}
//    		}	//end of loop locating
    /* We are searching through owned resources, if any have been marked to donate priority,
     * then taking the priority of the next thread scheduled to be executed */	
//    		return mostPriority;		//return thread of mostPriority	
//    	}
        int maxEffective = priority;
        Iterator<ThreadQueue> n = data.iterator();
        if (expired) {
            while( n.hasNext()) {  
                PriorityQueue temp = (PriorityQueue)(n.next()); 
                int effective = temp.getEffectivePriority();
                if (effective > maxEffective) {
                    maxEffective = effective;
                }
            }
        }
        return maxEffective;
    }
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
    /**
     * Set the priority of the associated thread to the specified value.
     *
     * @param   priority    the new priority.
     */
    
    /*
     * 	public void setPriority(int priority) {
		// --> second try going back to the first
		//this.priority = priority;
	    /* --> first try
	     if (this.priority == priority)
		return;
	    
	    this.priority = priority; */
	//	if (this.priority == priority) {
//			return;
	//	}
		//this.priority = priority;

	    // implement me
		//revise(); //don't forget to create the revise method to update the priority vals
//	} //end of setPriority()*/
    public void setPriority(int x) {
        if (priority != x){
            priority = x;
            setP();
        }
        
    }

    /**
     * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
     * the associated thread) is invoked on the specified priority queue.
     * The associated thread is therefore waiting for access to the
     * resource guarded by <tt>waitQueue</tt>. This method is only called
     * if the associated thread cannot immediately obtain access.
     *
     * @param   waitQueue   the queue that the associated thread is
     *              now waiting on.
     *
     * @see nachos.threads.ThreadQueue#waitForAccess
     */
    public void waitForAccess(PriorityQueue waitQueue) {
        waitQueue.waitingThreads.add(thread);
        waitQueue.setP();
        waitList = waitQueue;
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
	//} //end of waitForAccess()
   
        if (data.indexOf(waitQueue) >= 0) {
            data.remove(waitQueue);
            waitQueue.stateOfThread = null;             
        }
    }

    /**
     * Called when the associated thread has acquired access to whatever is
     * guarded by <tt>waitQueue</tt>. This can occur either as a result of
     * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
     * <tt>thread</tt> is the associated thread), or as a result of
     * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
     *
     * @see nachos.threads.ThreadQueue#acquire
     * @see nachos.threads.ThreadQueue#nextThread
     */
    // implement me
 		//Dealing with the situation where the waitQueue is owned by another thread
    public void acquire(PriorityQueue waitQueue) {
        // implement me
        /* in acquire() if it has an owner, boot it off */	
    	//	this.waitQueue.add(waitQueue); 
    	//	waitQueue.waitQueue.remove(thread);
    		//waitQueue.lock = this;
    	//	waitQueue.add(waitQueue);
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
        data.add(waitQueue);
        if (waitQueue == waitList){
            waitList = null;
        }
        setP();
    }   

    public void setP(){
        if (!expired) {
            expired = true;
            PriorityQueue temp = (PriorityQueue)waitList;
            if (temp != null) {
                temp.setP();
            }
        }
    }
    /*
     * //create revise() to keep track of the updates of priority values when swapping
	public void revise() {
		resourcePriority = expiredEffectivePriority;
		getEffectivePriority();
	} //end of revise()
*/

    /** The thread with which this object is associated. */    
    protected KThread thread;

    /** The priority of the associated thread. */
    
	/* --> had a previous error
	 * private LinkedList<KThread> waitQueue() {
	   TODO Auto-generated method stub
		return null;
		}*/
	
    protected int priority;
	//protected static final int expiredEffectivePriority = -1;
//	protected int resourcePriority = expiredEffectivePriority;
//	protected LinkedList<PriorityQueue> waitQueue = new LinkedList<PriorityQueue>();
	/** The thread with which this object is associated. */	   
    protected int mostPriority;  
    /** The priority of the associated thread. */
    protected boolean expired = false;
    protected LinkedList<ThreadQueue> data = new LinkedList<ThreadQueue>();  
    protected ThreadQueue waitList; 
    }

    /**
     * Return the effective priority of the associated thread.
     *
     * @return  the effective priority of the associated thread.
     */
    public int getEffectivePriority() {

        int maxEffective = priority;
        Iterator<ThreadQueue> n = data.iterator();
        if (dirty) {
            while( n.hasNext()) {  
                PriorityQueue temp = (PriorityQueue)(n.next()); 
                int effective = temp.getEffectivePriority();
                if (effective > maxEffective) {
                    maxEffective = effective;
                }
            }
        }
        return maxEffective;
    }

    /**
     * Set the priority of the associated thread to the specified value.
     *
     * @param   priority    the new priority.
     */
    public void setPriority(int pri) {
        if (priority != pri){
            priority = pri;
            setDirty();
        }
        
    }

    /**
     * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
     * the associated thread) is invoked on the specified priority queue.
     * The associated thread is therefore waiting for access to the
     * resource guarded by <tt>waitQueue</tt>. This method is only called
     * if the associated thread cannot immediately obtain access.
     *
     * @param   waitQueue   the queue that the associated thread is
     *              now waiting on.
     *
     * @see nachos.threads.ThreadQueue#waitForAccess
     */
    public void waitForAccess(PriorityQueue waitQueue) {
        /*DELETE this comment !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
         *change waitingThread to waitQueue if fails
        */
        waitQueue.waitingThreads.add(thread);
        waitQueue.setDirty();
        waitList = waitQueue;

        if (data.indexOf(waitQueue) >= 0) {
            data.remove(waitQueue);
            waitQueue.threadCondition = null;             
        }
    }

    /**
     * Called when the associated thread has acquired access to whatever is
     * guarded by <tt>waitQueue</tt>. This can occur either as a result of
     * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
     * <tt>thread</tt> is the associated thread), or as a result of
     * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
     *
     * @see nachos.threads.ThreadQueue#acquire
     * @see nachos.threads.ThreadQueue#nextThread
     */
    public void acquire(PriorityQueue waitQueue) {
        // implement me
        data.add(waitQueue);
        if (waitQueue == waitList){
            waitList = null;
        }
        setDirty();
    }   

    public void setDirty(){
        if (!dirty) {
            dirty = true;
            PriorityQueue temp = (PriorityQueue)waitList;
            if (temp != null) {
                temp.setDirty();
            }
        }
    }

    /** The thread with which this object is associated. */    
    protected KThread thread;

    /** The priority of the associated thread. */
    protected int priority;

    protected int highestPriority;  
    protected boolean dirty = false;
    protected LinkedList<ThreadQueue> data = new LinkedList<ThreadQueue>();  
    protected ThreadQueue waitList; 
    }
}
