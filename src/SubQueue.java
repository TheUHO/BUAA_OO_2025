import java.util.ArrayList;
import java.util.Iterator;

import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.UpdateRequest;

public class SubQueue {

    private ArrayList<Person> persons = new ArrayList<>();
    private boolean end = false;
    private ScheRequest scheRequest = null;
    private UpdateRequest updateRequest = null;

    public synchronized ArrayList<Person> getPersons() {
        // notifyAll();
        return persons;
    }

    public synchronized void addPersonRequest(Person person) {
        persons.add(person);
        notifyAll();
    }

    public synchronized boolean isEmpty() {
        notifyAll();
        return persons.isEmpty();
    }

    public synchronized boolean isEnd() {
        return end;
    }

    public synchronized void setEnd() {
        end = true;
        notifyAll();
    }

    public synchronized void setScheRequest(ScheRequest scheRequest) {
        this.scheRequest = scheRequest;
        notifyAll();
    }

    public synchronized ScheRequest getScheRequest() {
        // notifyAll();
        return scheRequest;
    }

    public synchronized boolean hasPersonRequest() {
        return !persons.isEmpty(); // 判断是否存在乘客请求
    }

    public synchronized boolean hasScheRequest() { // 判断是否存在临时调度请求
        return scheRequest != null;
    }

    public synchronized void setUpdateRequest(UpdateRequest updateRequest) {
        this.updateRequest = updateRequest;
        notifyAll();
    }

    public synchronized UpdateRequest getUpdateRequest() {
        // notifyAll();
        return updateRequest;
    }

    public synchronized boolean hasUpdateRequest() { // 判断是否存在更新请求
        return updateRequest != null;
    }

    public synchronized Person getPersonIn(int floor, int direction) { // 获取某层某方向的乘客
        if (persons.isEmpty()) {
            // notifyAll();
            return null;
        } else {
            Person highestPriorityPerson = null;
            Iterator<Person> iterator = persons.iterator();
            while (iterator.hasNext()) {
                Person p = iterator.next();
                if (p.getFromInt() == floor && p.getDirection() == direction) {
                    if (highestPriorityPerson == null ||
                        p.getPriority() > highestPriorityPerson.getPriority()) {
                        highestPriorityPerson = p;
                    }
                }
            }
            if (highestPriorityPerson != null) {
                persons.remove(highestPriorityPerson);
                // notifyAll();
                return highestPriorityPerson;
            }
            // notifyAll();
            return null;
        }    
    }

    public synchronized ArrayList<Person> getMatchingCandidates(int currentFloor, 
        int direction, int targetFloor, int capacity) { // 返回符合条件候选乘客列表
        ArrayList<Person> candidates = new ArrayList<>(); // 用于存放候选乘客
        Iterator<Person> iterator = persons.iterator(); // 获取队列迭代器
        while (iterator.hasNext() && candidates.size() < capacity) { // 遍历且未达容量
            Person p = iterator.next(); // 获取候选乘客
            if (p.getFromInt() == currentFloor && p.getDirection() == direction) { // 判断当前层和方向
                int extra = p.getToInt() - targetFloor; // 计算目标差值
                if (direction != 0 && direction * extra >= 0) { // 条件满足：目标楼层在调度目标“更远”
                    candidates.add(p); // 添加到候选列表
                    iterator.remove(); // 从等待队列中移除该乘客
                }
            }
        }
        return candidates; // 返回候选乘客列表
    }
    
    public synchronized Person getPrimaryRequest() {
        if (persons.isEmpty()) {
            return null;
        }
        Person selected = null;
        double maxScore = Double.NEGATIVE_INFINITY;
        long currentTime = System.currentTimeMillis();
        for (Person p : persons) {
            double waitingSeconds = (currentTime - p.getTimeStamp()) / 1000.0;
            double compositeScore = p.getPriority() * waitingSeconds;
            if (compositeScore > maxScore) {
                maxScore = compositeScore;
                selected = p;
            }
        }
        return selected;
    }

    public synchronized Person getUnableRequest(boolean isA, boolean isB, int tfFloor) {
        if (persons.isEmpty()) {
            return null;
        }
        Person selected = null;
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            boolean condition1 = isA && p.getFromInt() < tfFloor;
            boolean condition2 = isB && p.getFromInt() > tfFloor;
            boolean condition3 = isA && p.getFromInt() == tfFloor && p.getToInt() < tfFloor;
            boolean condition4 = isB && p.getFromInt() == tfFloor && p.getToInt() > tfFloor;
            if (condition1 || condition2 || condition3 || condition4) {
                selected = p;
                iterator.remove();
                break;
            }
        }
        return selected;
    }

    public synchronized Person getAndCleanRequest() {
        if (persons.isEmpty()) {
            return null;
        }
        Person selected = null;
        Iterator<Person> iterator = persons.iterator();
        while (iterator.hasNext()) {
            Person p = iterator.next();
            selected = p;
            iterator.remove(); // 从等待队列中移除该乘客
            break; // 只取第一个乘客
        }
        return selected;
    }
}

