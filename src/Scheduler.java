import java.util.HashMap;
import java.util.Collection;

public class Scheduler extends Thread {
    private final MainQueue mainQueue;
    private final HashMap<Integer, SubQueue> subQueues;
    private final HashMap<Integer, Elevator> elevators;

    public Scheduler(MainQueue mainQueue, HashMap<Integer, SubQueue> subQueues,
        HashMap<Integer, Elevator> elevators) {
        this.mainQueue = mainQueue;
        this.subQueues = subQueues;
        this.elevators = elevators;
    }

    public Integer bestElevator(Person person) {
        Collection<ShadowElevator> shadows = ElevatorStorage.getInstance().getAllShadows();
        int bestPerformance = Integer.MAX_VALUE;
        int bestElevatorId = -1;
        for (ShadowElevator shadow : shadows) {
            int perf = shadow.getEstimatePerformance(person);
            if (perf < bestPerformance) {
                bestPerformance = perf;
                bestElevatorId = shadow.getElevatorId();
            }
        }
        return bestElevatorId;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (mainQueue) {
                if (mainQueue.isEmpty() && mainQueue.isAllEnd()) {
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
