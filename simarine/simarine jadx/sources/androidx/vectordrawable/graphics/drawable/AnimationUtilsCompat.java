package androidx.vectordrawable.graphics.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.core.util.ObjectsCompat;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* JADX INFO: loaded from: classes.dex */
public class AnimationUtilsCompat {
    public static Interpolator loadInterpolator(Context context, int i) throws Resources.NotFoundException {
        Interpolator interpolatorLoadInterpolator = AnimationUtils.loadInterpolator(context, i);
        ObjectsCompat.requireNonNull(interpolatorLoadInterpolator, "Failed to parse interpolator, no start tag found");
        return interpolatorLoadInterpolator;
    }

    private static Interpolator createInterpolatorFromXml(Context context, XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        Interpolator pathInterpolatorCompat;
        int depth = xmlPullParser.getDepth();
        Interpolator linearInterpolator = null;
        while (true) {
            int next = xmlPullParser.next();
            if ((next != 3 || xmlPullParser.getDepth() > depth) && next != 1) {
                if (next == 2) {
                    AttributeSet attributeSetAsAttributeSet = Xml.asAttributeSet(xmlPullParser);
                    String name = xmlPullParser.getName();
                    name.hashCode();
                    switch (name) {
                        case "pathInterpolator":
                            pathInterpolatorCompat = new PathInterpolatorCompat(context, attributeSetAsAttributeSet, xmlPullParser);
                            break;
                        case "anticipateInterpolator":
                            pathInterpolatorCompat = new AnticipateInterpolator(context, attributeSetAsAttributeSet);
                            break;
                        case "linearInterpolator":
                            linearInterpolator = new LinearInterpolator();
                            continue;
                            break;
                        case "accelerateInterpolator":
                            pathInterpolatorCompat = new AccelerateInterpolator(context, attributeSetAsAttributeSet);
                            break;
                        case "bounceInterpolator":
                            linearInterpolator = new BounceInterpolator();
                            continue;
                            break;
                        case "overshootInterpolator":
                            pathInterpolatorCompat = new OvershootInterpolator(context, attributeSetAsAttributeSet);
                            break;
                        case "anticipateOvershootInterpolator":
                            pathInterpolatorCompat = new AnticipateOvershootInterpolator(context, attributeSetAsAttributeSet);
                            break;
                        case "decelerateInterpolator":
                            pathInterpolatorCompat = new DecelerateInterpolator(context, attributeSetAsAttributeSet);
                            break;
                        case "accelerateDecelerateInterpolator":
                            linearInterpolator = new AccelerateDecelerateInterpolator();
                            continue;
                            break;
                        case "cycleInterpolator":
                            pathInterpolatorCompat = new CycleInterpolator(context, attributeSetAsAttributeSet);
                            break;
                        default:
                            throw new RuntimeException("Unknown interpolator name: " + xmlPullParser.getName());
                    }
                    linearInterpolator = pathInterpolatorCompat;
                }
            }
        }
        if (linearInterpolator != null) {
            return linearInterpolator;
        }
        throw new RuntimeException("Failed to parse interpolator, no start tag found");
    }

    private AnimationUtilsCompat() {
    }
}
