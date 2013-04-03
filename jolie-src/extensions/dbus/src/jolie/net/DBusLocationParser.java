/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jolie.net;

/**
 *
 * @author jan
 */
public class DBusLocationParser {
  public static String[] parse(String location) {
    // Remove leading slash
    String path = location.substring(1);
    String[] parts = path.split(":", 2);
    
    if (parts != null) {
      if (parts.length == 2) {
        return parts;
      } else {
        throw new RuntimeException("Malformed location string, should be /[connection name][object path] " + location);
      }
    } else {
      throw new RuntimeException("Malformed location string " + location);
    }
  }
}
