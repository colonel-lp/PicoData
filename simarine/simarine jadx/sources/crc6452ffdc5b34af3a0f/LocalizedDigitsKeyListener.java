package crc6452ffdc5b34af3a0f;

import android.text.Spanned;
import android.text.method.NumberKeyListener;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class LocalizedDigitsKeyListener extends NumberKeyListener implements IGCUserPeer {
    public static final String __md_methods = "n_getInputType:()I:GetGetInputTypeHandler\nn_getAcceptedChars:()[C:GetGetAcceptedCharsHandler\nn_filter:(Ljava/lang/CharSequence;IILandroid/text/Spanned;II)Ljava/lang/CharSequence;:GetFilter_Ljava_lang_CharSequence_IILandroid_text_Spanned_IIHandler\n";
    private ArrayList refList;

    private native CharSequence n_filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4);

    private native char[] n_getAcceptedChars();

    private native int n_getInputType();

    static {
        Runtime.register("Microsoft.Maui.Platform.LocalizedDigitsKeyListener, Microsoft.Maui", LocalizedDigitsKeyListener.class, __md_methods);
    }

    public LocalizedDigitsKeyListener() {
        if (getClass() == LocalizedDigitsKeyListener.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.LocalizedDigitsKeyListener, Microsoft.Maui", "", this, new Object[0]);
        }
    }

    public LocalizedDigitsKeyListener(int i, char c) {
        if (getClass() == LocalizedDigitsKeyListener.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.LocalizedDigitsKeyListener, Microsoft.Maui", "Android.Text.InputTypes, Mono.Android:System.Char, System.Private.CoreLib", this, new Object[]{Integer.valueOf(i), Character.valueOf(c)});
        }
    }

    @Override // android.text.method.KeyListener
    public int getInputType() {
        return n_getInputType();
    }

    @Override // android.text.method.NumberKeyListener
    public char[] getAcceptedChars() {
        return n_getAcceptedChars();
    }

    @Override // android.text.method.NumberKeyListener, android.text.InputFilter
    public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
        return n_filter(charSequence, i, i2, spanned, i3, i4);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidAddReference(Object obj) {
        if (this.refList == null) {
            this.refList = new ArrayList();
        }
        this.refList.add(obj);
    }

    @Override // mono.android.IGCUserPeer
    public void monodroidClearReferences() {
        ArrayList arrayList = this.refList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }
}
