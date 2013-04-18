package jolie.net;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

  /*
   * Check if two types match. First checks if two types are equal, and if they are not,
   * a check is made to see whether the types are either DBusMapType or DBusListType, and
   * if they are a check is made to see whether the type of their values are the same.
   */
  private static Boolean typesMatch(Type t1, Type t2) {
    if (t1 == t2) {
      return true;
    } else if (t1 instanceof DBusMapType && t2 instanceof DBusMapType) {
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
  
  public static Object[] valueToDBus(Value value, String[] argNames) throws DBusException {
     ArrayList<Object> objects = new ArrayList<Object>();
    Map<String, ValueVector> children = value.children();
    String typeString;
    List<Type> types = new ArrayList<Type>();

    if (children.isEmpty()) {
      if (value.isDefined()) {
        Object valObj = value.valueObject();

        types.add(valObj.getClass());
        objects.add(valObj);
      }
    } else {
      for (String argName : argNames) {
        ValueVector vv = children.get(argName);
        objects.add(DBusMarshalling.valueVectorToDBus(vv));
      }
    }
    
    return objects.toArray();
  }
  
  public static synchronized Object[] valuesToDBus(Value value, StringBuilder builder) throws DBusException {
    ArrayList<Object> objects = new ArrayList<Object>();
    Map<String, ValueVector> children = value.children();
    String typeString;
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
    return objects.toArray();
    //return Marshalling.convertParameters(objects.toArray(), types.toArray(new Type[types.size()]), null);
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

  private static Object valueVectorToDBus(ValueVector vector) throws DBusException {
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

  private static Value singleDBusToJolie(Object val, Type t) {
    if (DBusMarshalling.specialType(t)) {
      return DBusMarshalling.specialTypeToJolieValue(val, t);
    } else if (t.equals(Short.class)) {
      return Value.create(((Short) val).intValue());
    } else if (t.equals(Integer.class)) {
      return Value.create((Integer) val);
    } else if (t.equals(Long.class)) {
      return Value.create(((Long) val));
    } else if (t.equals(UInt16.class)) {
      return Value.create(((UInt16) val).intValue());
    } else if (t.equals(UInt32.class)) {
      return Value.create(((UInt32) val).longValue());
    } else if (t.equals(UInt64.class)) {
      // UInt64 is actually a BigInteger, but Jolie only supports long :-(
      return Value.create(((UInt64) val).longValue());
    } else if (t.equals(Byte.class)) {
      return Value.create(((Byte) val).intValue());
    } else if (t.equals(String.class)) {
      return Value.create((String) val);
    } else if (t.equals(Boolean.class)) {
      return Value.create((Boolean) val);
    } else if (t.equals(Double.class)) {
      return Value.create((Double) val);
    } else if (t.equals(Long.class)) {
      return Value.create((Long) val);
    } else {
      throw new RuntimeException("Cannot translate DBus value to Jolie" + t);
    }
  }

  private static boolean specialType(Type t) {
    return t instanceof DBusMapType || t instanceof DBusListType;
  }

  private static Value DBusMapToJolie(Map m, DBusMapType t) {
    Value ret = Value.create();
    Map<String, ValueVector> children = ret.children();
    Type valType = t.getActualTypeArguments()[1];

    for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
      Entry e = (Entry) it.next();
      ValueVector v;

      if (valType instanceof DBusListType) {
        v = DBusMarshalling.DBusListToJolie((Iterable) e.getValue(), (DBusListType) valType);
      } else {
        v = ValueVector.create();
        v.add(DBusMarshalling.singleDBusToJolie(e.getValue(), valType));
      }
      children.put((String) e.getKey(), v);
    }

    return ret;
  }

  private static ValueVector DBusListToJolie(Object o, DBusListType t) {
    Type valType = t.getActualTypeArguments()[0];
    ValueVector v = ValueVector.create();

    if (o instanceof Iterable) { // Array of wrapper types 
      for (Object obj : (Iterable) o) {
        v.add(DBusMarshalling.singleDBusToJolie(obj, valType));
      }
    } else { // Array of primitive types
      for (int i = 0; i < Array.getLength(o); i++) {
        v.add(DBusMarshalling.singleDBusToJolie(Array.get(o, i), valType));
      }
    }

    return v;
  }

  private static Value specialTypeToJolieValue(Object o, Type t) {
    if (t instanceof DBusMapType) {
      return DBusMarshalling.DBusMapToJolie((Map) o, (DBusMapType) t);
    } else if (t instanceof DBusListType) {
      Value ret = Value.create();
      Map<String, ValueVector> children = ret.children();
      ValueVector v = DBusMarshalling.DBusListToJolie(o, (DBusListType) t);

      children.put("", v);

      return ret;
    }
    return null;
  }

  /*
   * Given an array óf objects and a DBus signature string, convert it to a Jolie Value.
   * If the array of objects is null or empty, returns an undefined value.
   * If the signature has a single type, return the value found at val[0] as a Jolie value
   * If the signature has multiple types, return them as a Jolie value with a single child
   * named `params` which is an array. 
   */
  public static Value ToJolieValue(Object[] val, String signature) throws DBusException {
    if (val == null || val.length == 0) {
      return Value.UNDEFINED_VALUE;
    } else {
      List<Type> types = new ArrayList<Type>();
      Marshalling.getJavaType(signature, types, -1);

      if (types.size() == 1 && !DBusMarshalling.specialType(types.get(0))) {
        return DBusMarshalling.singleDBusToJolie(val[0], types.get(0));
      } else {
        Value ret = Value.create();
        ValueVector vector = ret.getChildren("params");

        for (int i = 0; i < types.size(); i++) {
          Type type = types.get(i);

          if (type instanceof DBusListType) {
            ValueVector temp = DBusMarshalling.DBusListToJolie(val[i], (DBusListType) type);

            for (Value v : temp) {
              vector.add(v);
            }
          } else if (type instanceof DBusMapType) {
            vector.add(DBusMarshalling.DBusMapToJolie((Map) val[i], (DBusMapType) types.get(i)));
          } else {
            vector.add(DBusMarshalling.singleDBusToJolie(val[i], types.get(i)));
          }
        }

        return ret;
      }
    }
  }
}