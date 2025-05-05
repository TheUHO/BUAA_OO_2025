import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualRelationException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.OfficialAccountIdNotFoundException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.ArticleIdNotFoundException;
import com.oocourse.spec2.exceptions.ContributePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteArticlePermissionDeniedException;
import com.oocourse.spec2.exceptions.DeleteOfficialAccountPermissionDeniedException;
import com.oocourse.spec2.exceptions.EqualArticleIdException;
import com.oocourse.spec2.exceptions.EqualOfficialAccountIdException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.main.NetworkInterface;
import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

public class Network implements NetworkInterface {
    private final HashMap<Integer, Person> persons;
    private final Graph graph; // 并查集
    private final TagStorage tagStorage; // 标签存储
    private int tripleSum = 0;
    private final HashMap<Integer, HashMap<Integer, Integer>> shortestPathCache;
    private final HashMap<Integer, OfficialAccount> accounts;
    private final HashSet<Integer> articles;
    private final HashMap<Integer, Integer> articleContributors;

    public Network() {
        persons = new HashMap<>();
        graph = new Graph();
        tagStorage = new TagStorage(persons);
        tripleSum = 0;
        shortestPathCache = new HashMap<>();
        accounts = new HashMap<>();
        articles = new HashSet<>();
        articleContributors = new HashMap<>();
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
            tagStorage.modifyTagValueSum(id1, id2, value);
            shortestPathCache.clear(); // 清空缓存
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
                tagStorage.modifyTagValueSum(id1, id2, -person1.queryValue(person2));
                shortestPathCache.clear(); // 清空缓存
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
            if (shortestPathCache.containsKey(id1) && shortestPathCache.get(id1).containsKey(id2)) {
                return shortestPathCache.get(id1).get(id2);
            }
            if (shortestPathCache.containsKey(id2) && shortestPathCache.get(id2).containsKey(id1)) {
                return shortestPathCache.get(id2).get(id1);
            }
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
            account.deleteArticle(personId, articleId);
            articles.remove(articleId);
            articleContributors.remove(articleId);
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
                    // 更新缓存
                    shortestPathCache.putIfAbsent(currentId, new HashMap<>());
                    shortestPathCache.get(currentId).put(neighborId, distance.get(neighborId));
                    if (neighborId == id2) { // 找到目标id
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
}
