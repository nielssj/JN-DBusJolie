/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jolie.net;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.UInt64;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.DBusListType;
import org.freedesktop.dbus.types.DBusMapType;

/**
 *
 * @author niels
 */
public class DBusMarshalling {
  private static boolean TRACE = false;

  private static Type getType(Value value) {
    Map<String, ValueVector> children = value.children();

    if (children.isEmpty()) {
      if (value.isDefined()) {
        return value.valueObject().getClass();
      }
      return null;
    } else {
      Type type = null;
      Type nextType;

      for (Entry<String, ValueVector> e : children.entrySet()) {
        nextType = DBusMarshalling.getValueVectorType(e.getValue());
        if (type != null) {
          if (!DBusMarshalling.typesMatch(type, nextType)) {
            throw new RuntimeException("DBus maps does not support several types. Trying to add " + nextType + " to an map of " + type);
          }
        } else {
          type = nextType;
        }
      }

      return new DBusMapType(String.class, type);
    }
  }

  private static Type getValueVectorType(ValueVector vector) {
    if (vector.size() > 1) {
      Type type = null;
      Type nextType;

      for (Value v : vector) {
        nextType = DBusMarshalling.getType(v);
        if (type != null) {
          if (!DBusMarshalling.typesMatch(type, nextType)) {
            throw new RuntimeException("DBus arrays does not support several types. Trying to add " + nextType + " to an array of " + type);
          }
        } else {
          type = nextType;
        }
      }

      return new DBusListType(type);
    } else {
      return DBusMarshalling.getType(vector.first());
    }
  }
   private static Boolean typesMatch(Type t1 , Type t2) {
    if (t1 == t2) return true;
    else if (t1 instanceof DBusMapType && t2 instanceof DBusMapType) {
      DBusMapType map1 = (DBusMapType) t1;
      DBusMapType map2 = (DBusMapType) t2;
      
      // 0 is key - always string
      return DBusMarshalling.typesMatch(map1.getActualTypeArguments()[1], map2.getActualTypeArguments()[1]);
    } else if (t1 instanceof DBusListType && t2 instanceof DBusListType) {
      DBusListType list1 = (DBusListType) t1;
      DBusListType list2 = (DBusListType) t2;
      
      return DBusMarshalling.typesMatch(list1.getActualTypeArguments()[0], list2.getActualTypeArguments()[0]);
    } else {
      return false;
    }
  }

  public static synchronized Object[] valuesToDBus(Value value, StringBuilder builder) throws DBusException {
    ArrayList<Object> objects = new ArrayList<Object>();
    Map<String, ValueVector> children = value.children();
    String typeString = "";
    List<Type> types = new ArrayList<Type>();

    if (children.isEmpty()) {
      if (value.isDefined()) {
        Object valObj = value.valueObject();

        types.add(valObj.getClass());
        objects.add(valObj);
      }
    } else if (children.size() == 1 && (children.get("params") != null)) {
      ValueVector params = children.get("params");

      for (Value v : params) {
        objects.add(DBusMarshalling.valueToDBus(v));
        types.add(DBusMarshalling.getType(v));
      }
    } else {
      throw new RuntimeException("Arguments to DBus must be either a single type or a map with one property named .params");
    }

    typeString = Marshalling.getDBusType(types.toArray(new Type[types.size()]));
    builder.append(typeString);
    return Marshalling.convertParameters(objects.toArray(), types.toArray(new Type[types.size()]), null);
  }

  private static Object valueToDBus(Value value) throws DBusException {
    Map<String, ValueVector> children = value.children();

    if (children.isEmpty()) {
      if (value.isDefined()) {
        return value.valueObject();
      }
      return null;
    } else {
      Map<String, Object> objects = new HashMap<String, Object>();

      for (Entry<String, ValueVector> e : children.entrySet()) {
        objects.put(e.getKey(), DBusMarshalling.valueVectorToDBus(e.getValue()));
      }
      return objects;
    }
  }

  public static Object valueVectorToDBus(ValueVector vector) throws DBusException {
    if (vector.size() > 1) {
      ArrayList<Object> objects = new ArrayList<Object>();

      for (Value v : vector) {
        objects.add(DBusMarshalling.valueToDBus(v));
      }

      return objects.toArray();
    } else {
      return DBusMarshalling.valueToDBus(vector.first());
    }
  }

  public static Value singleDBusToJolie(Object val, Type t) {
    if (TRACE) System.out.println("singleDBusToJolie got type " + t);
    if (t.equals(Integer.class)) {
      return Value.create((Integer) val);
    } else if (t.equals(String.class)) {
      return Value.create((String) val);
    } else if (t.equals(Boolean.class)) {
      return Value.create((Boolean) val);
    } else if (t.equals(Double.class)) {
      return Value.create((Double) val);
    } else if (t.equals(Long.class)) {
      return Value.create((Long) val);
    } else if (t instanceof DBusListType) {
      DBusListType list = (DBusListType) t;



      //if (TRACE) System.out.println(i);
      if (TRACE) System.out.println(list.getActualTypeArguments()[0]);

      throw new RuntimeException("Cannot translate DBus value to Jolie" + t);
    } else {
      throw new RuntimeException("Cannot translate DBus value to Jolie" + t);
    }
  }

  public static Value ToJolieValue(Object[] val, String signature) {
    if (val == null || val.length == 0) {
      return Value.UNDEFINED_VALUE;
    } else {
      List<Type> types = new ArrayList<Type>();
      try {
        Marshalling.getJavaType(signature, types, -1);
      } catch (DBusException ex) {
        Logger.getLogger(DBusMarshalling.class.getName()).log(Level.SEVERE, null, ex);
      }
      if (TRACE) System.out.println("types " + Arrays.deepToString(types.toArray()));

      if (types.size() == 1) {
        return DBusMarshalling.singleDBusToJolie(val[0], types.get(0));
      } else {
        Value ret = Value.create();

        return ret;
      }
    }
  }
}
