package com.winlator;

import android.content.Context;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;


public class ProxyConnectionWrapper extends InputConnectionWrapper {
    /**
     * Initializes a wrapper.
     *
     * <p><b>Caveat:</b> Although the system can accept {@code (InputConnection) null} in some
     * places, you cannot emulate such a behavior by non-null {@link InputConnectionWrapper} that
     * has {@code null} in {@code target}.</p>
     *
     * @param target  the {@link InputConnection} to be proxied.
     * @param mutable set {@code true} to protect this object from being reconfigured to target
     *                another {@link InputConnection}.  Note that this is ignored while the target is {@code null}.
     */
    private static String SOU_GOU_NAME_PREFIX = "com.sohu.inputmethod.sogou";
    private static String SOU_GOU_NAME_SUFFIX = ".SogouIME";
    private boolean generateDelEvent = false;
    private int lastComposingTextLength = 0;
    private boolean inComposingMode = false;

    public ProxyConnectionWrapper(Context context, InputConnection target, boolean mutable) {
        super(target, mutable);

        // 在构造函数中获取输入法 id 信息
        String inputId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (inputId.startsWith(SOU_GOU_NAME_PREFIX) || inputId.endsWith(SOU_GOU_NAME_SUFFIX)) {
            generateDelEvent = true;
        }
    }

    @Override
    public boolean sendKeyEvent(KeyEvent keyEvent) {
        boolean result = super.sendKeyEvent(keyEvent);
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL
                && keyEvent.getAction() == KeyEvent.ACTION_DOWN
                && keyBackIntercepted(1)) {
            return true;
        }
        return result;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        boolean result = super.deleteSurroundingText(beforeLength, afterLength);
        boolean intercepted = keyBackIntercepted(1); // 是否拦截的标志
        if (intercepted) {
            return true;
        }
        return result;
    }

    // 预测模式下选择文本后，再次点击删除按钮，会重新触发预测，走到该方法
    @Override
    public boolean setComposingRegion(int start, int end) {
        // 开始预测模式，触发检测
        checkWhenComposingStart(end);
        return super.setComposingRegion(start, end);
    }

    // 预测模式下设置文本
    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        // 开始预测模式，触发检测
        checkWhenComposingStart(text.length());
        return super.setComposingText(text, newCursorPosition);
    }

    // 预测模式结束会出发这个方法
    @Override
    public boolean finishComposingText() {
        checkWhenComposingEnd(lastComposingTextLength);
        return super.finishComposingText();
    }

    // 预测模式下，输入框中的文本一直删，删到为空，最后会调用这个方法
    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        checkWhenComposingEnd(text.length());
        return super.commitText(text, newCursorPosition);
    }

    private void checkWhenComposingStart(int len) {
        int nowLength = len;
        // 上次的文本大于此次的文本
        if (lastComposingTextLength > nowLength) {
            keyBackIntercepted(lastComposingTextLength - nowLength);
        }
        lastComposingTextLength = nowLength;
    }

    private void checkWhenComposingEnd(int len) {
        if (!generateDelEvent || !inComposingMode) {
            return;
        }
        inComposingMode = false;

        int nowLength = len;

        // 上次的文本大于此次的文本
        if (lastComposingTextLength > nowLength) {
            keyBackIntercepted(lastComposingTextLength - nowLength);
        }
        lastComposingTextLength = 0;
    }

    private boolean keyBackIntercepted(int len) {
        return super.deleteSurroundingText(len, 0);
    }
}
