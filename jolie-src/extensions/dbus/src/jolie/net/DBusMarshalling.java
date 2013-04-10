/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jolie.net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.UInt64;
import org.freedesktop.dbus.types.DBusMapType;

/**
 *
 * @author niels
 */
public class DBusMarshalling {

  public static Object[] valueToDBus(Value value, StringBuilder builder) {
    ArrayList<Object> objects = new ArrayList<Object>();
    Map<String, ValueVector> children = value.children();

    if (children.isEmpty()) {
      if (value.isDefined()) {
        objects.add(value.valueObject());
        builder.append(DBusMarshalling.nativeValueToDBusString(value));
      }
    } else if (children.size() == 1 && (children.get("params") != null)) {
      ValueVector params = children.get("params");

      for (Value v : params) {
        objects.add(DBusMarshalling._valueToDBus(v, builder));
      }
    } else {
      // Please live up to the constraints!
    }

    return objects.toArray();
  }

  private static Object _valueToDBus(Value value, StringBuilder builder) {
    Map<String, ValueVector> children = value.children();

    if (children.isEmpty()) {
      if (value.isDefined()) {
        builder.append(DBusMarshalling.nativeValueToDBusString(value));
        return value.valueObject();
      }
      return null;
    } else {
      //Object[][] map = new Object[children.size()][];
      ArrayList<Object> map  = new ArrayList<Object>();
      String type = "";
      
      builder.append("{s");    
      int i = 0;

      for (Entry<String, ValueVector> e : children.entrySet()) {
        if (i == 0) type = DBusMarshalling.valueVectorToDBusString(e.getValue());
        else if (!type.equals(DBusMarshalling.valueVectorToDBusString(e.getValue()))) {
          throw new RuntimeException(
                  "D-Bus does not support maps with multiple types as values. Tried to add: "+
                  DBusMarshalling.valueVectorToDBusString(e.getValue())+
                  " with key "+
                  e.getKey()+
                  " to a map already containing: "+
                  type);
          // This i a bit of a lie 
        }
        
        //map[i] = new Object[] { e.getKey(), DBusMarshalling.valueVectorToObject(e.getValue()) };
        map.add(e.getKey());
        map.add(DBusMarshalling.valueVectorToObject(e.getValue()));
        i++;
      }
      builder.append(type);
      builder.append("}");

      return map.toArray();
    }
  }

  public static String nativeValueToDBusString(Value value) {
    if (value.isBool()) {
      return "b";
    } else if (value.isInt()) {
      return "u";
    } else if (value.isString()) {
      return "s";
    } else if (value.isDouble()) {
      return "d";
    } else if (value.isLong()) {
      return "x"; // Int64
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
    Map<String, ValueVector> children = value.children();

    if (children.isEmpty()) {
      if (value.isDefined()) {
        typeString.append(DBusMarshalling.nativeValueToDBusString(value));
      }
    } else if (children.size() == 1 && (children.get("params") != null)) {
      ValueVector params = children.get("params");

      for (Value v : params) {
        typeString.append(DBusMarshalling.nativeValueToDBusString(v));
      }
    } else {
      // Please live up to the constraints!
    }

    return typeString.toString();
  }

  public static Object[] valueToObjectArray(Value value) {
    ArrayList<Object> objects = new ArrayList<Object>();

    if (value.children().isEmpty()) {
      if (value.isDefined()) {
        objects.add(value.valueObject());
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
        //objects.add(DBusMarshalling.valueToObjectArray(vector.get(i)));
        
        objects.add(DBusMarshalling._valueToDBus(vector.get(i), new StringBuilder()));
      }
      return objects.toArray();
    } else {
      Value first = vector.first();
      if (first.children().isEmpty()) {
        return first.valueObject();
      } else {
        return DBusMarshalling.valueToObjectArray(vector.first());
      }
    }
  }

  public static Value ToJolieValue(Object[] val, String signature) {
    if (val == null || val.length == 0) {
      return Value.UNDEFINED_VALUE;
    } else {
      Object v = val[0];

      if (v instanceof UInt16) {
        UInt16 i = (UInt16) v;
        return Value.create(i.intValue());
      } else if (v instanceof UInt32) {
        UInt32 i = (UInt32) v;
        return Value.create(i.intValue());
      } else if (v instanceof UInt64) {
        UInt64 i = (UInt64) v;
        return Value.create(i.longValue());
      } else if (v instanceof Double) {
        return Value.create((Double) v);
      } else if (v instanceof String) {
        return Value.create((String) v);
      } else if (v instanceof Boolean) {
        return Value.create((Boolean) v);
      } else {
        throw new RuntimeException("Cannot translate DBus value to Jolie");
      }
    } 
  }
}
