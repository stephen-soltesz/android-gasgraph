package com.thinkseedo.gasgraph.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.GasGraphActivity.ImageAdapter.ImageHolder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DrawableManager {

    private final Map<String, Drawable> drawableMap;
    private String[] mGraphList;
    private int mW;
    private int mH;
    private ImageHolder mIH;
    
    public DrawableManager(String[] graph_list, int width, int height, ImageHolder ih) {
        drawableMap = new HashMap<String, Drawable>();
        mGraphList = graph_list;
        mW = width;
        mH = height;
        setmIH(ih);
    }
    
    public void fetchAllImages() {
    	Lg.d("fetch all images");
        Thread thread = new Thread() {
            @Override
            public void run() {
		      	GraphGenerator g = new GraphGenerator();
				for ( int i=1; i < mGraphList.length; i++) {
					File f = getFileFor(mGraphList[i]);
					Lg.d("file " + f.toString());
					if ( ! f.exists() ) {
						Lg.d("file does not exist");
						Lg.d("get: " + mGraphList[i]);
						String[] graphFields = App.mUnitAbbv.get(mGraphList[i]);
						Lg.d("gf.length: " + graphFields.length);
						Lg.d("gf0: " + graphFields[0]);
						Lg.d("gf3: " + graphFields[3]);
		      			String url=g.getGraphURL(mW,mH,graphFields[3],graphFields[0],
		    					 		App.mRecords.getValuesFor(graphFields[0]), 
		    					 		App.mRecords.getDatesFor(graphFields[0]));
		      			if ( url == null ) {
		      				continue;
		      			}
		    			try {
							fetch(url, graphFields[0]);
							//Thread.sleep(1000);
						} catch (MalformedURLException e) {
							App.postDataError("MalformedURL", "failed url: " + url, e.toString());
						//} catch (InterruptedException e) {
						//	App.postDataError("Interrupted", "IO error url:" + url, e.toString());
						} catch (IOException e) {
							App.postDataError("IO", "IO error url:" + url, e.toString());
						}
					}
				}
            }
        };
        thread.start();
    }

    public Drawable fetchDrawable(final String tag, String urlString) {
        if (drawableMap.containsKey(tag)) {
            return drawableMap.get(tag);
        }
        if ( urlString == null ) { 
        	return drawableFromFile(tag);
        }

        Lg.d("image tag:" + tag);
        try {
        	Drawable drawable = fetch(urlString, tag);
            if (drawable != null) {
                drawableMap.put(tag, drawable);
                Lg.d( "got a thumbnail drawable: " + drawable.getBounds() + ", "
                        + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
                        + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
            } else {
              Lg.w("could not get thumbnail");
            }
            return drawable;
            
        } catch (MalformedURLException e) {
			App.postDataError("MalformedURL", "failed url: " + urlString, e.toString());
            return null;
        } catch (IOException e) {
			App.postDataError("Interrupted", "IO error url:" + urlString, e.toString());
            return null;
        }
    }

   
    public void fetchDrawableWithThread(final String tag, 
    			final String urlString, 
    			final ImageView imageView,
    			final ProgressBar progressBar,
    			final TextView textView) {
        if (drawableMap.containsKey(tag)) {
            imageView.setImageDrawable(drawableMap.get(tag));
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                imageView.setImageDrawable((Drawable) message.obj);
                progressBar.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
    			imageView.setVisibility(View.VISIBLE);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                Drawable drawable = fetchDrawable(tag, urlString);
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }
    private Drawable drawableFromFile(final String tag) {
       	//File fList = new File(App.getContext().getFilesDir(), ".");
       	//FilenameFilter filter = new FilenameFilter() {
		//	@Override
		//	public boolean accept(File dir, String file) {
		//		if ( file.matches(tag + ".*.png") ) {
		//			return true;
		//		}
		//		return false;
		//	}
       	//};
       	//File[] list = fList.listFiles(filter);
       	//if ( list.length == 0 ) return null;
       	
        InputStream is=null;
		try {
			is = new FileInputStream(getFileFor(tag));
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			return null; 
		}
		return Drawable.createFromStream(is, "src");
    }
    
    private InputStream inputStreamToFile(InputStream is, File output) throws FileNotFoundException {
        try {
        	// write the inputStream to a FileOutputStream
        	OutputStream out = App.getContext().openFileOutput(output.getName(), Context.MODE_WORLD_READABLE);
        	int read = 0;
        	byte[] bytes = new byte[4096];
         
        	while ((read = is.read(bytes)) != -1) {
        		out.write(bytes, 0, read);
        	}
        	is.close();
        	out.flush();
        	out.close();
         
        } catch (IOException e) {
        	Lg.d("Exception: " + e);
        }	
		//Lg.d("saved to file: " + output);
        return new FileInputStream(output);
    }
    public static File getFileFor(String tag) {
        //return new File(App.getContext().getFilesDir(), tag + "_" + hash(url) + ".png");
        return new File(App.getContext().getFilesDir(), tag + "x.png");
    }

    private Drawable fetch(String urlString, String tag) throws MalformedURLException, IOException {
        URL myFileUrl= new URL(urlString);
        //byte[] data = getBytesFromURL(myFileUrl);
        File outFile = getFileFor(tag);
        HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
        conn.setDoInput(true); 
        conn.connect(); 
        InputStream is = conn.getInputStream();
        is = inputStreamToFile(is, outFile);
        Drawable drawable = Drawable.createFromStream(is, "src");
        is.close();
        conn.disconnect();
        return drawable;
    }
    
    @SuppressWarnings("unused")
	private byte[] getBytesFromURL(URL url) throws IOException {
    
    	HttpURLConnection urlconn = (HttpURLConnection)url.openConnection();
        urlconn.setDoInput(true); 
        urlconn.connect(); 
    	InputStream is = urlconn.getInputStream();
        int length = urlconn.getContentLength();
        byte[] bytes = new byte[length];
       
        // Read everything
        int offset = 0;
        int numRead = 0;
        while ((offset < bytes.length) && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read request body");
        }
        
        // clean-up connections
        is.close();
        urlconn.disconnect();
        return bytes;
    }
     
    @SuppressWarnings("unused")
	private void writeFile(byte[] b, String file) throws IOException {
    	FileOutputStream f = new FileOutputStream(new File(file));
    	f.write(b);
    }

	public ImageHolder getmIH() {
		return mIH;
	}

	public void setmIH(ImageHolder mIH) {
		this.mIH = mIH;
	}
}
