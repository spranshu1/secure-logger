# Secure Logger #

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/62ebdf2938b147e982b62fe4bd393377)](https://www.codacy.com/gh/spranshu1/secure-logger/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=spranshu1/secure-logger&amp;utm_campaign=Badge_Grade) [![Maven Build](https://github.com/spranshu1/secure-logger/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/spranshu1/secure-logger/actions/workflows/build.yml)

* [Version](#markdown-header-version)
* [Summary](#markdown-header-summary)
* [Prerequisites](#markdown-header-prerequisites)
* [Usages](#markdown-header-usages)
* [Use MDC Instead Of ThreadContext](#markdown-header-use-mdc-instead-of-threadcontext)
* [Release Log](#markdown-header-release-log)
* [Author](#markdown-header-author)


## Version ##

`1.0.1`



## Summary ##

Aimed at masking the logs (hiding the confidential information like password) and providing the standardize log pattern. Allows changing the log level at runtime.



## Prerequisites ##

In order to use this library, below dependency needs to be included in consumer's pom.xml 

		<dependency>
			<groupId>com.github.spranshu1</groupId>
			<artifactId>secure-logger</artifactId>
			<version>x.x.x</version>
		</dependency>

Note: Change the version as per the release


## Usages ##

This library provides two basic feature.

1. Masking of logs 
2. Standard log patterns.

###  Masking of logs ###

This library provides the default log4j2.xml file containing the masking key.

Default pattern for password: 

Library has provided the default pattern for password which looks like as mentioned below. 

```
LogMaskingKeys=PASSWD

PASSWD.search=password=((.+?&)|(.+?,)|(.+?])|(.+?\\s))
PASSWD.replace=password=###########
```

Note: Above regrex pattern has been defined assuming that password is separated from other attributes either by '&' or ',' or ']' or blank space 

#### Customizing the default pattern ####

As a consumer you can add new pattern for different attributes in your application.properties file

For example for masking the account number and customer Id, you need to add below entries in your application.properties 

```
LogMaskingKeys=ACC_NUMBER,CUST_ID

ACC_NUMBER.search=accountNumber=((.+?,)|(.+?])|(.+?\\s))
ACC_NUMBER.replace=accountNumber=XXXXXXXX

CUST_ID.search=customerId=((.+?,)|(.+?])|(.+?\\s))
CUST_ID.replace=customerId=*********
```
Consumer can also override the default pattern provided by this library (password)

In order to override the password, provide your own pattern  and replacement string but with same key name (for password it has to be PASSWD) as used in library.

For example: 

```
LogMaskingKeys=PASSWD

PASSWD.search=password=((.+?,)|(.+?])|(.+?\\s)) 
PASSWD.replace=password=*********
```
Note: Here only the pattern '((.+?,)|(.+?])|(.+?\\s))' and replacement string '*********' is changed while other info (like attribute name and key name is same)
      If you are overriding the defualt pattern provided for password or adding the new pattern then it has to be added in application.properties file and not in application.yml.
	  However if you are using application.yml instead of application.properties file then masking keys along with 'search' and 'replace' pattern have to be added using System.setProperty.

For example:
		
```
System.setProperty("LogMaskingKeys", "PASSWD");
System.setProperty("PASSWD.search", "password=((.+?,)|(.+?])|(.+?\\s))");
System.setProperty("PASSWD.replace", "password=*********");
```
Note:In the above customization it has been assumed that attributes are separated from each other either by ',' or ']' or blank space.
     Kindly change the regex in 'search' string accordingly if your attributes are separated by some other character like '&' etc.

#### How to skip the masking for particular log statement in your java file ####

By default masking is enable if you have added this library as a dependency in your pom.xml .
However if you want to skip the masking for any particular log statement then use this SKIP MARKER in your statement as shown below'

```
private static final Marker SKIPMASKING = MaskLog.SKIP_MARKER;
LOGGER.info(SKIPMASKING,"some message");

```

Note: If consumer wants to provide there own log4j2.xml file then they can do so. However it is recommended not to override the default log pattern provided by this library.


## Use MDC Instead Of ThreadContext

Every developer is requested to use MDC (from slf4j) instead of ThreadContext (from log4j) as going forward applications will no longer be using log4j directly.

Below example shows the usages of MDC.

```
MDC.put("thread", threadName);
MDC.clear();
```
Some of the commonly used methods in MDC which are equivalent to ThreadContext methods are given below.

| MDC                         | ThreadContext                                                                                
|-----------------------------| ---------------------------
| clear()                     | clearAll()       
| put(String key, String val) | put(String key, String val)
| get(String key)             | get(String key)
| remove(String key)          |remove(String key)

---

## Release Log ##

1.0.1 Added support to mask logs in `Cloud Foundry` console

1.0.0  First Version


## Author ##

spranshu1
