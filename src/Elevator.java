import com.oocourse.elevator2.TimableOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import com.oocourse.elevator2.ScheRequest;

public class Elevator extends Thread {
    private final int id;
    private final MainQueue mainQueue;
    private final SubQueue subQueue;
    private int currentFloor;
    private String curFloorStr;
    private int personsIn;
    private int direction;
    private final ArrayList<Person> persons;
    private final LookStrategy strategy;
    private final NewStrategy newStrategy;
    private final int maxPersonNum = 6;
    private long lastTime; // 上次运行时间
    private double speed = 0.4; // 电梯速度
    private AtomicReference<Person> mainRequest; 

    public Elevator(int id, MainQueue mainQueue, SubQueue subQueue) {
        this.id = id;
        this.mainQueue = mainQueue;
        this.subQueue = subQueue;
        this.currentFloor = 1;
        this.curFloorStr = "F1"; // 初始楼层为1
        this.personsIn = 0;
        this.direction = 0; // 如何确定初始方向？
        this.persons = new ArrayList<>();
        this.mainRequest = new AtomicReference<>(null);
        this.strategy = new LookStrategy(subQueue, persons, personsIn);
        this.newStrategy = new NewStrategy(subQueue, persons, personsIn, mainRequest);
        this.lastTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (true) {
            Advice advice = newStrategy.getAdvice(currentFloor, direction, personsIn);
            if (direction == 0) {
                initialize(); // 初始化电梯状态
            }
            // System.out.println("Elevator: " + id + " " + advice + " at " +
            //     currentFloor + " d:" + direction + " with p:" + personsIn + " main: " +
            //     ((mainRequest.get() == null) ? "null" : mainRequest.get().getPersonId()));
            // System.out.println("MainQueue: " + mainQueue.getPassengerCount() + " " +
            //     mainQueue.getScheRequestCount() + " " + mainQueue.isAllEnd());
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
            ElevatorStorage.getInstance().updateShadow(id, 
                currentFloor, direction, personsIn, null);
        }
    }

    private void initialize() {
        Person req = subQueue.getPrimaryRequest(); // 获取主请求
        if (req != null) {
            mainRequest.set(req); // 设置主请求
            direction = (req.getFromInt() >= currentFloor) ? 1 : -1; // 确定初始方向
            printReceiveRequest(req);
        } else {
            direction = 0; // 没有请求，电梯不动
        }
        ElevatorStorage.getInstance().updateShadow(id, 
            currentFloor, direction, personsIn, null); // 更新影子电梯状态
    }

    private void printReceiveRequest(Person person) {
        TimableOutput.println(String.format("RECEIVE-%d-%d", person.getPersonId(), id));
        lastTime = System.currentTimeMillis(); // 更新时间
    }
    
    private void printOutRequest(Person person) {
        String outSign = person.getToInt() == currentFloor ? "S" : "F";
        TimableOutput.println(String.format("OUT-%s-%d-%s-%d", 
            outSign, person.getPersonId(), curFloorStr, id));
        personsIn--;
        if (outSign.equals("S")) {
            mainQueue.subPassengerCount();
        }
        if (mainRequest.get() != null && person.getPersonId() == mainRequest.get().getPersonId()) {
            mainRequest.set(null);
            Person p = subQueue.getPrimaryRequest(); // 获取主请求
            if (p != null && personsIn == 0) {
                mainRequest.set(p); // 设置主请求，但不改变方向
                // System.out.println("New main request: Elevator " + id + " at " +
                //     currentFloor + " d:" + direction + " with p:" + personsIn + " main: " +
                //     mainRequest.get().getPersonId());
                printReceiveRequest(mainRequest.get());
            } else if (p == null && personsIn == 0) {
                direction = 0; // 没有主请求，电梯不动
            }
        } else if (mainRequest.get() == null && personsIn == 0) {
            direction = 0; // 没有主请求，电梯不动
        }
    }

    private void printInRequest(Person person) {
        if (!person.equals(mainRequest.get())) {
            printReceiveRequest(person); // 打印接收请求
        }
        TimableOutput.println(String.format("IN-%d-%s-%d", person.getPersonId(), curFloorStr, id));
        personsIn++;
    }

