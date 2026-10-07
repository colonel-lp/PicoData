package kotlinx.atomicfu;

import kotlin.Metadata;

/* JADX INFO: compiled from: AtomicFU.common.kt */
/* JADX INFO: loaded from: classes2.dex */
@Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\u0011\u0010\u000e\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0003H\u0087\u0002R\u0016\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\tR\u001a\u0010\u0002\u001a\u00020\u00038FX\u0087\u0004¢\u0006\f\u0012\u0004\b\n\u0010\u000b\u001a\u0004\b\f\u0010\r¨\u0006\u0010"}, d2 = {"Lkotlinx/atomicfu/AtomicIntArray;", "", "size", "", "<init>", "(I)V", "array", "", "Lkotlinx/atomicfu/AtomicInt;", "[Lkotlinx/atomicfu/AtomicInt;", "getSize$annotations", "()V", "getSize", "()I", "get", "index", "atomicfu"}, k = 1, mv = {2, 0, 0}, xi = 48)
public final class AtomicIntArray {
    private final AtomicInt[] array;

    public static /* synthetic */ void getSize$annotations() {
    }

    public AtomicIntArray(int i) {
        AtomicInt[] atomicIntArr = new AtomicInt[i];
        for (int i2 = 0; i2 < i; i2++) {
            atomicIntArr[i2] = AtomicFU.atomic(0);
        }
        this.array = atomicIntArr;
    }

    public final int getSize() {
        return this.array.length;
    }

    public final AtomicInt get(int index) {
        return this.array[index];
    }
}
