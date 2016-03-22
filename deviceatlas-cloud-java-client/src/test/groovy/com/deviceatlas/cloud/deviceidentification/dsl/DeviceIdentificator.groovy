package com.deviceatlas.cloud.deviceidentification.dsl

import com.deviceatlas.cloud.deviceidentification.cacheprovider.SimpleCacheProvider
import com.deviceatlas.cloud.deviceidentification.client.Client

import static com.deviceatlas.cloud.deviceidentification.client.ClientConstants.KEY_PROPERTIES

class DeviceIdentificator {
    def static final Map<String, ?> getDeviceDataFromUserAgent(String userAgent) {
        final String licence  = System.getProperty("activeLicence")
        Client client = Client.getInstance(new SimpleCacheProvider())
        client.setLicenceKey(licence)
        (Map<String, ?>) client.getDeviceDataByUserAgent(userAgent).get(KEY_PROPERTIES.toString())
    }
}
