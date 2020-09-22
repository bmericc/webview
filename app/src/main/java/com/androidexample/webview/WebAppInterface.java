package com.androidexample.webview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.sql.Date;
import java.text.SimpleDateFormat;

import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;



import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;



import android.os.Build;
import android.os.Bundle;
import android.os.Environment;



public class WebAppInterface {
	private Context mContext;
	private WebView web;
    
	
	private static final int FILECHOOSER_RESULTCODE   = 2888;
	private final static int KITKAT_RESULTCODE = 2;

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	

    private static final int REQUEST_GALLERY = 1;

    protected static final int REQUEST_PICK_CROP_IMAGE = 2;
	
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private Uri fileUri;

    
 
    /** Instantiate the interface and set the context */
    WebAppInterface(Context c, WebView web) {
        this.mContext = c;
        this.web = web;
    }
 
    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
    	Log.d("bahri", "js tetiklendi showToast");
        Toast.makeText(this.mContext, toast, Toast.LENGTH_SHORT).show();
    }
    
    public void showAndroid() {
    	Log.d("bahri", "js tetiklendi showAndroid");    	   
    }
    
    public void showPicker( ValueCallback<Uri> uploadMsg ){  
        // Here is part of the issue, the uploadMsg is null since it is not triggered from Android
    	Log.d("bahri", "js tetiklendi showPicker");
        mUploadMessage = uploadMsg; 
       /* Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");*/
        //startActivityForResult(Intent.createChooser(i, "File Chooser"), KITKAT_RESULTCODE);
    }

    
    protected void onActivityResult(int requestCode, int resultCode,  
	                                   Intent intent) { 
    
    	Log.d("bahri", "requestCode"+requestCode);
    	Log.d("bahri", "resultCode"+resultCode);
    	
    	
    	
    }
	
	
	  /**
     * Intent - Move to next screen
     */
    public void moveToNextScreen(){
    	
    	 Log.d("bahri", "moveToNextScreen");
    	
         AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.mContext);
         // Setting Dialog Title
         alertDialog.setTitle("Alert");
         // Setting Dialog Message
         alertDialog.setMessage("Are you sure you want to leave to next screen?");
         // Setting Positive "Yes" Button
         alertDialog.setPositiveButton("YES",
                 new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d("bahri", "yes");
					}
                 });
         // Setting Negative "NO" Button
         alertDialog.setNegativeButton("NO",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         // Cancel Dialog
                    	 
                    	 Log.d("bahri", "no");
                         dialog.cancel();
                     }
                 });
         // Showing Alert Message
         alertDialog.show();
    }
    

    @JavascriptInterface
    public void addEventListener(String method, String trigger) {
    	
    	Log.d("bahri", "add parameter -> " + method + "<->" + trigger);
    	
    }

    public void removeEventListener(String method, String trigger) {
    	
    	Log.d("bahri", "remove parameter -> " + method + "<->" + trigger);
    	
    }
    

    @JavascriptInterface
    public void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
	            	Log.d("bahri", "Take Photo");                	
	            	mContext.sendBroadcast(new Intent("show").putExtra("TakePhoto", true));         
	            	/* startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);*/
                }
                else if (options[item].equals("Choose from Gallery"))
                {                	
	            	Log.d("bahri", "Choose from Gallery");                	  
	            	mContext.sendBroadcast(new Intent("show").putExtra("ChoosePhoto", true));                
	            	/* startActivityForResult(pickCropImageIntent,REQUEST_PICK_CROP_IMAGE);  */
                } else if (options[item].equals("Cancel")) {
	            	dialog.dismiss();
                }
            }
			
        });
        builder.show();
    }
    
    
    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
          return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        SimpleDateFormat timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
    
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @author paulburke
	 * @source http://stackoverflow.com/a/20559175
	 */

    
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
    
    
}