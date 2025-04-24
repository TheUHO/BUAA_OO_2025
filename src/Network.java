import java.util.HashMap;

import com.oocourse.spec1.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.EqualTagIdException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.exceptions.TagIdNotFoundException;
import com.oocourse.spec1.main.NetworkInterface;
import com.oocourse.spec1.main.PersonInterface;
import com.oocourse.spec1.main.TagInterface;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons;
    private final Graph graph; // 并查集
    private int tripleSum = 0;

    public Network() {
        persons = new HashMap<>();
        graph = new Graph();
        tripleSum = 0;
    }

    @Override
    public boolean containsPerson(int id) {
        return persons.containsKey(id);
    }

    @Override
    public PersonInterface getPerson(int id) {
        if (persons.containsKey(id)) {
            return persons.get(id);
        } else {
            return null;
        }
    }

    @Override
    public void addPerson(PersonInterface person)  throws EqualPersonIdException {
        int id = person.getId();
        if (!containsPerson(id)) {
            persons.put(id, (Person) person);
            graph.addPerson(id); // 加入并查集
        } else {
            throw new EqualPersonIdException(id);
        }
    }

    @Override
    public void addRelation(int id1, int id2, int value) throws
        PersonIdNotFoundException, EqualRelationException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (getPerson(id1).isLinked(getPerson(id2))) {
            throw new EqualRelationException(id1, id2);
        } else {
            Person person1 = (Person) getPerson(id1);
            Person person2 = (Person) getPerson(id2);
            person1.addRelation(person2, value);
            person2.addRelation(person1, value);
            graph.addRelation(id1, id2); // 加入并查集
            tripleSum += getSharedRelation(id1, id2);
        }
    }

    @Override
    public void modifyRelation(int id1, int id2, int value) throws PersonIdNotFoundException,
        EqualPersonIdException, RelationNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new EqualPersonIdException(id1);
        } else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        } else {
            Person person1 = (Person) getPerson(id1);
            Person person2 = (Person) getPerson(id2);
            int nextValue = person1.queryValue(person2) + value;
            if (nextValue > 0) {
                person1.modifyRelation(person2, nextValue);
                person2.modifyRelation(person1, nextValue);
            } else {
                person1.deleteRelation(person2);
                person2.deleteRelation(person1);
                graph.deleteRelation(id1, id2); // 删除并查集
                tripleSum -= getSharedRelation(id1, id2);
            }
        }
    }

    @Override
    public int queryValue(int id1, int id2) throws
        PersonIdNotFoundException, RelationNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new RelationNotFoundException(id1, id2);
        } else {
            return ((Person)getPerson(id1)).queryValue(getPerson(id2));
        }
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else {
            return graph.isCircle(id1, id2);
        }
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    @Override
    public void addTag(int personId, TagInterface tag) 
        throws PersonIdNotFoundException, EqualTagIdException {
        int id = ((Tag)tag).getId();
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (persons.get(personId).containsTag(id)) {
            throw new EqualTagIdException(id);
        }
        persons.get(personId).addTag(tag);
    }

    @Override
    public void addPersonToTag(int personId1, int personId2, int tagId) 
        throws PersonIdNotFoundException,  RelationNotFoundException, 
        TagIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        } else if (personId1 == personId2) {
            throw new EqualPersonIdException(personId1);
        } else if (!getPerson(personId1).isLinked(getPerson(personId2))) {
            throw new RelationNotFoundException(personId1, personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else if (persons.get(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new EqualPersonIdException(personId1);
        } else {
            TagInterface tag = persons.get(personId2).getTag(tagId);
            if (tag.getSize() <= 999) {
                tag.addPerson(getPerson(personId1));
            }
        }
    }

    @Override
    public int queryTagAgeVar(int personId, int tagId) 
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!persons.get(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else {
            return persons.get(personId).getTag(tagId).getAgeVar();
        }
    }

    @Override
    public void delPersonFromTag(int personId1, int personId2, int tagId) 
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId1)) {
            throw new PersonIdNotFoundException(personId1);
        } else if (!containsPerson(personId2)) {
            throw new PersonIdNotFoundException(personId2);
        } else if (!getPerson(personId2).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else if (!getPerson(personId2).getTag(tagId).hasPerson(getPerson(personId1))) {
            throw new PersonIdNotFoundException(personId1);
        } else {
            getPerson(personId2).getTag(tagId).delPerson(getPerson(personId1));
        }
    }

    @Override
    public void delTag(int personId, int tagId) 
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!getPerson(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else {
            persons.get(personId).delTag(tagId);
        }
    }

    @Override
    public int queryBestAcquaintance(int id) 
        throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else if (((Person)getPerson(id)).getAcquaintance().isEmpty()) {
            throw new AcquaintanceNotFoundException(id);
        } else {
            return ((Person)getPerson(id)).queryBestAcquaintance();
        }
    }

    // <------自主实现函数------->
    // 计算三元组数目
    private int getSharedRelation(int id1, int id2) {
        int sum = 0;
        Person person1 = (Person) getPerson(id1);
        Person person2 = (Person) getPerson(id2);
        HashMap<Integer, Person> minAcquaintance = 
            (person1.getAcquaintance().size() < person2.getAcquaintance().size()) 
            ? person1.getAcquaintance() : person2.getAcquaintance();
        Person maxPerson = 
            (minAcquaintance == person1.getAcquaintance()) ? person2 : person1;
        for (Person person : minAcquaintance.values()) {
            if (person.isLinked(maxPerson) && person.getId() != maxPerson.getId()) {
                sum++;
            }
        }
        return sum;
    }

    public PersonInterface[] getPersons() {
        return (PersonInterface[]) persons.values().toArray(new PersonInterface[0]);
    }
}
