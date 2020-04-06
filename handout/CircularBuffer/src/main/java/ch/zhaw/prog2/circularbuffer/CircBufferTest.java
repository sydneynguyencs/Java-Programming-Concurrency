package ch.zhaw.prog2.circularbuffer;

public class CircBufferTest {
    public static void main(String[] args) {
        final int capacity = 15; // Number of buffer items
        final int prodCount = 1; // Number of producer threads
        final int consCount = 1; // Number of consumer threads
        final int maxProdTime = 500; // max. production time for one item
        final int maxConsTime = 500; // max. consumption time for one item

        try {
            Buffer<String> buffer = new CircularBuffer<>(
                    String.class, capacity);

            Consumer[] consumers = new Consumer[consCount];
            for (int i = 0; i < consCount; i++) {
                consumers[i] = new Consumer("Consumer_" + i, buffer,
                        maxConsTime);
                consumers[i].start();
            }
            Producer[] producers = new Producer[prodCount];
            for (int i = 0; i < prodCount; i++) {
                producers[i] = new Producer("Producer_" + i, buffer,
                        maxProdTime);
                producers[i].start();
            }

            while (true) {
                // buffer.printBufferSlots();
                buffer.printBufferContent();
                Thread.sleep(1000);
            }
        } catch (Exception logOrIgnore) {
            System.out.println(logOrIgnore.getMessage());
        }
    }

    private static class Producer extends Thread {
        // ToDo: Add required instance variables

        public Producer(String name, Buffer<String> buffer, int prodTime) {
            super(name);
            // ToDo implement Constructor
        }

        @Override
        public void run() {
            // ToDo: Continuously produce counting Strings in prodTime intervall
        }
    }

    private static class Consumer extends Thread {
        // ToDo: Add required instance variables

        public Consumer(String name, Buffer<String> buffer, int consTime) {
            super(name);
            // ToDo implement Constructor
        }

        @Override
        public void run() {
            // ToDo: Continuously consume Strings in prodTime intervall
        }
    }

}
