
package nachos.threads;

import nachos.machine.*;
import java.util.Random;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.HashSet;


/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
	
	//public static final int priorityDefault = 0;
			//won't work because each thread needs to be given a chance at the resource
			//not only the thread with the highest priority, which is the effective
	//		public static final int priorityDefault = 1;
			//minimum priority a thread should have
//			public static final int priorityMinimum = 1;
			//maximum priority value a thread should have
	//		public static final int priorityMaximum = Integer.MAX_VALUE;
	
	
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param   transferPriority    <tt>true</tt> if this queue should
     *                  transfer tickets from waiting threads
     *                  to the owning thread.
     * @return  a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean swapPriority) {
    // implement me, k
        return new LotTik(swapPriority);
    }
    
    /*
     * 
     *  //------------------------------------------------------------------

    @Override
    public int getPriority(KThread thread) {
    	Lib.assertTrue(Machine.interrupt().disabled());
    	
    	return getThreadState(thread).getEffectivePriority();
    }
    
  //------------------------------------------------------------------
    /*references: https://www.geeksforgeeks.org/operating-system-lottery-scheduling/
     * */
  //  @Override
    /*public void setPriority(KThread thread, int priority) { 
        Lib.assertTrue(Machine.interrupt().disabled()); 
        Lib.assertTrue(priority >= priorityMinimum && priority <= priorityMaximum);
        getThreadState(thread).setPriority(priority); 
    } */

    //------------------------------------------------------------------
  //  @Override
   /* public int getEffectivePriority(KThread thread){
    	Lib.assertTrue(Machine.interrupt().disabled());
    		return getThreadState(thread).getPriority();
    }*/
    
  //------------------------------------------------------------------
  /* @Override 
    public boolean increasePriority() { 
        boolean intStatus = Machine.interrupt().disable(); 
 
        KThread thread = KThread.currentThread(); 
        //create int variable priority to change from points to tickets
        int priority = getPriority(thread); //retrieve priority of thread
        if (priority == priorityMaximum) { //keep track of max to ensure we don't
        	//go over the total is greater than max val or random val 
        	return false; //boolean
        }
        setPriority(thread, priority + 1); 
 
        Machine.interrupt().restore(intStatus); 
        return true; 
    } */
 
  //------------------------------------------------------------------
 //  @Override 
  /*  public boolean decreasePriority() { 
        boolean intStatus = Machine.interrupt().disable(); 
 
        KThread thread = KThread.currentThread(); 
        //create int variable priority to change from points to tickets
        int priority = getPriority(thread); //retrieve priority of thread
        if (priority == priorityMinimum) { //keep track of max to ensure we don't
        	//go over the total is greater than max val or random val 
        	return false; //boolean
        }
        setPriority(thread, priority - 1); 
 
        Machine.interrupt().restore(intStatus); 
        return true; 
    } */
    
  //------------------------------------------------------------------
   
 //  @Override 
 /*  protected LotteryThreadState getThreadState(KThread thread) {
	   if (thread.schedulingState == null) 
   			thread.schedulingState = new LotteryThreadState(thread);
   			return (LotteryThreadState) thread.schedulingState;
   		
   }*/
  

   
//	protected class LotteryQueue extends PriorityScheduler.PriorityQueue {
//		LotteryQueue(boolean transferPriority) {
//			super(transferPriority);
//		}
		
		/*
		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disable());
			check if there is a thread currently at the resource
			if(threadWithResource != null) {
				LotteryThreadState prev_ThreadwithResource = (LotteryThreadState) ThreadwithResource;
			}
		}*/
		
	//	@Override
//		protected LotteryThreadState pickNextThread() {
			/*
			 * pickNextThread will randomly choose and return a thread that is located in
			 * waitQueue of the scheduler. This scheduler is based on the total amount of 
			 * tickets, the tickets are the priority value which is changed in getEffectivePriority.
			 * */
			
	//		LinkedList<KThread> waitQueue = new LinkedList<KThread>();
			
			//first check if there are currently any threads in waitQueue
			//if it's currently empty then retrun NULL, if not, then continue
			//to add the total ammount of tickets in the 'pool'
		//	if (waitQueue.isEmpty()) {
		//		return null;
			//}
			//initialize a variable to keep trach of the total amount of lottery tickets in draw
	//		int total_Lottery_Tickets = 0;
			
			//initialize an array to keep track of each thread's amount of ticket from waitQueue
			//keep track of the size of the array and the sum
			//the sum will be the total of amount of tickets in pool and the amount of tickets 
			//for a certain thread
			
		//	int[] sumTicketsinPool = new int[waitQueue.size()];
			
			//int i = 0;
			
