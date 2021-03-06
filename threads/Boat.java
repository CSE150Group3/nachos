package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;

	static Lock boatLock;
	static Communicator kimmunicator;
	static int boatLocation; // 0 for Oahu, 1 for Molokai
	static int adultsAtOahu;
	static int adultsAtMolokai;
	static int childrenAtOahu;
	static int childrenAtMolokai;
	static int totalAdults;
	static int totalChildren;
	static int boatRiders;
	static Condition adultsWaitingOahu;
	static Condition adultsWaitingMolokai;
	static Condition childrenWaitingOahu;
	static Condition childrenWaitingMolokai;
    
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
		kimmunicator = new Communicator();

		boatLocation = 0;
		adultsAtOahu = adults;
		adultsAtMolokai = 0;
		childrenAtOahu = children;
		childrenAtMolokai = 0;
		totalAdults = adults;
		totalChildren = children;
		boatRiders = 0;

		adultsWaitingOahu = new Condition(boatLock);
		adultsWaitingMolokai = new Condition(boatLock);
		childrenWaitingOahu = new Condition(boatLock);
		childrenWaitingMolokai = new Condition(boatLock);
		


		
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

		//Fork off a thread for each adult
		for(int i = 0; i < totalAdults; i++) {
			KThread t = new KThread(runAdult);
			t.setName("Adult " + i);
			t.fork();
		}
		//Fork off a thread for each child
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

		//While no children have been sent adults wait
		while(childrenAtOahu == totalChildren) {
			adultsWaitingMolokai.sleep();
			adultsWaitingOahu.sleep();
		}
		//While there are still people on Oahu
		while(adultsAtOahu + childrenAtOahu > 0) {
			if(boatLocation == 0) { //Oahu
				//send 2 children. Don't send any adults.
				if(childrenAtOahu == totalChildren || childrenAtMolokai <= 0) {// If it's the first time or there are no children at Molokai to bring boat back
					//Edit: Ended up doing this in ChildItinerary() Instead
					//childrenWaitingOahu.wakeAll();
					adultsWaitingOahu.sleep();
				}
				else {
					// We can send adult as long as there is a child at molokai
					boatLock.acquire();
					boatRiders +=2;
					--adultsAtOahu;
					bg.AdultRowToMolokai();
					boatRiders -= 2;
					++adultsAtMolokai;
					boatLocation = 1;
					adultsWaitingMolokai.sleep();

					//Edit: Not sure if needed or covered in Child() already
					//Wake up single child at Molokai so we can send the boat back
					childrenWaitingMolokai.wake();
					
					boatLock.release();
				}
			}
			else {	// Molokai
				//Adults should never leave Molokai so nothing happens here.
				adultsWaitingMolokai.sleep();
			}
		}

    }

    static void ChildItinerary()
    {
		//While people still at Oahu
		while(adultsAtOahu + childrenAtOahu > 0) {
			if(boatLocation == 0) { //Oahu

				//Sleep children on other island
				childrenWaitingMolokai.sleep();

				//Sleep if there is only one child or if boat is full
				while((childrenAtOahu == 1 && adultsAtOahu > 0) || boatRiders >= 2) {
					childrenWaitingOahu.sleep();
				}

				childrenWaitingOahu.wakeAll();

				boatLock.acquire(); // Acquire lock at start

				//If it's first time we send two children
				if(childrenAtOahu == totalChildren) {
					//Send 2 children
					boatRiders += 2;
					bg.ChildRowToMolokai();
					bg.ChildRideToMolokai();
					childrenAtMolokai += 2;
					childrenAtOahu -= 2;
					boatLocation = 1;
					boatRiders -= 2;
					childrenWaitingMolokai.sleep();
					adultsWaitingMolokai.sleep(); //Not sure if this one is needed

					//Send one back
					boatRiders = 1;
					bg.ChildRowToOahu();
					++childrenAtOahu;
					--childrenAtMolokai;
					boatLocation = 0;
					

				}
				//If there is only one more child on Oahu we send and are finished
				else if(childrenAtOahu == 1 && adultsAtOahu == 0) {
					bg.ChildRowToMolokai();
					++childrenAtMolokai;
					--childrenAtOahu;
					boatLocation = 1;
					//What do we do if finished? Use speaker/speak right? Check PDF
				}
				//Otherwise loop through and send all children to molokai
				else {
					while(childrenAtOahu > 1) {
						boatRiders += 2;
						bg.ChildRowToMolokai();
						bg.ChildRideToMolokai();
						childrenAtMolokai += 2;
						childrenAtOahu -= 2;
						boatLocation = 1;
						//Send a kid back and repeat
						boatRiders = 1;
						bg.ChildRowToOahu();
						--childrenAtMolokai;
						++childrenAtOahu;
						boatLocation = 0;
					}
				}
				//Release lock when done with Oahu Block
				boatLock.release();

			}
			else { // Molokai
			
				//Sleep children on other island
				childrenWaitingOahu.sleep();

				//Wake a child for boat
				childrenWaitingMolokai.wake();
			//Otherwise we send a child back to Molokai
				boatLock.acquire();
				boatRiders = 1;
				--childrenAtMolokai;
				bg.ChildRowToOahu();
				++childrenAtOahu;
				boatLocation = 0;
				boatLock.release();


//				childrenWaitingOahu.wakeAll();
//				childrenWaitingOahu.sleep();

			}
		}
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
