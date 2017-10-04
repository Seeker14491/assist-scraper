import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner credentialsFile = new Scanner(new File("credentials.txt"));
        String userName = credentialsFile.nextLine();
        String pass = credentialsFile.nextLine();

        List<String> schedule = getSchedule(userName, pass);

        for (String c : schedule) {
            System.out.print(c + "\n\n");
        }
    }

    private static List<String> getSchedule(String userName, String pass) throws IOException {
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

        ArrayList<String> ret = new ArrayList<>();
        for (HtmlAnchor c : classes) {
            if (c.getHrefAttribute().startsWith("/PROD/bwskfshd.P_CrseSchdDetl")) {
                ret.add(c.asText());
            }
        }

        return ret;
    }
}
