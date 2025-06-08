import java.util.ArrayList;
import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryTrace;

public class Book {
    private LibraryBookId bookId;
    private ArrayList<LibraryTrace> traces;
    private LibraryBookState bookState;

    public Book(LibraryBookId id) {
        this.bookId = id;
        this.traces = new ArrayList<>();
        this.bookState = LibraryBookState.BOOKSHELF; // 初始在书架
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public LibraryBookIsbn getBookIsbn() {
        return bookId.getBookIsbn();
    }

    public LibraryBookState getBookState() {
        return bookState;
    }

    public void addTrace(LibraryTrace trace) {
        traces.add(trace);
    }

    public ArrayList<LibraryTrace> getTraces() {
        return traces;
    }

    public void setBookState(LibraryBookState bookState) {
        this.bookState = bookState;
    }

    @Override
    public int hashCode() {
        return bookId.hashCode();
    }
}
