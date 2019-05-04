package nachos.userprog;

import nachos.machine.*;
import nachos.threads.KThread;
import nachos.threads.ThreadedKernel;

import java.io.EOFException;
import java.util.Iterator;
import java.util.PriorityQueue;

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

		// allocates 16 files for the file table.
		fileTable = new OpenFile[16];


		fileTable[0] = UserKernel.console.openForReading();
		fileTable[1] = UserKernel.console.openForWriting();

		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];
		for (int i=0; i<numPhysPages; i++)
			pageTable[i] = new TranslationEntry(i,i, true,false,false,false);

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
		currentThread = new UThread(this); //associate thread with userProcess
		currentThread.setName(name).fork();
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
        
        int transferred = 0;
        while (length > 0 && offset < data.length) {
        	int addrOffset = vaddr % 1024;
        	int virtualPage = vaddr / 1024;
        	
        	if (virtualPage >= pageTable.length || virtualPage < 0) {
        		break;
        	}
        	TranslationEntry pte = pageTable[virtualPage];
        	if (!pte.valid) {
        		break;
        	}
        	pte.used = true;
        	
        	int physPage = pte.ppn;
        	int physAddr = physPage * 1024 + addrOffset;
        	
        	int transferLength = Math.min(data.length-offset, Math.min(length, 1024-addrOffset));
        	System.arraycopy(memory, physAddr, data, offset, transferLength);
        	vaddr += transferLength;
        	offset += transferLength;
        	length -= transferLength;
        	transferred += transferLength;
        }
        
        return transferred;
//		Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
//
//		byte[] memory = Machine.processor().getMemory();
//
//		int transfer = 0;
//		int VP = vaddr/Processor.pageSize;
//		int addressOffset = vaddr % Processor.pageSize;
//		
//		
//		while (length > 0 && offset < data.length) {
//			//get virtual page and address offset
////			int VP = vaddr/Processor.pageSize;
////			int addressOffset = vaddr % Processor.pageSize;
//			
//			//check that virtual page is valid
//			if (VP >= pageTable.length || VP < 0) {
//				break;
//			}
//			//create page table entry
//			TranslationEntry pageTableEntry = pageTable[VP];
//			//check that page table entry is valid
//			if (!pageTableEntry.valid) {
//				break;
//			}
//			//mark page table entry as used
//			pageTableEntry.used = true;
//			//get physical page and address to read virtual memory
//			int physicalPage = pageTableEntry.ppn;
//			int physicalAddress = physicalPage * Processor.pageSize + addressOffset;
//			int amount = Math.min(data.length-offset, Math.min(length, Processor.pageSize-addressOffset));
//			System.arraycopy(memory, physicalAddress, data, offset, amount);
//			offset += amount;
//			length -= amount;
//			vaddr += amount;
//			transfer += amount;
//			
//			addressOffset = vaddr % Processor.pageSize;
//			VP = vaddr/Processor.pageSize;
//		}
//		return transfer;
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
	        
	        int transferred = 0;
	        while (length > 0 && offset < data.length) {
	        	int addrOffset = vaddr % 1024;
	        	int virtualPage = vaddr / 1024;
	        	
	        	if (virtualPage >= pageTable.length || virtualPage < 0) {
	        		break;
	        	}
	        	
	        	TranslationEntry pte = pageTable[virtualPage];
	        	if (!pte.valid || pte.readOnly) {
	        		break;
	        	}
	        	pte.used = true;
	        	pte.dirty = true;
	        	
	        	int physPage = pte.ppn;
	        	int physAddr = physPage * 1024 + addrOffset;
	        	
	        	int transferLength = Math.min(data.length-offset, Math.min(length, 1024-addrOffset));
	        	System.arraycopy(data, offset, memory, physAddr, transferLength);
	        	vaddr += transferLength;
	        	offset += transferLength;
	        	length -= transferLength;
	        	transferred += transferLength;
	        }
	        
	        return transferred;
	        
