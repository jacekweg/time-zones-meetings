package pl.polsl.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import pl.polsl.model.TimesModel;
import pl.polsl.view.TimesView;

/**
 * Servlet is responsible for acces to the history of meetings
 *
 * @author Jacek
 * @version 1.5
 */
@WebServlet("/DataHistoryController")
public class DataHistoryController extends HttpServlet {

    /**
     * View class responsible for printing out info the end user
     */
    private final TimesView view = new TimesView();

    /**
     * Method processes cookies, which contains number of participants at the
     * last meeting
     *
     * @param out PrintWriter used to output HTML
     * @param request servlet request
     * @param response servlet response
     * @param numberOfParticipants number of participants at the current meeting
     */
    private void processCookies(PrintWriter out, HttpServletRequest request, HttpServletResponse response, String numberOfParticipants) {
        Cookie[] cookies = request.getCookies();
        String oldNumber = "";
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lastNum")) {
                    oldNumber = cookie.getValue();
                }
            }
        }
        if (!"".equals(oldNumber)) {
            view.printNumberOfParticipants(out, oldNumber);
        }
        Cookie newNumber = new Cookie("lastNum", numberOfParticipants);
        newNumber.setMaxAge(60 * 60 * 24);
        response.addCookie(newNumber);
    }

    /**
     * Processes requests for both HTTP GET and POST
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            view.printHeader(out);
            HttpSession session = request.getSession();
            List<List<String>> participantInfo = (List<List<String>>) session.getAttribute("participantInfo");
            int numberOfParticipants = participantInfo.size();

            TimesModel model = (TimesModel) session.getAttribute("model");
            String meetingdate = (String) session.getAttribute("date");
            Connection con = (Connection) session.getAttribute("con");

            participantInfo.forEach((List<String> data) -> {
                view.printParticipantInfo(out, data.get(0), data.get(1));
            });

            try {
                model.createTables(con);
                participantInfo.forEach((List<String> data) -> {
                    try {
                        model.insertToDB(con, data.get(0), data.get(1));
                    } catch (SQLException sqle) {}
                });
            } catch (SQLException ex) {
                view.printError(response, ex.getMessage());
            }

            view.printCurrentMeeting(out, meetingdate);
            processCookies(out, request, response, numberOfParticipants + "");
            view.printHistory(out, model.getMeetingDateHistory());

            model.addDateToHisotry(meetingdate);

            view.printTableHeader(out);
            try {
                view.printDB(out, con);
            } catch (SQLException ex) {
                view.printError(response, ex.getMessage());
            }
            view.printTableFooter(out);

            view.printFooter(out);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
