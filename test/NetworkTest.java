import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
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
        final int Steps = 1000;
        for (int step = 0; step < Steps; step++) {
            double p = rnd.nextDouble();
            if (p < 0.2) {
                addPerson();
            } else if (p < 0.5) {
                addRelation();
            } else if (p < 0.8) {
                modifyRelation();
            } else {
                queryTripleSum();
            }
            /*@ pure @*/
            Person[] before = network.getPersons();
            // ensures 
            int tripleSumCount = countTripleSum();
            int got1 = network.queryTripleSum();
            int got2 = network.queryTripleSum();
            assertEquals(tripleSumCount, got1);
            assertEquals(tripleSumCount, got2);

            Person[] after = network.getPersons();
            assertEquals(before.length, after.length);
            for (Person p0 : before) {
                boolean found = false;
                for (Person p1 : after) {
                    if (((Person)p0).strictEquals(p1)) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found);
            }
        }
    }

    // 暴力统计当前网络中的三元组数：i<j<k 且三者两两直接相连
    private int countTripleSum() {
        Person[] ps = network.getPersons();
        int n = ps.length;
        int cnt = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!ps[i].isLinked(ps[j])) { continue; }
                for (int k = j + 1; k < n; k++) {
                    if (ps[i].isLinked(ps[k]) && ps[j].isLinked(ps[k])) {
                        cnt++;
                    }
                }
            }
        }
        return cnt;
    }

    private void addPerson() {
        int id = genId();
        String name = String.valueOf(nextNameId);
        nextNameId++;
        int age = rnd.nextInt(100) + 1;
        Person p = new Person(id, name, age);
        try {
            network.addPerson(p);
        } catch (EqualPersonIdException ignored) {}
    }

    private void addRelation() {
        int id1 = genId();
        int id2 = genId();
        int v = rnd.nextInt(10) + 1;
        try {
            network.addRelation(id1, id2, v);
        } catch (PersonIdNotFoundException | EqualRelationException ignored) {}
    }

    private void modifyRelation() {
        int id1 = genId();
        int id2 = genId();
        int delta = rnd.nextInt(101) - 50;
        try {
            network.modifyRelation(id1, id2, delta);
        } catch (PersonIdNotFoundException | RelationNotFoundException | EqualPersonIdException ignored) {}
    }

    private void queryTripleSum() {
        try {
            network.queryTripleSum();
        } catch (Exception ignored) {}
    }

    private int genId() {
        double p = rnd.nextDouble();
        if (p < 0.2) {
            return rnd.nextInt(5) + 1;
        } else if (p < 0.7) {
            return rnd.nextInt(10) + 1; 
        } else {
            return rnd.nextInt(100) - 50;
        }
    }
}
