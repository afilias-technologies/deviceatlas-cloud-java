# DeviceAtlas Cloud #

Welcome to the DeviceAtlas Cloud Java code repository.


## Cloud Free Device Identification ##

An easy and reliable way to identify device type (mobile phone, tablet, desktop, TV, etc.) and other device properties.  

### Data points provided ###

The following data points are available in the free version:

- Hardware Type
- OS Name
- OS Version
- Browser Name
- Browser Version
- Browser Rendering Engine

If you need [additional device properties](https://deviceatlas.com/resources/available-properties) or higher usage limits, you can easily upgrade to our paid Cloud or locally deployed versions. Please visit [Pricing and Trial](https://deviceatlas.com/pricing-and-trial) for more details.


## Get Started ##

1. Checkout the source code
2. Run Gradle (gradle build) or Maven (mvn package)
3. Get your [Free DeviceAtlas Cloud](https://deviceatlas.com/cloud-free-signup) licence


## Simple to Use ##

### Initialization ###

```java
Client client = Client.getInstance(new EhCacheCacheProvider());
client.setLicenceKey(licenceKey);
```

### Usage ###

```
Result result = client.getResult(request);
Properties properties = result.getProperties();
```


## Documentation ##

See [Cloud Client API](deviceatlas-cloud-client/README.md) readme for further doumentation.


## About DeviceAtlas ##

DeviceAtlas is the world’s leading device detection solution providing data on all mobile and connected devices including smartphones, tablets, laptops, and wearable devices. You can use device detection to analyse web traffic, redirect or adapt content for mobile audiences, or to target specific mobile devices.

Find more at https://deviceatlas.com/

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

_Copyright (c) 2008-2016 by Afilias Technologies Limited. All rights reserved. https://deviceatlas.com_
