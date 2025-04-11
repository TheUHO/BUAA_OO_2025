import com.oocourse.elevator3.ScheRequest;

public class ShadowElevator {
    private int elevatorId;
    private int currentFloor;
    private int direction;
    private int personsIn;
    private ScheRequest scheduleReq;
    private int assignedCount = 0;
    private static final int MAX_CAPACITY = 6;
    private boolean isA = false; // 是否是电梯A
    private boolean isB = false; // 是否是电梯B
    private int transferFloor = 100;
    
    public ShadowElevator(int elevatorId, int currentFloor,
        int direction, int personsIn, ScheRequest scheduleReq) {
        this.elevatorId = elevatorId;
        this.currentFloor = currentFloor;
        this.direction = direction;
        this.personsIn = personsIn;
        this.scheduleReq = scheduleReq;
        this.assignedCount = 0;
        this.isA = false;
        this.isB = false;
        this.transferFloor = 100;
    }
    
    public synchronized void update(int currentFloor, int direction, int personsIn, 
        ScheRequest scheduleReq, boolean isA, boolean isB, int transferFloor) {
        this.currentFloor = currentFloor;
        this.direction = direction;
        this.personsIn = personsIn;
        this.scheduleReq = scheduleReq;
        this.isA = isA;
        this.isB = isB;
        this.transferFloor = transferFloor;
    }

    public synchronized int getEstimatePerformance(Person person) {
        int pf = person.getFromInt(); // 乘客起始楼层
        int pd = person.getToInt();   // 乘客目的楼层
        double reversalDelay = 0.0;
        double transferDelay = 0.0;
        final double loadPenalty = personsIn * 0.2; // 电梯载客数可能带来的延迟（每人0.2秒）
        double speed = 0.4; // 电梯速度（秒/层）
        // 判断电梯类型并计算反转延迟
        if (isA) {
            speed = 0.2;
            if (pf > transferFloor || (pf == transferFloor && pd > transferFloor)) { // 乘客在A电梯的服务范围内
                transferDelay = (pd < transferFloor) ? 0.4 : 0.0;
            } else {
                return Integer.MAX_VALUE; // 乘客不在A电梯的服务范围内
            }
        } else if (isB) {
            speed = 0.2;
            if (pf < transferFloor || (pf == transferFloor && pd < transferFloor)) { // 乘客在B电梯的服务范围内
                transferDelay = (pd > transferFloor) ? 0.4 : 0.0;
            } else {
                return Integer.MAX_VALUE; // 乘客不在B电梯的服务范围内
            }
        }
        if (direction != 0 && (pf - currentFloor) * direction < 0) {
            if (direction > 0) { // 电梯上行，但乘客在下行方向
                reversalDelay = ((7 - currentFloor) + (7 - pf)) * speed * 0.8;
            } else { // direction < 0，电梯下行，但乘客在上行方向
                reversalDelay = ((currentFloor - (-4)) + (pf - (-4))) * speed * 0.8;
            }
        }
        // 计算电梯行程时间
        double travelTime;
        if (scheduleReq != null) { // 电梯处于临时调度中
            int target = parseFloor(scheduleReq.getToFloor());
            double reqSpeed = scheduleReq.getSpeed(); // 临时调度速度（秒/层）
            double finishOverhead = 1.0; // 完成调度额外开销（秒）
            double timeToFinish = Math.abs(currentFloor - target) * reqSpeed + finishOverhead;
            travelTime = timeToFinish + (Math.abs(target - pf) + Math.abs(pf - pd)) * speed;
        } else {
            travelTime = (Math.abs(currentFloor - pf) + Math.abs(pf - pd)) * speed;
        }

        double totalTime = travelTime + reversalDelay + loadPenalty + transferDelay;
        return (int)(totalTime * 1000); // 返回毫秒级的理论最短时间
    }

    private int parseFloor(String floorStr) {
        char type = floorStr.charAt(0);
        int num = floorStr.charAt(1) - '0';
        return (type == 'F') ? num : -num;
    }
    
    public int getElevatorId() { 
        return elevatorId;
    }

    public synchronized int getAssignedCount() {
        return assignedCount;
    }

    public synchronized void addAssignedCount() {
        assignedCount++;
    }
}
