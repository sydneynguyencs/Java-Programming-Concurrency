package ch.zhaw.prog2.trafficlight;

class Car extends Thread {
    private TrafficLight[] trafficLights;
    private int pos;

    public Car(String name, TrafficLight[] trafficLights) {
        super(name);
        this.trafficLights = trafficLights;
        pos = 0; // start at first light
        start();
    }

    public synchronized int position() {
        return pos;
    }

    private void gotoNextLight() {
        // Helper method to move car to next light
        if(pos < trafficLights.length - 1) {
            pos++;
        } else {
            pos = 0;
        }
    }
    @Override
    public void run() {
        while (true) {
            // drive endlessly through all lights
            trafficLights[pos].passby();
            //  Simulation der Zeitspanne für das Passieren der des Signals
            try {
                Thread.sleep((int)(Math.random() * 500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gotoNextLight();
        }
    }
}
