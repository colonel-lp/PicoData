package com.google.crypto.tink.shaded.protobuf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import sun.misc.Unsafe;

/* JADX INFO: loaded from: classes2.dex */
@CheckReturnValue
final class MessageSchema<T> implements Schema<T> {
    private static final int CHECK_INITIALIZED_BIT = 1024;
    private static final int ENFORCE_UTF8_MASK = 536870912;
    private static final int FIELD_TYPE_MASK = 267386880;
    private static final int HAS_HAS_BIT = 4096;
    private static final int INTS_PER_FIELD = 3;
    private static final int LEGACY_ENUM_IS_CLOSED_BIT = 2048;
    private static final int LEGACY_ENUM_IS_CLOSED_MASK = Integer.MIN_VALUE;
    private static final int NO_PRESENCE_SENTINEL = 1048575;
    private static final int OFFSET_BITS = 20;
    private static final int OFFSET_MASK = 1048575;
    static final int ONEOF_TYPE_OFFSET = 51;
    private static final int REQUIRED_BIT = 256;
    private static final int REQUIRED_MASK = 268435456;
    private static final int UTF8_CHECK_BIT = 512;
    private final int[] buffer;
    private final int checkInitializedCount;
    private final MessageLite defaultInstance;
    private final ExtensionSchema<?> extensionSchema;
    private final boolean hasExtensions;
    private final int[] intArray;
    private final ListFieldSchema listFieldSchema;
    private final boolean lite;
    private final MapFieldSchema mapFieldSchema;
    private final int maxFieldNumber;
    private final int minFieldNumber;
    private final NewInstanceSchema newInstanceSchema;
    private final Object[] objects;
    private final int repeatedFieldOffsetStart;
    private final ProtoSyntax syntax;
    private final UnknownFieldSchema<?, ?> unknownFieldSchema;
    private final boolean useCachedSizeField;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    private static boolean isEnforceUtf8(int value) {
        return (value & ENFORCE_UTF8_MASK) != 0;
    }

    private static boolean isLegacyEnumIsClosed(int value) {
        return (value & Integer.MIN_VALUE) != 0;
    }

    private static boolean isRequired(int value) {
        return (value & REQUIRED_MASK) != 0;
    }

    private static long offset(int value) {
        return value & 1048575;
    }

    private static int type(int value) {
        return (value & FIELD_TYPE_MASK) >>> 20;
    }

    private MessageSchema(int[] buffer, Object[] objects, int minFieldNumber, int maxFieldNumber, MessageLite defaultInstance, ProtoSyntax syntax, boolean useCachedSizeField, int[] intArray, int checkInitialized, int mapFieldPositions, NewInstanceSchema newInstanceSchema, ListFieldSchema listFieldSchema, UnknownFieldSchema<?, ?> unknownFieldSchema, ExtensionSchema<?> extensionSchema, MapFieldSchema mapFieldSchema) {
        this.buffer = buffer;
        this.objects = objects;
        this.minFieldNumber = minFieldNumber;
        this.maxFieldNumber = maxFieldNumber;
        this.lite = defaultInstance instanceof GeneratedMessageLite;
        this.syntax = syntax;
        this.hasExtensions = extensionSchema != null && extensionSchema.hasExtensions(defaultInstance);
        this.useCachedSizeField = useCachedSizeField;
        this.intArray = intArray;
        this.checkInitializedCount = checkInitialized;
        this.repeatedFieldOffsetStart = mapFieldPositions;
        this.newInstanceSchema = newInstanceSchema;
        this.listFieldSchema = listFieldSchema;
        this.unknownFieldSchema = unknownFieldSchema;
        this.extensionSchema = extensionSchema;
        this.defaultInstance = defaultInstance;
        this.mapFieldSchema = mapFieldSchema;
    }

    static <T> MessageSchema<T> newSchema(Class<T> messageClass, MessageInfo messageInfo, NewInstanceSchema newInstanceSchema, ListFieldSchema listFieldSchema, UnknownFieldSchema<?, ?> unknownFieldSchema, ExtensionSchema<?> extensionSchema, MapFieldSchema mapFieldSchema) {
        if (messageInfo instanceof RawMessageInfo) {
            return newSchemaForRawMessageInfo((RawMessageInfo) messageInfo, newInstanceSchema, listFieldSchema, unknownFieldSchema, extensionSchema, mapFieldSchema);
        }
        return newSchemaForMessageInfo((StructuralMessageInfo) messageInfo, newInstanceSchema, listFieldSchema, unknownFieldSchema, extensionSchema, mapFieldSchema);
    }

    /* JADX WARN: Code duplicated, block: B:121:0x0248  */
    /* JADX WARN: Code duplicated, block: B:122:0x024b  */
    /* JADX WARN: Code duplicated, block: B:125:0x0262  */
    /* JADX WARN: Code duplicated, block: B:126:0x0265  */
    static <T> MessageSchema<T> newSchemaForRawMessageInfo(RawMessageInfo messageInfo, NewInstanceSchema newInstanceSchema, ListFieldSchema listFieldSchema, UnknownFieldSchema<?, ?> unknownFieldSchema, ExtensionSchema<?> extensionSchema, MapFieldSchema mapFieldSchema) {
        int i;
        int iCharAt;
        int iCharAt2;
        int i2;
        int i3;
        int[] iArr;
        int i4;
        int i5;
        int i6;
        int i7;
        char cCharAt;
        int i8;
        char cCharAt2;
        int i9;
        char cCharAt3;
        int i10;
        char cCharAt4;
        int i11;
        char cCharAt5;
        int i12;
        char cCharAt6;
        int i13;
        char cCharAt7;
        int i14;
        char cCharAt8;
        int i15;
        int i16;
        int i17;
        int i18;
        int iObjectFieldOffset;
        int i19;
        int i20;
        int iObjectFieldOffset2;
        java.lang.reflect.Field fieldReflectField;
        int i21;
        char cCharAt9;
        int i22;
        int i23;
        int i24;
        Object obj;
        java.lang.reflect.Field fieldReflectField2;
        int i25;
        Object obj2;
        java.lang.reflect.Field fieldReflectField3;
        int i26;
        char cCharAt10;
        int i27;
        char cCharAt11;
        int i28;
        char cCharAt12;
        int i29;
        char cCharAt13;
        String stringInfo = messageInfo.getStringInfo();
        int length = stringInfo.length();
        char c = 55296;
        if (stringInfo.charAt(0) >= 55296) {
            int i30 = 1;
            while (true) {
                i = i30 + 1;
                if (stringInfo.charAt(i30) < 55296) {
                    break;
                }
                i30 = i;
            }
        } else {
            i = 1;
        }
        int i31 = i + 1;
        int iCharAt3 = stringInfo.charAt(i);
        if (iCharAt3 >= 55296) {
            int i32 = iCharAt3 & 8191;
            int i33 = 13;
            while (true) {
                i29 = i31 + 1;
                cCharAt13 = stringInfo.charAt(i31);
                if (cCharAt13 < 55296) {
                    break;
                }
                i32 |= (cCharAt13 & 8191) << i33;
                i33 += 13;
                i31 = i29;
            }
            iCharAt3 = i32 | (cCharAt13 << i33);
            i31 = i29;
        }
        if (iCharAt3 == 0) {
            iCharAt = 0;
            iCharAt2 = 0;
            i5 = 0;
            i6 = 0;
            i2 = 0;
            i4 = 0;
            iArr = EMPTY_INT_ARRAY;
            i3 = 0;
        } else {
            int i34 = i31 + 1;
            int iCharAt4 = stringInfo.charAt(i31);
            if (iCharAt4 >= 55296) {
                int i35 = iCharAt4 & 8191;
                int i36 = 13;
                while (true) {
                    i14 = i34 + 1;
                    cCharAt8 = stringInfo.charAt(i34);
                    if (cCharAt8 < 55296) {
                        break;
                    }
                    i35 |= (cCharAt8 & 8191) << i36;
                    i36 += 13;
                    i34 = i14;
                }
                iCharAt4 = i35 | (cCharAt8 << i36);
                i34 = i14;
            }
            int i37 = i34 + 1;
            int iCharAt5 = stringInfo.charAt(i34);
            if (iCharAt5 >= 55296) {
                int i38 = iCharAt5 & 8191;
                int i39 = 13;
                while (true) {
                    i13 = i37 + 1;
                    cCharAt7 = stringInfo.charAt(i37);
                    if (cCharAt7 < 55296) {
                        break;
                    }
                    i38 |= (cCharAt7 & 8191) << i39;
                    i39 += 13;
                    i37 = i13;
                }
                iCharAt5 = i38 | (cCharAt7 << i39);
                i37 = i13;
            }
            int i40 = i37 + 1;
            int iCharAt6 = stringInfo.charAt(i37);
            if (iCharAt6 >= 55296) {
                int i41 = iCharAt6 & 8191;
                int i42 = 13;
                while (true) {
                    i12 = i40 + 1;
                    cCharAt6 = stringInfo.charAt(i40);
                    if (cCharAt6 < 55296) {
                        break;
                    }
                    i41 |= (cCharAt6 & 8191) << i42;
                    i42 += 13;
                    i40 = i12;
                }
                iCharAt6 = i41 | (cCharAt6 << i42);
                i40 = i12;
            }
            int i43 = i40 + 1;
            int iCharAt7 = stringInfo.charAt(i40);
            if (iCharAt7 >= 55296) {
                int i44 = iCharAt7 & 8191;
                int i45 = 13;
                while (true) {
                    i11 = i43 + 1;
                    cCharAt5 = stringInfo.charAt(i43);
                    if (cCharAt5 < 55296) {
                        break;
                    }
                    i44 |= (cCharAt5 & 8191) << i45;
                    i45 += 13;
                    i43 = i11;
                }
                iCharAt7 = i44 | (cCharAt5 << i45);
                i43 = i11;
            }
            int i46 = i43 + 1;
            iCharAt = stringInfo.charAt(i43);
            if (iCharAt >= 55296) {
                int i47 = iCharAt & 8191;
                int i48 = 13;
                while (true) {
                    i10 = i46 + 1;
                    cCharAt4 = stringInfo.charAt(i46);
                    if (cCharAt4 < 55296) {
                        break;
                    }
                    i47 |= (cCharAt4 & 8191) << i48;
                    i48 += 13;
                    i46 = i10;
                }
                iCharAt = i47 | (cCharAt4 << i48);
                i46 = i10;
            }
            int i49 = i46 + 1;
            iCharAt2 = stringInfo.charAt(i46);
            if (iCharAt2 >= 55296) {
                int i50 = iCharAt2 & 8191;
                int i51 = 13;
                while (true) {
                    i9 = i49 + 1;
                    cCharAt3 = stringInfo.charAt(i49);
                    if (cCharAt3 < 55296) {
                        break;
                    }
                    i50 |= (cCharAt3 & 8191) << i51;
                    i51 += 13;
                    i49 = i9;
                }
                iCharAt2 = i50 | (cCharAt3 << i51);
                i49 = i9;
            }
            int i52 = i49 + 1;
            int iCharAt8 = stringInfo.charAt(i49);
            if (iCharAt8 >= 55296) {
                int i53 = iCharAt8 & 8191;
                int i54 = 13;
                while (true) {
                    i8 = i52 + 1;
                    cCharAt2 = stringInfo.charAt(i52);
                    if (cCharAt2 < 55296) {
                        break;
                    }
                    i53 |= (cCharAt2 & 8191) << i54;
                    i54 += 13;
                    i52 = i8;
                }
                iCharAt8 = i53 | (cCharAt2 << i54);
                i52 = i8;
            }
            int i55 = i52 + 1;
            int iCharAt9 = stringInfo.charAt(i52);
            if (iCharAt9 >= 55296) {
                int i56 = iCharAt9 & 8191;
                int i57 = 13;
                while (true) {
                    i7 = i55 + 1;
                    cCharAt = stringInfo.charAt(i55);
                    if (cCharAt < 55296) {
                        break;
                    }
                    i56 |= (cCharAt & 8191) << i57;
                    i57 += 13;
                    i55 = i7;
                }
                iCharAt9 = i56 | (cCharAt << i57);
                i55 = i7;
            }
            i2 = (iCharAt4 * 2) + iCharAt5;
            i3 = iCharAt4;
            iArr = new int[iCharAt9 + iCharAt2 + iCharAt8];
            i4 = iCharAt9;
            i31 = i55;
            i5 = iCharAt6;
            i6 = iCharAt7;
        }
        Unsafe unsafe = UNSAFE;
        Object[] objects = messageInfo.getObjects();
        Class<?> cls = messageInfo.getDefaultInstance().getClass();
        int[] iArr2 = new int[iCharAt * 3];
        Object[] objArr = new Object[iCharAt * 2];
        int i58 = i4 + iCharAt2;
        int i59 = i4;
        int i60 = i58;
        int i61 = 0;
        int i62 = 0;
        while (i31 < length) {
            int i63 = i31 + 1;
            int iCharAt10 = stringInfo.charAt(i31);
            if (iCharAt10 >= c) {
                int i64 = iCharAt10 & 8191;
                int i65 = i63;
                int i66 = 13;
                while (true) {
                    i28 = i65 + 1;
                    cCharAt12 = stringInfo.charAt(i65);
                    if (cCharAt12 < c) {
                        break;
                    }
                    i64 |= (cCharAt12 & 8191) << i66;
                    i66 += 13;
                    i65 = i28;
                }
                iCharAt10 = i64 | (cCharAt12 << i66);
                i15 = i28;
            } else {
                i15 = i63;
            }
            int i67 = i15 + 1;
            int iCharAt11 = stringInfo.charAt(i15);
            if (iCharAt11 >= c) {
                int i68 = iCharAt11 & 8191;
                int i69 = i67;
                int i70 = 13;
                while (true) {
                    i27 = i69 + 1;
                    cCharAt11 = stringInfo.charAt(i69);
                    if (cCharAt11 < c) {
                        break;
                    }
                    i68 |= (cCharAt11 & 8191) << i70;
                    i70 += 13;
                    i69 = i27;
                }
                iCharAt11 = i68 | (cCharAt11 << i70);
                i16 = i27;
            } else {
                i16 = i67;
            }
            int i71 = iCharAt11 & 255;
            int i72 = length;
            if ((iCharAt11 & 1024) != 0) {
                iArr[i61] = i62;
                i61++;
            }
            int i73 = i61;
            if (i71 >= 51) {
                int i74 = i16 + 1;
                int iCharAt12 = stringInfo.charAt(i16);
                char c2 = 55296;
                if (iCharAt12 >= 55296) {
                    int i75 = iCharAt12 & 8191;
                    int i76 = 13;
                    while (true) {
                        i26 = i74 + 1;
                        cCharAt10 = stringInfo.charAt(i74);
                        if (cCharAt10 < c2) {
                            break;
                        }
                        i75 |= (cCharAt10 & 8191) << i76;
                        i76 += 13;
                        i74 = i26;
                        c2 = 55296;
                    }
                    iCharAt12 = i75 | (cCharAt10 << i76);
                    i74 = i26;
                }
                int i77 = i71 - 51;
                int i78 = i74;
                if (i77 == 9 || i77 == 17) {
                    i23 = i2 + 1;
                    objArr[((i62 / 3) * 2) + 1] = objects[i2];
                } else {
                    if (i77 == 12 && (messageInfo.getSyntax().equals(ProtoSyntax.PROTO2) || (iCharAt11 & 2048) != 0)) {
                        i23 = i2 + 1;
                        objArr[((i62 / 3) * 2) + 1] = objects[i2];
                    }
                    i24 = iCharAt12 * 2;
                    obj = objects[i24];
                    if (obj instanceof java.lang.reflect.Field) {
                        fieldReflectField2 = (java.lang.reflect.Field) obj;
                    } else {
                        fieldReflectField2 = reflectField(cls, (String) obj);
                        objects[i24] = fieldReflectField2;
                    }
                    iObjectFieldOffset2 = (int) unsafe.objectFieldOffset(fieldReflectField2);
                    i25 = i24 + 1;
                    obj2 = objects[i25];
                    if (obj2 instanceof java.lang.reflect.Field) {
                        fieldReflectField3 = (java.lang.reflect.Field) obj2;
                    } else {
                        fieldReflectField3 = reflectField(cls, (String) obj2);
                        objects[i25] = fieldReflectField3;
                    }
                    i18 = i6;
                    i17 = i2;
                    i19 = i78;
                    stringInfo = stringInfo;
                    iObjectFieldOffset = (int) unsafe.objectFieldOffset(fieldReflectField3);
                    i20 = 0;
                }
                i2 = i23;
                i24 = iCharAt12 * 2;
                obj = objects[i24];
                if (obj instanceof java.lang.reflect.Field) {
                    fieldReflectField2 = (java.lang.reflect.Field) obj;
                } else {
                    fieldReflectField2 = reflectField(cls, (String) obj);
                    objects[i24] = fieldReflectField2;
                }
                iObjectFieldOffset2 = (int) unsafe.objectFieldOffset(fieldReflectField2);
                i25 = i24 + 1;
                obj2 = objects[i25];
                if (obj2 instanceof java.lang.reflect.Field) {
                    fieldReflectField3 = (java.lang.reflect.Field) obj2;
                } else {
                    fieldReflectField3 = reflectField(cls, (String) obj2);
                    objects[i25] = fieldReflectField3;
                }
                i18 = i6;
                i17 = i2;
                i19 = i78;
                stringInfo = stringInfo;
                iObjectFieldOffset = (int) unsafe.objectFieldOffset(fieldReflectField3);
                i20 = 0;
            } else {
                i17 = i2 + 1;
                java.lang.reflect.Field fieldReflectField4 = reflectField(cls, (String) objects[i2]);
                if (i71 == 9 || i71 == 17) {
                    i18 = i6;
                    objArr[((i62 / 3) * 2) + 1] = fieldReflectField4.getType();
                } else {
                    if (i71 == 27 || i71 == 49) {
                        i18 = i6;
                        i22 = i2 + 2;
                        objArr[((i62 / 3) * 2) + 1] = objects[i17];
                    } else if (i71 == 12 || i71 == 30 || i71 == 44) {
                        i18 = i6;
                        if (messageInfo.getSyntax() == ProtoSyntax.PROTO2 || (iCharAt11 & 2048) != 0) {
                            i22 = i2 + 2;
                            objArr[((i62 / 3) * 2) + 1] = objects[i17];
                        }
                    } else {
                        if (i71 == 50) {
                            int i79 = i59 + 1;
                            iArr[i59] = i62;
                            int i80 = (i62 / 3) * 2;
                            int i81 = i2 + 2;
                            objArr[i80] = objects[i17];
                            if ((iCharAt11 & 2048) != 0) {
                                i17 = i2 + 3;
                                objArr[i80 + 1] = objects[i81];
                                i59 = i79;
                            } else {
                                i59 = i79;
                                i17 = i81;
                            }
                        }
                        i18 = i6;
                    }
                    i17 = i22;
                }
                int iObjectFieldOffset3 = (int) unsafe.objectFieldOffset(fieldReflectField4);
                if ((iCharAt11 & 4096) == 0 || i71 > 17) {
                    iObjectFieldOffset = 1048575;
                    i19 = i16;
                    i20 = 0;
                } else {
                    i19 = i16 + 1;
                    int iCharAt13 = stringInfo.charAt(i16);
                    if (iCharAt13 >= 55296) {
                        int i82 = iCharAt13 & 8191;
                        int i83 = 13;
                        while (true) {
                            i21 = i19 + 1;
                            cCharAt9 = stringInfo.charAt(i19);
                            if (cCharAt9 < 55296) {
                                break;
                            }
                            i82 |= (cCharAt9 & 8191) << i83;
                            i83 += 13;
                            i19 = i21;
                        }
                        iCharAt13 = i82 | (cCharAt9 << i83);
                        i19 = i21;
                    }
                    int i84 = (i3 * 2) + (iCharAt13 / 32);
                    Object obj3 = objects[i84];
                    if (obj3 instanceof java.lang.reflect.Field) {
                        fieldReflectField = (java.lang.reflect.Field) obj3;
                    } else {
                        fieldReflectField = reflectField(cls, (String) obj3);
                        objects[i84] = fieldReflectField;
                    }
                    iObjectFieldOffset = (int) unsafe.objectFieldOffset(fieldReflectField);
                    i20 = iCharAt13 % 32;
                }
                if (i71 >= 18 && i71 <= 49) {
                    iArr[i60] = iObjectFieldOffset3;
                    i60++;
                }
                iObjectFieldOffset2 = iObjectFieldOffset3;
            }
            int i85 = i62 + 1;
            iArr2[i62] = iCharAt10;
            int i86 = i62 + 2;
            int i87 = i3;
            iArr2[i85] = iObjectFieldOffset2 | (i71 << 20) | ((iCharAt11 & 256) != 0 ? REQUIRED_MASK : 0) | ((iCharAt11 & 512) != 0 ? ENFORCE_UTF8_MASK : 0) | ((iCharAt11 & 2048) != 0 ? Integer.MIN_VALUE : 0);
            i62 += 3;
            iArr2[i86] = iObjectFieldOffset | (i20 << 20);
            i31 = i19;
            stringInfo = stringInfo;
            i2 = i17;
            length = i72;
            i6 = i18;
            i61 = i73;
            i3 = i87;
            i5 = i5;
            c = 55296;
        }
        return new MessageSchema<>(iArr2, objArr, i5, i6, messageInfo.getDefaultInstance(), messageInfo.getSyntax(), false, iArr, i4, i58, newInstanceSchema, listFieldSchema, unknownFieldSchema, extensionSchema, mapFieldSchema);
    }

