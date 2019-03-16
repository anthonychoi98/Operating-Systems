package nachos.threads;
import nachos.ag.BoatGrader;
import nachos.machine.Machine;

public class Boat
{
    static BoatGrader bg;

    public static Lock mainLock;
    public static Alarm sync;

    public static boolean boatLocation;
    public static int adultsAtOahu;
    public static int childrenAtOahu;
    public static int adultsAllocator;
    public static int childAllocator;
    public static int adultCounter;
    public static int childCounter;
    public static int boatCounter;

    public static boolean finished;
    public static Condition sleeper;

    public static void selfTest()
    {
        BoatGrader b = new BoatGrader();

        System.out.println("\n ***Testing Boats with only 2 children***");
        begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;

        // Instantiate global variables here

        mainLock = new Lock();
        sync = new Alarm();
        childCounter = children;
        adultCounter = adults;
        childrenAtOahu = children;
        adultsAtOahu = adults;
        finished = false;
        sleeper = new Condition(mainLock);

        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.

//	Runnable r = new Runnable() {
//	    public void run() {
//                SampleItinerary();
//            }
//        };
//        KThread t = new KThread(r);
//        t.setName("Sample Boat Thread");
//        t.fork();

        boolean state = Machine.interrupt().disable();

        Runnable childThread = new Runnable() {
            public void run() {
                ChildItinerary();
            }
        };

        Runnable adultThread = new Runnable() {
            public void run() {
                AdultItinerary();
            }
        };

        for (int i = 0; i < children; i++) {
            KThread kChild = new KThread(childThread);
            kChild.fork();
        }

        for (int i = 0; i < adults; i++) {
            KThread kAdult = new KThread(adultThread);
            kAdult.fork();
        }

        Machine.interrupt().setStatus(state); //Restoring the state and enabling interrupt
        mainLock.acquire();
        while (!finished) {
            sleeper.wakeAll();
            sleeper.sleep();
        }
        mainLock.release();


    }

    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/

        //acquire the lock > wake > and give priority to children
        mainLock.acquire();
        sleeper.wakeAll();
        sleeper.sleep();

        boolean adultsOnMolokai = false; //they're not there yet
        while (true) {
            if (!boatLocation && !adultsOnMolokai && childrenAtOahu < 2) { //if the boat is not at molokai, adults are not at molokai and children at oahua < 2
                boatCounter += 2;
                bg.AdultRowToMolokai();
                adultsAtOahu--;
                adultsOnMolokai = true;
                boatLocation = true; //boat is at Molokai
                boatCounter -= 2;
                sleeper.wakeAll();
                break;
            } else {
                sleeper.wakeAll();
                sleeper.sleep();
            }
        }
        mainLock.release();
    }

    static void ChildItinerary()
    {
        mainLock.acquire();
        boolean childOnMolokai = false;

        while(true) {
            if (!boatLocation && !childOnMolokai && boatCounter < 2 && (childrenAtOahu > 1 || boatCounter == 1)) { //
                boatCounter++;
                childrenAtOahu--;

                if (boatCounter == 1) { // pilot gets on
                    bg.ChildRowToMolokai();
                    sleeper.wakeAll();
                    sleeper.sleep();

                } else { // passenger gets on
                    bg.ChildRideToMolokai();
                }
                boatLocation = true;
                childOnMolokai = true;
                boatCounter--;
                sleeper.wakeAll();
                sleeper.sleep();

            } else if (!boatLocation && !childOnMolokai && boatCounter < 2 && childrenAtOahu == 1 && adultCounter == 0) {
                boatCounter++;
                childrenAtOahu--;
                childOnMolokai = true;
                bg.ChildRowToMolokai();
                break;

            } else if (!boatLocation && !childOnMolokai && boatCounter < 2 && childrenAtOahu == 1 && adultCounter > 0) { // Signal adult threads
                sleeper.wakeAll();
                sleeper.sleep();

            } else if (boatLocation && childOnMolokai && adultsAtOahu + childrenAtOahu != 0){ // Boat at Oahu and child at Oahu
                boatCounter++;
                childrenAtOahu++;
                childOnMolokai = false;
                boatLocation = false;
                bg.ChildRowToOahu();
                boatCounter--;
                sleeper.wakeAll();
            } else if (adultsAtOahu + childrenAtOahu == 0) {
                sleeper.wakeAll();
                finished = true;
                break;
            } else {
                sleeper.wakeAll();
                sleeper.sleep();
            }
        }
        mainLock.release();

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