import java.util.Collection;
import java.util.HashMap;

public class Scheduler extends Thread {
    private final MainQueue mainQueue; // 主请求队列
    private final HashMap<Integer, SubQueue> subQueues; // 各等待队列
    private final HashMap<Integer, Elevator> elevators; // 电梯集合

    public Scheduler(MainQueue mainQueue, HashMap<Integer, SubQueue> subQueues,
        HashMap<Integer, Elevator> elevators) {
        this.mainQueue = mainQueue;
        this.subQueues = subQueues;
        this.elevators = elevators;
    }

    public Integer bestElevator(Person person) {
        Collection<ShadowElevator> shadows = ElevatorStorage.getInstance().getAllShadows();
        int bestPerformance = Integer.MAX_VALUE;
        int bestAssignedCount = Integer.MAX_VALUE;
        int bestElevatorId = -1;
        ShadowElevator bestShadow = null; 
        for (ShadowElevator shadow : shadows) {
            int perf = shadow.getEstimatePerformance(person);
            if (perf < bestPerformance) {
                bestPerformance = perf;
                bestAssignedCount = shadow.getAssignedCount();
                bestElevatorId = shadow.getElevatorId();
                bestShadow = shadow;
            } else if (perf == bestPerformance) {
                int currentAssigned = shadow.getAssignedCount();
                if (currentAssigned < bestAssignedCount) {
                    bestAssignedCount = currentAssigned;
                    bestElevatorId = shadow.getElevatorId();
                    bestShadow = shadow;
                } else if (currentAssigned == bestAssignedCount) {
                    // 如果已分配数量也相同，则选择电梯编号较小的
                    if (shadow.getElevatorId() < bestElevatorId) {
                        bestElevatorId = shadow.getElevatorId();
                        bestShadow = shadow;
                    }
                }
            }
        }
        if (bestShadow != null) { // 选定后增加该电梯的请求分配计数
            bestShadow.addAssignedCount();
        }
        return bestElevatorId;
    }

    @Override
    public void run() {
        while (true) {
            if (mainQueue.isAllEnd()) {
                for (SubQueue subQueue : subQueues.values()) {
                    subQueue.setEnd();
                }
                return;
            }
            Person person = mainQueue.getPersonRequest();
            if (person != null) {
                int elevatorId = bestElevator(person);
                subQueues.get(elevatorId).addPersonRequest(person);
                // System.out.println("Per " + person.getPersonId() + "to Elevator " + elevatorId);
            } else {
                continue;
            }
        }
    }
}
