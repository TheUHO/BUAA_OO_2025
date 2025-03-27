import java.util.ArrayList;

public class Strategy {

    private final RequestQueue subQueue;
    private final ArrayList<Person> persons;
    private int personsIn;
    private final int maxPersonNum = 6;

    public Strategy(RequestQueue subQueue, ArrayList<Person> persons, int personsIn) {
        this.subQueue = subQueue;
        this.persons = persons;
        this.personsIn = personsIn;
    }

    public Advice getAdvice(int currentFloor, int direction, int personsIn) {
        this.personsIn = personsIn;
        if (hasPersonOut(currentFloor) || hasPersonIn(currentFloor, direction)) {
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
        for (Person person : subQueue.getPersons()) {
            if (person.getFromInt() == currentFloor && person.getDirection() == direction) {
                return true; // 如果当前层有等待乘客且方向相同
            }
        }
        return false;
    }

    private boolean hasRequestAhead(int currentFloor, int direction) {
        for (Person person : subQueue.getPersons()) { // 遍历请求队列
            if ((person.getFromInt() - currentFloor) * direction > 0) {
                return true; // 如果当前方向前方有请求
            }
        }
        return false;
    }
}
