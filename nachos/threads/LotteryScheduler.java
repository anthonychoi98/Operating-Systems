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
    public ThreadQueue newThreadQueue(boolean transferPriority) {
    // implement me, k
        return new LotteryTickets(transferPriority);
    }

    static final int checkBounds(int a, int b)throws ArithmeticException {                           
            if (b > 0 ) {                    
                if(a > 2147483647 - b){
                    return 2147483647;
                }
            }else{
                if(a < -2147483648 - b){
                    return 2147483647;
                }
            }                                                                  
            return a + b;                                               
    }

    protected class ThreadState extends PriorityScheduler.ThreadState {
        public ThreadState(KThread thread) {
          super(thread);
        }

        public int getEffectivePriority() {
            if (dirty) {
                highestPriority = this.priority;
                Iterator<ThreadQueue> n = this.data.iterator();
                while(n.hasNext()) {  
                    LotteryTickets temp = (LotteryTickets)(n.next());                 
                    highestPriority = checkBounds(highestPriority, temp.getEffectivePriority());   
                }
                dirty = false;
            }
            return highestPriority;
        } 

    } 

    protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState != null){
            return (ThreadState) thread.schedulingState;       
        }else{
            thread.schedulingState = new ThreadState(thread);
            return (ThreadState) thread.schedulingState;
        }
    }


    protected class LotteryTickets extends PriorityQueue {
       LotteryTickets(boolean transferPriority) {
            super(transferPriority);
       }

       public int getEffectivePriority() {
            if (transferPriority) {
                highestPriority = 0;
                Iterator<KThread> n = waitingThreads.iterator(); 
                while( n.hasNext()) {  
                   int priority = getThreadState(n.next()).getEffectivePriority();
                   highestPriority += priority;
                }
                dirty = false;
                return highestPriority;
            }else{
                return 0;
            }
       } 

       protected KThread pickNextThread() {
            KThread nextThread = null;
            KThread thread;
            int total = 0;
            
            Iterator<KThread> n = waitingThreads.iterator();
            
            while(n.hasNext()) {  
                total = checkBounds(total, getThreadState(n.next()).getEffectivePriority());                              
            }

            Random randomNum = new Random();
            int ticketValue = randomNum.nextInt(total) + 1;
            total = 0; 
            
            n = waitingThreads.iterator();
            while(n.hasNext()) {  
                thread = n.next(); 
                total = checkBounds(total, getThreadState(thread).getEffectivePriority());                               
                if (ticketValue <= total ) {
                    nextThread = thread;    
                    break;
                }
            }
            return nextThread;
       }


    }

}
