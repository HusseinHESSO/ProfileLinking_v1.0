package Matching;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.mysql.cj.jdbc.PreparedStatement;

/**
 * @author Hazimeh
 *
 */
public class ProfilerLinker_Desc {
	public static ArrayList<Profile> profiles = new ArrayList<Profile>();
	public static Connection conn;
	public static String dir = "C:/Users/Hazimeh/Desktop/chromedriver_win32 (3)";
	public static WebDriver driver;
	public static int j = 0;
	public static String matchedProfiles = "";
	public static FileWriter file;

	public static void main(String[] args) throws SQLException, IOException {

		file = new FileWriter("f:\\matching_result_by_bio.txt");
		conn = FacebookRetriever.connectMe();
		System.setProperty("webdriver.chrome.driver", dir
				+ "\\chromedriver.exe");
		driver = new ChromeDriver();
		get_profiles();
		for (int i = 0; i < profiles.size(); i++) {
			System.out.println("Profile name: " + profiles.get(i).screenname);
			System.out.println("Profile desc: " + profiles.get(i).bio);
			for (int j = 0; j < profiles.get(i).ids.length; j++) {
				// System.out.println("Profile ids "+profiles.get(i).ids[j]);
				searchTimeLine(profiles.get(i).ids[j], "", profiles.get(i).bio);
			}
			System.out
					.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}
		System.out.println("matched : " + matchedProfiles);
		// count_twitter_bios();

	}

	private static void get_profiles() {
		try {
			String query = "select distinct(fb_screenname),tw_screennames,tw_ids from twitter_users_bio";
			PreparedStatement preparedStmt = (PreparedStatement) conn
					.prepareStatement(query);
			ResultSet rs = preparedStmt.executeQuery(query);
			while (rs.next()) {
				Profile p = (new ProfilerLinker_Desc()).new Profile();
				String[] ids = rs.getString("tw_ids").split("-");
				String username = rs.getString("fb_screenname");
				p.set_screenname(username);
				p.set_ms(ids);
				p.set_bio(get_fb_bio_from_mysql(username));
				profiles.add(p);
			}
			preparedStmt.close();

		} catch (SQLException e) {
			System.out.print(e.getMessage());
		}

	}

	/***************************** get facebook bio ***********************/
	public static String get_fb_bio_from_mysql(String screenname) {
		String screenname1 = "";
		try {
			String query = "select * from users where username like'%"
					+ screenname + "%'";
			PreparedStatement preparedStmt = (PreparedStatement) conn
					.prepareStatement(query);
			ResultSet rs = preparedStmt.executeQuery(query);
			while (rs.next()) {
				screenname1 = rs.getString("bio");
			}
			preparedStmt.close();

		} catch (SQLException e) {
			System.out.print(e.getMessage());
		}
		return screenname1;
	}

	/***************************** count twitter bio ***********************/
	public static void count_twitter_bios() {
		String ids = "";
		try {
			String query = "select distinct(fb_screenname),tw_screennames,tw_ids from twitter_users_bio";
			PreparedStatement preparedStmt = (PreparedStatement) conn
					.prepareStatement(query);
			ResultSet rs = preparedStmt.executeQuery(query);
			while (rs.next()) {
				ids += rs.getString("tw_ids") + "-";
			}
			preparedStmt.close();

		} catch (SQLException e) {
			System.out.print(e.getMessage());
		}
		ids = ids + "@test";
		for (int i = 0; i < ids.split("-").length; i++) {
			String url = "https://twitter.com/" + ids.split("-")[i]
					+ "?lang=en";
			System.out.println(url);
			driver.get(url);
			WebElement desc = driver.findElement(By
					.xpath(".//*[@class='ProfileHeaderCard-bio u-dir']"));
			if (!(desc.getText().isEmpty())) {
				j++;
			}
		}
		System.out.println("size " + j);
	}

	/*****************************
	 * search a twitter user timeline for a life event given from facebook
	 * 
	 * @throws IOException
	 ***********************/
	public static void searchTimeLine(String userid, String event, String bio)
			throws IOException {

		String url = "https://twitter.com/search?l=&q=kind%20OR%20test%20from%3A"
				+ userid + "&src=typd&lang=en";
		driver.get(url);
		// driver.findElement(By.name("ors")).sendKeys("kind");
		// driver.findElement(By.name("from")).sendKeys("chan_56za");
		// driver.findElement(By.xpath(".//*[@class='button btn primary-btn submit selected']")).click();
		List<WebElement> e = driver
				.findElements(By
						.xpath(".//*[@class='TweetTextSize  js-tweet-text tweet-text']"));
		if (e.size() == 0)
			System.out.println("empty search results");
		else {
			for (int i = 0; i < e.size(); i++) {
				System.out.println(e.get(i).getText());
			}
		}
		driver.get("https://twitter.com/" + userid + "?lang=en");
		try {
			WebElement desc = driver.findElement(By
					.xpath(".//*[@class='ProfileHeaderCard-bio u-dir']"));
			System.out.println("Twitter desc: " + desc.getText());

			Bio_matcher(
					bio.replace(":", "").replace("|", "").replace("@", "")
							.replace(",", "").replace("'", "").replace("#", "")
							.replace("&", "").replace(".", "").replace("/", ""),
					desc.getText().replace(":", "").replace("|", "")
							.replace("@", "").replace(",", "").replace("'", "")
							.replace("#", "").replace("&", "").replace(".", "")
							.replace("/", ""), userid);
		} catch (NoSuchElementException exc) {
		}
	}

	public static void Bio_matcher(String fb_bio, String tw_bio, String name)
			throws IOException {

		CosineSimilarity cs1 = new CosineSimilarity();
		double sim_score = cs1.Cosine_Similarity_Score(fb_bio, tw_bio);
		System.out.println("comparing " + fb_bio + " @@with@@ " + tw_bio);
		System.out.println("Cosine similarity score = " + sim_score);
		if (sim_score > 0.1)
			matchedProfiles += name + "-";
		file.write("fb_bio :: " + fb_bio + " :: tw_bio " + tw_bio + " user:: "
				+ name + " :: score ::" + sim_score + "\n");
		file.flush();
		System.out.println("******************************");
	}

	public class Profile {

		public String screenname;
		public String[] ids;
		public String bio;

		public void set_screenname(String name) {
			this.screenname = name;
		}

		public void set_ms(String[] ms) {
			this.ids = ms;
		}

		public void set_bio(String bio) {
			this.bio = bio;
		}
	}
}
