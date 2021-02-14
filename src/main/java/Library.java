import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Library {
    private static final String url = "jdbc:mysql://localhost:3306/library";
    private static final String user = "root";
    private static final String password = "root";
    private static ResultSet rs;
    private final Connection con;


    public Library() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection(url, user, password);
    }
    public void closeConnection(){
        try{
            con.close();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }
    public void getBook(int student,int book) throws SQLException {
        Statement stmt = con.createStatement();
        String checkQuery = "SELECT * FROM books_student WHERE student=" + student + " AND return_date IS NULL AND CURDATE()-taken_date>30;"; //Запрос на получение информации о задолженности студента; если имеется хотя бы одна задолженность книга не выдается
        rs = stmt.executeQuery(checkQuery);
        if (!rs.next()) { //Если ResultSet пустой, задолженности нет
            String secondCheckQuery = "SELECT quantity FROM books WHERE book_id=" + book + ""; //Запрос на получение информации о количестве экземпляров определенной книги
            rs = stmt.executeQuery(secondCheckQuery);
            while (rs.next()) {
                if (rs.getInt(1) > 0) { // Если количество экземпляров > 0, можно выдать книгу студенту
                    String query = "INSERT INTO books_student(student,book) VALUES(" + student + "," + book + ");"; // Записываем id студента и id книги, которую он хочет взять
                    stmt.executeUpdate(query);
                    String secondQuery = "UPDATE books_student,books SET taken_date=CURDATE(),books.quantity=quantity-1 WHERE books_student.book=" + book + " AND books_student.student=" + student + " AND books.book_id=" + book + ";"; // Фиксируем дату выдачи книги и изменяем количество экземпляров книги
                    stmt.executeUpdate(secondQuery);
                } else {
                    System.out.println("Экзмепляров данной книги в библиотеке нет");
                }
            }
        } else {
                System.out.println("Невозможно взять книгу,имеется задолженность");
            }
            try {
                stmt.close();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        try{
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        }

    public void returnBook(int student,int book) throws SQLException {
        Statement stmt = con.createStatement();
        String query="UPDATE books_student,books SET return_date=CURDATE(),books.quantity=quantity+1 WHERE books_student.student="+ student +" AND books_student.book="+ book +" AND books.book_id="+ book +";"; // Фиксируем дату возвращения книги и изменяем количество экземпляров
        stmt.executeUpdate(query);
        try {
            stmt.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void getMostPopular(String firstTime,String secondTime) throws SQLException {
        Statement stmt = con.createStatement();
        //Запрос на получение самого популярного автора за год (например,2021)
        String query = "SELECT author,author_name FROM books_author,books_student JOIN authors ON author_id WHERE books_author.book=books_student.book AND DATE(books_student.taken_date) BETWEEN '"+firstTime+"' and '"+secondTime+"' AND author=author_id group by author order by count(books_student.book) DESC LIMIT 1;";
        rs=stmt.executeQuery(query);
        if(rs.next()){
            System.out.println(rs.getString("author_name"));
        }
        try {
            stmt.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try{
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void getEvilReader() throws SQLException {
        Statement stmt = con.createStatement();
        ArrayList<Integer> list = new ArrayList<>();
        //Студент является злостным читателем, если он не сдал книгу в срок (книгу можно вернуть в течении 30 дней) несколько раз
        // Самый злостный читатель - студент не сдавший книгу в срок наибольшее число раз по сравнению с другими злостными читателями (имеет максимальное число "просрочек" по дате возвращения)
        String query = "SELECT student FROM books_student WHERE DATEDIFF(return_date,taken_date)>30;";
        rs = stmt.executeQuery(query);
        while (rs.next()) {
            list.add(rs.getInt(1));
        }
        if(list.size()==0) { // Если нет людей просрочивших срок сдачи
            System.out.println("Злостных читателей нет");

        }else {
            if (list.size() == 1) {
                String firstQuery = "SELECT student_name FROM students WHERE student_id=" + list.get(0) + ";";
                rs = stmt.executeQuery(firstQuery);
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                }

            } else {
                Collections.sort(list);
                int maxCount = Integer.MIN_VALUE;
                int prevNumber = list.get(0);
                int currentCount = 0;
                Set<Integer> results = new HashSet<>();
                for (int num : list) {
                    if (prevNumber == num)
                        currentCount++;
                    else {
                        maxCount = Math.max(currentCount, maxCount);
                        if (maxCount == currentCount)
                            results.add(prevNumber);

                        prevNumber = num;
                        currentCount = 1;
                    }
                }
                String joinedList = results.stream().map(String::valueOf).collect(Collectors.joining(","));
                String secondQuery = "SELECT student_name FROM students WHERE student_id in (" + joinedList + ");";
                rs = stmt.executeQuery(secondQuery);
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                }
            }
        }
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try{
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }





    }



}

