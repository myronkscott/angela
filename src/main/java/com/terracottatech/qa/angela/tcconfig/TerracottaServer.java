package com.terracottatech.qa.angela.tcconfig;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Logical definition of a Terracotta server instance
 *
 * @author Aurelien Broszniowski
 */
public class TerracottaServer implements Serializable {

  private String serverSymbolicName;
  private String hostname;
  private Ports ports;

  public TerracottaServer() {
  }

  public TerracottaServer(String serverSymbolicName, String hostname, int tsaPort, int tsaGroupPort, int managementPort, int jmxPort) {
    this.serverSymbolicName = serverSymbolicName;
    this.hostname = hostname;
    this.ports = new Ports(tsaPort, tsaGroupPort, managementPort, jmxPort);
  }

  public String getHostname() {
    return hostname;
  }

  public String getIp() throws UnknownHostException {
    InetAddress address = InetAddress.getByName(hostname);
    return address.getHostAddress();
  }

  public Ports getPorts() {
    return ports;
  }

  public String getServerSymbolicName() {
    return serverSymbolicName != null ? serverSymbolicName : "" + hostname + ":" + ports.getTsaPort();
  }

  public void setPorts(final Ports ports) {
    this.ports = ports;
  }
}
