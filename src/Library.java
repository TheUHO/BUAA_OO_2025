import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static com.oocourse.library2.LibraryIO.PRINTER;
import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryTrace;
import com.oocourse.library2.annotation.Trigger;

public class Library {
    private final Bookshelf bookshelf;
    private final AppointmentOffice appointmentOffice;
    private final BrrowReturnOffice brrowReturnOffice;
    private final ReadingRoom readingRoom;
    private final HashMap<String, User> users;
    private final Books queryMachine; // 用于查询书籍信息
    private final HashSet<LibraryBookIsbn> hotIsbns; // 上次开馆后被借/读的 ISBN

    public Library(Map<LibraryBookIsbn, Integer> bookList) {
        Books allBooks = new Books(bookList);
        this.queryMachine = initQueryMachine(allBooks);
        this.bookshelf = new Bookshelf(allBooks);
        this.appointmentOffice = new AppointmentOffice();
        this.brrowReturnOffice = new BrrowReturnOffice();
        this.readingRoom = new ReadingRoom();
        this.users = new HashMap<>();
        this.hotIsbns = new HashSet<>();
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
        // 阅读室不应该有书
        clearReadingRoom(moveInfos, date);
        // 整理热门/普通书架
        reorganizeHotShelf(date, moveInfos);
        // 为预约预留书籍
        sendReservedBook(moveInfos, date);
        // 清空热门 ISBN 集合
        hotIsbns.clear();
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
    @Trigger(from = "bs", to = "user")
    @Trigger(from = "hbs", to = "user")
    public void borrowBook(String studentId, LibraryReqCmd req) {
        User user = getUser(studentId);
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (user.checkoutBorrow(isbn)) { // 检查是否可以借书
            Book book = bookshelf.borrowByIsbn(isbn);
            if (book != null) {
                // 借书成功
                user.addBook(book);
                hotIsbns.add(isbn); // 在借书成功后记录热门 ISBN
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
            // 预约成功
            ReservationInfo info = new ReservationInfo(studentId, isbn);
            appointmentOffice.addReservationInfo(info); // 添加预约信息到预约处
            user.addReservationInfo(info); // 添加预约信息到用户 
            // 打印预约操作
            PRINTER.accept(req);
            return;
        }
        // 预约失败
        PRINTER.reject(req);
    }

    // 取书
    @Trigger(from = "ao", to = "user")
    public void pickUpBook(String studentId, LibraryReqCmd req) {
        User user = getUser(studentId);
        LibraryBookIsbn isbn = req.getBookIsbn();
        if (user.checkoutBorrow(isbn)) {
            ReservationInfo info = user.getReservationInfo();
            if (info != null && info.getBookIsbn().equals(isbn)) {
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
    @Trigger(from = "user", to = "bro")
    public void returnBook(String studentId, LibraryReqCmd req) {
        User user = getUser(studentId);
        LibraryBookId bookId = req.getBookId();
        Book book = user.returnBook(bookId); // 用户还书
        if (book != null) {
            // 还书成功
            brrowReturnOffice.addBook(book); // 添加书籍到借还处
            addBookTrace(book, req.getDate(), LibraryBookState.BORROW_RETURN_OFFICE);
            // 打印还书操作
            PRINTER.accept(req);
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

    // 阅读
    @Trigger(from = "bs", to = "rr")
    @Trigger(from = "hbs", to = "rr")
    public void readBook(String studentId, LibraryReqCmd req) {
        User user = getUser(studentId);
        LocalDate today = req.getDate();
        if (!user.checkoutRead(today)) {
            PRINTER.reject(req);
            return;
        }
        Book book = bookshelf.borrowByIsbn(req.getBookIsbn());
        if (book != null) {
            user.addReadingBook(book, today); // 添加阅读书籍到用户
            readingRoom.addReadingBook(book, studentId); // 添加书籍到阅读室
            hotIsbns.add(req.getBookIsbn()); // 记录热门 ISBN
            addBookTrace(book, today, LibraryBookState.READING_ROOM);
            PRINTER.accept(req, book.getBookId());
        } else {
            PRINTER.reject(req);
        }
    }

    // 主动归还阅读室书
    @Trigger(from = "rr", to = "bro")
    public void restoreBook(String studentId, LibraryReqCmd req) {
        User user = getUser(studentId);
        Book book = user.restoreReadingBook(req.getBookId());
        if (book != null) {
            // 从阅读室取回
            readingRoom.restore(req.getBookId());
            brrowReturnOffice.addBook(book);
            addBookTrace(book, req.getDate(), LibraryBookState.BORROW_RETURN_OFFICE);
            PRINTER.accept(req);
        } else {
            PRINTER.reject(req);
        }
    }

    @Trigger(from = "bro", to = {"bs", "hbs"})
    private void clearBrrowReturnOffice(ArrayList<LibraryMoveInfo> moveInfos, LocalDate date) {
        ArrayList<Book> bookList = brrowReturnOffice.clearBrrowReturnOffice();
        for (Book book : bookList) {
            if (hotIsbns.contains(book.getBookIsbn())) {
                bookshelf.addHot(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), 
                    LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.HOT_BOOKSHELF));
                addBookTrace(book, date, LibraryBookState.HOT_BOOKSHELF);
            } else {
                bookshelf.addNormal(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), 
                    LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.BOOKSHELF));
                addBookTrace(book, date, LibraryBookState.BOOKSHELF);
            }
        }
    }

    @Trigger(from = "ao", to = {"bs", "hbs"})
    private void clearAppointmentOffice(ArrayList<LibraryMoveInfo> moveInfos, 
        LocalDate date, int limitDays) {
        ArrayList<Book> bookList = appointmentOffice.clearOutdatedBooks(date, limitDays);
        for (Book book : bookList) {
            if (hotIsbns.contains(book.getBookIsbn())) {
                bookshelf.addHot(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), 
                    LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.HOT_BOOKSHELF));
                addBookTrace(book, date, LibraryBookState.HOT_BOOKSHELF);
            } else {
                bookshelf.addNormal(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), 
                    LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF));
                addBookTrace(book, date, LibraryBookState.BOOKSHELF);
            }
        }
    }

    @Trigger(from = "rr", to = {"bs", "hbs"})
    private void clearReadingRoom(ArrayList<LibraryMoveInfo> moveInfos, LocalDate date) {
        ArrayList<Book> bookList = readingRoom.clearAllBooks();
        for (Book book : bookList) {
            if (hotIsbns.contains(book.getBookIsbn())) {
                bookshelf.addHot(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), 
                    LibraryBookState.READING_ROOM, LibraryBookState.HOT_BOOKSHELF));
                addBookTrace(book, date, LibraryBookState.HOT_BOOKSHELF);
            } else {
                bookshelf.addNormal(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(),
                    LibraryBookState.READING_ROOM, LibraryBookState.BOOKSHELF));
                addBookTrace(book, date, LibraryBookState.BOOKSHELF);
            }
            // 清除用户阅读记录
            getUser(readingRoom.getReader(book.getBookId())).clearReading();
        }
        readingRoom.clearReadingMap(); // 清空阅读记录
    }

    // 将所有在架图书按 hotIsbns 分流到热门/普通
    @Trigger(from = "bs", to = {"bs", "hbs"})
    @Trigger(from = "hbs", to = {"bs", "hbs"})
    private void reorganizeHotShelf(LocalDate date, ArrayList<LibraryMoveInfo> moveInfos) {
        // 一次性取出
        ArrayList<Book> norm = bookshelf.getNormalBooks().clearAllBooks();
        ArrayList<Book> hot  = bookshelf.getHotBooks().clearAllBooks();
        // 处理普通
        for (Book book : norm) {
            LibraryBookState from = LibraryBookState.BOOKSHELF;
            LibraryBookState to = hotIsbns.contains(book.getBookIsbn())
                ? LibraryBookState.HOT_BOOKSHELF : LibraryBookState.BOOKSHELF;
            if (from != to) {
                moveInfos.add(new LibraryMoveInfo(
                    book.getBookId(), from, to));
                addBookTrace(book, date, to);
            }
            if (to == LibraryBookState.HOT_BOOKSHELF) {
                bookshelf.addHot(book);
            } else {
                bookshelf.addNormal(book);
            }
        }
        // 处理热门
        for (Book book : hot) {
            LibraryBookState from = LibraryBookState.HOT_BOOKSHELF;
            LibraryBookState to = hotIsbns.contains(book.getBookIsbn())
                ? LibraryBookState.HOT_BOOKSHELF : LibraryBookState.BOOKSHELF;
            if (from != to) {
                moveInfos.add(new LibraryMoveInfo(
                    book.getBookId(), from, to));
                addBookTrace(book, date, to);
            }
            if (to == LibraryBookState.HOT_BOOKSHELF) {
                bookshelf.addHot(book);
            } else {
                bookshelf.addNormal(book);
            }
        }
    }

    @Trigger(from = "bs", to = "ao")
    private void sendReservedBook(ArrayList<LibraryMoveInfo> moveInfos, LocalDate date) {
        // 获取所有预约信息
        ArrayList<ReservationInfo> reservationInfos = appointmentOffice.getAllReservationInfo();
        for (ReservationInfo reservationInfo : reservationInfos) {
            // 根据预约信息获取书籍
            Book book = bookshelf.borrowByIsbn(reservationInfo.getBookIsbn());
            if (book == null) {
                // 书籍不存在，预约失败
                continue;
            } else if (appointmentOffice.addReservedBook(reservationInfo, book, date)) {
                // 预约成功，书籍放入预约处
                moveInfos.add(new LibraryMoveInfo(book.getBookId(),  LibraryBookState.BOOKSHELF, 
                    LibraryBookState.APPOINTMENT_OFFICE, reservationInfo.getStudentId()));
                addBookTrace(book, date, LibraryBookState.APPOINTMENT_OFFICE);
            } else {
                // 预约失败，书籍放回普通/热门书架
                if (book.getBookState() == LibraryBookState.HOT_BOOKSHELF) {
                    bookshelf.addHot(book);
                } else {
                    bookshelf.addNormal(book);
                }
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