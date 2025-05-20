import java.time.LocalDate;
import java.util.Map;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryCloseCmd;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryOpenCmd;
import com.oocourse.library1.LibraryReqCmd;
import static com.oocourse.library1.LibraryIO.SCANNER;

class MainClass {
    public static void main(String[] args) {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory(); // 获取图书馆内所有书籍ISBN号及相应副本数
        Library library = new Library(bookList); // 创建图书馆对象
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) { break; }
            LocalDate today = command.getDate(); // 今天的日期
            if (command instanceof LibraryOpenCmd) {
                // 在开馆时做点什么
                library.openLibrary(today);
            } else if (command instanceof LibraryCloseCmd) {
                // 在闭馆时做点什么
                library.closeLibrary(today);
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                LibraryReqCmd.Type type = req.getType(); // 指令对应的类型（查询/阅读/借阅/预约/还书/取书/归还）
                String studentId = req.getStudentId(); // 指令对应的用户Id
                // 对指令进行处理
                switch (type) {
                    case BORROWED:
                        library.borrowBook(studentId, req);
                        break;
                    case ORDERED:
                        library.orderBook(studentId, req);
                        break;
                    case PICKED:
                        library.pickUpBook(studentId, req);
                        break;
                    case RETURNED:
                        library.returnBook(studentId, req);
                        break;
                    case QUERIED:
                        library.queryBook(studentId, req);
                        break;
                    default: // 其他类型的指令
                        break;
                }
            }
        }
    }
}