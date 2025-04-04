import java.util.ArrayList;

public class NewStrategy { // 新策略：LOOK+ALS
    private final SubQueue subQueue; // 等待队列
    private final ArrayList<Person> persons; // 电梯内乘客列表
    private final ScheduleReq scheduleReq; // 临时调度请求
    private int personsIn;
    private final int maxPersonNum = 6;
    private Person mainRequest; // 主请求

    public NewStrategy(SubQueue subQueue, ArrayList<Person> persons, ScheduleReq scheduleReq, 
        int personsIn, Person mainRequest) {
        this.mainRequest = mainRequest; // 初始化主请求
        this.subQueue = subQueue; // 初始化等待队列
        this.persons = persons; // 初始化电梯内乘客列表
        this.scheduleReq = scheduleReq; // 初始化临时调度请求
        this.personsIn = personsIn;
    }

    public synchronized Advice getAdvice(int currentFloor, int direction, int personsIn) {
        this.personsIn = personsIn;
        // 处理临时调度请求
        if (scheduleReq != null) {
            return Advice.SCHE;
        }
        // 电梯空闲状态选择主请求
        if (personsIn == 0 && mainRequest == null) {
            mainRequest = subQueue.getPrimaryRequest();
            if (mainRequest != null) {
                return Advice.MOVE;
            } else {
                return subQueue.isEnd() ? Advice.OVER : Advice.WAIT;
            }
        }
        // 常规运行逻辑
        if (hasPersonOut(currentFloor) || hasPersonIn(currentFloor, direction)) {
            return Advice.OPEN;
        }

        if (hasRequestAhead(currentFloor, direction)) {
            return Advice.MOVE;
        } else {
            return personsIn > 0 ? Advice.MOVE : Advice.REVERSE;
        }
    }

    private boolean hasPersonOut(int currentFloor) {
        if (personsIn == 0) {
            return false; // 如果电梯内无乘客，无需判断是否有人到达当前楼层
        }
        for (Person person : persons) {
            if (person.getToInt() == currentFloor) {
                return true; // 如果电梯内有乘客到达目的楼层
            }
        }
        return false;
    }

    private boolean hasPersonIn(int currentFloor, int direction) {
        if (personsIn == maxPersonNum) {
            return false; // 如果电梯内已满，无需判断是否有人进入
        }
        synchronized (subQueue) {
            for (Person person : subQueue.getPersons()) {
                if (person.getFromInt() == currentFloor && person.getDirection() == direction) {
                    return true; // 如果当前层有等待乘客且方向相同
                }
            }
        }
        return false;
    }

    private boolean hasRequestAhead(int currentFloor, int direction) {
        synchronized (subQueue) {
            if (subQueue.isEmpty()) {
                return false; // 如果请求队列为空，无需判断前方是否有请求
            }
            for (Person person : subQueue.getPersons()) { // 遍历请求队列
                if ((person.getFromInt() - currentFloor) * direction > 0) {
                    return true; // 如果当前方向前方有请求
                }
            }
        }
        return false;
    }
}
