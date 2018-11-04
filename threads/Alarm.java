package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will n
    ot function correctly with more than one
     * alarm.
     */

    //private static ThreadQueue sleepingThreads = null;
    public PriorityQueue<WaitThread> waitQueue = new PriorityQueue<WaitThread>();

    public Alarm() {
    	Machine.timer().setInterruptHandler(new Runnable() {
    		public void run() { timerInterrupt(); }
    	});
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    	//Disable interrupts everywhere?
    	//check if it's time for next in queue to wake up 
    	Machine.interrupt().disable();
        //Check if queue is empty and if it is time for next in queue to wake
    	while(!waitQueue.isEmpty() && waitQueue.peek().time <= Machine.timer().getTime()) {
    		//Remove thread from queue and ready it
    		WaitThread wThread = waitQueue.remove();
    		wThread.thread.ready();
    	}
    	Machine.interrupt().enable();
    }


    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
    	
    	Machine.interrupt().disable();

        //Get time to wakeup, create WaitThread which links thread with a time, add to the waitQueue
        long wakeTime = Machine.timer().getTime() + x;
    	WaitThread currentWaitThread = new WaitThread(KThread.currentThread(), wakeTime);
    	waitQueue.add(currentWaitThread);
		KThread.sleep();

        Machine.interrupt().enable();
    }

    class WaitThread implements Comparable<WaitThread>{
    	private KThread thread;
    	private long time;

		WaitThread(KThread thr, long t) {
			this.thread = thr;
			this.time = t;
		}

        //Compare for priority Queue
        @Override
		public int compareTo(WaitThread wthread) {

            if(this.time != wthread.time) {
                if(this.time > wthread.time)
                    return 1;
                else
                    return -1;
            }
			else
				return 0;
		}
    }
}
