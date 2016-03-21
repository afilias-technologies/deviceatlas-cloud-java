package com.deviceatlas.cloud.deviceidentification.specs

import com.deviceatlas.cloud.deviceidentification.dsl.Data
import com.deviceatlas.cloud.deviceidentification.dsl.DeviceIdentificator
import spock.lang.*

@Ignore("""see https://developer.jboss.org/wiki/WhatsTheCauseOfThisExceptionJavalangClassFormatErrorAbsentCode""")
@Narrative("""
As a DA user
I want to identify devices
So that I can trust the tool
""")
@Unroll
class DeviceIdentificationScenariosSpec extends Specification {

    @Shared testData = Data.ingest('src/test/resources/device-identification-scenarios-data.yaml')

    def "identify specific scenarios accordingly"() {
        given: 'a user agent'
        String userAgent = yamlElem.'headers'.'user-agent'

        when: 'I identify it'
        Map<String, ?> deviceData = DeviceIdentificator.getDeviceDataFromUserAgent(userAgent)

        then: 'the data matches my expectations'
        yamlElem.'expected'.each { key, value ->
            assert deviceData.get(key) == value
        }

        where: 'the input data is defined in the yaml file'
        yamlElem    ||_
        testData[0] ||_
        testData[1] ||_
    }

}



