package pl.polsl.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.servlet.http.HttpServletResponse;

/**
 * The view part of the Model-View-Controller design patter. It represents the
 * visualization of the data that model contains.
 *
 * @author Jacek
 * @version 1.5
 */
public class TimesView {

    /**
     * Method prints header for HTML website
     *
     * @param out PrintWriter used to output HTML
     */
    public void printHeader(PrintWriter out) {
        out.println("<html>\n"
                + "<head>\n"
                + "<style>\n"
                + "table, th, td {\n"
                + "border:1px solid black;\n"
                + "}\n"
                + "</style>\n"
                + "<title>Time Zones</title>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "</head>\n"
                + "<body>\n"
                + "<h1>Local times for participants</h1>\n");
    }

    /**
     * Method prints footer for HTML website
     *
     * @param out PrintWriter used to output HTML
     */
    public void printFooter(PrintWriter out) {
        out.println("</body>\n"
                + "</html>");
    }

    /**
     * Method prints out history of previous meeting date in HTML
     *
     * @param out PrintWriter used to output HTML
     * @param dates previous meeting dates
     */
    public void printHistory(PrintWriter out, Vector<String> dates) {
        if (dates.size() != 0) {
            out.print("<h2>Previous meeting dates (for GMT+2):</h2>");
            for (String date : dates) {
                out.print(date + "<br>");
            }
        }
    }

    /**
     * Prints out info about participant to website
     *
     * @param out PrintWriter used to output HTML
     * @param info information about participant
     */
    public void printParticipantInfo(PrintWriter out, String... info) {
        out.println("<p>"
                + "Name: " + info[0]
                + "<br> "
                + "Local time: " + info[1]
                + "</p>\n");
    }

    /**
     * Prints info about error
     *
     * @param response servlet respone
     * @param message error message
     * @throws java.io.IOException when input is wrong
     */
    public void printError(HttpServletResponse response, String message) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
    }

    /**
     * Prints info about number of participants at the last meeting
     *
     * @param out PrintWriter used to output HTML
     * @param lastNumber number of participants at the last meeting
     */
    public void printNumberOfParticipants(PrintWriter out, String lastNumber) {
        if (lastNumber.equals("1")) {
            out.println("<p>There was <b>" + lastNumber + "</b> participant at the last meeting</p>");
        } else {
            out.println("<p>There were <b>" + lastNumber + "</b> participants at the last meeting</p>");
        }
    }

    /**
     * Prints out info about current meeting date in GMT+2 timezone
     *
     * @param out PrintWriter used to output HTML
     * @param newMeetingDate current meeting date
     */
    public void printCurrentMeeting(PrintWriter out, String newMeetingDate) {
        out.print("<h2>Current meeting date (for GMT+2): </h2>" + newMeetingDate);
    }

    /**
     * Prints data from database to the user in table format 
     * 
     * @param out PrintWriter used to output HTML
     * @param con Connection to database
     * @throws SQLException when sql statement is incorrect
     */
    public void printDB(PrintWriter out, Connection con) throws SQLException {
        Statement statement = con.createStatement();

        ResultSet rs = statement.executeQuery("SELECT * FROM PARTICIPANTS NATURAL JOIN TIMES");

        while (rs.next()) {
            out.print("<tr>\n"
                    + "<td>" + rs.getInt("ID") + "  </td>\n"
                    + "<td>" + rs.getString("NAME") + "</td>\n"
                    + "<td>" + rs.getString("TIME") + "</td>\n"
                    + "</tr>\n");
        }
        rs.close();
    }

    /**
     *  Prints header of the HTML table
     * 
     * @param out PrintWriter used to output HTML
     */
    public void printTableHeader(PrintWriter out) {
        out.print("<h2> Database data:</h2>");
        out.print("\n<table>\n"
                + "<tr>\n"
                + " <th>ID</th>\n"
                + " <th>Name</th>\n"
                + " <th>Time</th>\n"
                + "</tr>\n");
    }
    
    /**
     * Prints footer of the HTML table
     * 
     * @param out PrintWriter used to output HTML
     */
    public void printTableFooter(PrintWriter out){
        out.print("</table>\n");
    }
}
