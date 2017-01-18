Transaction Processing over XML (TPoX) is an application-level XML database benchmark based on a 
financial application scenario.

See TPoX project at: http://tpox.sourceforge.net/

To run TPoX benchmark download test data from http://sourceforge.net/projects/tpox/files/ then 
install it and set TPOX_HOME variable in the set-tpox-env.cmd file. Set proper Bagri DB host and port
via bdb.schema.members java option. By default it s set as

-Dbdb.schema.members=localhost:10500

To learn more about TPoX settings go to http://sourceforge.net/p/tpox/code/HEAD/tree/TPoX21/documentation/.
Or, check downloaded documents at $TPOX_HOME/documentation and $TPOX_HOME/WorkloadDriver/README.TXT

- tpox-xqj-custacc/tpox-xqj-orders/tpox-xqj-sec scripts are for XQJ (JSR225) interface performance tests. 

Client-side (TPOX) statistics are collected in ./output folder. 
Server-side statistics are collected in stats.txt file.