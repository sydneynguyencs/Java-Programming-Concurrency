package ch.zhaw.prog2.printer;

public class Printer {

    // test program
    public static void main(String[] arg) {
        PrinterThread a = new PrinterThread("PrinterA", '.', 10);
        PrinterThread b = new PrinterThread("PrinterB", '*', 20);
        a.start();
        b.start();
        b.run(); // wie kann das abgefangen werden?
    }


    private static class PrinterThread extends Thread {
        char symbol;
        int sleepTime;

        public PrinterThread(String name, char symbol, int sleepTime) {
            super(name);
            this.symbol = symbol;
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            System.out.println(getName() + " run started...");
            for (int i = 1; i < 100; i++) {
                System.out.print(symbol);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
            System.out.println('\n' + getName() + " run ended.");
        }
    }
}
