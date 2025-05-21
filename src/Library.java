import static com.oocourse.library1.LibraryIO.PRINTER;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryTrace;

public class Library {
    private final Bookshelf bookshelf;
    private final AppointmentOffice appointmentOffice;
    private final BrrowReturnOffice brrowReturnOffice;
    private final HashMap<String, User> users;
    private final Books queryMachine; // 用于查询书籍信息

    public Library(Map<LibraryBookIsbn, Integer> bookList) {
        Books allBooks = new Books(bookList);
        this.queryMachine = initQueryMachine(allBooks);
        this.bookshelf = new Bookshelf(allBooks);
        this.appointmentOffice = new AppointmentOffice();
        this.brrowReturnOffice = new BrrowReturnOffice();
        this.users = new HashMap<>();
    }

    private Books initQueryMachine(Books allBooks) {
        // 初始化查询机
        Books queryMachine = new Books();
        for (Book book : allBooks.getAllBooks()) {
            queryMachine.addBook(book);
        }
        return queryMachine;
    }

    public void openLibrary(LocalDate date) {
        // 开馆时的操作
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        // 借还处不应该有书
        clearBrrowReturnOffice(moveInfos, date);
        // 预约处不应该有逾期的书
        clearAppointmentOffice(moveInfos, date, 5);
        // 为预约预留书籍
        sendReservedBook(moveInfos, date);
        // 打印整理操作
        PRINTER.move(date, moveInfos);
    }

    public void closeLibrary(LocalDate date) {
        // 闭馆时的操作
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        // 预约处不应该有逾期的书
        clearAppointmentOffice(moveInfos, date, 4);
        // 借还处不应该有书
        clearBrrowReturnOffice(moveInfos, date);
        // 打印整理操作
        PRINTER.move(date, moveInfos);
    }

    // 借书
    public void borrowBook(String studentId, LibraryReqCmd req) {
        User user = getUser(studentId);
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (user.checkoutBorrow(isbn)) { // 检查是否可以借书
            Book book = bookshelf.borrowByIsbn(isbn);
            if (book != null) {
                // 借书成功
                user.addBook(book);
                addBookTrace(book, req.getDate(), LibraryBookState.USER);
                // 打印借书操作
                PRINTER.accept(req, book.getBookId());
                return;
            }
        }
        // 借书失败
        PRINTER.reject(req);
    }

    // 预约
    public void orderBook(String studentId, LibraryReqCmd req) {
        User user = getUser(studentId);
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (user.checkoutOrder(isbn)) { // 检查是否可以预约
            Book book = bookshelf.queryBookByIsbn(isbn);
            if (book != null) {
                // 预约成功
                ReservationInfo info = new ReservationInfo(studentId, book.getBookId());
                appointmentOffice.addReservationInfo(info); // 添加预约信息到预约处
                user.addReservationInfo(info); // 添加预约信息到用户
                // 打印预约操作
                PRINTER.accept(req);
                return;
            }
        }
        // 预约失败
        PRINTER.reject(req);
    }

    // 取书
    public void pickUpBook(String studentId, LibraryReqCmd req) {
        User user = getUser(studentId);
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (user.checkoutBorrow(isbn)) {
            ReservationInfo info = user.getReservationInfo();
            if (info != null && info.getBookId().getBookIsbn().equals(isbn)) {
                Book book = appointmentOffice.getReservedBook(info);
                if (book != null) {
                    user.addBook(book); // 添加书籍到用户
                    user.addReservationInfo(null); // 清除预约信息
                    addBookTrace(book, req.getDate(), LibraryBookState.USER);
                    // 打印取书操作
                    PRINTER.accept(req, book.getBookId());
                    return;
                }
            }
        }
        // 取书失败
        PRINTER.reject(req);
    }

    // 还书
    public void returnBook(String studentId, LibraryReqCmd req) {
        User user = getUser(studentId);
        LibraryBookId bookId = req.getBookId();
        Book book = user.returnBook(bookId); // 用户还书
        if (book != null) {
            // 还书成功
            brrowReturnOffice.addBook(book); // 添加书籍到借还处
            addBookTrace(book, req.getDate(), LibraryBookState.BORROW_RETURN_OFFICE);
            // 打印还书操作
            PRINTER.accept(req, book.getBookId());
            return;
        }
        // 还书失败
        PRINTER.reject(req);
    }

    // 查询
    public void queryBook(String studentId, LibraryReqCmd req) {
        LibraryBookId bookId = req.getBookId();
        ArrayList<LibraryTrace> traces = queryMachine.queryBookTrace(bookId);
        PRINTER.info(req.getDate(), bookId, traces);
    }

    private void clearBrrowReturnOffice(ArrayList<LibraryMoveInfo> moveInfos, LocalDate date) {
        ArrayList<Book> bookList = brrowReturnOffice.clearBrrowReturnOffice();
        for (Book book : bookList) {
            moveInfos.add(new LibraryMoveInfo(book.getBookId(), 
                LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.BOOKSHELF));
            bookshelf.addBook(book);
            addBookTrace(book, date, LibraryBookState.BOOKSHELF);
        }
    }

    private void clearAppointmentOffice(ArrayList<LibraryMoveInfo> moveInfos, 
        LocalDate date, int limitDays) {
        ArrayList<Book> bookList = appointmentOffice.clearOutdatedBooks(date, limitDays);
        for (Book book : bookList) {
            moveInfos.add(new LibraryMoveInfo(book.getBookId(), 
                LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF));
            bookshelf.addBook(book);
            addBookTrace(book, date, LibraryBookState.BOOKSHELF);
        }
    }

    private void sendReservedBook(ArrayList<LibraryMoveInfo> moveInfos, LocalDate date) {
        // 获取所有预约信息
        ArrayList<ReservationInfo> reservationInfos = appointmentOffice.getAllReservationInfo();
        for (ReservationInfo reservationInfo : reservationInfos) {
            // 根据预约信息获取书籍
            Book book = bookshelf.getBooks().getRemoveBookById(reservationInfo.getBookId());
            if (book == null) {
                // 书籍不存在，预约失败
                continue;
            } else if (appointmentOffice.addReservedBook(book, date)) {
                // 预约成功，书籍放入预约处
                moveInfos.add(new LibraryMoveInfo(book.getBookId(),  LibraryBookState.BOOKSHELF, 
                    LibraryBookState.APPOINTMENT_OFFICE, reservationInfo.getStudentId()));
                addBookTrace(book, date, LibraryBookState.APPOINTMENT_OFFICE);
            } else {
                // 预约失败，书籍放回书架
                bookshelf.addBook(book);
            }
        }
    }

    private void addBookTrace(Book book, LocalDate date, LibraryBookState newState) {
        book.addTrace(new LibraryTrace(date, book.getBookState(), newState));
        book.setBookState(newState);
    }

    private User getUser(String studentId) {
        users.computeIfAbsent(studentId, k -> new User(studentId));
        return users.get(studentId);
    }
}
