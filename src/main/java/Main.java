import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        Scanner credentialsFile = new Scanner(new File("credentials.txt"));
        String userName = credentialsFile.nextLine();
        String pass = credentialsFile.nextLine();

        int rc = login(userName, pass);
        System.out.println(rc);

        // Referrer: "https://mywebsis.utrgv.edu/PROD/twbkwbis.P_GenMenu?name=bmenu.P_RegMnu"
    }

    private static int login(String userName, String pass) throws IOException {
        HttpsURLConnection connection
                = (HttpsURLConnection) (new URL("https://mywebsis.utrgv.edu/PROD/twbkwbis.P_ValLogin")).openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // We get a 403 if we don't set this
        connection.setRequestProperty("Referer", "https://mywebsis.utrgv.edu/PROD/twbkwbis.P_WWWLogin");

        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        String data = "sid=" + URLEncoder.encode(userName, "UTF-8") + "&PIN=" + URLEncoder.encode(pass, "UTF-8");
        wr.write(data);
        wr.flush();
        wr.close();

        return connection.getResponseCode();
    }
}
