package ch.zhaw.prog2.printer;

public class PrinterB {

    // test program
    public static void main(String[] arg) {
        PrinterRunnable a = new PrinterRunnable("PrinterA", '.', 10);
        PrinterRunnable b = new PrinterRunnable("PrinterB", '*', 20);
        new Thread(a).start();
        new Thread(b).start();
    }


    public static class PrinterRunnable implements Runnable {
        String name;
        char symbol;
        int sleepTime;

        public PrinterRunnable(String name, char symbol, int sleepTime) {
            this.name = name;
            this.symbol = symbol;
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            System.out.println(name + " run started...");
            for (int i = 1; i < 100; i++) {
                System.out.print(symbol);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
            System.out.println('\n' + name + " run ended.");
        }
    }
}
