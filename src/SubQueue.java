import java.util.ArrayList;
import java.util.Iterator;

public class SubQueue {

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
                notifyAll();
                return highestPriorityPerson;
            }
            notifyAll();
            return null;
        }
        
    }
}
