public class Main {

    public static void main(String[] args) {
        Thread t;

        // Threads are created to represent the actors in the model.
        // They are set as deamon threads so that they will exit together with the main thread.
        t = new Thread(new CakeBaker(), "B1");
        t.setDaemon(true); t.start();
        t = new Thread(new CakeBaker(), "B2");
        t.setDaemon(true); t.start();
        t = new Thread(new Supplier(), "S1");
        t.setDaemon(true); t.start();
        t = new Thread(new CakeMonster(), "M1");
        t.setDaemon(true); t.start();
        t = new Thread(new CakeMonster(), "M2");
        t.setDaemon(true); t.start();
        t = new Thread(new CakeMonster(), "M3");
        t.setDaemon(true); t.start();
        t = new Thread(new CakeMonster(), "M4");
        t.setDaemon(true); t.start();
        t = new Thread(new CakeMonster(), "M5");
        t.setDaemon(true); t.start();

        // The main thread will sleep for a while before exiting.
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private static class CakeBaker implements Runnable {
        public void run() {
            try {
                int n = 7;      // number of cakes each CakeBaker bakes.
                while (n-- > 0) {
                    CakeStand.putCake((int)(3+Math.random()*5));
                    Thread.sleep((int)(Math.random()*100));
                }
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class CakeMonster implements Runnable {
        public void run() {
            try {
                int n = 10;     // number of slices each CakeMonster wants to eat.
                while (n-- > 0) {
                    CakeStand.randomStand().getSlice();
                    Thread.sleep((int)(Math.random()*20));
                }
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class Supplier implements Runnable {
        public void run() {
            try {
                int n = 3;     // number of stands each Supplier brings.
                while (n-- > 0) {
                    CakeStand.supplyStand();
                    Thread.sleep((int)(Math.random()*10000));
                }
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}