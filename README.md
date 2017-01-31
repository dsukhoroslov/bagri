## bagri project

### XML/Document DB on top of distributed cache.

Consists of the following Maven modules:

* bagri-core - Bagri API, common classes and utilities
* bagri-client - Bagri client side implementations
* bagri-server - Bagri server side implementations
* bagri-rest - Bagri REST server
* bagri-xqj - JSR225 implementation
* bagri-xquery - XQuery engine
* bagri-samples - sample apps, Bagri extensions
* bagri-test - Test Utilities, e.g. TPoX test suite runner, JSR225 TCK runner
* bagri-tools - Tools/Plugings, e.g. Bagri VisualVM Plugin
* bagri-distr - distributive package
* etc - additional libs, sample data, etc...

### License
Bagri is distributed under the Apache 2 License. 

### To start using it 
download Bagri distributive from the [release section](https://github.com/dsukhoroslov/bagri/releases/tag/untagged-168eb0f1f0bf37296c38)

Or, configure Maven dependencies in your project.
Cleint side dependencies:

```
<!-- Bagri XQJ driver -->
<dependency>
    <groupId>com.bagridb</groupId>
    <artifactId>bagri-xqj</artifactId>
    <version>${bagri.version}</version>
</dependency>

<!-- Bagri XDM API implementation -->
<dependency>
    <groupId>com.bagridb</groupId>
    <artifactId>bagri-client-hazelcast</artifactId>
    <version>${bagri.version}</version>
</dependency>
```

Server side dependencies:

```
<!-- Bagri REST module -->
<dependency>
    <groupId>com.bagridb</groupId>
    <artifactId>bagri-rest</artifactId>
    <version>${bagri.version}</version>
</dependency>

<!-- Bagri Server module -->
<dependency>
    <groupId>com.bagridb</groupId>
    <artifactId>bagri-server-hazelcast</artifactId>
    <version>${bagri.version}</version>
</dependency>
```

All project documentation and other details can be found on [Bagri Web site](http://bagridb.com)
To leave a feedback or question please visit [our forum](https://groups.google.com/forum/#!forum/bagridb)
