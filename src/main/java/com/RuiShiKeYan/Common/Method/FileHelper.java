package com.RuiShiKeYan.Common.Method;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class FileHelper {

	public static String GetFilecharset( File file ) {
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		try {
			boolean checked = false;
			BufferedInputStream bis = new BufferedInputStream( new FileInputStream( file ) );
			bis.mark( 0 );
			int read = bis.read( first3Bytes, 0, 3 );
			if ( read == -1 ) return charset;
			if ( first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE ) {
				charset = "UTF-16LE";
				checked = true;
			}
			else if ( first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF ) {
				charset = "UTF-16BE";
				checked = true;
			}
			else if ( first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF ) {
				charset = "UTF-8";
				checked = true;
			}
			bis.reset();
			if ( !checked ) {
				//    int len = 0;
//                int loc = 0;

				while ( (read = bis.read()) != -1 ) {
//                    loc++;
					if ( read >= 0xF0 ) break;
					if ( 0x80 <= read && read <= 0xBF ) // 单独出现BF以下的，也算是GBK
						break;
					if ( 0xC0 <= read && read <= 0xDF ) {
						read = bis.read();
						if ( 0x80 <= read && read <= 0xBF ) // 双字节 (0xC0 - 0xDF) (0x80
							// - 0xBF),也可能在GB编码内
							continue;
						else break;
					}
					else if ( 0xE0 <= read && read <= 0xEF ) {// 也有可能出错，但是几率较小
						read = bis.read();
						if ( 0x80 <= read && read <= 0xBF ) {
							read = bis.read();
							if ( 0x80 <= read && read <= 0xBF ) {
								charset = "UTF-8";
								break;
							}
							else break;
						}
						else break;
					}
				}
				//System.out.println( loc + " " + Integer.toHexString( read ) );
			}

			bis.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		return charset;
	}


	public synchronized static String ReadStringFromPath(String filePath) {
		try {

			File file = new File(filePath);
			if(!file.exists()) return "";
			ReadStringFromPath(new InputStreamReader(new FileInputStream(file), GetFilecharset(file)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 *
	 * @param filePath
	 * 文件路径
	 * @param subflag
	 * true表示传入路径为jar库相对路径，false表示传入绝对路径
	 * @return
	 * ASC文件字符集
	 */
	public synchronized static String ReadStringFromPath(String filePath,boolean subflag) {

		if(subflag) {
			InputStreamReader isr=new InputStreamReader(FileHelper.class.getResourceAsStream(filePath));
			return ReadStringFromPath(isr);
		}
		return ReadStringFromPath(filePath);
	}
	public synchronized static String ReadStringFromPath(InputStreamReader is)
	{
		try
		{
			BufferedReader reader = new BufferedReader(is);
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line + "\n");
			}
			reader.close();
			return builder.toString();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}
	public synchronized static String ReadStringFromPath(String filePath, String charset) {
		try {
			File file = new File(filePath);
			return ReadStringFromPath(new InputStreamReader(new FileInputStream(file), charset));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void CheckDir(String dir) {
		File outDir = new File(dir);
		if (outDir.exists() && outDir.isDirectory()) {

		} else {
			outDir.mkdirs();
		}
	}

	public static String GetFileName(String filePath) {
		return GetFileName(filePath, true);
	}

	public static String GetFileName(String filePath,boolean check) {
		File file = new File(filePath);
		if (!check || file.exists()) {
			String name = file.getName();
			int index = name.lastIndexOf(".");
			if (index > 0)
				return name.substring(0, index);
			return name;
		}
		return null;
	}

	/**
	 * 删除空目录
	 *
	 * @param dir
	 *            将要删除的目录路径
	 */
	public static void doDeleteEmptyDir(String dir) {
		boolean success = (new File(dir)).delete();
		if (success) {
			System.out.println("Successfully deleted empty directory: " + dir);
		} else {
			System.out.println("Failed to delete empty directory: " + dir);
		}
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 *
	 * @param dir
	 *            将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	public static synchronized boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				// if (!success) {
				// return false;
				// }
			}
		} else {
			dir.delete();
		}
		// 目录此时为空，可以删除
		// return dir.delete();
		return true;
	}

	public static synchronized boolean deleteDir(File dir,boolean delete) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteDir(new File(dir, children[i]),delete);
			}
		} else {
			dir.delete();
		}
		// 目录此时为空，可以删除
		if(delete) return dir.delete();
		return true;
	}

	public static void WriteStrng2File(String src, String desPath) {
		WriteStrng2File(false, src, desPath);
	}

	public static void WriteStrng2File(boolean bom, String src, String desPath) {
		WriteStrng2File(bom,src,desPath,false);
	}

	public static void WriteStrng2File(boolean bom, String src, String desPath,boolean isAppend) {
		if (src != null) {
			File file = new File(desPath);
			File parentFile = new File(file.getParent());
			if (!parentFile.exists())
				parentFile.mkdirs();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file,isAppend);//new FileOutputStream(file);
				if (bom) {
					byte[] uft8bom = { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };
					fos.write(uft8bom);
				}
				fos.write(src.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fos != null)
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

		}
	}

	public static void WriteStrng2File(String src, String desPath,String charset) {
		if (src != null) {
			File file = new File(desPath);
			File parentFile = new File(file.getParent());
			if (!parentFile.exists())
				parentFile.mkdirs();
			Writer writer = null;
			try {
				FileOutputStream fos = new FileOutputStream(file);
				writer = new OutputStreamWriter(fos);
				fos.write(src.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (writer != null)
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

		}
	}

	public static void WriteData2File(byte[] data, String desPath) {
		if (data != null) {
			File file = new File(desPath);
			File parentFile = new File(file.getParent());
			if (!parentFile.exists())
				parentFile.mkdirs();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				fos.write(data);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fos != null)
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}


	public static byte[] ReadDataFromFile(String desPath) throws IOException {
		FileChannel fc = null;
		try {
			fc = new RandomAccessFile(desPath, "r").getChannel();
			MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0,
					fc.size()).load();
			byte[] result = new byte[(int) fc.size()];
			if (byteBuffer.remaining() > 0) {
				// System.out.println("remain");
				byteBuffer.get(result, 0, byteBuffer.remaining());
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				fc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void WriteStrng2File(String src, String desPath, boolean cheakPath) {
		WriteStrng2File(src, desPath, cheakPath,false);
	}

	public static void WriteStrng2File(String src, String desPath, boolean cheakPath,boolean isAppend) {
		if (src != null) {
			File file = new File(desPath);
			if (cheakPath) {
				File parentFile = new File(desPath.substring(0, desPath.lastIndexOf("/")));
				if (!parentFile.exists())
					parentFile.mkdirs();
			}
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file, isAppend);//new FileOutputStream(file);
				fos.write(src.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fos != null)
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

		}
	}

	public static String GetFileSuffix(File file) {
		if (file != null) {
			String fileName = file.getName();
			int index = fileName.lastIndexOf(".");
			String suffix = index > 0 ? fileName.substring(index + 1) : fileName;
			return suffix;
		}
		return null;
	}

	public static void Copy(String oldPath, String newPath) {
		InputStream inStream = null;
		try {
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) {
				File des = new File(newPath);
				FileHelper.CheckDir(des.getParent());
				inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}

			}
		} catch (Exception e) {
			System.out.println("error  ");
			e.printStackTrace();
		} finally {
			try {
				inStream.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}

	public static synchronized void SaveObject(Serializable object, File out) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(out));
			oos.writeObject(object);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object GetObject(File in) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(in));
			Object object = ois.readObject();
			ois.close();
			return object;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void copyFile(File sourcefile, File targetFile) {
		try {
			// 新建文件输入流并对它进行缓冲
			FileInputStream input = new FileInputStream(sourcefile);
			BufferedInputStream inbuff = new BufferedInputStream(input);
			// 新建文件输出流并对它进行缓冲
			FileOutputStream out = new FileOutputStream(targetFile);
			BufferedOutputStream outbuff = new BufferedOutputStream(out);
			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len = 0;
			while ((len = inbuff.read(b)) != -1) {
				outbuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outbuff.flush();
			// 关闭流
			inbuff.close();
			outbuff.close();
			out.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void copyDirectiory(String sourceDir, String targetDir) {
		try {
			// 新建目标目录
			(new File(targetDir)).mkdirs();
			// 获取源文件夹当下的文件或目录
			File[] file = (new File(sourceDir)).listFiles();
			for (int i = 0; i < file.length; i++) {
				if (file[i].isFile()) {
					// 源文件
					File sourceFile = file[i];
					// 目标文件
					File targetFile = new File(
							new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());
					copyFile(sourceFile, targetFile);
				}
				if (file[i].isDirectory()) {
					// 准备复制的源文件夹
					String dir1 = sourceDir +"/"+ file[i].getName() + "/";
					// 准备复制的目标文件夹
					String dir2 = targetDir + "/" + file[i].getName();
					copyDirectiory(dir1, dir2);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readFile(InputStream is){
		try {
			if(is == null)return "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			StringBuffer sb = new StringBuffer();
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			is.close();
			return sb.toString();
		} catch (Exception e) {
		}
		return "";
	}


}
