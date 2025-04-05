import com.oocourse.elevator2.PersonRequest;

public class Person {
    private final String fromFloor;
    private final String toFloor;
    private final int personId;
    private final int priority;
    private final int direction;
    private final int fromInt;
    private final int toInt;
    private final long timeStamp; // 乘客请求的时间戳

    public Person(PersonRequest request) {
        this.fromFloor = request.getFromFloor();
        this.toFloor = request.getToFloor();
        this.personId = request.getPersonId();
        this.priority = request.getPriority();
        this.fromInt = floorToInt(fromFloor);
        this.toInt = floorToInt(toFloor);
        this.direction = Integer.compare(toInt, fromInt);
        this.timeStamp = System.currentTimeMillis(); // 记录请求的时间戳
    }

    public Person(String fromFloor, String toFloor, int personId, int priority, long timeStamp) {
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.personId = personId;
        this.priority = priority;
        this.fromInt = floorToInt(fromFloor);
        this.toInt = floorToInt(toFloor);
        this.direction = Integer.compare(toInt, fromInt);
        this.timeStamp = timeStamp; // 记录请求的时间戳
    }

    public int getPersonId() {
        return personId;
    }

    public int getPriority() {
        return priority;
    }

    public String getFromFloor() {
        return fromFloor;
    }

    public int getFromInt() {
        return fromInt;
    }

    public String getToFloor() {
        return toFloor;
    }

    public int getToInt() {
        return toInt;
    }
    
    public int getDirection() {
        return direction;
    }

    public long getTimeStamp() {
        return timeStamp; // 返回请求的时间戳
    }

    // fromFloor和toFloor可到达楼层：地下B4-B1层，地上F1-F7层，共11层
    private int floorToInt(String floor) {
        if (floor.startsWith("B")) {
            return -Integer.parseInt(floor.substring(1));
        } else if (floor.startsWith("F")) {
            return Integer.parseInt(floor.substring(1));
        } else {
            throw new IllegalArgumentException("Invalid floor: " + floor);
        }
    }
}