    private void handleScheRequset() {
        ScheRequest req = subQueue.getScheRequest();
        if (req == null) {
            return;
        }
        final int targetFloor = convertFloor(req.getToFloor());
        final double tempSpeed = req.getSpeed();
        int tempDirection = (targetFloor > currentFloor) ? 1 : 
            (targetFloor < currentFloor ? -1 : 0);// 确定运行方向：上行为 1，下行为 -1；若已在目标楼层则设为 0
        ElevatorStorage.getInstance().updateShadow(id, 
            currentFloor, tempDirection, 0, req); // 更新影子电梯状态
        scheduleOpenAndClose(tempDirection, targetFloor); // 如果电梯内已有乘客，开门
        TimableOutput.println(String.format("SCHE-BEGIN-%d", id)); // 保证门已关闭后，开始临时调度
        lastTime = System.currentTimeMillis(); // 更新时间
        final double originalSpeed = speed; // 保存原始速度
        speed = tempSpeed;
        int tempFloor = currentFloor;
        currentFloor = targetFloor; // 设置当前楼层为目标楼层
        curFloorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
        scheduleMove(tempDirection, targetFloor, tempFloor); // 模拟移动过程
        speed = originalSpeed; // 到达目标楼层后，恢复默认速度
        // 到达目标楼层，开门并保持 T_stop
        TimableOutput.println(String.format("OPEN-%s-%d", curFloorStr, id));
        lastTime = System.currentTimeMillis(); // 更新时间
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            printOutRequest(p);
            iterator.remove();
            if (p.getToInt() != currentFloor) {
                Person person = new Person(curFloorStr, 
                    p.getToFloor(), p.getPersonId(), p.getPriority());
                mainQueue.addPersonRequest(person); // 将乘客放回主队列
            }
        }
        long remainingTime = 1000 - System.currentTimeMillis() +  lastTime;
        if (remainingTime > 0) {
            try {
                synchronized (this) {
                    wait(remainingTime); // 保持开门1s
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", curFloorStr, id));
        TimableOutput.println(String.format("SCHE-END-%d", id));
        lastTime = System.currentTimeMillis(); // 更新时间
        direction = 0; // 设置电梯方向为0，表示等待
        subQueue.setScheRequest(null); // 处理完请求后，清空请求
        mainQueue.subScheRequestCount();
    }

    private int convertFloor(String floor) {
        char type = floor.charAt(0);
        int num = floor.charAt(1) - '0';
        return (type == 'F') ? num : -num;
    }

    private void scheduleOpenAndClose(int tempDirection, int targetFloor) {
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
            TimableOutput.println(String.format("OPEN-%s-%d",curFloorStr, id));
            lastTime = System.currentTimeMillis(); // 更新时间
            Iterator<Person> iterator = persons.iterator();
            while (iterator.hasNext()) {
                Person p = iterator.next();
                int dest = p.getToInt();
                int extraDirection = dest - targetFloor;
                boolean keep = false;
                keep = (tempDirection * extraDirection >= 0); // 上行时目标 > 调度目标，下行时目标 < 调度目标
                if (!keep) {
                    printOutRequest(p);
                    iterator.remove();
                    if (p.getToInt() != currentFloor) {
                        Person person = new Person(curFloorStr, 
                            p.getToFloor(), p.getPersonId(), p.getPriority());
                        mainQueue.addPersonRequest(person); // 将乘客放回主队列
                    }
                }
            }
            long remainingTime = 400 - System.currentTimeMillis() + lastTime;
            if (remainingTime > 0) {
                try {
                    synchronized (this) {
                        wait(remainingTime); // 保持开门400ms
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int capacity = maxPersonNum - personsIn; // 计算电梯可用容量
            ArrayList<Person> candidates = subQueue.getMatchingCandidates(currentFloor, 
                tempDirection, targetFloor, capacity); // 从等待队列获取所有符合条件的候选乘客
            for (Person p : candidates) { // 遍历候选乘客
                printInRequest(p); // 输出接收请求信息
                persons.add(p); // 将乘客加入电梯内部队列
                personsIn++; // 更新电梯内人数
            }
            TimableOutput.println(String.format("CLOSE-%s-%d", curFloorStr, id)); // 输出关闭电梯门信息
            lastTime = System.currentTimeMillis(); // 更新时间
        }
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
        TimableOutput.println(String.format("OPEN-%s-%d", curFloorStr, id)); // 开门
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            if (p.getToInt() == currentFloor) { // 有乘客到达目的楼层
                printOutRequest(p);
                iterator.remove();
            }
        }
        try {
            synchronized (this) {
                wait(400);
            }
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
            printInRequest(p);
            persons.add(p);
        }
        // 将这层楼所有未成功的请求放回主队列
        ArrayList<Person> unmatchedPersons = new ArrayList<>();
        Person person = subQueue.getPersonIn(currentFloor, direction);
        while (person != null) {
            Person p = this.mainRequest.get(); // 获取主请求
            if (p != null && person.getPersonId() == p.getPersonId()) {
                unmatchedPersons.add(person); // 如果是主请求，暂存到未匹配列表
            } else {
                mainQueue.addPersonRequest(person); // 如果不是主请求，放回主队列
            }
            person = subQueue.getPersonIn(currentFloor, direction);
        }
        for (Person unmatched : unmatchedPersons) {
            subQueue.addPersonRequest(unmatched); // 将未匹配的请求放回等待队列
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", curFloorStr, id));
        lastTime = System.currentTimeMillis(); // 更新时间
    }

    private void move() {
        // 量子电梯
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastTime;
        if (elapsedTime < (long)(speed * 1000)) {
            long remainingTime = (long)(speed * 1000) - elapsedTime;
            try {
                synchronized (this) {
                    wait(remainingTime);
                } 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 中断
            }
        }
        Advice advice = strategy.getAdvice(currentFloor, direction, personsIn);
        if (advice == Advice.SCHE) {
            // 更新楼层，避免出现B0层
            currentFloor += direction;
            if (currentFloor == 0) {
                currentFloor += direction;
            }
            curFloorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
            TimableOutput.println(String.format("ARRIVE-%s-%d", curFloorStr, id));
            lastTime = System.currentTimeMillis(); // 更新时间
            handleScheRequset();
            return;
        } else if (advice == Advice.OPEN) {
            openAndClose(); // 打开并关闭
            return;
        }
        // 更新楼层，避免出现B0层
        currentFloor += direction;
        if (currentFloor == 0) {
            currentFloor += direction;
        }
        curFloorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
        TimableOutput.println(String.format("ARRIVE-%s-%d", curFloorStr, id));
        lastTime = System.currentTimeMillis(); // 更新时间
    }

    private void reverse() {
        direction = -direction;
    }

    private void waitRequest() {
        direction = 0; // 设置电梯方向为0，表示等待
        ElevatorStorage.getInstance().updateShadow(id, 
            currentFloor, direction, personsIn, null); // 更新影子电梯状态
        synchronized (subQueue) {
            try {
                // 调用等待方法，使当前线程挂起
                subQueue.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
