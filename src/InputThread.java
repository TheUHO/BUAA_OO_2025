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
                // TODO: 注意人数不为0且临时调度未结束不能结束线程
                mainQueue.setEnd();
                break;
            } else if (request instanceof PersonRequest) {
                PersonRequest personRequest = (PersonRequest) request;
                Person person = new Person(personRequest);
                mainQueue.addPersonRequest(person);
            } else if (request instanceof ScheRequest) {
                ScheRequest scheRequest = (ScheRequest) request;
                // TODO: 处理临时调度请求，直接传给电梯
                int elevatorId = scheRequest.getElevatorId();
                ScheduleReq scheduleReq = scheduleReqs.get(elevatorId);
                scheduleReq.setScheRequest(scheRequest);
            }
        }
    }
}
