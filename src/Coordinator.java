import com.oocourse.elevator3.TimableOutput;

public class Coordinator {
    private final int elevatorAId;
    private final int elevatorBId;
    // 用于楼层换乘协调
    private boolean isOccupied;
    // 用于更新协作：表示 A 和 B 电梯是否已进入 update() 方法
    private boolean readyA;
    private boolean readyB;
    private boolean readyWaitEnd; // 用于A和B电梯是否已收到 update() 方法通知
    private boolean printBegin; // 用于是否输出 UPDATE-BEGIN 消息
    private boolean updateWaitEnd; // 用于A和B电梯是否结束1s等待
    private boolean printEnd; // 用于是否输出 UPDATE-END 消息
    private long startTime;
    
    public Coordinator(int elevatorAId, int elevatorBId) {
        this.elevatorAId = elevatorAId;
        this.elevatorBId = elevatorBId;
        this.isOccupied = false;
        this.readyA = false;
        this.readyB = false;
        this.startTime = -1;
        this.readyWaitEnd = false;
        this.printBegin = false;
        this.updateWaitEnd = false;
        this.printEnd = false;
    }
    
    // ---------------- 楼层换乘协调功能 ----------------
    public synchronized void robTransferFloor() {
        while (isOccupied) {
            try {
                wait(); // 等待直至换乘层空闲
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        isOccupied = true;
    }
    
    public synchronized void releaseTransferFloor() {
        isOccupied = false;
        notifyAll();
    }
    
    // ---------------- 更新协作功能 ----------------
    public synchronized void updateMonitor(int elevatorId) {
        // 标记己方进入更新状态
        if (elevatorId == elevatorAId) {
            readyA = true;
        } else if (elevatorId == elevatorBId) {
            readyB = true;
        }
        // System.out.println("    Elevator " + elevatorId + " is ready to update.");
        // 若对方未到位，则等待
        while (!(readyA && readyB)) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (!readyWaitEnd) {
            // 若对方未通知，则通知对方
            readyWaitEnd = true;
            notifyAll();
        }
        // System.out.println("    Elevator " + elevatorId + " is starting to update.");
        // 至此，双方均已进入更新状态
        // 如果当前线程为 A 电梯，则立即输出 UPDATE-BEGIN 消息
        if (!printBegin) {
            printBegin = true;
            TimableOutput.println(String.format("UPDATE-BEGIN-%d-%d", elevatorAId, elevatorBId));
        }
        // 强制等待至少1s，确保双方均在等待中
        if (startTime < 0) {
            startTime = System.currentTimeMillis();
        }
        while (System.currentTimeMillis() - startTime < 1000) {
            try {
                wait(1000 - (System.currentTimeMillis() - startTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (!updateWaitEnd) {
            // 若对方未通知，则通知对方
            updateWaitEnd = true;
            notifyAll();
        }
        // 1s 等待结束后，如果当前线程为 A 电梯，则输出 UPDATE-END 消息
        if (!printEnd) {
            printEnd = true;
            TimableOutput.println(String.format("UPDATE-END-%d-%d", elevatorAId, elevatorBId));
        }
        // System.out.println("    Elevator " + elevatorId + " is finished updating.");
    }
    
    public synchronized boolean bothReady() {
        return readyA && readyB;
    }
    
    public synchronized void resetUpdate() {
        readyA = false;
        readyB = false;
    }
}
