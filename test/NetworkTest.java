import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.PersonInterface;

import java.util.Collection;
import java.util.Random;

public class NetworkTest {
    private Network network;
    private final Random rnd = new Random();
    private int nextNameId = 0;

    @Before
    public void setup() {
        network = new Network();
    }

    @Test
    public void queryTripleSumTest() throws Exception {
        final int Steps = 500;
        network.addPerson(new Person(0, "0", 10));
        network.addPerson(new Person(1, "1", 10));
        network.addPerson(new Person(2, "2", 10));
        network.addPerson(new Person(3, "3", 10));
        network.addPerson(new Person(4, "4", 10));
        network.addPerson(new Person(5, "5", 10));
        network.addRelation(0, 1, 50);
        network.addRelation(0, 2, 50);
        network.addRelation(2, 1, 50);
        network.addRelation(1, 3, 50);
        network.addRelation(2, 4, 50);
        network.addRelation(5, 4, 50);
        network.addRelation(5, 3, 50);
        assertEquals(1, network.queryTripleSum());
        network.addRelation(4, 3, 50);
        assertEquals(2, network.queryTripleSum());
        for (int step = 0; step < Steps; step++) {
            if (step % 3 == 0) {
                addPerson();
            } else if (step % 3 == 1) {
                addRelation();
            } else {
                modifyRelation();
            }
            /*@ pure @*/
            PersonInterface[] before = network.getPersons();
            // ensures
            int tripleSumCount = countTripleSum();
            int got1 = network.queryTripleSum();
            int got2 = network.queryTripleSum();
            assertEquals(tripleSumCount, got1);
            assertEquals(tripleSumCount, got2);

            PersonInterface[] after = network.getPersons();
            assertEquals(before.length, after.length);
            for (PersonInterface p0 : before) {
                boolean found = false;
                for (PersonInterface p1 : after) {
                    if (((Person)p0).strictEquals(p1)) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found);
            }
            int n = before.length;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    assertEquals(before[i].isLinked(before[j]), before[j].isLinked(before[i]));
                    assertEquals(before[i].isLinked(before[j]), after[i].isLinked(after[j]));
                    if (before[i].isLinked(before[j])) {
                        assertEquals(before[i].queryValue(before[j]), before[j].queryValue(before[i]));
                        assertEquals(before[i].queryValue(before[j]), after[i].queryValue(after[j]));
                    }
                }
            }
        }
    }

    // 暴力统计当前网络中的三元组数：i<j<k 且三者两两直接相连
    private int countTripleSum() {
        PersonInterface[] persons = network.getPersons();
        int n = persons.length;
        int cnt = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!persons[i].isLinked(persons[j])) { continue; }
                for (int k = j + 1; k < n; k++) {
                    if (getPerson(persons[i].getId()).isLinked(getPerson(persons[j].getId()))
                        && getPerson(persons[j].getId()).isLinked(getPerson(persons[k].getId()))
                        && getPerson(persons[k].getId()).isLinked(getPerson(persons[i].getId()))) {
                        cnt++;
                    }
                }
            }
        }
        return cnt;
    }

    private PersonInterface getPerson(int id) {
        return network.getPerson(id);
    }

    private void addPerson() {
        int id = genId();
        String name = String.valueOf(nextNameId);
        nextNameId++;
        int age = rnd.nextInt(100) + 1;
        Person p = new Person(id, name, age);
        try {
            network.addPerson(p);
        } catch (EqualPersonIdException ignored) {
            // do nothing
        }
    }

    private void addRelation() {
        int id1 = genId();
        int id2 = genId();
        int v = rnd.nextInt(10) + 1;
        try {
            network.addRelation(id1, id2, v);
        } catch (PersonIdNotFoundException | EqualRelationException ignored) {
            // do nothing
        }
    }

    private void modifyRelation() {
        int id1 = genId();
        int id2 = genId();
        int delta = rnd.nextInt(101) - 50;
        try {
            network.modifyRelation(id1, id2, delta);
        } catch (PersonIdNotFoundException | RelationNotFoundException 
            | EqualPersonIdException ignored) {
            // do nothing
        }
    }

    private void queryTripleSum() {
        try {
            network.queryTripleSum();
        } catch (Exception ignored) {
            // do nothing
        }
    }

    private int genId() {
        double p = rnd.nextDouble();
        if (p < 0.6) {
            return rnd.nextInt(5) + 1;
        } else if (p < 0.8) {
            return rnd.nextInt(10) + 1; 
        } else {
            return rnd.nextInt(100) - 50;
        }
    }
}
