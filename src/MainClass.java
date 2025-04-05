import com.oocourse.elevator2.TimableOutput;
import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        MainQueue mainQueue = new MainQueue();
        HashMap<Integer, SubQueue> subQueues = new HashMap<>();
        HashMap<Integer, Elevator> elevators = new HashMap<>();
        HashMap<Integer, ScheduleReq> scheduleReqs = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            SubQueue subQueue = new SubQueue();
            subQueues.put(i, subQueue);
            ScheduleReq scheduleReq = new ScheduleReq(i);
            scheduleReqs.put(i, scheduleReq);
            Elevator elevator = new Elevator(i, mainQueue, subQueue, scheduleReq);
            elevators.put(i, elevator);
            elevator.start();
        }
        Scheduler scheduler = new Scheduler(mainQueue, subQueues, elevators);
        scheduler.start();
        InputThread inputThread = new InputThread(mainQueue, scheduleReqs);
        inputThread.start();
    }
}
