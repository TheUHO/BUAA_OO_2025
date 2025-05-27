import java.util.ArrayList;
import java.util.HashMap;
import com.oocourse.library2.LibraryBookId;

public class ReadingRoom {
    private final Books books;  // 存放阅读室里的书
    private final HashMap<LibraryBookId, String> readingMap;

    public ReadingRoom() {
        this.books = new Books();
        this.readingMap = new HashMap<>();
    }

    // 获取读者
    public String getReader(LibraryBookId bookId) {
        return readingMap.get(bookId);
    }

    // 用户开始阅读
    public void addReadingBook(Book book, String studentId) {
        books.addBook(book);
        readingMap.put(book.getBookId(), studentId);
    }

    // 用户归还阅读室中某本书
    public Book restore(LibraryBookId bookId) {
        if (!readingMap.containsKey(bookId)) {
            return null;
        }
        readingMap.remove(bookId);
        return books.getRemoveBookById(bookId);
    }

    // 闭馆或开馆整理时，把所有未归还的书移回普通书架
    public ArrayList<Book> clearAllBooks() {
        ArrayList<Book> list = books.clearAllBooks();
        return list;
    }

    // 清空阅读记录
    public void clearReadingMap() {
        readingMap.clear();
    }
}