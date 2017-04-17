package Matching;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.mysql.cj.jdbc.PreparedStatement;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////		

/**
 * @author Hazimeh
 *
 */
public class FacebookRetriever {

	public static ArrayList<User> users_list = new ArrayList<User>();
	public static String dir = "C:/Users/Hazimeh/Desktop/chromedriver_win32 (3)";
	public static WebDriver driver;

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] a) throws IOException,
			InterruptedException, TimeoutException {

		String accounts[] = { "https://www.facebook.com/hawraa.boutaam.7/friends?pnref=lhc", };
		System.setProperty("webdriver.chrome.driver", dir
				+ "\\chromedriver.exe");
		driver = new ChromeDriver();
		driver.get("https://www.facebook.com/");
		driver.findElement(By.name("email")).sendKeys(
				"h.hazimeh@youssefhazimeh.com");
		driver.findElement(By.name("pass")).sendKeys("Center12345678910");
		driver.findElement(By.id("loginbutton")).click();
		for (int x = 0; x < accounts.length; x++) {

			driver.get(accounts[x]);

			JavascriptExecutor js = ((JavascriptExecutor) driver);

			for (int i = 0; i < 5000; i++)
				js.executeScript("window.scrollTo(" + i
						+ ", document.body.scrollHeight)");
			Document doc = Jsoup.parse(driver.getPageSource());
			Elements links = doc.select("a");
			System.out.println("urls");
			List<String> urls = new ArrayList<String>();

			for (Element element : links) {
				if (element.attr("href").indexOf("friends_tab") != -1) {
					int i = element.attr("href").indexOf("?");
					urls.add(element.attr("href").substring(0, i));

				}
			}
			HashSet<String> uniqueURLs = new HashSet<>(urls);
			getsocialevents(uniqueURLs);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static void getsocialevents(HashSet<String> urls)
			throws InterruptedException, TimeoutException {
		ArrayList<SocialEvent> s = new ArrayList<SocialEvent>();
		String username;
		String bio;
		try {
			Connection conn = connectMe();
			for (String value : urls) {
				User u = new User();
				s = getSocialEvents(driver, value);
				username = get_screen_name(driver, value);
				bio = get_bio(driver, value);
				u.set_url(value);
				u.set_username(username);
				u.set_bio(bio);
				u.set_se(s);
				String query = " insert into users (url,username,bio)"
						+ " values (?,?,?)";
				PreparedStatement preparedStmt = (PreparedStatement) conn
						.prepareStatement(query);
				preparedStmt.setString(1, u.url);
				preparedStmt.setString(2, u.username);
				preparedStmt.setString(3, u.bio);
				preparedStmt.execute();

				for (int i = 0; i < s.size(); i++) {
					String query2 = " INSERT INTO `matching`.`events` (`desc`, `date`, `userurl`) VALUES (?,?,?);";
					PreparedStatement preparedStmt2 = (PreparedStatement) conn
							.prepareStatement(query2);
					preparedStmt2.setString(1, s.get(i).event_desc);
					preparedStmt2.setString(2,
							s.get(i).event_date.replace("Posted on", ""));
					preparedStmt2.setString(3, value);
					preparedStmt2.execute();
				}
				users_list.add(u);
				Thread.sleep(50);
			}
		} catch (SQLException e) {
			System.out.println("eroor sql" + e.getMessage());
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static ArrayList<SocialEvent> getSocialEvents(WebDriver driver,
			String URL) throws TimeoutException {

		ArrayList<SocialEvent> se_list = new ArrayList<SocialEvent>();
		String s[] = new String[2];
		System.out.println(URL);
		try {
			driver.get(URL);
			JavascriptExecutor js = ((JavascriptExecutor) driver);
			for (int i = 0; i < 3000; i++)
				js.executeScript("window.scrollTo(" + i
						+ ", document.body.scrollHeight)");

			try {
				List<WebElement> list = driver.findElements(By
						.xpath(".//*[@class='mvl _52jv']"));
				for (WebElement el : list) {
					SocialEvent _se = new SocialEvent();
					s = el.getText().split("\n");
					try {
						_se.set_event_desc(s[0]);
						_se.set_event_date(s[1]);
						se_list.add(_se);
						System.out.println("event: " + s[0] + " Year: " + s[1]);
					} catch (ArrayIndexOutOfBoundsException e) {
					}
					;
				}
			} catch (ElementNotFoundException e) {
				System.out.println("eroor " + e.getMessage());
			}
		} catch (org.openqa.selenium.TimeoutException e) {
		}
		return se_list;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static Connection connectMe() throws SQLException {

		String url = "jdbc:mysql://localhost/Matching?useUnicode=true&character_set_server=utf8mb4";
		String username = "root";
		String password = "ZrqymbyXcSXMxcLw";

		System.out.println("Connecting database...");

		Connection connection = DriverManager.getConnection(url, username,
				password);
		System.out.println("Database connected!");
		return connection;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static String get_screen_name(WebDriver driver, String URL) {
		String screen_nameTxt = "";
		try {
			WebElement screen_name = driver.findElement(By
					.id("fb-timeline-cover-name"));
			screen_nameTxt = screen_name.getText();
		} catch (NoSuchElementException e) {
		}
		return screen_nameTxt;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static String get_bio(WebDriver driver, String URL) {
		String bioTxt = "";
		try {
			WebElement bio = driver.findElement(By
					.xpath(".//*[@class='_50f9 _50f3']"));
			bioTxt = bio.getText();
		} catch (NoSuchElementException e) {
		}
		return bioTxt;
	}
}
