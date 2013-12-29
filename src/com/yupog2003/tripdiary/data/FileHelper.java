package com.yupog2003.tripdiary.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.input.CountingInputStream;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.yupog2003.tripdiary.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FileHelper {
	public static FilenameFilter getDirFilter() {
		return new FilenameFilter() {

			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				return new File(dir, filename).isDirectory() && !filename.startsWith(".");
			}

		};
	}

	public static void copyFile(File infile, File outfile) {
		byte[] buffer = new byte[4096];
		int size = -1;
		try {
			FileInputStream fis = new FileInputStream(infile);
			FileOutputStream fos = new FileOutputStream(outfile);
			while ((size = fis.read(buffer, 0, buffer.length)) != -1) {
				fos.write(buffer, 0, size);
			}
			fis.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void deletedir(String path) {
		File file = new File(path);
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				deletedir(files[i].getPath());
			}
			files[i].delete();
		}
		file.delete();
	}

	public static void concateFile(String file1, String file2) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file1, true));
			BufferedReader br = new BufferedReader(new FileReader(file2));
			String s;
			bw.write("\n");
			bw.flush();
			while ((s = br.readLine()) != null) {
				if (s.contains("<trkpt") || s.contains("<ele>") || s.contains("<time>") || s.contains("</trkpt>")) {
					bw.write(s + "\n");
					bw.flush();
				}
			}
			bw.close();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressLint("DefaultLocale")
	public static String getMIMEtype(String filename) {
		if (!filename.contains("."))
			return "file/*";
		String extension = filename.substring(filename.lastIndexOf(".") + 1);
		if (extension == null)
			return "file/*";
		String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
		if (mime == null)
			return "file/*";
		return mime;
	}

	public static String getMimeFromFile(File file) {
		return getMIMEtype(file.getName());
	}

	public static boolean isPicture(File file) {
		return file.isFile() && getMimeFromFile(file).startsWith("image");
	}

	public static boolean isVideo(File file) {
		return file.isFile() && getMimeFromFile(file).startsWith("video");
	}

	public static boolean isAudio(File file) {
		return file.isFile() && getMimeFromFile(file).startsWith("audio");
	}

	public static FileFilter getPictureFileFilter() {
		return new FileFilter() {

			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return isPicture(pathname);
			}
		};
	}

	public static FileFilter getVideoFileFilter() {
		return new FileFilter() {

			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return isVideo(pathname);
			}
		};
	}

	public static FileFilter getAudioFileFilter() {
		return new FileFilter() {

			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return isAudio(pathname);
			}
		};
	}

	public static void saveObjectToFile(Object obj, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Object readObjectFromFile(File file,final GpxAnalyzer2.ProgressChangedListener listener) {
		Object result = new Object();
		try {
			FileInputStream fis = new FileInputStream(file);
			final CountingInputStream cis = new CountingInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(cis);
			final long fileSize=file.length();
			if (listener!=null){
				new Thread(new Runnable() {
					
					public void run() {
						// TODO Auto-generated method stub
						long count;
						while((count=cis.getByteCount())<fileSize){
							listener.onProgressChanged(count);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}).start();
			}
			result = ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static void zip(File source, File zip) {
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
			dozip(source, zos, source.getName());
			zos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void dozip(File from, ZipOutputStream zos, String entry) {
		try {
			if (from.isDirectory()) {

				zos.putNextEntry(new ZipEntry(entry + "/"));
				zos.closeEntry();
				File[] files = from.listFiles();
				for (int i = 0; i < files.length; i++) {
					dozip(files[i], zos, entry + "/" + files[i].getName());
				}
			} else {
				zos.putNextEntry(new ZipEntry(entry));
				byte[] buffer = new byte[4096];
				int count = -1;
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(from));
				while ((count = bis.read(buffer, 0, 4096)) != -1) {
					zos.write(buffer, 0, count);
				}
				bis.close();
				zos.closeEntry();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void unZip(String zip, String target) {
		BufferedOutputStream bos = null;
		try {
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
			ZipEntry entry;
			String strEntry;
			int count;
			while ((entry = zis.getNextEntry()) != null) {
				byte[] data = new byte[4096];
				strEntry = entry.getName();
				if (entry.isDirectory()) {
					new File(target + strEntry).mkdirs();
				} else {
					File entryFile = new File(target + strEntry);
					File entryDir = new File(entryFile.getParent());
					if (!entryDir.exists()) {
						entryDir.mkdirs();
					}
					FileOutputStream fos = new FileOutputStream(entryFile);
					bos = new BufferedOutputStream(fos, 4096);
					while ((count = zis.read(data, 0, 4096)) != -1) {
						bos.write(data, 0, count);
					}
					bos.flush();
					bos.close();
				}
			}
			zis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void converToPlayableKml(File gpxFile, File kmlFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(gpxFile));
			StringBuffer sb = new StringBuffer();
			String s;
			while ((s = br.readLine()) != null) {
				if (s.contains("<trkpt")) {
					String[] toks = s.split("\"");
					if (s.indexOf("lat") > s.indexOf("lon")) {
						sb.append("<gx:coord>" + toks[1] + " " + toks[3] + " 0</gx:coord>\n");
					} else {
						sb.append("<gx:coord>" + toks[3] + " " + toks[1] + " 0</gx:coord>\n");
					}
				} else if (s.contains("<time>")) {
					sb.append("<when>" + s.substring(s.indexOf(">") + 1, s.lastIndexOf("<")) + "</when>\n");
				}
			}
			br.close();
			BufferedWriter bw = new BufferedWriter(new FileWriter(kmlFile, false));
			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			bw.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n");
			bw.write("xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n");
			bw.write("<Folder>\n");
			bw.write("<Placemark id=\"track\">\n");
			bw.write("<gx:Track>\n");
			bw.write(sb.toString());
			bw.write("</gx:Track>\n");
			bw.write("</Placemark>\n");
			bw.write("</Folder>\n");
			bw.write("</kml>");
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void convertToKml(ArrayList<Marker> POIs, LatLng[] track, File kmlFile, String note) {
		String name = kmlFile.getName();
		name = name.substring(0, name.lastIndexOf("."));
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(kmlFile, false));
			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			bw.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
			bw.write("<Document>\n");
			bw.write("<name>" + name + "</name>\n");
			bw.write("<description>" + note + "</description>\n");
			bw.write("<Folder>\n");
			bw.write("<Placemark>\n");
			bw.write("<name>" + name + "</name>\n");
			bw.write("<description>Generated by TripDiary</description>\n");
			bw.write("<LineString>\n");
			bw.write("<coordinates>");
			for (int i = 0; i < track.length; i++) {
				bw.write(" " + String.valueOf(track[i].longitude) + "," + String.valueOf(track[i].latitude) + ",0\n");
			}
			bw.write("</coordinates>\n");
			bw.write("</LineString>\n");
			bw.write("</Placemark>\n");
			final int POIsSize = POIs.size();
			for (int i = 0; i < POIsSize; i++) {
				bw.write("<Placemark>\n");
				bw.write("<name>" + POIs.get(i).getTitle() + "</name>\n");
				bw.write("<description>" + POIs.get(i).getSnippet() + "</description>\n");
				bw.write("<Point>\n");
				bw.write("<coordinates>" + String.valueOf(POIs.get(i).getPosition().longitude) + "," + String.valueOf(POIs.get(i).getPosition().latitude) + ",0</coordinates>\n");
				bw.write("</Point>\n");
				bw.write("</Placemark>\n");
			}
			bw.write("</Folder>\n");
			bw.write("</Document>\n");
			bw.write("</kml>");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class DirAdapter extends BaseAdapter implements OnItemClickListener {

		File root;
		File[] dirs;
		Context context;
		boolean showHideDir;

		public DirAdapter(Context context, boolean showHideDir, File root) {
			this.context = context;
			this.showHideDir = showHideDir;
			setDir(root);
		}

		public void setDir(File root) {
			this.root = root;
			File[] dirss = root.listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					// TODO Auto-generated method stub
					if (!showHideDir && pathname.getName().startsWith("."))
						return false;
					return pathname.isDirectory();
				}
			});
			Arrays.sort(dirss, new Comparator<File>() {

				public int compare(File lhs, File rhs) {
					// TODO Auto-generated method stub
					return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
			});
			dirs = new File[dirss.length + 2];
			dirs[0] = root;
			dirs[1] = root.getParentFile();
			for (int i = 2; i < dirs.length; i++) {
				dirs[i] = dirss[i - 2];
			}
		}

		public File getRoot() {
			return root;
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return dirs.length;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return dirs[position];
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			TextView textView = new TextView(context);
			textView.setTextAppearance(context, android.R.style.TextAppearance_Large);
			textView.setCompoundDrawablesWithIntrinsicBounds(position > 1 ? R.drawable.ic_folder : 0, 0, 0, 0);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			if (position == 0)
				textView.setText(root.getPath());
			else if (position == 1)
				textView.setText("...");
			else
				textView.setText(dirs[position].getName());
			return textView;
		}

		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			// TODO Auto-generated method stub
			if (position > 0) {
				setDir(dirs[position]);
				notifyDataSetChanged();
			}
		}

	}
}
