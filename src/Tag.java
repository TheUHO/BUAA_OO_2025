import java.util.HashMap;

import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.TagInterface;

public class Tag implements TagInterface {
    private int id;
    private final HashMap<Integer, Person> persons;
    private int ageSumCache = 0;
    private int ageVarSumCache = 0;
    private int valueSumCache = 0; // 注意遍历时是双向的

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
        // 计算valueSumCache
        HashMap<Integer, Person> acquaintance = p.getAcquaintance();
        for (Person tempPerson : acquaintance.values()) {
            if (persons.containsKey(tempPerson.getId())) {
                valueSumCache += tempPerson.queryValue(p) * 2; // 双向的
            }
        }
    }

    @Override
    public boolean hasPerson(PersonInterface person) {
        return persons.containsKey(person.getId());
    }

    @Override
    public int getValueSum() {
        return valueSumCache;
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
        // 计算valueSumCache
        HashMap<Integer, Person> acquaintance = p.getAcquaintance();
        for (Person tempPerson : acquaintance.values()) {
            if (persons.containsKey(tempPerson.getId())) {
                valueSumCache -= tempPerson.queryValue(p) * 2; // 双向的
            }
        }
    }

    @Override
    public int getSize() {
        return persons.size();
    }

    // <------自主实现函数------->

    public void modifyValueSum(int id1, int id2, int value) { // 增加、减少、修改关系时调用
        if (persons.containsKey(id1) && persons.containsKey(id2)) {
            valueSumCache += value * 2; // 双向的
        }
    }

    public void addSocialValue(int num) {
        for (Person person : persons.values()) {
            person.addSocialValue(num);
        }
    }

    public void addMoney(int num) {
        for (Person person : persons.values()) {
            person.addMoney(num);
        }
    }

    public void receiveArticle(int id) {
        for (Person person : persons.values()) {
            person.receiveArticle(id);
        }
    }

    public void addMessage(MessageInterface message) {
        for (Person person : persons.values()) {
            person.addMessage(message);
        }
    }
}
