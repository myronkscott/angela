/*
 * The contents of this file are subject to the Terracotta Public License Version
 * 2.0 (the "License"); You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://terracotta.org/legal/terracotta-public-license.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Covered Software is Angela.
 *
 * The Initial Developer of the Covered Software is
 * Terracotta, Inc., a Software AG company
 */

package org.terracotta.angela.client.config;

import org.terracotta.angela.common.TerracottaCommandLineEnvironment;
import org.terracotta.angela.common.tcconfig.License;
import org.terracotta.angela.common.topology.Topology;

public interface TsaConfigurationContext {
  Topology getTopology();

  License getLicense();

  String getClusterName();

  TerracottaCommandLineEnvironment getTerracottaCommandLineEnvironment(String whatFor);

  interface TerracottaCommandLineEnvironmentKeys {
    String CLUSTER_TOOL = "ClusterTool";
    String CONFIG_TOOL = "ConfigTool";
    String JCMD = "Jcmd";
    String SERVER_START_PREFIX = "Server-";
    String SERVER_STOP_PREFIX = "Stop-";
  }
}
