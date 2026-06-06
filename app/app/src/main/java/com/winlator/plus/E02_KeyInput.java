package com.winlator.plus;

import android.view.InputDevice;
import android.view.KeyEvent;

import com.winlator.core.AppUtils;
import com.winlator.xserver.Keyboard;
import com.winlator.xserver.XKeycode;
import com.winlator.xserver.XServer;

class E02_KeyInput {
    private static final String TAG = "E2_KeyInput";
    /** 用于输入unicode文字时，临时充数的keycode */
    public static final XKeycode[] stubKeyCode = {XKeycode.KEY_A, XKeycode.KEY_B, XKeycode.KEY_C, XKeycode.KEY_D, XKeycode.KEY_E, XKeycode.KEY_F, XKeycode.KEY_G, XKeycode.KEY_H, XKeycode.KEY_I, XKeycode.KEY_J, XKeycode.KEY_K, XKeycode.KEY_L, XKeycode.KEY_M, XKeycode.KEY_N, XKeycode.KEY_O, XKeycode.KEY_P, XKeycode.KEY_Q, XKeycode.KEY_R, XKeycode.KEY_S, XKeycode.KEY_T, XKeycode.KEY_U, XKeycode.KEY_V, XKeycode.KEY_W, XKeycode.KEY_X, XKeycode.KEY_Y, XKeycode.KEY_Z,};
    /** 用于输入unicode文字时，记录本次该用哪个充数的keycode，然后++ */
    public static int  currIndex = 0;
    private static int lastDownKeyCode = -1;
    /**
     * 此函数处理KeyEvent的情况
     */
    public static boolean handleAndroidKeyEvent(XServer xServer, KeyEvent event){

        boolean handled = false;
        int action = event.getAction();
        int keycode = event.getKeyCode();
        if (action == KeyEvent.ACTION_UP) {
            if (keycode == KeyEvent.KEYCODE_SPACE && lastDownKeyCode != keycode) {
                InputDevice device = event.getDevice();
                if (device != null && device.isVirtual()) {
                    xServer.injectKeyPress(XKeycode.KEY_SPACE, event.getUnicodeChar());
                }
            }
        } else if(action == KeyEvent.ACTION_MULTIPLE) {
            String characters = event.getCharacters();
            int codePointCount = characters.codePointCount(0, characters.length());
            xServer.keyboard.resetCustomKeysyms(codePointCount);
            for (int i = 0; i < codePointCount; i++) {
                int keySym = characters.codePointAt(characters.offsetByCodePoints(0, i));
                //大于0xff的，直接加上0x100,0000
                if(keySym>0xff) keySym = keySym | 0x1000000;

                XKeycode xKeycode = xServer.keyboard.getCustomXKeycodeForKeysym(keySym);
                xServer.injectKeyPress(xKeycode, keySym);
                xServer.injectKeyRelease(xKeycode);
                //currIndex = (currIndex+1)% stubKeyCode.length;//数组下标+1，为下一次设置另一个keycode做准备
                handled = true;
            }
        }
        lastDownKeyCode = action == KeyEvent.ACTION_DOWN ? keycode : -1;
        return handled;
    }
}
