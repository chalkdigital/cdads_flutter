package com.chalkdigital.nativeads.factories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chalkdigital.common.Preconditions;
import com.chalkdigital.nativeads.CDAdCustomEventNative;
import com.chalkdigital.nativeads.CustomEventNative;

import java.lang.reflect.Constructor;

public class CustomEventNativeFactory {
    protected static CustomEventNativeFactory instance = new CustomEventNativeFactory();

    public static CustomEventNative create(@Nullable final String className) throws Exception {
        if (className != null) {
            final Class<? extends CustomEventNative> nativeClass = Class.forName(className)
                    .asSubclass(CustomEventNative.class);
            return instance.internalCreate(nativeClass);
        } else {
            return new CDAdCustomEventNative();
        }
    }

    @Deprecated // for testing
    public static void setInstance(
            @NonNull final CustomEventNativeFactory customEventNativeFactory) {
        Preconditions.checkNotNull(customEventNativeFactory);

        instance = customEventNativeFactory;
    }

    @NonNull
    protected CustomEventNative internalCreate(
            @NonNull final Class<? extends CustomEventNative> nativeClass) throws Exception {
        Preconditions.checkNotNull(nativeClass);

        final Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
        nativeConstructor.setAccessible(true);
        return (CustomEventNative) nativeConstructor.newInstance();
    }
}
