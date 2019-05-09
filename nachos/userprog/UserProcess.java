	package nachos.userprog;
	
	import nachos.machine.*;
	import nachos.threads.*;
	import nachos.userprog.*;
	
	import java.io.EOFException;
	import java.util.HashMap;
	import java.util.LinkedList;
	import java.util.List;
	import java.util.*;
	
	/**
	 * Encapsulates the state of a user process that is not contained in its
	 * user thread (or threads). This includes its address translation state, a
	 * file table, and information about the program being executed.
	 *
	 * <p>
	 * This class is extended by other classes to support additional functionality
	 * (such as additional syscalls).
	 *
	 * @see	nachos.vm.VMProcess
	 * @see	nachos.network.NetProcess
	 */
	public class UserProcess {
		/**
		 * Allocate a new process.
		 */
		public UserProcess() {
	
			processID = nextPID;
			nextPID += 1;
			pids.put(processID, null);
			exitStatusCode = -255;
	
			// init read and open files for task 1
			openFilesMap.put(0, UserKernel.console.openForReading());
			openFilesMap.put(1, UserKernel.console.openForWriting());
			/**task 3 **/
			userProcessPID = UserKernel.nextPid();//need to know what our next pid assignment can be
			UserKernel.addProcess(userProcessPID, this); //add userProcess to userKernel for assignment
		}
	
		/**
		 * Allocate and return a new process of the correct class. The class name
		 * is specified by the <tt>nachos.conf</tt> key
		 * <tt>Kernel.processClassName</tt>.
		 *
		 * @return	a new process of the correct class.
		 */
		public static UserProcess newUserProcess() {
			return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
		}
	
		// The following are task 1 methods to implement.
		public int creat(String name) {
			int output;
			// open file
			OpenFile file = ThreadedKernel.fileSystem.open(name, true);
			if (file == null) {
				return -1;
			}
			openFilesMap.put(fdNext, file);
			output = fdNext;
			fdNext += 1;
			return output;
		}
	
		public int open(String name) {
			int output;
			OpenFile file = ThreadedKernel.fileSystem.open(name, false);
			if (file == null) {
				return -1;
			}
			openFilesMap.put(fdNext, file);
			output = fdNext;
			fdNext += 1;
			return output;
		}
	
	
		public int read(int fd, byte[] data, int numBytes) {
			OpenFile file;
			if (openFilesMap.containsKey(fd)) {
				file = openFilesMap.get(fd);
				return file.read(data, 0, data.length);
			}
			return -1;
		}
	
	
		public int write(int fd, byte[] data, int numBytesToWrite) {
			OpenFile file;
			if (openFilesMap.containsKey(fd)) {
				file = openFilesMap.get(fd);
				return file.write(data,  0, data.length);
			}
			return -1;
		}
	
	
		public int close(int fd) {
			OpenFile file;
			if (openFilesMap.containsKey(fd)) {
				file = openFilesMap.get(fd);
				file.close();
				openFilesMap.remove(fd);
				return 0;
			}
	
			return -1;
		}
	
	
		public int unlink(String name) {
			boolean fileRemoved = ThreadedKernel.fileSystem.remove(name);
			if (!fileRemoved) {
				return -1;
			}
			return 0;
		}
	
		public void closeAllFileDescriptors() {
			for (Integer fd : openFilesMap.keySet()) {
				openFilesMap.get(fd).close();
			}
			openFilesMap.clear();
		}
	
		// ******************************************************
	
	
		// Task 2 - page tables part ****************************
		public void initPageTable() {
			this.process = this;
			int numPhysPages = Machine.processor().getNumPhysPages();
			pageTable = new TranslationEntry[numPhysPages];
			for (int i=0; i<numPhysPages; i++)
				pageTable[i] = new TranslationEntry(i,i, false,false,false,false);
			return;
		}
	
		public int allocate(boolean readonly) {
			// allocate memory from UserKernel
			int output;
	
			// lock acquired so that other threads don't mess with the allocation process
			UserKernel.acquire();
			int p = UserKernel.allocate(process);
			UserKernel.release();
			if (p == -1) {
				return -1;
			}
			pageTable[nextVirtualAddr] = new TranslationEntry(
					nextVirtualAddr,
					p,
					true,
					readonly,
					false,
					false);
			output = nextVirtualAddr;
			nextVirtualAddr += 1;
			return output;
		}
	
		public void deAllocateMemory() {
			// Acquire lock and free all atomically
			UserKernel.acquire();
			UserKernel.freeAll(process);
			UserKernel.release();
			return;
		}
	
		public int translate(int virtualAddr) {
			int output;
			// get the virtual page number
			int vpn = virtualAddr / pageSize;
			// get page offset
			int pageOffset = virtualAddr % pageSize;
			// look for physical page number from page table using the virtual page number
			int ppn = find(vpn);
			// if not found, page fault
			if (ppn == -1) {
				return -1;
			}
			output = (ppn * pageSize) + pageOffset;
			return output;
		}
	
		public int find(int virtualPageNum) {
			// check if out of bounds
			if (virtualPageNum < 0 || virtualPageNum > Machine.processor().getNumPhysPages()) {
				return -1;
			}
			// check valid bit
			if (pageTable[virtualPageNum].valid) {
				return pageTable[virtualPageNum].ppn;
			}
			// otherwise, page translation not valid
			return -1;
		}
	
		public TranslationEntry[] getPageTable() {
			return this.pageTable;
		}
	
		// *****************************************************
		/**
		 * Execute the specified program with the specified arguments. Attempts to
		 * load the program, and then forks a thread to run it.
		 *
		 * @param	name	the name of the file containing the executable.
		 * @param	args	the arguments to pass to the executable.
		 * @return	<tt>true</tt> if the program was successfully executed.
		 */
		public boolean execute(String name, String[] args) {
			if (!load(name, args))
				return false;
	
			/**Task 3 change**/
			numOfProcesses += 1;
			UThread ut = new UThread(this);
			pids.put(processID, ut);
			ut.setName(name).fork();
	
			//	    currentThread = new UThread(this); //associate thread with userProcess
			//	    currentThread.setName(name).fork();
	
			//new UThread(this).setName(name).fork();
	
			return true;
		}
	
		/**
		 * Save the state of this process in preparation for a context switch.
		 * Called by <tt>UThread.saveState()</tt>.
		 */
		public void saveState() {
		}
	
		/**
		 * Restore the state of this process after a context switch. Called by
		 * <tt>UThread.restoreState()</tt>.
		 */
		public void restoreState() {
			Machine.processor().setPageTable(pageTable);
		}
	
		/**
		 * Read a null-terminated string from this process's virtual memory. Read
		 * at most <tt>maxLength + 1</tt> bytes from the specified address, search
		 * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
		 * without including the null terminator. If no null terminator is found,
		 * returns <tt>null</tt>.
		 *
		 * @param	vaddr	the starting virtual address of the null-terminated
		 *			string.
		 * @param	maxLength	the maximum number of characters in the string,
		 *				not including the null terminator.
		 * @return	the string read, or <tt>null</tt> if no null terminator was
		 *		found.
		 */
		public String readVirtualMemoryString(int vaddr, int maxLength) {
			Lib.assertTrue(maxLength >= 0);
	
			byte[] bytes = new byte[maxLength+1];
	
			int bytesRead = readVirtualMemory(vaddr, bytes);
	
			for (int length=0; length<bytesRead; length++) {
				if (bytes[length] == 0)
					return new String(bytes, 0, length);
			}
			return null;
		}
	
		/**
		 * Transfer data from this process's virtual memory to all of the specified
		 * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
		 *
		 * @param	vaddr	the first byte of virtual memory to read.
		 * @param	data	the array where the data will be stored.
		 * @return	the number of bytes successfully transferred.
		 */
		public int readVirtualMemory(int vaddr, byte[] data) {
			return readVirtualMemory(vaddr, data, 0, data.length);
		}
	
		/**
		 * Transfer data from this process's virtual memory to the specified array.
		 * This method handles address translation details. This method must
		 * <i>not</i> destroy the current process if an error occurs, but instead
		 * should return the number of bytes successfully copied (or zero if no
		 * data could be copied).
		 *
		 * @param	vaddr	the first byte of virtual memory to read.
		 * @param	data	the array where the data will be stored.
		 * @param	offset	the first byte to write in the array.
		 * @param	length	the number of bytes to transfer from virtual memory to
		 *			the array.
		 * @return	the number of bytes successfully transferred.
		 */
		public int readVirtualMemory(int vaddr, byte[] data, int offset,
				int length) {
			Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
	
			byte[] memory = Machine.processor().getMemory();
	
			// for now, just assume that virtual addresses equal physical addresses
			// virtual addresses do not equal physical addresses any more
			// get contents of physical memory from page table lookup.
			int physicalAddr = translate(vaddr);
			if (physicalAddr < 0 || physicalAddr >= memory.length)
				return 0;
	
			int amount = Math.min(length, memory.length-physicalAddr);
			System.arraycopy(memory, physicalAddr, data, offset, amount);
	
			return amount;
		}
	
		/**
		 * Transfer all data from the specified array to this process's virtual
		 * memory.
		 * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
		 *
		 * @param	vaddr	the first byte of virtual memory to write.
		 * @param	data	the array containing the data to transfer.
		 * @return	the number of bytes successfully transferred.
		 */
		public int writeVirtualMemory(int vaddr, byte[] data) {
			return writeVirtualMemory(vaddr, data, 0, data.length);
		}
	
		/**
		 * Transfer data from the specified array to this process's virtual memory.
		 * This method handles address translation details. This method must
		 * <i>not</i> destroy the current process if an error occurs, but instead
		 * should return the number of bytes successfully copied (or zero if no
		 * data could be copied).
		 *
		 * @param	vaddr	the first byte of virtual memory to write.
		 * @param	data	the array containing the data to transfer.
		 * @param	offset	the first byte to transfer from the array.
		 * @param	length	the number of bytes to transfer from the array to
		 *			virtual memory.
		 * @return	the number of bytes successfully transferred.
		 */
		public int writeVirtualMemory(int vaddr, byte[] data, int offset,
				int length) {
			Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
	
			byte[] memory = Machine.processor().getMemory();
	
			// for now, just assume that virtual addresses equal physical addresses
			// above assumption is no longer true
			int physicalAddr = translate(vaddr);
			if (physicalAddr < 0 || physicalAddr >= memory.length)
				return 0;
	
			int amount = Math.min(length, memory.length-physicalAddr);
			System.arraycopy(data, offset, memory, physicalAddr, amount);
	
			return amount;
		}
	
		/**
		 * Load the executable with the specified name into this process, and
		 * prepare to pass it the specified arguments. Opens the executable, reads
		 * its header information, and copies sections and arguments into this
		 * process's virtual memory.
		 *
		 * @param	name	the name of the file containing the executable.
		 * @param	args	the arguments to pass to the executable.
		 * @return	<tt>true</tt> if the executable was successfully loaded.
		 */
		private boolean load(String name, String[] args) {
			Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
	
			// initialize page table
			initPageTable();
	
			OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
			if (executable == null) {
				Lib.debug(dbgProcess, "\topen failed");
				return false;
			}
	
			try {
				coff = new Coff(executable);
			}
			catch (EOFException e) {
				executable.close();
				Lib.debug(dbgProcess, "\tcoff load failed");
				return false;
			}
			// make sure the sections are contiguous and start at page 0
			numPages = 0;
			for (int s=0; s<coff.getNumSections(); s++) {
				CoffSection section = coff.getSection(s);
				if (section.getFirstVPN() != numPages) {
					coff.close();
					Lib.debug(dbgProcess, "\tfragmented executable");
					return false;
				}
				numPages += section.getLength();
			}
	
			// make sure the argv array will fit in one page
			byte[][] argv = new byte[args.length][];
			int argsSize = 0;
			for (int i=0; i<args.length; i++) {
				argv[i] = args[i].getBytes();
				// 4 bytes for argv[] pointer; then string plus one for null byte
				argsSize += 4 + argv[i].length + 1;
			}
			if (argsSize > pageSize) {
				coff.close();
				Lib.debug(dbgProcess, "\targuments too long");
				return false;
			}
	
			// program counter initially points at the program entry point
			initialPC = coff.getEntryPoint();	
	
			// next comes the stack; stack pointer initially points to top of it
			numPages += stackPages;
			initialSP = numPages*pageSize;
	
			// and finally reserve 1 page for arguments
			numPages++;
	
			if (!loadSections()) {
				deAllocateMemory();
				return false;
			}
	
			for (int i = 0; i < stackPages; i++) {
				allocate(false);
			}
	
			int virtualPageNum = allocate(false);
			// store arguments in last page
			int entryOffset = (virtualPageNum)*pageSize;
			int stringOffset = entryOffset + args.length*4;
	
			this.argc = args.length;
			this.argv = entryOffset;
	
			for (int i=0; i<argv.length; i++) {
				byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
				Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
				entryOffset += 4;
				Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
						argv[i].length);
				stringOffset += argv[i].length;
				Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
				stringOffset += 1;
			}
	
			return true;
		}
	
		/**
		 * Allocates memory for this process, and loads the COFF sections into
		 * memory. If this returns successfully, the process will definitely be
		 * run (this is the last step in process initialization that can fail).
		 *
		 * @return	<tt>true</tt> if the sections were successfully loaded.
		 */
		protected boolean loadSections() {
			if (numPages > Machine.processor().getNumPhysPages()) {
				coff.close();
				Lib.debug(dbgProcess, "\tinsufficient physical memory");
				return false;
			}
	
			// load sections
			for (int s=0; s<coff.getNumSections(); s++) {
				CoffSection section = coff.getSection(s);
	
				Lib.debug(dbgProcess, "\tinitializing " + section.getName()
				+ " section (" + section.getLength() + " pages)");
	
				for (int i=0; i<section.getLength(); i++) {
					int vpn = allocate(section.isReadOnly());
					if (vpn == -1) {
						coff.close();
						return false;
					}
	
					// for now, just assume virtual addresses=physical addresses
					// once again, above assumption no longer the case.
					section.loadPage(i, find(vpn));
				}
			}
	
			return true;
		}
	
		/**
		 * Release any resources allocated by <tt>loadSections()</tt>.
		 */
		protected void unloadSections() {
		}    
	
		/**
		 * Initialize the processor's registers in preparation for running the
		 * program loaded into this process. Set the PC register to point at the
		 * start function, set the stack pointer register to point at the top of
		 * the stack, set the A0 and A1 registers to argc and argv, respectively,
		 * and initialize all other registers to 0.
		 */
		public void initRegisters() {
			Processor processor = Machine.processor();
	
			// by default, everything's 0
			for (int i=0; i<processor.numUserRegisters; i++)
				processor.writeRegister(i, 0);
	
			// initialize PC and SP according
			processor.writeRegister(Processor.regPC, initialPC);
			processor.writeRegister(Processor.regSP, initialSP);
	
			// initialize the first two argument registers to argc and argv
			processor.writeRegister(Processor.regA0, argc);
			processor.writeRegister(Processor.regA1, argv);
		}
	
		/**
		 * Handle the halt() system call. 
		 */
		private int handleHalt() {
			// TODO
			// do a check to see if this is the root process
			// if process is NOT root process, then don't allow it to call Machine.halt()
			if (this.processID == 0) {
				Machine.halt();
			}
	
	
			//Lib.assertNotReached("Machine.halt() did not halt machine!");
			return 0;
		}
	
		//	/**TASK 3 IMPLEMENTATIONS **/
	
	
		private void TerminateKernel() {
			Kernel.kernel.terminate();
		}
		//	/**
		// * Exit function for syscalls
		// * @param status
		// */
		private int exit(int status) {
			closeAllFileDescriptors();
			deAllocateMemory();
	
			this.exitStatusCode = status;
	
			numOfProcesses -= 1;
	
			if (numOfProcesses == 0) {
				TerminateKernel();
			}
	
			UThread.finish();
			return 0;
	
		}
	
		//	/**
		//     * exec function for syscalls
		//     * @param file
		//     * @param argc
		//     * @param argv
		//     * @return 
		//     */
		//   
		private int exec(int a0, int a1, int a2) {
			String fileRead = readVirtualMemoryString(a0, 256);
			if(fileRead == null) {return -1;}
			String lookForExtension = fileRead.substring(fileRead.length()-5, fileRead.length());
			if(!lookForExtension.equals(".coff")){
				//System.out.println("Extension not .coff");
			}
			byte[] data = new byte[4];
			String[] arguments = new String[a1];
	
			int i = 0;
			while( i < a1) {
				int count = readVirtualMemory(a2 + i*4, data);
				if(count == 4) {
	
				}else {
					return -1;
				}
				arguments[i] = readVirtualMemoryString(Lib.bytesToInt(data, 0), 256);
				i++;
			}
			if(arguments == null) {
				arguments = new String[] {};
			}
	
			UserProcess process = UserProcess.newUserProcess();
			process.parent = this;
			child.add(process);
			if (process.execute(fileRead, arguments)) {
				return process.processID;
			}
			child.remove(process);
			return -1;
	
	
	
		}
	
	
	
		/**
		 * Helper function to find and remove child in Child Queue
		 * @param childPID
		 * @return true if found and removed
		 */
		boolean findAndRemoveChild(int childPID) {
			int PIDcheck;
			Iterator<Integer> itr = childrenProcesses.iterator();
			while(itr.hasNext()) {
				PIDcheck = itr.next();
				if(PIDcheck == childPID) {
					childrenProcesses.remove(PIDcheck);
					return true; //found and removed 
				}
			}
			return false;
	
		}
	
	
		/**
		 * Join function for syscalls
		 * @param cPid
		 * @param addr
		 * @return
		 */
		//   
		boolean findAndRemove(int pid) {
			if(pids.containsKey(pid)) {
				return true;
			}else {
				return false;
			}
	
		}
	
		private int join(int pid, int addr) {
			int exitCode = -255;
			if(!findAndRemove(pid) ) {return -1;}
			UThread thread = pids.get(pid);
			if(thread != null && addr > 0) {
	
				int test;
	
				UserProcess UProcess = thread.process;
				if (UProcess.parent == this) {
					thread.join();
					if (thread.process.exitStatusCode == -255) {
						test = 0;
					}
					test = 1;
				}else {
					return -1;
				}
	
	
				exitCode = thread.process.exitStatusCode;
				byte data[] = new byte[4];                                         
				data=Lib.bytesFromInt(exitCode);
				writeVirtualMemory(addr, data);
				return test;
			}
	
			return -1;
	
	
		}
	
	
	
	
	
	
		/**End of TASK3 **/
	
		private static final int
		syscallHalt = 0,
		syscallExit = 1,
		syscallExec = 2,
		syscallJoin = 3,
		syscallCreate = 4,
		syscallOpen = 5,
		syscallRead = 6,
		syscallWrite = 7,
		syscallClose = 8,
		syscallUnlink = 9;
	
		/**
		 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
		 * <i>syscall</i> argument identifies which syscall the user executed:
		 *
		 * <table>
		 * <tr><td>syscall#</td><td>syscall prototype</td></tr>
		 * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
		 * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
		 * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
		 * 								</tt></td></tr>
		 * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
		 * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
		 * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
		 * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
		 *								</tt></td></tr>
		 * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
		 *								</tt></td></tr>
		 * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
		 * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
		 * </table>
		 * 
		 * @param	syscall	the syscall number.
		 * @param	a0	the first syscall argument.
		 * @param	a1	the second syscall argument.
		 * @param	a2	the third syscall argument.
		 * @param	a3	the fourth syscall argument.
		 * @return	the value to be returned to the user.
		 */
		public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
			int MAX_FILENAME_SIZE = 256;
			String str1;
			String[] arguments;
			byte[] buffer;
			int ret;
			int m;
			UThread t;
	
	
			switch (syscall) {
			case syscallHalt:
				return handleHalt();
			case syscallCreate:
				str1 = readVirtualMemoryString(a0, MAX_FILENAME_SIZE);
				if (str1 == null) {
					return -1;
				}
				return creat(str1);
			case syscallOpen:
				str1 = readVirtualMemoryString(a0, MAX_FILENAME_SIZE);
				if (str1 == null) {
					return -1;
				}
				return open(str1);
	
			case syscallRead:
				if (a2 > pageSize) {
					return -1;
				}
				buffer = new byte[a2];
				ret = read(a0, buffer, a2);
				if (ret == -1) {
					return ret;
				}
				writeVirtualMemory(a1, buffer, 0, ret);
				return ret;
			case syscallWrite:
				if (a2 > pageSize) {
					return -1;
				}
				buffer = new byte[a2];
				readVirtualMemory(a1, buffer, 0, a2);
				return write(a0, buffer, a2);
			case syscallClose:
				return close(a0);
			case syscallUnlink:
				str1 = readVirtualMemoryString(a0, MAX_FILENAME_SIZE);
				if (str1 == null) {
					return -1;
				}
				return unlink(str1);
			case syscallExit:
				return exit(a0);
			case syscallExec:
				return exec(a0, a1, a2);
			case syscallJoin:
	
				return join(a0,a1);
			default:
				Lib.debug(dbgProcess, "Unknown syscall " + syscall);
				Lib.assertNotReached("Unknown system call!");
			}
			return 0;
		}
	
		/**
		 * Handle a user exception. Called by
		 * <tt>UserKernel.exceptionHandler()</tt>. The
		 * <i>cause</i> argument identifies which exception occurred; see the
		 * <tt>Processor.exceptionZZZ</tt> constants.
		 *
		 * @param	cause	the user exception that occurred.
		 */
		public void handleException(int cause) {
			Processor processor = Machine.processor();
	
			switch (cause) {
			case Processor.exceptionSyscall:
				int result = handleSyscall(processor.readRegister(Processor.regV0),
						processor.readRegister(Processor.regA0),
						processor.readRegister(Processor.regA1),
						processor.readRegister(Processor.regA2),
						processor.readRegister(Processor.regA3)
						);
				processor.writeRegister(Processor.regV0, result);
				processor.advancePC();
				break;				       
	
			default:
				Lib.debug(dbgProcess, "Unexpected exception: " +
						Processor.exceptionNames[cause]);
				Lib.assertNotReached("Unexpected exception");
			}
		}
	
		/** The program being run by this process. */
		protected Coff coff;
	
		/** This process's page table. */
		protected TranslationEntry[] pageTable;
		/** The number of contiguous pages occupied by the program. */
		protected int numPages;
	
		/** The number of pages in the program's stack. */
		protected final int stackPages = 8;
	
		private int initialPC, initialSP;
		private int argc, argv;
	
		private static final int pageSize = Processor.pageSize;
		private static final char dbgProcess = 'a';
	
		// Task 1 declarations ***********************
		private HashMap<Integer, OpenFile> openFilesMap = new HashMap<Integer, OpenFile>();;
		private int fdNext = 2;
	
		// *******************************************
	
		// Task 2 declarations ***********************
	
		private UserProcess process;
		private int nextVirtualAddr = 0;
	
		// *******************************************
		/**TASK 3 DECLARATIONS**/
		//userprocess pid
		int userProcessPID;
		int parentPID; //parentsPid
		int statusOfExit; //exiting status for exit()
		UThread currentThread; // thread to associated process
	
		public int processID, exitStatusCode;
		public UserProcess parent = null;
		private LinkedList<UserProcess> child = new LinkedList<UserProcess>();
		private static HashMap<Integer, UThread> pids = new HashMap<Integer, UThread>();
		private static int nextPID = 0;
		private static int numOfProcesses = 0;
		//Queue to hold all child processes of parent process
		static PriorityQueue<Integer> childrenProcesses = new PriorityQueue<Integer>();
		/** END OF TASK 3**/
	
	
	}
