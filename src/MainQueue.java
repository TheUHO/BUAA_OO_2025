import java.util.ArrayList;

public class MainQueue {

    private ArrayList<Person> persons = new ArrayList<>();
    private int passengerCount = 0;
    private int scheRequestCount = 0;
    private int updateCount = 0;
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
        while (persons.isEmpty() && !this.isAllEnd()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.isAllEnd()) {
            return null;
        }
        Person person = persons.get(0);
        persons.remove(0);   
        return person;
    }

    public synchronized boolean isInputEnd() {
        return inputEnd;
    }

    public synchronized void setInputEnd() {
        inputEnd = true;
        notifyAll();
    }

    public synchronized boolean isAllEnd() { // 所有任务都完成
        return inputEnd && passengerCount == 0 && scheRequestCount == 0 && updateCount == 0;
    }

    public synchronized void addPassengerCount() {
        passengerCount++;
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
    }

    public synchronized int getScheRequestCount() {
        return scheRequestCount;
    }

    public synchronized void subScheRequestCount() {
        scheRequestCount--;
        notifyAll();
    }

    public synchronized void addUpdateCount() {
        updateCount++;
    }

    public synchronized int getUpdateCount() {
        return updateCount;
    }

    public synchronized void subUpdateCount() {
        updateCount--;
        notifyAll();
    }
}
