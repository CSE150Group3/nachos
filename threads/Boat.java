package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;

	static Lock boatLock;
	static Condition waitingForBoat;
	static Communicator kimmunicator;
	static int boatLocation; // 0 for Oahu, 1 for Molokai
	static int adultsAtOahu;
	static int adultsAtMolokai;
	static int childrenAtOahu;
	static int childrenAtMolokai;
	static int totalAdults;
	static int totalChildren;
    
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
		boatLock = new Lock();
		waitingForBoat = new Condition(boatLock);
		kimmunicator = new Communicator();

		boatLocation = 0;
		adultsAtOahu = adults;
		adultsAtMolokai = 0;
		childrenAtOahu = children;
		childrenAtMolokai = 0;
		totalAdults = adults;
		totalChildren = children;


		
		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.
		/*
		Runnable r = new Runnable() {
			public void run() {
					SampleItinerary();
				}
		};
			KThread t = new KThread(r);
			t.setName("Sample Boat Thread");
			t.fork();
		*/

		Runnable runAdult = new Runnable() {
			public void run() {
					AdultItinerary();
				}
		};

		Runnable runChild = new Runnable() {
			public void run() {
					ChildItinerary();
				}
		};

		for(int i = 0; i < totalAdults; i++) {
			KThread t = new KThread(runAdult);
			t.setName("Adult " + i);
			t.fork();
		}

		for(int i = 0; i < totalChildren; i++) {
			KThread t = new KThread(runChild);
			t.setName("Child " + i);
			t.fork();	
		}
    }



    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
    }

    static void ChildItinerary()
    {
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
