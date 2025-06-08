import java.time.LocalDate;
import java.util.Objects;

import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

public class ReservationInfo {
    private final String studentId;
    private final LibraryBookIsbn bookIsbn;
    private LocalDate reservationDate;
    private LibraryBookId reservationId;
    private boolean valid;

    public ReservationInfo(String studentId, LibraryBookIsbn bookIsbn) {
        this.studentId = studentId;
        this.bookIsbn = bookIsbn;
        this.reservationDate = null;
        this.reservationId = null;
        this.valid = true;
    }

    public String getStudentId() {
        return studentId;
    }

    public LibraryBookIsbn getBookIsbn() {
        return bookIsbn;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public LibraryBookId getReservationId() {
        return reservationId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public void setReservationId(LibraryBookId reservationId) {
        this.reservationId = reservationId;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public int hashCode() { 
        // 使用studentId和bookId的hashCode作为ReservationInfo的hashCode
        return Objects.hash(studentId, bookIsbn);
    }

    @Override
    public boolean equals(Object obj) {
        // 根据studentId和bookId判断两个ReservationInfo对象是否相等
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ReservationInfo)) {
            return false;
        }
        ReservationInfo that = (ReservationInfo) obj;
        return Objects.equals(studentId, that.studentId) &&
               Objects.equals(bookIsbn, that.bookIsbn); 
    }
}
