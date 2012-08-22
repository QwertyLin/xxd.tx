package q.util;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public final class QFile {
	
	public static String root;
		
	public static final void init(Context ctx){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//挂载sd卡
			root = Environment.getExternalStorageDirectory().getPath() + "/" + ctx.getPackageName() + "/";
		}else{
			root = ctx.getCacheDir() + "/";
		}
		File file = new File(root);
		if(!file.exists()){
			file.mkdirs();
		}
		//System.out.println(Environment.getExternalStorageDirectory().getPath() + "/" + ctx.getPackageName() + "/");
		//System.out.println(ctx.getCacheDir() + "/");
	}
	
	public static String get(String dir){
		File file = new File(root + dir);
		if(!file.exists()){
			file.mkdirs();
		}
		return file.getPath() + "/";
	}
	
	
	
	public String toString(Context ctx) {
		StringBuffer sb = new StringBuffer();
		sb.append("/***\n");
		sb.append("system root dir = " + Environment.getRootDirectory() + "\n");
		sb.append("external storage dir = " + Environment.getExternalStorageDirectory() + "\n");
		sb.append("external storage state = " + Environment.getExternalStorageState() + "\n");
		sb.append("system dcim dir = " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "\n");
		sb.append("app file dir = " + ctx.getFilesDir() + "\n");
		sb.append("app cache dir = " + ctx.getCacheDir() + "\n");
		sb.append("***/");
		return sb.toString();
	}
}
