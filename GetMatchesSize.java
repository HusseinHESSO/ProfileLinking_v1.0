package Matching;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * @author Hazimeh
 *
 */
public class GetMatchesSize {
	public static ArrayList<MatchesSize> mla = new ArrayList<MatchesSize>();
	public static Connection conn;

	public static void main(String args[]) throws SQLException {
		conn = FacebookRetriever.connectMe();
		try {

			String query = "SELECT * FROM twitter_users_bio";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);

			while (rs.next()) {
				String name = rs.getString("fb_screenname");
				String[] nom = rs.getString("tw_screennames").split("-");
				MatchesSize o = (new GetMatchesSize()).new MatchesSize();
				o.set_screenname(name);
				o.set_ms(nom.length);
				mla.add(o);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		for (int i = 0; i < mla.size(); i++) {
			System.out.println("Name: " + mla.get(i).screenname + " "
					+ mla.get(i).ms);
			String query2 = "insert into matchessize(fbname, matches) values(?,?)";

			java.sql.PreparedStatement preparedStmt = null;
			try {
				preparedStmt = conn.prepareStatement(query2);
				preparedStmt.setString(1, mla.get(i).screenname);
				preparedStmt.setInt(2, mla.get(i).ms);
				preparedStmt.addBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			preparedStmt.executeBatch();

		}
	}

	public class MatchesSize {

		public String screenname;
		public int ms;

		public void set_screenname(String name) {
			this.screenname = name;
		}

		public void set_ms(int ms) {
			this.ms = ms;
		}
	}
}
