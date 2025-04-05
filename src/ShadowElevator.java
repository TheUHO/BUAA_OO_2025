import com.oocourse.elevator2.ScheRequest;

public class ShadowElevator {
    private int elevatorId;
    private int currentFloor;
    private int direction;
    private int personsIn;
    private ScheRequest scheduleReq;
    private int assignedCount = 0;
    private static final int MAX_CAPACITY = 6;
    
    public ShadowElevator(int elevatorId, int currentFloor,
        int direction, int personsIn, ScheRequest scheduleReq) {
        this.elevatorId = elevatorId;
        this.currentFloor = currentFloor;
        this.direction = direction;
        this.personsIn = personsIn;
        this.scheduleReq = scheduleReq;
        this.assignedCount = 0;
    }
    
    public synchronized void update(int currentFloor, int direction,
        int personsIn, ScheRequest scheduleReq) {
        this.currentFloor = currentFloor;
        this.direction = direction;
        this.personsIn = personsIn;
        this.scheduleReq = scheduleReq;
    }

    public synchronized int getEstimatePerformance(Person person) {
        int pf = person.getFromInt(); // 乘客起始楼层
        int pd = person.getToInt();   // 乘客目的楼层
        
        double reversalDelay = 0.0;
        // 判断是否需要反转：电梯正行驶且乘客楼层方向与当前方向相反
        if (direction != 0 && (pf - currentFloor) * direction < 0) {
            if (direction > 0) { // 电梯上行，但乘客在下行方向
                reversalDelay = ((7 - currentFloor) + (7 - pf)) * 0.4;
            } else { // direction < 0，电梯下行，但乘客在上行方向
                reversalDelay = (((currentFloor - (-4)) + (pf - (-4))) * 0.4);
            }
        }
        double loadPenalty = personsIn * 0.4; // 电梯载客数带来的延迟（每人0.5秒）
        double travelTime;
        if (scheduleReq != null) { // 电梯处于临时调度中
            int target = parseFloor(scheduleReq.getToFloor());
            double reqSpeed = scheduleReq.getSpeed(); // 临时调度速度（秒/层）
            double finishOverhead = 1.0; // 完成调度额外开销（秒）
            double timeToFinish = Math.abs(currentFloor - target) * reqSpeed + finishOverhead;
            travelTime = timeToFinish + (Math.abs(target - pf) + Math.abs(pf - pd)) * 0.4;
        } else {
            travelTime = (Math.abs(currentFloor - pf) + Math.abs(pf - pd)) * 0.4;
        }
        
        double totalTime = travelTime + reversalDelay + loadPenalty;
        return (int)(totalTime * 1000);
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
