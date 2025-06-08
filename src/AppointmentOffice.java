import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import com.oocourse.library3.LibraryBookId;

public class AppointmentOffice {
    private final HashSet<ReservationInfo> reservationInfos;
    private final Books reservedBooks;

    public AppointmentOffice() {
        this.reservationInfos = new HashSet<>();
        this.reservedBooks = new Books();
    }

    public void addReservationInfo(ReservationInfo reservationInfo) {
        if (reservationInfo == null || reservationInfos.contains(reservationInfo)) {
            return;
        }
        reservationInfos.add(reservationInfo);
    }

    public boolean addReservedBook(ReservationInfo info, Book book, LocalDate date) {
        if (book == null || info == null || !reservationInfos.contains(info)) {
            return false;
        } else if (info.getReservationDate() != null || info.getReservationId() != null) {
            return false;
        }
        reservedBooks.addBook(book);
        info.setReservationId(book.getBookId());
        info.setReservationDate(date);
        return true;
    }

    public Book getReservedBook(ReservationInfo info) {
        if (info == null || !reservationInfos.contains(info) 
            || info.getReservationDate() == null || info.getReservationId() == null) {
            return null;
        } else if (!reservedBooks.containsBookId(info.getReservationId())) {
            return null;
        } else {
            LibraryBookId bookId = info.getReservationId();
            Book book = reservedBooks.getRemoveBookById(bookId);
            if (book != null) {
                reservationInfos.remove(info);
            }
            return book;
        }
    }

    public ArrayList<ReservationInfo> getAllReservationInfo() {
        ArrayList<ReservationInfo> reservationInfoList = new ArrayList<>(reservationInfos);
        return reservationInfoList;
    }

    public ArrayList<ReservationInfo> getExpiredReservationInfos(LocalDate date, int limitDays) {
        ArrayList<ReservationInfo> expired = new ArrayList<>();
        for (ReservationInfo info : reservationInfos) {
            LocalDate rdate = info.getReservationDate();
            if (rdate != null && rdate.until(date, ChronoUnit.DAYS) >= limitDays) {
                expired.add(info);
            }
        }
        return expired;
    }

    // 清除过期的预约信息和书籍：遍历reservationInfos迭代器进行删除
    public ArrayList<Book> clearOutdatedBooks(LocalDate date, int limitDays) {
        ArrayList<Book> outdatedBooks = new ArrayList<>();
        Iterator<ReservationInfo> iterator = reservationInfos.iterator();
        while (iterator.hasNext()) {
            ReservationInfo reservation = iterator.next();
            if (reservation != null) {
                LocalDate reservationDate = reservation.getReservationDate();
                if (reservationDate != null && 
                    reservationDate.until(date, ChronoUnit.DAYS) >= limitDays) {
                    Book book = reservedBooks.getRemoveBookById(reservation.getReservationId());
                    if (book != null) {
                        outdatedBooks.add(book);
                    }
                    // 预约信息失效
                    reservation.setValid(false);
                    reservation.setReservationId(null);
                    reservation.setReservationDate(null);
                    iterator.remove();
                }
            }
        }
        return outdatedBooks;
    }
}
