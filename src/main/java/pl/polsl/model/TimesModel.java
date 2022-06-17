package pl.polsl.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Model part of the Model-View-Controller is responsible for computing and processing values
 *  
 * @author Jacek
 * @version 1.5
 */
public class TimesModel {
    
    /** Name of the participant */
    private String name;
    
    /** Local time of the participant */
    private String localTime;
    
    /** Vector containing history of meeting dates */
    private final Vector<String> meetingDateHistory = new Vector<String>();
    
    /** Returns history of meeting dates 
     *
     * @return vector of previous meeting dates
     */
    public Vector<String> getMeetingDateHistory()
    {
        return meetingDateHistory;
    }
    
    /**
     * Returns the name of the participant
     * @return Name of the participant
     */
    public String getName() {
        return name;
    }

    /**
     * Sets participants' name
     * @param name Name of the participant
     * @throws InvalidInputException If string with name is empty
     */
    public void setName(String name) throws InvalidInputException {
        
        if(name.isEmpty())
        {
            this.name = "";
            throw new InvalidInputException("Name cannot be empty!"); 
        }
        this.name = name;
    }

    /**
     * Sets participants' local time
     * @param localTime Local time of the participant
     */
    private void setLocalTime(String localTime){
        this.localTime = localTime;
    }
    
    /**
     * Returns the local time of the participant
     * @return Local time of the participant
     */
    public String getLocalTime(){
        return localTime;
    }

    /**
     * Opens file with names and timezones and then converts each line to
     * two-element list "splitStr" where first element it the name of the
     * participant and the second is their timezone. If it can't open file it
     * throws FileNotFoundException. Then it adds this array to list "times". 
     * At the end it assigns this list to atribute called "participants".
     * 
     * @param filename path to the file
     * @return list containing info about participants
     * @throws FileNotFoundException if file with participants' data isn't found
     */
    public List<List<String>> convertFileInfo(String filename) throws FileNotFoundException {
        List<List<String>> participantsInfo = new ArrayList<>();
        //File fileObj = new File(System.getProperty("user.home")+ "/times/" + filename);
        File fileObj = new File(filename);
        Scanner myReader = new Scanner(fileObj.getAbsoluteFile());
        String data;
        List<String> stringList;

        while (myReader.hasNextLine()) {
            data = myReader.nextLine();
            stringList = Pattern.compile("\\s+").splitAsStream(data).collect(Collectors.toList());
            participantsInfo.add(stringList);
        }
        
        myReader.close();

    return participantsInfo;
    }
    /**
     * Calculates time for specific timezone relative to the time of the meeting.
     * Checks if the meeting date is correct.
     * First it creates new SimpleDateFormat then it sets timezone of the
     * meeting which is GMT+2, then it parses string "meetingDate" to newly
     * created DateFormat, and finally it offsets it by the offset which was
     * given as a parameter, and sets it as a local time for the Model class. If
     * date is empty will handle exeption.
     *
     * @param offset Time difference between the timezone of the participant and
     * the timezone of the meeting
     * @param meetingDate Date of the meeting without offset
     * 
     * @throws ParseException If date of the meeting is in incorrect format
     * @throws InvalidInputException If the date or hour is incorrect or empty
     */
    public void calculateTime(String offset, String meetingDate) throws ParseException, InvalidInputException {
        Pattern DATE_PATTERN = Pattern
                .compile("^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]) (2[0-3]|[01]?[0-9]):([0-5]?[0-9]):([0-5]?[0-9])$");
        
        if(meetingDate.isBlank() || meetingDate.isEmpty())
            throw new InvalidInputException("Date was empty!");
   
        if(!DATE_PATTERN.matcher(meetingDate).matches() )
            throw new InvalidInputException("Date was invalid! Please use yyyy-MM-dd HH:mm:ss format");
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        Date date = df.parse(meetingDate);
        df.setTimeZone(TimeZone.getTimeZone(offset));
        String time = df.format(date);
        this.setLocalTime(time);
    }
    
    /** Adds date to date history
     * @param meetingDate date of the actual meeting
     */
    public void addDateToHisotry(String meetingDate)
    {
        meetingDateHistory.add(0, meetingDate);
    }
    
    /**
     * Checks if appropriate tables exist if they don't
     * it creates them, otherwise it does nothing
     * 
     * @param con Connection to database
     * @throws SQLException if sql statement is invalid
     */
    public void createTables(Connection con) throws SQLException 
    {
        DatabaseMetaData dbm = con.getMetaData();
        ResultSet tableParticipants = dbm.getTables(null, null, "PARTICIPANTS", new String[] {"TABLE"});
        ResultSet tableTimes = dbm.getTables(null, null, "TIMES", new String[] {"TABLE"});

        if (tableParticipants.next() && tableTimes.next()) 
        {
            return;
        }
        else 
        {
            Statement statement = con.createStatement();
            statement.executeUpdate("CREATE TABLE Participants "
                    + "(id INTEGER NOT NULL PRIMARY KEY "
                    + "GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + "name VARCHAR(50))");

            statement.executeUpdate("CREATE TABLE Times "
                    + "(id INTEGER NOT NULL PRIMARY KEY "
                    + "GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + "time VARCHAR(50))");

            statement.executeUpdate("Alter Table APP.Participants " 
                    + "Add FOREIGN KEY (ID) " 
                    + "References APP.Times (ID)");
        }
    }
    
    /**
     * Inserts data to TIMES and PARTICIPANT tables
     * 
     *
     * @param con Connection to database
     * @param name of the participant
     * @param date  their local time
     * @throws SQLException when sql statement is invalid
     */
    public void insertToDB(Connection con, String name, String date) throws SQLException
    {
       Statement statement = con.createStatement();

       statement.executeUpdate("INSERT INTO APP.TIMES (TIME) VALUES ('"+date+"')");
       statement.executeUpdate("INSERT INTO APP.PARTICIPANTS (NAME) VALUES ('"+name+"')");
    }
}
