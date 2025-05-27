import java.time.LocalDate;
import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;

public class User {
    private final String studentId;
    private final Books books;
    private boolean hasTypeB;
    private ReservationInfo reservationInfo;
    private Book readingBook;
    private LocalDate readingDate;

    public User(String studentId) {
        this.studentId = studentId;
        this.books = new Books();
        this.hasTypeB = false;
        this.reservationInfo = null;
    }

    public String getStudentId() {
        return studentId;
    }

    public ReservationInfo getReservationInfo() {
        if (reservationInfo != null && reservationInfo.isValid()) {
            return reservationInfo;
        } else {
            reservationInfo = null;
            return null;
        }
    }

    public boolean containsTypeB() {
        return hasTypeB;
    }

    public boolean containsIsbn(LibraryBookIsbn isbn) {
        return books.containsIsbn(isbn);
    }

    public boolean checkoutBorrow(LibraryBookIsbn isbn) {
        if (isbn.isTypeA()) {
            return false;
        } else if (isbn.isTypeB() && containsTypeB()) {
            return false;
        } else if (isbn.isTypeC() && containsIsbn(isbn)) {
            return false;
        }
        return true;
    }

    public boolean checkoutOrder(LibraryBookIsbn isbn) {
        if (!checkoutBorrow(isbn)) {
            return false;
        } else if (reservationInfo != null && reservationInfo.isValid()) {
            return false;
        }
        return true;
    }

    public boolean checkoutRead(LocalDate today) {
        // 检查是否有正在阅读的书籍
        return readingBook == null || !readingDate.equals(today);
    }

    public void addBook(Book book) {
        if (book.getBookIsbn().isTypeB()) {
            hasTypeB = true;
        }
        books.addBook(book);
    }

    public Book returnBook(LibraryBookId bookId) {
        Book book = books.getRemoveBookById(bookId);
        if (book != null && bookId.isTypeB()) {
            hasTypeB = false;
        }
        return book;
    }

    public void addReservationInfo(ReservationInfo reservationInfo) {
        this.reservationInfo = reservationInfo;
    }

    public void addReadingBook(Book book, LocalDate date) {
        this.readingBook = book;
        this.readingDate = date;
    }

    public void clearReading() {
        this.readingBook = null;
        this.readingDate = null;
    }

    public Book restoreReadingBook(LibraryBookId bookId) {
        if (readingBook != null && readingBook.getBookId().equals(bookId)) {
            Book book = readingBook;
            clearReading();
            return book;
        }
        return null;
    }
}
