package Matching;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.mysql.cj.jdbc.PreparedStatement;

/**
 * @author Hazimeh
 *
 */
public class TwitterRetriever {

	public static String dir = "C:/Users/Hazimeh/Desktop/chromedriver_win32 (3)";
	public static WebDriver driver;
	public static ArrayList<ArrayList<TwitterUser>> user_networks = new ArrayList<ArrayList<TwitterUser>>();

	public static void main(String[] args) {

		System.setProperty("webdriver.chrome.driver", dir
				+ "\\chromedriver.exe");
		driver = new ChromeDriver();
		ArrayList<String> usernames = getFBscreennames();
		for (int i = 0; i < usernames.size(); i++) {
			System.out.format("%s\n", usernames.get(i));
			ArrayList<TwitterUser> tu = new ArrayList<TwitterUser>();
			try {
				tu = getTwitterUsers(usernames.get(i));
				if (!(tu.isEmpty()))
					user_networks.add(tu);
			} catch (NullPointerException e) {
			}
		}
		System.out.println("Printing user networks");
		System.out.println("user nets size " + user_networks.size());
		insert_tw_screennames_into_mysql();
	}

	/***************************** get list of fb screennames from mysql database ***********************/
	public static ArrayList<String> getFBscreennames() {

		ArrayList<String> usernames = new ArrayList<String>();
		try {

			Connection conn = FacebookRetriever.connectMe();
			String query = "SELECT distinct(username),id,bio,url FROM `users`  WHERE NOT (bio <=> '')";
			PreparedStatement preparedStmt = (PreparedStatement) conn
					.prepareStatement(query);
			ResultSet rs = preparedStmt.executeQuery(query);
			while (rs.next()) {
				String[] username = rs.getString("username").split("\n");
				usernames.add(username[0].replace("(", "").replace(")", "")
						.replace("?", ""));
			}
			preparedStmt.close();

		} catch (SQLException e) {
			System.out.print(e.getMessage());
		}
		return usernames;
	}

	/***************************** get list of twitter users have same facebook screenname ***********************/
	public static ArrayList<TwitterUser> getTwitterUsers(String username) {

		ArrayList<TwitterUser> users = new ArrayList<TwitterUser>();

		String url = "https://twitter.com/search?f=users&q="
				+ username.replace(" ", "%20") + "&lang=en";
		driver.get(url);

		List<WebElement> ids = driver.findElements(By
				.xpath(".//*[@class='u-linkComplex-target']"));
		List<WebElement> screennames = driver
				.findElements(By
						.xpath(".//*[@class='fullname ProfileNameTruncated-link u-textInheritColor js-nav']"));

		for (int i = 0; i < screennames.size(); i++) {
			TwitterUser u = new TwitterUser();
			if (screennames.get(i).getText().toLowerCase()
					.equals(username.toLowerCase())
					|| ids.get(i).getText().replace("@", "")
							.equals(username.toLowerCase().replace(" ", ""))) {
				u.set_screenname(screennames.get(i).getText());
				u.set_id(ids.get(i).getText());
				u.set_fb_screename(username);
				System.out.println(u.id);
				users.add(u);
			}
		}
		System.out.println("************************");
		// driver.close();
		return users;
	}

	/************************** insert correspondent tw screennames for a given fb screenname into mysql *********************/
	public static void insert_tw_screennames_into_mysql() {
		try {
			Connection conn = FacebookRetriever.connectMe();
			for (int i = 0; i < user_networks.size(); i++) {
				String fb_screenname = "";
				String tw_screennames = "";
				String tw_ids = "";
				for (int j = 0; j < user_networks.get(i).size(); j++) {
					fb_screenname = user_networks.get(i).get(j).fb_screename;
					if (j == user_networks.get(i).size() - 1)
						tw_screennames += user_networks.get(i).get(j).screenname;
					else
						tw_screennames += user_networks.get(i).get(j).screenname
								+ "-";
					if (j == user_networks.get(i).size() - 1)
						tw_ids += user_networks.get(i).get(j).id;
					else
						tw_ids += user_networks.get(i).get(j).id + "-";
				}
				String query = " insert into twitter_users_bio (fb_screenname, tw_screennames,tw_ids)"
						+ " values (?, ?, ?)";
				java.sql.PreparedStatement preparedStmt = conn
						.prepareStatement(query);
				preparedStmt.setString(1, fb_screenname);
				preparedStmt.setString(2, tw_screennames);
				preparedStmt.setString(3, tw_ids);
				preparedStmt.addBatch();
				preparedStmt.clearParameters();
				preparedStmt.executeBatch();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}
