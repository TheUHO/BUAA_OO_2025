import java.util.ArrayList;
import java.util.HashMap;

public class TagStorage {
    private final HashMap<Integer, Person> persons;
    private final HashMap<Integer, ArrayList<Tag>> personInTag; // p1, tags

    public TagStorage(HashMap<Integer, Person> persons) {
        this.persons = persons;
        personInTag = new HashMap<>();
    }

    public void linkPersonToTag(int personId1, int personId2, int tagId) {
        if (!personInTag.containsKey(personId1)) {
            personInTag.put(personId1, new ArrayList<>());
        }
        ArrayList<Tag> tags = personInTag.get(personId1);
        tags.add((Tag)persons.get(personId2).getTag(tagId));
    }

    // 无需删除ArrayList中的tag元素，因为如果是关系清空，则Person.deleteRelation会调用tag.delPerson
    // 也就无法在tag.modifyTagValueSum中修改了
    // 如果是删除tag，则不会再通过network查询到这个tag了，也就不用在意valueSum的修改了
    // public void deleteTagFromPerson1(int personId1, int personId2, int tagId) {
    //     if (personInTag.containsKey(personId1) && 
    //         personInTag.get(personId1).containsKey(personId2) &&
    //         personInTag.get(personId1).get(personId2).contains(tagId)) {
    //         personInTag.get(personId1).get(personId2).remove(tagId);
    //     }
    // }

    // public void deleteTagFromPerson2(int personId2, int tagId) {
    //     for (int personId1 : personInTag.keySet()) {
    //         if (personInTag.get(personId1).containsKey(personId2) &&
    //             personInTag.get(personId1).get(personId2).contains(tagId)) {
    //             personInTag.get(personId1).get(personId2).remove(tagId);
    //         }
    //     }
    // }

    public void modifyTagValueSum(int personId1, int personId2, int offsetValue) {
        if (personInTag.containsKey(personId1)) {
            ArrayList<Tag> tags = personInTag.get(personId1);
            for (Tag tag : tags) {
                tag.modifyValueSum(personId1, personId2, offsetValue);
            }
        }
    }
}
