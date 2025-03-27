import com.oocourse.elevator1.TimableOutput;
import java.util.ArrayList;
import java.util.Iterator;

public class Elevator extends Thread {
    private final int id;
    private final RequestQueue subQueue;
    private int currentFloor;
    private int personsIn;
    private int direction;
    private final ArrayList<Person> persons;
    private final Strategy strategy;
    private final int maxPersonNum = 6;

    public Elevator(int id, RequestQueue subQueue) {
        this.id = id;
        this.subQueue = subQueue;
        this.currentFloor = 1;
        this.personsIn = 0;
        this.direction = 1; // 如何确定初始方向？
        this.persons = new ArrayList<>();
        this.strategy = new Strategy(subQueue, persons, personsIn);
    }

    @Override
    public void run() {
        while (true) {
            Advice advice = strategy.getAdvice(currentFloor, direction, personsIn);
            //System.out.println("Elevator " + id + " " + advice + " " +
            //currentFloor + " " + direction + " " + personsIn);
            switch (advice) {
                case OPEN:
                    openAndClose();
                    break;
                case MOVE:
                    move();
                    break;
                case REVERSE:
                    reverse();
                    break;
                case OVER: // 结束
                    return;
                case WAIT:
                    waitRequest();
                    break;
                default:
                    System.out.println("Error in Elevator.run");
                    break;
            }
        }
    }

    private void openAndClose() {
        String floorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
        TimableOutput.println(String.format("OPEN-%s-%d", floorStr, id)); // 开门
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            if (p.getToInt() == currentFloor) { // 有乘客到达目的楼层
                TimableOutput.println(String.format("OUT-%d-%s-%d", p.getPersonId(), floorStr, id));
                iterator.remove();
                personsIn--;
            }
        }
        try {
            sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (personsIn < maxPersonNum) { // 上人
            Person p = subQueue.getPersonIn(currentFloor, direction);
            if (p == null) {
                break;
            }
            TimableOutput.println(String.format("IN-%d-%s-%d", p.getPersonId(), floorStr, id));
            persons.add(p);
            personsIn++;
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", floorStr, id));
    }

    private void move() {
        try {
            sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 更新楼层
        currentFloor += direction;
        String floorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
        TimableOutput.println(String.format("ARRIVE-%s-%d", floorStr, id));
    }

    private void reverse() {
        direction = -direction;
    }

    private void waitRequest() {
        synchronized (subQueue) {
            try {
                // 调用等待方法，使当前线程挂起
                subQueue.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
