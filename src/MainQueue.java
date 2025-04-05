import java.util.ArrayList;

public class MainQueue {

    private ArrayList<Person> persons = new ArrayList<>();
    private int passengerCount = 0;
    private int scheRequestCount = 0;
    private boolean inputEnd = false;

    public synchronized ArrayList<Person> getPersons() {
        notifyAll();
        return persons;
    }

    public synchronized void addPersonRequest(Person person) {
        persons.add(person);
        notifyAll();
    }

    public synchronized Person getPersonRequest() { // 分配任务
        while (persons.isEmpty() && !this.inputEnd) {
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

    public synchronized boolean isInputEnd() {
        return inputEnd;
    }

    public synchronized void setInputEnd() {
        inputEnd = true;
        notifyAll();
    }

    public synchronized boolean isAllEnd() {
        return inputEnd && passengerCount == 0 && scheRequestCount == 0; // 所有任务都完成
    }

    public synchronized void addPassengerCount() {
        passengerCount++;
        notifyAll();
    }

    public synchronized int getPassengerCount() {
        return passengerCount;
    }
    
    public synchronized void subPassengerCount() {
        passengerCount--;
        notifyAll();
    }

    public synchronized void addScheRequestCount() {
        scheRequestCount++;
        notifyAll();
    }

    public synchronized int getScheRequestCount() {
        return scheRequestCount;
    }

    public synchronized void subScheRequestCount() {
        scheRequestCount--;
        notifyAll();
    }
}
