package com.winlator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.AppCompatEditText;

public class ProxyEditView extends AppCompatEditText {

    private InputMethodManager mInputMethodManager;
    private Activity mActivity;

    public ProxyEditView(Context context) {
        super(context);
        init(null);
    }

    public ProxyEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ProxyEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mActivity = (Activity)getContext();

        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);

        // 初始化输入法管理器
        mInputMethodManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.subSequence(start, start + count).toString();
                KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(), text,-1, 0);
                handleKeyEvent(event);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            innerShowInputMethod();
        } else {
            hideInputMethod();
        }
    }

    public void showInputMethod() {
        if (!isFocused()) {
            requestFocus();
        } else {
            innerShowInputMethod();
        }
    }

    private void innerShowInputMethod() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            postDelayed(() -> mInputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT), 500L);
        }
        else {
            mInputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideInputMethod() {
        if (mInputMethodManager != null && mInputMethodManager.isActive(this)) {
            mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    private boolean handleKeyEvent(KeyEvent event) {
        return mActivity.dispatchKeyEvent(event);
    }

    private boolean performEditorAction(int actionCode) {
        if (actionCode == EditorInfo.IME_ACTION_NEXT) {
            View v = focusSearch(FOCUS_DOWN);
            if (v != null) {
                if (!v.requestFocus(FOCUS_DOWN)) {
                    throw new IllegalStateException("focus search returned a view "
                            + "that wasn't able to take focus!");
                }
            }
            return true;

        } else if (actionCode == EditorInfo.IME_ACTION_PREVIOUS) {
            View v = focusSearch(FOCUS_UP);
            if (v != null) {
                if (!v.requestFocus(FOCUS_UP)) {
                    throw new IllegalStateException("focus search returned a view "
                            + "that wasn't able to take focus!");
                }
            }
            return true;

        } else if (actionCode == EditorInfo.IME_ACTION_DONE) {
            hideInputMethod();
            return true;
        }

        long eventTime = SystemClock.uptimeMillis();
        handleKeyEvent(
                new KeyEvent(eventTime, eventTime,
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0,
                        KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE
                                | KeyEvent.FLAG_EDITOR_ACTION));
        handleKeyEvent(
                new KeyEvent(SystemClock.uptimeMillis(), eventTime,
                        KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0,
                        KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE
                                | KeyEvent.FLAG_EDITOR_ACTION));
        return true;
    }
}
