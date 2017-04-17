package Matching;

import java.util.ArrayList;

/**
 * @author Hazimeh
 *
 */
public class User {

	public String url;
	public String username;
	public String bio;
	public static ArrayList<SocialEvent> se_list = new ArrayList<SocialEvent>();

	public void set_url(String url) {
		this.url = url;
	}

	public void set_username(String username) {
		this.username = username;
	}

	public void set_se(ArrayList<SocialEvent> e) {
		User.se_list = e;
	}

	public void set_bio(String bio) {
		this.bio = bio;
	}
}
