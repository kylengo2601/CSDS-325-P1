import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
    public static Properties loadProps() throws FileNotFoundException {
        FileInputStream propIn = new FileInputStream("E:\\Network Server\\src\\config.properties");
        Properties prop = new Properties();
        try {
            prop.load(propIn);
        }
        catch (IOException ioe) {
            System.out.println("Can't load the properties file");
            ioe.printStackTrace();
        }
        catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }

        return prop;
    }
}
