package Matching;

/**
 * @author Hazimeh
 *
 */
public class TwitterUser {

	public String screenname;
	public String id;
	public String fb_screename;
	public int fb_user_id;

	public void set_screenname(String screename) {
		this.screenname = screename;
	}

	public void set_id(String id) {
		this.id = "@" + id;
	}

	public void set_fb_screename(String fb_screenaname) {
		this.fb_screename = fb_screenaname;
	}

	public void set_fb_user_id(int id) {
		this.fb_user_id = id;
	}

}
