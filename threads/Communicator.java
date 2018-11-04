package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	this.communicatorLock = new Lock();
    	this.speakerReady = new Condition(communicatorLock);
    	this.listenerReady = new Condition(communicatorLock);
    	this.wordReady = new Condition(communicatorLock);
    	this.speakers = 0;
    	this.listeners = 0;
    	this.waitingForSpeaker = 0;
    	this.waitingForListener = 0;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	this.communicatorLock.acquire();
    	++this.speakers;
    	while (this.listeners == 0) {	//if there are no listeners, the speaker waits until a listener is present
    		this.speakerReady.sleep();
    	}
    	this.word = word;				//save speaker's word into the communicator
    										//two cases possible
    	if (this.waitingForSpeaker == 0) {	//case 1: Wake up next sleeping listener and signal that listen() will listen to the correct word.
    		this.listenerReady.wake();
    		this.waitingForListener = 1;
    	}
    	else {								//case 2: Wake up the listener that is waiting for speak() to save its word into the communicator.
    		this.wordReady.wake();
    		this.waitingForSpeaker = 0;
    	}
    	--this.speakers;
    	this.communicatorLock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	this.communicatorLock.acquire();
    	++this.listeners;
    	while (this.speakers == 0){			//if there are no speakers, the listener waits until a speaker is present
    		this.listenerReady.sleep();
    	}
    										//two cases possible
    	if (this.waitingForListener == 1){	//case 1: speaker's word is already in the communicator, so proceed to return the word in the communicator
    		this.waitingForListener = 0;
    	}
    	else {								//case 2: speaker's word is not present, so signal to the next speaker to input its word into the communicator
    		this.waitingForSpeaker = 1;
    		this.speakerReady.wake();
    		this.wordReady.sleep();
    	}
    	int word = this.word;				//save the communicator's word into a variable inside the method as the value of communicator's word may change
    	--this.listeners;
    	this.communicatorLock.release();
    	return word;
    }
    private Lock communicatorLock;
    private Condition speakerReady, listenerReady, wordReady;	//side note: wordReady holds at most only one listener 
    															//when that listener is waiting for the speaker to enter its word into the communicator
    private int word, speakers, listeners, waitingForSpeaker, waitingForListener;
    //speakers and listeners are counters
    //waitingForSpeaker and waitingForListener are to signal to the speakers and listeners the different cases
}
