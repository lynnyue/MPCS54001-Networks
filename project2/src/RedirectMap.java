import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class RedirectMap extends HashMap<String, String> {

    // constructor of the RedirectMap class
    public RedirectMap(String fileName) throws IOException{

        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        String line;
        // read the contents in the special file
        // put the two urls as value/key pair and put them in a map
        while ((line = bufferedReader.readLine()) != null) {
            String[] tmp = line.split(" ");
            this.put(tmp[0], tmp[1]);
        }
    }
}
