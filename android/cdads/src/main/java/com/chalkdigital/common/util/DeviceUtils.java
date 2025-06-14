package com.chalkdigital.common.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.chalkdigital.common.CreativeOrientation;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.network.response.TypeParser;

import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketException;
import java.util.Date;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static com.chalkdigital.common.util.Reflection.MethodBuilder;

public class DeviceUtils {
    private static final int MAX_MEMORY_CACHE_SIZE = 30 * 1024 * 1024; // 30 MB
    private static final int MIN_DISK_CACHE_SIZE = 30 * 1024 * 1024; // 30 MB
    private static final int MAX_DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100 MB

    private DeviceUtils() {}

    public enum ForceOrientation {
        FORCE_PORTRAIT("portrait"),
        FORCE_LANDSCAPE("landscape"),
        DEVICE_ORIENTATION("device"),
        UNDEFINED("");

        @NonNull private final String mKey;

        ForceOrientation(@NonNull final String key) {
            mKey = key;
        }

        @NonNull
        public static ForceOrientation getForceOrientation(@Nullable String key) {
            for (final ForceOrientation orientation : ForceOrientation.values()) {
                if (orientation.mKey.equalsIgnoreCase(key)) {
                    return orientation;
                }
            }

            return UNDEFINED;
        }
    }

    public static boolean isNetworkAvailable(@Nullable final Context context) {
        if (context == null) {
            return false;
        }

        if (!DeviceUtils.isPermissionGranted(context, INTERNET)) {
            return false;
        }

        /**
         * This is only checking if we have permission to access the network state
         * It's possible to not have permission to check network state but still be able
         * to access the network itself.
         */
        if (!DeviceUtils.isPermissionGranted(context, ACCESS_NETWORK_STATE)) {
            return true;
        }

        // Otherwise, perform the connectivity check.
        try {
            final ConnectivityManager connnectionManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connnectionManager.getActiveNetworkInfo();
            return networkInfo.isConnected();
        } catch (NullPointerException e) {
//                        Utils.logStackTrace(e);
            return false;
        }
    }

    public static int memoryCacheSizeBytes(final Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        long memoryClass = activityManager.getMemoryClass();

        try {
            final int flagLargeHeap = ApplicationInfo.class.getDeclaredField("FLAG_LARGE_HEAP").getInt(null);
            if (Utils.bitMaskContainsFlag(context.getApplicationInfo().flags, flagLargeHeap)) {
                memoryClass = (Integer) new MethodBuilder(activityManager, "getLargeMemoryClass").execute();
            }
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Unable to reflectively determine large heap size.");
        }

        long result = Math.min(MAX_MEMORY_CACHE_SIZE, memoryClass / 8 * 1024 * 1024);
        return (int) result;
    }

