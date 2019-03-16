package nachos.threads;

public class ThreadWithTime /*implements Comparable<ThreadWithTime>*/  {

    private KThread currentThread;
    private long waitTime;

    public ThreadWithTime (KThread thread, long time) {
        currentThread = thread;
        waitTime = time;
    }
    // function to get the wait time of the thread object
    public long getWaitTime() {
        return waitTime;
    }
    
    // function to get the thread object
    public KThread getThread() {
        return currentThread;
    }
    
    /*
    @Override
    public int compareTo(ThreadWithTime otherThreadTimer) {
        // Compare KThreads based on time asleep
        if (otherThreadTimer.waitTime < this.waitTime) {
            return 1;
        } else if (otherThreadTimer.waitTime == this.waitTime) {
            return 0;
        } else {
            return -1;
        }
    }
    */

}