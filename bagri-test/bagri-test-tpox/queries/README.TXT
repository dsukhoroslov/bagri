--------------------------------------------------------------------------------
--  (C) Copyright IBM Corp. 2011
--
--  This program is made available under the terms of the Common Public 
--  License 1.0 as published by the Open Source Initiative (OSI). 
--  http://www.opensource.org/licenses/cpl1.0.php
--------------------------------------------------------------------------------

This directory contains workload property files used by the workload driver.
This set of workload property files are for 1GB TPoX data scale (XXS). The 
1GB TPoX data has been pregenerated for user's convenience and can be downloaded
from https://sourceforge.net/projects/tpox/files/

A workload property file defines a set of transactions (queries, inserts,
updates, deletes) run as a workload. Transactions may have slightly different
syntax for each database system. Hence the property files reference the
transaction templates for a specific database (currently DB2). 

The paths to the transaction templates are *relative* to where the workload 
driver is being run from (recommended to be TPoX/$DBS or TPoX/WorkloadDriver).

For how to make workload property files for other TPoX data scale, please refer
the following documentation:

TPoX/documentation/WorkloadDriverUsage_v2.1.pdf
TPoX/documentation/TPoX_DataGeneration_v2.1.pdf