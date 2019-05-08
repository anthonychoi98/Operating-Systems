package nachos.threads;
import nachos.ag.BoatGrader;
import java.util.*;
import nachos.machine.*;

public class Boat
{
	static BoatGrader bg;

	static Lock Boat = new Lock();

	static Condition2 Oahu = new Condition2(Boat); // The boat is at Oahu...
	static Condition2 Molokai = new Condition2(Boat); // The boat is at Molokai...
	static Condition2 Full = new Condition2(Boat); // wait for boat to be full

	static Communicator comm = new Communicator();

	static int adultsOnA;
	static int adultsOnB;
	static int childrenOnA;
	static int childrenOnB;
	static int passengers;
	// static int childrenFromBoat; // children that arrived at an island...
	static boolean boatOnA;


	/* -------------METHODOLOGY-------------
	All children go to Molokai in groups of 2 (if possible)

	When there is 1 child or no children left on Oahu,
	an adult rows from Oahu to Molokai, and 1 children rows back to Oahu.
	This happens twice, and there's 2 children on Oahu,
	then they both go to Molokai together

	this repeats until there are no adults on Oahu
	remaining children on Oahu go to Molokai
	FINISHED
	*/
	public static void selfTest()
	{
		BoatGrader b = new BoatGrader();

		// System.out.println("\n ***Testing Boats with only 2 children***");
		// begin(0, 2, b);

		// System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		// begin(1, 2, b);

		// System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		// begin(3, 3, b);
	}

	public static void begin( int adults, int children, BoatGrader b )
	{
		Lib.assertTrue(children >= 2);
		Lib.assertTrue(b != null);
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		System.out.println("### CHILDREN: " + children + "\n### ADULTS: " + adults);

		// Instantiate global variables here
		adultsOnA = adults;
		adultsOnB = 0;
		childrenOnA = children;
		childrenOnB = 0;
		passengers = 0;
		// childrenFromBoat = 0;
		boatOnA = true;

		Runnable c = new Runnable()
		{
			public void run() { ChildItinerary(); }
		};

		Runnable a = new Runnable()
		{
			public void run() { AdultItinerary(); }
		};

		for(int i = 0; i < children; i++) // create child threads
		{
			System.out.println("...CREATING CHILD...");
			KThread t = new KThread(c);
			t.setName("child" + i);
			t.fork();
		}

		for(int i = 0; i < adults; i++) // create adult threads
		{
			System.out.println("...CREATING ADULT...");
			KThread t = new KThread(a);
			t.setName("adult" + i);
			t.fork();
		}

		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.

		// Runnable r = new Runnable()
		// {
		// 	public void run()
		// 	{
		// 			SampleItinerary();
		// 	}
		// };
		// KThread t = new KThread(r);
		// t.setName("Sample Boat Thread");
		// t.fork();

		int word = comm.listen();
        while(word != children + adults)
        {
                System.out.println("------- WORD RECEIVED: " + word);
                word = comm.listen();
        }
	}