/*should range from ( 0 - (#total amount of tickets - 1) )*/
			
			/*
			 * check if the total tickets is greater than 0, meaning that they are actually threads,
			 * and that these threads will all have access to the resource, unlick PriorityScheduler
			 * */
			
	//		for (KThread thread : waitQueue)
				// end of adding the total amount of lottery tickets in pool and a current threads # of tickets
		//		sumTicketsinPool[i++] = total_Lottery_Tickets += getThreadState(thread).getEffectivePriority();
			
			/*go through each thread in waitQueue, retrieve the difference between the # and the current
			 * threads highestPriority, */
	//		int lotteryTik = random.nextInt(total_Lottery_Tickets);

		//	i = 0;
		//	for (KThread thread : waitQueue)
		//		if (lotteryTik < sumTicketsinPool[i++])
		//			return (LotteryThreadState) getThreadState(thread); 

	//		Lib.assertNotReached();
//			return null;
	//	} //this for loop should be able to find/locate a threadState to return
//	}
//	*/
    
	/*
	 * INCORRECT--> was trying to figure out the donation of tickets, but was confusing myself
	 * because I was doing something too similar to PriorityScheduler which is not 
	 * needed since we are inheriting code from that class
	//Small boolean to see if we need to transfer our priority in the situation where
	//the our effectivePriority is going to change based on the results from searching
	//through the owned HashSet
	boolean transferPriority;
	
	if(currentPriority != effectivePriority){
		transferPriority = true;
	}
	else{
		transferPriority = false;
	} */

    static final int boundChecker(int x, int y)throws ArithmeticException {                           
            if (y > 0 ) {                    
                if(x > 2147483647 - y){
                    return 2147483647;
                }
            }else{
                if(x < -2147483648 - y){
                    return 2147483647;
                }
            }                                                                  
            return x + y;                                               
    }
	//Set the effective priority to the newly calculated priority else it would just be
	//the base priority as declared above
	//effectivePriority = currentPriority;
	
	//*/
	
	/* We need to ensure that the scheduling state of a thread, should it should require
	 * that threads priority value, the highest priority value (effective), along with 
	 * any objects the thread may own. Each thread should have their ticket value and the 
	 * total ammount of tickets in the pool*/
	
	//protected class LotteryThreadState extends PriorityScheduler.ThreadState {
		
		//protected class, meaning that none of the values can be changed, unless done manually
		//LotteryThreadState extends the functionality of the donation of priority
		//shouldn't require a large amount of code because of this 
		//protected LinkedList<LotteryQueue> donationQueue = new LinkedList<LotteryQueue>();
		
		//create a new linkedlist for the lottery threads and prepare for donation of tickets
		//between threads
		
		//public LotteryThreadState(KThread thread) { //retreive threads from KThread
			//the compiler creates byte code equivalent to super()
		//	super(thread); //pass thread to LotteryThreadState super class
		//}
  //  @Override
	//allow a sub/child class to provide a different specific implementation of a method,
	//that may be already provided by it's super/parent class
	//this is located in the above super classes for LotteryThreadState
    protected class ThreadState extends PriorityScheduler.ThreadState {
        public ThreadState(KThread thread) {
          super(thread);
        }

        public int getEffectivePriority() {
       /*
        * public int getEffectivePriority() {
	/*return the Hash Set, we need to use a HashSet to prevent the threads from having 
	the same ammount of tickets as another thread, which wouldn't work due to if		//we randomly choose a thread containing 20, if there are 3 threads containing 20
	tickets, then it would return 3 threads of highest priority to the resource
	which could not happen as only one threads at a time has access to the resource*/
//			return getEffectivePriority(new HashSet<LotteryThreadState>());
	//	}*/
            if (expired) {
                mostPriority = this.priority;
                Iterator<ThreadQueue> n = this.data.iterator();
                while(n.hasNext()) {  //hasNext JAVA example
                    LotTik temp = (LotTik)(n.next());                 
                    mostPriority = boundChecker(mostPriority, temp.getEffectivePriority());   
                }
                expired = false;
            }
            return mostPriority;
        } 

    } 

    protected ThreadState getThreadState(KThread thread) {
    	//check if there is a thread in scheduling state
        if (thread.schedulingState != null){
        	//if empty  let ThreadState know
            return (ThreadState) thread.schedulingState;       
        }else{
        	//other wise, retrieve thread and return
            thread.schedulingState = new ThreadState(thread);
            return (ThreadState) thread.schedulingState;
        }
    }


    protected class LotTik extends PriorityQueue {
       LotTik(boolean swapPriority) {
            super(swapPriority);
       }

       public int getEffectivePriority() {
   //		private int getEffectivePriority(HashSet<LotteryThreadState> set) {
   			/*now set the HashSet created to private so that the member's access is private
   			and so that the member is only visible from within THIS class, not child/sub/parent class */		
   //				if (set.contains(this)) { //required from Java.util.HashSet.contains() method above
   			//we need to check if a set contains any particular element
   	//				return priority;
   			//return the priority of that thread that is present (ticket value)
   	//			}
   				
   			/*initialize a variable to keep track of the ticket values from the threads in the HashSet
   			the amount of tickets from the thread will be the highest/effective priority
   			we don't need to compare values for the "highest" value as we are get the priority
   			based on probability */
            if (swapPriority) {
                mostPriority = 0;
                Iterator<KThread> n = waitingThreads.iterator(); 
                while( n.hasNext()) {  
                   int priority = getThreadState(n.next()).getEffectivePriority();
                   mostPriority += priority;
                }
                expired = false;
                return mostPriority;
            }else{
                return 0;
            }
       } 
   	/*
		 * check the new donation queue created and apply transferPriority so that the threads
		 * can donate their tickets to one another. If we wanted the lottery winner to contain
		 * 23 tickets for example, and if there were a thread that contained 22 tickets and 
		 * that thread was the closest to winning the lottery compared to the others then 
		 * other threads will donate their tickets so that thread can have access to the resource
		 * */
	/*		if (queue.swapPriority) //go through the queue of threads
				//for threads in waitQueue
				for (KThread thread : queue.waitQueue) {
					set.add(this);
					resourcePriority += getThreadState(thread).getEffectivePriority();
					set.remove(this);
				}*/
		

       protected KThread pickNextThread() {
            KThread nextThread = null;
            KThread thread;
            int total_Lottery_Tickets = 0;	//initialize lottery pool to 0
            
            Iterator<KThread> n = waitingThreads.iterator(); //n = waiting threads
            
            while(n.hasNext()) {  //check next thread
            //we need to check the bound of the lottery tickets
            	//make sure that there isnt a ticket that's more than the total
            	//tickets in pool
            	//if if so swap
                total_Lottery_Tickets = boundChecker(total_Lottery_Tickets, getThreadState(n.next()).getEffectivePriority());                              
            }

            Random randomNum = new Random();
            int ticketValue = randomNum.nextInt(total_Lottery_Tickets) + 1;
            total_Lottery_Tickets = 0; 
       /*
        * 	PriorityQueue queue = (PriorityQueue) thread.waitForJoin; 
			/*this is important so that there is no lock, meaning less 
			 * donations would need to be done*/
//			if (queue.swapPriority)
//				for (KThread thread : queue.waitQueue) {
		//			set.add(this);
				/*
					for (PriorityQueue j : owned) {
							if(j.transferPriority){
									if(j.pickNextThread() != null){
										int donatingPri = j.pickNextThread().getEffectivePriority();
										if(donatingPri > currentPriority){
											currentPriority = donatingPri;
										}
										*/
	//				resourcePriority += getThreadState(thread).getEffectivePriority();
	//				set.remove(this);
		//		}

			//return resourcePriority; //return priority of associated thread
	//	}
//	}*/     
            n = waitingThreads.iterator();
            while(n.hasNext()) {  
                thread = n.next(); 
                total_Lottery_Tickets = boundChecker(total_Lottery_Tickets, getThreadState(thread).getEffectivePriority());                               
                if (ticketValue <= total_Lottery_Tickets ) {
                    nextThread = thread;    
                    break;
                }
            }
            return nextThread; //return priority associated with thread
       }
    }
//	protected Random random = new Random(25);
}
/* INCORRECT --> REDO
//Save the current Priority for comparison and swapping purposes
		int currentPriority = priority;
		
		//PriorityQueue x = waitingFor.iterator().next();
		
		//for loop used to access the (in this case one element) of the HashSet
		//(this) is then removed temporarily from the waitQueue to ensure we 
		//don't mistakenly compare (this) to itself
		for (PriorityQueue j : waitingFor){
			j.waitQueue.remove(this); 
		}
	
		//Walk through (this)'s owned resources looking for waitQueues marked 
		//with transfer priority. Once one is found, retrieve the threads priority
		//and (if the donating priority is greater than current priority) make the swap.
		for (PriorityQueue j : owned) {
			if(j.transferPriority){
				if(j.pickNextThread() != null){
					int donatingPri = j.pickNextThread().getEffectivePriority();
					if(donatingPri > currentPriority){
						currentPriority = donatingPri;
					}
				}
			}	
		}
*/  