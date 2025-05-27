import java.util.ArrayList;

public class BrrowReturnOffice {
    private final Books books; // 用于存储借还台的书籍

    public BrrowReturnOffice() {
        this.books = new Books();
    }

    public void addBook(Book book) {
        books.addBook(book);
    }

    public ArrayList<Book> clearBrrowReturnOffice() {
        return books.clearAllBooks();
    }
}
