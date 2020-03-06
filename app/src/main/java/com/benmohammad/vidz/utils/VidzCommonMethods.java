package com.benmohammad.vidz.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

public class VidzCommonMethods {

    private static String tagName = VidzCommonMethods.class.getSimpleName();
    public static File writeIntoFile(Context context, Intent data, File file) {
        AssetFileDescriptor videoAsset = null;
        try {
            videoAsset = context.getContentResolver().openAssetFileDescriptor(data.getData(), "r");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FileInputStream in;
        try {
            in = videoAsset.createInputStream();
            OutputStream out = null;
            out = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int len;

            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
        } finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    public static long convertDurationInSeconds(long duration) {
        return (TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }




    public static long convertDurationInMinutes(long duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        Log.v(tagName, "MIN:" + minutes);

        if(minutes > 0) {
            return minutes;
        } else {
            return 0;
        }

    }

    public static String convertDuration(long duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        Log.v(tagName, "MIN: " + minutes);

        if(minutes > 0) {
            return minutes + "";
        } else {
            return "00:" + (TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        }
    }

    public static int getMediaDuration(Context context, Uri uriOfFile) {
        MediaPlayer mp = MediaPlayer.create(context, uriOfFile);
        return mp.getDuration();
    }

    public static String getFileExtension(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf("."));
        Log.v(tagName, "extension: " + extension);
        return extension;
    }



}
