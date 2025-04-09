import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;

import com.oocourse.elevator3.ScheRequest;

public class ElevatorStorage {
    private static ElevatorStorage instance = new ElevatorStorage(); // 单例实例
    private HashMap<Integer, ShadowElevator> shadowElevators; // 保存影子电梯状态

    private ElevatorStorage() { 
        shadowElevators = new HashMap<>();
    }

    public static ElevatorStorage getInstance() { 
        return instance;
    }
    
    public synchronized void updateShadow(int elevatorId, int currentFloor,
        int direction, int personsIn, ScheRequest scheRequest) {
        ShadowElevator shadow = shadowElevators.get(elevatorId);
        if (shadow == null) {
            shadow = new ShadowElevator(elevatorId, currentFloor, 
                direction, personsIn, scheRequest);
            shadowElevators.put(elevatorId, shadow);
        } else {
            shadow.update(currentFloor, direction, personsIn, scheRequest);
        }
    }
    
    public synchronized ShadowElevator getShadow(int elevatorId) {
        return shadowElevators.get(elevatorId);
    }
    
    public synchronized Collection<ShadowElevator> getAllShadows() {
        return new ArrayList<>(shadowElevators.values());
    }
}