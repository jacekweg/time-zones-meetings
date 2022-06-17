package pl.polsl.controller;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import pl.polsl.model.TimesModel;
import pl.polsl.model.InvalidInputException;
import pl.polsl.view.TimesView;

/**
 *
 * Servlet is responsible for acces to the computational part of the model
 *
 * @author Jacek
 * @version 1.5
 */
@WebServlet("/Form")
public class TimesController extends HttpServlet {

    /**
     * View object instance
     */
    private final TimesView view = new TimesView();
    /**
     * Instance of TimesModel class
     */
    private final TimesModel model = new TimesModel();

    /**
     * Date of meeting in format "yyyy-MM-dd HH:mm:ss"
     */
    private String meetingDate;

    /**
     * List of two element arrays where first one is the name of the participant
     * and the second is his timezone
     */
    private List<List<String>> participants;

    /**
     * Name of the file with participants names and their timezones
     */
    private String filePath;

    /**
     * Processes data provided by the user, processes it and passes on to the
     * DataHistoryController
     *
     * @param request servlet request
     * @param response servlet response
     * @throws FileNotFoundException if the file provided by the user wasnt
     * found
     * @throws ServletException if something is wrong with servlet
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, ServletException, IOException {

        response.setContentType("text/html; charset=ISO-8859-2");

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
        } catch (ClassNotFoundException cnfe) {
            view.printError(response, cnfe.getMessage());
        }

        Connection con = null;
        try (PrintWriter out = response.getWriter()) {
            HttpSession session = request.getSession();
            filePath = request.getParameter("filename");
            meetingDate = request.getParameter("meetingdate");

            if (filePath.length() <= 0 || meetingDate.length() <= 0) {
                view.printError(response, "You should provide both values !");
                return;
            }
            List<List<String>> participantInfo = null;
            try {
                con = DriverManager.getConnection("jdbc:derby://localhost:1527/TimeZones", "app", "app");
                participantInfo = getParticipantInfo(filePath, meetingDate);
            } catch (SQLException | InvalidInputException ex) {
                view.printError(response, ex.getMessage());
            } catch (ParseException ex) {
                view.printError(response, "Incorrect date!");
            }

            if (participantInfo != null) {
                session.setAttribute("con", con);
                session.setAttribute("participantInfo", participantInfo);
                session.setAttribute("model", model);
                session.setAttribute("filename", filePath);
                session.setAttribute("date", meetingDate);
                request.getRequestDispatcher("/DataHistoryController").forward(request, response);
            }
        } catch (IOException ex) {}
    }

    /**
     * Reasds file path and meeting date and returns processed data in List
     * format
     *
     * @param filePath path to the file
     * @param meetingDate date of the meeting
     * @return List of participants names and their local time
     * @throws FileNotFoundException if file with participants' data isn't found
     * @throws InvalidInputException if input data was incorrect
     * @throws ParseException if date of the meeting couldn't be parsed
     */
    @SuppressWarnings("empty-statement")
    public List<List<String>> getParticipantInfo(String filePath, String meetingDate) throws FileNotFoundException, InvalidInputException, ParseException {

        this.filePath = filePath;
        this.meetingDate = meetingDate;
        this.execute();

        List<List<String>> correctedParticipants = new ArrayList<>();;
        for (List<String> participant : participants) {
            List<String> auxiliary = new ArrayList<>();
            this.setParticipantName(participant.get(0));
            auxiliary.add(model.getName());
            this.calculateTime(participant.get(1));
            auxiliary.add(model.getLocalTime());
            correctedParticipants.add(auxiliary);
        }

        return correctedParticipants;
    }

    /**
     * Processes the data from text file to list, then for each element of this
     * list calls method "setParticipantName" (for the first elemnt in the list)
     * and "calculateTime" (for the second elemnt in the list). It also counts
     * participants, because if a given participant would have incorrect data,
     * it would be easier to indentify where it is, because number of the will
     * be shown in the error window
     *
     * @throws FileNotFoundException if file with participants' data isn't found
     * @throws InvalidInputException if input data was incorrect
     * @throws ParseException if date of the meeting couldn't be parsed
     */
    private void execute() throws FileNotFoundException, InvalidInputException, ParseException {

        int participantNumber = 0;

        this.getTimes();
        for (List<String> participant : participants) {
            try {
                ++participantNumber;
                this.setParticipantName(participant.get(0));
                this.calculateTime(participant.get(1));
            } catch (IndexOutOfBoundsException e) {
                throw new InvalidInputException("Participants info was incorrect!");
            }
        }
    }

    /**
     * Calculates time for specific timezone relative to the time of the meeting
     * First it creates new SimpleDateFormat then it sets timezone of the
     * meeting which is GMT+2, then it parses string "meetingDate" to newly
     * created DateFormat, and finally it offsets it by the offset which was
     * given as a parameter, and sets it as a local time for the Model class.If
     * date is empty will handle exeption.
     *
     * @param offset Time difference between the timezone of the participant and
     * the timezone of the meeting
     * @throws ParseException if date of the meeting couldn't be parsed
     * @throws InvalidInputException if input data was incorrect
     */
    private void calculateTime(String offset) throws ParseException, InvalidInputException {
        model.calculateTime(offset, meetingDate);
    }

    /**
     * Sets name of the participant in the Model class
     *
     * @param name Name of the participant
     * @throws InvalidInputException if input data was incorrect
     */
    private void setParticipantName(String name) throws InvalidInputException {
        model.setName(name);
    }

    /**
     * Opens file with names and timezones and then converts each line to
     * two-element array "splitStr" where first element it the name of the
     * participant and the second is their timezone.If it can't open file it
     * throws FileNotFoundException. Then it adds this array to list "times". At
     * the end it assigns this list to atribute called "participants".
     *
     * @throws FileNotFoundException if the file isn't found
     */
    private void getTimes() throws FileNotFoundException {
        participants = model.convertFileInfo(filePath);
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
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
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
