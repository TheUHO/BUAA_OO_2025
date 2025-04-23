import java.util.HashMap;

import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

public class Tag implements TagInterface {
    private int id;
    private final HashMap<Integer, Person> persons;
    private int ageSumCache = 0;
    private int ageVarSumCache = 0;

    public Tag(int id) {
        this.id = id;
        persons = new HashMap<>();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Tag) {
            return id == ((Tag) obj).getId();
        } else {
            return false;
        }
    }

    @Override
    public void addPerson(PersonInterface person) {
        Person p = (Person) person;
        persons.put(p.getId(), p);
        int age = p.getAge();
        ageSumCache += age;
        ageVarSumCache += (age * age);
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        return persons.containsKey(person.getId());
    }

    @Override
    public int getAgeMean() {
        return  persons.size() == 0 ? 0 : (ageSumCache / persons.size());
    }

    @Override
    public int getAgeVar() {
        int n = persons.size();
        if (n == 0) {
            return 0;
        } else {
            int mean = getAgeMean();
            // \sum x_i^2 - 2 * ageSum * mean + n * mean^2
            return (ageVarSumCache - 2 * ageSumCache * mean + n * mean * mean) / n;
        }
    }

    @Override
    public void delPerson(PersonInterface person) {
        Person p = (Person) person;
        if (!persons.containsKey(p.getId())) { // 如果没有这个人
            return;
        }
        persons.remove(p.getId());
        int age = p.getAge();
        ageSumCache -= age;
        ageVarSumCache -= (age * age);
    }

    @Override
    public int getSize() {
        return persons.size();
    }
}