    public static long diskCacheSizeBytes(File dir, long minSize) {
        long size = minSize;
        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long availableBytes = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
            size = availableBytes / 50;
        } catch (IllegalArgumentException e) {
                        Utils.logStackTrace(e);
            CDAdLog.d("Unable to calculate 2% of available disk space, defaulting to minimum");
        }

        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
    }

    public static long diskCacheSizeBytes(File dir) {
        return diskCacheSizeBytes(dir, MIN_DISK_CACHE_SIZE);
    }

    public static int getScreenOrientation(@NonNull final Context context) {
//        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//        final int deviceOrientation = activity.getResources().getConfiguration().orientation;
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final int currentRotation = display.getRotation();
        final int deviceOrientation = context.getResources().getConfiguration().orientation;

        return getScreenOrientationFromRotationAndOrientation(currentRotation, deviceOrientation);
    }

    static int getScreenOrientationFromRotationAndOrientation(int rotation, int orientation) {
        if (Configuration.ORIENTATION_PORTRAIT == orientation) {
            switch (rotation) {
                case Surface.ROTATION_90:
                case Surface.ROTATION_180:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

                case Surface.ROTATION_0:
                case Surface.ROTATION_270:
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } else if (Configuration.ORIENTATION_LANDSCAPE == orientation) {
            switch (rotation) {
                case Surface.ROTATION_180:
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

                case Surface.ROTATION_0:
                case Surface.ROTATION_90:
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else {
            CDAdLog.d("Unknown screen orientation. Defaulting to portrait.");
            return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        }
    }

    /**
     * Lock this activity in the requested orientation, rotating the display if necessary.
     *
     * @param creativeOrientation the orientation of the screen needed by the ad creative.
     */
    public static void lockOrientation(@NonNull Activity activity, @NonNull CreativeOrientation creativeOrientation) {
        if (!Preconditions.NoThrow.checkNotNull(creativeOrientation) || !Preconditions.NoThrow.checkNotNull(activity)) {
            return;
        }

        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final int currentRotation = display.getRotation();
        final int deviceOrientation = activity.getResources().getConfiguration().orientation;

        final int currentOrientation = getScreenOrientationFromRotationAndOrientation(currentRotation, deviceOrientation);
        int requestedOrientation;

        // Choose a requested orientation that will result in the smallest change from the existing orientation.
        if (CreativeOrientation.PORTRAIT == creativeOrientation) {
            if (ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT == currentOrientation) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } else if (CreativeOrientation.LANDSCAPE == creativeOrientation) {
            if (ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE == currentOrientation) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else if (CreativeOrientation.DEVICE == creativeOrientation){
            // Don't lock screen orientation if the creative doesn't care.
//            activity.setRequestedOrientation(currentOrientation);
            return;
        }else if (CreativeOrientation.VIEW == creativeOrientation){
            // Don't lock screen orientation if the creative doesn't care.
//            activity.setRequestedOrientation(currentRotation);
            return;
        }else{
            return;
        }

//        activity.setRequestedOrientation(requestedOrientation);
    }

    /**
     * Lock this activity in the requested orientation, rotating the display if necessary.
     *
     * @param creativeOrientation the orientation of the screen needed by the ad creative.
     */
    public static int requestedOrientation(@NonNull Context context, @NonNull CreativeOrientation creativeOrientation) {
        if (!Preconditions.NoThrow.checkNotNull(creativeOrientation) || !Preconditions.NoThrow.checkNotNull(context)) {
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final int currentRotation = display.getRotation();
        final int deviceOrientation = context.getResources().getConfiguration().orientation;

        final int currentOrientation = getScreenOrientationFromRotationAndOrientation(currentRotation, deviceOrientation);
        int requestedOrientation;

        // Choose a requested orientation that will result in the smallest change from the existing orientation.
        if (CreativeOrientation.PORTRAIT == creativeOrientation) {
            if (ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT == currentOrientation) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } else if (CreativeOrientation.LANDSCAPE == creativeOrientation) {
            if (ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE == currentOrientation) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else if (CreativeOrientation.DEVICE == creativeOrientation){
            // Don't lock screen orientation if the creative doesn't care.
            return currentOrientation;
        }else{
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }

        return requestedOrientation;
    }

    /**
     * Lock this activity in the requested orientation, rotating the display if necessary.
     *
     * @param creativeOrientation the orientation of the screen needed by the ad creative.
     */
    public static void lockOrientation(@NonNull Activity activity, String creativeOrientation) {
        if (!Preconditions.NoThrow.checkNotNull(creativeOrientation) || !Preconditions.NoThrow.checkNotNull(activity)) {
            return;
        }

        int orientation = TypeParser.parseInteger(creativeOrientation, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (orientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            return;

        activity.setRequestedOrientation(orientation);
    }

    /**
     * This tries to get the physical number of pixels on the device. This attempts to include
     * the pixels in the notification bar and soft buttons.
     *
     * @param context Needs a context (application is fine) to determine width/height.
     * @return Width and height of the device
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Point getDeviceDimensions(@NonNull final Context context) {
        Integer bestWidthPixels = null;
        Integer bestHeightPixels = null;

        final WindowManager windowManager = (WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final Point screenSize = new Point();
            display.getRealSize(screenSize);
            bestWidthPixels = screenSize.x;
            bestHeightPixels = screenSize.y;
        } else {
            try {
                bestWidthPixels = (Integer) new MethodBuilder(display,
                        "getRawWidth").execute();
                bestHeightPixels = (Integer) new MethodBuilder(display,
                        "getRawHeight").execute();
            } catch (Exception e) {
                        Utils.logStackTrace(e);
                // Best effort. If this fails, just get the height and width normally,
                // which may not capture the pixels used in the notification bar.
                CDAdLog.v("Display#getRawWidth/Height failed.", e);
            }
        }

        if (bestWidthPixels == null || bestHeightPixels == null) {
            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            bestWidthPixels = displayMetrics.widthPixels;
            bestHeightPixels = displayMetrics.heightPixels;
        }

        return new Point(bestWidthPixels, bestHeightPixels);
    }

    public static boolean isPermissionGranted(@NonNull final Context context,
            @NonNull final String permission) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(permission);

        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @deprecated As of release 4.4.0
     */
    @Deprecated
    public enum IP { IPv4, IPv6 }

    /**
     * @deprecated As of release 4.4.0
     */
    @Deprecated
    @Nullable
    public static String getIpAddress(IP ip) throws SocketException {
        return null;
    }

    /**
     * @deprecated As of release 4.4.0
     */
    @Deprecated
    @Nullable
    public static String getHashedUdid(final Context context) {
        return null;
    }


    public static void takeScreenshot(Activity activity) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = activity.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(activity, imageFile);
        } catch (Throwable e) {
                        Utils.logStackTrace(e);
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private static void openScreenshot(Context context, File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        context.startActivity(intent);
    }

    private static String getSystemProperty(String name) throws Exception {
        Class systemPropertyClazz = Class.forName("android.os.SystemProperties");
        return (String) systemPropertyClazz.getMethod("get", new Class[]{String.class}).invoke(systemPropertyClazz, new Object[]{name});
    }

    public static boolean isEmulator() {
        try {
            boolean goldfish = getSystemProperty("ro.hardware").contains("goldfish");
            boolean emu = getSystemProperty("ro.kernel.qemu").length() > 0;
            boolean sdk = getSystemProperty("ro.product.model").equals("sdk");
            return goldfish || emu || sdk;
        } catch (Exception e) {
                        Utils.logStackTrace(e);
            e.printStackTrace();
            return false;
        }
    }

}
