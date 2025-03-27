import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

public class InputThread extends Thread {
    private final RequestQueue mainQueue;

    public InputThread(RequestQueue mainQueue) {
        this.mainQueue = mainQueue;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            PersonRequest request = (PersonRequest) elevatorInput.nextRequest();
            if (request == null) {
                mainQueue.setEnd();
                break;
            } else {
                Person person = new Person(request);
                mainQueue.addPersonRequest(person);
            }
        }
    }
}
