import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import com.oocourse.spec2.main.OfficialAccountInterface;
import com.oocourse.spec2.main.PersonInterface;

public class OfficialAccount implements OfficialAccountInterface {
    private final int ownerId;
    private final int id;
    private final String name;
    private final HashMap<Integer, Person> followers;
    private final HashSet<Integer> articles;
    private final HashMap<Integer, Integer> contributions;
    private final PriorityQueue<Integer> priorityQueue;

    public OfficialAccount(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.name = name;
        followers = new HashMap<>();
        articles = new HashSet<>();
        contributions = new HashMap<>();
        priorityQueue = new PriorityQueue<>(
            Comparator.<Integer>comparingInt(personId -> contributions.get(personId))
            .reversed()
            .thenComparingInt(personId -> personId));
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void addFollower(PersonInterface person) {
        Person p = (Person) person;
        int personId = p.getId();
        if (!followers.containsKey(personId)) {
            followers.put(personId, p);
            contributions.put(personId, 0);
            priorityQueue.add(personId);
        }
    }

    @Override
    public boolean containsFollower(PersonInterface person) {
        Person p = (Person) person;
        int personId = p.getId();
        return followers.containsKey(personId);
    }

    @Override
    public void addArticle(PersonInterface person, int id) {
        Person p = (Person) person;
        int personId = p.getId();
        if (!articles.contains(id) && followers.containsKey(personId)) {
            articles.add(id);
            contributions.put(personId, contributions.get(personId) + 1);
            priorityQueue.remove(personId);
            priorityQueue.add(personId);
        }
    }

    @Override
    public boolean containsArticle(int id) {
        return articles.contains(id);
    }

    @Override
    public void removeArticle(int id) {
        if (articles.contains(id)) {
            articles.remove(id);
        }
    }

    @Override
    public int getBestContributor() {
        return priorityQueue.peek();
    }

    // <------自主实现函数------>
    public void contributeArticle(int personId, int articleId) {
        addArticle(followers.get(personId), articleId);
        for (PersonInterface follower : followers.values()) { // 通知所有粉丝
            ((Person)follower).receiveArticle(articleId); // 添加到粉丝链表的头部
        }
    }

    public void deleteArticle(int contributorId, int articleId) {
        removeArticle(articleId);
        contributions.put(contributorId, contributions.get(contributorId) - 1); // 减少贡献值
        priorityQueue.remove(contributorId);
        priorityQueue.add(contributorId);
        for (PersonInterface follower : followers.values()) { // 通知所有粉丝
            ((Person)follower).deleteArticle(articleId); // 在粉丝的链表中删除
        }
    }
    
}
