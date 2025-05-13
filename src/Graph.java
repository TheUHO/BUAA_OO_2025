import java.util.HashMap;
import java.util.HashSet;

public class Graph {
    private final HashMap<Integer, Integer> parent;
    private final HashMap<Integer, Integer> rank;
    private final HashMap<Integer, HashSet<Integer>> adjacentMap; // 记录每个人相邻的人的id
    private boolean dirty = false; // 是否需要重建

    public Graph() {
        parent = new HashMap<>();
        rank = new HashMap<>();
        adjacentMap = new HashMap<>();
    }

    public void addPerson(int id) {
        if (!parent.containsKey(id)) {
            parent.put(id, id); // id的代表元是自己
            rank.put(id, 0); // id的秩是0
        }
    }

    public int findRoot(int id) {
        int root = id;
        while (root != parent.get(root)) { // 找到根节点
            root = parent.get(root);
        }
        int current = id;
        while (current != root) { // 路径压缩
            int next = parent.get(current);
            parent.put(current, root);
            current = next;
        }
        return root;
    }

    public boolean addRelation(int id1, int id2) {
        int min = Math.min(id1, id2);
        int max = Math.max(id1, id2);
        if (adjacentMap.containsKey(min)) {
            adjacentMap.get(min).add(max);
        } else {
            HashSet<Integer> set = new HashSet<>();
            set.add(max);
            adjacentMap.put(min, set);
        }
        int root1 = findRoot(id1);
        int root2 = findRoot(id2);
        if (root1 == root2) {
            return false; // 已经在同一集合中
        }
        int rank1 = rank.get(root1);
        int rank2 = rank.get(root2);
        if (rank1 < rank2) { // 将秩小的集合合并到秩大的集合中
            parent.put(root1, root2);
        } else if (rank1 > rank2) {
            parent.put(root2, root1);
        } else { // 秩相等，选择一个作为新的根节点
            parent.put(root2, root1);
            rank.put(root1, rank1 + 1); // 秩加一
        }
        return true;
    }

    public void deleteRelation(int id1, int id2) {
        int min = Math.min(id1, id2);
        int max = Math.max(id1, id2);
        if (adjacentMap.containsKey(min)) {
            adjacentMap.get(min).remove(max);
            if (adjacentMap.get(min).isEmpty()) {
                adjacentMap.remove(min);
            }
            dirty = true; // 标记为脏
        }
    }

    public boolean isCircle(int id1, int id2) {
        if (dirty) {
            remake();
        }
        int root1 = findRoot(id1);
        int root2 = findRoot(id2);
        return root1 == root2;
    }

    private void remake() {
        for (Integer key : parent.keySet()) {
            parent.put(key, key); // 重置父节点
        }
        for (Integer key : rank.keySet()) {
            rank.put(key, 0); // 重置秩
        }
        for (Integer key : adjacentMap.keySet()) {
            for (Integer value : adjacentMap.get(key)) {
                this.addRelation(key, value); // 重新添加关系
            }
        }
        dirty = false; // 标记为干净
    }
}
