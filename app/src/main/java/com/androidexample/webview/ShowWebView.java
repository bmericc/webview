package com.androidexample.webview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;




public class ShowWebView extends Activity {

	//private Button button;
	private WebView webView;
	
	final Activity activity = this;
	
	public Uri imageUri;
	
	private static final int FILECHOOSER_RESULTCODE   = 2888;
	private final static int KITKAT_RESULTCODE = 2;

	
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.show_web_view);
		registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent1) {
				// TODO Auto-generated method stub
										
				Boolean ChoosePhoto = intent1.getExtras().getBoolean("ChoosePhoto");
				Boolean TakePhoto = intent1.getExtras().getBoolean("TakePhoto");
				
				if(TakePhoto) {
					
					try{	
		                final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		                startActivityForResult(captureIntent, KITKAT_RESULTCODE);
					   }
		             catch(Exception e){
		            	 Toast.makeText(getBaseContext(), "Camera Exception:"+e, Toast.LENGTH_LONG).show();
		            	 Log.d("bahri", "Camera Exception:"+e);
		             }
				}
				
				if(ChoosePhoto) {
			        Intent intent = new Intent(Intent.ACTION_PICK);
			        intent.setType("image/*");    
	                startActivityForResult(intent, KITKAT_RESULTCODE);
				}
				
			}
		}, new IntentFilter("show"));
		    
	    // Define url that will open in webview	
		//String webViewUrl = "file:///android_asset/index.html";
		String webViewUrl = "https://secure.prj.be/webSqlApp/";
		
		//Get webview 
		webView = (WebView) findViewById(R.id.webView1);   
		//startWebView("http://50.73.3.244/Mobile/");
		
		// Javascript inabled on webview  
	    webView.getSettings().setJavaScriptEnabled(true);
	    
	    
	    // Other webview options
	    webView.getSettings().setLoadWithOverviewMode(true);
	    
	    //webView.getSettings().setUseWideViewPort(true);
	    
	    //Other webview settings
	    webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
	    webView.setScrollbarFadingEnabled(false);
	    webView.getSettings().setBuiltInZoomControls(true);
	    webView.getSettings().setPluginState(PluginState.ON);
	    webView.getSettings().setAllowFileAccess(true);
	    webView.getSettings().setSupportZoom(true); 
	    
	    webView.getSettings().setDomStorageEnabled(true);
	    webView.getSettings().setDatabaseEnabled(true);    	   
	
	    webView.getSettings().setAllowFileAccess(true);
	    webView.getSettings().setAllowContentAccess(true);
	    
	    webView.addJavascriptInterface(new WebAppInterface(this, webView), "JSInterface");
	    
	    
	    
	    //Load url in webview
	    webView.loadUrl(webViewUrl);
	    
	    // Define Webview manage classes
		startWebView(); 
		
	} 
	
	private void startWebView() {
	    
		
		
		//Create new webview Client to show progress dialog
		//Called When opening a url or click on link
		
		webView.setWebViewClient(new WebViewClient() {      
	        ProgressDialog progressDialog;
	     
	        //If you will not use this method url links are open in new brower not in webview
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {              
	        	
	        	// Check if Url contains ExternalLinks string in url 
	        	// then open url in new browser
	        	// else all webview links will open in webview browser
	        	if(url.contains("ExternalLinks")){ 
	        		
	        		// Could be cleverer and use a regex
	        		//Open links in new browser
	        		view.getContext().startActivity(
	                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	        		
	        		// Here we can open new activity
	                
	        		return true;
	        		
	        	} else {
	        		
	        		// Stay within this webview and load url
	                view.loadUrl(url); 
	                return true;
	            }
	        	  
	        }
	        
	        
	        
	        //Show loader on url load
	        public void onLoadResource (WebView view, String url) {
	        
	         	// if url contains string androidexample
	        	// Then show progress  Dialog
	            if (progressDialog == null && url.contains("androidexample") 
	            		) {
	            	
	                // in standard case YourActivity.this
	                progressDialog = new ProgressDialog(ShowWebView.this);
	                progressDialog.setMessage("Loading...");
	                progressDialog.show();
	            }
	        }
	        
	        // Called when all page resources loaded
	        public void onPageFinished(WebView view, String url) {
	        	
	            try{
	            	// Close progressDialog
		            if (progressDialog.isShowing()) {
		                progressDialog.dismiss();
		                progressDialog = null;
		            }
	            }catch(Exception exception){
	                exception.printStackTrace();
	            }
	        }
	       
	    }); 
	     
	      
	    
	    // implement WebChromeClient inner class
		// we will define openFileChooser for select file from camera
		  
	    webView.setWebChromeClient(new WebChromeClient() {
	    	
	    	// openFileChooser for Android 3.0+
	        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){  
	           /**updated, out of the IF **/
	        		mUploadMessage = uploadMsg;
	           /**updated, out of the IF **/	                            	           
	            	
	            try{	
	            	File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Uygulamam");
	                if (!imageStorageDir.exists()) {
	                    imageStorageDir.mkdirs();
	                }
	                File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
	                mCapturedImageURI = Uri.fromFile(file); // save to the private variable

	                final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
	               // captureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	                Intent i = new Intent(Intent.ACTION_GET_CONTENT); 
	                i.addCategory(Intent.CATEGORY_OPENABLE);
	                i.setType("image/*");

	                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
	                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[] { captureIntent });

	                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
	              }
	             catch(Exception e){
	            	 Toast.makeText(getBaseContext(), "Camera Exception:"+e, Toast.LENGTH_LONG).show();
	             }
	            //}
	        }
	        
	        // openFileChooser for Android < 3.0
	        public void openFileChooser(ValueCallback<Uri> uploadMsg){
	            openFileChooser(uploadMsg, "");
	        }
	        
	        //openFileChooser for other Android versions
	        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
	            openFileChooser(uploadMsg, acceptType);
	        }

	        /** Added code to clarify chooser. **/

	        //The webPage has 2 filechoosers and will send a console message informing what action to perform, taking a photo or updating the file
	        public boolean onConsoleMessage(ConsoleMessage cm) {        
	            onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
	        	//Toast.makeText(getBaseContext(), cm.message()+" :message", Toast.LENGTH_LONG).show();
	            return true;
	        }
	        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
	            //Log.d("androidruntime", "Per c�nsola: " + message);
	            //Toast.makeText(getBaseContext(), message+":message", Toast.LENGTH_LONG).show();
	            //if(message.endsWith("foto")){ boolFileChooser= true; }
	            //else if(message.endsWith("pujada")){ boolFileChooser= false; }
	        }
	        /** Added code to clarify chooser. **/
	    	
	    });
	   


	     
	     
	}
	
	// Return here when file selected from camera or from SDcard
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode,  
	                                   Intent intent) { 
		 
	Log.d("bahri", "("+requestCode+ ") - (" +resultCode  + ") - (" + intent + ") - " + mUploadMessage);		
		
	 Uri result=null;	
		
	 if(requestCode==FILECHOOSER_RESULTCODE) {  	   
		   if (null == this.mUploadMessage) {
	            return;
	       }		   	   
		   try{
		        if (resultCode != RESULT_OK) {		        	
		            result = null;		            
		        } else {		        	
		        	// retrieve from the private variable if the intent is null
		            result = intent == null ? mCapturedImageURI : intent.getData(); 
		        } 
		    }
	        catch(Exception e)
	        {
	            Toast.makeText(getApplicationContext(), "activity :"+e, Toast.LENGTH_LONG).show();
	        }	       
		    mUploadMessage.onReceiveValue(result);
		    mUploadMessage = null;	 
	 }else if (requestCode==KITKAT_RESULTCODE) {
		 
			Log.d("bahri", "resultCode Activity ->"+resultCode);
			Log.d("bahri", "requestCode Activity ->"+requestCode);
			
			final int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION  | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);			
			
			result = intent.getData();  		      
		    String path = getPath( this, result);  
		    File selectedFile = new File(path); 
		    
		    Log.d("bahri", "file ->" + selectedFile.toString());
		    Log.d("bahri", "path ->" + path.toString());
		    Log.d("bahri", "takeFlags ->" + takeFlags);
			
			    
		    CharSequence[] sendJs = { path.toString()};
		    
		    Log.d("bahri", "sendJs "+sendJs.toString());
		    
			callJavaScript("fixAFU_onFileChoose", sendJs);
			
			String Base64Image = null;
			try {
				Base64Image = readImage( path.toString() );
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			Base64Image = Base64Image.substring(0, 70);
			
		    CharSequence[] sendBase64 = { Base64Image };
		    
		    Log.d("bahri", "sendBase64 "+sendBase64.toString());
	
			callJavaScript("fixAFU_base64Image", sendBase64);

			
			
	 }
		
	}
	
	 public String readImage(String path) throws FileNotFoundException {
	    	
    	InputStream inputStream = new FileInputStream(path);//You can get an inputStream using any IO API
    	byte[] bytes;
    	byte[] buffer = new byte[8192];
    	int bytesRead;
    	ByteArrayOutputStream output = new ByteArrayOutputStream();
    	try {
    	    while ((bytesRead = inputStream.read(buffer)) != -1) {
    	    output.write(buffer, 0, bytesRead);
    	}
    	} catch (IOException e) {
    	e.printStackTrace();
    	}
    	bytes = output.toByteArray();
    	String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
    	
    	return encodedString;	    	
	}
	
	private void callJavaScript(String methodName, Object...params){
	    StringBuilder stringBuilder = new StringBuilder();
	    stringBuilder.append("javascript:try{");
	    stringBuilder.append(methodName);
	    stringBuilder.append("(");
	    for (Object param : params) {
	        if(param instanceof String){
	            stringBuilder.append("'");
	            stringBuilder.append(param);
	            stringBuilder.append("'");
	        }
	        stringBuilder.append(",");
	    }
	    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
	    
	    stringBuilder.append(")}catch(error){Android.onError(error.message);}");	    
	    
	    Log.d("bahri", "url  " + stringBuilder.toString());
	    webView.loadUrl(stringBuilder.toString());
	}
		
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String getPath(final Context context, final Uri uri) {

	    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

	    // DocumentProvider
	    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            return getDataColumn(context, contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            return getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {
	        return getDataColumn(context, uri, null, null);
	    }
	    // File
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 * @source http://stackoverflow.com/a/20559175
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	                                   String[] selectionArgs) {

	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
	                null);
	        if (cursor != null && cursor.moveToFirst()) {
	            final int column_index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(column_index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 * @source http://stackoverflow.com/a/20559175
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 * @source http://stackoverflow.com/a/20559175
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 * @source http://stackoverflow.com/a/20559175
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}  
	
	@Override
    // Detect when the back button is pressed
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }
	
	




}