import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

public class Bookshelf {
    private final Books books;

    public Bookshelf(Books allBooks) {
        this.books = allBooks;
    }

    public Book borrowByIsbn(LibraryBookIsbn isbn) {
        return books.getRemoveBookByIsbn(isbn);
    }

    public Book borrowById(LibraryBookId bookId) {
        return books.getRemoveBookById(bookId);
    }

    public Book queryBookByIsbn(LibraryBookIsbn isbn) {
        return books.getBookByIsbn(isbn);
    }

    public void addBook(Book book) {
        if (book == null) {
            return;
        }
        books.addBook(book);
    }

    public int getAvailableCount(LibraryBookIsbn isbn) {
        return books.getAvailableCount(isbn);
    }

    public Books getBooks() {
        return books;
    }
}
