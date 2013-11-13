// export CLASSPATH=`ls -1 *.jar | tr '\n' ':'`

import java.text.SimpleDateFormat;
import java.util.Date;
import groovy.util.slurpersupport.NodeChildren;

import org.bridgedb.IDMapperException;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.rdb.construct.DBConnector;
import org.bridgedb.rdb.construct.DataDerby;
import org.bridgedb.rdb.construct.GdbConstruct;
import org.bridgedb.rdb.construct.GdbConstructImpl3;

import org.jsoup.Jsoup;

GdbConstruct database = GdbConstructImpl3.createInstance(
  "lyco_metabolites", new DataDerby(), DBConnector.PROP_RECREATE
);
database.createGdbTables();
database.preInsert();

//inchiDS = DataSource.register ("Cin", "InChI").asDataSource()
//inchikeyDS = DataSource.register ("Cik", "InChIKey").asDataSource()
chemspiderDS = DataSource.register ("Cs", "Chemspider").asDataSource()
lycocycDS = DataSource.register ("Clc", "LycoCyc").asDataSource()
casDS = BioDataSource.CAS
pubchemDS = BioDataSource.PUBCHEM_COMPOUND
chebiDS = BioDataSource.CHEBI
keggDS = BioDataSource.KEGG_COMPOUND
// drugbankDS = BioDataSource.DRUGBANK
wikipediaDS = BioDataSource.WIKIPEDIA

String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
database.setInfo("BUILDDATE", dateStr);
database.setInfo("DATASOURCENAME", "LycoCyc");
database.setInfo("DATASOURCEVERSION", "metabolites_" + dateStr);
database.setInfo("DATATYPE", "Metabolite");
database.setInfo("SERIES", "standard_metabolite");

def addXRef(GdbConstruct database, Xref ref, String node, DataSource source) {
   id = node.trim()
   println "id: $id"
   if (id.length() > 0) {
     ref2 = new Xref(id, source);
     if (database.addGene(ref2)) println "Error (addGene): " + database.getErrorMessage()
     if (database.addLink(ref, ref2)) println "Error (addLink): " + database.getErrorMessage()
   }
}

def addAttribute(GdbConstruct database, Xref ref, String key, String value) {
   id = value.trim()
   println "attrib($key): $id"
   if (id.length() > 255) {
     println "Warn: attribute does not fit the Derby SQL schema: $id"
   } else if (id.length() > 0) {
     if (database.addAttribute(ref, key, value)) println "Error (addAttrib): " + database.getErrorMessage()
   }
}

def cleanKey(String inchikey) {
   String cleanKey = inchikey.trim()
   if (cleanKey.startsWith("InChIKey=")) cleanKey = cleanKey.substring(9)
   cleanKey
}

// load the LycoCyc content
counter = 0
// load the names
def chebiNames = new File('lycocyc/3.3/data/compounds.dat')
def Xref ref;
chebiNames.eachLine { line->
  if (!line.startsWith("#") && // comment line
      !line.startsWith("/")) { // continuation of a field
    columns = line.split(" - ")
    def field = columns[0]
    def value = columns[1]
    if (field == "UNIQUE-ID") {
      // reset all content
      ref = new Xref(value, lycocycDS);
      error = database.addGene(ref);
      error += database.addLink(ref,ref);
      println "key:" + field
    } else if (field == "COMMON-NAME" || field == "SYNONYMS") {
      println "key:" + field
      println "val: " + value
      value = Jsoup.parseBodyFragment(value).text()
      addAttribute(database, ref, "Synonym", value);
    } else if (field == "DBLINKS") {
      println "key:" + field
      println "val: " + value
      try {
        parts = value.split("\"")
        db = parts[0].trim()
        value = parts[1].trim()
        println "db: " + db
        println "val: " + value
        if (db == "(LIGAND-CPD") {
          println "KEGG ID: " + value
          addXRef(database, ref, value, BioDataSource.KEGG_COMPOUND);
	} else if (db == "(CAS") {
          println "CAS ID: " + value
          addXRef(database, ref, value, BioDataSource.CAS);
	} else if (db == "(CHEBI") {
          println "ChEBI ID: " + value
          addXRef(database, ref, value, BioDataSource.CHEBI);
	} else if (db == "(PUBCHEM") {
          println "PubChem Compound ID: " + value
          addXRef(database, ref, value, BioDataSource.PUBCHEM_COMPOUND);
        }
      } catch (Exception e) {
	println "ERROR: " + e.message
      } // just skip it
    } else {
      //println "key:" + field
    }
  }
}

pubchemDS = BioDataSource.PUBCHEM_COMPOUND
chebiDS = BioDataSource.CHEBI

database.commit();
database.finalize();
