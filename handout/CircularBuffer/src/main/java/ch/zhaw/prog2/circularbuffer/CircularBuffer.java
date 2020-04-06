package ch.zhaw.prog2.circularbuffer;

import java.lang.reflect.Array;

public class CircularBuffer<T> implements Buffer<T> {
    private static final char EMPTY = '-';
    private static final char FILLED = '*';
    private T[] items;
    private int count = 0;
    private int insertPosition = 0;
    private int outputPosition = 0;
    private StringBuffer printout;

    @SuppressWarnings("unchecked")
    public CircularBuffer(Class<T> clazz, int bufferSize) {
        printout = new StringBuffer();
        if (bufferSize <= 1)
            bufferSize = 1;
        this.items = (T[]) Array.newInstance(clazz, bufferSize);
    }

    public boolean put(T item) {
        if (this.full())
            return false;
        items[insertPosition] = item;
        insertPosition = (insertPosition + 1) % items.length;
        count++;
        return true;
    }

    public T get() {
        if (empty())
            return null;
        T item = items[outputPosition];
        outputPosition = (outputPosition + 1) % items.length;
        count--;
        return item;
    }

    public boolean empty() {
        return count == 0;
    }

    public boolean full() {
        return count >= items.length;
    }

    public int count() {
        return count;
    }

    public void printBufferSlots() {
        printout.delete(0, printout.length());

        int i = 0;

        // from where to where is the buffer filled?
        printout.append("filled where: ||");

        if (full()) {
            for (i = 0; i < items.length; i++)
                printout.append(FILLED);
        } else {
            char c1;
            char c2;
            if (insertPosition < outputPosition) { // if iPos < oPos, the buffer
                                                   // starts with
                // filled slots at pos. 0; otherwise it
                // starts with empty slots
                c1 = FILLED; // * -> filled slot
                c2 = EMPTY; // - -> empty slot
            } else {
                c1 = EMPTY;
                c2 = FILLED;
            }
            for (i = 0; i < outputPosition && i < insertPosition; i++)
                printout.append(c1);
            for (; i < outputPosition || i < insertPosition; i++)
                printout.append(c2);
            for (; i < items.length; i++)
                printout.append(c1);
        }
        printout.append("||  how full: || ");

        // how full is the buffer generally?
        for (i = 0; i < count; i++)
            printout.append(FILLED);
        for (; i < items.length; i++)
            printout.append(EMPTY);
        printout.append("||");
        System.out.println(printout);
    }

    public void printBufferContent() {
        System.out.println("Anzahl El im Puffer " + count);
        for (int i = 0; i < count; i++) {
            int index = (outputPosition + i) % items.length;
            System.out.println("index: " + index + " wert: " + items[index]);
        }
    }

}
