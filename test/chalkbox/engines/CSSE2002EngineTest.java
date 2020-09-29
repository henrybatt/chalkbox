package chalkbox.engines;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class CSSE2002EngineTest {
    private static final String BASE_FOLDER = "./test/resources/csse2002/";

    private static final String CONFIG_BASIC_PATH = BASE_FOLDER + "config-basic.yml";
    private static final String CONFIG_CHECKSTYLE_PATH = BASE_FOLDER + "config-checkstyle.yml";
    private static final String RESULTS_PATH = BASE_FOLDER + "results/results.json";
    private static final String EXPECTED_BASIC_PATH = BASE_FOLDER + "results/expected-basic.json";

    @Test
    public void testEngineBasic() throws IOException {
        Engine engine = null;
        try {
            engine = EngineLoader.load(CONFIG_BASIC_PATH);
        } catch (ConfigFormatException e) {
            fail("Configuration file should not be invalid");
        }
        engine.run();

        String expected = Files.readString(Paths.get(EXPECTED_BASIC_PATH),
                StandardCharsets.UTF_8);
        String actual = Files.readString(Paths.get(RESULTS_PATH),
                StandardCharsets.UTF_8);
        System.err.println("expected: " + expected);
        System.err.println("actual: " + actual);
        assertEquals("Output JSON files are different", expected, actual);
    }

    @Test
    public void testEngineWithCheckstyle() throws IOException {
        Engine engine = null;
        try {
            engine = EngineLoader.load(CONFIG_CHECKSTYLE_PATH);
        } catch (ConfigFormatException e) {
            fail("Configuration file should not be invalid");
        }
        engine.run();

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new BufferedReader(new FileReader(RESULTS_PATH)));
            JSONObject chalkboxOutput = (JSONObject) obj;
            JSONArray tests = (JSONArray) chalkboxOutput.get("tests");
            JSONObject checkstyleTest = null;
            for (Object test : tests) {
                JSONObject jsonObject = (JSONObject) test;
                String name = (String) jsonObject.get("name");
                if (name.equals("Automated Style")) {
                    checkstyleTest = jsonObject;
                }
            }
            if (checkstyleTest == null) {
                fail("Could not find Checkstyle test result");
            }
            String output = (String) checkstyleTest.get("output");
            assertFalse(output.isBlank());
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
    }
}
