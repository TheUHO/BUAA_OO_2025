import java.util.Objects;
import com.oocourse.library1.LibraryBookId;

public class ReservationInfo {
    private final String studentId;
    private final LibraryBookId bookId;
    private boolean valid;

    public ReservationInfo(String studentId, LibraryBookId bookId) {
        this.studentId = studentId;
        this.bookId = bookId;
        this.valid = true;
    }

    public String getStudentId() {
        return studentId;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public int hashCode() { 
        // 使用studentId和bookId的hashCode作为ReservationInfo的hashCode
        return Objects.hash(studentId, bookId);
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
               Objects.equals(bookId, that.bookId); 
    }
}
