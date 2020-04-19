package ch.zhaw.prog2.circularbuffer;

/*
 * notifyAll() is used such that all waiting threads will be notified and not only one.
 * notify() will only wake one thread (from waiting into entry room) which could be
 * problematic in case this thread still has to wait at the same time.
 */
public class GuardedCircularBuffer<T> implements Buffer<T> {
    private int size;
    private CircularBuffer<T> buffer;

    public GuardedCircularBuffer(Class<T> clazz, int size) {
        this.size = size;
        this.buffer = new CircularBuffer<T>(clazz, size);
    }

    @Override
    public synchronized boolean put(T element) throws InterruptedException {
        // Aufrufe von put blockieren, solange der Puffer voll ist, d.h.,
        // bis also mindestens wieder ein leeres Puffer-Element vorhanden ist.
        while(full()) {
            wait();
        }
        notifyAll(); //see top
        return buffer.put(element);
    }

    @Override
    public synchronized T get() throws InterruptedException {
        // Aufrufe von get blockieren, solange der Puffer leer ist, d.h,
        // bis also mindestens ein Element im Puffer vorhanden ist
        while(empty()) {
            wait();
        }
        notifyAll(); //see top
        return buffer.get();
    }

    @Override
    public synchronized boolean empty() {
        return buffer.empty();
    }

    @Override
    public synchronized boolean full() {
        return buffer.full();
    }

    @Override
    public synchronized int count() {
        return buffer.count();
    }

    @Override
    public synchronized void printBufferSlots() {
        buffer.printBufferSlots();
    }

    @Override
    public synchronized void printBufferContent() {
        buffer.printBufferContent();
    }
}
