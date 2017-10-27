import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static Pattern classTimeSlotPattern
            = Pattern.compile("(\\S+) (\\d+)-(\\d+)<br>(\\d+)\\D+<br>(\\d+:\\d+\\s+.m)-(\\d+:\\d+\\s+.m)<br>" +
            "(\\S+\\s+\\S+)");

    public static void main(String[] args) throws IOException {
        String userName;
        String pass;
        try (Scanner credentialsFile = new Scanner(new File("credentials.txt"))) {
            userName = credentialsFile.nextLine();
            pass = credentialsFile.nextLine();
        }

        Connection.Response loginResponse = Jsoup.connect("https://mywebsis.utrgv.edu/PROD/twbkwbis.P_ValLogin")
                .referrer("https://mywebsis.utrgv.edu/PROD/twbkwbis.P_WWWLogin")
                .data("sid", userName)
                .data("PIN", pass)

                // The server checks this to see if cookies are enabled. We can't log on without this.
                .cookie("TESTID", "set")

                .method(Connection.Method.POST)
                .execute();

        Document scheduleDocument = Jsoup.connect("https://mywebsis.utrgv.edu/PROD/bwskfshd.P_CrseSchd")
                .referrer("https://mywebsis.utrgv.edu/PROD/twbkwbis.P_GenMenu?name=bmenu.P_RegMnu")
                .cookies(loginResponse.cookies())
                .get();

        Elements scheduleElements = scheduleDocument.getElementsByAttributeValueStarting("href", "/PROD/bwskfshd" +
                ".P_CrseSchdDetl");

        ArrayList<ClassTimeSlot> classTimeSlots = new ArrayList<>();
        for (Element e : scheduleElements) {
            int dayId = e.parent().elementSiblingIndex();

            Matcher matcher = classTimeSlotPattern.matcher(e.html());

            System.out.print(e.html() + "\n\n");
        }
    }
}
