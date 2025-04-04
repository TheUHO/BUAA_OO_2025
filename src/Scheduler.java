import java.util.HashMap;

public class Scheduler extends Thread {
    private final MainQueue mainQueue;
    private final HashMap<Integer, SubQueue> subQueues;
    private final HashMap<Integer, Elevator> elevators;
    private int count = 0;

    public Scheduler(MainQueue mainQueue, HashMap<Integer, SubQueue> subQueues,
        HashMap<Integer, Elevator> elevators) {
        this.mainQueue = mainQueue;
        this.subQueues = subQueues;
        this.elevators = elevators;
    }

    public Integer bestElevator(Person person) {
        // TODO: 根据电梯状态选择最优电梯
        count = (count + 1) % elevators.size(); // 轮询选择电梯
        return count;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (mainQueue) {
                if (mainQueue.isEmpty() && mainQueue.isEnd()) {
                    // TODO: 需要等到所有请求都被处理完，添加计数逻辑
                    for (SubQueue subQueue : subQueues.values()) {
                        subQueue.setEnd();
                    }
                    return;
                }
            }
            Person person = mainQueue.getPersonRequest();
            if (person != null) {
                subQueues.get(bestElevator(person)).addPersonRequest(person);
            } else {
                continue;
            }
        }
    }

}
