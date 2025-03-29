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
    private long lastTime; // 上次运行时间

    public Elevator(int id, RequestQueue subQueue) {
        this.id = id;
        this.subQueue = subQueue;
        this.currentFloor = 1;
        this.personsIn = 0;
        this.direction = -1; // 如何确定初始方向？
        this.persons = new ArrayList<>();
        this.strategy = new Strategy(subQueue, persons, personsIn);
        this.lastTime = System.currentTimeMillis();
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
        // 如果电梯已满，尝试优先级替换
        if (personsIn == maxPersonNum) {
            // 从等待队列中获取当前层、该方向的候选乘客
            Person waitingCandidate = subQueue.getPersonIn(currentFloor, direction);
            if (waitingCandidate != null) {
                // 尝试替换
                boolean replaced = tryReplaceLowestPassenger(waitingCandidate, floorStr);
                if (!replaced) {
                    // 如果未满足替换条件，将等待乘客重新放回等待队列
                    subQueue.addPersonRequest(waitingCandidate);
                }
            }
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
        lastTime = System.currentTimeMillis(); // 更新时间
    }

    private void move() {
        // 量子电梯
        final long speed = 400; // 速度
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastTime;
        if (elapsedTime < speed) {
            long remainingTime = speed - elapsedTime;
            try {
                synchronized (this) {
                    wait(remainingTime);
                } 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 中断
            }
        }
        Advice advice = strategy.getAdvice(currentFloor, direction, personsIn); // 获取建议
        if (advice == Advice.OPEN) {
            openAndClose(); // 打开并关闭
            // lastTime = System.currentTimeMillis(); // 更新时间
            return;
        }
        // 更新楼层，避免出现B0层
        currentFloor += direction;
        if (currentFloor == 0) {
            currentFloor += direction;
        }
        String floorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
        TimableOutput.println(String.format("ARRIVE-%s-%d", floorStr, id));
        lastTime = System.currentTimeMillis(); // 更新时间
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

    private boolean tryReplaceLowestPassenger(Person waitingCandidate, String floorStr) {
        int maxElevatorPriority = Integer.MIN_VALUE;
        int minElevatorPriority = Integer.MAX_VALUE;
        Person lowestP = null;
        for (Person p : persons) {
            if (p.getPriority() > maxElevatorPriority) {
                maxElevatorPriority = p.getPriority();
            }
            if (p.getPriority() < minElevatorPriority) {
                minElevatorPriority = p.getPriority();
                lowestP = p;
            }
        }
        // 检查 waitingCandidate 的优先级是否达到替换标准（>= 4倍最高优先级）
        if (waitingCandidate.getPriority() >= 4 * maxElevatorPriority && lowestP != null) {
            // 输出被替换乘客下电梯信息
            TimableOutput.println(String.format("OUT-%d-%s-%d", 
                lowestP.getPersonId(), floorStr, id));
            // 执行深拷贝，将被替换乘客的 from 楼层改为当前楼层
            Person replacedP = new Person(floorStr, lowestP.getToFloor(),
                lowestP.getPersonId(), lowestP.getPriority(), lowestP.getElevatorId());
            // 从电梯中移除该乘客
            persons.remove(lowestP);
            personsIn--;
            // 将深拷贝的对象加入等待队列
            subQueue.addPersonRequest(replacedP);
            // 让等待队列中的高优先级候选乘客上电梯
            TimableOutput.println(String.format("IN-%d-%s-%d", 
                waitingCandidate.getPersonId(), floorStr, id));
            persons.add(waitingCandidate);
            personsIn++;
            return true;
        }
        return false;
    }
}
