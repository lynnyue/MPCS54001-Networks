// print out header
// 1. status code
// 2. connection status
// 3. Date and time
// 4. Name of the server
// 5. last-modified time
// 6. content-length -- number of bytes in the object being sent
// add content length in the HTTPServer file
// 7. content-type

import java.io.IOException;
import java.util.Date;

public class HTTPHeader {
    // initiate an empty string
    String header = " ";
    // constructor of the class
    public HTTPHeader (int code, MIME fileType, String url) throws IOException {
        // generate date
        Date date = new Date();
        // generate status code -- first line
        switch (code) {
            case 200:
                header += "200 OK" + "\r\n";
                break;
            case 301:
                header += "301 Moved Permanently\r\n";
                header += "Location: " + url + "\r\n";
                break;
            case 400:
                header += "400 Bad Request + \"\\r\\n\"";
                break;
            case 403:
                header += "403 Forbidden" + "\r\n";
                break;
            case 404:
                header += "404 Not Found" + "\r\n";
                break;
        }
        // connection status -- second line
        header += "Connection: close\r\n";
        // date and time -- third line
        header += "Date: " + date + "\r\n";
        // generate file type from fileType input
        switch (fileType) {
            case NA:
                break;
            case TXT:
                header += "Content-Type: text/plain\r\n";
            case HTML:
                header += "Content-Type: text/html\r\n";
                break;
            case JPEG:
                header += "Content-Type: image/jpeg\r\n";
                break;
            case PNG:
                header += "Content-Type: image/png\r\n";
                break;
            case PDF:
                header += "Content-Type: application/pdf\r\n";
        }
        header += "\r\n";
        System.out.println("HTTP/1.1" + header);
    }

    public String getHeader() {
        return header;
    }
}
