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
import org.apache.http.protocol.BasicHttpContext;
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
	private static final String TYMY_NEW_POST_PAGE = "/index.php";
	public static final Object TYMY_UNAME_COOKIE = "uname";
	public static final Object TYMY_SESSION_COOKIE = "PHPSESSID";



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

	public String newPost (String url, String id, String user, String pass, HttpContext httpContext, String post) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("page", "discussion"));
		nameValuePairs.add(new BasicNameValuePair("pg_d", id));
		nameValuePairs.add(new BasicNameValuePair("frm_action", "Insert"));
		nameValuePairs.add(new BasicNameValuePair("frm_item", post));
		String result = httpPost(url, TYMY_NEW_POST_PAGE, user, pass, httpContext, nameValuePairs);
		return result;
	}
	
	public static boolean isLogged (HttpContext httpContext, String user) {
		boolean isLogged = false;
		CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
		if (cookieStore != null) {
			cookieStore.clearExpired(new Date());
			for (Cookie cookie : cookieStore.getCookies()) {
				if (cookie.getName().equals(TymyPageLoader.TYMY_UNAME_COOKIE)) {
					isLogged = cookie.getValue().equals(user);
				}
			}
		}
		return isLogged;
	}

	public Boolean testLogin(String url, String user, String pass, HttpContext httpContext) {
		if (httpContext == null) {
			httpContext = new BasicHttpContext();
		}
		return login(url, user, pass, httpContext);
	}

	public static String getURLLoginAttr (HttpContext httpContext) {
		StringBuilder attr = new StringBuilder(TYMY_MAIN_PAGE + "?");
		CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
		if (cookieStore != null) {
			cookieStore.clearExpired(new Date());
			for (Cookie cookie : cookieStore.getCookies()) {
				if (cookie.getName().equals(TYMY_UNAME_COOKIE)) {
					attr.append(cookie.getName() + "=" + cookie.getValue() + "&");
				}
				if (cookie.getName().equals(TYMY_SESSION_COOKIE)) {
					attr.append(cookie.getName() + "=" + cookie.getValue() + "&");
				}
			}
		}
		return attr.toString();
	}
	
	private boolean login(String url, String user, String pass, HttpContext httpContext) {
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
			if (client != null) client.close();
			Log.v(TymyReader.TAG, "Error while login to " + url, e);
			ACRA.getErrorReporter().handleSilentException(e);
		} catch (IOException e) {
			if (client != null) client.close();
			Log.v(TymyReader.TAG, "Error while login to " + url, e);
			ACRA.getErrorReporter().handleSilentException(e);
		} finally {
			if (client != null) client.close();
		}
		// Test if login was successful
		isLogged = isLogged(httpContext, user);
		return isLogged;
	}

	private String httpGet (String url, String page, String user, String pass, HttpContext httpContext) {
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		HttpGet getRequest = new HttpGet("http://" + url + page);
		StringBuilder output = new StringBuilder();

		try {
			if (!isLogged(httpContext, user)) { //not logged
				if (!login(url, user, pass, httpContext)) {
					return null; // login failed
				}
			}

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
			if (client != null) client.close();
			ACRA.getErrorReporter().handleSilentException(e);
			Log.v(TymyReader.TAG, "Error while downloading " + url + page, e);
		} finally {
			if (client != null) client.close();
		}
		return output.toString();
	}

	private String httpPost(String url, String page, String user, String pass, HttpContext httpContext, List<NameValuePair> nameValuePairs) {
		// Create a new HttpClient and Post Header
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		HttpPost httpPost = new HttpPost("http://" + url + page);
		StringBuilder output = new StringBuilder();

		try {
			if (!isLogged(httpContext, user)) { //not logged
				if (!login(url, user, pass, httpContext)) {
					return null; // login failed
				}
			}
			
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

			// Execute HTTP Post Request
			final HttpResponse response = client.execute(httpPost, httpContext);
			final int statusCode = response.getStatusLine().getStatusCode();
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
			httpPost.abort();
			if (client != null) client.close();			
			ACRA.getErrorReporter().handleSilentException(e);
			Log.v(TymyReader.TAG, "Error while POST " + url, e);
		} catch (IOException e) {
			if (client != null) client.close();			
			ACRA.getErrorReporter().handleSilentException(e);
			Log.v(TymyReader.TAG, "Error while POST " + url, e);
		} finally {
			if (client != null) client.close();			
		}
		return output.toString();
	}
}
