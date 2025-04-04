import java.util.ArrayList;

public class MainQueue {

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
        while (persons.isEmpty() && !this.end) {
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
}
