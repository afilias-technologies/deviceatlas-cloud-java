package com.deviceatlas.cloud.deviceidentification.dsl

import org.yaml.snakeyaml.Yaml

class Data {
    def static final ingest(String yamlFilePath) {
        def yamlData = new File(yamlFilePath).text
        new Yaml().load(yamlData)
    }
}
