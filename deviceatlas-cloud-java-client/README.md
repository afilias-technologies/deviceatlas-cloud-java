# DeviceAtlas Cloud Client API #

DeviceAtlas Cloud is a web service that returns device information such
as screen width, screen height, device type, vendor, model name, etc.

Client API provides an easy way to query DeviceAtlas Cloud, cache the results
locally and has automatic failover should one of the global DeviceAtlas Cloud
endpoints become unavailable. As of version 1.2, the Client API is able to
leverage data collected by the DeviceAtlas Client-side Component. This data
is sent to DeviceAtlas Cloud to augment the data found from the normal HTTP
headers and also allows the detection of the different iPhone and iPad models. 

To see a full list of properties, please visit 
[Available Properties](https://deviceatlas.com/resources/available-properties) at deviceatlas.com.


## Dependencies ##

### Java version ###
The API requires at least Java 6. The methods 
`getResult(HttpServletRequest request)` and `getDeviceData(HttpServletRequest request)`
must only be used inside a servlet container. The other getResult / getDeviceData methods
may be used outside a servlet container and do not require the JavaEE libraries on the classpath.

### Cache providers ###
Client API requires a cache provider to cache device data and the
cloud end-points list. By default, the API uses Ehcache. The API also comes with 
a number of other alternative providers: Memcached, FileCacheProvider and 
SimpleCacheProvider. The API includes a cache provider interface to allow for custom 
caching solutions.


## Configuration ##

### Runtime ###

The only required setting is your DeviceAtlas licence key.

### Caching ###

The API provides a number of cache providers and each has a configuration file 
located in the Api/config directory. The configuration file for the selected 
cache provider must be available on the classpath.

## Simple to Use ##

### Basic Usage ###

*When you initialize your application, you need to setup a cache provider as follow:*

```java
Client client = Client.getInstance(new EhCacheCacheProvider());
client.setLicenceKey(licenceKey);
``` 

*Then you can use the API instance from different parts of your code. In this case in
a servlet container with a HttpServletRequest object.*

```java
Client client = Client.getInstance();
Result result = client.getResult(request);
Properties properties = result.getProperties();
``` 

*Imports*

```java
import com.deviceatlas.cloud.deviceidentification.client.Client;
import com.deviceatlas.cloud.deviceidentification.client.Result;
import com.deviceatlas.cloud.deviceidentification.client.Properties;
import com.deviceatlas.cloud.deviceidentification.client.ClientException;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.CacheException;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.CacheProvider;
import com.deviceatlas.cloud.deviceidentification.cacheprovider.EhCacheCacheProvider;
```

### Multiple HTTP headers ###

```java
Map<String, String> headers = new HashMap<String, String>();
headers.put("user-agent", "THE USER AGENT ...");
headers.put("accept-language", "da, en-gb;q=0.8, en;q=0.7");

Result result = client.getResultByHeaders(headers);
Properties properties = result.getProperties();
``` 

### User-Agent string only ###

```java
String userAgent = "THE USER AGENT ...";

Result result = client.getResultByUserAgent(userAgent);
Properties properties = result.getProperties();
``` 

### Exceptions ###

```java
try {

    Client client = Client.getInstance();
    Result result = client.getResult(request);
    Properties properties = result.getProperties();

} catch (ClientException ex) {
    /* handle the errors */

} catch (CacheException ex) {
    /* handle the errors */
}
```

### Properties Usage ###
    
```java
/* example 1: Get the screen width for image optimization */
int displayWidth = properties.containsKey("displayWidth") ? properties.get("displayWidth").asInteger() : 100;

/* example 2: Get the device vendor name */
String vendor = properties.containsKey("vendor") ? properties.get("vendor").asString(): "";

/* example 3: Touch screen optimization */
boolean useBiggerIcons = properties.containsKey("touchScreen") ? properties.get("touchScreen").asBoolean() : false;

/* example 4: Send Geo Location JS to client? */
boolean supportsGeoLocation = properties.containsKey("js.geoLocation") ?	properties.get("js.geoLocation").asBoolean() : false;
```

Before 
accessing a property always check if it exists in the set or not.

To see a full list of properties, please visit 
[Available Properties](https://deviceatlas.com/resources/available-properties) at deviceatlas.com.


## Proxy usage ##
The API may also route via a proxy by configuring the API with a `java.net.Proxy`
object. The following code example shows how to pass a Proxy instance to the API.

```java
Client client = Client.getInstance();
client.setLicenceKey(licenceKey);
client.setProxy(proxy);
```

## Examples ##
The package contains a number of examples to demonstrate the API features, usage 
and some possible use cases. They are split into two:

 1. Basic command line example to show basic usage.
 2. A Spring Boot application to show more complete examples of sample use cases.

The examples can be compiled with either Maven (3.x version) or Gradle.

### Command line example ###
The command line example shows basic usage of the API and can be build and run
as follows:

*Gradle*

```shell
% <path to gradle>/gradle build
% java -Dlicencekey="<your licence key>" -jar build/libs/device-identification-cloud-example-cli-0.1.0.jar
```

*Maven*

```shell
% mvn package
% java -Dlicencekey="<your licence key>" -jar target/device-identification-cloud-example-cli-0.1.0.jar
```

The slf4j api library and logger implementation libraries (like logback) are
needed on the classpath.

### Web application example ###
The Web application example uses the Spring Boot framework and embeds a web
server in the final Jar to allow for easy execution on the command line and 
avoiding the need to deploy within an application server.

*Gradle*

```shell
% <path to gradle>/gradle build
% java -Dlicencekey="<your licence key>" -jar build/libs/device-identification-cloud-example-web-0.1.0.jar
```

*Maven*

```shell
% mvn package spring-boot:repackage
% java -Dlicencekey="<your licence key>" -jar target/device-identification-cloud-example-web-0.1.0.jar
```

The example can be viewed via a web browser at `http://localhost:8080/`

The homepage of the example application links to a number of use-cases:

#### Redirection ####
This web example uses the API to get properties for the current request and then
uses some basic property values to decide which website provides the most suitable
content for the device making the request.

#### Content Adaptation ####
This web example uses the API to get properties for the device making the current
request and then uses some basic property values to choose a suitable template to
wrap around the content.

#### Analytics ####
This web example uses the API to get properties for user-agents from a given list.
Some properties such as vendor, browser name and device type are aggregated and
the results are displayed as graphs and numbers.

#### Content Targeting ####
This example uses the API to detect the device and use some of its properties to
show certain advertisements and download links which may be related or of interest
to the user, considering his/her device.

Note that in the web examples which use the API, the client side properties are
taken into account automatically by the API if the cookie exists from the browser.
This means if the cookie already exists within your browser you will still see
the client side properties in the result even when the DeviceAtlas client side
component is not added to the page. You can delete the cookie manually to see the
differences between the results from examples which use the client side component
and those that don't.

## Caching ##
The API can cache the returned data after a call to the DeviceAtlas Cloud service,
this speeds up subsequent requests as it avoid a network request round trip.

The client API provides several cache solutions. It is recommended to always
use the cache if possible.

The cache can be configured by putting the relevant config file in
the classpath with the necessary settings. Sample config files can be found in
the "Api/config" directory.

If Ehcache or Memcached are used, they must be be shutdown properly to ensure that 
the memory cache gets written to disk. This can be done in a number of ways:

1. Call `client.shutdown()` in the DeviceAtlas client class.

2. A shutdown hook can be registered by setting a system property.

    `net.sf.ehcache.enableShutdownHook = true`


To get and set the disk cache path, the diskstore path of the XML configuration
file can be checked.

```xml
<diskStore path="<path>" />
```

For more information please visit http://ehcache.org/documentation

## Client-side Component ##
In addition to the properties from the user-agent detection, properties can be
gathered from the client's browser and used both on the client side and on the
server side.

### Usage with Client-side Component ###
The "deviceatlas.min.js" file must be included on your webpage in order for it 
to detect the client side properties. This script gathers the properties and
creates a cookie containing them. This cookie is sent to the server on the next
request. Both client side and server side properties are merged and additional
logic is used to determine other properties such iPhone and iPad models which 
are normally not detectable.

By default, if the cookie exists it will be used by the API. To disable using
the client side cookie:

```java
client.setUseClientCookie(false);
```

Please see [Client-side readme](../ExtraTools/ClientSideProperties/README.md)
for more details.

## Cloud Service End-points ##

The DeviceAtlas Cloud Service is powered by independent clusters of servers
spread around the world. This ensures optimum speed and reliability. The API is
able to automatically switch to a different end-point if the current end-point
becomes unavailable. It can also (optionally) auto-rank all of the service
end-points to choose the end-point with the lowest latency for your location.

Cloud service provider endpoints are defined as an array of "EndPoint" objects.
Class "Server" exists in the "com.deviceatlas.cloud.deviceidentification" package.
A default Server array is built in the API but you can manually set:

```java
EndPoint[] endpoints = {
    new EndPoint("SERVER-HOST-ADDRESS", SERVER-PORT),
    new EndPoint("SERVER-HOST-ADDRESS", SERVER-PORT)
};

client = Client.getInstance();
client.setEndPoints(endpoints);
```

By default the API will analyze the end-points from time to time to rank them by 
their stability and response speed. The ranked list is then cached and used
whenever the Client API needs to query the DeviceAtlas Cloud Service. If an end-
point fails, the Client API will automatically switch to the next end-point on 
the list.

There is no need to set the servers array if auto-ranking is turned on. If you
wish, you may re-order the array and turn auto-ranking off. In this case the API
will respect your preferred order of end-points and only switch to a different
end-point should the primary one fail to resolve.

Please refer to [Cloud Service End-Points](https://deviceatlas.com/resources/cloud-service-end-points)
for a list of active regions.

### Notes ###

* With the default auto-ranking settings, the ranking is done every 24 hours.
   The actual time may be more than 24 hours as the ranking is only triggered by
   a request to the Client API and the cached server list is older than value set
   by client.setAutoServerRankListLifetime(1440).

* During end-point analysis a number of requests are made to each end-point.
   Please note that these requests count towards your total hits to
   the Cloud service.

   e.g:
   
    ```
    if
        EndPoint list contains 3 endpoints
        AUTO SERVER RANKING LIFETIME = 1440
        AUTO SERVER RANKING NUM REQUESTS = 3
    then
        auto ranking will add 9 (3x3) hits per day
    ```
    
### Methods ###

#### Get the ranked server list ####

```java
EndPoint[] rankedServerList = client.getEndPoints();
```

The first end-point in the list will be used to make a request to the cloud, if
it fails the next end-point will be take it's place.

#### Get the end-point used for the last request ####

```java
EndPoint endpoint = client.getCloudUrl();
```
	
Note that if the data comes from cache this method will return "null".

#### Get end-point info ####

```java
EndPoint[] endpoints = client.getServersLatencies();
```

This is useful when you want to manually rank the server list.

### Cloud Server end-point settings ###

#### client.setAutoServerRanking(true) ####

To turn auto ranking on/off. To manually rank the servers set to "false"
and edit the SERVERS list to set your preferred order of end-points. 
The API will not rank the servers and will use the SERVERS list items 
directly with the topmost server used first to get device data. On fail-
over the next end-point in the list will be used.

#### client.setCloudServiceTimeout(2) ####

Time in seconds. If an end-point fails to respond in this amount of time 
the API will fail-over to the next end-point on the list.

#### client.setAutoServerRankingMaxFailures(1) ####

When auto ranking servers, if a server fails more than this number of
times it will not be included in the list.

#### client.setAutoServerRankingNumRequests(3) ####

When auto ranking servers, number of requests to perform for service 
speed calculation.

#### client.setServerRankingLifetime(1440) ####

In the case of auto ranking, it is the time in minutes of how often
to auto rank servers.

0 = servers will be ranked and cached only once and this list will not
be updated automatically. You can update this list manually if needed:

```java
DeviceAtlasCloudClient.rankServers();
```

In the case of manual ranking, specifies how long to use the fail-over
endpoints before the preferred end-point is re-checked. If the preferred
end-point is available it will be added back into the list of end-points
and used for future requests.

## Extra Tools ##

This package comes with extra tools that can help you enhance your mobile websites.

### DeviceAtlas Client-side Component ###

This is the DeviceAtlas Client-side Component which discovers device info on
client side to augment the server data. This library is used in the DeviceAtlas
Cloud Client API examples.

### Latency Checker ###

This tool can be used to get info about the DeviceAtlas Cloud Service end-points.
The info shown can be used for manually setting up the DeviceAtlas Clous service
end-points in the API. To access the cloud servers a valid DeviceAtlas licence
would be required.

Usage (command line):

```shell
java -cp ./Api/config:./Api/deviceatlas-cloud-java-client-VERSION.jar com.deviceatlas.cloud.deviceidentification.tools.LatencyChecker LICENCE-KEY
```

*Windows*

```shell
java -cp ./Api/config;./Api/deviceatlas-cloud-java-client-VERSION.jar com.deviceatlas.cloud.deviceidentification.tools.LatencyChecker LICENCE-KEY
```


## Support ##

Please contact <support@deviceatlas.com> if you have any queries.

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

_Copyright (c) 2008-2016 by Afilias Technologies Limited. All rights reserved. https://deviceatlas.com_
