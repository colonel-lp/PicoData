package crc6452ffdc5b34af3a0f;

import android.content.Context;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.View;
import androidx.appcompat.widget.AppCompatEditText;
import java.util.ArrayList;
import mono.android.IGCUserPeer;
import mono.android.Runtime;
import mono.android.TypeManager;

/* JADX INFO: loaded from: classes2.dex */
public class MauiTimePicker extends AppCompatEditText implements IGCUserPeer, View.OnClickListener {
    public static final String __md_methods = "n_getDefaultMovementMethod:()Landroid/text/method/MovementMethod;:GetGetDefaultMovementMethodHandler\nn_onClick:(Landroid/view/View;)V:GetOnClick_Landroid_view_View_Handler:Android.Views.View/IOnClickListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n";
    private ArrayList refList;

    private native MovementMethod n_getDefaultMovementMethod();

    private native void n_onClick(View view);

    static {
        Runtime.register("Microsoft.Maui.Platform.MauiTimePicker, Microsoft.Maui", MauiTimePicker.class, "n_getDefaultMovementMethod:()Landroid/text/method/MovementMethod;:GetGetDefaultMovementMethodHandler\nn_onClick:(Landroid/view/View;)V:GetOnClick_Landroid_view_View_Handler:Android.Views.View/IOnClickListenerInvoker, Mono.Android, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\n");
    }

    public MauiTimePicker(Context context) {
        super(context);
        if (getClass() == MauiTimePicker.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiTimePicker, Microsoft.Maui", "Android.Content.Context, Mono.Android", this, new Object[]{context});
        }
    }

    public MauiTimePicker(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (getClass() == MauiTimePicker.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiTimePicker, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android", this, new Object[]{context, attributeSet});
        }
    }

    public MauiTimePicker(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (getClass() == MauiTimePicker.class) {
            TypeManager.Activate("Microsoft.Maui.Platform.MauiTimePicker, Microsoft.Maui", "Android.Content.Context, Mono.Android:Android.Util.IAttributeSet, Mono.Android:System.Int32, System.Private.CoreLib", this, new Object[]{context, attributeSet, Integer.valueOf(i)});
        }
    }

    @Override // android.widget.EditText, android.widget.TextView
    public MovementMethod getDefaultMovementMethod() {
        return n_getDefaultMovementMethod();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        n_onClick(view);
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
