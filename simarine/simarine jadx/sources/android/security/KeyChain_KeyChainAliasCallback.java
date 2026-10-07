package android.security;

import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes.dex */
public class KeyChain_KeyChainAliasCallback implements IGCUserPeer, KeyChainAliasCallback {
    public static final String __md_methods = "n_alias:(Ljava/lang/String;)V:GetAlias_Ljava_lang_String_Handler:Android.Security.IKeyChainAliasCallbackInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native void n_alias(String str);

    static {
        Runtime.register("Android.Security.KeyChain+KeyChainAliasCallback, Mono.Android", KeyChain_KeyChainAliasCallback.class, __md_methods);
    }

    public KeyChain_KeyChainAliasCallback() {
        if (getClass() == KeyChain_KeyChainAliasCallback.class) {
            TypeManager.Activate("Android.Security.KeyChain+KeyChainAliasCallback, Mono.Android", "", this, new Object[0]);
        }
    }

    @Override // android.security.KeyChainAliasCallback
    public void alias(String str) {
        n_alias(str);
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
