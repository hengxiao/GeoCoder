package main;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;
import org.xml.sax.InputSource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GeoCoder {
	private static final String GEOCODE_API_JSON_ADDRESS = "https://maps.google.com/maps/api/geocode/json";

	public GeoCoder(String address, String country) {
		this.address = address;
		this.country = country;
		this.found = false;
	}
	
	public void getCoordinate() {
		HttpsURLConnection connection = null;
		JSONObject infoGet = null;
		try {
			String paraString = "address=\"" + URLEncoder.encode(address, "UTF-8") + "\"" + '&' + "components" + URLEncoder.encode("=country:" + country, "UTF-8");

			URL url = new URL(GEOCODE_API_JSON_ADDRESS + '?' + paraString);

			System.err.println("Requesting coordinate from " + GEOCODE_API_JSON_ADDRESS + '?' + paraString + '.');
			connection = (HttpsURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			infoGet = new JSONObject(new JSONTokener(connection.getInputStream()));
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		} finally {
			connection.disconnect();
		}
		
		System.err.println("Parsing JSON");
		
		
		JSONArray results = infoGet.getJSONArray("results");
		System.err.println(results.toString());
		if (results.length() == 0) {
			found = false;
			return;
		}
		
		locations = new GeoCoderLocation[results.length()];
		for (int i = 0; i < locations.length; i++) {
			JSONObject result = results.getJSONObject(i);
			JSONObject geometry = result.getJSONObject("geometry");
			JSONObject coordinate = geometry.getJSONObject("location");
			
			locations[i] = new GeoCoderLocation();
			GeoCoderLocation location = locations[i];
			location.latitude = coordinate.getDouble("lat");
			location.longtitude = coordinate.getDouble("lng");
			if (result.has("partial_match")) {
				location.partial_match = result.getBoolean("partial_match");
			} else {
				location.partial_match = false;
			}
		}
	}
	
	public GeoCoderLocation[] getLocations() {
		return locations.clone();
	}
	
	public String resultsToString() {
		String result = "";
		for (GeoCoderLocation l : locations) {
			result += "(" + l.latitude + ", " + l.longtitude + ")=>" + (l.partial_match ? "PartialMatch" : "ExactMatch") + " ";
		}
		return result;
	}
	
	private String address;
	private String country;
	
	private boolean found;
	private GeoCoderLocation[] locations;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeoCoder coder = new GeoCoder("82 Durban Way, Portland, Mitchell's Plain", "South Africa");
		coder.getCoordinate();
		
		
		System.out.println(coder.resultsToString());
	}

}
