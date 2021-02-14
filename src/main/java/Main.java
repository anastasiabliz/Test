import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Main {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Library library = new Library();
        //library.getBook(1,2);
        //library.returnBook(1,2);
        //library.getMostPopular("2021-01-01","2021-12-31");
        library.getEvilReader();
        //library.closeConnection();
    }
}










