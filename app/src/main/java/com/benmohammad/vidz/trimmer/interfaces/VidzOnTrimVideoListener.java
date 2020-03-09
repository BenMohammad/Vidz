package com.benmohammad.vidz.trimmer.interfaces;

import android.net.Uri;

public interface VidzOnTrimVideoListener {

    void onTrimStarted(int startPosition, int endPosition);

    void getResult(final Uri uri);

    void cancelAction();

    void onError(final String message);
}
