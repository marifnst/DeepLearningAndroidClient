package com.dlclient.main;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

	final int ACTIVITY_CHOOSE_FILE = 1;
	private WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);

		webview.addJavascriptInterface(new Object() {
			@JavascriptInterface
			public void performClick() throws Exception {
				// Toast.makeText(MainActivity.this, "Login clicked",
				// Toast.LENGTH_LONG).show();
				Intent chooseFile;
				Intent intent;
				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFile.setType("file/*");
				intent = Intent.createChooser(chooseFile, "Choose a file");
				startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
			}
		}, "login");
		webview.loadUrl("file:///android_asset/index.html");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch(requestCode) {
	      case ACTIVITY_CHOOSE_FILE: {
	        if (resultCode == RESULT_OK){
	          Uri uri = data.getData();
	          String filePath = uri.getPath();
	          webview.loadUrl("javascript:setPathValue('" + filePath + "');");
	        }
	      }
	    }
	  }
}
