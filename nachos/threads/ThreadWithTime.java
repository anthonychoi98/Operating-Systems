package nachos.threads;

public class ThreadWithTime implements Comparable<ThreadWithTime>  {

    private KThread currentThread;
    private long waitTime;

    public ThreadWithTime (KThread thread, long time) {
        currentThread = thread;
        waitTime = time;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public KThread getThread() {
        return currentThread;
    }

    @Override
    public int compareTo(ThreadWithTime otherThreadTimer) {
        // TODO Compare KThreads based on time asleep
        if (otherThreadTimer.waitTime < this.waitTime) {
            return 1;
        } else if (otherThreadTimer.waitTime == this.waitTime) {
            return 0;
        } else {
            return -1;
        }
    }

}