package com.android.insecurebankv2;

import static com.marcohc.toasteroid.Toasteroid.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.io.File;

/**
 * The page that allows the user to view transaction history for the logged-in user
 * @author Dinesh Shetty
 */
public class ViewStatement extends Activity {
	String uname;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_statement);
		Intent intent = getIntent();
		uname = intent.getStringExtra("uname");
		//String statementLocation=Environment.getExternalStorageDirectory()+ "/Statements_" + uname + ".html";
		String FILENAME="Statements_" + uname + ".html";
		File fileToCheck = new File(getExternalFilesDir(null), FILENAME);
		System.out.println("Checking for file:" + fileToCheck.getAbsolutePath());
		if (fileToCheck.exists()) {
			show(this, "Statement Exists!!", STYLES.WARNING, LENGTH_SHORT);

			WebView mWebView = findViewById(R.id.webView1);
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setDomStorageEnabled(true);
			mWebView.getSettings().setBuiltInZoomControls(true);
			mWebView.setWebViewClient(new MyWebViewClient());
			WebChromeClient cClient = new WebChromeClient();
			mWebView.setWebChromeClient(cClient);
			
			try {
				java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(fileToCheck));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();
				mWebView.loadDataWithBaseURL("file://" + fileToCheck.getAbsolutePath(), sb.toString(), "text/html", "UTF-8", null);
			} catch (Exception e) {
				show(this, "Error reading statement!!", STYLES.WARNING, LENGTH_SHORT);
				System.out.println("Error reading statement: " + e.getLocalizedMessage());
			}
		} else
		{
			Intent gobacktoPostLogin =new Intent(this,PostLogin.class);
			startActivity(gobacktoPostLogin);
			show(this, "Statement does not Exist!!", STYLES.WARNING, LENGTH_SHORT);
		}
	}
	// Added for handling menu operations
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Added for handling menu operations
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			callPreferences();
			return true;
		} else if (id == R.id.action_exit) {
			Intent i = new Intent(getBaseContext(), LoginActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void callPreferences() {
		// TODO Auto-generated method stub
		Intent i = new Intent(this, FilePrefActivity.class);
		startActivity(i);
	}
}