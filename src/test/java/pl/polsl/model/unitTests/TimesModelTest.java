package pl.polsl.model.unitTests;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import pl.polsl.model.InvalidInputException;
import pl.polsl.model.TimesModel;

/**
 * Class used to test model functionalities
 *
 * @author Jacek
 * @version 1.5
 */
public class TimesModelTest {
   /**
    * Object used to test model class
    */
    private TimesModel model;

   /**
    * Function creates new model instance before
    * every test
    */
    @BeforeEach
    public void setUp() {
        model = new TimesModel();
    }
     
    /**
     * Tests whether exception will be thrown for 
     * incorrect values passed to the function
     * 
     * @param input Offset of the date
     * @param inputSecond Date of the meeting
     */
    @ParameterizedTest
    @CsvSource({"GMT+1,''","GMT+2,' '", "GMT-9,9999-99-99 99:99:99", "GMT+2,0-0-0 00:00:00", "GMT+5,2021/11/15 15:30:00"})
    public void testCalculateTimeIncorrect(String input, String inputSecond) {
        try {
            model.calculateTime(input, inputSecond);
            fail("calculateTime should throw an exception");
        } catch (ParseException | InvalidInputException e) {}
        
    }
    /**
     * Tests whether exception won't be thrown for 
     * correct values passed to the function and
     * whether date will be correctly offsetted
     * @param input Offset of the date
     * @param expected outup after using calculateTime function
     */
    @ParameterizedTest
    @CsvSource({"GMT+1,2021-11-15 14:30:00", "GMT-9,2021-11-15 04:30:00", "GMT+2,2021-11-15 15:30:00"})
    public void testCalculateTimeParameterized(String input, String expected) {
        String date = "2021-11-15 15:30:00";
        try {
            model.calculateTime(input, date);
        } catch (ParseException | InvalidInputException ex) {
            fail("CalculateTime shouldn't throw an exception");
        }
        assertEquals(expected, model.getLocalTime());
    }

    /**
     * Test checks if the FileNotFoundException will be
     * thrown for nonexistent files
     * @param input Incorrect name of the file
     */
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "nonexistent.txt", "nonsense"})
    public void testConvertFileInfoIncorrect(String input) {
        try {
            model.convertFileInfo(input);
            fail("ConvertFileInfo should throw an exception");
        } catch (FileNotFoundException e) {}
    }

    /**
     * Test checks if the function will convert data
     * from test file into correct ArrayList
     */
    @Test
    public void testConvertFileInfoCorrect() {
        List<List<String>> expected = new ArrayList<>();
        List<String> auxiliary = new ArrayList<>();
        auxiliary.add("Test");
        auxiliary.add("GMT+1");
        expected.add(auxiliary);
        try {
            List<List<String>> actualValue = model.convertFileInfo(System.getProperty("user.home")+"/times/test.txt");
            assertEquals(expected, actualValue, "Tested values are not equal even though they should be");
        } catch (FileNotFoundException e) {
            fail("ConvertFileInfo shouldn't throw an exception");
        }
    }     
    /**
     * Tests checks if the meeting dates are added correctly to the vector
     */
    @Test
    public void addMeetingHistory() {
        Vector<String> test = new Vector<String>();
        test.add(0, "2022-01-03 15:30:00");
        test.add(0, "2022-01-04 15:30:00");
        test.add(0, "2022-01-05 15:30:00");
        model.addDateToHisotry("2022-01-03 15:30:00");
        model.addDateToHisotry("2022-01-04 15:30:00");
        model.addDateToHisotry("2022-01-05 15:30:00");
        
        assertEquals(test, model.getMeetingDateHistory(), "Tested values are not equal even though they should be");
    }
        


}