//		Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
//
//		byte[] memory = Machine.processor().getMemory();
//		int addressOffset = vaddr % pageSize;
//		int VP = vaddr / pageSize;
//
//		int transfer = 0;
//		
//		while (length > 0 && offset < data.length && VP < pageTable.length && VP > 0) {
//
////			if (VP >= pageTable.length || VP < 0) {
////				break;
////			}
//
//			TranslationEntry pageTableEntry = pageTable[VP];
//			if (!pageTableEntry.valid || pageTableEntry.readOnly) {
//				break;
//			}
//			pageTableEntry.used = true;
//			pageTableEntry.dirty = true;
//
//			int physicalPage = pageTableEntry.ppn;
//			int physicalAddress = physicalPage * pageSize + addressOffset;
//			//check that physical address makes sense
//			if(physicalAddress < 0 || physicalAddress >= memory.length) {
//				return 0;
//			}
//			
//			//amount is minimum byte length to transfer
//			int amount = Math.min(data.length-offset, Math.min(length, pageSize-addressOffset));
//			//update
//			System.arraycopy(data, offset, memory, physicalAddress, amount);
//			offset += amount;
//			length -= amount;
//			vaddr += amount;
//			transfer += amount;
//			
//			addressOffset = vaddr % pageSize;
//			VP = vaddr / pageSize;
//		}
//
//		return transfer;
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

		if (!loadSections())
			return false;

		// store arguments in last page
		int entryOffset = (numPages-1)*pageSize;
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


		pageTable = new TranslationEntry[numPages];

		for (int i=0; i<numPages; i++) {
			int physPage = UserKernel.allocatePage();
			if (physPage < 0) {
				Lib.debug(dbgProcess, "\tunable to allocate pages; tried " + numPages + ", did " + i );
				for (int j=0; j<i; j++) {
					if (pageTable[j].valid) {
						UserKernel.deallocatePage(pageTable[j].ppn);
						pageTable[j].valid = false;
					}
				}
				coff.close();
				return false;
			}
			pageTable[i] = new TranslationEntry(
					i, physPage, true, false, false, false);
		}


		// load sections
		for (int s=0; s<coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);

			Lib.debug(dbgProcess, "\tinitializing " + section.getName()
			+ " section (" + section.getLength() + " pages)");


			for (int i=0; i<section.getLength(); i++) {
				int vpn = section.getFirstVPN()+i;

				// for now, just assume virtual addresses=physical addresses
				int ppn = pageTable[vpn].ppn;
				section.loadPage(i, ppn);
				if (section.isReadOnly()) {
					pageTable[vpn].readOnly = true;
				}
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

		Machine.halt();

		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;
	}

	/**TASK 3 IMPLEMENTATIONS **/
	/**
	 * Exit function for syscalls
	 * @param status
	 */
	public void exit(int status) {


		//close open file descriptors belonging to the process
		// TODO WRITE THIS FUNCTION
		//		closeAllFileDescriptors();


		while(!childrenProcesses.isEmpty()) {
			UserProcess childP = UserKernel.findUserProcess(childrenProcesses.peek());
			childP.parentPID = 1;
			childrenProcesses.remove();

		}
		statusOfExit = status;

		//release all resources allocated 
		for(int i = 0; i < numPages;i++) {
			UserKernel.newPageFree(pageTable[i].ppn);
			pageTable[i].valid = false;
		}
		if(this.userProcessPID == 1) {
			Kernel.kernel.terminate();
		}else {
			KThread.currentThread().finish();
		}

		Lib.assertNotReached();
	}
	/**
	 * exec function for syscalls
	 * @param file
	 * @param argc
	 * @param argv
	 * @return 
	 */
	public int exec(int file, int argc, int argv) {
		String fileRead = readVirtualMemoryString(file, 256);

		if(argc < 1) {return -1;}

		String arguments[] = new String[argc];
		byte data[] = new byte[4];
		//look for .coff extension
		String lookForExtension = fileRead.substring(fileRead.length()-5, fileRead.length());
		//check if less than one arg and null for file read and for file extension .coff
		if(argc < 1 || fileRead ==null || !(lookForExtension.equals(".coff"))) return -1;


		int i = 0;
		while( i < argc) {
			int count = readVirtualMemory(argv + i*4, data);
			if(count == 4) {

			}else {
				return -1;
			}
			arguments[i] = readVirtualMemoryString(Lib.bytesToInt(data, 0), 256);
			i++;
		}


		//now we can create a new child process to be added to his parent userProcess
		UserProcess child = UserProcess.newUserProcess();
		child.parentPID = this.userProcessPID;
		this.childrenProcesses.add(child.userProcessPID); //add to queue

		if(child.execute(fileRead, arguments)) {
			return child.userProcessPID;
		}








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
	public int Join(int cPid, int addr) {

		if(!findAndRemoveChild(cPid)) { return -1;} //not found in parent child PID list


		UserProcess childToJoin = UserKernel.findUserProcess(cPid);


		if(childToJoin != null) { //hasnt joined yet
			childToJoin.currentThread.join();
			//removed from kernels Queue of userprocess
			UserKernel.removeProcess(cPid);

			if(writeVirtualMemory(addr, Lib.bytesFromInt(childToJoin.statusOfExit)) != 4) {
				return 1;
			}else {
				return 0;
			}


		}


		return -2;
	}
	/**End of TASK3 **/


	//  ____            _     _
	// |  _ \ __ _ _ __| |_  / |
	// | |_) / _` | '__| __| | |
	// |  __/ (_| | |  | |_  | |
	// |_|   \__,_|_|   \__| |_|



	public int creat(int name){
		// function doesn't take argument because it's only ever used for fileTable purposes.
		int fileDescriptor = findFreeFileDescriptor();

		// adds file to virtual memory file table
		String filename = readVirtualMemoryString(name, maxbyte);

		// opens file according to ThreadedKernel syntax
		OpenFile file = ThreadedKernel.fileSystem.open(filename, true);

		// value is -1 if no index found
		if (fileDescriptor == -1){
			return -1;
		}

		//if filename doesn't exist -- TODO maybe check if length == 0
		if (filename == null){
			return -1;
		}

		// if file doesn't exist
		if (file == null){
			return -1;
		}

		// all checks passed, continuing
		fileTable[fileDescriptor] = file;
		return fileDescriptor;
	}

	private int open(int name){
		// same lines as creat() except
		// this time we pass false for ThreadedKernel.fileSystem.open as
		int fileDescriptor = findFreeFileDescriptor();
		String filename = readVirtualMemoryString(name, maxbyte);
		OpenFile file = ThreadedKernel.fileSystem.open(filename, false);

		// if there is no available index, return -1
		if (fileDescriptor == -1){
			return -1;
		}

		//if filename doesn't exist -- TODO maybe check if length == 0
		if (filename == null){
			return -1;
		}

		// if file doesn't exist
		if (file == null){
			return -1;
		}

		// open() and creat() are very similar
		fileTable[fileDescriptor] = file;
		return fileDescriptor;
	}

	private int read(int fileDescriptor, int buffer, int size){
		int readBytes = 0;
		byte[] byteBuff = new byte[size];

		// checks for invalid fd
		if (fileDescriptor < 0 || fileDescriptor >= maxfileTableValue){
			return -1;
		}

		// checks if entry exists in fileTable
		if (fileTable[fileDescriptor] == null) {
			return -1;
		}

		// checks of size makes sense
		if (size < 0){
			return -1;
		}

		// reads from the fileTable
		readBytes = fileTable[fileDescriptor].read(byteBuff, readBytes, size - readBytes);
		// writes to the virtual address space
		boolean condition = writeVirtualMemory(buffer,byteBuff,0,readBytes) != readBytes;

		if(condition){
			return -1;
		}

		//TODO rename above

		// returns bytes that have been read
		return readBytes;
	}

	private int write(int fileDescriptor, int buffer, int size){
		// this function closes fd
		// if fd refers to a file written by write() will be flushed to disk before close()

		// checks for invalid fd
		if (fileDescriptor < 0 || fileDescriptor >= maxfileTableValue){
			return -1;
		}

		// checks if entry exists in fileTable
		if (fileTable[fileDescriptor] == null) {
			return -1;
		}

		// checks of size makes sense
		if (size < 0){
			return -1;
		}

		// if not finish current thread and return -1;

		byte[] byteBuff = new byte[size];
		OpenFile wriFi = fileTable[fileDescriptor];
		int bytes = readVirtualMemory(buffer, byteBuff);

		// checks if the bytes are equal to the size tha was passed to write()
		// this check is before before we try to write
		if (bytes != size){
			return -1;
		}

		// writes the bytes with our write file from 0 to size
		bytes = wriFi.write(byteBuff, 0, size);

		// returns -1 if we fail to write bytes
		if (bytes == -1){
			return -1;
		}

		return bytes;
	}

	public int close(int fileDescriptor){
		// checks for invalid fd
		if (fileDescriptor < 0 || fileDescriptor >= maxfileTableValue){
			return -1;
		}

		// declaration of close file object
		OpenFile cloFi = fileTable[fileDescriptor];

		// checks if size < 0
		if (cloFi == null || cloFi.length() < 0){
			return -1;
		}

		// closes close file
		cloFi.close();

		// if the length is not -1 something is wrong
		if (cloFi.length() != -1){
			return -1;
		}

		// sets the table entry to null
		fileTable[fileDescriptor] = null;

		// completed successfully
		return 0;
	}

	public int unlink(int name){
		String filename = readVirtualMemoryString(name, maxbyte);

		// checks if filename is valid
		if (filename.length() == 0 || filename == null){
			return -1;
		}

		// if fails to unlink, returns
		if (!UserKernel.fileSystem.remove(filename)){
			return -1;
		} else {
			return 0; // successfully unlinks file
		}

	}


	// ____ ____ _  _ ___  ____ _  _ _ ____ _  _  ____ _  _ _  _ ____ ___ _ ____ _  _ ____
	// |    |  | |\/| |__] |__| |\ | | |  | |\ |  |___ |  | |\ | |     |  | |  | |\ | [__
	// |___ |__| |  | |    |  | | \| | |__| | \|  |    |__| | \| |___  |  | |__| | \| ___]


	//TODO extract more functions from repeated code

	// searches fileTable for a space that's null
	public int findFreeFileDescriptor(){

		for (int i = 0; i < maxfileTableValue; i++){
			if (fileTable[i] == null){
				return i;
			}
		}

		// if there is no free file descriptor found, return -1
		return -1;
	}

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
		switch (syscall) {
		case syscallHalt:
			return handleHalt();
		case syscallExit:
			exit(a0);
			return 0;
		case syscallExec:
			return exec(a0,a1,a2);
		case syscallJoin:
			return Join(a0,a1);
		case syscallCreate:
			return creat(a0);
		case syscallOpen:
			return open(a0);
		case syscallRead:
			return read(a0, a1, a2);
		case syscallWrite:
			return write(a0, a1, a2);
		case syscallClose:
			return close(a0);
		case syscallUnlink:
			return unlink(a0);


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

	/**TASK 3 DECLARATIONS**/
	//userprocess pid
	int userProcessPID;
	int parentPID; //parentsPid
	int statusOfExit; //exiting status for exit()
	UThread currentThread; // thread to associated process

	//Queue to hold all child processes of parent process
	static PriorityQueue<Integer> childrenProcesses = new PriorityQueue<Integer>();
	/** END OF TASK 3**/

	// ____ ____ _  _ ___  ____ _  _ _ ____ _  _  _  _ ____ ____ _ ____ ___  _    ____ ____
	// |    |  | |\/| |__] |__| |\ | | |  | |\ |  |  | |__| |__/ | |__| |__] |    |___ [__
	// |___ |__| |  | |    |  | | \| | |__| | \|   \/  |  | |  \ | |  | |__] |___ |___ ___]

	// defines max fileTable value
	// TODO add better name
	private static final int maxfileTableValue = 16;
	// defines maximum byte value
	private static final int maxbyte = 256;
	// I wonder if we could make this a vector for fun
	private OpenFile[] fileTable;


}
