import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;

public class Bookshelf {
    private final Books normalBooks;  // 普通书架
    private final Books hotBooks;     // 热门书架

    public Bookshelf(Books normalBooks) {
        this.normalBooks = normalBooks;
        this.hotBooks = new Books();
    }

    // 借书：优先热门，再普通
    public Book borrowByIsbn(LibraryBookIsbn isbn) {
        Book b = hotBooks.getRemoveBookByIsbn(isbn);
        return (b != null ? b : normalBooks.getRemoveBookByIsbn(isbn));
    }

    public Book borrowById(LibraryBookId bookId) {
        Book b = hotBooks.getRemoveBookById(bookId);
        return (b != null ? b : normalBooks.getRemoveBookById(bookId));
    }

    // 查询：先热门再普通
    public Book queryBookByIsbn(LibraryBookIsbn isbn) {
        Book b = hotBooks.getBookByIsbn(isbn);
        return (b != null ? b : normalBooks.getBookByIsbn(isbn));
    }

    // 添加还回的书到普通书架
    public void addNormal(Book book) {
        if (book != null) {
            normalBooks.addBook(book);
        }
    }

    // 添加还回的书到热门书架
    public void addHot(Book book) {
        if (book != null) {
            hotBooks.addBook(book);
        }
    }

    public int getAvailableCount(LibraryBookIsbn isbn) {
        return hotBooks.getAvailableCount(isbn)
             + normalBooks.getAvailableCount(isbn);
    }

    // 整理时一次性取出
    public Books getNormalBooks() { return normalBooks; }

    public Books getHotBooks()    { return hotBooks; }
}