import com.oocourse.elevator2.ScheRequest;

public class ShadowElevator {
    private int elevatorId;
    private int currentFloor;
    private int direction;
    private int personsIn;
    private ScheRequest scheduleReq;
    private static final int MAX_CAPACITY = 6;
    
    public ShadowElevator(int elevatorId, int currentFloor,
        int direction, int personsIn, ScheRequest scheduleReq) {
        this.elevatorId = elevatorId;
        this.currentFloor = currentFloor;
        this.direction = direction;
        this.personsIn = personsIn;
        this.scheduleReq = scheduleReq;
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
        double doorTime = 0.4; // 门操作时间（开关门合计）
        double reversalPenalty = 0.0; // 如果电梯正行驶方向与乘客不符则加延迟
        if (direction != 0 && (pf - currentFloor) * direction < 0) {
            reversalPenalty = 2.0; // 加 2 秒反转延迟
        }
        double totalTime;
        if (scheduleReq != null) { // 电梯处于临时调度中
            String floorStr = scheduleReq.getToFloor();
            char type = floorStr.charAt(0);
            int num = floorStr.charAt(1) - '0';
            int target = (type == 'F') ? num : -num;
            double reqSpeed = scheduleReq.getSpeed(); // 临时调度速度（秒/层）
            double tempOverhead = 1.8; // 完成临时调度的额外开销（秒）
            // 完成临时调度所需时间
            double timeToFinish = Math.abs(currentFloor - target) * reqSpeed + tempOverhead;
            double timeFromTarget = (Math.abs(target - pf) + Math.abs(pf - pd)) * 0.4 + doorTime;
            totalTime = timeToFinish + timeFromTarget + reversalPenalty;
        } else {
            // 电梯空闲时，直接以默认速度行驶
            totalTime = (Math.abs(currentFloor - pf) + Math.abs(pf - pd)) * 0.4 
                + doorTime + reversalPenalty;
        }
        return (int)(totalTime * 1000); // 返回毫秒级的理论最短时间
    }
    
    public int getElevatorId() { 
        return elevatorId;
    }
}
