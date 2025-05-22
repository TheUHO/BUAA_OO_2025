import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryTrace;
import com.oocourse.library1.LibraryBookId;

public class Books {
    private final HashMap<LibraryBookIsbn, HashMap<LibraryBookId, Book>> bookMap = new HashMap<>();

    // 初始化：构建书籍副本
    public Books(Map<LibraryBookIsbn, Integer> bookList) {
        for (Map.Entry<LibraryBookIsbn, Integer> entry : bookList.entrySet()) {
            LibraryBookIsbn isbn = entry.getKey();
            int count = entry.getValue();
            HashMap<LibraryBookId, Book> copies = new HashMap<>();
            for (int i = 1; i <= count; i++) {
                String copyId = String.format("%02d", i);
                LibraryBookId bookId = new LibraryBookId(isbn.getType(), isbn.getUid(), copyId);
                Book book = new Book(bookId);
                copies.put(bookId, book);
            }
            bookMap.put(isbn, copies);
        }
    }

    public Books() {
        // 默认构造函数
    }

    // 查询一本书的借阅记录
    public ArrayList<LibraryTrace> queryBookTrace(LibraryBookId bookId) {
        HashMap<LibraryBookId, Book> copies = bookMap.get(bookId.getBookIsbn());
        if (copies == null || copies.isEmpty()) {
            return new ArrayList<>();
        }
        Book book = copies.get(bookId);
        if (book == null) {
            return new ArrayList<>();
        }
        return book.getTraces();
    }

    // 借出一本书
    public Book getRemoveBookByIsbn(LibraryBookIsbn isbn) {
        if (isbn.isTypeA()) {
            return null;
        }
        HashMap<LibraryBookId, Book> copies = bookMap.get(isbn);
        if (copies == null || copies.isEmpty()) {
            return null;
        }
        LibraryBookId firstKey = copies.keySet().iterator().next();
        return copies.remove(firstKey);
    }

    public Book getRemoveBookById(LibraryBookId bookId) {
        if (bookId.isTypeA()) {
            return null;
        }
        HashMap<LibraryBookId, Book> copies = bookMap.get(bookId.getBookIsbn());
        if (copies == null || copies.isEmpty()) {
            return null;
        }
        return copies.remove(bookId);
    }

    // 添加一本书
    public void addBook(Book book) {
        LibraryBookIsbn isbn = book.getBookIsbn();
        bookMap.computeIfAbsent(isbn, k -> new HashMap<>())
            .put(book.getBookId(), book);
    }

    // 查询某个ISBN剩余数量
    public int getAvailableCount(LibraryBookIsbn isbn) {
        Map<LibraryBookId, Book> copies = bookMap.get(isbn);
        return (copies == null) ? 0 : copies.size();
    }

    // 判断是否有TypeB书籍
    public boolean containsTypeB() {
        for (LibraryBookIsbn isbn : bookMap.keySet()) {
            if (isbn.isTypeB() && !bookMap.get(isbn).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // 判断是否包含该ISBN
    public boolean containsIsbn(LibraryBookIsbn isbn) {
        return bookMap.containsKey(isbn) && !bookMap.get(isbn).isEmpty();
    }

    // 判断是否包含该BookId
    public boolean containsBookId(LibraryBookId bookId) {
        HashMap<LibraryBookId, Book> copies = bookMap.get(bookId.getBookIsbn());
        return (copies != null && copies.containsKey(bookId));
    }

    // 获取一本书
    public Book getBookByIsbn(LibraryBookIsbn isbn) {
        HashMap<LibraryBookId, Book> copies = bookMap.get(isbn);
        if (copies == null || copies.isEmpty()) {
            return null;
        }
        LibraryBookId firstKey = copies.keySet().iterator().next();
        return copies.get(firstKey);
    }

    // 获取所有书
    public ArrayList<Book> getAllBooks() {
        ArrayList<Book> all = new ArrayList<>();
        for (Map<LibraryBookId, Book> copies : bookMap.values()) {
            all.addAll(copies.values());
        }
        return all;
    }

    // 获取并清空所有书
    public ArrayList<Book> clearAllBooks() {
        ArrayList<Book> allBooks = new ArrayList<>();
        for (Map<LibraryBookId, Book> copies : bookMap.values()) {
            allBooks.addAll(copies.values());
            copies.clear();
        }
        return allBooks;
    }
}
