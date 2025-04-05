import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

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
        ArrayList<Integer> bestElevatorIds = new ArrayList<>();
        for (ShadowElevator shadow : shadows) {
            int perf = shadow.getEstimatePerformance(person);
            // System.out.println("Person: " + person.getPersonId() + " for elevator " +
            //     shadow.getElevatorId() + " " + perf);
            if (perf < bestPerformance) {
                bestPerformance = perf;
                bestElevatorIds.clear();
                bestElevatorIds.add(shadow.getElevatorId());
            } else if (perf == bestPerformance) {
                bestElevatorIds.add(shadow.getElevatorId());
            }
        }
        if (!bestElevatorIds.isEmpty()) {
            Random rand = new Random();
            int index = rand.nextInt(bestElevatorIds.size());
            return bestElevatorIds.get(index);
        }
        return -1;
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
            } else {
                continue;
            }
        }
    }
}