    private static java.lang.reflect.Field reflectField(Class<?> messageClass, String fieldName) {
        try {
            return messageClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException unused) {
            java.lang.reflect.Field[] declaredFields = messageClass.getDeclaredFields();
            for (java.lang.reflect.Field field : declaredFields) {
                if (fieldName.equals(field.getName())) {
                    return field;
                }
            }
            throw new RuntimeException("Field " + fieldName + " for " + messageClass.getName() + " not found. Known fields are " + Arrays.toString(declaredFields));
        }
    }

    static <T> MessageSchema<T> newSchemaForMessageInfo(StructuralMessageInfo messageInfo, NewInstanceSchema newInstanceSchema, ListFieldSchema listFieldSchema, UnknownFieldSchema<?, ?> unknownFieldSchema, ExtensionSchema<?> extensionSchema, MapFieldSchema mapFieldSchema) {
        int fieldNumber;
        int fieldNumber2;
        FieldInfo[] fields = messageInfo.getFields();
        if (fields.length == 0) {
            fieldNumber = 0;
            fieldNumber2 = 0;
        } else {
            fieldNumber = fields[0].getFieldNumber();
            fieldNumber2 = fields[fields.length - 1].getFieldNumber();
        }
        int length = fields.length;
        int[] iArr = new int[length * 3];
        Object[] objArr = new Object[length * 2];
        int i = 0;
        int i2 = 0;
        for (FieldInfo fieldInfo : fields) {
            if (fieldInfo.getType() == FieldType.MAP) {
                i++;
            } else if (fieldInfo.getType().id() >= 18 && fieldInfo.getType().id() <= 49) {
                i2++;
            }
        }
        int[] iArr2 = i > 0 ? new int[i] : null;
        int[] iArr3 = i2 > 0 ? new int[i2] : null;
        int[] checkInitialized = messageInfo.getCheckInitialized();
        if (checkInitialized == null) {
            checkInitialized = EMPTY_INT_ARRAY;
        }
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        while (i3 < fields.length) {
            FieldInfo fieldInfo2 = fields[i3];
            int fieldNumber3 = fieldInfo2.getFieldNumber();
            storeFieldData(fieldInfo2, iArr, i4, objArr);
            if (i5 < checkInitialized.length && checkInitialized[i5] == fieldNumber3) {
                checkInitialized[i5] = i4;
                i5++;
            }
            if (fieldInfo2.getType() == FieldType.MAP) {
                iArr2[i6] = i4;
                i6++;
            } else {
                if (fieldInfo2.getType().id() >= 18 && fieldInfo2.getType().id() <= 49) {
                    iArr3[i7] = (int) UnsafeUtil.objectFieldOffset(fieldInfo2.getField());
                    i7++;
                }
                i3++;
                i4 += 3;
            }
            i3++;
            i4 += 3;
        }
        if (iArr2 == null) {
            iArr2 = EMPTY_INT_ARRAY;
        }
        if (iArr3 == null) {
            iArr3 = EMPTY_INT_ARRAY;
        }
        int[] iArr4 = new int[checkInitialized.length + iArr2.length + iArr3.length];
        System.arraycopy(checkInitialized, 0, iArr4, 0, checkInitialized.length);
        System.arraycopy(iArr2, 0, iArr4, checkInitialized.length, iArr2.length);
        System.arraycopy(iArr3, 0, iArr4, checkInitialized.length + iArr2.length, iArr3.length);
        return new MessageSchema<>(iArr, objArr, fieldNumber, fieldNumber2, messageInfo.getDefaultInstance(), messageInfo.getSyntax(), true, iArr4, checkInitialized.length, checkInitialized.length + iArr2.length, newInstanceSchema, listFieldSchema, unknownFieldSchema, extensionSchema, mapFieldSchema);
    }

