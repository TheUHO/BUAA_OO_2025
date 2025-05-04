import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.main.PersonInterface;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

@RunWith(Parameterized.class)
public class QueryCoupleSumTest {
    private final Network oldNetwork;
    private final Network testNetwork;

    public QueryCoupleSumTest(Network oldNetwork, Network testNetwork) {
        this.oldNetwork = oldNetwork;
        this.testNetwork = testNetwork;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
        final int steps = 100;
        List<Object[]> cases = new ArrayList<>();
        cases.add(buildPointNetwork());
        cases.add(buildLinearNetwork());
        cases.add(buildFullNetwork());
        // add several random instances
        for (int i = 0; i < steps; i++) {
            cases.add(buildRndNetwork());
        }
        return cases;
    }

    private static Object[] buildPointNetwork() throws Exception {
        Network oldNetwork = new Network();
        Random rnd = new Random();
        final int n = 20;
        for (int i = 1; i <= n; i++) {
            Person p = new Person(i, String.valueOf(i), rnd.nextInt(100) + 1);
            try {
                oldNetwork.addPerson(p);
            } catch (EqualPersonIdException ignored) {
                // do nothing
            }
        }
        return new Object[]{ oldNetwork, deepCopyNetwork(oldNetwork) };
    }

    private static Object[] buildLinearNetwork() throws Exception {
        Network oldNetwork = new Network();
        Random rnd = new Random();
        final int n = 20;
        for (int i = 1; i <= n; i++) {
            Person p = new Person(i, String.valueOf(i), rnd.nextInt(100) + 1);
            try {
                oldNetwork.addPerson(p);
            } catch (EqualPersonIdException ignored) {
                // do nothing
            }
        }
        for (int i = 1; i < n; i++) {
            oldNetwork.addRelation(i, i + 1, rnd.nextInt(100) + 1);
        }
        return new Object[]{ oldNetwork, deepCopyNetwork(oldNetwork) };
    }

    private static Object[] buildFullNetwork() throws Exception {
        Network oldNetwork = new Network();
        Random rnd = new Random();
        final int n = 20;
        // add persons
        for (int i = 1; i <= n; i++) {
            Person p = new Person(i, String.valueOf(i), rnd.nextInt(100) + 1);
            try {
                oldNetwork.addPerson(p);
            } catch (EqualPersonIdException ignored) {
                // do nothing
            }
        }
        // add relations
        for (int i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                oldNetwork.addRelation(i, j, 100);
            }
        }
        return new Object[]{ oldNetwork, deepCopyNetwork(oldNetwork) };
    }

    private static Object[] buildRndNetwork() throws Exception {
        Network oldNetwork = new Network();
        final int steps = 100;
        for (int step = 0; step < steps; step++) {
            if (step % 3 == 0) {
                addPerson(oldNetwork);
            } else if (step % 3 == 1) {
                addRelation(oldNetwork);
            } else {
                modifyRelation(oldNetwork);
            }
        }
        return new Object[]{ oldNetwork, deepCopyNetwork(oldNetwork) };
    }

    @Test
    public void checkQueryCoupleSum() {
        // check equal
        int totalSum = 0;
        Network countNetwork = deepCopyNetwork(oldNetwork);
        PersonInterface[] persons = countNetwork.getPersons();
        for (PersonInterface pi : persons) {
            for (PersonInterface pj : persons) {
                if (pi.getId() < pj.getId()) {
                    try {
                        int bi = countNetwork.queryBestAcquaintance(pi.getId());
                        int bj = countNetwork.queryBestAcquaintance(pj.getId());
                        if (bi == pj.getId() && bj == pi.getId()) {
                            totalSum++;
                        }
                    } catch (Exception e) {
                        // do nothing
                    }
                }
            }
        }
        int got1 = testNetwork.queryCoupleSum();
        Network testNetwork2 = deepCopyNetwork(testNetwork);
        int got2 = testNetwork2.queryCoupleSum();
        assertEquals(got1, got2);
        assertEquals(totalSum, got1);
        // pure
        PersonInterface[] before = oldNetwork.getPersons();
        PersonInterface[] after = testNetwork.getPersons();
        assertEquals(before.length, after.length);
        int n = before.length;
        for (int i = 0; i < n; i++) {
            assertTrue(((Person) before[i]).strictEquals(after[i]));
        }
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

    private static void addPerson(Network network) {
        Random rnd = new Random();
        int id = genId();
        String name = String.valueOf(id);
        int age = rnd.nextInt(100) + 1;
        Person p = new Person(id, name, age);
        try {
            network.addPerson(p);
        } catch (EqualPersonIdException ignored) {
            // do nothing
        }
    }

    private static void addRelation(Network network) {
        Random rnd = new Random();
        int id1 = genId();
        int id2 = genId();
        int v = rnd.nextInt(10) + 1;
        try {
            network.addRelation(id1, id2, v);
        } catch (PersonIdNotFoundException | EqualRelationException ignored) {
            // do nothing
        }
    }

    private static void modifyRelation(Network network) {
        Random rnd = new Random();
        int id1 = genId();
        int id2 = genId();
        int delta = rnd.nextInt(20) - 10;
        try {
            network.modifyRelation(id1, id2, delta);
        } catch (PersonIdNotFoundException | RelationNotFoundException 
            | EqualPersonIdException ignored) {
            // do nothing
        }
    }

    private static int genId() {
        Random rnd = new Random();
        double p = rnd.nextDouble();
        if (p < 0.6) {
            return rnd.nextInt(10) + 1;
        } else if (p < 0.8) {
            return rnd.nextInt(20) + 1; 
        } else {
            return rnd.nextInt(100) - 50;
        }
    }

    private static Network deepCopyNetwork(Network original) {
        PersonInterface[] originals = original.getPersons();
        Network clonedNetwork = new Network();
        // 复制persons
        for (PersonInterface person : originals) {
            Person p = (Person) person;
            try {
                clonedNetwork.addPerson(new Person(p.getId(), p.getName(), p.getAge()));
            } catch (EqualPersonIdException ignored) {
                // do nothing
            }
        }
        // 复制关系
        int n = originals.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                PersonInterface pi = originals[i];
                PersonInterface pj = originals[j];
                if (pi.isLinked(pj)) {
                    int weight = pi.queryValue(pj);
                    try {
                        clonedNetwork.addRelation(pi.getId(), pj.getId(), weight);
                    } catch (PersonIdNotFoundException | EqualRelationException ignored) {
                        // do nothing
                    }
                }
            }
        }
        return clonedNetwork;
    }
}
