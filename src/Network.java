import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualRelationException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec3.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec3.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualArticleIdException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.EmojiMessageInterface;
import com.oocourse.spec3.main.ForwardMessageInterface;
import com.oocourse.spec3.main.MessageInterface;
import com.oocourse.spec3.main.NetworkInterface;
import com.oocourse.spec3.main.PersonInterface;
import com.oocourse.spec3.main.RedEnvelopeMessageInterface;
import com.oocourse.spec3.main.TagInterface;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons;
    private final Graph graph; // 并查集
    private final TagStorage tagStorage; // 标签存储
    private int tripleSum = 0;
    private final HashMap<Integer, HashMap<Integer, Integer>> shortestPathCache;
    private final HashMap<Integer, OfficialAccount> accounts;
    private final HashSet<Integer> articles;
    private final HashMap<Integer, Integer> articleContributors;
    private final HashMap<Integer, MessageInterface> messages;
    private final HashMap<Integer, Integer> emojiMap;
    private boolean shortestPathCacheDirty = true; // 缓存是否脏

    public Network() {
        persons = new HashMap<>();
        graph = new Graph();
        tagStorage = new TagStorage(persons);
        tripleSum = 0;
        shortestPathCache = new HashMap<>();
        shortestPathCacheDirty = true;
        accounts = new HashMap<>();
        articles = new HashSet<>();
        articleContributors = new HashMap<>();
        messages = new HashMap<>();
        emojiMap = new HashMap<>();
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
            shortestPathCacheDirty = true;
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
            tagStorage.modifyTagValueSum(id1, id2, value);
            shortestPathCacheDirty = true;
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
                tagStorage.modifyTagValueSum(id1, id2, value);
            } else {
                person1.deleteRelation(person2);
                person2.deleteRelation(person1);
                graph.deleteRelation(id1, id2); // 删除并查集
                tripleSum -= getSharedRelation(id1, id2);
                tagStorage.modifyTagValueSum(id1, id2, -(nextValue - value));
                shortestPathCacheDirty = true;
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
                tagStorage.linkPersonToTag(personId1, personId2, tagId); // 记录tagId和personId1的对应关系
            }
        }
    }

    @Override
    public int queryTagValueSum(int personId, int tagId) 
        throws PersonIdNotFoundException, TagIdNotFoundException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!persons.get(personId).containsTag(tagId)) {
            throw new TagIdNotFoundException(tagId);
        } else {
            return persons.get(personId).getTag(tagId).getValueSum();
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
    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    @Override
    public void addMessage(MessageInterface message) throws EqualMessageIdException, 
        EmojiIdNotFoundException, EqualPersonIdException, ArticleIdNotFoundException {
        if (containsMessage(message.getId())) {
            throw new EqualMessageIdException(message.getId());
        } else if ((message instanceof EmojiMessageInterface) &&
            !containsEmojiId(((EmojiMessageInterface) message).getEmojiId())) {
            throw new EmojiIdNotFoundException(((EmojiMessageInterface) message).getEmojiId());
        } else if ((message instanceof ForwardMessageInterface) &&
            !containsArticle(((ForwardMessageInterface) message).getArticleId())) {
            int articleId = ((ForwardMessageInterface) message).getArticleId();
            throw new ArticleIdNotFoundException(articleId);
        } else if ((message instanceof ForwardMessageInterface) &&
            containsArticle(((ForwardMessageInterface) message).getArticleId()) &&
            !(message.getPerson1().getReceivedArticles().contains(
            ((ForwardMessageInterface) message).getArticleId()))) {
            int articleId = ((ForwardMessageInterface) message).getArticleId();
            throw new ArticleIdNotFoundException(articleId);
        } else if (message.getType() == 0 && message.getPerson1().equals(message.getPerson2())) {
            throw new EqualPersonIdException(message.getPerson1().getId());
        } else {
            messages.put(message.getId(), message);
        }
    }

    @Override
    public MessageInterface getMessage(int id) {
        if (containsMessage(id)) {
            return messages.get(id);
        } else {
            return null;
        }
    }

    @Override 
    public void sendMessage(int id) throws RelationNotFoundException, 
        MessageIdNotFoundException, TagIdNotFoundException {
        if (!containsMessage(id)) {
            throw new MessageIdNotFoundException(id);
        } else if (getMessage(id).getType() == 0 &&
            !(getMessage(id).getPerson1().isLinked(getMessage(id).getPerson2()))) {
            throw new RelationNotFoundException(getMessage(id).getPerson1().getId(),
            getMessage(id).getPerson2().getId());
        } else if (getMessage(id).getType() == 1 &&
            !getMessage(id).getPerson1().containsTag(getMessage(id).getTag().getId())) {
            throw new TagIdNotFoundException(getMessage(id).getTag().getId());
        } else {
            MessageInterface message = getMessage(id);
            if (message.getType() == 0) {
                Person person1 = (Person) message.getPerson1();
                Person person2 = (Person) message.getPerson2();
                int socialValue = message.getSocialValue();
                person1.addSocialValue(socialValue);
                person2.addSocialValue(socialValue);
                if (message instanceof RedEnvelopeMessageInterface) {
                    int redEnvelope = ((RedEnvelopeMessageInterface) message).getMoney();
                    person1.addMoney(-redEnvelope);
                    person2.addMoney(redEnvelope);
                } else if  (message instanceof ForwardMessageInterface) {
                    int articleId = ((ForwardMessageInterface) message).getArticleId();
                    person2.receiveArticle(articleId);
                } else if (message instanceof EmojiMessageInterface) {
                    int emojiId = ((EmojiMessageInterface) message).getEmojiId();
                    int emojiHeat = emojiMap.get(emojiId) + 1;
                    emojiMap.replace(emojiId, emojiHeat);
                }
                person2.addMessage(message);
            } else if (message.getType() == 1) {
                Person person1 = (Person) message.getPerson1();
                Tag tag = (Tag) message.getTag();
                int socialValue = message.getSocialValue();
                person1.addSocialValue(socialValue);
                tag.addSocialValue(socialValue);
                if (message instanceof RedEnvelopeMessageInterface  && tag.getSize() > 0) {
                    int size = tag.getSize();
                    int moneyPer = ((RedEnvelopeMessageInterface) message).getMoney() / size;
                    person1.addMoney(- (moneyPer * size));
                    tag.addMoney(moneyPer);
                } else if  (message instanceof ForwardMessageInterface && tag.getSize() > 0) {
                    int articleId = ((ForwardMessageInterface) message).getArticleId();
                    tag.receiveArticle(articleId);
                } else if (message instanceof EmojiMessageInterface) {
                    int emojiId = ((EmojiMessageInterface) message).getEmojiId();
                    int emojiHeat = emojiMap.get(emojiId) + 1;
                    emojiMap.replace(emojiId, emojiHeat);
                }
                tag.addMessage(message);
            }
            messages.remove(id);
        }
    }

    @Override
    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            return ((Person)getPerson(id)).getSocialValue();
        }
    }

    @Override
    public List<MessageInterface> queryReceivedMessages(int id) 
        throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            return getPerson(id).getReceivedMessages();
        }
    }

    @Override
    public boolean containsEmojiId(int id) {
        return emojiMap.containsKey(id);
    }

    @Override
    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (containsEmojiId(id)) {
            throw new EqualEmojiIdException(id);
        } else {
            emojiMap.put(id, 0);
        }
    }

    @Override
    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            return getPerson(id).getMoney();
        }
    }

    @Override
    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (!containsEmojiId(id)) {
            throw new EmojiIdNotFoundException(id);
        } else {
            return emojiMap.get(id);
        }
    }

    @Override
    public int deleteColdEmoji(int limit) {
        emojiMap.entrySet().removeIf(entry -> entry.getValue() < limit);
        messages.entrySet().removeIf(entry -> {
            MessageInterface message = entry.getValue();
            return (message instanceof EmojiMessageInterface) &&
                !containsEmojiId(((EmojiMessageInterface) message).getEmojiId());
        });
        return emojiMap.size();
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

    @Override
    public int queryCoupleSum() {
        int sum = 0;
        for (Person p : persons.values()) {
            if (p.isAcquaintanceEmpty()) { continue; }
            if (persons.get(p.queryBestAcquaintance()).queryBestAcquaintance() == p.getId()) {
                sum++;
            }
        }
        return sum / 2; // 去除重复计算
    }

    @Override
    public int queryShortestPath(int id1,int id2) 
        throws PersonIdNotFoundException, PathNotFoundException {
        if (!containsPerson(id1)) {
            throw new PersonIdNotFoundException(id1);
        } else if (!containsPerson(id2)) {
            throw new PersonIdNotFoundException(id2);
        } else if (!graph.isCircle(id1, id2)) {
            throw new PathNotFoundException(id1, id2);
        } else {
            int result = bfsShortestPath(id1, id2);
            return result;
        }
    }

    @Override
    public boolean containsAccount(int id) {
        return accounts.containsKey(id);
    }

    @Override
    public void createOfficialAccount(int personId, int accountId, String name) 
        throws PersonIdNotFoundException, EqualOfficialAccountIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (containsAccount(accountId)) {
            throw new EqualOfficialAccountIdException(accountId);
        } else {
            OfficialAccount account = new OfficialAccount(personId, accountId, name);
            account.addFollower(getPerson(personId));
            accounts.put(accountId, account);
        }
    }

    @Override
    public void deleteOfficialAccount(int personId, int accountId) throws PersonIdNotFoundException,
        OfficialAccountIdNotFoundException, DeleteOfficialAccountPermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteOfficialAccountPermissionDeniedException(personId, accountId);
        } else {
            accounts.remove(accountId);
        }
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    @Override
    public void contributeArticle(int personId,int accountId,int articleId) throws
        PersonIdNotFoundException, OfficialAccountIdNotFoundException, 
        EqualArticleIdException, ContributePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (containsArticle(articleId)) {
            throw new EqualArticleIdException(articleId);
        } else if (!accounts.get(accountId).containsFollower(getPerson(personId))) {
            throw new ContributePermissionDeniedException(personId, articleId);
        } else {
            OfficialAccount account = accounts.get(accountId);
            account.contributeArticle(personId, articleId);
            articles.add(articleId);
            articleContributors.put(articleId, personId);
        }
    }

    @Override
    public void deleteArticle(int personId,int accountId,int articleId) 
        throws PersonIdNotFoundException, OfficialAccountIdNotFoundException, 
        ArticleIdNotFoundException, DeleteArticlePermissionDeniedException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (!accounts.get(accountId).containsArticle(articleId)) {
            throw new ArticleIdNotFoundException(articleId);
        } else if (accounts.get(accountId).getOwnerId() != personId) {
            throw new DeleteArticlePermissionDeniedException(personId, articleId);
        } else {
            OfficialAccount account = accounts.get(accountId);
            int contributorId = articleContributors.get(articleId);
            account.deleteArticle(contributorId, articleId);
        }
    }

    @Override
    public void followOfficialAccount(int personId,int accountId)  throws 
        PersonIdNotFoundException, OfficialAccountIdNotFoundException, EqualPersonIdException {
        if (!containsPerson(personId)) {
            throw new PersonIdNotFoundException(personId);
        } else if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else if (accounts.get(accountId).containsFollower(getPerson(personId))) {
            throw new EqualPersonIdException(personId);
        } else {
            accounts.get(accountId).addFollower(getPerson(personId));
        }
    }

    @Override
    public int queryBestContributor(int accountId) throws OfficialAccountIdNotFoundException {
        if (!containsAccount(accountId)) {
            throw new OfficialAccountIdNotFoundException(accountId);
        } else {
            return accounts.get(accountId).getBestContributor();
        }
    }

    @Override
    public List<Integer> queryReceivedArticles(int id) throws PersonIdNotFoundException {
        if (!containsPerson(id)) {
            throw new PersonIdNotFoundException(id);
        } else {
            return getPerson(id).queryReceivedArticles();
        }
    }

    // <------自主实现函数------->

    private int bfsShortestPath(int id1, int id2) throws PathNotFoundException {
        if (!shortestPathCacheDirty) {
            if (shortestPathCache.containsKey(id1) && shortestPathCache.get(id1).containsKey(id2)) {
                return shortestPathCache.get(id1).get(id2);
            }
            if (shortestPathCache.containsKey(id2) && shortestPathCache.get(id2).containsKey(id1)) {
                return shortestPathCache.get(id2).get(id1);
            }
        }
        shortestPathCache.clear();
        shortestPathCacheDirty = false;
        if (id1 == id2) {
            return 0;
        }
        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(id1);
        HashMap<Integer, Integer> distance = new HashMap<>();
        distance.put(id1, 0); // 初始化
        HashSet<Integer> visited = new HashSet<>();
        visited.add(id1);
        while (!queue.isEmpty()) {
            int currentId = queue.poll();
            Person currentPerson = (Person) getPerson(currentId); // 获取当前节点
            for (Person acquaintance : currentPerson.getAcquaintance().values()) {
                int neighborId = acquaintance.getId();
                if (!visited.contains(neighborId)) { // 如果没有访问过
                    visited.add(neighborId);
                    queue.add(neighborId);
                    distance.put(neighborId, distance.get(currentId) + 1); // 更新距离
                    if (neighborId == id2) { // 找到目标id
                        shortestPathCache.put(id1, distance);
                        return distance.get(neighborId);
                    }
                }
            }
        }
        throw new PathNotFoundException(id1, id2);
    }

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

    public MessageInterface[] getMessages() {
        return (MessageInterface[]) messages.values().toArray(new MessageInterface[0]);
    }

    public int[] getEmojiIdList() {
        int[] emojiIdList = new int[emojiMap.size()];
        int index = 0;
        for (int id : emojiMap.keySet()) {
            emojiIdList[index++] = id;
        }
        return emojiIdList;
    }

    public int[] getEmojiHeatList() {
        int[] emojiHeatList = new int[emojiMap.size()];
        int index = 0;
        for (int heat : emojiMap.values()) {
            emojiHeatList[index++] = heat;
        }
        return emojiHeatList;
    }
}
