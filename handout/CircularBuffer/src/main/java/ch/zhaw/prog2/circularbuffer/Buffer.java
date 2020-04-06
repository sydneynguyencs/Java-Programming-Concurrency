package ch.zhaw.prog2.circularbuffer;

public interface Buffer<T> {
    boolean put(T element) throws InterruptedException;
    T get() throws InterruptedException;
    boolean empty();
    boolean full();
    int count();
    void printBufferSlots();
    void printBufferContent();
}
