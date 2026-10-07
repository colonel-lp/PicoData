package com.google.crypto.tink.shaded.protobuf;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import kotlin.text.Typography;

/* JADX INFO: loaded from: classes2.dex */
final class MessageLiteToString {
    private static final String BUILDER_LIST_SUFFIX = "OrBuilderList";
    private static final String BYTES_SUFFIX = "Bytes";
    private static final char[] INDENT_BUFFER;
    private static final String LIST_SUFFIX = "List";
    private static final String MAP_SUFFIX = "Map";

    static {
        char[] cArr = new char[80];
        INDENT_BUFFER = cArr;
        Arrays.fill(cArr, ' ');
    }

    private MessageLiteToString() {
    }

    static String toString(MessageLite messageLite, String commentString) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ");
        sb.append(commentString);
        reflectivePrintWithIndent(messageLite, sb, 0);
        return sb.toString();
    }

    /* JADX WARN: Code duplicated, block: B:62:0x016c  */
    /* JADX WARN: Code duplicated, block: B:64:0x0186  */
    /* JADX WARN: Code duplicated, block: B:66:0x018e  */
    /* JADX WARN: Code duplicated, block: B:68:0x0194  */
    /* JADX WARN: Code duplicated, block: B:69:0x0196  */
    /* JADX WARN: Code duplicated, block: B:70:0x0198  */
    /* JADX WARN: Code duplicated, block: B:72:0x01a6  */
    /* JADX WARN: Instruction removed from duplicated block: B:62:0x016c, please report this as an issue */
    private static void reflectivePrintWithIndent(MessageLite messageLite, StringBuilder buffer, int indent) {
        int i;
        int i2;
        java.lang.reflect.Method method;
        java.lang.reflect.Method method2;
        Object objInvokeOrDie;
        boolean zBooleanValue;
        java.lang.reflect.Method method3;
        java.lang.reflect.Method method4;
        HashSet hashSet = new HashSet();
        HashMap map = new HashMap();
        TreeMap treeMap = new TreeMap();
        java.lang.reflect.Method[] declaredMethods = messageLite.getClass().getDeclaredMethods();
        int length = declaredMethods.length;
        int i3 = 0;
        while (true) {
            i = 3;
            if (i3 >= length) {
                break;
            }
            java.lang.reflect.Method method5 = declaredMethods[i3];
            if (!Modifier.isStatic(method5.getModifiers()) && method5.getName().length() >= 3) {
                if (method5.getName().startsWith("set")) {
                    hashSet.add(method5.getName());
                } else if (Modifier.isPublic(method5.getModifiers()) && method5.getParameterTypes().length == 0) {
                    if (method5.getName().startsWith("has")) {
                        map.put(method5.getName(), method5);
                    } else if (method5.getName().startsWith("get")) {
                        treeMap.put(method5.getName(), method5);
                    }
                }
            }
            i3++;
        }
        for (Map.Entry entry : treeMap.entrySet()) {
            String strSubstring = ((String) entry.getKey()).substring(i);
            if (strSubstring.endsWith(LIST_SUFFIX) && !strSubstring.endsWith(BUILDER_LIST_SUFFIX) && !strSubstring.equals(LIST_SUFFIX) && (method4 = (java.lang.reflect.Method) entry.getValue()) != null && method4.getReturnType().equals(List.class)) {
                printField(buffer, indent, strSubstring.substring(0, strSubstring.length() - 4), GeneratedMessageLite.invokeOrDie(method4, messageLite, new Object[0]));
                i = 3;
            } else {
                if (strSubstring.endsWith(MAP_SUFFIX) && !strSubstring.equals(MAP_SUFFIX) && (method3 = (java.lang.reflect.Method) entry.getValue()) != null && method3.getReturnType().equals(Map.class) && !method3.isAnnotationPresent(Deprecated.class) && Modifier.isPublic(method3.getModifiers())) {
                    i2 = 3;
                    printField(buffer, indent, strSubstring.substring(0, strSubstring.length() - 3), GeneratedMessageLite.invokeOrDie(method3, messageLite, new Object[0]));
                } else {
                    i2 = 3;
                    if (hashSet.contains("set" + strSubstring)) {
                        if (strSubstring.endsWith(BYTES_SUFFIX)) {
                            if (!treeMap.containsKey("get" + strSubstring.substring(0, strSubstring.length() - 5))) {
                                method = (java.lang.reflect.Method) entry.getValue();
                                method2 = (java.lang.reflect.Method) map.get("has" + strSubstring);
                                if (method != null) {
                                    objInvokeOrDie = GeneratedMessageLite.invokeOrDie(method, messageLite, new Object[0]);
                                    if (method2 == null) {
                                        if (isDefaultValue(objInvokeOrDie)) {
                                            zBooleanValue = false;
                                        } else {
                                            zBooleanValue = true;
                                        }
                                    } else {
                                        zBooleanValue = ((Boolean) GeneratedMessageLite.invokeOrDie(method2, messageLite, new Object[0])).booleanValue();
                                    }
                                    if (zBooleanValue) {
                                        printField(buffer, indent, strSubstring, objInvokeOrDie);
                                    }
                                }
                            }
                        } else {
                            method = (java.lang.reflect.Method) entry.getValue();
                            method2 = (java.lang.reflect.Method) map.get("has" + strSubstring);
                            if (method != null) {
                                objInvokeOrDie = GeneratedMessageLite.invokeOrDie(method, messageLite, new Object[0]);
                                if (method2 == null) {
                                    if (isDefaultValue(objInvokeOrDie)) {
                                        zBooleanValue = true;
                                    } else {
                                        zBooleanValue = false;
                                    }
                                } else {
                                    zBooleanValue = ((Boolean) GeneratedMessageLite.invokeOrDie(method2, messageLite, new Object[0])).booleanValue();
                                }
                                if (zBooleanValue) {
                                    printField(buffer, indent, strSubstring, objInvokeOrDie);
                                }
                            }
                        }
                    }
                }
                i = i2;
            }
        }
        if (messageLite instanceof GeneratedMessageLite.ExtendableMessage) {
            Iterator<Map.Entry<T, Object>> it = ((GeneratedMessageLite.ExtendableMessage) messageLite).extensions.iterator();
            while (it.hasNext()) {
                Map.Entry entry2 = (Map.Entry) it.next();
                printField(buffer, indent, "[" + ((GeneratedMessageLite.ExtensionDescriptor) entry2.getKey()).getNumber() + "]", entry2.getValue());
            }
        }
        GeneratedMessageLite generatedMessageLite = (GeneratedMessageLite) messageLite;
        if (generatedMessageLite.unknownFields != null) {
            generatedMessageLite.unknownFields.printWithIndent(buffer, indent);
        }
    }

    private static boolean isDefaultValue(Object o) {
        if (o instanceof Boolean) {
            return !((Boolean) o).booleanValue();
        }
        if (o instanceof Integer) {
            return ((Integer) o).intValue() == 0;
        }
        if (o instanceof Float) {
            return Float.floatToRawIntBits(((Float) o).floatValue()) == 0;
        }
        if (o instanceof Double) {
            return Double.doubleToRawLongBits(((Double) o).doubleValue()) == 0;
        }
        if (o instanceof String) {
            return o.equals("");
        }
        if (o instanceof ByteString) {
            return o.equals(ByteString.EMPTY);
        }
        if (o instanceof MessageLite) {
            return o == ((MessageLite) o).getDefaultInstanceForType();
        }
        return (o instanceof java.lang.Enum) && ((java.lang.Enum) o).ordinal() == 0;
    }

    static void printField(StringBuilder buffer, int indent, String name, Object object) {
        if (object instanceof List) {
            Iterator it = ((List) object).iterator();
            while (it.hasNext()) {
                printField(buffer, indent, name, it.next());
            }
            return;
        }
        if (object instanceof Map) {
            Iterator it2 = ((Map) object).entrySet().iterator();
            while (it2.hasNext()) {
                printField(buffer, indent, name, (Map.Entry) it2.next());
            }
            return;
        }
        buffer.append('\n');
        indent(indent, buffer);
        buffer.append(pascalCaseToSnakeCase(name));
        if (object instanceof String) {
            buffer.append(": \"");
            buffer.append(TextFormatEscaper.escapeText((String) object));
            buffer.append(Typography.quote);
            return;
        }
        if (object instanceof ByteString) {
            buffer.append(": \"");
            buffer.append(TextFormatEscaper.escapeBytes((ByteString) object));
            buffer.append(Typography.quote);
            return;
        }
        if (object instanceof GeneratedMessageLite) {
            buffer.append(" {");
            reflectivePrintWithIndent((GeneratedMessageLite) object, buffer, indent + 2);
            buffer.append("\n");
            indent(indent, buffer);
            buffer.append("}");
            return;
        }
        if (object instanceof Map.Entry) {
            buffer.append(" {");
            Map.Entry entry = (Map.Entry) object;
            int i = indent + 2;
            printField(buffer, i, "key", entry.getKey());
            printField(buffer, i, "value", entry.getValue());
            buffer.append("\n");
            indent(indent, buffer);
            buffer.append("}");
            return;
        }
        buffer.append(": ");
        buffer.append(object);
    }

    private static void indent(int indent, StringBuilder buffer) {
        while (indent > 0) {
            char[] cArr = INDENT_BUFFER;
            int length = indent > cArr.length ? cArr.length : indent;
            buffer.append(cArr, 0, length);
            indent -= length;
        }
    }

    private static String pascalCaseToSnakeCase(String pascalCase) {
        if (pascalCase.isEmpty()) {
            return pascalCase;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toLowerCase(pascalCase.charAt(0)));
        for (int i = 1; i < pascalCase.length(); i++) {
            char cCharAt = pascalCase.charAt(i);
            if (Character.isUpperCase(cCharAt)) {
                sb.append("_");
            }
            sb.append(Character.toLowerCase(cCharAt));
        }
        return sb.toString();
    }
}
