# AWS Pekko-Http SPI implementation 

[![Continuous Integration](https://github.com/pjfanning/aws-spi-pekko-http/actions/workflows/ci.yml/badge.svg)](https://github.com/pjfanning/aws-spi-pekko-http/actions/workflows/ci.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.pjfanning/aws-spi-pekko-http_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.pjfanning/aws-spi-pekko-http_2.13)
[![License](http://img.shields.io/:license-Apache%202-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

This library implements the provided [SPI](https://en.wikipedia.org/wiki/Service_provider_interface) for the asynchronous 
and non-blocking http calls in the new [AWS Java SDK](https://github.com/aws/aws-sdk-java-v2) with 
[Apache Pekko HTTP](https://github.com/apache/pekko-http).

This is an alternative implementation to the built-in netty-based async http engine in the AWS SDK. Use at your own risk.

If you are using Pekko HTTP 1.1 or Pekko Connectors 1.1, you should use

    "org.apache.pekko" %% "pekko-connectors-aws-spi-pekko-http" % "1.1.x"

This is a fork of [matsluni/aws-spi-akka-http](https://github.com/matsluni/aws-spi-akka-http).

## Usage

Create a dependency to this library by adding the following to your `build.sbt`:

    "com.github.pjfanning" %% "aws-spi-pekko-http" % "???"
    
or for Maven, the following to `pom.xml`:

```
<dependency>
    <groupId>com.github.pjfanning</groupId>
    <artifactId>aws-spi-pekko-http_2.12</artifactId>
    <version>???</version>
</dependency>
```

An example (in scala) from the test shows how to use pekko-http as the underlying http provider instead of netty.

```scala
val pekkoClient = new PekkoHttpAsyncHttpService().createAsyncHttpClientFactory().build()

val client = S3AsyncClient
              .builder()
              .credentialsProvider(ProfileCredentialsProvider.builder().build())
              .region(Region.EU_CENTRAL_1)
              .httpClient(pekkoClient)
              .build()

val eventualResponse = client.listBuckets()
```

If you connect to an AWS service from inside a corporate network, it may be necessary to configure a proxy. This can be achieved in the following way:

```scala
val system = ActorSystem("aws-pekko-http")

val proxyHost = "localhost"
val proxyPort = 8888

val httpsProxyTransport = ClientTransport.httpsProxy(InetSocketAddress.createUnresolved(proxyHost, proxyPort))

val settings = ConnectionPoolSettings(system)
  .withConnectionSettings(ClientConnectionSettings(system)
  .withTransport(httpsProxyTransport))

lazy val pekkoHttpClient = 
  PekkoHttpClient
    .builder()
    .withActorSystem(system)
    .withConnectionPoolSettings(settings)
    .build()
    
val client = S3AsyncClient
	.builder()
	.credentialsProvider(ProfileCredentialsProvider.builder().build())
	.region(Region.EU_CENTRAL_1)
	.httpClient(pekkoHttpClient)
	.build()
              
val eventualResponse = client.listBuckets()
```

When you use this library and a specific AWS service (e.g. S3, SQS, etc...) you may want to exclude the transitive
Netty dependency `netty-nio-client` like this:

```scala
libraryDependencies ++= Seq(
  "software.amazon.awssdk" % "s3" % "2.11.4" exclude("software.amazon.awssdk", "netty-nio-client")
)
```

If you not exclude the transitive dependency like shown above you have to explicitly declare which `httpClient` to use 
in the service client instantiation. If no client is explicitly set and multiple implementations for the `SdkAsyncHttpClient`
are found on the classpath, an exception like the following is thrown at runtime:

```java
software.amazon.awssdk.core.exception.SdkClientException: Multiple HTTP implementations were found on the classpath. 
    To avoid non-deterministic loading implementations, please explicitly provide an HTTP client via the client builders, 
    set the software.amazon.awssdk.http.async.service.impl system property with the FQCN of the HTTP service to use as the 
    default, or remove all but one HTTP implementation from the classpath
```

To further reduce the classpath it is also optional possible to exclude `"software.amazon.awssdk", "apache-client"`. 
This excludes an additional HttpClient, which comes as a transitive dependency with a AWS service.

There also exists an [mini example project](https://github.com/pjfanning/aws-spi-pekko-http-example) which shows the usage.
This example uses gradle and also shows how to exclude the netty dependency.

## Running the tests in this repo

In this repository there are unit tests and integration tests. The unit tests run against some local started aws 
services, which test some basic functionality and do not use real services from aws and cost no money. 

But, there are also integration tests which require **valid aws credentials** to run and running these tests is **not for free**. 
If you are not careful it can cost you money on aws. Be warned.

The integration tests look for aws credentials as either an environment variable, a system property or a credential file.
See the [here](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html) and 
[here](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html) in the aws documentation for details.

The tests can be run in `sbt` with:

    test
    
To run the integration tests

    it:test



# License
This library is Open Source and available under the Apache 2 License.
