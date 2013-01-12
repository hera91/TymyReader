package com.ph.tymyreader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import android.util.Log;

public class TymyPageLoader {

	private static final String TAG = "TymyReader";

	public String loadPage(String url, String user, String pass, StringBuilder cookies, String id) {

		StringBuilder output = new StringBuilder(); 
		try {
			String data = null;
			DataOutputStream wr;
			if (cookies.toString() == "") {
				Log.v(TAG, "cookies = \"\"");				
				data = setFormData(user, pass);

				//			Log.v(TAG,"Debug: " + user + pass + tym);
				HttpURLConnection connection = createConnection(data, url + "/index.php", cookies, "POST");

				wr = new DataOutputStream(connection.getOutputStream ());
				wr.writeBytes(data);
				wr.flush();
				wr.close();

				cookies.append(extractCookies(connection));               
			}

			HttpURLConnection connection = createConnection(data, 
					url + "/index.php?page=discussion&id=" + id + "&level=101", cookies, "GET");

			BufferedReader rd = null;
			try {
				rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) {
					output.append(line);
				}
			} catch (Exception e) {
				Log.v(TAG, e.toString());
				e.printStackTrace(System.out);
			}			

			rd.close();
			connection.disconnect();						
		}
		catch (Exception e) {
			Log.v(TAG, e.toString());
			e.printStackTrace(System.out);
		}
		return output.toString();
	}

	public String login(final String url, final String user, final String pass, StringBuilder cookies) {

		StringBuilder output = new StringBuilder(); 
		try {
			String data = null;
			DataOutputStream wr;
			if (cookies.toString() == "") {
				Log.v(TAG, "cookies = \"\"");				
				data = setFormData(user, pass);

				//			Log.v(TAG,"Debug: " + user + pass + tym);
				HttpURLConnection connection = createConnection(data, url + "/index.php", cookies, "POST");

				wr = new DataOutputStream(connection.getOutputStream ());
				wr.writeBytes(data);
				wr.flush();
				wr.close();

				cookies.append(extractCookies(connection));               
				connection.disconnect();						
			}
		}
		catch (Exception e) {
			Log.v(TAG, e.toString());
			e.printStackTrace(System.out);
		}
		return output.toString();
	}

	private String setFormData(final String login, final String password) throws UnsupportedEncodingException {
		String data = URLEncoder.encode("frm_user_name", "UTF-8") + "=" + URLEncoder.encode(login, "UTF-8");
		data += "&" + URLEncoder.encode("frm_password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
		data += "&" + URLEncoder.encode("frm_action", "UTF-8") + "=" + URLEncoder.encode("Přihlášení", "UTF-8");
		data += "&" + URLEncoder.encode("javascript", "UTF-8") + "=" + URLEncoder.encode("YES", "UTF-8");
		data += "&" + URLEncoder.encode("page", "UTF-8") + "=" + URLEncoder.encode("main", "UTF-8");
		return data;
	}

	private String extractCookies(HttpURLConnection connection) {
		String headerName=null;
		StringBuilder cookies = new StringBuilder();
		for (int i=1; (headerName = connection.getHeaderFieldKey(i))!=null; i++) {
			if (headerName.equals("Set-Cookie")) {                  
				String cookie = connection.getHeaderField(i);
				cookie = cookie.substring(0, cookie.indexOf(";"));
				cookies.append(cookie);
				cookies.append(";");
			}
		}
		return cookies.toString();
	}

	private HttpURLConnection createConnection(String data, final String page, StringBuilder cookies, final String method)
			throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL("http://" + page); 
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false); 
		connection.setRequestMethod(method); 
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
		connection.setRequestProperty("charset", "utf-8");
		if (!method.equals("GET")) {
			connection.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
		}
		connection.setUseCaches (false);

		if (cookies.toString() != "") {
			connection.setRequestProperty("Cookie", cookies.toString());
		}
		return connection;
	}

	@SuppressWarnings("unused")
	private void printHeader(HttpURLConnection conn) {
		String headerName=null;
		for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
			System.out.println(headerName + ": " + conn.getHeaderField(i));
		}
	}	
}

