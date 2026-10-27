package com.android.insecurebankv2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.os.Environment;
import android.widget.Toast;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.marcohc.toasteroid.Toasteroid;

/*
The page that allows the user to transfer an amount between two accounts
@author Dinesh Shetty
*/
public class DoTransfer extends Activity {

	String result;
	String passNormalized;
	String usernameBase64ByteString;
	BufferedReader reader;
	//	The EditText that holds the from account number
	EditText from;
	//	The EditText that holds the to account number
	EditText to;
	//	The EditText that holds the to amount to be transferred between the accounts
	EditText amount;
	/*The EditText that takes the Phone number as input from the user. A confirmation of 
	successful transfer is sent to this phone number*/
	EditText phoneNumber;
	String number = "5554";
	//	The Button that handles the from and to account autofill operation on the basis of logged in user
	Button getAccounts;
	//	The Button that handles the transfer operation activity
	Button transfer;
	String acc1, acc2;
	HttpResponse responseBody;
	JSONObject jsonObject;
	InputStream in ;
	String serverip = "";
	String serverport = "";
	String protocol = "http://";
	Button button1;
	SharedPreferences serverDetails;
	public static final String MYPREFS2 = "mySharedPreferences";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_do_transfer);

        // Get Server details from Shared Preference file.
        serverDetails = PreferenceManager.getDefaultSharedPreferences(this);
		serverip = serverDetails.getString("serverip", null);
		serverport = serverDetails.getString("serverport", null);

        // Handle the transfer functionality
		transfer = (Button) findViewById(R.id.button_Transfer);
		transfer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				from = (EditText) findViewById(R.id.editText_from);
				to = (EditText) findViewById(R.id.editText_to);
				new RequestDoTransferTask().execute("username");
			}
		});

		button1 = (Button) findViewById(R.id.button_CreateUser);
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new RequestDoGets2().execute("username");
			}
		});
	}

	public class RequestDoTransferTask extends AsyncTask < String, String, String > {
		String fromAcc, toAcc, transferAmount;

		/**
		 * constructor
		 * @return 
		 */
		public void AsyncHttpTransferPost(String string) {
			//do something
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			from = (EditText) findViewById(R.id.editText_from);
			to = (EditText) findViewById(R.id.editText_to);
			amount = (EditText) findViewById(R.id.editText_amount);
			fromAcc = from != null && from.getText() != null ? from.getText().toString() : "";
			toAcc = to != null && to.getText() != null ? to.getText().toString() : "";
			transferAmount = amount != null && amount.getText() != null ? amount.getText().toString() : "";
		}

		/**
		 * background functions
		 */
		@Override
		protected String doInBackground(String...params) {
			try {
				String str = "";
				str = "dinesh";
				SharedPreferences settings = getSharedPreferences(MYPREFS2, 0);
				final String username = settings.getString("EncryptedUsername", null);
				if (username == null) {
					return null;
				}
				byte[] usernameBase64Byte = Base64.decode(username, Base64.DEFAULT);
				try {
					usernameBase64ByteString = new String(usernameBase64Byte, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				final String password = settings.getString("superSecurePassword", null);
				if (password != null) {
					try {
						passNormalized = getNormalizedPassword(password);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}

				URL url = new URL(protocol + serverip + ":" + serverport + "/dotransfer");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setDoOutput(true);
				conn.setDoInput(true);

				String postDataParams = "username=" + URLEncoder.encode(usernameBase64ByteString, "UTF-8") +
						"&password=" + URLEncoder.encode(passNormalized != null ? passNormalized : "", "UTF-8") +
						"&from_acc=" + URLEncoder.encode(fromAcc, "UTF-8") +
						"&to_acc=" + URLEncoder.encode(toAcc, "UTF-8") +
						"&amount=" + URLEncoder.encode(transferAmount, "UTF-8");

				OutputStream os = conn.getOutputStream();
				os.write(postDataParams.getBytes("UTF-8"));
				os.flush();
				os.close();

				int responseCode = conn.getResponseCode();
				InputStream in = (responseCode >= 400) ? conn.getErrorStream() : conn.getInputStream();
				if (in != null) {
					result = convertStreamToString( in );
				} else {
					result = "";
				}

				if (result != null) {
					result = result.replace("\n", "");
				} else {
					result = "";
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						AsyncHttpTransferPost("result");
						if (result != null) {
							if (result.indexOf("Success") != -1) {
								Toasteroid.show(DoTransfer.this, "Transfer Successful!!", Toasteroid.STYLES.SUCCESS, Toasteroid.LENGTH_SHORT);

								try {
									jsonObject = new JSONObject(result);
									acc1 = jsonObject.getString("from");
									acc2 = jsonObject.getString("to");
									System.out.println("Message:" + jsonObject.getString("message") + " From:" + fromAcc + " To:" + toAcc + " Amount:" + transferAmount);
									final String status = new String("\nMessage:" + "Success" + " From:" + fromAcc + " To:" + toAcc + " Amount:" + transferAmount + "\n");
									try {
										File file = new File(getExternalFilesDir(null), "Statements_" + usernameBase64ByteString + ".html");
										BufferedWriter out2 = new BufferedWriter(new FileWriter(file, true));
										out2.write(status);
										out2.write("<hr>");
										out2.close();
									} catch (IOException e) {
										e.toString();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							} else {
								Toasteroid.show(DoTransfer.this, "Transfer Failed!!", Toasteroid.STYLES.ERROR, Toasteroid.LENGTH_SHORT);

								System.out.println("Message:" + "Failure" + " From:" + fromAcc + " To:" + toAcc + " Amount:" + transferAmount);
								final String status = new String("\nMessage:" + "Failure" + " From:" + fromAcc + " To:" + toAcc + " Amount:" + transferAmount + "\n");
								try {
									File file = new File(getExternalFilesDir(null), "Statements_" + usernameBase64ByteString + ".html");
									BufferedWriter out2 = new BufferedWriter(new FileWriter(file, true));
									out2.write(status);
									out2.write("<hr>");
									out2.close();
								} catch (IOException e) {
									e.toString();
								}
							}
						}
					}

				});
				return str;
			} catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {}

		protected void onProgressUpdate(String...progress) {}
	}

	public class RequestDoGets2 extends AsyncTask < String, String, String > {
		/**
		 * constructor
		 * @return 
		 */
		public void AsyncHttpPost(String string) {
			//do something
		}

		/**
		 * background operations
		 */
		@Override
		public String doInBackground(String...params) {
			try {
				String str = "";
				str = "dinesh";
				SharedPreferences settings = getSharedPreferences(MYPREFS2, 0);
				final String username = settings.getString("EncryptedUsername", null);
				if (username == null) {
					return null;
				}
				byte[] usernameBase64Byte = Base64.decode(username, Base64.DEFAULT);

				try {
					usernameBase64ByteString = new String(usernameBase64Byte, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				final String password = settings.getString("superSecurePassword", null);
				if (password != null) {
					try {
						passNormalized = getNormalizedPassword(password);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}

				URL url = new URL(protocol + serverip + ":" + serverport + "/getaccounts");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setDoOutput(true);
				conn.setDoInput(true);

				String postDataParams = "username=" + URLEncoder.encode(usernameBase64ByteString, "UTF-8") +
						"&password=" + URLEncoder.encode(passNormalized != null ? passNormalized : "", "UTF-8");

				OutputStream os = conn.getOutputStream();
				os.write(postDataParams.getBytes("UTF-8"));
				os.flush();
				os.close();

				int responseCode = conn.getResponseCode();
				InputStream in = (responseCode >= 400) ? conn.getErrorStream() : conn.getInputStream();
				if (in != null) {
					result = convertStreamToString( in );
				} else {
					result = "";
				}

				if (result != null) {
					result = result.replace("\n", "");
					if (result.indexOf("Correct") != -1) {
						try {
							jsonObject = new JSONObject(result);
							acc1 = jsonObject.getString("from");
							acc2 = jsonObject.getString("to");
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						AsyncHttpPost("result");
						from = (EditText) findViewById(R.id.editText_from);
						to = (EditText) findViewById(R.id.editText_to);
						if (from != null && acc1 != null) from.setText(acc1);
						if (to != null && acc2 != null) to.setText(acc2);
					}
				});
				return str;
			} catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}

		/**
		 * on getting result
		 */
		@Override
		public void onPostExecute(String result) {}

		public void onProgressUpdate(String...progress) {}
	}

	/*
	The function that handles the aes256 decryption of the password from the encrypted password.
	password: Encrypted password input to the aes function
	returns: Plaintext password outputted by the aes function
	*/
	private String getNormalizedPassword(String password) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		CryptoClass crypt = new CryptoClass();
		return crypt.aesDeccryptedString(password);
	}


	public String convertStreamToString(InputStream in ) throws IOException {
		// TODO Auto-generated method stub
		try {
			reader = new BufferedReader(new InputStreamReader( in , "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		} in .close();
		return sb.toString();
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
        // Handle action bar item clicks here. The action bar wil
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            return true;
        } else if (id == R.id.action_settings) {
            callPreferences();
            return true;
        } else if (id == R.id.action_exit) {
            Intent i = new Intent(getBaseContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

	}

	public void callPreferences() {
		// TODO Auto-generated method stub
		Intent i = new Intent(this, FilePrefActivity.class);
		startActivity(i);
	}
}