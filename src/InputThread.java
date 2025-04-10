import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;

import java.util.HashMap;

public class InputThread extends Thread {
    private final MainQueue mainQueue;
    private final HashMap<Integer, SubQueue> subQueues;
    private final HashMap<Integer, Elevator> elevators;

    public InputThread(MainQueue mainQueue, HashMap<Integer, SubQueue> subQueues, 
        HashMap<Integer, Elevator> elevators) {
        this.mainQueue = mainQueue;
        this.subQueues = subQueues;
        this.elevators = elevators;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) { 
                mainQueue.setInputEnd();
                break;
            } else if (request instanceof PersonRequest) {
                PersonRequest personRequest = (PersonRequest) request;
                Person person = new Person(personRequest);
                mainQueue.addPersonRequest(person);
                mainQueue.addPassengerCount();
            } else if (request instanceof ScheRequest) {
                ScheRequest scheRequest = (ScheRequest) request;
                int elevatorId = scheRequest.getElevatorId();
                SubQueue subQueue = subQueues.get(elevatorId);
                subQueue.setScheRequest(scheRequest);
                mainQueue.addScheRequestCount();
            } else if (request instanceof UpdateRequest) {
                UpdateRequest updateRequest = (UpdateRequest) request;
                int elevatorAId = updateRequest.getElevatorAId();
                int elevatorBId = updateRequest.getElevatorBId();
                Coordinator coordinator = new Coordinator(elevatorAId, elevatorBId);
                Elevator elevatorA = elevators.get(elevatorAId);
                Elevator elevatorB = elevators.get(elevatorBId);
                elevatorA.setCoordinator(coordinator);
                elevatorB.setCoordinator(coordinator);
                SubQueue subQueueA = subQueues.get(elevatorAId);
                SubQueue subQueueB = subQueues.get(elevatorBId);
                subQueueA.setUpdateRequest(updateRequest);
                subQueueB.setUpdateRequest(updateRequest);
            }
        }
    }
}
