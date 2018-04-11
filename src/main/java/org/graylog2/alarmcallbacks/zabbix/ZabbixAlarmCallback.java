/**
 * Copyright 2013-2014 TORCH GmbH, 2015 Graylog, Inc.
 *
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.alarmcallbacks.zabbix;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hengyunabc.zabbix.sender.DataObject;
import io.github.hengyunabc.zabbix.sender.SenderResult;
import io.github.hengyunabc.zabbix.sender.ZabbixSender;

public class ZabbixAlarmCallback implements AlarmCallback {
    private static final Logger LOG = LoggerFactory.getLogger(ZabbixAlarmCallback.class);

    private static final String NAME = "Zabbix trapper Alarm Callback";

    private static final String CK_ZABBIX_SERVER = "zabbix_server";
    private static final String CK_ZABBIX_PORT = "zabbix_port";
    private static final String CK_ZABBIX_HOST = "zabbix_host";
    private static final String CK_ZABBIX_KEY = "zabbix_key";
    private static final String[] MANDATORY_CONFIGURATION_KEYS = new String[]{
            CK_ZABBIX_SERVER, CK_ZABBIX_KEY, CK_ZABBIX_HOST
    };
    
    private Configuration configuration;

    @Override
    public void initialize(final Configuration config) throws AlarmCallbackConfigurationException {
        this.configuration = config;
    }

    @Override
    public void call(Stream stream, AlertCondition.CheckResult result) throws AlarmCallbackException {
        ZabbixSender zabbixSender = new ZabbixSender(configuration.getString(CK_ZABBIX_SERVER), Integer.parseInt(configuration
        .getString(CK_ZABBIX_PORT)));

        for (MessageSummary msg : result.getMatchingMessages()) {
            DataObject dataObject = new DataObject();
            dataObject.setHost(configuration.getString(CK_ZABBIX_HOST));
            dataObject.setKey(configuration.getString(CK_ZABBIX_KEY));
            dataObject.setValue(msg.getMessage());
            // TimeUnit is SECONDS.
            dataObject.setClock(msg.getTimestamp().getMillis()/1000);
            try {
                SenderResult res = zabbixSender.send(dataObject);
                if (!res.success()) {
                    LOG.warn(res.toString());
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }  
        }
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        final ConfigurationRequest cr = new ConfigurationRequest();

        cr.addField(new TextField(CK_ZABBIX_SERVER, "Zabbix server or proxy", "", "Zabbix server to send events to",
                ConfigurationField.Optional.NOT_OPTIONAL));
        cr.addField(new TextField(CK_ZABBIX_PORT, "Zabbix port", "10051", "Zabbix port for active checks",
                ConfigurationField.Optional.NOT_OPTIONAL));
        cr.addField(new TextField(CK_ZABBIX_HOST, "Zabbix host", "", "Zabbix configured host",
                ConfigurationField.Optional.NOT_OPTIONAL));    
        cr.addField(new TextField(CK_ZABBIX_KEY, "Zabbix item", "", "Zabbix item of trapper type",
                ConfigurationField.Optional.NOT_OPTIONAL));    
        return cr;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Maps.transformEntries(configuration.getSource(), new Maps.EntryTransformer<String, Object, Object>() {
            @Override
            public Object transformEntry(String key, Object value) {
                return value;
            }
        });
}
    @Override
    public void checkConfiguration() throws ConfigurationException {
        for (String key : MANDATORY_CONFIGURATION_KEYS) {
            if (!configuration.stringIsSet(key)) {
                throw new ConfigurationException(key + " is mandatory and must not be empty.");
            }
        }
    }

    public String getName() {
        return NAME;
    }

}