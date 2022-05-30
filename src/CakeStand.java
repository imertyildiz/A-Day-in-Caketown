import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CakeStand {
    // Static lock for locking entrance of static members into Cake Ground. Only 1 member (Baker or Supplier) can enter.
    static Lock lock = new ReentrantLock();
    // Lock for Monsters in the Stand.
    Lock monsterLock = new ReentrantLock();
    // Static condition for waiting at least one available stand
    static Condition putCakeCondition = lock.newCondition();
    // Static condition for 1 supplier in the Cake Ground
    static Condition supplyCondition = lock.newCondition();
    // Static condition for waiting the first stand to put in Cake Ground
    static Condition anyStandCondition = lock.newCondition();
    // Condition of a Stand for waiting Cake Slices if there is no Cake Slice in the Stand
    Condition standCondition = monsterLock.newCondition();
    // Static condition for 1 Baker in the Cake Ground
    static Condition bakerCondition = lock.newCondition();
    // Synchronized List of All Stands.
    static List<CakeStand> synchronizedList = Collections.synchronizedList(new ArrayList<>());
    // Synchronized List of All AVAILABLE Stands.
    static List<CakeStand> availableStandList = Collections.synchronizedList(new ArrayList<>());
    // Condition variable of supplier Number
    static int supplierNumber = 0;
    // Condition variable of Baker Number
    static int bakerNumber = 0;
    // Condition variable of Stand Number, and used also in ids
    static int standNumber = 0;
    // Condition variable of Available supplier Number
    static int availableStandNumber = 0;
    // Stand Name of a Stand.
    public String standName;
    // Left Cake Slice of a Stand
    public int leftCakeSlice = 0;

    public CakeStand(String standName) {
        this.standName = standName;
    }

    /*
     * In This function I first print the bringing the stand into Cake Town.
     * Then, I lock the static lock of this class to handle static variable issues.
     * If this is the first supply, I signal whole waiting random Stand functions.
     * I add this stand to whole and available list.
     * I print the added sentence.
     * I signal the one waiting cake baker and supplier if any.
     * Finally, unlock.
     */
    public static void supplyStand() {
        System.out.println(Thread.currentThread().getName() + " brought a new stand.");
        lock.lock(); // Acquire the lock
        try {
            while (supplierNumber > 0) {
                supplyCondition.await();
            }
            supplierNumber++;
            CakeStand stand = new CakeStand("Stand#" + ++standNumber);
            if (standNumber == 1) {
                anyStandCondition.signalAll();
            }
            availableStandNumber++;
            synchronizedList.add(stand);
            availableStandList.add(stand);
            System.out.println(Thread.currentThread().getName() + " added " + stand.standName + ".");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            supplierNumber--;
            supplyCondition.signal();
            putCakeCondition.signal();
            lock.unlock(); // Release the lock
        }
    }

    /*
     * In This function I first print the baking the cake with slices.
     * Then, I lock the static lock of this class to handle static variable issues.
     * Waiting the other bakers (this is because that other bakers can enter the lock if a baker waits an available stand )
     * Then, waiting an available stand.
     * Select a random stand and put the cake and signaling all monsters if any.
     * Finally, printing, signaling waiting bakers and unlock the static lock.
     */
    public static void putCake(int slices) {
        System.out.println(Thread.currentThread().getName() + " baked a cake with " + slices + " slices.");
        lock.lock(); // Acquire the lock
        try {
            while (bakerNumber > 0) {
                bakerCondition.await();
            }
            bakerNumber++;
            while (availableStandNumber < 1) {
                putCakeCondition.await();
            }
            CakeStand selectedStand = availableStandList.get(ThreadLocalRandom.current().nextInt(0, availableStandList.size()));
            selectedStand.leftCakeSlice = slices;
            availableStandList.remove(selectedStand);
            availableStandNumber--;
            selectedStand.monsterLock.lock();
            selectedStand.standCondition.signalAll();
            selectedStand.monsterLock.unlock();
            System.out.println(Thread.currentThread().getName() + " put the cake with " + slices + " slices on " + selectedStand.standName + ".");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            bakerNumber--;
            bakerCondition.signal();
            lock.unlock(); // Release the lock
        }
    }


    /*
     * This function returns a random Stand. If there is no stand in the town, it waits until a stand come.
     * */
    public static CakeStand randomStand() {
        lock.lock();
        try {
            while (standNumber == 0) {
                anyStandCondition.await();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock(); // Release the lock
        }
        return synchronizedList.get(ThreadLocalRandom.current().nextInt(0, synchronizedList.size()));
    }

    /*
     * In This function I first print the coming monster sentence.
     * Then, I lock the lock of this stand to handle local variable issues.
     * Then, it waits a cake with slices.
     * Then, I print taking slices and decrement the leftcakeslices variable.
     * After, if slices is empty, then I add this stand into available stand. Of course with using global static lock.
     * Finally,unlock the lock of stand.
     */
    public void getSlice() {
        System.out.println(Thread.currentThread().getName() + " came to " + this.standName + " for a slice.");
        this.monsterLock.lock(); // Acquire the lock
        try {
            while (leftCakeSlice < 1) {
                this.standCondition.await();
            }
            System.out.println(Thread.currentThread().getName() + " got a slice from " + this.standName + ", so " + --leftCakeSlice + " slices left.");
            if (leftCakeSlice == 0) {
                lock.lock();
                availableStandList.add(this);
                availableStandNumber++;
                putCakeCondition.signal();
                lock.unlock();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            this.monsterLock.unlock(); // Release the lock
        }
    }
}
