import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class NewStrategy { // 新策略：LOOK+ALS
    private final SubQueue subQueue; // 等待队列
    private final ArrayList<Person> persons; // 电梯内乘客列表
    private int personsIn;
    private final int maxPersonNum = 6;
    private AtomicReference<Person> mainRequestRef;
    private boolean hasUpdated = false; // 是否更新过请求
    private int transferFloor = 0; // 换乘楼层
    private boolean isA = false; // 是否是电梯A
    private boolean isB = false; // 是否是电梯B

    public NewStrategy(SubQueue subQueue, ArrayList<Person> persons, 
        int personsIn, AtomicReference<Person> mainRequestRef) {
        this.mainRequestRef = mainRequestRef; // 主请求
        this.subQueue = subQueue; // 初始化等待队列
        this.persons = persons; // 初始化电梯内乘客列表
        this.personsIn = personsIn;
    }

    public Advice getAdvice(int currentFloor, int direction, int personsIn, 
        boolean hasUpdated, int transferFloor, boolean isA, boolean isB) {
        this.personsIn = personsIn;
        this.hasUpdated = hasUpdated;
        this.transferFloor = transferFloor;
        this.isA = isA;
        this.isB = isB;
        if (subQueue.hasUpdateRequest()) { // 处理更新请求
            return Advice.UPDATE;
        }
        if (subQueue.hasScheRequest()) { // 处理临时调度请求
            return Advice.SCHE;
        }
        if (direction == 0) { // 电梯空闲状态选择主请求
            Person p = subQueue.getPrimaryRequest();
            if (p != null) {
                return Advice.MOVE;
            } else {
                return subQueue.isEnd() ? Advice.OVER : Advice.WAIT;
            }
        } else if (hasPersonOut(currentFloor) || hasPersonIn(currentFloor, direction)) {
            return Advice.OPEN; // 如果电梯内有乘客到达目的楼层，或者当前层有等待乘客且方向相同
        } else if (personsIn > 0) {
            return Advice.MOVE; // 如果电梯内有乘客，继续前进
        } else if (!subQueue.isEmpty()) {
            if (hasRequestAhead(currentFloor, direction)) {
                return Advice.MOVE; // 如果当前方向前方有请求，继续前进
            } else {
                return Advice.REVERSE; // 如果当前方向前方无请求，改变方向
            }
        } else if (subQueue.isEnd()) {
            return Advice.OVER; // 如果请求队列为空，且输入结束，结束电梯线程
        } else {
            return Advice.WAIT; // 如果电梯内无乘客，外部无请求，等待新请求
        }
    }

    private boolean hasPersonOut(int currentFloor) {
        if (personsIn == 0) {
            return false; // 如果电梯内无乘客，无需判断是否有人到达当前楼层
        }
        if (hasUpdated && currentFloor == transferFloor) {
            if (isA) {
                for (Person person : persons) {
                    if (person.getToInt() <= currentFloor) {
                        return true; // 如果电梯内有乘客到达目的楼层
                    }
                }
            } else if (isB) {
                for (Person person : persons) {
                    if (person.getToInt() >= currentFloor) {
                        return true; // 如果电梯内有乘客到达目的楼层
                    }
                }
            }
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
