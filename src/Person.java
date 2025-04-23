import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

public class Person implements PersonInterface {
    private int id;
    private String name;
    private int age;
    private final HashMap<Integer, Person> acquaintance;
    private final HashMap<Integer, Integer> value;
    private final HashMap<Integer, Tag> tags;
    private final PriorityQueue<Integer> priorityQueue;

    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        acquaintance = new HashMap<>();
        value = new HashMap<>();
        tags = new HashMap<>();
        priorityQueue = new PriorityQueue<>(
            Comparator.<Integer>comparingInt(personId -> this.value.get(personId))
                      .reversed()
                      .thenComparingInt(personId -> personId)
        );
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public boolean containsTag(int id) {
        return tags.containsKey(id);
    }

    @Override
    public TagInterface getTag(int id) {
        if (tags.containsKey(id)) {
            return (TagInterface)tags.get(id);
        } else {
            return null;
        }
    }

    @Override
    public void addTag(TagInterface tag) {
        Tag t = (Tag) tag;
        if (!tags.containsKey(t.getId())) {
            tags.put(t.getId(), t);
        }
    }

    @Override
    public void delTag(int id) {
        if (tags.containsKey(id)) {
            tags.remove(id);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Person) {
            return ((Person) obj).getId() == id;
        } else {
            return false;
        }
    }

    @Override
    public boolean isLinked(PersonInterface person) {
        Person p = (Person) person;
        return acquaintance.containsKey(p.getId()) || p.getId() == id;
    }

    @Override
    public int queryValue(PersonInterface person) {
        int personId = ((Person)person).getId();
        if (acquaintance.containsKey(personId)) {
            return value.get(personId);
        } else {
            return 0;
        }
    }

    // <-------自主实现函数------->

    public HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    // 添加与person的关系
    public void addRelation(Person person, int value) {
        int personId = person.getId();
        acquaintance.put(personId, person);
        this.value.put(personId, value);
        priorityQueue.add(personId);
    }

    public void modifyRelation(Person person, int newValue) {
        int personId = person.getId();
        this.value.replace(personId, newValue);
        priorityQueue.remove(personId);
        priorityQueue.add(personId);
    }

    public void deleteRelation(Person person) {
        int personId = person.getId();
        acquaintance.remove(personId);
        this.value.remove(personId);
        priorityQueue.remove(personId);
        for (Tag tag : tags.values()) { // 删除与该person相关联的tag
            tag.delPerson(person); // 方法内判断是否有这个人
        }
    }

    public boolean isAcquaintanceEmpty() {
        return acquaintance.isEmpty(); // 判断是否为空  
    }

    public int queryBestAcquaintance() {
        return priorityQueue.peek(); // 返回优先级最高的acquaintance的id
    }

    public boolean strictEquals(Person person) {
        return id == person.getId() &&
               name.equals(person.getName()) &&
               age == person.getAge() &&
               acquaintance.equals(person.getAcquaintance()) &&
               value.equals(person.value) &&
               tags.equals(person.tags);
    }
}
