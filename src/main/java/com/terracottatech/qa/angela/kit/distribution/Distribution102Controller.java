package com.terracottatech.qa.angela.kit.distribution;

import com.terracottatech.qa.angela.kit.TerracottaServerInstance;
import com.terracottatech.qa.angela.topology.LicenseType;
import com.terracottatech.qa.angela.topology.PackageType;
import com.terracottatech.qa.angela.topology.Version;

/**
 * @author Aurelien Broszniowski
 */

public class Distribution102Controller extends DistributionController {

  public Distribution102Controller(final Version version, final PackageType packageType, final LicenseType licenseType) {
    super(version, packageType, licenseType);
  }

  @Override
  public TerracottaServerInstance.TerracottaServerState start() {
    System.out.println("start 10.2");
    return TerracottaServerInstance.TerracottaServerState.STARTED_AS_ACTIVE;
  }

  @Override
  public void stop() {
    System.out.println("stop 10.2");
  }
}
