package com.bagri.tools.vvm.manager;

import org.openide.util.NbBundle;

public class Resources {
    private Resources() {
        throw new AssertionError();
    }

    /**
     * Returns the text of the resource for the specified
     * key formatted with the specified arguments.
     */
    public static String getText(String key, Object... args) {
        return NbBundle.getMessage(Resources.class, key, args);
    }

    /**
     * Returns the mnemonic keycode int of the resource for
     * the specified key.
     */
    public static int getMnemonicInt(String key) {
        String m = getText(key + ".mnemonic"); // NOI18N
        int mnemonic = m.charAt(0);
        if (mnemonic >= 'a' && mnemonic <= 'z') {
            mnemonic -= ('a' - 'A');
        }
        return mnemonic;
    }
}
