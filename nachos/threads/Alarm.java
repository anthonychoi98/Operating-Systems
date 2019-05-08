package nachos.threads;

import nachos.machine.*;


import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
//created a threading class that will hold our thread with its associated wake Time
class threading{
	KThread thread;
	long wakeUpTime;
}
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
	
	
	//we will use a priority queue based on a threading objects that would prioritize
	//by wake up times, the threads with lower wake up times will be at the top of the queue
	//creating a min heap type of structure 
	static PriorityQueue<threading> wakeUpThreads = new PriorityQueue<threading>(1000, new Comparator<threading>(){
		@Override
		public int compare(threading thread1, threading thread2){ 
			if(thread1.wakeUpTime > thread2.wakeUpTime) return 1;//make switch, thread1 should be after thread2
		    else if(thread1.wakeUpTime < thread2.wakeUpTime) return -1; //correct spots thread1 should be before thread2
		    else return 0;
		
	}
		});
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }
    
    static boolean isEmpty(){
    	return wakeUpThreads.peek() == null;
    }
    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    	Machine.interrupt().disable();
    	
    	//we need to wake up all threads that wakeUpTime <= current Machine Time
    	//we need to put these back onto the ready queue to be executed
    	//while(!isEmpty() && wakeUpThreads.peek().wakeUpTime <=)
    	Iterator<threading> itr = wakeUpThreads.iterator(); //iterator for the priority queue
    	while(itr.hasNext()){ //while theres still threads in queue
    		if(wakeUpThreads.peek().wakeUpTime <= Machine.timer().getTime()){ //wake up thread
			//find next thread and put it in ready queue
			//remove from priority queue
    			wakeUpThreads.remove().thread.ready();
    			//wakeUpThreads.peek().thread.ready(); 
    			//wakeUpThreads.remove(); 
    		}
    		if(isEmpty() || wakeUpThreads.peek().wakeUpTime > Machine.timer().getTime()){
    			break; //no need to go over threads > machine timer
    		}
    	}
    	
    	KThread.currentThread().yield();
    	
    	Machine.interrupt().enable();
    	
    	KThread.yield();
    	
	
	
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
	//while (wakeTime > Machine.timer().getTime())
	  //  KThread.yield();
	
	threading threadToSleep = new threading(); //create new thread to put to sleep
	threadToSleep.thread = KThread.currentThread(); //get current thread we want to sleep for x time
	threadToSleep.wakeUpTime = wakeTime; //get associated wakeup time
	
	
	Machine.interrupt().disable();
	
	wakeUpThreads.add(threadToSleep);	//add thread to our priority queue
	KThread.sleep(); //current thread is slept
	
	Machine.interrupt().enable();
	
	
    }
}