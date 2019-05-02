package nachos.threads;

// import for priority queue
import java.util.PriorityQueue;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */

    // priorityQueue to keep threads organized based off wake times
    public PriorityQueue<ThreadWithTime> PQ = new PriorityQueue<ThreadWithTime>();

    public Alarm() {
        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() { timerInterrupt(); }
        });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    	
        while (PQ.peek() != null && PQ.peek().getWaitTime() <= Machine.timer().getTime()) {
            // removes newly woken thread from priority queue
            // wakes up threads with past wake times and puts them in execution
            PQ.remove().getThread().ready();

        }
        // allows for newly woken threads to execute
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
		/*
		while (wakeTime > Machine.timer().getTime())
	    KThread.yield();
		 */
        
        // create new ThreadWithTime object passing the new wake time
        ThreadWithTime twt = new ThreadWithTime(KThread.currentThread(), wakeTime);
        
        // disable the interrupt to add new thread object to priority queue 
        Machine.interrupt().disable();

        // add ThreadWithTime object to Priority Queue
        PQ.add(twt);

        // put current thread to sleep
        KThread.sleep();
        
        // enable interrupt for OS control
        Machine.interrupt().enable();
    }
}