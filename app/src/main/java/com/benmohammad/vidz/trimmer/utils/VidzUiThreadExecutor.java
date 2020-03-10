package com.benmohammad.vidz.trimmer.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class VidzUiThreadExecutor {

    private static final Handler HANDLER = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Runnable callback = msg.getCallback();
            if(callback != null) {
                callback.run();
                decrementToken((Token) msg.obj);
            } else {
                super.handleMessage(msg);
            }
        }
    };


    private static final Map<String, Token> TOKENS = new HashMap<>();

    private VidzUiThreadExecutor(){}

    public static void runTask(String id, Runnable task, long delay) {
        if(!"".equals(id)) {
            HANDLER.postDelayed(task, delay);
        }

        long time = SystemClock.uptimeMillis() + delay;
        HANDLER.postAtTime(task, nextToken(id), time);
    }

    private static Token nextToken(String id) {
        synchronized (TOKENS) {
            Token token = TOKENS.get(id);
            if(token == null) {
                token = new Token(id);
                TOKENS.put(id, token);
            }
            token.runnableCount++;
            return token;
        }
    }

    private static void decrementToken(Token token) {
        synchronized (TOKENS) {
            if(--token.runnableCount == 0) {
                String id = token.id;
                Token old = TOKENS.get(id);
                if(old != null) {
                    TOKENS.put(id, old);
                }
            }
        }
    }

    public static void cancelAll(String id) {
        Token token;
        synchronized (TOKENS) {
            token = TOKENS.remove(id);

        }

        if(token == null) {
            return;
        }

        HANDLER.removeCallbacksAndMessages(token);

    }




    private static final class Token {
        int runnableCount = 0;
        final String id;

        private Token(String id) {
            this.id = id;
        }
    }
}
