package nachos.userprog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.*;
import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
//hold pid and corresponding userProcess and time it is put on queue 
/**TASK 3**/
class Process{
	int pid;
	UserProcess UserProcess;
	int time;
	
}
/**TASK 3**/ 
/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
    /**
     * Allocate a new user kernel.
     */
    public UserKernel() {
	super();
    }

    /**
     * Initialize this kernel. Creates a synchronized console and sets the
     * processor's exception handler.
     */
    public void initialize(String[] args) {
	super.initialize(args);
	
	processLock = new Lock();
	freePageList = new LinkedList<Integer>();
	processAllocations = new HashMap<UserProcess, ArrayList<Integer>>();
	console = new SynchConsole(Machine.console());
	
	// initialize free page list
	for (int i = 0; i < Machine.processor().getNumPhysPages(); i++) {
		freePageList.add(i);
	}
	
	Machine.processor().setExceptionHandler(new Runnable() {
		public void run() { exceptionHandler(); }
	    });
    }

    /**
     * Test the console device.
     */	
    public void selfTest() {
	super.selfTest();

	System.out.println("Testing the console device. Typed characters");
	System.out.println("will be echoed until q is typed.");

	char c;

	do {
	    c = (char) console.readByte(true);
	    console.writeByte(c);
	}
	while (c != 'q');

	System.out.println("");
    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
	if (!(KThread.currentThread() instanceof UThread))
	    return null;
	
	return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
	Lib.assertTrue(KThread.currentThread() instanceof UThread);

	UserProcess process = ((UThread) KThread.currentThread()).process;
	int cause = Machine.processor().readRegister(Processor.regCause);
	process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
	super.run();

	UserProcess process = UserProcess.newUserProcess();
	
	String shellProgram = Machine.getShellProgramName();	
	Lib.assertTrue(process.execute(shellProgram, new String[] { }));

	KThread.currentThread().finish();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }
    
    
    // Task 2 methods **********************************
    public static int allocate(UserProcess process) {
    	// want to allocate memory only if process holds the lock,
    	// if there are free pages.
    	if (!processLock.isHeldByCurrentThread()) {
    		return -1;
    	}
    	if (freePageList.isEmpty()) {
    		return -1;
    	}
    	int freePageNum = freePageList.poll();
    	if (processAllocations.containsKey(process)) {
    		processAllocations.get(process).add(freePageNum);
    	}
    	else {
    		// this is a new process, allocate memory for it.
    		ArrayList<Integer> arrList = new ArrayList<Integer>();
    		arrList.add(freePageNum);
    		processAllocations.put(process, arrList); // setup new key-value mapping
    	}
    	return freePageNum;
    }
    
    public void free(UserProcess process, int page) {
    	if (!processLock.isHeldByCurrentThread()) {
    		return;
    	}
    	if (!processAllocations.containsKey(process)) {
			return;
		}
    	if (freePageList.contains(page)) {
    		return;
    	}
    	processAllocations.get(process).remove(page);
    	freePageList.addLast(page);
    	return;
    }
    
    public static void freeAll(UserProcess process) {
    	if (!processLock.isHeldByCurrentThread()) {
    		return;
    	}
    	// check if process even has memory allocated
    	if (processAllocations.containsKey(process)) {
    		// if so, free all memory allocated.
    		freePageList.addAll(processAllocations.get(process));
    		processAllocations.remove(process);
    	}
    	return;
    }
    
    public static void acquire() {
    	processLock.acquire();
    }
    
    public static void release() {
    	processLock.release();
    }
    
    // ************************************************
    /**TASK 3 IMPLEMENTATIONS**/
	
     /**
     * 
     * 
     * @param pid
     * @return Process found with corresponding pid for userprocess in userProcessQ
     */
    public static Process findProcess(int pid) {
		Iterator<Process> itr = userProcessQ.iterator();
		while(itr.hasNext()) {
			Process found = itr.next();
			if(found.pid == pid) {
				return found;
			}
		}
		return null;
	}
	 /**
     * 
     * 
     * @param pid
     * @return UserProcess found with corresponding pid for userprocess in userProcessQ
     */
    public static UserProcess findUserProcess(int pid) {
		Iterator<Process> itr = userProcessQ.iterator();
		while(itr.hasNext()) {
			Process found = itr.next();
			if(found.pid == pid) {
				return found.UserProcess;
			}
		}
		return null;
	}
  /**
     * 
     * @return next pid kept in track by userKernel
     */
    public static int nextPid() {
    	       return UPpid;
    }
	
	/**
     * 
     * 
     * @param pid
     * @param proc
     * @return userprocess
     * add Process with id to Queue
     */
    
    public static UserProcess addProcess(int pid, UserProcess proc) {
    		
    		Machine.interrupt().disable();
    		Process newProcess = new Process();
    		newProcess.pid = pid;
    		newProcess.UserProcess = proc;
    		newProcess.time = time;
    		time++;
    		UPpid++;
    		userProcessQ.add(newProcess); //add to Queue next process
    		Machine.interrupt().enable();
    		
    		return newProcess.UserProcess;
    		
    			
    }
	/**Function to add a new page onto free pages if it is not there already**/
    public static void newPageFree(int num) {
    	
    	if(num < Machine.processor().getNumPhysPages() && num >-1 && !(freePageList.contains(num))) {
    		Machine.interrupt().disable();
    		freePageList.add(num);
    		Machine.interrupt().enable();
    	}
    }
	 /**
     * 
     * 
     * @param pid
     * 
     * @return UserProcess of removed process pid
     * 
     */
    public static UserProcess removeProcess(int pid) {
    		Process removeFound = null;
    		Machine.interrupt().disable();
    		removeFound = findProcess(pid);
    		if(removeFound != null) {
    			userProcessQ.remove(removeFound); //remove found item
    		}
    		Machine.interrupt().enable();
    		
    		return removeFound.UserProcess;
    }

/** END OF TASK 3 IMPLEMENTATIONS **/
	
	
    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;
    
    // Task 2 declarations
    private static Lock processLock;
    private static LinkedList<Integer> freePageList;
    private static HashMap<UserProcess, ArrayList<Integer>> processAllocations;
    
    // dummy variables to make javac smarter
    private static Coff dummy1 = null;
	
	/**TASK 3 VARIABLES AND DATA STRUCTURE**/
	//use priority queue to hold userProcess with pid
    static PriorityQueue<Process> userProcessQ = new PriorityQueue<Process>(1000, new Comparator<Process>(){
		@Override
		public int compare(Process p1, Process p2){ 
			if(p1.pid > p2.pid) return 1;//make switch, thread1 should be after thread2
		    else if(p1.pid < p2.pid) return -1; //correct spots thread1 should be before thread2
		    else return 0;
		
	}
		});
	
     //pid and time counters
    static int UPpid = 1;
    static int time = 0;
	
	/**END OF TASK 3 VARIABLES AND DATA STRUCTURE**/
	
}
