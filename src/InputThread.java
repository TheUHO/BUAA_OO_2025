import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;
import java.util.HashMap;

public class InputThread extends Thread {
    private final MainQueue mainQueue;
    private final HashMap<Integer, ScheduleReq> scheduleReqs;

    public InputThread(MainQueue mainQueue, HashMap<Integer, ScheduleReq> scheduleReqs) {
        this.mainQueue = mainQueue;
        this.scheduleReqs = scheduleReqs;
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
                ScheduleReq scheduleReq = scheduleReqs.get(elevatorId);
                scheduleReq.setScheRequest(scheRequest);
                mainQueue.addScheRequestCount();
            }
        }
    }
}
