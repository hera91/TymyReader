package com.ph.tymyreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.acra.ACRA;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.net.http.AndroidHttpClient;
import android.util.Log;

//import android.util.Log;

public class TymyPageLoader {

	//	private static final String TAG = "TymyReader";
	private static final String TYMY_MAIN_PAGE="/index.php"; 
	private static final String TYMY_DS_PAGE_FORMAT="/index.php?page=discussion&id=%s&level=101";
	private static final String TYMY_AJAX_PAGE = "/ajax.php?page=main";
	public static final Object TYMY_LOGIN_COOKIE = "uname";

	// TODO zobecnit metody pro download stranek


	//********* HttpClient ************//
	public String loadMainPage (String url, String user, String pass, HttpContext httpContext) {
		return httpGet(url, TYMY_MAIN_PAGE, user, pass, httpContext);
	}

	public String loadDsPage(String url, String id, String user, String pass, HttpContext httpContext) {
		return httpGet(url, String.format(TYMY_DS_PAGE_FORMAT, id), user, pass, httpContext);
	}
	
	public String loadAjaxPage (String url, String user, String pass, HttpContext httpContext) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("xajax", "getNewInformation"));
		nameValuePairs.add(new BasicNameValuePair("xajaxr", "1358695211471"));
		return httpPost(url, TYMY_AJAX_PAGE, user, pass, httpContext, nameValuePairs);
	}

	public boolean login(String url, String user, String pass, HttpContext httpContext) {
		boolean isLogged = false;
		// Create a new HttpClient and Post Header
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		HttpPost httpPost = new HttpPost("http://" + url);
		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("frm_user_name", user));
			nameValuePairs.add(new BasicNameValuePair("frm_password", pass));
			nameValuePairs.add(new BasicNameValuePair("frm_action", "Přihlášení"));
			nameValuePairs.add(new BasicNameValuePair("javascript", "YES"));
			nameValuePairs.add(new BasicNameValuePair("page", "main"));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

			// Create a local instance of cookie store
			CookieStore cookieStore = new BasicCookieStore();

			// Bind custom cookie store to the local context
			httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			// Execute HTTP Post Request
			final HttpResponse response = client.execute(httpPost, httpContext);
			final int statusCode = response.getStatusLine().getStatusCode();
			response.getEntity().consumeContent();
			if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
				Log.v(TymyReader.TAG, "Error " + response + " while login to " + url);
				return isLogged = false;
			} 
		} catch (ClientProtocolException e) {
			httpPost.abort();
			Log.v(TymyReader.TAG, "Error while login to " + url, e);
			ACRA.getErrorReporter().handleSilentException(e);
		} catch (IOException e) {
			Log.v(TymyReader.TAG, "Error while login to " + url, e);
			ACRA.getErrorReporter().handleSilentException(e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		// Test if login was successful
		isLogged = isLogged(httpContext, user);
		return isLogged;
	}

	private String httpGet (String url, String page, String user, String pass, HttpContext httpContext) {
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		HttpGet getRequest = new HttpGet("http://" + url + page);
		StringBuilder output = new StringBuilder();

		if (!isLogged(httpContext, user)) { //not logged
			if (!login(url, user, pass, httpContext)) {
				return null; // login failed
			}
		}

		try {
			HttpResponse response = client.execute(getRequest, httpContext);
			final int statusCode = response.getStatusLine().getStatusCode();
			if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
				Log.v(TymyReader.TAG, "Error " + statusCode + " while download  to " + url + page);
				return null;
			} 

			// Get the response
			BufferedReader rd = new BufferedReader
					(new InputStreamReader(response.getEntity().getContent()));

			String line = new String();
			while ((line = rd.readLine()) != null) {
				output.append(line);
			}
			response.getEntity().consumeContent();
		} catch (Exception e) {
			// Could provide a more explicit error message for IOException or IllegalStateException
			getRequest.abort();
			Log.v(TymyReader.TAG, "Error while downloading " + url + page, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return output.toString();
	}

	private String httpPost(String url, String page, String user, String pass, HttpContext httpContext, List<NameValuePair> nameValuePairs) {
		// Create a new HttpClient and Post Header
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		HttpPost httpPost = new HttpPost("http://" + url + page);
		StringBuilder output = new StringBuilder();

		if (!isLogged(httpContext, user)) { //not logged
			if (!login(url, user, pass, httpContext)) {
				return null; // login failed
			}
		}

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

			// Execute HTTP Post Request
			final HttpResponse response = client.execute(httpPost, httpContext);
			final int statusCode = response.getStatusLine().getStatusCode();
			response.getEntity().consumeContent();
			if ((statusCode != HttpStatus.SC_OK) && (statusCode != HttpStatus.SC_MOVED_TEMPORARILY)) {
				Log.v(TymyReader.TAG, "Error " + statusCode + " while POST " + url + page);
				return null;
			} 

			// Get the response
			BufferedReader rd = new BufferedReader
					(new InputStreamReader(response.getEntity().getContent()));

			String line = new String();
			while ((line = rd.readLine()) != null) {
				output.append(line);
			}
			response.getEntity().consumeContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			httpPost.abort();
			Log.v(TymyReader.TAG, "Error while POST " + url, e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.v(TymyReader.TAG, "Error while POST " + url, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return output.toString();
	}
	
	public static boolean isLogged (HttpContext httpContext, String user) {
		boolean isLogged = false;
		CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
		if (cookieStore != null) {
			cookieStore.clearExpired(new Date());
			for (Cookie cookie : cookieStore.getCookies()) {
				if (cookie.getName().equals(TymyPageLoader.TYMY_LOGIN_COOKIE)) {
					isLogged = cookie.getValue().equals(user);
				}
			}
		}
		return isLogged;
	}
}


//public String loadMainPage(String url, String user, String pass,
//StringBuilder cookies) {
//return loadPage(String.format("%s/index.php", url), user, pass, cookies);
//}
//
//public String loadDsPage(String url, String id, String user, String pass, StringBuilder cookies) {
//return loadPage(String.format("%s/index.php?page=discussion&id=%s&level=101", url, id, url), user, pass, cookies);
//}
//
//public String login(final String url, final String user, final String pass, StringBuilder cookies) {
//
//StringBuilder output = new StringBuilder();
//try {
//String data = null;
//DataOutputStream wr;
//if (cookies.toString().equals("")) {
//	data = setFormData(user, pass);
//	//			Log.v(TAG,"Debug: " + user + pass + tym);
//	HttpURLConnection connection = createConnection(data, url + "/index.php", cookies, "POST");
//
//	wr = new DataOutputStream(connection.getOutputStream ());
//	wr.writeBytes(data);
//	wr.flush();
//	wr.close();
//
//	cookies.append(extractCookies(connection));               
//	connection.disconnect();						
//}
//}
//catch (Exception e) {
//e.printStackTrace(System.out);
//}
//return output.toString();
//}
//
//private String loadPage(String url, String user, String pass, StringBuilder cookies) {
//
//StringBuilder output = new StringBuilder(); 
//try {
//login(url, user, pass, cookies);
//String data = null;
//
//HttpURLConnection connection = createConnection(data, url, cookies, "GET");
//
//BufferedReader rd = null;
//try {
//	rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//	String line;
//	while ((line = rd.readLine()) != null) {
//		output.append(line);
//	}
//} catch (Exception e) {
//	e.printStackTrace(System.out);
//}			
//
//rd.close();
//connection.disconnect();						
//}
//catch (Exception e) {
//e.printStackTrace(System.out);
//}
//return output.toString();
//}
//
//public String loadAjaxPage(String url, String user, String pass, StringBuilder cookies) {
//StringBuilder output = new StringBuilder();
//try {
//login(url, user, pass, cookies);
//String data = URLEncoder.encode("xajax", "UTF-8") + "=" + URLEncoder.encode("getNewInformation", "UTF-8") +
//		"&" + URLEncoder.encode("xajaxr", "UTF-8") + "=" + URLEncoder.encode("1358695211471", "UTF-8");
//DataOutputStream wr;
////			Log.v(TAG,"Debug: " + user + pass + tym);
//HttpURLConnection connection = createConnection(data, url + "/ajax.php?page=main", cookies, "POST");
//
//wr = new DataOutputStream(connection.getOutputStream ());
//wr.writeBytes(data);
//wr.flush();
//wr.close();
//
//BufferedReader rd = null;
//try {
//	rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//	String line;
//	while ((line = rd.readLine()) != null) {
//		output.append(line);
//	}
//} catch (Exception e) {
//	e.printStackTrace(System.out);
//}			
//
//rd.close();
//connection.disconnect();						
//}
//catch (Exception e) {
//e.printStackTrace(System.out);
//}
//return output.toString();
//}
//
//private String setFormData(final String login, final String password) throws UnsupportedEncodingException {
//String data = URLEncoder.encode("frm_user_name", "UTF-8") + "=" + URLEncoder.encode(login, "UTF-8");
//data += "&" + URLEncoder.encode("frm_password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
//data += "&" + URLEncoder.encode("frm_action", "UTF-8") + "=" + URLEncoder.encode("Přihlášení", "UTF-8");
//data += "&" + URLEncoder.encode("javascript", "UTF-8") + "=" + URLEncoder.encode("YES", "UTF-8");
//data += "&" + URLEncoder.encode("page", "UTF-8") + "=" + URLEncoder.encode("main", "UTF-8");
//return data;
//}
//
//private String extractCookies(HttpURLConnection connection) {
//String headerName=null;
//StringBuilder cookies = new StringBuilder();
//for (int i=1; (headerName = connection.getHeaderFieldKey(i))!=null; i++) {
//if (headerName.equals("Set-Cookie")) {                  
//	String cookie = connection.getHeaderField(i);
//	cookie = cookie.substring(0, cookie.indexOf(";"));
//	cookies.append(cookie);
//	cookies.append(";");
//}
//}
//return cookies.toString();
//}
//
//private HttpURLConnection createConnection(String data, final String page, StringBuilder cookies, final String method)
//throws MalformedURLException, IOException, ProtocolException {
//URL url = new URL("http://" + page); 
//HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
//connection.setDoOutput(true);
//connection.setDoInput(true);
//connection.setInstanceFollowRedirects(false); 
//connection.setRequestMethod(method); 
//connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
//connection.setRequestProperty("charset", "utf-8");
//if (!method.equals("GET")) {
//connection.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
//}
//connection.setUseCaches (false);
//
//if (cookies.toString() != "") {
//connection.setRequestProperty("Cookie", cookies.toString());
//}
//return connection;
//}
//
//@SuppressWarnings("unused")
//private void printHeader(HttpURLConnection conn) {
//String headerName=null;
//for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
//System.out.println(headerName + ": " + conn.getHeaderField(i));
//}
//}
