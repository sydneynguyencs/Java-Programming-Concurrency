package ch.zhaw.prog2.bridge;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Controls the traffic passing the bridge
 */
public class TrafficController {
    private ReentrantLock mutex = new ReentrantLock();
    private Condition goLeft = mutex.newCondition();
    private Condition goRight = mutex.newCondition();
    private boolean bridgeOccupied = false;


    /* Called when a car wants to enter the bridge form the left side */
    public void enterLeft() {
        mutex.lock();
        try {
            while (bridgeOccupied) {
                goRight.await();
            }
            bridgeOccupied = true;
            //goLeft.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }

    /* Called when a wants to enter the bridge form the right side */
    public void enterRight() {
        mutex.lock();
        try {
            while (bridgeOccupied) {
                goLeft.await();
            }
            bridgeOccupied = true;
            //goRight.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }

    }

    /* Called when the car leaves the bridge on the left side */
    public void leaveLeft() {
        mutex.lock();
        try {
            bridgeOccupied = false;
            if (mutex.hasWaiters(goLeft)) {
                goLeft.signal();

            } else {
                goRight.signal();
            }
        } finally {
            mutex.unlock();
        }
    }

    /* Called when the car leaves the bridge on the right side */
    public void leaveRight() {
        mutex.lock();
        try {
            bridgeOccupied = false;
            if (mutex.hasWaiters(goRight)) {
                goRight.signal();

            } else {
                goLeft.signal();
            }
        } finally {
            mutex.unlock();
        }
    }
}
