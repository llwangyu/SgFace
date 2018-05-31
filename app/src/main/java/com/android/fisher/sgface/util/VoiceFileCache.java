package com.android.fisher.sgface.util;


import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

public class VoiceFileCache {
    private static final String WAVCACHDIR = "WavCach";
    private static final String WHOLESALE_CONV = ".cach";

    private static final int MB = 1024 * 1024;
    private static final int CACHE_SIZE = 10;
    private static final int FREE_SD_SPACE_NEEDED_TO_CACHE = 10;

    public VoiceFileCache() {
        // 清理文件缓存
//		removeCache(getDirectory());
    }

    /** 从缓存中获取文件 **/
    public File getCacheFile(final String url) {
        final String path = getDirectory() + "/" + convertUrlToFileName(url);
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    /** 将图片存入文件缓存 **/
    public void saveWavFile(InputStream ins, String url) {
        if (ins == null) {
            return;
        }
        // 判断sdcard上的空间
        if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            // SD空间不足
            return;
        }
        String filename = convertUrlToFileName(url);
        String dir = getDirectory();
        File dirFile = new File(dir);
        if (!dirFile.exists())
            dirFile.mkdirs();
        File file = new File(dir + "/" + filename);
        try {
            file.createNewFile();
            OutputStream outStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length=0;
            while ((length = ins.read(buffer) )!= -1) {
                outStream.write(buffer, 0, length);
            }
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            ToolUtil.print("ImageFileCache", "FileNotFoundException");
        } catch (IOException e) {
            ToolUtil.print("ImageFileCache", "IOException");
        }
    }

    /**
     * 计算存储目录下的文件大小，
     * 当文件总大小大于规定的CACHE_SIZE或者sdcard剩余空间小于FREE_SD_SPACE_NEEDED_TO_CACHE的规定
     * 那么删除40%最近没有被使用的文件
     */
    private boolean removeCache(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return true;
        }
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return false;
        }

        int dirSize = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(WHOLESALE_CONV)) {
                dirSize += files[i].length();
            }
        }

        if (dirSize > CACHE_SIZE * MB
                || FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            int removeFactor = (int) ((0.4 * files.length) + 1);
            Arrays.sort(files, new FileLastModifSort());
            for (int i = 0; i < removeFactor; i++) {
                if (files[i].getName().contains(WHOLESALE_CONV)) {
                    files[i].delete();
                }
            }
        }

        if (freeSpaceOnSd() <= CACHE_SIZE) {
            return false;
        }

        return true;
    }

    /** 修改文件的最后修改时间 **/
    public void updateFileTime(String path) {
        File file = new File(path);
        long newModifiedTime = System.currentTimeMillis();
        file.setLastModified(newModifiedTime);
    }

    /** 计算sdcard上的剩余空间 **/
    private int freeSpaceOnSd() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        @SuppressWarnings("deprecation")
        double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
                .getBlockSize()) / MB;
        return (int) sdFreeMB;
    }

    /** 将url转成文件名 **/
    private String convertUrlToFileName(String url) {
        String[] strs = url.split("/");
        String name = strs[strs.length - 1];
        name = name.replaceAll("\\?", "-");
        return name;
    }

    /** 获得缓存目录 **/
    private String getDirectory() {
        String dir = FileUtil.getCacheFilePath() + "/" + WAVCACHDIR;
        return dir;
    }

    /** 取SD卡路径 **/
    // private String getSDPath() {
    // File sdDir = null;
    // boolean sdCardExist = Environment.getExternalStorageState().equals(
    // android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
    // if (sdCardExist) {
    // sdDir = Environment.getExternalStorageDirectory(); // 获取根目录
    // }
    // if (sdDir != null) {
    // return sdDir.toString();
    // } else {
    // return "";
    // }
    // }

    /**
     * 根据文件的最后修改时间进行排序
     */
    private class FileLastModifSort implements Comparator<File> {
        public int compare(File arg0, File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
