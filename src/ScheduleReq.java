import com.oocourse.elevator2.ScheRequest;

public class ScheduleReq {
    private ScheRequest scheRequest;
    private final int elevatorId;

    public ScheduleReq(int elevatorId) {
        this.scheRequest = null;
        this.elevatorId = elevatorId;
    }

    public synchronized void setScheRequest(ScheRequest scheRequest) {
        this.scheRequest = scheRequest;
        notifyAll();
    }

    public int getElevatorId() {
        return elevatorId;
    }

    public synchronized ScheRequest getScheRequest() {
        ScheRequest temp = scheRequest;
        scheRequest = null;
        notifyAll();
        return temp;
    }   
}
