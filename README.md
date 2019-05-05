# CSE180
## Operating Systems and NACHOS



### Sync project dir to the testing server

We recommend adding an alias to your ~/.bashrc

Simply replace `~/Repos/CSE150` with the location of this repo on your machine.

```bash
alias ossync='rsync -azP --delete ~/Repos/CSE150 s19-4l-g3@klwin00.ucmerced.edu:~/project2/'
```


### Useful commands for Project 2

We've got two convienent aliases setup on the testing server

Get to the project directory on the server by typing `p2dir`

Run the `test-subm proj2-test` easily by simply typing `test2`

~/.bashrc looks like
```bash
alias test2='test-subm proj2-test'
alias p2dir='cd ~/project2/CSE150/nachos'
```


### Run C/MIPS programs
#### for testing purposes

ssh into the server
`ssh s19-4l-g3@klwin00.ucmerced.edu`

Navigate to the project directory and run make in both locations.
```bash
p2dir
cd test
make
cd ../proj2
make
```
At this point if you haven't done this before type `chmod +x ../bin/nachos` to give yourself permissions to run nachos.

Finally run nachos
```bash
../bin/nachos -d ma
```

This allows us to trace what happens as the user program gets loaded, runs, and invokes a system call (the 'm' debug flag enables MIPS disassembly, and the 'a' debug flag
prints process loading information).


Run other test programs with
```
../bin/nachos -x PROGNAME.coff
```