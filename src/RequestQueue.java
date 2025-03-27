import java.util.ArrayList;
import java.util.Iterator;

public class RequestQueue {

    private ArrayList<Person> persons = new ArrayList<>();
    private boolean end = false;

    public synchronized ArrayList<Person> getPersons() {
        notifyAll();
        return persons;
    }

    public synchronized void addPersonRequest(Person person) {
        persons.add(person);
        notifyAll();
    }

    public synchronized Person getPersonRequest() { // 分配任务
        if (persons.isEmpty() && !this.end) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (persons.isEmpty()) {
            return null;
        }
        Person person = persons.get(0);
        persons.remove(0);
        notifyAll();    
        return person;
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

    public synchronized Person getPersonIn(int floor, int direction) { // 获取某层某方向的乘客
        if (persons.isEmpty()) {
            notifyAll();
            return null;
        } else {
            Iterator<Person> iterator = persons.iterator();
            while (iterator.hasNext()) {
                Person p = iterator.next();
                if (p.getFromInt() == floor && p.getDirection() == direction) {
                    iterator.remove();
                    notifyAll();
                    return p;
                }
            }
            notifyAll();
            return null;
        }
        
    }
}
