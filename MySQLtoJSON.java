package Matching;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author Hazimeh
 *
 */
public class MySQLtoJSON {

	private static FileWriter file;
	private static JSONObject o;
	private static JSONArray list;
	private static JSONObject o2;

	/**
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		file = new FileWriter("f:\\data.json");
		try {
			Connection conn = FacebookRetriever.connectMe();
			String query = "SELECT * FROM users";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			file.write("[");
			while (rs.next()) {
				String query2 = "SELECT * FROM `events`  WHERE userurl='"
						+ rs.getString("url") + "'";
				Statement st2 = conn.createStatement();
				ResultSet rs2 = st2.executeQuery(query2);

				if (rs2.next()) {
					o = new JSONObject();
					o.put("Username: ", rs.getString("username"));
					o.put("BIO: ", rs.getString("bio"));
					o.put("URL: ", rs.getString("url"));

					System.out.println(rs.getString("username"));

					rs2.beforeFirst();

					list = new JSONArray();
					while (rs2.next()) {
						o2 = new JSONObject();
						o2.put("Event_DESC: ", rs2.getString("desc"));
						o2.put("Event_DATE: ", rs2.getString("date"));
						if (rs2.getString("desc").toLowerCase()
								.contains("traveled".toLowerCase()))
							o2.put("EVENT_CLASS", "Travel");
						else if (rs2.getString("desc").toLowerCase()
								.contains("started working".toLowerCase()))
							o2.put("EVENT_CLASS", "New job");
						else if (rs2.getString("desc").toLowerCase()
								.contains("left job".toLowerCase()))
							o2.put("EVENT_CLASS", "Left job");
						else if (rs2.getString("desc").toLowerCase()
								.contains("Graduated".toLowerCase()))
							o2.put("EVENT_CLASS", "Graduation");
						else if (rs2.getString("desc").toLowerCase()
								.contains("started school".toLowerCase()))
							o2.put("EVENT_CLASS", "Start school");
						else if (rs2.getString("desc").toLowerCase()
								.contains("moved".toLowerCase()))
							o2.put("EVENT_CLASS", "Change city");
						else if (rs2.getString("desc").toLowerCase()
								.contains("engaged".toLowerCase()))
							o2.put("EVENT_CLASS", "Engagement");
						else if (rs2.getString("desc").toLowerCase()
								.contains("published".toLowerCase()))
							o2.put("EVENT_CLASS", "Publication");
						else if (rs2.getString("desc").toLowerCase()
								.contains("married".toLowerCase()))
							o2.put("EVENT_CLASS", "Marriage");
						else if (rs2.getString("desc").toLowerCase()
								.contains("relationship".toLowerCase()))
							o2.put("EVENT_CLASS", "New relashiship");
						else if (rs2.getString("desc").toLowerCase()
								.contains("award".toLowerCase()))
							o2.put("EVENT_CLASS", "Get an award");
						else
							o2.put("EVENT_CLASS", "Other");
						list.add(o2);
					}
					o.put("Life_events: ", list);
					file.write(o.toJSONString() + ",");
					file.flush();
				}
			}
			st.close();
		} catch (Exception e) {
		}
	}
}