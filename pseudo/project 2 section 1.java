public int creat(int name){
    // function doesn't take argument because it's only ever used for fileTable purposes.
    int fileDescriptorIndex = findFreeFileDescriptor();

    // adds file to virtual memory file table
    String filename = readVirtualMemoryString(name, maxbyte);

    // opens file according to ThreadedKernel syntax
    OpenFile file = ThreadedKernel.fileSystem.open(filename, true);

    // value is -1 if no index found
    if (fileDescriptorIndex == -1){
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
    fileTable[fileDescriptorIndex] = file;
    return fileDescriptorIndex;
}

private int open(int name){
    // same lines as creat() except
    // this time we pass false for ThreadedKernel.fileSystem.open as 
    int fileDescriptorIndex = findFreeFileDescriptor();
    String filename = readVirtualMemoryString(name, maxbyte);
    OpenFile file = ThreadedKernel.fileSystem.open(filename, false);

    // if there is no available index, return -1
    if (fileDescriptorIndex == -1){
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

    // open() and creat() are very simliar
    fileTable[fileDescriptorIndex] = file;
    return fileDescriptorIndex;
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
        retunr -1;
    }

    // checks of size makes sense
    if (size < 0){
        return -1;
    }

    // reads from the fileTable
    readBytes = fileTable[fileDescriptor].read();
    // writes to the virtual address space
    readBytes = writeVirtualMemory();

    //TODO checks 

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
        retunr -1;
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
        } else {
            return -1;
        }
    }
}


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


// reference variables (already declared in project)
//The number of contiguous pages occupied by the program.
protected int numPages;