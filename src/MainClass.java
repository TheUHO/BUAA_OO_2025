import com.oocourse.elevator1.TimableOutput;
import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        RequestQueue mainQueue = new RequestQueue();
        HashMap<Integer, RequestQueue> subQueues = new HashMap<>();
        HashMap<Integer, Elevator> elevators = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            RequestQueue subQueue = new RequestQueue();
            subQueues.put(i, subQueue);
            Elevator elevator = new Elevator(i, subQueue);
            elevators.put(i, elevator);
            elevator.start();
        }
        Scheduler scheduler = new Scheduler(mainQueue, subQueues, elevators);
        scheduler.start();
        InputThread inputThread = new InputThread(mainQueue);
        inputThread.start();
    }
}
