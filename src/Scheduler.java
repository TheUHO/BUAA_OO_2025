import java.util.HashMap;

public class Scheduler extends Thread {
    private final RequestQueue mainQueue;
    private final HashMap<Integer, RequestQueue> subQueues;
    private final HashMap<Integer, Elevator> elevators;

    public Scheduler(RequestQueue mainQueue, HashMap<Integer, RequestQueue> subQueues,
        HashMap<Integer, Elevator> elevators) {
        this.mainQueue = mainQueue;
        this.subQueues = subQueues;
        this.elevators = elevators;
    }

    public Integer bestElevator(Person person) {
        return person.getElevatorId();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (mainQueue) {
                if (mainQueue.isEmpty() && mainQueue.isEnd()) {
                    for (RequestQueue subQueue : subQueues.values()) {
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
