import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JavaHTTPServer implements Runnable{
    static FileInputStream propIn;
    static {
        try {
            propIn = new FileInputStream("E:\\Network Server\\src\\config.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static Properties prop = new Properties();
    static {
        try {
            prop.load(propIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static final File WEB_ROOT = new File(prop.getProperty("WEB_ROOT"));
    static final String FILE_NOT_FOUND = prop.getProperty("FILE_NOT_FOUND");
    static final int PORT = Integer.parseInt(prop.getProperty("PORT"));   // port to listen connection



    // verbose mode
    static final boolean verbose = true;

    // Client Connection via Socket Class
    private Socket connect;



    public JavaHTTPServer(Socket c) throws IOException {
        connect = c;
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        }
        finally {
            if (fileIn != null) fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from Kyle N.");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (verbose)
            System.out.println("File " + fileRequested + " not found");
    }

    @Override
    public void run() {
        // manage the particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));  // read characters from client via input stream on socket
            out = new PrintWriter(connect.getOutputStream());  // get character output stream to client (for headers)
            dataOut = new BufferedOutputStream(connect.getOutputStream());  // get binary output stream to client (for requested data)


            //get file requested and HTTP method
            String input = in.readLine();// get first line of the request from client
            StringTokenizer parse = new StringTokenizer(input);  // parse the request with string tokenizer
            String method = parse.nextToken().toUpperCase(); // get the HTTP method of client
            fileRequested = parse.nextToken().toLowerCase();  // get file requested
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            in.readLine();
            String cookieLine = in.readLine();
            String[] cookieArr = cookieLine.split("; ");

            //get the cookie (yum)
            boolean hasCookie = false;
            String cookieVal = "1";
            Pattern pattern = Pattern.compile("time_visited=");
            int valStart = cookieArr[2].indexOf('=') + 1;
            int valEnd = cookieArr[2].length();
            String currCookie = cookieArr[2].substring(valStart, valEnd);
            //String currCookie = cookieVal;
//            while((cookieLine = in.readLine()) != null) {
//                Matcher matcher = pattern.matcher(cookieLine);
//                if(matcher.find()) {
//                    hasCookie = true;
//                    StringTokenizer strTk = new StringTokenizer(cookieLine);
//                    strTk.nextToken();
//
//                    String cookieName = strTk.nextToken();
//
//                    int valStart = cookieName.indexOf('=');
//                    int valEnd = cookieName.indexOf(';');
//                    cookieVal += cookieName.substring(valStart, valEnd);
//                }
//            }

//            if(!fileRequested.equals("/visits.html")) {
//                File file = new File(WEB_ROOT, fileRequested);
//                int fileLength = (int) file.length();
//                String content = getContentType(fileRequested);
//
//                if (method.equals("GET")) { // GET method so send content
//                    byte[] fileData = readFileData(file, fileLength);
//
//                    // send HTTP Headers
//                    out.println("HTTP/1.1 200 OK");
//                    out.println("Server: Java HTTP Server from Kyle N.");
//                    out.println("Date: " + new Date());
//                    out.println("Content-type: " + content);
//                    out.println("Content-length: " + fileLength);
//                    if(!hasCookie) {
//                        out.println("Set-Cookie: time_visited=1; " +
//                                "Expires=Wed, 25 Nov 2020 07:28:00 GMT; " +
//                                "Domain=eecslab-10.case.edu; " +
//                                "Path=/ktn27");
//                    }
//                    else {
//                        String visit_count = Integer.toString(Integer.parseInt(cookieVal) + 1);
//                        System.out.println(visit_count);
//                        out.println("Set-cookie: time_visited=" + visit_count + "; " +
//                                "Expires=Wed, 25, Nov 2020 07:28:00 GMT; " +
//                                "Domain=eecslab-10.case.edu; " +
//                                "Path=/ktn27");
//                    }
//
//                    out.println();
//                    out.flush();
//
//                    dataOut.write(fileData, 0, fileLength);
//                    dataOut.flush();
//                }
//
//                if (verbose) {
//                    System.out.println("File " + fileRequested + " of type " + content + " sent");
//                }
//            }
//            else {
//                if (method.equals("GET")) { // GET method so send content
//
//                    // send HTTP Headers
//                    out.println("HTTP/1.1 200 OK");
//                    out.println("Server: Java HTTP Server from Kyle N.");
//                    out.println("Date: " + new Date());
//                    out.println("Content-type: text/html");
//                    if (!hasCookie) {
//                        out.println("Set-Cookie: time_visited=1; " +
//                                "Expires=Wed, 25 Nov 2020 07:28:00 GMT; " +
//                                "Domain=eecslab-10.case.edu; " +
//                                "Path=/ktn27");
//                    } else {
//                        String visit_count = Integer.toString(Integer.parseInt(cookieVal) + 1);
//                        System.out.println(visit_count);
//                        out.println("Set-cookie: time_visited=" + visit_count + "; " +
//                                "Expires=Wed, 25, Nov 2020 07:28:00 GMT; " +
//                                "Domain=eecslab-10.case.edu; " +
//                                "Path=/ktn27");
//                    }
//
//                    out.println();
//                    out.flush();
//
////                    String visitCounter =
////                            "<!DOCTYPE html>\n" +
////                                    "<html lang=\"en\">\n" +
////                                    "<head>\n" +
////                                    "<meta charset=\"UTF-8\">\n" +
////                                    "<title>Visit count</title>\n" +
////                                    "</head>\n" +
////                                    "<body>\n" +
////                                    "<p>Your browser visited various URLs on this site " + visit_count + " times</p>\n" +
////                                    "</body>\n" +
////                                    "</html>";
//                }
//            }

            File file = new File(WEB_ROOT, fileRequested);
            int fileLength = (int) file.length();
            String content = getContentType(fileRequested);

            if (method.equals("GET")) { // GET method so send content
                byte[] fileData = readFileData(file, fileLength);

                // send HTTP Headers
                out.println("HTTP/1.1 200 OK");
                out.println("Server: Java HTTP Server from Kyle N.");
                out.println("Date: " + new Date());
                out.println("Content-type: " + content);
                out.println("Content-length: " + fileLength);
                out.println("Set-Cookie: time_visited=" + currCookie + "; " +
                        "Expires=Wed Dec 2, 2020;");

                out.println();
                out.flush();

                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            }

            if (verbose) {
                System.out.println("File " + fileRequested + " of type " + content + " sent");
            }


        }
        catch (FileNotFoundException fnfe) {
            try {
                fileNotFound(out, dataOut, fileRequested);
            }
            catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }

        }
        catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        }
        finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection
            }
            catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (verbose)
                System.out.println("Connection closed.\n");
        }

    }

    public static void main(String[] args) {
        try {
            ServerSocket serverConnect = new ServerSocket(JavaHTTPServer.PORT);
            System.out.println("My beautiful server started.\nYou'd better make connections on port : " + PORT + " ...\n");

            // we listen until user halts server execution
            while (true) {
                JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());

                if (verbose) {
                    System.out.println("Connection opened. (" + new Date() + ")");
                }

                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }

        }
        catch (IOException e) { System.err.println("Server Connection Error : " + e.getMessage()); }
    }
}