    /* JADX WARN: Code duplicated, block: B:21:0x007a  */
    /* JADX WARN: Code duplicated, block: B:22:0x007d  */
    /* JADX WARN: Code duplicated, block: B:25:0x0084  */
    /* JADX WARN: Code duplicated, block: B:28:0x009e  */
    /* JADX WARN: Code duplicated, block: B:30:0x00aa  */
    /* JADX WARN: Code duplicated, block: B:31:0x00af  */
    /* JADX WARN: Code duplicated, block: B:33:0x00b5  */
    /* JADX WARN: Code duplicated, block: B:34:0x00be A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:35:0x00c0  */
    /* JADX WARN: Code duplicated, block: B:36:0x00c9  */
    /* JADX WARN: Code duplicated, block: B:38:0x00cf  */
    /* JADX WARN: Code duplicated, block: B:41:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:44:? A[RETURN, SYNTHETIC] */
    private static void storeFieldData(FieldInfo fi, int[] buffer, int bufferIndex, Object[] objects) {
        int iObjectFieldOffset;
        int iId;
        long jObjectFieldOffset;
        int iObjectFieldOffset2;
        int iNumberOfTrailingZeros;
        int i;
        Class<?> messageFieldClass;
        int i2;
        OneofInfo oneof = fi.getOneof();
        if (oneof != null) {
            iId = fi.getType().id() + 51;
            iObjectFieldOffset = (int) UnsafeUtil.objectFieldOffset(oneof.getValueField());
            jObjectFieldOffset = UnsafeUtil.objectFieldOffset(oneof.getCaseField());
        } else {
            FieldType type = fi.getType();
            iObjectFieldOffset = (int) UnsafeUtil.objectFieldOffset(fi.getField());
            iId = type.id();
            if (!type.isList() && !type.isMap()) {
                java.lang.reflect.Field presenceField = fi.getPresenceField();
                iObjectFieldOffset2 = presenceField == null ? 1048575 : (int) UnsafeUtil.objectFieldOffset(presenceField);
                iNumberOfTrailingZeros = Integer.numberOfTrailingZeros(fi.getPresenceMask());
            } else if (fi.getCachedSizeField() == null) {
                iObjectFieldOffset2 = 0;
                iNumberOfTrailingZeros = 0;
            } else {
                jObjectFieldOffset = UnsafeUtil.objectFieldOffset(fi.getCachedSizeField());
            }
            buffer[bufferIndex] = fi.getFieldNumber();
            int i3 = bufferIndex + 1;
            if (fi.isEnforceUtf8()) {
                i = ENFORCE_UTF8_MASK;
            } else {
                i = 0;
            }
            buffer[i3] = (fi.isRequired() ? REQUIRED_MASK : 0) | i | (iId << 20) | iObjectFieldOffset;
            buffer[bufferIndex + 2] = iObjectFieldOffset2 | (iNumberOfTrailingZeros << 20);
            messageFieldClass = fi.getMessageFieldClass();
            if (fi.getMapDefaultEntry() != null) {
                if (messageFieldClass != null) {
                    objects[((bufferIndex / 3) * 2) + 1] = messageFieldClass;
                    return;
                } else {
                    if (fi.getEnumVerifier() != null) {
                        objects[((bufferIndex / 3) * 2) + 1] = fi.getEnumVerifier();
                        return;
                    }
                    return;
                }
            }
            i2 = (bufferIndex / 3) * 2;
            objects[i2] = fi.getMapDefaultEntry();
            if (messageFieldClass != null) {
                objects[i2 + 1] = messageFieldClass;
            } else if (fi.getEnumVerifier() != null) {
                objects[i2 + 1] = fi.getEnumVerifier();
            }
        }
        iObjectFieldOffset2 = (int) jObjectFieldOffset;
        iNumberOfTrailingZeros = 0;
        buffer[bufferIndex] = fi.getFieldNumber();
        int i4 = bufferIndex + 1;
        if (fi.isEnforceUtf8()) {
            i = ENFORCE_UTF8_MASK;
        } else {
            i = 0;
        }
        buffer[i4] = (fi.isRequired() ? REQUIRED_MASK : 0) | i | (iId << 20) | iObjectFieldOffset;
        buffer[bufferIndex + 2] = iObjectFieldOffset2 | (iNumberOfTrailingZeros << 20);
        messageFieldClass = fi.getMessageFieldClass();
        if (fi.getMapDefaultEntry() != null) {
            if (messageFieldClass != null) {
                objects[((bufferIndex / 3) * 2) + 1] = messageFieldClass;
                return;
            } else {
                if (fi.getEnumVerifier() != null) {
                    objects[((bufferIndex / 3) * 2) + 1] = fi.getEnumVerifier();
                    return;
                }
                return;
            }
        }
        i2 = (bufferIndex / 3) * 2;
        objects[i2] = fi.getMapDefaultEntry();
        if (messageFieldClass != null) {
            objects[i2 + 1] = messageFieldClass;
        } else if (fi.getEnumVerifier() != null) {
            objects[i2 + 1] = fi.getEnumVerifier();
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public T newInstance() {
        return (T) this.newInstanceSchema.newInstance(this.defaultInstance);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public boolean equals(T message, T other) {
        int length = this.buffer.length;
        for (int i = 0; i < length; i += 3) {
            if (!equals(message, other, i)) {
                return false;
            }
        }
        if (!this.unknownFieldSchema.getFromMessage(message).equals(this.unknownFieldSchema.getFromMessage(other))) {
            return false;
        }
        if (this.hasExtensions) {
            return this.extensionSchema.getExtensions(message).equals(this.extensionSchema.getExtensions(other));
        }
        return true;
    }

    private boolean equals(T message, T other, int pos) {
        int iTypeAndOffsetAt = typeAndOffsetAt(pos);
        long jOffset = offset(iTypeAndOffsetAt);
        switch (type(iTypeAndOffsetAt)) {
            case 0:
                return arePresentForEquals(message, other, pos) && Double.doubleToLongBits(UnsafeUtil.getDouble(message, jOffset)) == Double.doubleToLongBits(UnsafeUtil.getDouble(other, jOffset));
            case 1:
                return arePresentForEquals(message, other, pos) && Float.floatToIntBits(UnsafeUtil.getFloat(message, jOffset)) == Float.floatToIntBits(UnsafeUtil.getFloat(other, jOffset));
            case 2:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, jOffset) == UnsafeUtil.getLong(other, jOffset);
            case 3:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, jOffset) == UnsafeUtil.getLong(other, jOffset);
            case 4:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, jOffset) == UnsafeUtil.getInt(other, jOffset);
            case 5:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, jOffset) == UnsafeUtil.getLong(other, jOffset);
            case 6:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, jOffset) == UnsafeUtil.getInt(other, jOffset);
            case 7:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getBoolean(message, jOffset) == UnsafeUtil.getBoolean(other, jOffset);
            case 8:
                return arePresentForEquals(message, other, pos) && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, jOffset), UnsafeUtil.getObject(other, jOffset));
            case 9:
                return arePresentForEquals(message, other, pos) && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, jOffset), UnsafeUtil.getObject(other, jOffset));
            case 10:
                return arePresentForEquals(message, other, pos) && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, jOffset), UnsafeUtil.getObject(other, jOffset));
            case 11:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, jOffset) == UnsafeUtil.getInt(other, jOffset);
            case 12:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, jOffset) == UnsafeUtil.getInt(other, jOffset);
            case 13:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, jOffset) == UnsafeUtil.getInt(other, jOffset);
            case 14:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, jOffset) == UnsafeUtil.getLong(other, jOffset);
            case 15:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, jOffset) == UnsafeUtil.getInt(other, jOffset);
            case 16:
                return arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, jOffset) == UnsafeUtil.getLong(other, jOffset);
            case 17:
                return arePresentForEquals(message, other, pos) && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, jOffset), UnsafeUtil.getObject(other, jOffset));
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
                return SchemaUtil.safeEquals(UnsafeUtil.getObject(message, jOffset), UnsafeUtil.getObject(other, jOffset));
            case 50:
                return SchemaUtil.safeEquals(UnsafeUtil.getObject(message, jOffset), UnsafeUtil.getObject(other, jOffset));
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
                return isOneofCaseEqual(message, other, pos) && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, jOffset), UnsafeUtil.getObject(other, jOffset));
            default:
                return true;
        }
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public int hashCode(T message) {
        int i;
        int iHashLong;
        int length = this.buffer.length;
        int i2 = 0;
        for (int i3 = 0; i3 < length; i3 += 3) {
            int iTypeAndOffsetAt = typeAndOffsetAt(i3);
            int iNumberAt = numberAt(i3);
            long jOffset = offset(iTypeAndOffsetAt);
            int iHashCode = 37;
            switch (type(iTypeAndOffsetAt)) {
                case 0:
                    i = i2 * 53;
                    iHashLong = Internal.hashLong(Double.doubleToLongBits(UnsafeUtil.getDouble(message, jOffset)));
                    i2 = i + iHashLong;
                    break;
                case 1:
                    i = i2 * 53;
                    iHashLong = Float.floatToIntBits(UnsafeUtil.getFloat(message, jOffset));
                    i2 = i + iHashLong;
                    break;
                case 2:
                    i = i2 * 53;
                    iHashLong = Internal.hashLong(UnsafeUtil.getLong(message, jOffset));
                    i2 = i + iHashLong;
                    break;
                case 3:
                    i = i2 * 53;
                    iHashLong = Internal.hashLong(UnsafeUtil.getLong(message, jOffset));
                    i2 = i + iHashLong;
                    break;
                case 4:
                    i = i2 * 53;
                    iHashLong = UnsafeUtil.getInt(message, jOffset);
                    i2 = i + iHashLong;
                    break;
                case 5:
                    i = i2 * 53;
                    iHashLong = Internal.hashLong(UnsafeUtil.getLong(message, jOffset));
                    i2 = i + iHashLong;
                    break;
                case 6:
                    i = i2 * 53;
                    iHashLong = UnsafeUtil.getInt(message, jOffset);
                    i2 = i + iHashLong;
                    break;
                case 7:
                    i = i2 * 53;
                    iHashLong = Internal.hashBoolean(UnsafeUtil.getBoolean(message, jOffset));
                    i2 = i + iHashLong;
                    break;
                case 8:
                    i = i2 * 53;
                    iHashLong = ((String) UnsafeUtil.getObject(message, jOffset)).hashCode();
                    i2 = i + iHashLong;
                    break;
                case 9:
                    Object object = UnsafeUtil.getObject(message, jOffset);
                    if (object != null) {
                        iHashCode = object.hashCode();
                    }
                    i2 = (i2 * 53) + iHashCode;
                    break;
                case 10:
                    i = i2 * 53;
                    iHashLong = UnsafeUtil.getObject(message, jOffset).hashCode();
                    i2 = i + iHashLong;
                    break;
                case 11:
                    i = i2 * 53;
                    iHashLong = UnsafeUtil.getInt(message, jOffset);
                    i2 = i + iHashLong;
                    break;
                case 12:
                    i = i2 * 53;
                    iHashLong = UnsafeUtil.getInt(message, jOffset);
                    i2 = i + iHashLong;
                    break;
                case 13:
                    i = i2 * 53;
                    iHashLong = UnsafeUtil.getInt(message, jOffset);
                    i2 = i + iHashLong;
                    break;
                case 14:
                    i = i2 * 53;
                    iHashLong = Internal.hashLong(UnsafeUtil.getLong(message, jOffset));
                    i2 = i + iHashLong;
                    break;
                case 15:
                    i = i2 * 53;
                    iHashLong = UnsafeUtil.getInt(message, jOffset);
                    i2 = i + iHashLong;
                    break;
                case 16:
                    i = i2 * 53;
                    iHashLong = Internal.hashLong(UnsafeUtil.getLong(message, jOffset));
                    i2 = i + iHashLong;
                    break;
                case 17:
                    Object object2 = UnsafeUtil.getObject(message, jOffset);
                    if (object2 != null) {
                        iHashCode = object2.hashCode();
                    }
                    i2 = (i2 * 53) + iHashCode;
                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                    i = i2 * 53;
                    iHashLong = UnsafeUtil.getObject(message, jOffset).hashCode();
                    i2 = i + iHashLong;
                    break;
                case 50:
                    i = i2 * 53;
                    iHashLong = UnsafeUtil.getObject(message, jOffset).hashCode();
                    i2 = i + iHashLong;
                    break;
                case 51:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = Internal.hashLong(Double.doubleToLongBits(oneofDoubleAt(message, jOffset)));
                        i2 = i + iHashLong;
                    }
                    break;
                case 52:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = Float.floatToIntBits(oneofFloatAt(message, jOffset));
                        i2 = i + iHashLong;
                    }
                    break;
                case 53:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = Internal.hashLong(oneofLongAt(message, jOffset));
                        i2 = i + iHashLong;
                    }
                    break;
                case 54:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = Internal.hashLong(oneofLongAt(message, jOffset));
                        i2 = i + iHashLong;
                    }
                    break;
                case 55:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = oneofIntAt(message, jOffset);
                        i2 = i + iHashLong;
                    }
                    break;
                case 56:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = Internal.hashLong(oneofLongAt(message, jOffset));
                        i2 = i + iHashLong;
                    }
                    break;
                case 57:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = oneofIntAt(message, jOffset);
                        i2 = i + iHashLong;
                    }
                    break;
                case 58:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = Internal.hashBoolean(oneofBooleanAt(message, jOffset));
                        i2 = i + iHashLong;
                    }
                    break;
                case 59:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = ((String) UnsafeUtil.getObject(message, jOffset)).hashCode();
                        i2 = i + iHashLong;
                    }
                    break;
                case 60:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = UnsafeUtil.getObject(message, jOffset).hashCode();
                        i2 = i + iHashLong;
                    }
                    break;
                case 61:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = UnsafeUtil.getObject(message, jOffset).hashCode();
                        i2 = i + iHashLong;
                    }
                    break;
                case 62:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = oneofIntAt(message, jOffset);
                        i2 = i + iHashLong;
                    }
                    break;
                case 63:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = oneofIntAt(message, jOffset);
                        i2 = i + iHashLong;
                    }
                    break;
                case 64:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = oneofIntAt(message, jOffset);
                        i2 = i + iHashLong;
                    }
                    break;
                case 65:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = Internal.hashLong(oneofLongAt(message, jOffset));
                        i2 = i + iHashLong;
                    }
                    break;
                case 66:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = oneofIntAt(message, jOffset);
                        i2 = i + iHashLong;
                    }
                    break;
                case 67:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = Internal.hashLong(oneofLongAt(message, jOffset));
                        i2 = i + iHashLong;
                    }
                    break;
                case 68:
                    if (isOneofPresent(message, iNumberAt, i3)) {
                        i = i2 * 53;
                        iHashLong = UnsafeUtil.getObject(message, jOffset).hashCode();
                        i2 = i + iHashLong;
                    }
                    break;
            }
        }
        int iHashCode2 = (i2 * 53) + this.unknownFieldSchema.getFromMessage(message).hashCode();
        return this.hasExtensions ? (iHashCode2 * 53) + this.extensionSchema.getExtensions(message).hashCode() : iHashCode2;
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public void mergeFrom(T message, T other) {
        checkMutable(message);
        other.getClass();
        for (int i = 0; i < this.buffer.length; i += 3) {
            mergeSingleField(message, other, i);
        }
        SchemaUtil.mergeUnknownFields(this.unknownFieldSchema, message, other);
        if (this.hasExtensions) {
            SchemaUtil.mergeExtensions(this.extensionSchema, message, other);
        }
    }

    private void mergeSingleField(T message, T other, int pos) {
        int iTypeAndOffsetAt = typeAndOffsetAt(pos);
        long jOffset = offset(iTypeAndOffsetAt);
        int iNumberAt = numberAt(pos);
        switch (type(iTypeAndOffsetAt)) {
            case 0:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putDouble(message, jOffset, UnsafeUtil.getDouble(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 1:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putFloat(message, jOffset, UnsafeUtil.getFloat(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 2:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putLong(message, jOffset, UnsafeUtil.getLong(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 3:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putLong(message, jOffset, UnsafeUtil.getLong(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 4:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putInt(message, jOffset, UnsafeUtil.getInt(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 5:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putLong(message, jOffset, UnsafeUtil.getLong(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 6:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putInt(message, jOffset, UnsafeUtil.getInt(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 7:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putBoolean(message, jOffset, UnsafeUtil.getBoolean(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 8:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putObject(message, jOffset, UnsafeUtil.getObject(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 9:
                mergeMessage(message, other, pos);
                break;
            case 10:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putObject(message, jOffset, UnsafeUtil.getObject(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 11:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putInt(message, jOffset, UnsafeUtil.getInt(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 12:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putInt(message, jOffset, UnsafeUtil.getInt(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 13:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putInt(message, jOffset, UnsafeUtil.getInt(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 14:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putLong(message, jOffset, UnsafeUtil.getLong(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 15:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putInt(message, jOffset, UnsafeUtil.getInt(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 16:
                if (isFieldPresent(other, pos)) {
                    UnsafeUtil.putLong(message, jOffset, UnsafeUtil.getLong(other, jOffset));
                    setFieldPresent(message, pos);
                }
                break;
            case 17:
                mergeMessage(message, other, pos);
                break;
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
                this.listFieldSchema.mergeListsAt(message, other, jOffset);
                break;
            case 50:
                SchemaUtil.mergeMap(this.mapFieldSchema, message, other, jOffset);
                break;
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
                if (isOneofPresent(other, iNumberAt, pos)) {
                    UnsafeUtil.putObject(message, jOffset, UnsafeUtil.getObject(other, jOffset));
                    setOneofPresent(message, iNumberAt, pos);
                }
                break;
            case 60:
                mergeOneofMessage(message, other, pos);
                break;
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
                if (isOneofPresent(other, iNumberAt, pos)) {
                    UnsafeUtil.putObject(message, jOffset, UnsafeUtil.getObject(other, jOffset));
                    setOneofPresent(message, iNumberAt, pos);
                }
                break;
            case 68:
                mergeOneofMessage(message, other, pos);
                break;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void mergeMessage(T targetParent, T sourceParent, int pos) {
        if (isFieldPresent(sourceParent, pos)) {
            long jOffset = offset(typeAndOffsetAt(pos));
            Unsafe unsafe = UNSAFE;
            Object object = unsafe.getObject(sourceParent, jOffset);
            if (object == null) {
                throw new IllegalStateException("Source subfield " + numberAt(pos) + " is present but null: " + sourceParent);
            }
            Schema messageFieldSchema = getMessageFieldSchema(pos);
            if (!isFieldPresent(targetParent, pos)) {
                if (!isMutable(object)) {
                    unsafe.putObject(targetParent, jOffset, object);
                } else {
                    Object objNewInstance = messageFieldSchema.newInstance();
                    messageFieldSchema.mergeFrom(objNewInstance, object);
                    unsafe.putObject(targetParent, jOffset, objNewInstance);
                }
                setFieldPresent(targetParent, pos);
                return;
            }
            Object object2 = unsafe.getObject(targetParent, jOffset);
            if (!isMutable(object2)) {
                Object objNewInstance2 = messageFieldSchema.newInstance();
                messageFieldSchema.mergeFrom(objNewInstance2, object2);
                unsafe.putObject(targetParent, jOffset, objNewInstance2);
                object2 = objNewInstance2;
            }
            messageFieldSchema.mergeFrom(object2, object);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void mergeOneofMessage(T targetParent, T sourceParent, int pos) {
        int iNumberAt = numberAt(pos);
        if (isOneofPresent(sourceParent, iNumberAt, pos)) {
            long jOffset = offset(typeAndOffsetAt(pos));
            Unsafe unsafe = UNSAFE;
            Object object = unsafe.getObject(sourceParent, jOffset);
            if (object == null) {
                throw new IllegalStateException("Source subfield " + numberAt(pos) + " is present but null: " + sourceParent);
            }
            Schema messageFieldSchema = getMessageFieldSchema(pos);
            if (!isOneofPresent(targetParent, iNumberAt, pos)) {
                if (!isMutable(object)) {
                    unsafe.putObject(targetParent, jOffset, object);
                } else {
                    Object objNewInstance = messageFieldSchema.newInstance();
                    messageFieldSchema.mergeFrom(objNewInstance, object);
                    unsafe.putObject(targetParent, jOffset, objNewInstance);
                }
                setOneofPresent(targetParent, iNumberAt, pos);
                return;
            }
            Object object2 = unsafe.getObject(targetParent, jOffset);
            if (!isMutable(object2)) {
                Object objNewInstance2 = messageFieldSchema.newInstance();
                messageFieldSchema.mergeFrom(objNewInstance2, object2);
                unsafe.putObject(targetParent, jOffset, objNewInstance2);
                object2 = objNewInstance2;
            }
            messageFieldSchema.mergeFrom(object2, object);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v10 */
    /* JADX WARN: Type inference failed for: r0v215 */
    /* JADX WARN: Type inference failed for: r0v217 */
    /* JADX WARN: Type inference failed for: r0v219 */
    /* JADX WARN: Type inference failed for: r15v1 */
    /* JADX WARN: Type inference failed for: r15v10 */
    /* JADX WARN: Type inference failed for: r15v11 */
    /* JADX WARN: Type inference failed for: r15v12 */
    /* JADX WARN: Type inference failed for: r15v13 */
    /* JADX WARN: Type inference failed for: r15v14 */
    /* JADX WARN: Type inference failed for: r15v15 */
    /* JADX WARN: Type inference failed for: r15v16 */
    /* JADX WARN: Type inference failed for: r15v17 */
    /* JADX WARN: Type inference failed for: r15v2 */
    /* JADX WARN: Type inference failed for: r15v3 */
    /* JADX WARN: Type inference failed for: r15v4 */
    /* JADX WARN: Type inference failed for: r15v5 */
    /* JADX WARN: Type inference failed for: r15v6 */
    /* JADX WARN: Type inference failed for: r15v7 */
    /* JADX WARN: Type inference failed for: r15v8 */
    /* JADX WARN: Type inference failed for: r15v9 */
    /* JADX WARN: Type inference failed for: r16v0 */
    /* JADX WARN: Type inference failed for: r16v1 */
    /* JADX WARN: Type inference failed for: r16v2 */
    /* JADX WARN: Type inference failed for: r17v0 */
    /* JADX WARN: Type inference failed for: r17v1 */
    /* JADX WARN: Type inference failed for: r17v2 */
    /* JADX WARN: Type inference failed for: r1v0 */
    /* JADX WARN: Type inference failed for: r1v1 */
    /* JADX WARN: Type inference failed for: r1v23 */
    /* JADX WARN: Type inference failed for: r1v75 */
    /* JADX WARN: Type inference failed for: r1v76 */
    /* JADX WARN: Type inference failed for: r1v78 */
    /* JADX WARN: Type inference failed for: r4v10 */
    /* JADX WARN: Type inference failed for: r4v11 */
    /* JADX WARN: Type inference failed for: r4v12 */
    /* JADX WARN: Type inference failed for: r4v13 */
    /* JADX WARN: Type inference failed for: r4v14 */
    /* JADX WARN: Type inference failed for: r4v15 */
    /* JADX WARN: Type inference failed for: r4v16 */
    /* JADX WARN: Type inference failed for: r4v17 */
    /* JADX WARN: Type inference failed for: r4v18 */
    /* JADX WARN: Type inference failed for: r4v19 */
    /* JADX WARN: Type inference failed for: r4v20 */
    /* JADX WARN: Type inference failed for: r4v21 */
    /* JADX WARN: Type inference failed for: r4v23 */
    /* JADX WARN: Type inference failed for: r4v4 */
    /* JADX WARN: Type inference failed for: r4v6 */
    /* JADX WARN: Type inference failed for: r4v7 */
    /* JADX WARN: Type inference failed for: r4v8 */
    /* JADX WARN: Type inference failed for: r4v9 */
    /* JADX WARN: Type inference failed for: r5v10 */
    /* JADX WARN: Type inference failed for: r5v11 */
    /* JADX WARN: Type inference failed for: r5v12 */
    /* JADX WARN: Type inference failed for: r5v13 */
    /* JADX WARN: Type inference failed for: r5v14 */
    /* JADX WARN: Type inference failed for: r5v15 */
    /* JADX WARN: Type inference failed for: r5v16 */
    /* JADX WARN: Type inference failed for: r5v17 */
    /* JADX WARN: Type inference failed for: r5v18 */
    /* JADX WARN: Type inference failed for: r5v19 */
    /* JADX WARN: Type inference failed for: r5v2 */
    /* JADX WARN: Type inference failed for: r5v20 */
    /* JADX WARN: Type inference failed for: r5v21 */
    /* JADX WARN: Type inference failed for: r5v3 */
    /* JADX WARN: Type inference failed for: r5v4 */
    /* JADX WARN: Type inference failed for: r5v41 */
    /* JADX WARN: Type inference failed for: r5v5 */
    /* JADX WARN: Type inference failed for: r5v6 */
    /* JADX WARN: Type inference failed for: r5v7 */
    /* JADX WARN: Type inference failed for: r5v8 */
    /* JADX WARN: Type inference failed for: r5v9 */
    /* JADX WARN: Type inference failed for: r9v0 */
    /* JADX WARN: Type inference failed for: r9v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r9v16 */
    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public int getSerializedSize(T t) {
        int i;
        ?? r16;
        ?? r17;
        ?? r5;
        ?? r15;
        int iComputeDoubleSize;
        int iComputeBoolSize;
        int iComputeSizeFixed64List;
        int iComputeSizeFixed64ListNoTag;
        int iComputeTagSize;
        int iComputeUInt32SizeNoTag;
        ?? r1;
        int i2;
        ?? r0;
        Unsafe unsafe = UNSAFE;
        ?? r9 = 0;
        int i3 = 1048575;
        ?? r2 = 0;
        int i4 = 0;
        int iComputeSFixed32Size = 0;
        int i5 = 1048575;
        while (i4 < this.buffer.length) {
            int iTypeAndOffsetAt = typeAndOffsetAt(i4);
            int iType = type(iTypeAndOffsetAt);
            int iNumberAt = numberAt(i4);
            int i6 = this.buffer[i4 + 2];
            int i7 = i6 & i3;
            if (iType <= 17) {
                if (i7 != i5) {
                    if (i7 == i3) {
                        r0 = r9;
                    } else {
                        r0 = unsafe.getInt(t, i7 == true ? 1L : 0L);
                    }
                    r1 = r0;
                    i2 = i7 == true ? 1 : 0;
                }
                i = i2;
                r16 = r1;
                r17 = 1 << (i6 >>> 20);
            } else {
                i2 = i5;
                r1 = r2;
                i = i5 == true ? 1 : 0;
                r16 = r2 == true ? 1 : 0;
                r17 = r9;
            }
            long jOffset = offset(iTypeAndOffsetAt);
            if (iType < FieldType.DOUBLE_LIST_PACKED.id() || iType > FieldType.SINT64_LIST_PACKED.id()) {
                r5 = i7;
                r5 = r9;
            }
            r5 = i7;
            ?? r3 = r5;
            switch (iType) {
                case 0:
                    r15 = r9;
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeDoubleSize = CodedOutputStream.computeDoubleSize(iNumberAt, 0.0d);
                        r15 = r15;
                        iComputeSFixed32Size += iComputeDoubleSize;
                    }
                    break;
                case 1:
                    r15 = r9;
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeDoubleSize = CodedOutputStream.computeFloatSize(iNumberAt, 0.0f);
                        r15 = r15;
                        iComputeSFixed32Size += iComputeDoubleSize;
                    }
                    break;
                case 2:
                    r15 = r9;
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeDoubleSize = CodedOutputStream.computeInt64Size(iNumberAt, unsafe.getLong(t, jOffset));
                        r15 = r15;
                        iComputeSFixed32Size += iComputeDoubleSize;
                    }
                    break;
                case 3:
                    r15 = r9;
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeDoubleSize = CodedOutputStream.computeUInt64Size(iNumberAt, unsafe.getLong(t, jOffset));
                        r15 = r15;
                        iComputeSFixed32Size += iComputeDoubleSize;
                    }
                    break;
                case 4:
                    r15 = r9;
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeDoubleSize = CodedOutputStream.computeInt32Size(iNumberAt, unsafe.getInt(t, jOffset));
                        r15 = r15;
                        iComputeSFixed32Size += iComputeDoubleSize;
                    }
                    break;
                case 5:
                    r15 = r9;
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeDoubleSize = CodedOutputStream.computeFixed64Size(iNumberAt, 0L);
                        r15 = r15;
                        iComputeSFixed32Size += iComputeDoubleSize;
                    }
                    break;
                case 6:
                    if (!isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        r15 = 0;
                    } else {
                        r15 = 0;
                        iComputeDoubleSize = CodedOutputStream.computeFixed32Size(iNumberAt, 0);
                        iComputeSFixed32Size += iComputeDoubleSize;
                    }
                    break;
                case 7:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeBoolSize = CodedOutputStream.computeBoolSize(iNumberAt, true);
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 8:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        Object object = unsafe.getObject(t, jOffset);
                        if (object instanceof ByteString) {
                            iComputeBoolSize = CodedOutputStream.computeBytesSize(iNumberAt, (ByteString) object);
                        } else {
                            iComputeBoolSize = CodedOutputStream.computeStringSize(iNumberAt, (String) object);
                        }
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 9:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeBoolSize = SchemaUtil.computeSizeMessage(iNumberAt, unsafe.getObject(t, jOffset), getMessageFieldSchema(i4));
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 10:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeBoolSize = CodedOutputStream.computeBytesSize(iNumberAt, (ByteString) unsafe.getObject(t, jOffset));
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 11:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeBoolSize = CodedOutputStream.computeUInt32Size(iNumberAt, unsafe.getInt(t, jOffset));
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 12:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeBoolSize = CodedOutputStream.computeEnumSize(iNumberAt, unsafe.getInt(t, jOffset));
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 13:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeSFixed32Size += CodedOutputStream.computeSFixed32Size(iNumberAt, 0);
                    }
                    r15 = 0;
                    break;
                case 14:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeBoolSize = CodedOutputStream.computeSFixed64Size(iNumberAt, 0L);
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 15:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeBoolSize = CodedOutputStream.computeSInt32Size(iNumberAt, unsafe.getInt(t, jOffset));
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 16:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeBoolSize = CodedOutputStream.computeSInt64Size(iNumberAt, unsafe.getLong(t, jOffset));
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 17:
                    if (isFieldPresent(t, i4, i == true ? 1 : 0, r16 == true ? 1 : 0, r17 == true ? 1 : 0)) {
                        iComputeBoolSize = CodedOutputStream.computeGroupSize(iNumberAt, (MessageLite) unsafe.getObject(t, jOffset), getMessageFieldSchema(i4));
                        iComputeSFixed32Size += iComputeBoolSize;
                    }
                    r15 = 0;
                    break;
                case 18:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeFixed64List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 19:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeFixed32List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 20:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeInt64List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 21:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeUInt64List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 22:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeInt32List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 23:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeFixed64List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 24:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeFixed32List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 25:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeBoolList(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 26:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeStringList(iNumberAt, (List) unsafe.getObject(t, jOffset));
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 27:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeMessageList(iNumberAt, (List) unsafe.getObject(t, jOffset), getMessageFieldSchema(i4));
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 28:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeByteStringList(iNumberAt, (List) unsafe.getObject(t, jOffset));
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 29:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeUInt32List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 30:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeEnumList(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 31:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeFixed32List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 32:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeFixed64List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 33:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeSInt32List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 34:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeSInt64List(iNumberAt, (List) unsafe.getObject(t, jOffset), r9);
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 35:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeFixed64ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 36:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeFixed32ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 37:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeInt64ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 38:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeUInt64ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 39:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeInt32ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 40:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeFixed64ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 41:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeFixed32ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 42:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeBoolListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 43:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeUInt32ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 44:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeEnumListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 45:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeFixed32ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 46:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeFixed64ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 47:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeSInt32ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 48:
                    iComputeSizeFixed64ListNoTag = SchemaUtil.computeSizeSInt64ListNoTag((List) unsafe.getObject(t, jOffset));
                    if (iComputeSizeFixed64ListNoTag > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, r3 == true ? 1L : 0L, iComputeSizeFixed64ListNoTag);
                        }
                        iComputeTagSize = CodedOutputStream.computeTagSize(iNumberAt);
                        iComputeUInt32SizeNoTag = CodedOutputStream.computeUInt32SizeNoTag(iComputeSizeFixed64ListNoTag);
                        iComputeSizeFixed64List = iComputeTagSize + iComputeUInt32SizeNoTag + iComputeSizeFixed64ListNoTag;
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 49:
                    iComputeSizeFixed64List = SchemaUtil.computeSizeGroupList(iNumberAt, (List) unsafe.getObject(t, jOffset), getMessageFieldSchema(i4));
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 50:
                    iComputeSizeFixed64List = this.mapFieldSchema.getSerializedSize(iNumberAt, unsafe.getObject(t, jOffset), getMapFieldDefaultEntry(i4));
                    iComputeSFixed32Size += iComputeSizeFixed64List;
                    r15 = r9;
                    break;
                case 51:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeDoubleSize(iNumberAt, 0.0d);
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 52:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeFloatSize(iNumberAt, 0.0f);
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 53:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeInt64Size(iNumberAt, oneofLongAt(t, jOffset));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 54:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeUInt64Size(iNumberAt, oneofLongAt(t, jOffset));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 55:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeInt32Size(iNumberAt, oneofIntAt(t, jOffset));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 56:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeFixed64Size(iNumberAt, 0L);
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 57:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeFixed32Size(iNumberAt, r9);
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 58:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeBoolSize(iNumberAt, true);
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 59:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        Object object2 = unsafe.getObject(t, jOffset);
                        if (object2 instanceof ByteString) {
                            iComputeSizeFixed64List = CodedOutputStream.computeBytesSize(iNumberAt, (ByteString) object2);
                        } else {
                            iComputeSizeFixed64List = CodedOutputStream.computeStringSize(iNumberAt, (String) object2);
                        }
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 60:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = SchemaUtil.computeSizeMessage(iNumberAt, unsafe.getObject(t, jOffset), getMessageFieldSchema(i4));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 61:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeBytesSize(iNumberAt, (ByteString) unsafe.getObject(t, jOffset));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 62:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeUInt32Size(iNumberAt, oneofIntAt(t, jOffset));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 63:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeEnumSize(iNumberAt, oneofIntAt(t, jOffset));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 64:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeSFixed32Size(iNumberAt, r9);
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 65:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeSFixed64Size(iNumberAt, 0L);
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 66:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeSInt32Size(iNumberAt, oneofIntAt(t, jOffset));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 67:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeSInt64Size(iNumberAt, oneofLongAt(t, jOffset));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                case 68:
                    if (isOneofPresent(t, iNumberAt, i4)) {
                        iComputeSizeFixed64List = CodedOutputStream.computeGroupSize(iNumberAt, (MessageLite) unsafe.getObject(t, jOffset), getMessageFieldSchema(i4));
                        iComputeSFixed32Size += iComputeSizeFixed64List;
                    }
                    r15 = r9;
                    break;
                default:
                    r15 = r9;
                    break;
            }
            i4 += 3;
            i5 = i;
            r9 = r15;
            r2 = r16;
            i3 = 1048575;
        }
        int unknownFieldsSerializedSize = iComputeSFixed32Size + getUnknownFieldsSerializedSize(this.unknownFieldSchema, t);
        return this.hasExtensions ? unknownFieldsSerializedSize + this.extensionSchema.getExtensions(t).getSerializedSize() : unknownFieldsSerializedSize;
    }

    private <UT, UB> int getUnknownFieldsSerializedSize(UnknownFieldSchema<UT, UB> schema, T message) {
        return schema.getSerializedSize(schema.getFromMessage(message));
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public void writeTo(T message, Writer writer) throws IOException {
        if (writer.fieldOrder() == Writer.FieldOrder.DESCENDING) {
            writeFieldsInDescendingOrder(message, writer);
        } else {
            writeFieldsInAscendingOrder(message, writer);
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:7:0x0022  */
    private void writeFieldsInAscendingOrder(T message, Writer writer) throws IOException {
        Map.Entry<?, ?> entry;
        Iterator it;
        int i;
        Map.Entry<?, ?> entry2;
        int i2;
        int i3;
        Map.Entry<?, ?> entry3;
        boolean z;
        if (this.hasExtensions) {
            FieldSet<T> extensions = this.extensionSchema.getExtensions(message);
            if (extensions.isEmpty()) {
                entry = null;
                it = null;
            } else {
                Iterator it2 = extensions.iterator();
                entry = (Map.Entry) it2.next();
                it = it2;
            }
        } else {
            entry = null;
            it = null;
        }
        int length = this.buffer.length;
        Unsafe unsafe = UNSAFE;
        int i4 = 1048575;
        int i5 = 1048575;
        int i6 = 0;
        int i7 = 0;
        while (i7 < length) {
            int iTypeAndOffsetAt = typeAndOffsetAt(i7);
            int iNumberAt = numberAt(i7);
            int iType = type(iTypeAndOffsetAt);
            if (iType <= 17) {
                int i8 = this.buffer[i7 + 2];
                int i9 = i8 & i4;
                if (i9 != i5) {
                    i6 = i9 == i4 ? 0 : unsafe.getInt(message, i9);
                    i5 = i9;
                } else {
                    entry = entry;
                }
                int i10 = 1 << (i8 >>> 20);
                i = i5;
                i2 = i10;
                entry2 = entry;
            } else {
                i = i5;
                entry2 = entry;
                i2 = 0;
            }
            int i11 = i6;
            while (entry2 != null && this.extensionSchema.extensionNumber(entry2) <= iNumberAt) {
                this.extensionSchema.serializeExtension(writer, entry2);
                entry2 = it.hasNext() ? (Map.Entry) it.next() : null;
            }
            long jOffset = offset(iTypeAndOffsetAt);
            switch (iType) {
                case 0:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeDouble(iNumberAt, doubleAt(message, jOffset));
                    }
                    break;
                case 1:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeFloat(iNumberAt, floatAt(message, jOffset));
                    }
                    break;
                case 2:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeInt64(iNumberAt, unsafe.getLong(message, jOffset));
                    }
                    break;
                case 3:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeUInt64(iNumberAt, unsafe.getLong(message, jOffset));
                    }
                    break;
                case 4:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeInt32(iNumberAt, unsafe.getInt(message, jOffset));
                    }
                    break;
                case 5:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeFixed64(iNumberAt, unsafe.getLong(message, jOffset));
                    }
                    break;
                case 6:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeFixed32(iNumberAt, unsafe.getInt(message, jOffset));
                    }
                    break;
                case 7:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeBool(iNumberAt, booleanAt(message, jOffset));
                    }
                    break;
                case 8:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writeString(iNumberAt, unsafe.getObject(message, jOffset), writer);
                    }
                    break;
                case 9:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeMessage(iNumberAt, unsafe.getObject(message, jOffset), getMessageFieldSchema(i7));
                    }
                    break;
                case 10:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeBytes(iNumberAt, (ByteString) unsafe.getObject(message, jOffset));
                    }
                    break;
                case 11:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeUInt32(iNumberAt, unsafe.getInt(message, jOffset));
                    }
                    break;
                case 12:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeEnum(iNumberAt, unsafe.getInt(message, jOffset));
                    }
                    break;
                case 13:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeSFixed32(iNumberAt, unsafe.getInt(message, jOffset));
                    }
                    break;
                case 14:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeSFixed64(iNumberAt, unsafe.getLong(message, jOffset));
                    }
                    break;
                case 15:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeSInt32(iNumberAt, unsafe.getInt(message, jOffset));
                    }
                    break;
                case 16:
                    i3 = length;
                    entry3 = entry2;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeSInt64(iNumberAt, unsafe.getLong(message, jOffset));
                    }
                    break;
                case 17:
                    entry3 = entry2;
                    i3 = length;
                    if (isFieldPresent(message, i7, i, i11, i2)) {
                        writer.writeGroup(iNumberAt, unsafe.getObject(message, jOffset), getMessageFieldSchema(i7));
                    }
                    break;
                case 18:
                    z = false;
                    SchemaUtil.writeDoubleList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 19:
                    z = false;
                    SchemaUtil.writeFloatList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 20:
                    z = false;
                    SchemaUtil.writeInt64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 21:
                    z = false;
                    SchemaUtil.writeUInt64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 22:
                    z = false;
                    SchemaUtil.writeInt32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 23:
                    z = false;
                    SchemaUtil.writeFixed64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 24:
                    z = false;
                    SchemaUtil.writeFixed32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 25:
                    z = false;
                    SchemaUtil.writeBoolList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 26:
                    SchemaUtil.writeStringList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 27:
                    SchemaUtil.writeMessageList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, getMessageFieldSchema(i7));
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 28:
                    SchemaUtil.writeBytesList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 29:
                    z = false;
                    SchemaUtil.writeUInt32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 30:
                    z = false;
                    SchemaUtil.writeEnumList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 31:
                    z = false;
                    SchemaUtil.writeSFixed32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 32:
                    z = false;
                    SchemaUtil.writeSFixed64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 33:
                    z = false;
                    SchemaUtil.writeSInt32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 34:
                    z = false;
                    SchemaUtil.writeSInt64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, false);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 35:
                    SchemaUtil.writeDoubleList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 36:
                    SchemaUtil.writeFloatList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 37:
                    SchemaUtil.writeInt64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 38:
                    SchemaUtil.writeUInt64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 39:
                    SchemaUtil.writeInt32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 40:
                    SchemaUtil.writeFixed64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 41:
                    SchemaUtil.writeFixed32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 42:
                    SchemaUtil.writeBoolList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 43:
                    SchemaUtil.writeUInt32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 44:
                    SchemaUtil.writeEnumList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 45:
                    SchemaUtil.writeSFixed32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 46:
                    SchemaUtil.writeSFixed64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 47:
                    SchemaUtil.writeSInt32List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 48:
                    SchemaUtil.writeSInt64List(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, true);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 49:
                    SchemaUtil.writeGroupList(numberAt(i7), (List) unsafe.getObject(message, jOffset), writer, getMessageFieldSchema(i7));
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 50:
                    writeMapHelper(writer, iNumberAt, unsafe.getObject(message, jOffset), i7);
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 51:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeDouble(iNumberAt, oneofDoubleAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 52:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeFloat(iNumberAt, oneofFloatAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 53:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeInt64(iNumberAt, oneofLongAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 54:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeUInt64(iNumberAt, oneofLongAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 55:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeInt32(iNumberAt, oneofIntAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 56:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeFixed64(iNumberAt, oneofLongAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 57:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeFixed32(iNumberAt, oneofIntAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 58:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeBool(iNumberAt, oneofBooleanAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 59:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writeString(iNumberAt, unsafe.getObject(message, jOffset), writer);
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 60:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeMessage(iNumberAt, unsafe.getObject(message, jOffset), getMessageFieldSchema(i7));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 61:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeBytes(iNumberAt, (ByteString) unsafe.getObject(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 62:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeUInt32(iNumberAt, oneofIntAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 63:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeEnum(iNumberAt, oneofIntAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 64:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeSFixed32(iNumberAt, oneofIntAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 65:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeSFixed64(iNumberAt, oneofLongAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 66:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeSInt32(iNumberAt, oneofIntAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 67:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeSInt64(iNumberAt, oneofLongAt(message, jOffset));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                case 68:
                    if (isOneofPresent(message, iNumberAt, i7)) {
                        writer.writeGroup(iNumberAt, unsafe.getObject(message, jOffset), getMessageFieldSchema(i7));
                    }
                    i3 = length;
                    entry3 = entry2;
                    break;
                default:
                    i3 = length;
                    entry3 = entry2;
                    break;
            }
            i7 += 3;
            i5 = i;
            entry = entry3;
            i6 = i11;
            length = i3;
            i4 = 1048575;
        }
        while (entry != null) {
            this.extensionSchema.serializeExtension(writer, entry);
            entry = it.hasNext() ? (Map.Entry) it.next() : null;
        }
        writeUnknownInMessageTo(this.unknownFieldSchema, message, writer);
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0021  */
    private void writeFieldsInDescendingOrder(T message, Writer writer) throws IOException {
        Iterator itDescendingIterator;
        Map.Entry<?, ?> entry;
        writeUnknownInMessageTo(this.unknownFieldSchema, message, writer);
        if (this.hasExtensions) {
            FieldSet<T> extensions = this.extensionSchema.getExtensions(message);
            if (extensions.isEmpty()) {
                itDescendingIterator = null;
                entry = null;
            } else {
                itDescendingIterator = extensions.descendingIterator();
                entry = (Map.Entry) itDescendingIterator.next();
            }
        } else {
            itDescendingIterator = null;
            entry = null;
        }
        for (int length = this.buffer.length - 3; length >= 0; length -= 3) {
            int iTypeAndOffsetAt = typeAndOffsetAt(length);
            int iNumberAt = numberAt(length);
            while (entry != null && this.extensionSchema.extensionNumber(entry) > iNumberAt) {
                this.extensionSchema.serializeExtension(writer, entry);
                entry = itDescendingIterator.hasNext() ? (Map.Entry) itDescendingIterator.next() : null;
            }
            switch (type(iTypeAndOffsetAt)) {
                case 0:
                    if (isFieldPresent(message, length)) {
                        writer.writeDouble(iNumberAt, doubleAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 1:
                    if (isFieldPresent(message, length)) {
                        writer.writeFloat(iNumberAt, floatAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 2:
                    if (isFieldPresent(message, length)) {
                        writer.writeInt64(iNumberAt, longAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 3:
                    if (isFieldPresent(message, length)) {
                        writer.writeUInt64(iNumberAt, longAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 4:
                    if (isFieldPresent(message, length)) {
                        writer.writeInt32(iNumberAt, intAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 5:
                    if (isFieldPresent(message, length)) {
                        writer.writeFixed64(iNumberAt, longAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 6:
                    if (isFieldPresent(message, length)) {
                        writer.writeFixed32(iNumberAt, intAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 7:
                    if (isFieldPresent(message, length)) {
                        writer.writeBool(iNumberAt, booleanAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 8:
                    if (isFieldPresent(message, length)) {
                        writeString(iNumberAt, UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer);
                    }
                    break;
                case 9:
                    if (isFieldPresent(message, length)) {
                        writer.writeMessage(iNumberAt, UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), getMessageFieldSchema(length));
                    }
                    break;
                case 10:
                    if (isFieldPresent(message, length)) {
                        writer.writeBytes(iNumberAt, (ByteString) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 11:
                    if (isFieldPresent(message, length)) {
                        writer.writeUInt32(iNumberAt, intAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 12:
                    if (isFieldPresent(message, length)) {
                        writer.writeEnum(iNumberAt, intAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 13:
                    if (isFieldPresent(message, length)) {
                        writer.writeSFixed32(iNumberAt, intAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 14:
                    if (isFieldPresent(message, length)) {
                        writer.writeSFixed64(iNumberAt, longAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 15:
                    if (isFieldPresent(message, length)) {
                        writer.writeSInt32(iNumberAt, intAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 16:
                    if (isFieldPresent(message, length)) {
                        writer.writeSInt64(iNumberAt, longAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 17:
                    if (isFieldPresent(message, length)) {
                        writer.writeGroup(iNumberAt, UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), getMessageFieldSchema(length));
                    }
                    break;
                case 18:
                    SchemaUtil.writeDoubleList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 19:
                    SchemaUtil.writeFloatList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 20:
                    SchemaUtil.writeInt64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 21:
                    SchemaUtil.writeUInt64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 22:
                    SchemaUtil.writeInt32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 23:
                    SchemaUtil.writeFixed64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 24:
                    SchemaUtil.writeFixed32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 25:
                    SchemaUtil.writeBoolList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 26:
                    SchemaUtil.writeStringList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer);
                    break;
                case 27:
                    SchemaUtil.writeMessageList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, getMessageFieldSchema(length));
                    break;
                case 28:
                    SchemaUtil.writeBytesList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer);
                    break;
                case 29:
                    SchemaUtil.writeUInt32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 30:
                    SchemaUtil.writeEnumList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 31:
                    SchemaUtil.writeSFixed32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 32:
                    SchemaUtil.writeSFixed64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 33:
                    SchemaUtil.writeSInt32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 34:
                    SchemaUtil.writeSInt64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, false);
                    break;
                case 35:
                    SchemaUtil.writeDoubleList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 36:
                    SchemaUtil.writeFloatList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 37:
                    SchemaUtil.writeInt64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 38:
                    SchemaUtil.writeUInt64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 39:
                    SchemaUtil.writeInt32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 40:
                    SchemaUtil.writeFixed64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 41:
                    SchemaUtil.writeFixed32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 42:
                    SchemaUtil.writeBoolList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 43:
                    SchemaUtil.writeUInt32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 44:
                    SchemaUtil.writeEnumList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 45:
                    SchemaUtil.writeSFixed32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 46:
                    SchemaUtil.writeSFixed64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 47:
                    SchemaUtil.writeSInt32List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 48:
                    SchemaUtil.writeSInt64List(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, true);
                    break;
                case 49:
                    SchemaUtil.writeGroupList(numberAt(length), (List) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer, getMessageFieldSchema(length));
                    break;
                case 50:
                    writeMapHelper(writer, iNumberAt, UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), length);
                    break;
                case 51:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeDouble(iNumberAt, oneofDoubleAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 52:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeFloat(iNumberAt, oneofFloatAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 53:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeInt64(iNumberAt, oneofLongAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 54:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeUInt64(iNumberAt, oneofLongAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 55:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeInt32(iNumberAt, oneofIntAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 56:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeFixed64(iNumberAt, oneofLongAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 57:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeFixed32(iNumberAt, oneofIntAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 58:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeBool(iNumberAt, oneofBooleanAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 59:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writeString(iNumberAt, UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), writer);
                    }
                    break;
                case 60:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeMessage(iNumberAt, UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), getMessageFieldSchema(length));
                    }
                    break;
                case 61:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeBytes(iNumberAt, (ByteString) UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 62:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeUInt32(iNumberAt, oneofIntAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 63:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeEnum(iNumberAt, oneofIntAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 64:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeSFixed32(iNumberAt, oneofIntAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 65:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeSFixed64(iNumberAt, oneofLongAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 66:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeSInt32(iNumberAt, oneofIntAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 67:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeSInt64(iNumberAt, oneofLongAt(message, offset(iTypeAndOffsetAt)));
                    }
                    break;
                case 68:
                    if (isOneofPresent(message, iNumberAt, length)) {
                        writer.writeGroup(iNumberAt, UnsafeUtil.getObject(message, offset(iTypeAndOffsetAt)), getMessageFieldSchema(length));
                    }
                    break;
            }
        }
        while (entry != null) {
            this.extensionSchema.serializeExtension(writer, entry);
            entry = itDescendingIterator.hasNext() ? (Map.Entry) itDescendingIterator.next() : null;
        }
    }

    private <K, V> void writeMapHelper(Writer writer, int number, Object mapField, int pos) throws IOException {
        if (mapField != null) {
            writer.writeMap(number, this.mapFieldSchema.forMapMetadata(getMapFieldDefaultEntry(pos)), this.mapFieldSchema.forMapData(mapField));
        }
    }

    private <UT, UB> void writeUnknownInMessageTo(UnknownFieldSchema<UT, UB> schema, T message, Writer writer) throws IOException {
        schema.writeTo(schema.getFromMessage(message), writer);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public void mergeFrom(T message, Reader reader, ExtensionRegistryLite extensionRegistry) throws Throwable {
        extensionRegistry.getClass();
        checkMutable(message);
        mergeFromHelper(this.unknownFieldSchema, this.extensionSchema, message, reader, extensionRegistry);
    }

    /* JADX WARN: Code duplicated, block: B:166:0x0642 A[Catch: all -> 0x069a, TRY_LEAVE, TryCatch #9 {all -> 0x069a, blocks: (B:153:0x0613, B:164:0x063c, B:166:0x0642, B:176:0x066a, B:177:0x066f), top: B:207:0x0613 }] */
    /* JADX WARN: Code duplicated, block: B:171:0x064f A[LOOP:2: B:169:0x064b->B:171:0x064f, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:173:0x0664  */
    /* JADX WARN: Code duplicated, block: B:175:0x0668 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:176:0x066a A[Catch: all -> 0x069a, TRY_ENTER, TryCatch #9 {all -> 0x069a, blocks: (B:153:0x0613, B:164:0x063c, B:166:0x0642, B:176:0x066a, B:177:0x066f), top: B:207:0x0613 }] */
    /* JADX WARN: Code duplicated, block: B:182:0x067c A[LOOP:3: B:180:0x0678->B:182:0x067c, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:184:0x0691  */
    /* JADX WARN: Code duplicated, block: B:198:0x06ab A[LOOP:4: B:196:0x06a7->B:198:0x06ab, LOOP_END] */
    /* JADX WARN: Code duplicated, block: B:200:0x06c0  */
    /* JADX WARN: Code duplicated, block: B:228:0x0648 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:229:0x0675 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:242:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:243:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r22v0, types: [com.google.crypto.tink.shaded.protobuf.Reader] */
    private <UT, UB, ET extends FieldSet.FieldDescriptorLite<ET>> void mergeFromHelper(UnknownFieldSchema<UT, UB> unknownFieldSchema, ExtensionSchema<ET> extensionSchema, T t, Reader reader, ExtensionRegistryLite extensionRegistryLite) throws Throwable {
        T t2;
        UnknownFieldSchema unknownFieldSchema2;
        int i;
        Object objFilterMapUnknownEnumValues;
        T t3;
        ExtensionRegistryLite extensionRegistryLite2;
        int i2;
        Object objFilterMapUnknownEnumValues2;
        int i3;
        Object objFilterMapUnknownEnumValues3;
        Object obj;
        UnknownFieldSchema unknownFieldSchema3 = unknownFieldSchema;
        T t4 = t;
        ExtensionRegistryLite extensionRegistryLite3 = extensionRegistryLite;
        Object builderFromMessage = null;
        FieldSet mutableExtensions = null;
        while (true) {
            try {
                int fieldNumber = reader.getFieldNumber();
                int iPositionForFieldNumber = positionForFieldNumber(fieldNumber);
                if (iPositionForFieldNumber >= 0) {
                    t2 = t4;
                    try {
                        int iTypeAndOffsetAt = typeAndOffsetAt(iPositionForFieldNumber);
                        try {
                            switch (type(iTypeAndOffsetAt)) {
                                case 0:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putDouble(t2, offset(iTypeAndOffsetAt), reader.readDouble());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 1:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putFloat(t2, offset(iTypeAndOffsetAt), reader.readFloat());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 2:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putLong(t2, offset(iTypeAndOffsetAt), reader.readInt64());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 3:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putLong(t2, offset(iTypeAndOffsetAt), reader.readUInt64());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 4:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putInt(t2, offset(iTypeAndOffsetAt), reader.readInt32());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 5:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putLong(t2, offset(iTypeAndOffsetAt), reader.readFixed64());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 6:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putInt(t2, offset(iTypeAndOffsetAt), reader.readFixed32());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 7:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putBoolean(t2, offset(iTypeAndOffsetAt), reader.readBool());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 8:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    readString(t2, iTypeAndOffsetAt, reader);
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 9:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    MessageLite messageLite = (MessageLite) mutableMessageFieldForMerge(t2, iPositionForFieldNumber);
                                    reader.mergeMessageField(messageLite, getMessageFieldSchema(iPositionForFieldNumber), extensionRegistryLite2);
                                    storeMessageField(t2, iPositionForFieldNumber, messageLite);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 10:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), reader.readBytes());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 11:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putInt(t2, offset(iTypeAndOffsetAt), reader.readUInt32());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 12:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    int i4 = reader.readEnum();
                                    Internal.EnumVerifier enumFieldVerifier = getEnumFieldVerifier(iPositionForFieldNumber);
                                    if (enumFieldVerifier == null || enumFieldVerifier.isInRange(i4)) {
                                        UnsafeUtil.putInt(t2, offset(iTypeAndOffsetAt), i4);
                                        setFieldPresent(t2, iPositionForFieldNumber);
                                        builderFromMessage = obj;
                                    } else {
                                        builderFromMessage = SchemaUtil.storeUnknownEnum(t2, fieldNumber, i4, obj, unknownFieldSchema2);
                                    }
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 13:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putInt(t2, offset(iTypeAndOffsetAt), reader.readSFixed32());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 14:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putLong(t2, offset(iTypeAndOffsetAt), reader.readSFixed64());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 15:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putInt(t2, offset(iTypeAndOffsetAt), reader.readSInt32());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 16:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    UnsafeUtil.putLong(t2, offset(iTypeAndOffsetAt), reader.readSInt64());
                                    setFieldPresent(t2, iPositionForFieldNumber);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 17:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    MessageLite messageLite2 = (MessageLite) mutableMessageFieldForMerge(t2, iPositionForFieldNumber);
                                    reader.mergeGroupField(messageLite2, getMessageFieldSchema(iPositionForFieldNumber), extensionRegistryLite2);
                                    storeMessageField(t2, iPositionForFieldNumber, messageLite2);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 18:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readDoubleList(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 19:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readFloatList(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 20:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readInt64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 21:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readUInt64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 22:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readInt32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 23:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readFixed64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 24:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readFixed32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 25:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readBoolList(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 26:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    readStringList(t2, iTypeAndOffsetAt, reader);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 27:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    readMessageList(t, iTypeAndOffsetAt, reader, getMessageFieldSchema(iPositionForFieldNumber), extensionRegistryLite);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 28:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readBytesList(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 29:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readUInt32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 30:
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    List listMutableListAt = this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt));
                                    reader.readEnumList(listMutableListAt);
                                    builderFromMessage = SchemaUtil.filterUnknownEnumList(t, fieldNumber, (List<Integer>) listMutableListAt, getEnumFieldVerifier(iPositionForFieldNumber), builderFromMessage, unknownFieldSchema);
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 31:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readSFixed32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 32:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readSFixed64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 33:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readSInt32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 34:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readSInt64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 35:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readDoubleList(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 36:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readFloatList(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 37:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readInt64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 38:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readUInt64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 39:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readInt32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 40:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readFixed64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 41:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readFixed32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 42:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readBoolList(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 43:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readUInt32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 44:
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    List listMutableListAt2 = this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt));
                                    reader.readEnumList(listMutableListAt2);
                                    builderFromMessage = SchemaUtil.filterUnknownEnumList(t, fieldNumber, (List<Integer>) listMutableListAt2, getEnumFieldVerifier(iPositionForFieldNumber), builderFromMessage, unknownFieldSchema);
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 45:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readSFixed32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 46:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readSFixed64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 47:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readSInt32List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 48:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    reader.readSInt64List(this.listFieldSchema.mutableListAt(t2, offset(iTypeAndOffsetAt)));
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 49:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    readGroupList(t, offset(iTypeAndOffsetAt), reader, getMessageFieldSchema(iPositionForFieldNumber), extensionRegistryLite);
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 50:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    try {
                                        mergeMap(t, iPositionForFieldNumber, getMapFieldDefaultEntry(iPositionForFieldNumber), extensionRegistryLite, reader);
                                        unknownFieldSchema2 = unknownFieldSchema3;
                                        builderFromMessage = obj;
                                    } catch (InvalidProtocolBufferException.InvalidWireTypeException unused) {
                                        unknownFieldSchema2 = unknownFieldSchema3;
                                        builderFromMessage = obj;
                                        if (unknownFieldSchema2.shouldDiscardUnknownFields(reader)) {
                                            if (builderFromMessage == null) {
                                                builderFromMessage = unknownFieldSchema2.getBuilderFromMessage(t2);
                                            }
                                            if (!unknownFieldSchema2.mergeOneFieldFrom(builderFromMessage, reader, 0)) {
                                                objFilterMapUnknownEnumValues2 = builderFromMessage;
                                                for (i2 = this.checkInitializedCount; i2 < this.repeatedFieldOffsetStart; i2++) {
                                                    objFilterMapUnknownEnumValues2 = filterMapUnknownEnumValues(t, this.intArray[i2], objFilterMapUnknownEnumValues2, unknownFieldSchema, t);
                                                }
                                                if (objFilterMapUnknownEnumValues2 != null) {
                                                    unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues2);
                                                    return;
                                                }
                                                return;
                                            }
                                        } else if (!reader.skipField()) {
                                            objFilterMapUnknownEnumValues3 = builderFromMessage;
                                            for (i3 = this.checkInitializedCount; i3 < this.repeatedFieldOffsetStart; i3++) {
                                                objFilterMapUnknownEnumValues3 = filterMapUnknownEnumValues(t, this.intArray[i3], objFilterMapUnknownEnumValues3, unknownFieldSchema, t);
                                            }
                                            if (objFilterMapUnknownEnumValues3 != null) {
                                                unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues3);
                                                return;
                                            }
                                            return;
                                        }
                                    } catch (Throwable th) {
                                        th = th;
                                        unknownFieldSchema2 = unknownFieldSchema3;
                                        builderFromMessage = obj;
                                        objFilterMapUnknownEnumValues = builderFromMessage;
                                        for (i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; i++) {
                                            objFilterMapUnknownEnumValues = filterMapUnknownEnumValues(t, this.intArray[i], objFilterMapUnknownEnumValues, unknownFieldSchema, t);
                                        }
                                        if (objFilterMapUnknownEnumValues != null) {
                                            unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues);
                                        }
                                        throw th;
                                    }
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 51:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Double.valueOf(reader.readDouble()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 52:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Float.valueOf(reader.readFloat()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 53:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Long.valueOf(reader.readInt64()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 54:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Long.valueOf(reader.readUInt64()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 55:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Integer.valueOf(reader.readInt32()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 56:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Long.valueOf(reader.readFixed64()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 57:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Integer.valueOf(reader.readFixed32()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 58:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Boolean.valueOf(reader.readBool()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 59:
                                    readString(t2, iTypeAndOffsetAt, reader);
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 60:
                                    MessageLite messageLite3 = (MessageLite) mutableOneofMessageFieldForMerge(t2, fieldNumber, iPositionForFieldNumber);
                                    reader.mergeMessageField(messageLite3, getMessageFieldSchema(iPositionForFieldNumber), extensionRegistryLite3);
                                    storeOneofMessageField(t2, fieldNumber, iPositionForFieldNumber, messageLite3);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 61:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), reader.readBytes());
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 62:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Integer.valueOf(reader.readUInt32()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 63:
                                    int i5 = reader.readEnum();
                                    Internal.EnumVerifier enumFieldVerifier2 = getEnumFieldVerifier(iPositionForFieldNumber);
                                    if (enumFieldVerifier2 == null || enumFieldVerifier2.isInRange(i5)) {
                                        UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Integer.valueOf(i5));
                                        setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                        obj = builderFromMessage;
                                        extensionRegistryLite2 = extensionRegistryLite3;
                                        unknownFieldSchema2 = unknownFieldSchema3;
                                        builderFromMessage = obj;
                                    } else {
                                        builderFromMessage = SchemaUtil.storeUnknownEnum(t2, fieldNumber, i5, builderFromMessage, unknownFieldSchema3);
                                        extensionRegistryLite2 = extensionRegistryLite3;
                                        unknownFieldSchema2 = unknownFieldSchema3;
                                    }
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 64:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Integer.valueOf(reader.readSFixed32()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 65:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Long.valueOf(reader.readSFixed64()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 66:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Integer.valueOf(reader.readSInt32()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 67:
                                    UnsafeUtil.putObject(t2, offset(iTypeAndOffsetAt), Long.valueOf(reader.readSInt64()));
                                    setOneofPresent(t2, fieldNumber, iPositionForFieldNumber);
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    builderFromMessage = obj;
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                case 68:
                                    try {
                                        MessageLite messageLite4 = (MessageLite) mutableOneofMessageFieldForMerge(t2, fieldNumber, iPositionForFieldNumber);
                                        reader.mergeGroupField(messageLite4, getMessageFieldSchema(iPositionForFieldNumber), extensionRegistryLite3);
                                        storeOneofMessageField(t2, fieldNumber, iPositionForFieldNumber, messageLite4);
                                        obj = builderFromMessage;
                                        extensionRegistryLite2 = extensionRegistryLite3;
                                        unknownFieldSchema2 = unknownFieldSchema3;
                                        builderFromMessage = obj;
                                    } catch (InvalidProtocolBufferException.InvalidWireTypeException unused2) {
                                        extensionRegistryLite2 = extensionRegistryLite3;
                                        unknownFieldSchema2 = unknownFieldSchema3;
                                        if (unknownFieldSchema2.shouldDiscardUnknownFields(reader)) {
                                            if (builderFromMessage == null) {
                                                builderFromMessage = unknownFieldSchema2.getBuilderFromMessage(t2);
                                            }
                                            if (!unknownFieldSchema2.mergeOneFieldFrom(builderFromMessage, reader, 0)) {
                                                objFilterMapUnknownEnumValues2 = builderFromMessage;
                                                while (i2 < this.repeatedFieldOffsetStart) {
                                                    objFilterMapUnknownEnumValues2 = filterMapUnknownEnumValues(t, this.intArray[i2], objFilterMapUnknownEnumValues2, unknownFieldSchema, t);
                                                }
                                                if (objFilterMapUnknownEnumValues2 != null) {
                                                    unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues2);
                                                    return;
                                                }
                                                return;
                                            }
                                        } else if (!reader.skipField()) {
                                            objFilterMapUnknownEnumValues3 = builderFromMessage;
                                            while (i3 < this.repeatedFieldOffsetStart) {
                                                objFilterMapUnknownEnumValues3 = filterMapUnknownEnumValues(t, this.intArray[i3], objFilterMapUnknownEnumValues3, unknownFieldSchema, t);
                                            }
                                            if (objFilterMapUnknownEnumValues3 != null) {
                                                unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues3);
                                                return;
                                            }
                                            return;
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        unknownFieldSchema2 = unknownFieldSchema3;
                                        objFilterMapUnknownEnumValues = builderFromMessage;
                                        while (i < this.repeatedFieldOffsetStart) {
                                            objFilterMapUnknownEnumValues = filterMapUnknownEnumValues(t, this.intArray[i], objFilterMapUnknownEnumValues, unknownFieldSchema, t);
                                        }
                                        if (objFilterMapUnknownEnumValues != null) {
                                            unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues);
                                        }
                                        throw th;
                                    }
                                    extensionRegistryLite3 = extensionRegistryLite2;
                                    unknownFieldSchema3 = unknownFieldSchema2;
                                    t4 = t2;
                                    break;
                                default:
                                    obj = builderFromMessage;
                                    extensionRegistryLite2 = extensionRegistryLite3;
                                    unknownFieldSchema2 = unknownFieldSchema3;
                                    if (obj == null) {
                                        try {
                                            builderFromMessage = unknownFieldSchema2.getBuilderFromMessage(t2);
                                        } catch (InvalidProtocolBufferException.InvalidWireTypeException unused3) {
                                            builderFromMessage = obj;
                                            if (unknownFieldSchema2.shouldDiscardUnknownFields(reader)) {
                                                if (builderFromMessage == null) {
                                                    builderFromMessage = unknownFieldSchema2.getBuilderFromMessage(t2);
                                                }
                                                if (!unknownFieldSchema2.mergeOneFieldFrom(builderFromMessage, reader, 0)) {
                                                    objFilterMapUnknownEnumValues2 = builderFromMessage;
                                                    while (i2 < this.repeatedFieldOffsetStart) {
                                                        objFilterMapUnknownEnumValues2 = filterMapUnknownEnumValues(t, this.intArray[i2], objFilterMapUnknownEnumValues2, unknownFieldSchema, t);
                                                    }
                                                    if (objFilterMapUnknownEnumValues2 != null) {
                                                        unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues2);
                                                        return;
                                                    }
                                                    return;
                                                }
                                            } else if (!reader.skipField()) {
                                                objFilterMapUnknownEnumValues3 = builderFromMessage;
                                                while (i3 < this.repeatedFieldOffsetStart) {
                                                    objFilterMapUnknownEnumValues3 = filterMapUnknownEnumValues(t, this.intArray[i3], objFilterMapUnknownEnumValues3, unknownFieldSchema, t);
                                                }
                                                if (objFilterMapUnknownEnumValues3 != null) {
                                                    unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues3);
                                                    return;
                                                }
                                                return;
                                            }
                                            extensionRegistryLite3 = extensionRegistryLite2;
                                            unknownFieldSchema3 = unknownFieldSchema2;
                                            t4 = t2;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            builderFromMessage = obj;
                                        }
                                    } else {
                                        builderFromMessage = obj;
                                    }
                                    try {
                                        try {
                                            if (!unknownFieldSchema2.mergeOneFieldFrom(builderFromMessage, reader, 0)) {
                                                Object objFilterMapUnknownEnumValues4 = builderFromMessage;
                                                for (int i6 = this.checkInitializedCount; i6 < this.repeatedFieldOffsetStart; i6++) {
                                                    objFilterMapUnknownEnumValues4 = filterMapUnknownEnumValues(t, this.intArray[i6], objFilterMapUnknownEnumValues4, unknownFieldSchema, t);
                                                }
                                                if (objFilterMapUnknownEnumValues4 != null) {
                                                    unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues4);
                                                    return;
                                                }
                                                return;
                                            }
                                        } catch (InvalidProtocolBufferException.InvalidWireTypeException unused4) {
                                            if (unknownFieldSchema2.shouldDiscardUnknownFields(reader)) {
                                                if (builderFromMessage == null) {
                                                    builderFromMessage = unknownFieldSchema2.getBuilderFromMessage(t2);
                                                }
                                                if (!unknownFieldSchema2.mergeOneFieldFrom(builderFromMessage, reader, 0)) {
                                                    objFilterMapUnknownEnumValues2 = builderFromMessage;
                                                    while (i2 < this.repeatedFieldOffsetStart) {
                                                        objFilterMapUnknownEnumValues2 = filterMapUnknownEnumValues(t, this.intArray[i2], objFilterMapUnknownEnumValues2, unknownFieldSchema, t);
                                                    }
                                                    if (objFilterMapUnknownEnumValues2 != null) {
                                                        unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues2);
                                                        return;
                                                    }
                                                    return;
                                                }
                                            } else if (!reader.skipField()) {
                                                objFilterMapUnknownEnumValues3 = builderFromMessage;
                                                while (i3 < this.repeatedFieldOffsetStart) {
                                                    objFilterMapUnknownEnumValues3 = filterMapUnknownEnumValues(t, this.intArray[i3], objFilterMapUnknownEnumValues3, unknownFieldSchema, t);
                                                }
                                                if (objFilterMapUnknownEnumValues3 != null) {
                                                    unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues3);
                                                    return;
                                                }
                                                return;
                                            }
                                        }
                                        extensionRegistryLite3 = extensionRegistryLite2;
                                        unknownFieldSchema3 = unknownFieldSchema2;
                                        t4 = t2;
                                    } catch (Throwable th4) {
                                        th = th4;
                                    }
                                    break;
                            }
                        } catch (InvalidProtocolBufferException.InvalidWireTypeException unused5) {
                        }
                    } catch (Throwable th5) {
                        th = th5;
                    }
                } else {
                    if (fieldNumber == Integer.MAX_VALUE) {
                        Object objFilterMapUnknownEnumValues5 = builderFromMessage;
                        for (int i7 = this.checkInitializedCount; i7 < this.repeatedFieldOffsetStart; i7++) {
                            objFilterMapUnknownEnumValues5 = filterMapUnknownEnumValues(t, this.intArray[i7], objFilterMapUnknownEnumValues5, unknownFieldSchema, t);
                        }
                        if (objFilterMapUnknownEnumValues5 != null) {
                            unknownFieldSchema3.setBuilderToMessage(t4, objFilterMapUnknownEnumValues5);
                            return;
                        }
                        return;
                    }
                    try {
                        Object objFindExtensionByNumber = !this.hasExtensions ? null : extensionSchema.findExtensionByNumber(extensionRegistryLite3, this.defaultInstance, fieldNumber);
                        if (objFindExtensionByNumber != null) {
                            if (mutableExtensions == null) {
                                mutableExtensions = extensionSchema.getMutableExtensions(t);
                            }
                            FieldSet fieldSet = mutableExtensions;
                            t3 = t4;
                            try {
                                builderFromMessage = extensionSchema.parseExtension(t, reader, objFindExtensionByNumber, extensionRegistryLite, fieldSet, builderFromMessage, unknownFieldSchema);
                                mutableExtensions = fieldSet;
                            } catch (Throwable th6) {
                                th = th6;
                                t2 = t3;
                                unknownFieldSchema2 = unknownFieldSchema3;
                                objFilterMapUnknownEnumValues = builderFromMessage;
                                while (i < this.repeatedFieldOffsetStart) {
                                    objFilterMapUnknownEnumValues = filterMapUnknownEnumValues(t, this.intArray[i], objFilterMapUnknownEnumValues, unknownFieldSchema, t);
                                }
                                if (objFilterMapUnknownEnumValues != null) {
                                    unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues);
                                }
                                throw th;
                            }
                        } else {
                            t3 = t4;
                            if (unknownFieldSchema3.shouldDiscardUnknownFields(reader)) {
                                if (!reader.skipField()) {
                                }
                                mutableExtensions = mutableExtensions;
                            } else {
                                if (builderFromMessage == null) {
                                    builderFromMessage = unknownFieldSchema3.getBuilderFromMessage(t3);
                                }
                                if (!unknownFieldSchema3.mergeOneFieldFrom(builderFromMessage, reader, 0)) {
                                }
                                mutableExtensions = mutableExtensions;
                            }
                        }
                        t4 = t3;
                    } catch (Throwable th7) {
                        th = th7;
                        t2 = t4;
                        unknownFieldSchema2 = unknownFieldSchema3;
                        objFilterMapUnknownEnumValues = builderFromMessage;
                        while (i < this.repeatedFieldOffsetStart) {
                            objFilterMapUnknownEnumValues = filterMapUnknownEnumValues(t, this.intArray[i], objFilterMapUnknownEnumValues, unknownFieldSchema, t);
                        }
                        if (objFilterMapUnknownEnumValues != null) {
                            unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues);
                        }
                        throw th;
                    }
                }
            } catch (Throwable th8) {
                th = th8;
            }
            objFilterMapUnknownEnumValues = builderFromMessage;
            while (i < this.repeatedFieldOffsetStart) {
                objFilterMapUnknownEnumValues = filterMapUnknownEnumValues(t, this.intArray[i], objFilterMapUnknownEnumValues, unknownFieldSchema, t);
            }
            if (objFilterMapUnknownEnumValues != null) {
                unknownFieldSchema2.setBuilderToMessage(t2, objFilterMapUnknownEnumValues);
            }
            throw th;
        }
        int i8 = this.checkInitializedCount;
        Object objFilterMapUnknownEnumValues6 = builderFromMessage;
        while (i8 < this.repeatedFieldOffsetStart) {
            objFilterMapUnknownEnumValues6 = filterMapUnknownEnumValues(t, this.intArray[i8], objFilterMapUnknownEnumValues6, unknownFieldSchema, t);
            i8++;
            t3 = t3;
        }
        T t5 = t3;
        if (objFilterMapUnknownEnumValues6 != null) {
            unknownFieldSchema3.setBuilderToMessage(t5, objFilterMapUnknownEnumValues6);
        }
    }

    static UnknownFieldSetLite getMutableUnknownFields(Object message) {
        GeneratedMessageLite generatedMessageLite = (GeneratedMessageLite) message;
        UnknownFieldSetLite unknownFieldSetLite = generatedMessageLite.unknownFields;
        if (unknownFieldSetLite != UnknownFieldSetLite.getDefaultInstance()) {
            return unknownFieldSetLite;
        }
        UnknownFieldSetLite unknownFieldSetLiteNewInstance = UnknownFieldSetLite.newInstance();
        generatedMessageLite.unknownFields = unknownFieldSetLiteNewInstance;
        return unknownFieldSetLiteNewInstance;
    }

    /* JADX INFO: renamed from: com.google.crypto.tink.shaded.protobuf.MessageSchema$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$protobuf$WireFormat$FieldType;

        static {
            int[] iArr = new int[WireFormat.FieldType.values().length];
            $SwitchMap$com$google$protobuf$WireFormat$FieldType = iArr;
            try {
                iArr[WireFormat.FieldType.BOOL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.BYTES.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.DOUBLE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.FIXED32.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SFIXED32.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.FIXED64.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SFIXED64.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.FLOAT.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.ENUM.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.INT32.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.UINT32.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.INT64.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.UINT64.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.MESSAGE.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SINT32.ordinal()] = 15;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SINT64.ordinal()] = 16;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.STRING.ordinal()] = 17;
            } catch (NoSuchFieldError unused17) {
            }
        }
    }

    private int decodeMapEntryValue(byte[] data, int position, int limit, WireFormat.FieldType fieldType, Class<?> messageType, ArrayDecoders.Registers registers) throws IOException {
        switch (AnonymousClass1.$SwitchMap$com$google$protobuf$WireFormat$FieldType[fieldType.ordinal()]) {
            case 1:
                int iDecodeVarint64 = ArrayDecoders.decodeVarint64(data, position, registers);
                registers.object1 = Boolean.valueOf(registers.long1 != 0);
                return iDecodeVarint64;
            case 2:
                return ArrayDecoders.decodeBytes(data, position, registers);
            case 3:
                registers.object1 = Double.valueOf(ArrayDecoders.decodeDouble(data, position));
                return position + 8;
            case 4:
            case 5:
                registers.object1 = Integer.valueOf(ArrayDecoders.decodeFixed32(data, position));
                return position + 4;
            case 6:
            case 7:
                registers.object1 = Long.valueOf(ArrayDecoders.decodeFixed64(data, position));
                return position + 8;
            case 8:
                registers.object1 = Float.valueOf(ArrayDecoders.decodeFloat(data, position));
                return position + 4;
            case 9:
            case 10:
            case 11:
                int iDecodeVarint32 = ArrayDecoders.decodeVarint32(data, position, registers);
                registers.object1 = Integer.valueOf(registers.int1);
                return iDecodeVarint32;
            case 12:
            case 13:
                int iDecodeVarint65 = ArrayDecoders.decodeVarint64(data, position, registers);
                registers.object1 = Long.valueOf(registers.long1);
                return iDecodeVarint65;
            case 14:
                return ArrayDecoders.decodeMessageField(Protobuf.getInstance().schemaFor((Class) messageType), data, position, limit, registers);
            case 15:
                int iDecodeVarint33 = ArrayDecoders.decodeVarint32(data, position, registers);
                registers.object1 = Integer.valueOf(CodedInputStream.decodeZigZag32(registers.int1));
                return iDecodeVarint33;
            case 16:
                int iDecodeVarint66 = ArrayDecoders.decodeVarint64(data, position, registers);
                registers.object1 = Long.valueOf(CodedInputStream.decodeZigZag64(registers.long1));
                return iDecodeVarint66;
            case 17:
                return ArrayDecoders.decodeStringRequireUtf8(data, position, registers);
            default:
                throw new RuntimeException("unsupported field type.");
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private <K, V> int decodeMapEntry(byte[] data, int position, int limit, MapEntryLite.Metadata<K, V> metadata, Map<K, V> target, ArrayDecoders.Registers registers) throws IOException {
        int iDecodeVarint32;
        int iDecodeVarint33 = ArrayDecoders.decodeVarint32(data, position, registers);
        int i = registers.int1;
        if (i < 0 || i > limit - iDecodeVarint33) {
            throw InvalidProtocolBufferException.truncatedMessage();
        }
        int i2 = iDecodeVarint33 + i;
        Object obj = metadata.defaultKey;
        Object obj2 = metadata.defaultValue;
        while (iDecodeVarint33 < i2) {
            int i3 = iDecodeVarint33 + 1;
            int i4 = data[iDecodeVarint33];
            if (i4 < 0) {
                iDecodeVarint32 = ArrayDecoders.decodeVarint32(i4, data, i3, registers);
                i4 = registers.int1;
            } else {
                iDecodeVarint32 = i3;
            }
            int i5 = i4 >>> 3;
            int i6 = i4 & 7;
            if (i5 == 1) {
                if (i6 == metadata.keyType.getWireType()) {
                    iDecodeVarint33 = decodeMapEntryValue(data, iDecodeVarint32, limit, metadata.keyType, null, registers);
                    obj = registers.object1;
                } else {
                    iDecodeVarint33 = ArrayDecoders.skipField(i4, data, iDecodeVarint32, limit, registers);
                }
            } else if (i5 == 2 && i6 == metadata.valueType.getWireType()) {
                iDecodeVarint33 = decodeMapEntryValue(data, iDecodeVarint32, limit, metadata.valueType, metadata.defaultValue.getClass(), registers);
                obj2 = registers.object1;
            } else {
                iDecodeVarint33 = ArrayDecoders.skipField(i4, data, iDecodeVarint32, limit, registers);
            }
        }
        if (iDecodeVarint33 != i2) {
            throw InvalidProtocolBufferException.parseFailure();
        }
        target.put(obj, obj2);
        return i2;
    }

    private int parseRepeatedField(T message, byte[] data, int position, int limit, int tag, int number, int wireType, int bufferPosition, long typeAndOffset, int fieldType, long fieldOffset, ArrayDecoders.Registers registers) throws IOException {
        int iDecodeVarint32List;
        Unsafe unsafe = UNSAFE;
        Internal.ProtobufList protobufListMutableCopyWithCapacity2 = (Internal.ProtobufList) unsafe.getObject(message, fieldOffset);
        if (!protobufListMutableCopyWithCapacity2.isModifiable()) {
            int size = protobufListMutableCopyWithCapacity2.size();
            protobufListMutableCopyWithCapacity2 = protobufListMutableCopyWithCapacity2.mutableCopyWithCapacity2(size == 0 ? 10 : size * 2);
            unsafe.putObject(message, fieldOffset, protobufListMutableCopyWithCapacity2);
        }
        switch (fieldType) {
            case 18:
            case 35:
                if (wireType == 2) {
                    return ArrayDecoders.decodePackedDoubleList(data, position, protobufListMutableCopyWithCapacity2, registers);
                }
                return wireType == 1 ? ArrayDecoders.decodeDoubleList(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 19:
            case 36:
                if (wireType == 2) {
                    return ArrayDecoders.decodePackedFloatList(data, position, protobufListMutableCopyWithCapacity2, registers);
                }
                return wireType == 5 ? ArrayDecoders.decodeFloatList(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 20:
            case 21:
            case 37:
            case 38:
                if (wireType == 2) {
                    return ArrayDecoders.decodePackedVarint64List(data, position, protobufListMutableCopyWithCapacity2, registers);
                }
                return wireType == 0 ? ArrayDecoders.decodeVarint64List(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 22:
            case 29:
            case 39:
            case 43:
                if (wireType == 2) {
                    return ArrayDecoders.decodePackedVarint32List(data, position, protobufListMutableCopyWithCapacity2, registers);
                }
                return wireType == 0 ? ArrayDecoders.decodeVarint32List(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 23:
            case 32:
            case 40:
            case 46:
                if (wireType == 2) {
                    return ArrayDecoders.decodePackedFixed64List(data, position, protobufListMutableCopyWithCapacity2, registers);
                }
                return wireType == 1 ? ArrayDecoders.decodeFixed64List(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 24:
            case 31:
            case 41:
            case 45:
                if (wireType == 2) {
                    return ArrayDecoders.decodePackedFixed32List(data, position, protobufListMutableCopyWithCapacity2, registers);
                }
                return wireType == 5 ? ArrayDecoders.decodeFixed32List(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 25:
            case 42:
                if (wireType == 2) {
                    return ArrayDecoders.decodePackedBoolList(data, position, protobufListMutableCopyWithCapacity2, registers);
                }
                return wireType == 0 ? ArrayDecoders.decodeBoolList(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 26:
                if (wireType != 2) {
                    return position;
                }
                if ((typeAndOffset & 536870912) == 0) {
                    return ArrayDecoders.decodeStringList(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers);
                }
                return ArrayDecoders.decodeStringListRequireUtf8(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers);
            case 27:
                return wireType == 2 ? ArrayDecoders.decodeMessageList(getMessageFieldSchema(bufferPosition), tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 28:
                return wireType == 2 ? ArrayDecoders.decodeBytesList(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 30:
            case 44:
                if (wireType == 2) {
                    iDecodeVarint32List = ArrayDecoders.decodePackedVarint32List(data, position, protobufListMutableCopyWithCapacity2, registers);
                } else {
                    if (wireType != 0) {
                        return position;
                    }
                    iDecodeVarint32List = ArrayDecoders.decodeVarint32List(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers);
                }
                SchemaUtil.filterUnknownEnumList((Object) message, number, (List<Integer>) protobufListMutableCopyWithCapacity2, getEnumFieldVerifier(bufferPosition), (Object) null, (UnknownFieldSchema<UT, Object>) this.unknownFieldSchema);
                return iDecodeVarint32List;
            case 33:
            case 47:
                if (wireType == 2) {
                    return ArrayDecoders.decodePackedSInt32List(data, position, protobufListMutableCopyWithCapacity2, registers);
                }
                return wireType == 0 ? ArrayDecoders.decodeSInt32List(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 34:
            case 48:
                if (wireType == 2) {
                    return ArrayDecoders.decodePackedSInt64List(data, position, protobufListMutableCopyWithCapacity2, registers);
                }
                return wireType == 0 ? ArrayDecoders.decodeSInt64List(tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            case 49:
                return wireType == 3 ? ArrayDecoders.decodeGroupList(getMessageFieldSchema(bufferPosition), tag, data, position, limit, protobufListMutableCopyWithCapacity2, registers) : position;
            default:
                return position;
        }
    }

    private <K, V> int parseMapField(T message, byte[] data, int position, int limit, int bufferPosition, long fieldOffset, ArrayDecoders.Registers registers) throws IOException {
        Unsafe unsafe = UNSAFE;
        Object mapFieldDefaultEntry = getMapFieldDefaultEntry(bufferPosition);
        Object object = unsafe.getObject(message, fieldOffset);
        if (this.mapFieldSchema.isImmutable(object)) {
            Object objNewMapField = this.mapFieldSchema.newMapField(mapFieldDefaultEntry);
            this.mapFieldSchema.mergeFrom(objNewMapField, object);
            unsafe.putObject(message, fieldOffset, objNewMapField);
            object = objNewMapField;
        }
        return decodeMapEntry(data, position, limit, this.mapFieldSchema.forMapMetadata(mapFieldDefaultEntry), this.mapFieldSchema.forMutableMapData(object), registers);
    }

    private int parseOneofField(T message, byte[] data, int position, int limit, int tag, int number, int wireType, int typeAndOffset, int fieldType, long fieldOffset, int bufferPosition, ArrayDecoders.Registers registers) throws IOException {
        int iMergeMessageField;
        Unsafe unsafe = UNSAFE;
        long j = this.buffer[bufferPosition + 2] & 1048575;
        switch (fieldType) {
            case 51:
                if (wireType != 1) {
                    return position;
                }
                unsafe.putObject(message, fieldOffset, Double.valueOf(ArrayDecoders.decodeDouble(data, position)));
                int i = position + 8;
                unsafe.putInt(message, j, number);
                return i;
            case 52:
                if (wireType != 5) {
                    return position;
                }
                unsafe.putObject(message, fieldOffset, Float.valueOf(ArrayDecoders.decodeFloat(data, position)));
                int i2 = position + 4;
                unsafe.putInt(message, j, number);
                return i2;
            case 53:
            case 54:
                if (wireType != 0) {
                    return position;
                }
                int iDecodeVarint64 = ArrayDecoders.decodeVarint64(data, position, registers);
                unsafe.putObject(message, fieldOffset, Long.valueOf(registers.long1));
                unsafe.putInt(message, j, number);
                return iDecodeVarint64;
            case 55:
            case 62:
                if (wireType != 0) {
                    return position;
                }
                int iDecodeVarint32 = ArrayDecoders.decodeVarint32(data, position, registers);
                unsafe.putObject(message, fieldOffset, Integer.valueOf(registers.int1));
                unsafe.putInt(message, j, number);
                return iDecodeVarint32;
            case 56:
            case 65:
                if (wireType != 1) {
                    return position;
                }
                unsafe.putObject(message, fieldOffset, Long.valueOf(ArrayDecoders.decodeFixed64(data, position)));
                int i3 = position + 8;
                unsafe.putInt(message, j, number);
                return i3;
            case 57:
            case 64:
                if (wireType != 5) {
                    return position;
                }
                unsafe.putObject(message, fieldOffset, Integer.valueOf(ArrayDecoders.decodeFixed32(data, position)));
                int i4 = position + 4;
                unsafe.putInt(message, j, number);
                return i4;
            case 58:
                if (wireType != 0) {
                    return position;
                }
                int iDecodeVarint65 = ArrayDecoders.decodeVarint64(data, position, registers);
                unsafe.putObject(message, fieldOffset, Boolean.valueOf(registers.long1 != 0));
                unsafe.putInt(message, j, number);
                return iDecodeVarint65;
            case 59:
                if (wireType != 2) {
                    return position;
                }
                int iDecodeVarint33 = ArrayDecoders.decodeVarint32(data, position, registers);
                int i5 = registers.int1;
                if (i5 == 0) {
                    unsafe.putObject(message, fieldOffset, "");
                } else {
                    if ((typeAndOffset & ENFORCE_UTF8_MASK) != 0 && !Utf8.isValidUtf8(data, iDecodeVarint33, iDecodeVarint33 + i5)) {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    unsafe.putObject(message, fieldOffset, new String(data, iDecodeVarint33, i5, Internal.UTF_8));
                    iDecodeVarint33 += i5;
                }
                unsafe.putInt(message, j, number);
                return iDecodeVarint33;
            case 60:
                if (wireType != 2) {
                    return position;
                }
                Object objMutableOneofMessageFieldForMerge = mutableOneofMessageFieldForMerge(message, number, bufferPosition);
                iMergeMessageField = ArrayDecoders.mergeMessageField(objMutableOneofMessageFieldForMerge, getMessageFieldSchema(bufferPosition), data, position, limit, registers);
                storeOneofMessageField(message, number, bufferPosition, objMutableOneofMessageFieldForMerge);
                break;
                break;
            case 61:
                if (wireType != 2) {
                    return position;
                }
                int iDecodeBytes = ArrayDecoders.decodeBytes(data, position, registers);
                unsafe.putObject(message, fieldOffset, registers.object1);
                unsafe.putInt(message, j, number);
                return iDecodeBytes;
            case 63:
                if (wireType != 0) {
                    return position;
                }
                int iDecodeVarint34 = ArrayDecoders.decodeVarint32(data, position, registers);
                int i6 = registers.int1;
                Internal.EnumVerifier enumFieldVerifier = getEnumFieldVerifier(bufferPosition);
                if (enumFieldVerifier == null || enumFieldVerifier.isInRange(i6)) {
                    unsafe.putObject(message, fieldOffset, Integer.valueOf(i6));
                    unsafe.putInt(message, j, number);
                } else {
                    getMutableUnknownFields(message).storeField(tag, Long.valueOf(i6));
                }
                return iDecodeVarint34;
            case 66:
                if (wireType != 0) {
                    return position;
                }
                int iDecodeVarint35 = ArrayDecoders.decodeVarint32(data, position, registers);
                unsafe.putObject(message, fieldOffset, Integer.valueOf(CodedInputStream.decodeZigZag32(registers.int1)));
                unsafe.putInt(message, j, number);
                return iDecodeVarint35;
            case 67:
                if (wireType != 0) {
                    return position;
                }
                int iDecodeVarint66 = ArrayDecoders.decodeVarint64(data, position, registers);
                unsafe.putObject(message, fieldOffset, Long.valueOf(CodedInputStream.decodeZigZag64(registers.long1)));
                unsafe.putInt(message, j, number);
                return iDecodeVarint66;
            case 68:
                if (wireType != 3) {
                    return position;
                }
                Object objMutableOneofMessageFieldForMerge2 = mutableOneofMessageFieldForMerge(message, number, bufferPosition);
                iMergeMessageField = ArrayDecoders.mergeGroupField(objMutableOneofMessageFieldForMerge2, getMessageFieldSchema(bufferPosition), data, position, limit, (tag & (-8)) | 4, registers);
                storeOneofMessageField(message, number, bufferPosition, objMutableOneofMessageFieldForMerge2);
                break;
                break;
            default:
                return position;
        }
        return iMergeMessageField;
    }

    private Schema getMessageFieldSchema(int pos) {
        int i = (pos / 3) * 2;
        Schema schema = (Schema) this.objects[i];
        if (schema != null) {
            return schema;
        }
        Schema<T> schemaSchemaFor = Protobuf.getInstance().schemaFor((Class) this.objects[i + 1]);
        this.objects[i] = schemaSchemaFor;
        return schemaSchemaFor;
    }

    private Object getMapFieldDefaultEntry(int pos) {
        return this.objects[(pos / 3) * 2];
    }

    private Internal.EnumVerifier getEnumFieldVerifier(int pos) {
        return (Internal.EnumVerifier) this.objects[((pos / 3) * 2) + 1];
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:27:0x0099. Please report as an issue. */
    int parseMessage(T message, byte[] data, int position, int limit, int endDelimited, ArrayDecoders.Registers registers) throws IOException {
        Unsafe unsafe;
        MessageSchema<T> messageSchema;
        int i;
        int i2;
        int i3;
        int i4;
        T t;
        int i5;
        int iPositionForFieldNumber;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        byte[] bArr;
        int i12;
        int i13;
        byte b;
        byte[] bArr2;
        int iDecodeVarint64;
        int i14;
        int i15;
        MessageSchema<T> messageSchema2 = this;
        T t2 = message;
        byte[] bArr3 = data;
        limit = limit;
        endDelimited = endDelimited;
        ArrayDecoders.Registers registers2 = registers;
        checkMutable(message);
        Unsafe unsafe2 = UNSAFE;
        int iDecodeUnknownField = position;
        int i16 = 0;
        int i17 = 0;
        int i18 = 0;
        int i19 = -1;
        int i20 = 1048575;
        while (true) {
            if (iDecodeUnknownField < limit) {
                int i21 = iDecodeUnknownField + 1;
                byte b2 = bArr3[iDecodeUnknownField];
                if (b2 < 0) {
                    int iDecodeVarint32 = ArrayDecoders.decodeVarint32(b2, bArr3, i21, registers2);
                    i5 = registers2.int1;
                    i21 = iDecodeVarint32;
                } else {
                    i5 = b2;
                }
                int i22 = i5 >>> 3;
                int i23 = i5 & 7;
                if (i22 > i19) {
                    iPositionForFieldNumber = messageSchema2.positionForFieldNumber(i22, i16 / 3);
                } else {
                    iPositionForFieldNumber = messageSchema2.positionForFieldNumber(i22);
                }
                int i24 = iPositionForFieldNumber;
                if (i24 == -1) {
                    i6 = i22;
                    i7 = i21;
                    i2 = i5;
                    i8 = i18;
                    i9 = i20;
                    unsafe = unsafe2;
                    endDelimited = endDelimited;
                    i10 = 0;
                } else {
                    int i25 = messageSchema2.buffer[i24 + 1];
                    int iType = type(i25);
                    long jOffset = offset(i25);
                    int i26 = i5;
                    if (iType <= 17) {
                        int i27 = messageSchema2.buffer[i24 + 2];
                        int i28 = 1 << (i27 >>> 20);
                        int i29 = 1048575;
                        int i30 = i27 & 1048575;
                        if (i30 != i20) {
                            if (i20 != 1048575) {
                                unsafe2.putInt(t2, i20, i18);
                                i29 = 1048575;
                            }
                            i9 = i30;
                            i11 = i30 == i29 ? 0 : unsafe2.getInt(t2, i30);
                        } else {
                            i11 = i18;
                            i9 = i20;
                        }
                        switch (iType) {
                            case 0:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 1) {
                                    UnsafeUtil.putDouble(t2, jOffset, ArrayDecoders.decodeDouble(bArr, i21));
                                    iDecodeUnknownField = i21 + 8;
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 1:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 5) {
                                    UnsafeUtil.putFloat(t2, jOffset, ArrayDecoders.decodeFloat(bArr, i21));
                                    iDecodeUnknownField = i21 + 4;
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 2:
                            case 3:
                                bArr2 = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 0) {
                                    iDecodeVarint64 = ArrayDecoders.decodeVarint64(bArr2, i21, registers2);
                                    unsafe2.putLong(message, jOffset, registers2.long1);
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr2;
                                    iDecodeUnknownField = iDecodeVarint64;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 4:
                            case 11:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 0) {
                                    iDecodeUnknownField = ArrayDecoders.decodeVarint32(bArr, i21, registers2);
                                    unsafe2.putInt(t2, jOffset, registers2.int1);
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 5:
                            case 14:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 1) {
                                    unsafe2.putLong(message, jOffset, ArrayDecoders.decodeFixed64(bArr, i21));
                                    iDecodeUnknownField = i21 + 8;
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 6:
                            case 13:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 5) {
                                    unsafe2.putInt(t2, jOffset, ArrayDecoders.decodeFixed32(bArr, i21));
                                    iDecodeUnknownField = i21 + 4;
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 7:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 0) {
                                    iDecodeUnknownField = ArrayDecoders.decodeVarint64(bArr, i21, registers2);
                                    UnsafeUtil.putBoolean(t2, jOffset, registers2.long1 != 0);
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 8:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 2) {
                                    if (isEnforceUtf8(i25)) {
                                        iDecodeUnknownField = ArrayDecoders.decodeStringRequireUtf8(bArr, i21, registers2);
                                    } else {
                                        iDecodeUnknownField = ArrayDecoders.decodeString(bArr, i21, registers2);
                                    }
                                    unsafe2.putObject(t2, jOffset, registers2.object1);
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 9:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 2) {
                                    Object objMutableMessageFieldForMerge = messageSchema2.mutableMessageFieldForMerge(t2, i12);
                                    iDecodeUnknownField = ArrayDecoders.mergeMessageField(objMutableMessageFieldForMerge, messageSchema2.getMessageFieldSchema(i12), data, i21, limit, registers);
                                    messageSchema2.storeMessageField(t2, i12, objMutableMessageFieldForMerge);
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 10:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 2) {
                                    iDecodeUnknownField = ArrayDecoders.decodeBytes(bArr, i21, registers2);
                                    unsafe2.putObject(t2, jOffset, registers2.object1);
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 12:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 0) {
                                    iDecodeUnknownField = ArrayDecoders.decodeVarint32(bArr, i21, registers2);
                                    int i31 = registers2.int1;
                                    Internal.EnumVerifier enumFieldVerifier = messageSchema2.getEnumFieldVerifier(i12);
                                    if (!isLegacyEnumIsClosed(i25) || enumFieldVerifier == null || enumFieldVerifier.isInRange(i31)) {
                                        unsafe2.putInt(t2, jOffset, i31);
                                        i18 = i11 | i28;
                                        i16 = i12;
                                        bArr3 = bArr;
                                        i17 = i13;
                                        i20 = i9;
                                        i19 = i6;
                                    } else {
                                        getMutableUnknownFields(message).storeField(i13, Long.valueOf(i31));
                                        endDelimited = endDelimited;
                                        i16 = i12;
                                        i18 = i11;
                                        i17 = i13;
                                        i20 = i9;
                                        i19 = i6;
                                        bArr3 = bArr;
                                    }
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 15:
                                bArr = data;
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                if (i23 == 0) {
                                    iDecodeUnknownField = ArrayDecoders.decodeVarint32(bArr, i21, registers2);
                                    unsafe2.putInt(t2, jOffset, CodedInputStream.decodeZigZag32(registers2.int1));
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 16:
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                bArr2 = data;
                                if (i23 == 0) {
                                    iDecodeVarint64 = ArrayDecoders.decodeVarint64(bArr2, i21, registers2);
                                    unsafe2.putLong(message, jOffset, CodedInputStream.decodeZigZag64(registers2.long1));
                                    i18 = i11 | i28;
                                    i16 = i12;
                                    bArr3 = bArr2;
                                    iDecodeUnknownField = iDecodeVarint64;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            case 17:
                                if (i23 == 3) {
                                    Object objMutableMessageFieldForMerge2 = messageSchema2.mutableMessageFieldForMerge(t2, i24);
                                    i6 = i22;
                                    i13 = i26;
                                    iDecodeUnknownField = ArrayDecoders.mergeGroupField(objMutableMessageFieldForMerge2, messageSchema2.getMessageFieldSchema(i24), data, i21, limit, (i22 << 3) | 4, registers);
                                    messageSchema2.storeMessageField(t2, i24, objMutableMessageFieldForMerge2);
                                    i18 = i11 | i28;
                                    bArr3 = data;
                                    i16 = i24;
                                    i17 = i13;
                                    i20 = i9;
                                    i19 = i6;
                                } else {
                                    i6 = i22;
                                    i13 = i26;
                                    b = -1;
                                    i12 = i24;
                                    endDelimited = endDelimited;
                                    i7 = i21;
                                    i10 = i12;
                                    unsafe = unsafe2;
                                    i8 = i11;
                                    i2 = i13;
                                }
                                break;
                            default:
                                i6 = i22;
                                i12 = i24;
                                i13 = i26;
                                b = -1;
                                endDelimited = endDelimited;
                                i7 = i21;
                                i10 = i12;
                                unsafe = unsafe2;
                                i8 = i11;
                                i2 = i13;
                                break;
                        }
                    } else {
                        i6 = i22;
                        int i32 = i20;
                        i8 = i18;
                        if (iType != 27) {
                            i9 = i32;
                            if (iType <= 49) {
                                int i33 = i21;
                                unsafe = unsafe2;
                                i10 = i24;
                                i15 = i26;
                                iDecodeUnknownField = parseRepeatedField(message, data, i21, limit, i26, i6, i23, i24, i25, iType, jOffset, registers);
                                if (iDecodeUnknownField != i33) {
                                    messageSchema2 = this;
                                    t2 = message;
                                    bArr3 = data;
                                    endDelimited = endDelimited;
                                    registers2 = registers;
                                    i17 = i15;
                                    i18 = i8;
                                    i16 = i10;
                                    i20 = i9;
                                    i19 = i6;
                                    unsafe2 = unsafe;
                                } else {
                                    i7 = iDecodeUnknownField;
                                    i2 = i15;
                                }
                            } else {
                                i14 = i21;
                                unsafe = unsafe2;
                                i10 = i24;
                                i15 = i26;
                                if (iType != 50) {
                                    iDecodeUnknownField = parseOneofField(message, data, i14, limit, i15, i6, i23, i25, iType, jOffset, i10, registers);
                                    if (iDecodeUnknownField != i14) {
                                        messageSchema2 = this;
                                        t2 = message;
                                        bArr3 = data;
                                        endDelimited = endDelimited;
                                        registers2 = registers;
                                        i17 = i15;
                                        i18 = i8;
                                        i16 = i10;
                                        i20 = i9;
                                        i19 = i6;
                                        unsafe2 = unsafe;
                                    } else {
                                        i7 = iDecodeUnknownField;
                                        i2 = i15;
                                    }
                                } else if (i23 == 2) {
                                    iDecodeUnknownField = parseMapField(message, data, i14, limit, i10, jOffset, registers);
                                    if (iDecodeUnknownField != i14) {
                                        messageSchema2 = this;
                                        t2 = message;
                                        bArr3 = data;
                                        endDelimited = endDelimited;
                                        registers2 = registers;
                                        i17 = i15;
                                        i18 = i8;
                                        i16 = i10;
                                        i20 = i9;
                                        i19 = i6;
                                        unsafe2 = unsafe;
                                    } else {
                                        i7 = iDecodeUnknownField;
                                        i2 = i15;
                                    }
                                }
                            }
                        } else if (i23 == 2) {
                            Internal.ProtobufList protobufListMutableCopyWithCapacity2 = (Internal.ProtobufList) unsafe2.getObject(t2, jOffset);
                            if (!protobufListMutableCopyWithCapacity2.isModifiable()) {
                                int size = protobufListMutableCopyWithCapacity2.size();
                                protobufListMutableCopyWithCapacity2 = protobufListMutableCopyWithCapacity2.mutableCopyWithCapacity2(size == 0 ? 10 : size * 2);
                                unsafe2.putObject(t2, jOffset, protobufListMutableCopyWithCapacity2);
                            }
                            iDecodeUnknownField = ArrayDecoders.decodeMessageList(messageSchema2.getMessageFieldSchema(i24), i26, data, i21, limit, protobufListMutableCopyWithCapacity2, registers);
                            endDelimited = endDelimited;
                            i16 = i24;
                            i17 = i26;
                            i18 = i8;
                            i20 = i32;
                            i19 = i6;
                            bArr3 = data;
                        } else {
                            i9 = i32;
                            i14 = i21;
                            unsafe = unsafe2;
                            i10 = i24;
                            i15 = i26;
                        }
                        i7 = i14;
                        i2 = i15;
                    }
                }
                if (i2 != endDelimited || endDelimited == 0) {
                    if (this.hasExtensions && registers.extensionRegistry != ExtensionRegistryLite.getEmptyRegistry()) {
                        iDecodeUnknownField = ArrayDecoders.decodeExtensionOrUnknownField(i2, data, i7, limit, message, this.defaultInstance, this.unknownFieldSchema, registers);
                    } else {
                        iDecodeUnknownField = ArrayDecoders.decodeUnknownField(i2, data, i7, limit, getMutableUnknownFields(message), registers);
                    }
                    t2 = message;
                    bArr3 = data;
                    i17 = i2;
                    messageSchema2 = this;
                    registers2 = registers;
                    i18 = i8;
                    i16 = i10;
                    i20 = i9;
                    i19 = i6;
                    unsafe2 = unsafe;
                    endDelimited = endDelimited;
                } else {
                    i4 = 1048575;
                    messageSchema = this;
                    i = i7;
                    i18 = i8;
                    i3 = i9;
                }
            } else {
                int i34 = i20;
                unsafe = unsafe2;
                endDelimited = endDelimited;
                messageSchema = messageSchema2;
                i = iDecodeUnknownField;
                i2 = i17;
                i3 = i34;
                i4 = 1048575;
            }
        }
        if (i3 != i4) {
            t = message;
            unsafe.putInt(t, i3, i18);
        } else {
            t = message;
        }
        UnknownFieldSetLite unknownFieldSetLite = null;
        for (int i35 = messageSchema.checkInitializedCount; i35 < messageSchema.repeatedFieldOffsetStart; i35++) {
            unknownFieldSetLite = (UnknownFieldSetLite) filterMapUnknownEnumValues(message, messageSchema.intArray[i35], unknownFieldSetLite, messageSchema.unknownFieldSchema, message);
        }
        if (unknownFieldSetLite != null) {
            messageSchema.unknownFieldSchema.setBuilderToMessage(t, unknownFieldSetLite);
        }
        if (endDelimited == 0) {
            if (i != limit) {
                throw InvalidProtocolBufferException.parseFailure();
            }
        } else if (i > limit || i2 != endDelimited) {
            throw InvalidProtocolBufferException.parseFailure();
        }
        return i;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private Object mutableMessageFieldForMerge(T message, int pos) {
        Schema messageFieldSchema = getMessageFieldSchema(pos);
        long jOffset = offset(typeAndOffsetAt(pos));
        if (!isFieldPresent(message, pos)) {
            return messageFieldSchema.newInstance();
        }
        Object object = UNSAFE.getObject(message, jOffset);
        if (isMutable(object)) {
            return object;
        }
        Object objNewInstance = messageFieldSchema.newInstance();
        if (object != null) {
            messageFieldSchema.mergeFrom(objNewInstance, object);
        }
        return objNewInstance;
    }

    private void storeMessageField(T message, int pos, Object field) {
        UNSAFE.putObject(message, offset(typeAndOffsetAt(pos)), field);
        setFieldPresent(message, pos);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private Object mutableOneofMessageFieldForMerge(T message, int fieldNumber, int pos) {
        Schema messageFieldSchema = getMessageFieldSchema(pos);
        if (!isOneofPresent(message, fieldNumber, pos)) {
            return messageFieldSchema.newInstance();
        }
        Object object = UNSAFE.getObject(message, offset(typeAndOffsetAt(pos)));
        if (isMutable(object)) {
            return object;
        }
        Object objNewInstance = messageFieldSchema.newInstance();
        if (object != null) {
            messageFieldSchema.mergeFrom(objNewInstance, object);
        }
        return objNewInstance;
    }

    private void storeOneofMessageField(T message, int fieldNumber, int pos, Object field) {
        UNSAFE.putObject(message, offset(typeAndOffsetAt(pos)), field);
        setOneofPresent(message, fieldNumber, pos);
    }

    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public void mergeFrom(T message, byte[] data, int position, int limit, ArrayDecoders.Registers registers) throws IOException {
        parseMessage(message, data, position, limit, 0, registers);
    }

    /* JADX WARN: Code duplicated, block: B:25:0x0069  */
    /* JADX WARN: Code duplicated, block: B:27:0x006f  */
    /* JADX WARN: Code duplicated, block: B:40:0x007c A[SYNTHETIC] */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public void makeImmutable(T message) {
        if (isMutable(message)) {
            if (message instanceof GeneratedMessageLite) {
                GeneratedMessageLite generatedMessageLite = (GeneratedMessageLite) message;
                generatedMessageLite.clearMemoizedSerializedSize();
                generatedMessageLite.clearMemoizedHashCode();
                generatedMessageLite.markImmutable();
            }
            int length = this.buffer.length;
            for (int i = 0; i < length; i += 3) {
                int iTypeAndOffsetAt = typeAndOffsetAt(i);
                long jOffset = offset(iTypeAndOffsetAt);
                int iType = type(iTypeAndOffsetAt);
                if (iType != 9) {
                    if (iType != 60 && iType != 68) {
                        switch (iType) {
                            case 17:
                                if (isFieldPresent(message, i)) {
                                    getMessageFieldSchema(i).makeImmutable(UNSAFE.getObject(message, jOffset));
                                }
                                break;
                            case 18:
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 27:
                            case 28:
                            case 29:
                            case 30:
                            case 31:
                            case 32:
                            case 33:
                            case 34:
                            case 35:
                            case 36:
                            case 37:
                            case 38:
                            case 39:
                            case 40:
                            case 41:
                            case 42:
                            case 43:
                            case 44:
                            case 45:
                            case 46:
                            case 47:
                            case 48:
                            case 49:
                                this.listFieldSchema.makeImmutableListAt(message, jOffset);
                                break;
                            case 50:
                                Unsafe unsafe = UNSAFE;
                                Object object = unsafe.getObject(message, jOffset);
                                if (object != null) {
                                    unsafe.putObject(message, jOffset, this.mapFieldSchema.toImmutable(object));
                                }
                                break;
                        }
                    } else if (isOneofPresent(message, numberAt(i), i)) {
                        getMessageFieldSchema(i).makeImmutable(UNSAFE.getObject(message, jOffset));
                    }
                } else if (isFieldPresent(message, i)) {
                    getMessageFieldSchema(i).makeImmutable(UNSAFE.getObject(message, jOffset));
                }
            }
            this.unknownFieldSchema.makeImmutable(message);
            if (this.hasExtensions) {
                this.extensionSchema.makeImmutable(message);
            }
        }
    }

    private final <K, V> void mergeMap(Object message, int pos, Object mapDefaultEntry, ExtensionRegistryLite extensionRegistry, Reader reader) throws IOException {
        long jOffset = offset(typeAndOffsetAt(pos));
        Object object = UnsafeUtil.getObject(message, jOffset);
        if (object == null) {
            object = this.mapFieldSchema.newMapField(mapDefaultEntry);
            UnsafeUtil.putObject(message, jOffset, object);
        } else if (this.mapFieldSchema.isImmutable(object)) {
            Object objNewMapField = this.mapFieldSchema.newMapField(mapDefaultEntry);
            this.mapFieldSchema.mergeFrom(objNewMapField, object);
            UnsafeUtil.putObject(message, jOffset, objNewMapField);
            object = objNewMapField;
        }
        reader.readMap(this.mapFieldSchema.forMutableMapData(object), this.mapFieldSchema.forMapMetadata(mapDefaultEntry), extensionRegistry);
    }

    private <UT, UB> UB filterMapUnknownEnumValues(Object obj, int i, UB ub, UnknownFieldSchema<UT, UB> unknownFieldSchema, Object obj2) {
        Internal.EnumVerifier enumFieldVerifier;
        int iNumberAt = numberAt(i);
        Object object = UnsafeUtil.getObject(obj, offset(typeAndOffsetAt(i)));
        return (object == null || (enumFieldVerifier = getEnumFieldVerifier(i)) == null) ? ub : (UB) filterUnknownEnumMap(i, iNumberAt, this.mapFieldSchema.forMutableMapData(object), enumFieldVerifier, ub, unknownFieldSchema, obj2);
    }

    private <K, V, UT, UB> UB filterUnknownEnumMap(int i, int i2, Map<K, V> map, Internal.EnumVerifier enumVerifier, UB ub, UnknownFieldSchema<UT, UB> unknownFieldSchema, Object obj) {
        MapEntryLite.Metadata<?, ?> metadataForMapMetadata = this.mapFieldSchema.forMapMetadata(getMapFieldDefaultEntry(i));
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> next = it.next();
            if (!enumVerifier.isInRange(((Integer) next.getValue()).intValue())) {
                if (ub == null) {
                    ub = unknownFieldSchema.getBuilderFromMessage(obj);
                }
                ByteString.CodedBuilder codedBuilderNewCodedBuilder = ByteString.newCodedBuilder(MapEntryLite.computeSerializedSize(metadataForMapMetadata, next.getKey(), next.getValue()));
                try {
                    MapEntryLite.writeTo(codedBuilderNewCodedBuilder.getCodedOutput(), metadataForMapMetadata, next.getKey(), next.getValue());
                    unknownFieldSchema.addLengthDelimited(ub, i2, codedBuilderNewCodedBuilder.build());
                    it.remove();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return ub;
    }

    /* JADX WARN: Code duplicated, block: B:39:0x0089  */
    /* JADX WARN: Code duplicated, block: B:58:0x008f A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:63:0x00aa A[SYNTHETIC] */
    @Override // com.google.crypto.tink.shaded.protobuf.Schema
    public final boolean isInitialized(T message) {
        int i;
        int i2;
        int i3 = 1048575;
        int i4 = 0;
        int i5 = 0;
        while (i5 < this.checkInitializedCount) {
            int i6 = this.intArray[i5];
            int iNumberAt = numberAt(i6);
            int iTypeAndOffsetAt = typeAndOffsetAt(i6);
            int i7 = this.buffer[i6 + 2];
            int i8 = i7 & 1048575;
            int i9 = 1 << (i7 >>> 20);
            if (i8 != i3) {
                if (i8 != 1048575) {
                    i4 = UNSAFE.getInt(message, i8);
                }
                i2 = i4;
                i = i8;
            } else {
                i = i3;
                i2 = i4;
            }
            if (isRequired(iTypeAndOffsetAt) && !isFieldPresent(message, i6, i, i2, i9)) {
                return false;
            }
            int iType = type(iTypeAndOffsetAt);
            if (iType == 9 || iType == 17) {
                if (isFieldPresent(message, i6, i, i2, i9) && !isInitialized(message, iTypeAndOffsetAt, getMessageFieldSchema(i6))) {
                    return false;
                }
            } else if (iType == 27) {
                if (!isListInitialized(message, iTypeAndOffsetAt, i6)) {
                    return false;
                }
            } else if (iType == 60 || iType == 68) {
                if (isOneofPresent(message, iNumberAt, i6) && !isInitialized(message, iTypeAndOffsetAt, getMessageFieldSchema(i6))) {
                    return false;
                }
            } else if (iType == 49) {
                if (!isListInitialized(message, iTypeAndOffsetAt, i6)) {
                    return false;
                }
            } else if (iType == 50 && !isMapInitialized(message, iTypeAndOffsetAt, i6)) {
                return false;
            }
            i5++;
            i3 = i;
            i4 = i2;
        }
        return !this.hasExtensions || this.extensionSchema.getExtensions(message).isInitialized();
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static boolean isInitialized(Object message, int typeAndOffset, Schema schema) {
        return schema.isInitialized(UnsafeUtil.getObject(message, offset(typeAndOffset)));
    }

    /* JADX WARN: Multi-variable type inference failed */
    private <N> boolean isListInitialized(Object message, int typeAndOffset, int pos) {
        List list = (List) UnsafeUtil.getObject(message, offset(typeAndOffset));
        if (list.isEmpty()) {
            return true;
        }
        Schema messageFieldSchema = getMessageFieldSchema(pos);
        for (int i = 0; i < list.size(); i++) {
            if (!messageFieldSchema.isInitialized(list.get(i))) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v3 */
    /* JADX WARN: Type inference failed for: r4v4 */
    /* JADX WARN: Type inference failed for: r4v5, types: [com.google.crypto.tink.shaded.protobuf.Schema] */
    /* JADX WARN: Type inference failed for: r4v8 */
    /* JADX WARN: Type inference failed for: r4v9 */
    private boolean isMapInitialized(T t, int i, int i2) {
        Map<?, ?> mapForMapData = this.mapFieldSchema.forMapData(UnsafeUtil.getObject(t, offset(i)));
        if (mapForMapData.isEmpty()) {
            return true;
        }
        if (this.mapFieldSchema.forMapMetadata(getMapFieldDefaultEntry(i2)).valueType.getJavaType() != WireFormat.JavaType.MESSAGE) {
            return true;
        }
        ?? SchemaFor = 0;
        for (Object obj : mapForMapData.values()) {
            if (SchemaFor == 0) {
                SchemaFor = SchemaFor;
                SchemaFor = Protobuf.getInstance().schemaFor((Class) obj.getClass());
            }
            SchemaFor = SchemaFor;
            if (!SchemaFor.isInitialized(obj)) {
                return false;
            }
        }
        return true;
    }

    private void writeString(int fieldNumber, Object value, Writer writer) throws IOException {
        if (value instanceof String) {
            writer.writeString(fieldNumber, (String) value);
        } else {
            writer.writeBytes(fieldNumber, (ByteString) value);
        }
    }

    private void readString(Object message, int typeAndOffset, Reader reader) throws IOException {
        if (isEnforceUtf8(typeAndOffset)) {
            UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readStringRequireUtf8());
        } else if (this.lite) {
            UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readString());
        } else {
            UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readBytes());
        }
    }

    private void readStringList(Object message, int typeAndOffset, Reader reader) throws IOException {
        if (isEnforceUtf8(typeAndOffset)) {
            reader.readStringListRequireUtf8(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
        } else {
            reader.readStringList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
        }
    }

    private <E> void readMessageList(Object message, int typeAndOffset, Reader reader, Schema<E> schema, ExtensionRegistryLite extensionRegistry) throws IOException {
        reader.readMessageList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)), schema, extensionRegistry);
    }

    private <E> void readGroupList(Object message, long offset, Reader reader, Schema<E> schema, ExtensionRegistryLite extensionRegistry) throws IOException {
        reader.readGroupList(this.listFieldSchema.mutableListAt(message, offset), schema, extensionRegistry);
    }

    private int numberAt(int pos) {
        return this.buffer[pos];
    }

    private int typeAndOffsetAt(int pos) {
        return this.buffer[pos + 1];
    }

    private int presenceMaskAndOffsetAt(int pos) {
        return this.buffer[pos + 2];
    }

    private static boolean isMutable(Object message) {
        if (message == null) {
            return false;
        }
        if (message instanceof GeneratedMessageLite) {
            return ((GeneratedMessageLite) message).isMutable();
        }
        return true;
    }

    private static void checkMutable(Object message) {
        if (isMutable(message)) {
            return;
        }
        throw new IllegalArgumentException("Mutating immutable message: " + message);
    }

    private static <T> double doubleAt(T message, long offset) {
        return UnsafeUtil.getDouble(message, offset);
    }

    private static <T> float floatAt(T message, long offset) {
        return UnsafeUtil.getFloat(message, offset);
    }

    private static <T> int intAt(T message, long offset) {
        return UnsafeUtil.getInt(message, offset);
    }

    private static <T> long longAt(T message, long offset) {
        return UnsafeUtil.getLong(message, offset);
    }

    private static <T> boolean booleanAt(T message, long offset) {
        return UnsafeUtil.getBoolean(message, offset);
    }

    private static <T> double oneofDoubleAt(T message, long offset) {
        return ((Double) UnsafeUtil.getObject(message, offset)).doubleValue();
    }

    private static <T> float oneofFloatAt(T message, long offset) {
        return ((Float) UnsafeUtil.getObject(message, offset)).floatValue();
    }

    private static <T> int oneofIntAt(T message, long offset) {
        return ((Integer) UnsafeUtil.getObject(message, offset)).intValue();
    }

    private static <T> long oneofLongAt(T message, long offset) {
        return ((Long) UnsafeUtil.getObject(message, offset)).longValue();
    }

    private static <T> boolean oneofBooleanAt(T message, long offset) {
        return ((Boolean) UnsafeUtil.getObject(message, offset)).booleanValue();
    }

    private boolean arePresentForEquals(T message, T other, int pos) {
        return isFieldPresent(message, pos) == isFieldPresent(other, pos);
    }

    private boolean isFieldPresent(T message, int pos, int presenceFieldOffset, int presenceField, int presenceMask) {
        if (presenceFieldOffset == 1048575) {
            return isFieldPresent(message, pos);
        }
        return (presenceField & presenceMask) != 0;
    }

    private boolean isFieldPresent(T message, int pos) {
        int iPresenceMaskAndOffsetAt = presenceMaskAndOffsetAt(pos);
        long j = 1048575 & iPresenceMaskAndOffsetAt;
        if (j != 1048575) {
            return ((1 << (iPresenceMaskAndOffsetAt >>> 20)) & UnsafeUtil.getInt(message, j)) != 0;
        }
        int iTypeAndOffsetAt = typeAndOffsetAt(pos);
        long jOffset = offset(iTypeAndOffsetAt);
        switch (type(iTypeAndOffsetAt)) {
            case 0:
                return Double.doubleToRawLongBits(UnsafeUtil.getDouble(message, jOffset)) != 0;
            case 1:
                return Float.floatToRawIntBits(UnsafeUtil.getFloat(message, jOffset)) != 0;
            case 2:
                return UnsafeUtil.getLong(message, jOffset) != 0;
            case 3:
                return UnsafeUtil.getLong(message, jOffset) != 0;
            case 4:
                return UnsafeUtil.getInt(message, jOffset) != 0;
            case 5:
                return UnsafeUtil.getLong(message, jOffset) != 0;
            case 6:
                return UnsafeUtil.getInt(message, jOffset) != 0;
            case 7:
                return UnsafeUtil.getBoolean(message, jOffset);
            case 8:
                Object object = UnsafeUtil.getObject(message, jOffset);
                if (object instanceof String) {
                    return !((String) object).isEmpty();
                }
                if (object instanceof ByteString) {
                    return !ByteString.EMPTY.equals(object);
                }
                throw new IllegalArgumentException();
            case 9:
                return UnsafeUtil.getObject(message, jOffset) != null;
            case 10:
                return !ByteString.EMPTY.equals(UnsafeUtil.getObject(message, jOffset));
            case 11:
                return UnsafeUtil.getInt(message, jOffset) != 0;
            case 12:
                return UnsafeUtil.getInt(message, jOffset) != 0;
            case 13:
                return UnsafeUtil.getInt(message, jOffset) != 0;
            case 14:
                return UnsafeUtil.getLong(message, jOffset) != 0;
            case 15:
                return UnsafeUtil.getInt(message, jOffset) != 0;
            case 16:
                return UnsafeUtil.getLong(message, jOffset) != 0;
            case 17:
                return UnsafeUtil.getObject(message, jOffset) != null;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void setFieldPresent(T message, int pos) {
        int iPresenceMaskAndOffsetAt = presenceMaskAndOffsetAt(pos);
        long j = 1048575 & iPresenceMaskAndOffsetAt;
        if (j == 1048575) {
            return;
        }
        UnsafeUtil.putInt(message, j, (1 << (iPresenceMaskAndOffsetAt >>> 20)) | UnsafeUtil.getInt(message, j));
    }

    private boolean isOneofPresent(T message, int fieldNumber, int pos) {
        return UnsafeUtil.getInt(message, (long) (presenceMaskAndOffsetAt(pos) & 1048575)) == fieldNumber;
    }

    private boolean isOneofCaseEqual(T message, T other, int pos) {
        long jPresenceMaskAndOffsetAt = presenceMaskAndOffsetAt(pos) & 1048575;
        return UnsafeUtil.getInt(message, jPresenceMaskAndOffsetAt) == UnsafeUtil.getInt(other, jPresenceMaskAndOffsetAt);
    }

    private void setOneofPresent(T message, int fieldNumber, int pos) {
        UnsafeUtil.putInt(message, presenceMaskAndOffsetAt(pos) & 1048575, fieldNumber);
    }

    private int positionForFieldNumber(final int number) {
        if (number < this.minFieldNumber || number > this.maxFieldNumber) {
            return -1;
        }
        return slowPositionForFieldNumber(number, 0);
    }

    private int positionForFieldNumber(final int number, final int min) {
        if (number < this.minFieldNumber || number > this.maxFieldNumber) {
            return -1;
        }
        return slowPositionForFieldNumber(number, min);
    }

    private int slowPositionForFieldNumber(final int number, int min) {
        int length = (this.buffer.length / 3) - 1;
        while (min <= length) {
            int i = (length + min) >>> 1;
            int i2 = i * 3;
            int iNumberAt = numberAt(i2);
            if (number == iNumberAt) {
                return i2;
            }
            if (number < iNumberAt) {
                length = i - 1;
            } else {
                min = i + 1;
            }
        }
        return -1;
    }

    int getSchemaSize() {
        return this.buffer.length * 3;
    }
}
