import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

public class User {
    private final String studentId;
    private final Books books;
    private boolean hasTypeB;
    private ReservationInfo reservationInfo;
    private Book readingBook;
    private LocalDate readingDate;
    private int creditScore = 100;
    // 新增：记录每本书借出的日期
    private final Map<LibraryBookId, LocalDate> borrowDates;

    public User(String studentId) {
        this.studentId = studentId;
        this.books = new Books();
        this.hasTypeB = false;
        this.reservationInfo = null;
        this.borrowDates = new HashMap<>();
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

    // 判断是否包含类型B
    public boolean containsTypeB() {
        return hasTypeB;
    }

    public boolean containsIsbn(LibraryBookIsbn isbn) {
        return books.containsIsbn(isbn);
    }

    public int getCreditScore() {
        return creditScore;
    }

    private void increaseCredit(int x) {
        creditScore = Math.min(180, creditScore + x);
    }

    private void decreaseCredit(int x) {
        creditScore = Math.max(0, creditScore - x);
    }

    public boolean checkoutBorrow(LibraryBookIsbn isbn) {
        // 借阅B/C类书，信用分需≥60
        if (creditScore < 60) {
            return false;
        }
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
        // 预约B/C类书，信用分需≥100
        if (creditScore < 100) {
            return false;
        }
        if (!checkoutBorrow(isbn)) {
            return false;
        } else if (reservationInfo != null && reservationInfo.isValid()) {
            return false;
        }
        return true;
    }

    public boolean checkoutRead(LocalDate today, LibraryBookIsbn isbn) {
        // A类阅读需≥40分，B/C类需>0分
        int min = isbn.isTypeA() ? 40 : 1;
        if (creditScore < min) {
            return false;
        }
        // 检查是否有正在阅读的书籍
        return readingBook == null || !readingDate.equals(today);
    }
    
    public int getOverdueDays(LibraryBookId bookId, LocalDate today) {
        LocalDate borrowDate = borrowDates.get(bookId);
        if (borrowDate == null) {
            return 0;
        }
        // B 类 30 天，其他 60 天
        int limitDays = bookId.isTypeB() ? 30 : 60;
        long diff = ChronoUnit.DAYS.between(borrowDate.plusDays(limitDays), today);
        return (int) Math.max(diff, 0);
    }

    public void addBook(Book book, LocalDate date) {
        if (book.getBookIsbn().isTypeB()) {
            hasTypeB = true;
        }
        books.addBook(book);
        borrowDates.put(book.getBookId(), date);
    }

    public Book returnBook(LibraryBookId bookId, LocalDate today) {
        Book book = books.getRemoveBookById(bookId);
        if (book == null) {
            return null;
        }
        if (bookId.isTypeB()) {
            hasTypeB = false;
        }
        // 逾期不还已在每次开馆时扣分
        if (getOverdueDays(bookId, today) <= 0) {
            increaseCredit(10);
        }
        borrowDates.remove(bookId);
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

    public Book restoreReadingBook(LibraryBookId bookId, LocalDate today) {
        if (readingBook != null && readingBook.getBookId().equals(bookId)) {
            Book book = readingBook;
            clearReading();
            // 当日主动归还阅读书籍，加10分
            increaseCredit(10);
            return book;
        }
        return null;
    }

    // 开馆时，逾期书籍扣分
    public void deductOverdueCredit(LocalDate lastOpenDate, LocalDate today) {
        for (Map.Entry<LibraryBookId, LocalDate> e : borrowDates.entrySet()) {
            LibraryBookId bookId = e.getKey();
            int overdueToday = getOverdueDays(bookId, today);
            int overdueLast = (lastOpenDate == null) ? 0 : getOverdueDays(bookId, lastOpenDate);
            int newly = overdueToday - overdueLast;
            if (newly > 0) {
                decreaseCredit(newly * 5);
            }
        }
    }

    // 阅读后不还，闭馆时扣10分
    public void punishNotReturnedReading() {
        decreaseCredit(10);
    }

    // 预约后不取，闭馆时扣15分
    public void punishNotPickedReservation() {
        decreaseCredit(15);
    }
}
