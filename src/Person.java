import com.oocourse.elevator1.PersonRequest;

public class Person {
    private final String fromFloor;
    private final String toFloor;
    private final int personId;
    private final int priority;
    private final int elevatorId;
    private final int direction;
    private final int fromInt;
    private final int toInt;

    public Person(PersonRequest request) {
        this.fromFloor = request.getFromFloor();
        this.toFloor = request.getToFloor();
        this.personId = request.getPersonId();
        this.priority = request.getPriority();
        this.elevatorId = request.getElevatorId();
        this.fromInt = floorToInt(fromFloor);
        this.toInt = floorToInt(toFloor);
        this.direction = Integer.compare(toInt, fromInt);
    }

    public int getPersonId() {
        return personId;
    }

    public int getPriority() {
        return priority;
    }

    public int getElevatorId() {
        return elevatorId;
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