	static void AdultItinerary()
	{
		// bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
		//DO NOT PUT ANYTHING ABOVE THIS LINE.

		/* This is where you should put your solutions. Make calls
		   to the BoatGrader to show that it is synchronized. For
		   example:
			   // bg.AdultRowToMolokai();
		   indicates that an adult has rowed the boat across to Molokai
		*/

		// lock Boat because an adult is accessing it
		Boat.acquire();

		System.out.println("...Start of Adult Itinerary...");

		int val = boatOnA ? 1 : 0; // used to cheat java compiler, 0 = false, 1 = true

		while(true)
		{
			System.out.println("while(true)");
			if(val == 0)
				Molokai.sleep(); // we are waiting for the boat on Molokai

			else if(val == 1)
			{
				// send children first, but leave one child on Oahu
				while(childrenOnA > 1 || !boatOnA || passengers > 0)
					Oahu.sleep();

				bg.AdultRowToMolokai();

				boatOnA = false;
				adultsOnA--;
				adultsOnB++;

				comm.speak(childrenOnB + adultsOnB);
				// Lib.assertTrue(childrenOnB > 0)

				Molokai.wakeAll(); // wake child in Molokai
				Molokai.sleep(); // sleep adult
			}

			else // this else statement won't ever execute
			{
				// Lib.assertTrue(false);
				break; // prevents compiler error
			}
			System.out.println("end of while(true)");
		}
		System.out.println("after while(true)");
		Boat.release(); // this adult is done!
	}
	/* -------------METHODOLOGY-------------
	All children go to Molokai in groups of 2 (if possible)

	When there is 1 child or no children left on Oahu,
	an adult rows from Oahu to Molokai, and 1 children rows back to Oahu.
	This happens twice, and there's 2 children on Oahu,
	then they both go to Molokai together

	this repeats until there are no adults on Oahu
	remaining children on Oahu go to Molokai
	FINISHED
	*/
	// we can only have at most 2 threads running at a time... (2 children or 1 adult...)
	static void ChildItinerary()
	{
		// DO NOT PUT ANYTHING ABOVE THIS LINE!
		// bg.initializeChild(); // Required for autograder interface. Must be the first thing called.
		// System.out.println("test1");

		Boat.acquire();

		// System.out.println("test2");

		while(true) // always true, easier than tricking compiler...
		{
			System.out.println("while(childrenOnA + childrenOnB > 0)");
			if(boatOnA)
			{
				// System.out.println("...BOAT ON A...");
				while((adultsOnA > 0 && childrenOnA == 1)|| !boatOnA || passengers == 2) // send adults when there's one child left on Oahu, need boat to be there, and an available space...
					Oahu.sleep();

				Oahu.wakeAll();

				if(childrenOnA >= 2) // if there are 2 or more children, they go to Molokai as a pair
				{
					passengers++;

					if(passengers == 2)
					{
						Full.wake(); // notify the pilot to row... (boat is full)
						Full.sleep(); // wait for pilot

						childrenOnA--; // now riding to Molokai!

						bg.ChildRideToMolokai();

						boatOnA = false; // we have arrived on Molokai!
						// passengers--; // rider off of boat...
						// childrenOnB++; // ...and onto Molokai
						passengers -= 2; // passengers have disembarked. Need to decrement here to avoid errors with conditionals...
						childrenOnB++;

						System.out.println("----- C on B: " + childrenOnB);

						comm.speak(childrenOnB + adultsOnB);

						Molokai.wakeAll(); // wake child in Molokai
						Molokai.sleep(); // sleep child
					}
					if(passengers == 1)
					{
						Full.sleep(); // wait for passenger

						childrenOnA--; // now rowing to Molokai!

						bg.ChildRowToMolokai();

						// boatOnA = false; // we have arrived on Molokai!
						// passengers--; // rider off of boat...
						childrenOnB++; // ...and onto Molokai

						System.out.println("----- C on B: " + childrenOnB);

						Full.wake(); // notify passenger to get in boat
						Molokai.sleep();
					}
				}

				if(childrenOnA == 1 && adultsOnA == 0 && boatOnA) // if there's only one child left on Oahu, send him to Molokai (and call C.P.S.)
				{
					// System.out.println("childrenOnA == 1 && adultsOnA == 0");
					childrenOnA--; // rowing to Molokai!

					bg.ChildRowToMolokai();

					boatOnA = false; // we have arrived on Molokai!
					passengers = 0; // rider off of boat, reset...
					childrenOnB++; // ...and onto Molokai

					comm.speak(childrenOnB + adultsOnB);

					Molokai.sleep(); // DONE!
				}
			}
			else if(childrenOnA == 0 && adultsOnA == 0)
			{
				System.out.println(KThread.currentThread());
				break;
			}
			else
			{
				System.out.println("...ELSE...");

				Lib.assertTrue(childrenOnB > 0); // check if there is a child there

				while(boatOnA) // check
					Molokai.sleep();

				childrenOnB--; // now rowing to Oahu!

				bg.ChildRowToOahu();

				boatOnA = true; // we have arrived on Oahu!
				childrenOnA++;

				Oahu.wakeAll(); // wake children on Oahu to come back to Molokai
				Oahu.sleep();
				System.out.println("end of else...");
			}
		} // end of while loop
		System.out.println("...End of Child Itinerary...");
		Boat.release();
	}

	static void SampleItinerary()
	{
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}
}
