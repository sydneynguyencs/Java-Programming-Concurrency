package ch.zhaw.prog2.circularbuffer;

/*
 * prodCount vs consCount - maxSleepTime stays the same for cons and prods:
 * if we have more producer threads, the buffer will be more likely full or 14
 * if we have more consumer threads, the buffer will be more likely empty or 1.
 *
 * maxProdTime vs maxConsTime - prod and cons thread count stays the same:
 * if we set maxProdTime > maxConsTime, the buffer will be more likely full or 14
 * if we set maxConsTime > maxProdTime, the buffer will be more likely empty or 1.
 *
 * Sprünge im Wert bevor Threadesafetyy implementiert wurde
 */
public class CircBufferTest {
    public static void main(String[] args) {
        final int capacity = 15; // Number of buffer items
        final int prodCount = 1; // Number of producer threads
        final int consCount = 1; // Number of consumer threads
        final int maxProdTime = 100; // max. production time for one item
        final int maxConsTime = 500; // max. consumption time for one item

        try {
            Buffer<String> buffer = new GuardedCircularBuffer<>(
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
        // Add required instance variables
        Buffer<String> buffer;
        int prodTime;
        int count;

        public Producer(String name, Buffer<String> buffer, int prodTime) {
            super(name);
            // implement Constructor
            this.buffer = buffer;
            this.prodTime = prodTime;
            count = 0;
        }

        @Override
        public void run() {
            // Continuously produce counting Strings in prodTime intervall
            while (true) {
                try {
                    // produce strings
                    buffer.put("" + count++);
                    Thread.sleep((int)(Math.random()*prodTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Consumer extends Thread {
        // Add required instance variables
        Buffer<String> buffer;
        int consTime;

        public Consumer(String name, Buffer<String> buffer, int consTime) {
            super(name);
            // implement Constructor
            this.buffer = buffer;
            this.consTime = consTime;
        }

        @Override
        public void run() {
            // Continuously consume Strings in prodTime intervall
            while (true) {
                try {
                    buffer.get();
                    Thread.sleep((int)(Math.random() * consTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
