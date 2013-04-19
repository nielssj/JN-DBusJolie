package jolie.net;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jolie.lang.NativeType;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.UInt64;
import org.freedesktop.dbus.Variant;
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

  /*
   * Build an array of objects from a Jolie Value. Requires the name of the arguments, which means you must have introspected the object that you are calling.
   * The length of Value.children() and argNames must match, and the keys must be the same
   */
  public static Object[] valueToDBus(Value value, String[] argNames) throws DBusException {
    ArrayList<Object> objects = new ArrayList<Object>();
    Map<String, ValueVector> children = value.children();

    if (children.isEmpty()) {
      if (value.isDefined()) {
        Object valObj = value.valueObject();

        objects.add(valObj);
      }
    } else {
      // We need to use sets here, since the order of the argument names does not matter
      if (!children.keySet().equals(new HashSet<String>(Arrays.asList(argNames)))) {
        throw new RuntimeException("Trying to pass an object with keys " + children.keySet() + " to a method expecting " + Arrays.deepToString(argNames));
      }

      for (String argName : argNames) {
        ValueVector vv = children.get(argName);
        objects.add(DBusMarshalling.valueVectorToDBus(vv));
      }
    }

    return objects.toArray();
  }

  /*
   * Builds an array of objects and their type string from a Jolie Value. The value must be either a single root value (with no children),
   * or the children must be named arg0, arg1, etc.
   * 
   * This should be used when you have no introspection data, i.e. no knowledge (in Jolie) of argument names and types.
   */
  public static synchronized Object[] valueToDBus(Value value, StringBuilder builder) throws DBusException {
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
      // Sort the arg names first, to ensure that they are ordered arg0, arg1 etc. 
      String[] sortedNames = children.keySet().toArray(new String[children.keySet().size()]);
      Arrays.sort(sortedNames);

      for (String name : sortedNames) {
        if (name.indexOf("arg") == -1) {
          throw new RuntimeException("Values given to non-introspectable types must be of the form arg0, arg1 etc.");
        } else {
          for (Value v : children.get(name)) {
            objects.add(DBusMarshalling.valueToDBus(v));
            types.add(DBusMarshalling.getType(v));
          }
        }
      }
    }

    typeString = Marshalling.getDBusType(types.toArray(new Type[types.size()]));

    builder.append(typeString);
    return objects.toArray();
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
    if (t instanceof DBusMapType) {
      return DBusMarshalling.DBusMapToJolie((Map) val, (DBusMapType) t);
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

  private static Value DBusMapToJolie(Map m, DBusMapType t) {
    Value ret = Value.create();
    Map<String, ValueVector> children = ret.children();
    Type valType = t.getActualTypeArguments()[1];

    for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
      Entry e = (Entry) it.next();
      ValueVector v = ValueVector.create();
      DBusMarshalling.addObjectToValueVector(e.getValue(), valType, v);
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

  /*
   * Given an array of objects and a DBus signature string, convert it to a Jolie Value.
   * If the array of objects is null or empty, returns an undefined value.
   * If the signature has a single type, return the value found at val[0] as a Jolie value
   * If the signature has multiple types, return them as a Jolie value with a single child
   * named `params` which is an array. 
   */
  public static Value ToJolieValue(Object[] val, String signature, String[] argNames) throws DBusException {
    if (val == null || val.length == 0) {
      return Value.UNDEFINED_VALUE;
    } else {
      List<Type> types = new ArrayList<Type>();
      Marshalling.getJavaType(signature, types, -1);

      if (types.size() == 1 && !(types.get(0) instanceof DBusListType)) {
        return DBusMarshalling.singleDBusToJolie(val[0], types.get(0));
      } else {
        Value ret = Value.create();

        // argNames will always have same length as types
        for (int i = 0; i < types.size(); i++) {
          String argName = argNames != null ? argNames[i] : "arg" + i;
          Type type = types.get(i);
          ValueVector vector = ret.getChildren(argName);

          DBusMarshalling.addObjectToValueVector(val[i], type, vector);
        }
        return ret;
      }
    }
  }

   private static void addObjectToValueVector(Object o, Type type, ValueVector target) {
    if (type instanceof DBusListType) {
      ValueVector temp = DBusMarshalling.DBusListToJolie(o, (DBusListType) type);

      for (Value v : temp) {
        target.add(v);
      }
    } else if (type instanceof DBusMapType) {
      target.add(DBusMarshalling.DBusMapToJolie((Map) o, (DBusMapType) type));
    } else {
      target.add(DBusMarshalling.singleDBusToJolie(o, type));
    }
  }
  
  private static Map<jolie.lang.NativeType, java.lang.reflect.Type> jnToJavaMap;
  static 
  {
      jnToJavaMap = new EnumMap<jolie.lang.NativeType, java.lang.reflect.Type>(jolie.lang.NativeType.class);
      jnToJavaMap.put(NativeType.STRING, String.class);
      jnToJavaMap.put(NativeType.INT, int.class);
      jnToJavaMap.put(NativeType.LONG, long.class);
      jnToJavaMap.put(NativeType.BOOL, boolean.class);
      jnToJavaMap.put(NativeType.DOUBLE, Double.class);
      jnToJavaMap.put(NativeType.ANY, Object.class);
  }
  
  private static java.lang.reflect.Type jolieNativeTypeToJava(jolie.lang.NativeType jNType) {
      return DBusMarshalling.jnToJavaMap.get(jNType);
  }
  
  public static Map<String, String> jolieTypeToDBusString(jolie.runtime.typing.Type jType) throws DBusException {
      Map<String, String> types = new HashMap<String, String>();
      java.lang.reflect.Type[] javaType;
      
      // Is it a complex type?
      if(jType.subTypeSet().size() > 0) {
          // Loop through sub-types 
          for (Entry<String, jolie.runtime.typing.Type> st : jType.subTypeSet()) {
              
              // Is it an array?
              if(st.getValue().cardinality().max() > 1) {
                  // Make array type string
                  java.lang.reflect.Type elementType = DBusMarshalling.jolieNativeTypeToJava(st.getValue().nativeType());
                  javaType = new java.lang.reflect.Type[] { 
                      new DBusListType(elementType)
                  };
              } else {
                  // Is it a native type or deeper tree structure?
                  if(st.getValue().subTypeSet().size() > 0) {
                      // Make native type string
                      javaType = new java.lang.reflect.Type[] { 
                            jolieNativeTypeToJava(st.getValue().nativeType())
                      };
                  } else {
                      // Make map type string
                      javaType = new java.lang.reflect.Type[] { 
                            new DBusMapType(String.class, Variant.class)
                      };
                  }
              }
              
              types.put(st.getKey(), Marshalling.getDBusType(javaType));
          }
          
          
      } else {
          javaType = new java.lang.reflect.Type[] { 
                DBusMarshalling.jolieNativeTypeToJava(jType.nativeType())
          };
          types.put("", Marshalling.getDBusType(javaType));
      }
      
      return types;
  }
}