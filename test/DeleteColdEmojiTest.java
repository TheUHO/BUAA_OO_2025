import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.ForwardMessageInterface;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.RedEnvelopeMessageInterface;
import com.oocourse.spec3.main.TagInterface;

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
public class DeleteColdEmojiTest {
    private static final int TEST_COUNT = 200;      // 测试实例总数
    private static final int PERSON_COUNT = 10;         // 总人数
    private static final int PERSON_EXTRA_COUNT = 5;      // 随机增加人数
    private static final int EXTRA_STEPS = 10;          // 随机增删网络步数
    private static final int EMOJI_MESSAGE_COUNT = 100; // 固定的 Emoji 消息数
    private static final int RANDOM_MESSAGE_COUNT = 100;// 其余随机消息数，总共 100 条
    private static final int SEND_COUNT = 200;       // 总消息数
    private static final int LIMIT_BOUND = 10;          // limit 的上限

    private final Network oldNetwork;
    private final Network testNetwork;
    private final int limit;

    public DeleteColdEmojiTest(Network oldNetwork, Network testNetwork, int limit) {
        this.oldNetwork = oldNetwork;
        this.testNetwork = testNetwork;
        this.limit = limit;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws Exception {
        List<Object[]> cases = new ArrayList<>();
        for (int i = 0; i < TEST_COUNT; i++) {
            Object[] caseData = buildNetwork();
            generateMessages(caseData);
            sendMessage(caseData);
            cases.add(caseData);
        }
        return cases;
    }
    
    private static Object[] buildNetwork() throws Exception {
        Network oldNetwork = new Network();
        Network testNetwork = new Network();
        Random rnd = new Random();
        // add persons and relations
        for (int i = 1; i <= PERSON_COUNT; i++) {
            Person p = new Person(i, String.valueOf(i), rnd.nextInt(100) + 1);
            try {
                oldNetwork.addPerson(p);
                testNetwork.addPerson(p);
            } catch (Exception ignored) {
                // do nothing
            }
        }
        for (int i = 1; i <= PERSON_COUNT; i++) {
            for (int j = i + 1; j <= PERSON_COUNT; j++) {
                int relation = rnd.nextInt(50) + 50;
                try {
                    oldNetwork.addRelation(i, j, relation);
                    testNetwork.addRelation(i, j, relation);
                } catch (Exception ignored) {
                    // do nothing
                }
            }
        }
        // randomly add person and relations
        for (int i = PERSON_COUNT; i <= PERSON_EXTRA_COUNT + PERSON_COUNT; i++) {
            Person p = new Person(i, String.valueOf(i), rnd.nextInt(100) + 1);
            try {
                oldNetwork.addPerson(p);
                testNetwork.addPerson(p);
            } catch (Exception ignored) {
                // do nothing
            }
        }
        for (int i = 1; i <= EXTRA_STEPS; i++) {
            int op = rnd.nextInt(3);
            int p1 = rnd.nextInt(PERSON_EXTRA_COUNT) + PERSON_COUNT + 1;
            int p2 = rnd.nextInt(PERSON_EXTRA_COUNT + PERSON_COUNT) + 1;
            int p3 = rnd.nextInt(PERSON_EXTRA_COUNT + PERSON_COUNT) + 1;
            try {
                if (op == 0) {
                    int relation = rnd.nextInt(50) + 50;
                    oldNetwork.addRelation(p1, p2, relation);
                    testNetwork.addRelation(p1, p2, relation);
                } else if (op == 1) {
                    int offset = rnd.nextInt(100);
                    oldNetwork.modifyRelation(p2, p3, offset);
                    testNetwork.modifyRelation(p2, p3, offset);
                } else {
                    int offset = rnd.nextInt(100);
                    oldNetwork.modifyRelation(p1, p2, offset);
                    testNetwork.modifyRelation(p1, p2, offset);
                }
            } catch (Exception ignored) {
                // do nothing
            }
        }
        return new Object[]{ oldNetwork, testNetwork, rnd.nextInt(LIMIT_BOUND)};
    }

    private static void generateMessages(Object[] caseData) {
        Network oldNetwork = (Network) caseData[0];
        Network testNetwork = (Network) caseData[1];
        Random rnd = new Random();
        // 先 EMOJI_MESSAGE_COUNT 条 EmojiMessage
        for (int i = 0; i < EMOJI_MESSAGE_COUNT; i++) {
            int eid = rnd.nextInt(20);
            int p1 = rnd.nextInt(PERSON_COUNT + PERSON_EXTRA_COUNT) + 1;
            int p2 = rnd.nextInt(PERSON_COUNT + PERSON_EXTRA_COUNT) + 1;
            while (p2 == p1) { p2 = rnd.nextInt(PERSON_COUNT + PERSON_EXTRA_COUNT) + 1; }
            PersonInterface oldPerson1 = oldNetwork.getPerson(p1);
            PersonInterface newPerson1 = testNetwork.getPerson(p1);
            try {
                Message oldM;
                Message newM;
                if (rnd.nextBoolean()) { // type 1
                    TagInterface oldTag;
                    TagInterface newTag;
                    int tagId = rnd.nextInt(20);    
                    if (oldPerson1.containsTag(tagId) && newPerson1.containsTag(tagId)) {
                        oldTag = oldPerson1.getTag(tagId);
                        newTag = newPerson1.getTag(tagId);
                    } else { // 如果没有这个tag，则新建一个
                        oldTag = new Tag(tagId);
                        newTag = new Tag(tagId);
                        oldPerson1.addTag(oldTag);
                        newPerson1.addTag(newTag);
                        oldNetwork.addPersonToTag(p2, p1, tagId);
                        testNetwork.addPersonToTag(p2, p1, tagId);
                    }
                    oldM = new EmojiMessage(i, eid, oldPerson1, oldTag);
                    newM = new EmojiMessage(i, eid, newPerson1, newTag);
                } else { // type 0
                    PersonInterface oldPerson2 = oldNetwork.getPerson(p2);
                    PersonInterface newPerson2 = testNetwork.getPerson(p2);
                    oldM = new EmojiMessage(i, eid, oldPerson1, oldPerson2);
                    newM = new EmojiMessage(i, eid, newPerson1, newPerson2);
                }
                oldNetwork.addMessage(oldM);
                testNetwork.addMessage(newM);
            } catch (Exception ignored) {
                // do nothing
            }
        }
        // 再 RANDOM_MESSAGE_COUNT 条，随机四种
        for (int i = EMOJI_MESSAGE_COUNT; i < EMOJI_MESSAGE_COUNT + RANDOM_MESSAGE_COUNT; i++) {
            int kind = rnd.nextInt(3);  // 0:RedEnvelope,1:Forward,2:Emoji
            int p1 = rnd.nextInt(PERSON_COUNT + PERSON_EXTRA_COUNT) + 1;
            int p2 = rnd.nextInt(PERSON_COUNT + PERSON_EXTRA_COUNT) + 1;
            while (p2 == p1) { p2 = rnd.nextInt(PERSON_COUNT + PERSON_EXTRA_COUNT) + 1; }
            PersonInterface oldPerson1 = oldNetwork.getPerson(p1);
            PersonInterface newPerson1 = testNetwork.getPerson(p1);
            try {
                Message old;
                Message newM;
                if (rnd.nextBoolean()) { // type 1
                    TagInterface oldTag;
                    TagInterface newTag;
                    int tagId = rnd.nextInt(20);    
                    if (oldPerson1.containsTag(tagId) && newPerson1.containsTag(tagId)) {
                        oldTag = oldPerson1.getTag(tagId);
                        newTag = newPerson1.getTag(tagId);
                    } else { // 如果没有这个tag，则新建一个
                        oldTag = new Tag(tagId);
                        newTag = new Tag(tagId);
                        oldPerson1.addTag(oldTag);
                        newPerson1.addTag(newTag);
                        oldNetwork.addPersonToTag(p2, p1, tagId);
                        testNetwork.addPersonToTag(p2, p1, tagId);
                    }
                    switch (kind) {
                        case 0:
                            int m = rnd.nextInt(100);
                            old = new RedEnvelopeMessage(i, m, oldPerson1, oldTag);
                            newM = new RedEnvelopeMessage(i, m, newPerson1, newTag);
                            break;
                        case 1:
                            int aid = rnd.nextInt(20);
                            old = new ForwardMessage(i, aid, oldPerson1, oldTag);
                            newM = new ForwardMessage(i, aid, newPerson1, newTag);
                            break;
                        case 2:
                            int eid = rnd.nextInt(20);
                            old = new EmojiMessage(i, eid, oldPerson1, oldTag);
                            newM = new EmojiMessage(i, eid, newPerson1, newTag);
                            break;
                        default:
                            old = new EmojiMessage(i, 20, oldPerson1, oldTag);
                            newM = new EmojiMessage(i, 20, newPerson1, newTag);
                            break;
                    }
                } else {
                    PersonInterface oldPerson2 = oldNetwork.getPerson(p2);
                    PersonInterface newPerson2 = testNetwork.getPerson(p2);
                    switch (kind) {
                        case 0:
                            int m = rnd.nextInt(100);
                            old = new RedEnvelopeMessage(i, m, oldPerson1, oldPerson2);
                            newM = new RedEnvelopeMessage(i, m, newPerson1, newPerson2);
                            break;
                        case 1:
                            int aid = rnd.nextInt(20);
                            old = new ForwardMessage(i, aid, oldPerson1, oldPerson2);
                            newM = new ForwardMessage(i, aid, newPerson1, newPerson2);
                            break;
                        case 2:
                            int eid = rnd.nextInt(20);
                            old = new EmojiMessage(i, eid, oldPerson1, oldPerson2);
                            newM = new EmojiMessage(i, eid, newPerson1, newPerson2);
                            break;
                        default:
                            old = new EmojiMessage(i, 20, oldPerson1, oldPerson2);
                            newM = new EmojiMessage(i, 20, newPerson1, newPerson2);
                            break;
                    }
                }
                oldNetwork.addMessage(old);
                testNetwork.addMessage(newM);
            } catch (Exception ignored) {
                // do nothing
            }
        }
    }

    private static void sendMessage(Object[] caseData) {
        Network oldNetwork = (Network) caseData[0];
        Network testNetwork = (Network) caseData[1];
        Random rnd = new Random();
        for (int i = 0; i < SEND_COUNT; i++) {
            int messageId = rnd.nextInt(EMOJI_MESSAGE_COUNT + RANDOM_MESSAGE_COUNT - 50);
            try {
                oldNetwork.sendMessage(messageId);
                testNetwork.sendMessage(messageId);
            } catch (Exception ignored) {
                // do nothing
            }
        }
    }
 
    @Test
    public void checkDeleteColdEmoji() {
        // 删除cold emoji
        final int testCount = testNetwork.deleteColdEmoji(limit);
        int[] oldEmojiIdList = oldNetwork.getEmojiIdList();
        int[] oldEmojiHeatList = oldNetwork.getEmojiHeatList();
        int[] testEmojiIdList = testNetwork.getEmojiIdList();
        int[] testEmojiHeatList = testNetwork.getEmojiHeatList();
        // 大于limit的 emojiId 一定在 testNetwork 中
        for (int i = 0; i < oldEmojiIdList.length; i++) {
            if (oldEmojiHeatList[i] >= limit) {
                boolean found = false;
                for (int j = 0; j < testEmojiIdList.length; j++) {
                    if (oldEmojiIdList[i] == testEmojiIdList[j]) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found);
            }
        }
        // testNetwork 中的 emojiId 一定在 oldNetwork 中
        assertEquals(testEmojiIdList.length, testEmojiHeatList.length);
        for (int i = 0; i < testEmojiIdList.length; i++) {
            boolean found = false;
            for (int j = 0; j < oldEmojiIdList.length; j++) {
                if (testEmojiIdList[i] == oldEmojiIdList[j] && 
                    testEmojiHeatList[i] == oldEmojiHeatList[j]) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
        // length = testCount
        int emojiIdlength = 0;
        for (int i = 0; i < oldEmojiIdList.length; i++) {
            if (oldEmojiHeatList[i] >= limit) {
                emojiIdlength++;
            }
        }
        assertEquals(testEmojiIdList.length, emojiIdlength);
        // 所有的 message 在emojiIdList中出现的都在 testNetwork 中
        MessageInterface[] oldMessages = oldNetwork.getMessages();
        MessageInterface[] testMessages = testNetwork.getMessages();
        for (MessageInterface oldMessage : oldMessages) {
            if (oldMessage instanceof EmojiMessageInterface) {
                EmojiMessageInterface oldEmojiMessage = (EmojiMessageInterface) oldMessage;
                int oldEmojiId = oldEmojiMessage.getEmojiId();
                if (testNetwork.containsEmojiId(oldEmojiId)) {
                    boolean found = false;
                    for (MessageInterface testMessage : testMessages) {
                        if (equalMessage(oldMessage, testMessage)) {
                            found = true;
                            break;
                        }
                    }
                    assertTrue(found);
                }
            } else {
                boolean found = false;
                for (MessageInterface testMessage : testMessages) {
                    if (equalMessage(oldMessage, testMessage)) {
                        found = true;
                        break;
                    }
                }
                assertTrue(found);
            }
        }
        // messageslength
        int messagesLength = 0;
        for (MessageInterface oldMessage : oldMessages) {
            if (oldMessage instanceof EmojiMessageInterface) {
                EmojiMessageInterface oldEmojiMessage = (EmojiMessageInterface) oldMessage;
                int oldEmojiId = oldEmojiMessage.getEmojiId();
                if (testNetwork.containsEmojiId(oldEmojiId)) {
                    messagesLength++;
                }
            } else {
                messagesLength++;
            }
        }
        assertEquals(testMessages.length, messagesLength);
        // result == emojiIdList.length
        assertEquals(testCount, testEmojiIdList.length);
    }

    public boolean equalMessage(MessageInterface message1, MessageInterface message2) {
        if (message1.getId() == message2.getId() && message1.getType() == message2.getType()
            && message1.getSocialValue() == message2.getSocialValue() 
            && message1.getPerson1().equals(message2.getPerson1())) {
            if (message1.getType() == 0 && message1.getTag() == null && message2.getTag() == null
                && message1.getPerson2().equals(message2.getPerson2())) {
                if (message1 instanceof RedEnvelopeMessageInterface) {
                    RedEnvelopeMessageInterface redEnvelopeMessage1 = (RedEnvelopeMessageInterface) message1;
                    RedEnvelopeMessageInterface redEnvelopeMessage2 = (RedEnvelopeMessageInterface) message2;
                    return redEnvelopeMessage1.getMoney() == redEnvelopeMessage2.getMoney();
                } else if (message1 instanceof ForwardMessageInterface) {
                    ForwardMessageInterface forwardMessage1 = (ForwardMessageInterface) message1;
                    ForwardMessageInterface forwardMessage2 = (ForwardMessageInterface) message2;
                    return forwardMessage1.getArticleId() == forwardMessage2.getArticleId();
                } else if (message1 instanceof EmojiMessageInterface) {
                    EmojiMessageInterface emojiMessage1 = (EmojiMessageInterface) message1;
                    EmojiMessageInterface emojiMessage2 = (EmojiMessageInterface) message2;
                    return emojiMessage1.getEmojiId() == emojiMessage2.getEmojiId();
                } else {
                    return false;
                }
            } else if (message1.getType() == 1 && message1.getPerson2() == null && message2.getPerson2() == null
                && message1.getTag().equals(message2.getTag())) {
                if (message1 instanceof RedEnvelopeMessageInterface) {
                    RedEnvelopeMessageInterface redEnvelopeMessage1 = (RedEnvelopeMessageInterface) message1;
                    RedEnvelopeMessageInterface redEnvelopeMessage2 = (RedEnvelopeMessageInterface) message2;
                    return redEnvelopeMessage1.getMoney() == redEnvelopeMessage2.getMoney();
                } else if (message1 instanceof ForwardMessageInterface) {
                    ForwardMessageInterface forwardMessage1 = (ForwardMessageInterface) message1;
                    ForwardMessageInterface forwardMessage2 = (ForwardMessageInterface) message2;
                    return forwardMessage1.getArticleId() == forwardMessage2.getArticleId();
                } else if (message1 instanceof EmojiMessageInterface) {
                    EmojiMessageInterface emojiMessage1 = (EmojiMessageInterface) message1;
                    EmojiMessageInterface emojiMessage2 = (EmojiMessageInterface) message2;
                    return emojiMessage1.getEmojiId() == emojiMessage2.getEmojiId();
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static Network deepCopyNetwork(Network original) { // 无法深拷贝
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
        // 复制消息
        // Copy messages
        for (MessageInterface message : original.getMessages()) {
            try {
                clonedNetwork.addMessage(message);
            } catch (Exception ignored) {
                // do nothing
            }
        }

        // Copy emoji and their heat
        int[] emojiIds = original.getEmojiIdList();
        int[] emojiHeats = original.getEmojiHeatList(); // 没法做
        for (int i = 0; i < emojiIds.length; i++) {
             try {
                clonedNetwork.storeEmojiId(i);
            } catch (Exception ignored) {
                // do nothing
            }
        }
        return clonedNetwork;
    }
}
