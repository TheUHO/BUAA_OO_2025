import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.UpdateRequest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import com.oocourse.elevator3.ScheRequest;

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
    private Coordinator coordinator = null;
    private boolean isA = false; // 是否是电梯A
    private boolean isB = false; // 是否是电梯B
    private boolean hasUpdated = false; // 是否更新过请求
    private int transferFloor = 100;

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
        this.transferFloor = 100; // 初始换乘楼层为100，表示未设置换乘楼层
    }

    @Override
    public void run() {
        while (true) {
            Advice advice = newStrategy.getAdvice(currentFloor, direction, 
                personsIn, hasUpdated, transferFloor, isA, isB);
            if (direction == 0) {
                initialize(); // 初始化电梯状态
            }
            // System.out.println("Elevator: " + id + " " + advice + " at " +
            //     currentFloor + " d:" + direction + " with p:" + personsIn + " main: " +
            //     ((mainRequest.get() == null) ? "null" : mainRequest.get().getPersonId()));
            // System.out.println("MainQueue: " + mainQueue.getPassengerCount() + " " +
            //     mainQueue.getScheRequestCount() + " "  + mainQueue.getUpdateCount());
            // System.out.println("Sub: " + subQueue.getPersons().size() + " " + subQueue.isEnd());
            switch (advice) {
                case UPDATE:
                    update();
                    break;
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
            ElevatorStorage.getInstance().updateShadow(id, currentFloor, direction, 
                personsIn, null, isA, isB, transferFloor); // 更新影子电梯状态
        }
    }

    private void initialize() {
        Person req = subQueue.getPrimaryRequest(); // 获取主请求
        if (req != null) {
            mainRequest.set(req); // 设置主请求
            int targetFloor = req.getFromInt();
            direction = (targetFloor > currentFloor) ? 1 :
                        (targetFloor < currentFloor) ? -1 : 
                        (req.getToInt() > currentFloor) ? 1 : 
                        (req.getToInt() < currentFloor) ? -1 : 0;
            printReceiveRequest(req);
        } else { direction = 0; } // 没有请求，电梯不动
        ElevatorStorage.getInstance().updateShadow(id, currentFloor, direction, 
            personsIn, null, isA, isB, transferFloor); // 更新影子电梯状态
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
        } else {
            Person p = new Person(curFloorStr, person.getToFloor(), 
                person.getPersonId(), person.getPriority(), person.getTimeStamp());
            mainQueue.addPersonRequest(p); // 将乘客放回主队列
        }
        if (mainRequest.get() != null && person.getPersonId() == mainRequest.get().getPersonId()) {
            mainRequest.set(null);
        }
    }

    private void printInRequest(Person person) {
        if (!person.equals(mainRequest.get())) { printReceiveRequest(person); } // 打印接收请求
        TimableOutput.println(String.format("IN-%d-%s-%d", person.getPersonId(), curFloorStr, id));
        lastTime = System.currentTimeMillis(); // 更新时间
        personsIn++;
    }

    void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator; // 设置协作器
    }

    private void update() {
        if (hasUpdated) { return; } // 如果已经更新过请求，则不再更新
        if (personsIn != 0) { cleanOpenAndClose(400); } // 如果电梯内有乘客，清空电梯内乘客
        UpdateRequest updateRequest = subQueue.getUpdateRequest(); // 获取更新请求
        int aid = updateRequest.getElevatorAId();
        int bid = updateRequest.getElevatorBId();
        String targetStr = updateRequest.getTransferFloor();
        transferFloor = convertFloor(targetStr);
        isA = (id == aid);
        isB = (id == bid);
        if (!isA && !isB) { return; } // 不是参与改造的电梯
        ElevatorStorage.getInstance().updateShadow(id, currentFloor, direction, 
            personsIn, null, isA, isB, transferFloor); // 更新影子电梯状态
        coordinator.updateMonitor(id); // 协作等待
        currentFloor = transferFloor + (isA ? 1 : -1);
        if (currentFloor == 0) { currentFloor += (isA ? 1 : -1); } // 避免出现 B0 层
        curFloorStr = currentFloor > 0 ? "F" + currentFloor : "B" + (-currentFloor);
        speed = 0.2; // 调整运行速度为 0.2 s/层
        hasUpdated = true;
        synchronized (subQueue) {
            Person unablPerson = subQueue.getUnableRequest(isA, isB, transferFloor); // 清除等待队列中的无效请求
            while (unablPerson != null) {
                mainQueue.addPersonRequest(unablPerson); // 将无效请求放回主队列
                unablPerson = subQueue.getUnableRequest(isA, isB, transferFloor);
            }
        }
        subQueue.setUpdateRequest(null); // 清除等待队列中的更新请求
        if (isA) { mainQueue.subUpdateCount(); }
    }

    private void cleanOpenAndClose(long waitingTime) { // 清空电梯内所有乘客
        TimableOutput.println(String.format("OPEN-%s-%d",curFloorStr, id));
        lastTime = System.currentTimeMillis(); // 更新时间
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            printOutRequest(p);
            iterator.remove();
        }
        long remainingTime = waitingTime - System.currentTimeMillis() +  lastTime;
        if (remainingTime > 0) {
            try { synchronized (this) { wait(remainingTime); } } 
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", curFloorStr, id));
        lastTime = System.currentTimeMillis(); // 更新时间
    }

    private void handleScheRequset() {
        ScheRequest req = subQueue.getScheRequest();
        if (req == null) { return; } // 如果没有临时调度请求，则返回
        final int targetFloor = convertFloor(req.getToFloor());
        final double tempSpeed = req.getSpeed();
        int tempDirection = (targetFloor > currentFloor) ? 1 : 
            (targetFloor < currentFloor ? -1 : 0);// 确定运行方向：上行为 1，下行为 -1；若已在目标楼层则设为 0
        ElevatorStorage.getInstance().updateShadow(id, currentFloor, direction, 
            personsIn, null, isA, isB, transferFloor); // 更新影子电梯状态
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
        cleanOpenAndClose(1000);
        TimableOutput.println(String.format("SCHE-END-%d", id));
        lastTime = System.currentTimeMillis(); // 更新时间
        direction = 0; // 设置电梯方向为0，表示等待
        subQueue.setScheRequest(null); // 处理完请求后，清空请求
        mainQueue.subScheRequestCount();
    }

    private int convertFloor(String floor) {
        return (floor.charAt(0) == 'F') ? floor.charAt(1) - '0' : -(floor.charAt(1) - '0');
    }

    private void scheduleOpenAndClose(int tempDirection, int targetFloor) {
        if (personsIn == 0) { return; }
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
                }
            }
            long remainingTime = 400 - System.currentTimeMillis() + lastTime;
            if (remainingTime > 0) {
                try { synchronized (this) { wait(remainingTime); } } 
                catch (InterruptedException e) { e.printStackTrace(); }
            }
            int capacity = maxPersonNum - personsIn; // 计算电梯可用容量
            ArrayList<Person> candidates = subQueue.getMatchingCandidates(currentFloor, 
                tempDirection, targetFloor, capacity); // 从等待队列获取所有符合条件的候选乘客
            for (Person p : candidates) { // 遍历候选乘客
                printInRequest(p); // 输出接收请求信息
                persons.add(p); // 将乘客加入电梯内部队列
            }
            TimableOutput.println(String.format("CLOSE-%s-%d", curFloorStr, id)); // 输出关闭电梯门信息
            lastTime = System.currentTimeMillis(); // 更新时间
        }
    }

    private void scheduleMove(int tempDirection, int targetFloor, int floor) { // 模拟移动过程
        int tempFloor = floor;
        while (tempFloor != targetFloor) { // 如果当前楼层与目标楼层不同，则模拟移动过程
            tempFloor += tempDirection;
            if (tempFloor == 0) { tempFloor += tempDirection; } // 避免出现 B0 层
            String floorStr = tempFloor > 0 ? "F" + tempFloor : "B" + (-tempFloor);
            long remainingTime = (long)(speed * 1000) - System.currentTimeMillis() + lastTime;
            if (remainingTime > 0) {
                try { synchronized (this) { wait(remainingTime); } }
                catch (InterruptedException e) { e.printStackTrace(); }
            }
            TimableOutput.println(String.format("ARRIVE-%s-%d", floorStr, id));
            lastTime = System.currentTimeMillis(); // 更新时间
        }
    }

    private void openAndClose() {
        TimableOutput.println(String.format("OPEN-%s-%d", curFloorStr, id)); // 开门
        lastTime = System.currentTimeMillis();
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            if ((isA && currentFloor == transferFloor && p.getToInt() <= currentFloor) ||
                (isB && currentFloor == transferFloor && p.getToInt() >= currentFloor) ||
                (p.getToInt() == currentFloor)) {
                iterator.remove();
                printOutRequest(p);
            }
        }
        long remainingTime = (long)(400) - (System.currentTimeMillis() - lastTime);
        if (remainingTime > 0) {
            try { synchronized (this) { wait(remainingTime); } } 
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        if (strategy.getAdvice(currentFloor, direction, personsIn) == Advice.REVERSE) {
            reverse(); // 没人可以直接反向，节省一次开关门时间
        }
        while (personsIn < maxPersonNum) { // 上人
            Person p = subQueue.getPersonIn(currentFloor, direction);
            if (p == null) { break; }
            printInRequest(p);
            persons.add(p);
        }
        ArrayList<Person> unmatchedPersons = new ArrayList<>(); // 存储未匹配的请求
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
        if (mainRequest.get() == null && personsIn == 0 && 
            !(hasUpdated && currentFloor == transferFloor)) {
            Person person = subQueue.getPrimaryRequest(); // 获取主请求
            if (person != null) {
                mainRequest.set(person); // 获取主请求
                printReceiveRequest(person);
            } else {
                direction = 0; // 没有请求，电梯不动
                return;
            }
        }
        int nextFloor = currentFloor + direction;
        if (nextFloor == 0) {
            nextFloor += direction; // 避免出现B0 层
        }
        String nextFloorStr = nextFloor > 0 ? "F" + nextFloor : "B" + (-nextFloor);
        long remainingTime = (long)(speed * 1000) - (System.currentTimeMillis() - lastTime);
        if (remainingTime > 0) {
            try { synchronized (this) { wait(remainingTime); } 
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); } // 中断
        }
        Advice advice = strategy.getAdvice(currentFloor, direction, personsIn);
        if (advice == Advice.SCHE || advice == Advice.OPEN) {
            moveHandleRequest(advice, nextFloor, nextFloorStr);
            return;
        }
        currentFloor = nextFloor; // 更新当前楼层
        curFloorStr = nextFloorStr; // 更新当前楼层字符串
        if (currentFloor == transferFloor) {
            coordinator.robTransferFloor(); // 等待进入换乘层
        }
        TimableOutput.println(String.format("ARRIVE-%s-%d", curFloorStr, id));
        lastTime = System.currentTimeMillis(); // 更新时间
        int tempFloor = transferFloor + direction;
        if (tempFloor == 0) {
            tempFloor += direction; // 避免出现B0 层
        }
        if (currentFloor == tempFloor) {
            coordinator.releaseTransferFloor(); // 离开换乘层，释放换乘层
        }
    }

    void moveHandleRequest(Advice advice, int nextFloor, String nextFloorStr) {
        if (advice == Advice.SCHE) {
            ScheRequest req = subQueue.getScheRequest();
            if (req != null) {
                final int targetFloor = convertFloor(req.getToFloor());
                if (personsIn == 0 && Math.abs(currentFloor + direction - targetFloor) 
                    > Math.abs(currentFloor - targetFloor)) {
                    // 如果电梯内没人且移动后离目标楼层更远，则不更新楼层
                } else { // 更新楼层，避免出现B0层
                    currentFloor = nextFloor;
                    curFloorStr = nextFloorStr;
                    int tempFloor = transferFloor + direction;
                    if (tempFloor == 0) { tempFloor += direction; } // 避免出现B0 层
                    if (currentFloor == tempFloor) {
                        coordinator.releaseTransferFloor(); // 离开换乘层，释放换乘层
                    }
                    TimableOutput.println(String.format("ARRIVE-%s-%d", curFloorStr, id));
                    lastTime = System.currentTimeMillis(); // 更新时间
                }
                handleScheRequset();
            }
        } else if (advice == Advice.OPEN) {
            openAndClose(); // 打开并关闭
        }
    }

    private void reverse() {
        direction = -direction;
    }

    private void waitRequest() {
        if (hasUpdated && currentFloor == transferFloor) {
            if (isA) {
                direction = 1; // 电梯A在换乘层等待，方向向上
                move(); // 离开换乘层
            } else if (isB) {
                direction = -1; // 电梯B在换乘层等待，方向向下
                move(); // 离开换乘层
            }
            return;
        }
        direction = 0; // 设置电梯方向为0，表示等待
        ElevatorStorage.getInstance().updateShadow(id, currentFloor, direction, 
            personsIn, null, isA, isB, transferFloor); // 更新影子电梯状态
        synchronized (subQueue) {
            if (subQueue.isEnd()) { return; } // 如果请求队列已结束，则不再等待
            try { subQueue.wait(); } 
            catch (InterruptedException e) { Thread.currentThread().interrupt(); } // 调用等待方法，使当前线程挂起
        }
    }
}
