package com.terracottatech.qa.angela;

import com.terracottatech.qa.angela.client.ClientArray;
import com.terracottatech.qa.angela.client.ClientArrayFuture;
import com.terracottatech.qa.angela.client.ClientJob;
import com.terracottatech.qa.angela.client.ClusterFactory;
import com.terracottatech.qa.angela.client.Tms;
import com.terracottatech.qa.angela.client.TmsHttpClient;
import com.terracottatech.qa.angela.client.Tsa;
import com.terracottatech.qa.angela.client.config.ConfigurationContext;
import com.terracottatech.qa.angela.client.config.custom.CustomConfigurationContext;
import com.terracottatech.qa.angela.common.distribution.Distribution;
import com.terracottatech.qa.angela.common.tcconfig.ServerSymbolicName;
import com.terracottatech.qa.angela.common.tms.security.config.TmsClientSecurityConfig;
import com.terracottatech.qa.angela.common.tms.security.config.TmsServerSecurityConfig;
import com.terracottatech.qa.angela.common.topology.ClientArrayTopology;
import com.terracottatech.qa.angela.common.topology.LicenseType;
import com.terracottatech.qa.angela.common.topology.PackageType;
import com.terracottatech.qa.angela.common.topology.Topology;
import com.terracottatech.qa.angela.test.Versions;
import com.terracottatech.security.test.util.SecurityRootDirectory;
import com.terracottatech.security.test.util.SecurityRootDirectoryBuilder;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static com.terracottatech.qa.angela.common.clientconfig.ClientArrayConfig.newClientArrayConfig;
import static com.terracottatech.qa.angela.common.distribution.Distribution.distribution;
import static com.terracottatech.qa.angela.common.tcconfig.NamedSecurityRootDirectory.withSecurityFor;
import static com.terracottatech.qa.angela.common.tcconfig.SecureTcConfig.secureTcConfig;
import static com.terracottatech.qa.angela.common.tcconfig.SecurityRootDirectory.securityRootDirectory;
import static com.terracottatech.qa.angela.common.topology.Version.version;
import static com.terracottatech.security.test.util.SecurityTestUtil.StoreCharacteristic.VALID;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TmsSecurityCredentialsTest {

  private final static Logger LOGGER = LoggerFactory.getLogger(TmsSecurityCredentialsTest.class);

  private static ClusterFactory factory;
  private static final String TMS_HOSTNAME = "localhost";

  private static URI clientTruststoreUri;
  private static Tms TMS;
  private static Tsa TSA;

  @ClassRule
  public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

  @BeforeClass
  public static void setUp() throws Exception {
    SecurityRootDirectory clientSecurityRootDirectory = new SecurityRootDirectoryBuilder(TEMPORARY_FOLDER.newFolder())
        .withTruststore(VALID)
        .withCredentials("credentials.properties")
        .build();
    SecurityRootDirectory serverSecurityRootDirectory = new SecurityRootDirectoryBuilder(TEMPORARY_FOLDER.newFolder())
        .withTruststore(VALID)
        .withKeystore(VALID)
        .withUsersXml("users-with-roles.xml")
        .build();
    SecurityRootDirectory tmsSecurityRootDirectory = new SecurityRootDirectoryBuilder(TEMPORARY_FOLDER.newFolder())
        .withTruststore(VALID)
        .withKeystore(VALID)
        .withUsersXml("users-with-roles.xml")
        .build();

    clientTruststoreUri = clientSecurityRootDirectory.getTruststorePaths().iterator().next().toUri();

    Distribution distribution = distribution(version(Versions.TERRACOTTA_VERSION), PackageType.KIT, LicenseType.TERRACOTTA);

    TmsServerSecurityConfig securityConfig = new TmsServerSecurityConfig.Builder()
        .with(config->{
              config.tmsSecurityRootDirectory = tmsSecurityRootDirectory.getPath().toString();
              config.tmsSecurityRootDirectoryConnectionDefault = clientSecurityRootDirectory.getPath().toString();
              config.tmsSecurityHttpsEnabled = "true";
              config.tmsSecurityAuthenticationScheme = "file";
              config.tmsSecurityAuthorizationScheme = "file";

            }
        ).build();
    ConfigurationContext configContext = CustomConfigurationContext.customConfigurationContext()
        .tsa(tsa -> tsa.topology(new Topology(
            distribution(
                version(Versions.TERRACOTTA_VERSION),
                PackageType.KIT, LicenseType.TERRACOTTA
            ),
            secureTcConfig(
                version(Versions.TERRACOTTA_VERSION),
                TmsSecurityCredentialsTest.class.getResource("/terracotta/10/tc-config-a-with-security-credentials.xml"),
                withSecurityFor(new ServerSymbolicName("Server1"), securityRootDirectory(serverSecurityRootDirectory.getPath()))
            )))
            .license(LicenseType.TERRACOTTA.defaultLicense())
        ).tms(tms -> tms.distribution(distribution)
            .license(LicenseType.TERRACOTTA.defaultLicense())
            .hostname(TMS_HOSTNAME)
            .securityConfig(securityConfig)
        ).clientArray(clientArray -> clientArray.license(LicenseType.TERRACOTTA.defaultLicense())
            .clientArrayTopology(new ClientArrayTopology(distribution(version(Versions.TERRACOTTA_VERSION), PackageType.KIT, LicenseType.TERRACOTTA), newClientArrayConfig().host("localhost")))
        );

    factory = new ClusterFactory("TmsSecurityCredentialsTest::testSecureConnection", configContext);
    TSA = factory.tsa()
        .startAll()
        .licenseAll(securityRootDirectory(clientSecurityRootDirectory.getPath()));

    TMS = factory.tms();
    TMS.start();
  }

  @Test
  public void step1_could_create_connection_to_secure_cluster_test() throws Exception {
    TmsClientSecurityConfig tmsClientSecurityConfig = new TmsClientSecurityConfig("terracotta_security_password", clientTruststoreUri, "dave", "password");
    TmsHttpClient tmsHttpClient = TMS.httpClient(tmsClientSecurityConfig);
    tmsHttpClient.login();
    String connectionName = tmsHttpClient.createConnectionToCluster(TSA.uri());
    assertThat(connectionName, startsWith("TmsSecurityCredentialsTest"));
  }

  @Test
  public void step2_http_client_connects_to_tms_using_ssl_test() throws Exception {
    TmsClientSecurityConfig tmsClientSecurityConfig = new TmsClientSecurityConfig("terracotta_security_password", clientTruststoreUri, "dave", "password");
    ClientArray clientArray = factory.clientArray();
    TmsHttpClient tmsHttpClient = TMS.httpClient(tmsClientSecurityConfig);

    ClientJob clientJobTms = (context) -> {
      tmsHttpClient.login();

      String response = tmsHttpClient.sendGetRequest("/api/connections");
      LOGGER.info("tms list connections result :" + response);
      assertThat(response, Matchers.containsString("TmsSecurityCredentialsTest"));

      String infoResponse = tmsHttpClient.sendGetRequest("/api/info");
      LOGGER.info("tms info :" + response);

      assertThat(infoResponse, Matchers.containsString("\"connection_secured\":true"));
      assertThat(infoResponse, Matchers.containsString("\"connection_sslEnabled\":true"));
      assertThat(infoResponse, Matchers.containsString("\"connection_certificateAuthenticationEnabled\":false"));
      assertThat(infoResponse, Matchers.containsString("\"connection_hasPasswordToPresent\":true"));

    };

    ClientArrayFuture fTms = clientArray.executeOnAll(clientJobTms);
    fTms.get();
    LOGGER.info("---> Stop");
  }

  @AfterClass
  public static void tearDownStuff() throws Exception {
    if (factory != null) {
      factory.close();
    }
  }
}
