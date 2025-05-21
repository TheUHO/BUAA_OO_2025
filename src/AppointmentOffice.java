import java.time.LocalDate;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import com.oocourse.library1.LibraryBookId;

public class AppointmentOffice {
    private final HashSet<ReservationInfo> reservationInfos;
    private final Books reservedBooks;
    private final HashMap<ReservationInfo, LocalDate> reservationInfoToDate;

    public AppointmentOffice() {
        this.reservationInfos = new HashSet<>();
        this.reservedBooks = new Books();
        this.reservationInfoToDate = new HashMap<>();
    }

    public void addReservationInfo(ReservationInfo reservationInfo) {
        if (reservationInfo == null || reservationInfos.contains(reservationInfo)) {
            return;
        }
        reservationInfos.add(reservationInfo);
    }

    public boolean addReservedBook(ReservationInfo reservationInfo, Book book, LocalDate date) {
        if (book == null || reservedBooks.containsIsbn(book.getBookIsbn())) {
            return false;
        }
        reservedBooks.addBook(book);
        reservationInfoToDate.put(reservationInfo, date);
        return true;
    }

    public Book getReservedBook(ReservationInfo reservationInfo) {
        if (reservationInfo == null || !reservationInfos.contains(reservationInfo) 
            || !reservationInfoToDate.containsKey(reservationInfo)) {
            return null;
        } else if (!reservedBooks.containsBookId(reservationInfo.getBookId())) {
            return null;
        } else {
            LibraryBookId bookId = reservationInfo.getBookId();
            Book book = reservedBooks.getRemoveBookById(bookId);
            if (book != null) {
                reservationInfos.remove(reservationInfo);
                reservationInfoToDate.remove(reservationInfo);
            }
            return book;
        }
    }

    public ArrayList<ReservationInfo> getAllReservationInfo() {
        ArrayList<ReservationInfo> reservationInfoList = new ArrayList<>(reservationInfos);
        return reservationInfoList;
    }

    // 清除过期的预约信息和书籍：遍历reservationInfos迭代器进行删除
    public ArrayList<Book> clearOutdatedBooks(LocalDate date, int limitDays) {
        ArrayList<Book> outdatedBooks = new ArrayList<>();
        Iterator<ReservationInfo> iterator = reservationInfos.iterator();
        while (iterator.hasNext()) {
            ReservationInfo reservation = iterator.next();
            if (reservationInfoToDate.containsKey(reservation)) {
                LocalDate reservationDate = reservationInfoToDate.get(reservation);
                if (reservationDate.until(date).getDays() >= limitDays) {
                    Book book = reservedBooks.getRemoveBookById(reservation.getBookId());
                    if (book != null) {
                        outdatedBooks.add(book);
                    }
                    // 预约信息失效
                    reservation.setValid(false);
                    iterator.remove();
                    reservationInfoToDate.remove(reservation);
                }
            }
        }
        return outdatedBooks;
    }
}
