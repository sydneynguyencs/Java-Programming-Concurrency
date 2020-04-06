package ch.zhaw.prog2.philosopher;

import java.util.Observable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class PhilosopherTable extends Observable {
    private final int philoCount;
    private final Philosopher[] philosophers;
    private final ForkManager forkManager;

    public PhilosopherTable(int philoCount) {
        System.out.println("creating table ...");
        this.philoCount = philoCount;
        philosophers = new Philosopher[philoCount];
        forkManager = new ForkManager(philoCount);

        for (int i = philoCount - 1; i >= 0; i--) {
            philosophers[i] = new Philosopher(this, i);
        }
    }

    public ForkManager getForkManager() {
        return forkManager;
    }

    public void notifyStateChange(Philosopher sender) {
        setChanged();
        notifyObservers(sender);
    }

    public void start() {
        notifyStateChange(null);
        for (int i = philoCount - 1; i >= 0; i--) {
            philosophers[i].start();
            philosophers[i].setPriority(Thread.MIN_PRIORITY);
        }
    }

    public Philosopher getPhilo(int i) {
        return philosophers[i];
    }

    public int left(int i) {
        return (philoCount + i - 1) % philoCount;
    }

    public int right(int i) {
        return (i + 1) % philoCount;
    }

}


// ForkManager manages the resources (=forks), used by the philosophers
class ForkManager {

    enum ForkState {
        FREE, OCCUPIED
    }

    static class Fork {
        public final Condition cond;

        public ForkState forkState;
        public Fork(Lock m) {
            cond = m.newCondition();
            forkState = ForkState.FREE;
        }

    }

    private int nrForks;
    private Fork[] forks;

    private Lock mutex;

    public ForkManager(int nrForks) {
        this.mutex = new ReentrantLock();
        this.nrForks = nrForks;
        this.forks = new Fork[nrForks];
        for (int i = 0; i < nrForks; i++)
            forks[i] = new Fork(mutex);
    }

    public void acquireFork(int i) {
        try {
            mutex.lock();
            while (forks[i].forkState == ForkState.OCCUPIED)
                forks[i].cond.await();
            forks[i].forkState = ForkState.OCCUPIED;
        } catch (InterruptedException e) {
            System.err.println("Interrupted: " + e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public void releaseFork(int i) {
        try {
            mutex.lock();
            forks[i].forkState = ForkState.FREE;
            forks[i].cond.signal();
        } finally {
            mutex.unlock();
        }
    }

    public int left(int i) {
        return (nrForks + i - 1) % nrForks;
    }
    public int right(int i) {
        return (i + 1) % nrForks;
    }

}


class Philosopher extends Thread {
    private final static int THINK_TIME_FACTOR = 1;
    private final static int EAT_TIME_FACTOR = 1;
    private final int id;
    private final PhilosopherTable table;
    private PhiloState philoState = PhiloState.THINKING;

    enum PhiloState {
        THINKING, HUNGRY, EATING;
    }

    public Philosopher(PhilosopherTable table, int id) {
        this.id = id;
        this.table = table;
    }

    public PhiloState getPhiloState() {
        return philoState;
    }

    public long getId() {
        return id;
    }

    public int getIdOfLeftNeighbour() {
        return table.left(id);
    }

    public int getIdOfRightNeighbour() {
        return table.right(id);
    }

    private void think() {
        try {
            philoState = PhiloState.THINKING;
            table.notifyStateChange(this);
            int time = 1;
            sleep((int) (Math.random() * THINK_TIME_FACTOR * 500));
        } catch (InterruptedException e) {
            System.err.println("Interrupted: " + e.getMessage());
        }
    }

    private void eat() {
        try {
            philoState = PhiloState.EATING;
            table.notifyStateChange(this);
            int time = 1;
            sleep((int) (Math.random() * EAT_TIME_FACTOR * 500));
        } catch (InterruptedException e) {
            System.err.println("Interrupted: " + e.getMessage());
        }
    }

    private void takeForks() {
        philoState = PhiloState.HUNGRY;
        table.notifyStateChange(this);

        ForkManager mgr = table.getForkManager();
        mgr.acquireFork(id);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.err.println("Interrupted: " + e.getMessage());
        }
        mgr.acquireFork(mgr.right(id));
    }

    private void putForks() {
        ForkManager mgr = table.getForkManager();
        mgr.releaseFork(id);
        mgr.releaseFork(mgr.right(id));

    }

    @Override
    public void run() {
        while (true) {
            think();
            takeForks();
            eat();
            putForks();
        }
    }

}
