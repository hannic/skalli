/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.skalli.api.rest.config.ConfigResource;
import org.eclipse.skalli.common.configuration.ConfigKey;

public class GerritResource extends ConfigResource<ConfigKeyGerrit, GerritConfig> {

  @Override
  protected Class<GerritConfig> getConfigClass() {
    return GerritConfig.class;
  }

  @Override
  protected ConfigKeyGerrit[] getAllKeys() {
    return ConfigKeyGerrit.values();
  }

  @Override
  protected Map<ConfigKey, String> configToMap(GerritConfig configObject) {
    Map<ConfigKey, String> ret = new HashMap<ConfigKey, String>();
    ret.put(ConfigKeyGerrit.HOST, configObject.getHost());
    ret.put(ConfigKeyGerrit.PORT, configObject.getPort());
    ret.put(ConfigKeyGerrit.USER, configObject.getUser());
    ret.put(ConfigKeyGerrit.PASSPHRASE, configObject.getPassphrase());
    ret.put(ConfigKeyGerrit.PRIVATEKEY, configObject.getPrivateKey());
    ret.put(ConfigKeyGerrit.CONTACT, configObject.getContact());
    return ret;
  }

  @Override
  protected GerritConfig mapToConfig(Map<ConfigKeyGerrit, String> values) {
    GerritConfig ret = new GerritConfig();
    ret.setHost(values.get(ConfigKeyGerrit.HOST));
    ret.setPort(values.get(ConfigKeyGerrit.PORT));
    ret.setUser(values.get(ConfigKeyGerrit.USER));
    ret.setPassphrase(values.get(ConfigKeyGerrit.PASSPHRASE));
    ret.setPrivateKey(values.get(ConfigKeyGerrit.PRIVATEKEY));
    ret.setContact(values.get(ConfigKeyGerrit.CONTACT));
    return ret;
  }

}
