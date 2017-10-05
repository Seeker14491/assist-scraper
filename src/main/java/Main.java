import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static Pattern classTimeSlotPattern
            = Pattern.compile("(\\S+)\\s+(\\d+)-(\\d+)\\s+(\\d+)\\D+(\\d+:\\d+\\s+.m)-(\\d+:\\d+\\s+.m)\\s+(\\S+\\s+\\S+)");

    public static void main(String[] args) throws IOException {
        Scanner credentialsFile = new Scanner(new File("credentials.txt"));
        String userName = credentialsFile.nextLine();
        String pass = credentialsFile.nextLine();

        List<ClassTimeSlot> schedule = getClassTimeSlots(userName, pass);

        for (ClassTimeSlot c : schedule) {
            System.out.println(c.subject + " " + c.course);
        }
    }

    private static List<ClassTimeSlot> getClassTimeSlots(String userName, String pass) throws IOException {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        HtmlPage loginPage = client.getPage("https://mywebsis.utrgv.edu");

        HtmlForm loginForm = loginPage.getForms().get(0);

        HtmlTextInput usernameInput = loginForm.getInputByName("sid");
        HtmlPasswordInput passInput = loginForm.getInputByName("PIN");
        HtmlSubmitInput loginButton = loginForm.getInputByValue("Login");

        usernameInput.setValueAttribute(userName);
        passInput.setValueAttribute(pass);

        HtmlPage mainMenu = loginButton.click();
        HtmlPage studentServices = mainMenu.getAnchorByText("Student Services").click();
        HtmlPage registration = studentServices.getAnchorByText("Registration").click();
        HtmlPage weekAtAGlance = registration.getAnchorByText("Week at a Glance").click();

        List<HtmlAnchor> classes = weekAtAGlance.getAnchors();

        ArrayList<ClassTimeSlot> classTimeSlots = new ArrayList<>();
        for (HtmlAnchor c : classes) {
            if (c.getHrefAttribute().startsWith("/PROD/bwskfshd.P_CrseSchdDetl")) {
                // Subtracting 1 then dividing by 2 is needed for some reason. 0 -> Monday, 1 -> Tuesday ...
                int dayId = (c.getParentNode().getIndex() - 1) / 2;

                Matcher matcher = classTimeSlotPattern.matcher(c.asText());
                // FIXME: don't fail silently
                if (matcher.matches()) {
                    String subject = matcher.group(1);
                    int course = Integer.parseInt(matcher.group(2));
                    int section = Integer.parseInt(matcher.group(3));
                    int crn = Integer.parseInt(matcher.group(4));

                    // FIXME: use proper day of week type, or at least a String of the day's name
                    String dayOfWeek = String.valueOf(dayId);

                    String startTime = matcher.group(5);
                    String endTime = matcher.group(6);
                    String location = matcher.group(7);

                    classTimeSlots.add(
                            new ClassTimeSlot(subject, course, section, crn, dayOfWeek, startTime, endTime, location));
                }
            }
        }

        return classTimeSlots;
    }
}
