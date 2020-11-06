

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JavaHTTPServer implements Runnable{
    static FileInputStream propIn;
    static {
        try {
            propIn = new FileInputStream("ktn27\\home\\CSDS-325\\src\\config.properties");
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
    //static final String FILE_NOT_FOUND = "/ktn27/404.html";
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
        File file = new File(WEB_ROOT, "/ktn27/404.html");
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

            //get the cookie (yum)
            boolean hasCookie = false;
            String cookieVal = "";
            Pattern pattern = Pattern.compile("time_visited");
            StringBuilder sb = new StringBuilder();
            while(sb.append(in.readLine()).length() > 0) {
                String currLine = sb.toString();
                Matcher matcher = pattern.matcher(currLine);
                if(matcher.find()) {
                    hasCookie = true;

                    System.out.println(currLine);
                    int valStart = currLine.indexOf("time_visited=") + 13;
                    String newCookieVal = "";
                    int currPos = valStart;
                    while(currPos < currLine.length() && currLine.charAt(currPos) != ';'){
                        newCookieVal += currLine.charAt(currPos);
                        currPos++;
                    }
                    cookieVal += (Integer.parseInt(newCookieVal) + 1);
                }
                sb.delete(0, sb.length());
            }


            File file = new File(WEB_ROOT, fileRequested);
            int fileLength = (int) file.length();
            String content = getContentType(fileRequested);
            boolean visitCount = fileRequested.equals("/ktn27/visits.html");

            if (method.equals("GET")) { // GET method so send content
                byte[] fileData = readFileData(file, fileLength);

                // send HTTP Headers
                out.println("HTTP/1.1 200 OK");
                out.println("Server: Java HTTP Server from Kyle N.");
                out.println("Date: " + new Date());
                out.println("Content-type: " + content);
                out.println("Content-length: " + fileLength);
                if(hasCookie) {
                    int increaseCount = Integer.parseInt(cookieVal) + 1;
                    out.println("Set-Cookie: time_visited=" + cookieVal + "; " +
                            "Expires=Wed Dec 2, 2020;");
                }
                else {
                    out.println("Set-Cookie: time_visited=1; " +
                            "Expires=Wed Dec 2, 2020;");
                }
//                out.println("Set-Cookie: time_visited=1; " +
//                        "Expires=Wed Dec 2, 2020;");
                out.println();
                out.flush();

                if(fileRequested.equals("/ktn27/visits.html")) {
                    int increaseCount = Integer.parseInt(cookieVal) + 1;
                    try{
                        FileWriter visitFileWriter = new FileWriter(WEB_ROOT + fileRequested);
                        visitFileWriter.write("<!DOCTYPE html>\n" +
                                "<html lang=\"en\">\n" +
                                "<head>\n" +
                                "<meta charset=\"UTF-8\">\n" +
                                "<title>Visit count</title>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "<p id=\"content\">Your browser visited various URLs on this site " + increaseCount + " times.</p>\n" +
                                "</body>\n" +
                                "</html>\n"
                                );
                        visitFileWriter.close();
                    } catch (IOException e) {
                        System.out.println("An error occured.");
                        e.printStackTrace();
                    }

                    int newFileLength = (int) file.length();
                    byte[] visitsFile = readFileData(file, newFileLength);
                    dataOut.write(visitsFile, 0, newFileLength);
                }
                else {
                    dataOut.write(fileData, 0, fileLength);
                }
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
