package ch.zhaw.prog2.trafficlight;

class TrafficLight {
    private boolean red;

    public TrafficLight() {
        red = true;
    }

    public synchronized void passby() {
        // wait as long the light is red
        while(red) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switchToGreen();
        }
    }

    public synchronized void switchToRed() {
        // set light to red
        red = true;
    }

    public synchronized void switchToGreen() {
        // set light to green
        red = false;
        // waiting cars can now pass by
        notifyAll();
        //notifyAll() otherwise only one car can pass. Here we want all waiting cars to pass.
    }
}
