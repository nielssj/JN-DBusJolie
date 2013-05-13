/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jolie.test;

import org.freedesktop.dbus.DBusInterface;

/**
 *
 * @author jan
 */
public interface JolieToJava extends DBusInterface {
  public String concat(String s1, String s2);
}