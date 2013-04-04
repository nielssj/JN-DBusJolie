/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jolie.net;

import java.util.ArrayList;
import java.util.Map;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.UInt64;

/**
 *
 * @author niels
 */
public class DBusMarshalling {
    
    public static String nativeValueToDBusString(Value value) {
        if (value.isBool()) {
            return "b";
        } else if (value.isInt()) {
            return "u";
        } else if (value.isString()) {
            return "s";
        }

        return "";
    }

    public static String valueVectorToDBusString(ValueVector vector) {
        if (vector.size() > 1) {
            String arrType = DBusMarshalling.valueToDBusString(vector.first());
            return "a(" + arrType + ")";
        } else {
            return DBusMarshalling.valueToDBusString(vector.first());
        }
    }

    public static String valueToDBusString(Value value) {
        StringBuilder typeString = new StringBuilder();

        if (value.children().isEmpty()) {
            if (value.isDefined()) {
                typeString.append(DBusMarshalling.nativeValueToDBusString(value));
            }
        } else {
            int size = value.children().size();
            int i = 0;
            for (Map.Entry< String, ValueVector> child : value.children().entrySet()) {
                if (child.getValue().isEmpty() == false) {
                    typeString.append(DBusMarshalling.valueVectorToDBusString(child.getValue()));
                }
            }
        }

        return typeString.toString();
    }

    public static Object[] valueToObjectArray(Value value) {
        ArrayList<Object> objects = new ArrayList<Object>();

        if (value.children().isEmpty()) {
            if (value.isDefined()) {
                objects.add(DBusMarshalling.nativeValueToObject(value));
            }
        } else {
            for (Map.Entry< String, ValueVector> child : value.children().entrySet()) {
                if (child.getValue().isEmpty() == false) {
                    objects.add(DBusMarshalling.valueVectorToObject(child.getValue()));
                }
            }
        }

        return objects.toArray();
    }

    public static Object valueVectorToObject(ValueVector vector) {
        if (vector.size() > 1) {
            ArrayList<Object> objects = new ArrayList<Object>();

            for (int i = 0; i < vector.size(); i++) {
                objects.add(DBusMarshalling.valueToObjectArray(vector.get(i)));
            }
            return objects.toArray();
        } else {
            Value first = vector.first();
            if (first.children().isEmpty()) {
                return DBusMarshalling.nativeValueToObject(first);
            } else {
                return DBusMarshalling.valueToObjectArray(vector.first());
            }
        }
    }

    public static Object nativeValueToObject(Value value) {
        if (value.isBool()) {
            return value.boolValue();
        } else if (value.isInt()) {
            return value.intValue();
        } else if (value.isString()) {
            return value.strValue();
        } else {
            return null;
        }
    }
    
    public static Value ToJolieValue(Object[] val, String signature) {
        if (val == null || val.length == 0) {
            return Value.UNDEFINED_VALUE;
        } else {
            Object v = val[0];

            if (v instanceof UInt32) {
                UInt32 i = (UInt32) v;
                return Value.create(i.intValue());
            } else if (v instanceof UInt16) {
                UInt16 i = (UInt16) v;
                return Value.create(i.intValue());
            } else if (v instanceof UInt64) {
                UInt64 i = (UInt64) v;
                return Value.create(i.intValue());
            } else if (v instanceof String) {
                return Value.create((String) v);
            } else if (v instanceof Boolean) {
                return Value.create((Boolean) v);
            } else {
                throw new RuntimeException("Cannot translate DBus response to Jolie");
            }
        }
    }
    
    public static void printArray(Object[] arr) {
        System.out.println("-- Begin print array");
        for (Object o : arr) {
            System.out.printf("arr %s \n", o);
        }
        System.out.println("-- End print array");
    }
}
