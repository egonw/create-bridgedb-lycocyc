Create BridgeDB Identity Mapping files for LycoCyc
==================================================

License
-------

This repository: New BSD.

Derby License -> http://db.apache.org/derby/license.html
BridgeDB License -> http://www.bridgedb.org/browser/trunk/LICENSE-2.0.txt

Run the script
--------------

1. add the jars to your classpath, e.g. on Linux with:

  export CLASSPATH=\`ls -1 *.jar | tr '\n' ':'\`

2. save the file

3. run the script with Groovy:

  groovy lyco2derby.groovy

4. open the file in PathVisio

References
----------

1. http://svn.bigcat.unimaas.nl/bridgedbcreator/trunk/src/org/bridgedb/creator/
2. http://bridgedb.org/
3. ftp://ftp.sgn.cornell.edu/pathways/
