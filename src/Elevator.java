import com.oocourse.elevator2.TimableOutput;
import java.util.ArrayList;
import java.util.Iterator;
import com.oocourse.elevator2.ScheRequest;

public class Elevator extends Thread {
    private final int id;
    private final SubQueue subQueue;
    private int currentFloor;
    private int personsIn;
    private int direction;
    private final ArrayList<Person> persons;
    private final ScheduleReq scheduleReq;
    private final LookStrategy strategy;
    private final int maxPersonNum = 6;
    private long lastTime; // 上次运行时间
    private double speed = 0.4; // 电梯速度

    public Elevator(int id, SubQueue subQueue, ScheduleReq scheduleReq) {
        this.id = id;
        this.subQueue = subQueue;
        this.currentFloor = 1;
        this.personsIn = 0;
        this.direction = 1; // 如何确定初始方向？
        this.persons = new ArrayList<>();
        this.scheduleReq = scheduleReq;
        this.strategy = new LookStrategy(subQueue, persons, scheduleReq, personsIn);
        this.lastTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (true) {
            Advice advice = strategy.getAdvice(currentFloor, direction, personsIn);
            //System.out.println("Elevator " + id + " " + advice + " " +
            //currentFloor + " " + direction + " " + personsIn);
            switch (advice) {
                case SCHE: // 临时调度请求
                    handleScheRequset();
                    break;
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

    private void handleScheRequset() {
        ScheRequest req = scheduleReq.getScheRequest();
        if (req == null) {
            return;
        }
        final int targetFloor = convertFloor(req.getToFloor());
        final double tempSpeed = req.getSpeed();
        int tempDirection = (targetFloor > currentFloor) ? 1 : 
            (targetFloor < currentFloor ? -1 : 0);// 确定运行方向：上行为 1，下行为 -1；若已在目标楼层则设为 0
        scheduleOpen(tempDirection, targetFloor); // 如果电梯内已有乘客，开门
        try {
            sleep(400); // 开门等待0.4s
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        scheduleClose(tempDirection, targetFloor); // 检查是否有候选乘客上电梯
        TimableOutput.println(String.format("SCHE-BEGIN-%d", id)); // 保证门已关闭后，开始临时调度
        lastTime = System.currentTimeMillis(); // 更新时间
        final double originalSpeed = speed; // 保存原始速度
        speed = tempSpeed;
        int tempFloor = currentFloor;
        currentFloor = targetFloor; // 设置当前楼层为目标楼层
        scheduleMove(tempDirection, targetFloor, tempFloor); // 模拟移动过程
        speed = originalSpeed; // 到达目标楼层后，恢复默认速度
        // 到达目标楼层，开门并保持 T_stop
        String targetFloorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
        TimableOutput.println(String.format("OPEN-%s-%d", targetFloorStr, id));
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            String outSign = p.getToInt() == currentFloor ? "S" : "F";
            TimableOutput.println(String.format("OUT-%s-%d-%s-%d", outSign,
                p.getPersonId(), targetFloorStr, id));
            iterator.remove();
            // TODO: 这里需要考虑将乘客放回主队列
            personsIn--;
        }
        long remainingTime = 1000 - System.currentTimeMillis() +  lastTime;
        if (remainingTime > 0) {
            try {
                sleep(remainingTime); // 保持开门1s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", targetFloorStr, id));
        TimableOutput.println(String.format("SCHE-END-%d", id));
        scheduleReq.setScheRequest(null); // 处理完请求后，清空请求
        lastTime = System.currentTimeMillis(); // 更新时间
    }

    private int convertFloor(String floor) {
        char type = floor.charAt(0);
        int num = floor.charAt(1) - '0';
        return (type == 'F') ? num : -num;
    }

    private void scheduleOpen(int tempDirection, int targetFloor) {
        boolean needOpen = false;
        for (Person p : persons) {
            int dest = p.getToInt();
            int extraDirection = dest - targetFloor;
            if ((tempDirection * extraDirection < 0)) { // 上行时目标 < 调度目标，下行时目标 > 调度目标
                needOpen = true;
                break;
            } else if (tempDirection == 0) { // tempDirection==0，直接认为需要开门
                needOpen = true;
                break;
            }
        }
        if (needOpen) { // 若需要下乘客，则开门让不满足条件的乘客下电梯
            String floorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
            TimableOutput.println(String.format("OPEN-%s-%d", 
                currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor), id));
            Iterator<Person> iterator = persons.iterator();
            while (iterator.hasNext()) {
                Person p = iterator.next();
                int dest = p.getToInt();
                int extraDirection = dest - targetFloor;
                boolean keep = false;
                keep = (tempDirection * extraDirection >= 0); // 上行时目标 > 调度目标，下行时目标 < 调度目标
                if (!keep) {
                    String outSign = p.getToInt() == currentFloor ? "S" : "F";
                    TimableOutput.println(String.format("OUT-%s-%d-%s-%d", outSign,
                        p.getPersonId(), floorStr, id));
                    iterator.remove();
                    // TODO: 这里需要考虑将乘客放回主队列
                    personsIn--;
                }
            }
        }
    }

    private void scheduleClose(int tempDirection, int targetFloor) {
        // 检查等待队列中是否有合适的候选乘客上电梯
        while (personsIn < maxPersonNum) {
            Person candidate = subQueue.getPersonIn(currentFloor, tempDirection);
            if (candidate == null) {
                break;
            }
            int dest = candidate.getToInt();
            int extraDirection = dest - targetFloor;
            boolean valid = false;
            if (tempDirection * extraDirection >= 0 && tempDirection != 0) {
                valid = true; // 满足条件，允许上电梯
            } 
            if (valid) {
                String floorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
                TimableOutput.println(String.format("IN-%d-%s-%d", 
                    candidate.getPersonId(), floorStr, id));
                persons.add(candidate);
                personsIn++;
            } else {
                // 若不满足条件，则将候选人放回队列，退出检查
                subQueue.addPersonRequest(candidate);
                break;
            }
        }
        // 关闭电梯门
        TimableOutput.println(String.format("CLOSE-%s-%d", 
            currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor), id));
    }

    private void scheduleMove(int tempDirection, int targetFloor, int floor) {
        // 模拟移动过程
        int tempFloor = floor;
        while (tempFloor != targetFloor) { // 如果当前楼层与目标楼层不同，则模拟移动过程
            tempFloor += tempDirection;
            if (tempFloor == 0) {
                tempFloor += tempDirection; // 避免出现 B0 层
            }
            String floorStr = tempFloor > 0 ? "F" + tempFloor : "B" + (-tempFloor);
            long remainingTime = (long)(speed * 1000) - System.currentTimeMillis() + lastTime;
            if (remainingTime > 0) {
                try {
                    sleep(remainingTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            TimableOutput.println(String.format("ARRIVE-%s-%d", floorStr, id));
            lastTime = System.currentTimeMillis(); // 更新时间
        }
    }

    private void openAndClose() {
        String floorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
        TimableOutput.println(String.format("OPEN-%s-%d", floorStr, id)); // 开门
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            if (p.getToInt() == currentFloor) { // 有乘客到达目的楼层
                String outSign = p.getToInt() == currentFloor ? "S" : "F";
                TimableOutput.println(String.format("OUT-%s-%d-%s-%d", outSign,
                    p.getPersonId(), floorStr, id));
                iterator.remove();
                personsIn--;
            }
        }
        try {
            sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (strategy.getAdvice(currentFloor, direction, personsIn) == Advice.REVERSE) {
            reverse(); // 没人可以直接反向，节省一次开关门时间
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
        // 如果电梯已满，尝试优先级替换
        while (personsIn == maxPersonNum) { // 电梯已满
            // 从等待队列中获取当前层、该方向的候选乘客
            Person waitingCandidate = subQueue.getPersonIn(currentFloor, direction);
            if (waitingCandidate == null) {
                break; // 如果没有候选乘客，跳出循环
            }
            // 尝试替换
            boolean replaced = tryReplaceLowestPassenger(waitingCandidate, floorStr);
            if (!replaced) {
                // 如果未满足替换条件，将等待乘客重新放回等待队列并跳出循环
                subQueue.addPersonRequest(waitingCandidate);
                break;
            }
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", floorStr, id));
        lastTime = System.currentTimeMillis(); // 更新时间
    }

    private void move() {
        // 量子电梯
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastTime;
        if (elapsedTime < speed) {
            long remainingTime = (long)(speed * 1000) - elapsedTime;
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
        int minElevatorPriority = Integer.MAX_VALUE;
        Person lowestP = null;
        for (Person p : persons) {
            if (p.getPriority() < minElevatorPriority) {
                minElevatorPriority = p.getPriority();
                lowestP = p;
            }
        }
        // 检查 waitingCandidate 的优先级是否达到替换标准（>=最低优先级）
        if (waitingCandidate.getPriority() >= minElevatorPriority && lowestP != null) {
            // 输出被替换乘客下电梯信息
            String outSign = waitingCandidate.getToInt() == currentFloor ? "S" : "F";
            TimableOutput.println(String.format("OUT-%s-%d-%s-%d", outSign,
                lowestP.getPersonId(), floorStr, id));
            // 执行深拷贝，将被替换乘客的 from 楼层改为当前楼层
            Person replacedP = new Person(floorStr, lowestP.getToFloor(),
                lowestP.getPersonId(), lowestP.getPriority());
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
