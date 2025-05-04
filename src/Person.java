import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import com.oocourse.spec2.main.PersonInterface;
import com.oocourse.spec2.main.TagInterface;

public class Person implements PersonInterface {
    private int id;
    private String name;
    private int age;
    private final HashMap<Integer, Person> acquaintance;
    private final HashMap<Integer, Integer> value;
    private final HashMap<Integer, Tag> tags;
    private final PriorityQueue<Integer> priorityQueue; // 优先队列，存储与该人有关系的人的id，优先级为value值
    // private final LinkedList<Integer> receivedArticles; // 该人收到的文章id，删除操作是O(n)，添加操作是O(1)
    
    private static class ListNode { // 双向链表节点
        private int articleId; // 文章id
        private ListNode prev = null; // 前驱节点
        private ListNode next = null; // 后继节点

        ListNode(int articleId) {
            this.articleId = articleId;
            this.prev = this.next = null;
        }
    }

    private final HashMap<Integer, ListNode> articleNodeMap; // 存储文章id对应的链表节点，删除和添加操作都是O(1)
    private ListNode head = null; // 双向链表的头指针
    private ListNode tail = null; // 双向链表的尾指针
    private boolean dirtyCache;
    private List<Integer> articleCache;

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
        // receivedArticles = new LinkedList<Integer>();
        articleNodeMap = new HashMap<>();
        head = tail = null;
        dirtyCache = true;
        articleCache = new LinkedList<>();
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

    @Override
    public List<Integer> getReceivedArticles() {
        rebuildArticleList(); // 重建缓存
        return articleCache;
    }

    @Override
    public List<Integer> queryReceivedArticles() {
        rebuildArticleList(); // 重建缓存
        return articleCache.subList(0, Math.min(articleCache.size(), 5)); // 返回前5个元素
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
        for (Tag tag : tags.values()) { // 与该person相关联的tag
            tag.delPerson(person); // 方法内判断是否有这个人
        }
    }

    public boolean isAcquaintanceEmpty() {
        return acquaintance.isEmpty(); // 判断是否为空  
    }

    public int queryBestAcquaintance() {
        return priorityQueue.peek(); // 返回优先级最高的acquaintance的id
    }

    // 添加文章到头部
    public void receiveArticle(int id) { // 文章id添加到最前面
        ListNode newNode = new ListNode(id);
        if (head == null) { // 如果链表为空
            head = tail = newNode;
        } else { // 如果链表不为空
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
        articleNodeMap.put(id, newNode); // 哈希表记录文章位置
        dirtyCache = true; // 标记
    }

    // 删除文章
    public void deleteArticle(int articleId) {
        ListNode node = articleNodeMap.get(articleId);
        if (node == null) { return; } // 如果文章不存在
        if (node == head) { // 如果是头节点
            head = head.next;
            if (head != null) {
                head.prev = null;
            }
        } else if (node == tail) { // 如果是尾节点
            tail = tail.prev;
            if (tail != null) {
                tail.next = null;
            }
        } else { // 如果是中间节点
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        articleNodeMap.remove(articleId); // 移除hash表中的记录
        dirtyCache = true; // 标记
    }

    private void rebuildArticleList() {
        if (!dirtyCache) { return; }
        articleCache.clear();
        for (ListNode cur = head; cur != null; cur = cur.next) {
            articleCache.add(cur.articleId);
        }
        dirtyCache = false;
    }

    public boolean strictEquals(PersonInterface person) {
        Person p = (Person) person;
        return id == p.getId() &&
               name.equals(p.getName()) &&
               age == p.getAge() &&
               acquaintance.equals(p.getAcquaintance()) &&
               value.equals(p.value) &&
               tags.equals(p.tags);
    }
}
