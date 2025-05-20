import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

public class User {
    private final String studentId;
    private final Books books;
    private boolean hasTypeB;
    private ReservationInfo reservationInfo;

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
            reservationInfo.setValid(false);
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
}
