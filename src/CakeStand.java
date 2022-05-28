import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CakeStand {
    static Lock lock = new ReentrantLock();
    Lock monsterLock = new ReentrantLock();
    static Condition supplyCondition = lock.newCondition();
    static Condition anyStandCondition = lock.newCondition();
    static Condition putCakeCondition = lock.newCondition();
    Condition standCondition = monsterLock.newCondition();
    static Condition bakerCondition = lock.newCondition();
    static List<CakeStand> synchronizedList = Collections.synchronizedList(new ArrayList<>());
    static List<CakeStand> availableStandList = Collections.synchronizedList(new ArrayList<>());
    static int supplierNumber =0;
    static int bakerNumber =0;
    static int standNumber =0;
    static int availableStandNumber =0;
    public String standName;
    public Boolean isAvailable = false;
    public int leftCakeSlice = 0;

    public CakeStand(String standName) {
        this.standName = standName;
    }

    public static void supplyStand(){
        lock.lock(); // Acquire the lock
        System.out.println(Thread.currentThread().getName() + " brought a new stand.");
        try {
            while (supplierNumber > 0) {
                supplyCondition.await();
            }
            supplierNumber++;
            CakeStand stand = new CakeStand("Stand#" + ++standNumber);
            if (standNumber==1){
                anyStandCondition.signalAll();
            }
            availableStandNumber++;
            stand.isAvailable = true;
            synchronizedList.add(stand);
            availableStandList.add(stand);
            System.out.println(Thread.currentThread().getName() + " added " + stand.standName + ".");
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        finally {
            supplierNumber--;
            supplyCondition.signal();
            putCakeCondition.signal();
            lock.unlock(); // Release the lock
        }
    }
    public static void putCake(int slices){
        System.out.println(Thread.currentThread().getName() + " baked a cake with "+slices+" slices");
        lock.lock(); // Acquire the lock
        try {
            while(bakerNumber > 0){
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
            System.out.println(Thread.currentThread().getName() + " put the cake with "+slices+" slices on "+selectedStand.standName+".");
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        finally {
            bakerNumber--;
            bakerCondition.signal();
            lock.unlock(); // Release the lock
        }
    }
    public static CakeStand randomStand(){
        // TODO: Check if synchronizedList is empty.
        lock.lock();
        try {
            while(standNumber == 0){
                anyStandCondition.await();
            }
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        finally {
            lock.unlock(); // Release the lock
        }
        return synchronizedList.get(ThreadLocalRandom.current().nextInt(0, synchronizedList.size()));
    }
    public void getSlice(){
        System.out.println(Thread.currentThread().getName() + " came to "+this.standName+" for a slice.");
        this.monsterLock.lock(); // Acquire the lock
        try {
            while(leftCakeSlice < 1){
                this.standCondition.await();
            }
            System.out.println(Thread.currentThread().getName() + " got a slice from "+this.standName+", so "+--leftCakeSlice+" slices left.");
            if (leftCakeSlice == 0){
                lock.lock();
                availableStandList.add(this);
                availableStandNumber++;
                putCakeCondition.signal();
                lock.unlock();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            this.monsterLock.unlock(); // Release the lock
        }
    }
}
