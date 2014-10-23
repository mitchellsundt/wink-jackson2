wink-jackson2
=============

Stripped-down Apache Wink 1.4 Client ( http://wink.apache.org/ ) for Android.
Consists of:
  wink-jackson2-common-1.4.jar
  wink-jackson2-client-1.4.jar

Strips out all dependencies on JAXB annotations, Beans and Spring framework.
Relies on Jackson 2.4+ annotations and message serializer for handling 
JSON interactions with a server. 

Retains the simple and flexible handler chain that Apache Wink defines.

Based off the Apache Wink 1.4 code tree (at the Apache 1.4 release tag)
