package Matching;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By; 
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.w3c.dom.*;

import javax.xml.parsers.*;

import com.mysql.cj.jdbc.PreparedStatement;

/**
 * @author Hazimeh
 * 
 */
public class ProfilerLinker_Events {
	public static ArrayList<Profile> profiles = new ArrayList<Profile>();
	public static Connection conn;
	public static String dir = "C:/Users/Hazimeh/Desktop/chromedriver_win32 (3)";
	public static WebDriver driver;
	public static int j = 0;
	public static String matchedProfiles = "";
	public static FileWriter file;
	public static File file2;

	public static void main(String[] args) throws Exception {

		file2 = new File("f:/output.xml");
		file = new FileWriter("f:\\matching_result_by_events.txt");
		conn = FacebookRetriever.connectMe();
		System.setProperty("webdriver.chrome.driver", dir
				+ "\\chromedriver.exe");
		driver = new ChromeDriver();
		get_profiles();

		for (int i = 0; i < 30; i++) {
			System.out.println("Profile name: " + profiles.get(i).screenname);
			System.out.println("Events desc: " + profiles.get(i).events);

			NER.get_entities(profiles.get(i).events.replace(":", "")
					.replace("|", "").replace("@", "").replace(",", "")
					.replace("'", "").replace("#", "").replace("&", "")
					.replace(".", "").replace("/", ""));

			String twitter_query = get_entities_from_xml();
			twitter_query = twitter_query.replace(" ", "%20");

			for (int j = 0; j < profiles.get(i).ids.length; j++) {
				System.out.println("Profile ids " + profiles.get(i).ids[j]);
				searchTimeLine(profiles.get(i).ids[j], twitter_query);
			}
			System.out
					.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		}
		System.out.println("matched : " + matchedProfiles);
		// count_twitter_bios();

	}

	private static void get_profiles() {
		try {
			String query = "select distinct(fb_screenname),tw_screennames,tw_ids from twitter_users";
			PreparedStatement preparedStmt = (PreparedStatement) conn
					.prepareStatement(query);
			ResultSet rs = preparedStmt.executeQuery(query);
			while (rs.next()) {
				Profile p = (new ProfilerLinker_Events()).new Profile();
				String[] ids = rs.getString("tw_ids").split("-");
				String username = rs.getString("fb_screenname");
				p.set_screenname(username);
				p.set_ms(ids);
				p.set_events(get_fb_bio_from_mysql(username));
				profiles.add(p);
			}
			preparedStmt.close();

		} catch (SQLException e) {
			System.out.print(e.getMessage());
		}

	}

	/***************************** get facebook bio ***********************/
	public static String get_fb_bio_from_mysql(String screenname) {
		String events = "*";
		try {
			String query = "select * from alldata where username like'%"
					+ screenname + "%'";
			PreparedStatement preparedStmt = (PreparedStatement) conn
					.prepareStatement(query);
			ResultSet rs = preparedStmt.executeQuery(query);
			while (rs.next()) {
				events += rs.getString("desc") + " ";
			}
			preparedStmt.close();

		} catch (SQLException e) {
			System.out.print(e.getMessage());
		}
		return events;
	}

	/************************ get entities from xml file *********************/
	public static String get_entities_from_xml() {
		String query = "";
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file2);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("wi");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				Element eElement = (Element) nNode;
				// System.out.println("Student roll no : "+
				// eElement.getAttribute("entity"));
				// System.out.println("First Name : "+ nNode.getTextContent());
				if (!(eElement.getAttribute("entity").equals("O")))
					query += nNode.getTextContent() + " ";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return query;
	}

	/***************************** count twitter bio ***********************/
	public static void count_twitter_bios() {
		String ids = "";
		try {
			String query = "select distinct(fb_screenname),tw_screennames,tw_ids from twitter_users";
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
	public static void searchTimeLine(String userid, String query)
			throws IOException {

		String url = "https://twitter.com/search?l=&q=" + query + "from%3A"
				+ userid + "&src=typd&lang=en";
		driver.get(url);
		// driver.findElement(By.name("ors")).sendKeys("kind");
		// driver.findElement(By.name("from")).sendKeys("chan_56za");
		// driver.findElement(By.xpath(".//*[@class='button btn primary-btn submit selected']")).click();
		List<WebElement> e = driver
				.findElements(By
						.xpath(".//*[@class='TweetTextSize  js-tweet-text tweet-text']"));
		if (e.size() == 0){
			System.out.println("empty search results");
			
		}
		else {
			file.write("fb_events :: " + query + "  user:: "
					+ userid + "\n");
			file.flush();
			for (int i = 0; i < e.size(); i++) {
				System.out.println(e.get(i).getText());
			}
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
		public String events;

		public void set_screenname(String name) {
			this.screenname = name;
		}

		public void set_ms(String[] ms) {
			this.ids = ms;
		}

		public void set_bio(String bio) {
			this.bio = bio;
		}

		public void set_events(String events) {
			this.events = events;
		}
	}
}
