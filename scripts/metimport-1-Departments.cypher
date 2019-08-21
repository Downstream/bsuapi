// copy-pasta into browser, or pipe into cypher-shell:
// cat metimport-1-Departments.cypher | cypher-shell -u [user] -p [password] --format plain > cypherscript.log 2>&1 &; tail -f cypherscript.log

CREATE INDEX ON :Source(name);
CREATE INDEX ON :Department(id);
CREATE INDEX ON :Artwork(id);
CREATE INDEX ON :Artist(name);
CREATE INDEX ON :Classification(name);
CREATE INDEX ON :Tag(name);
CREATE INDEX ON :Nation(name);

:param urlbase => 'https://collectionapi.metmuseum.org/public/collection/v1';
:param cleanFields => ["isPublicDomain","primaryImage","objectURL","additionalImages","isHighlight","medium","metadataDate","department","objectEndDate","objectBeginDate","repository","accessionNumber","creditLine","constituents"];
:param cleanValues => ['',null];

// SOURCE MET + DEPARTMENTS
MERGE (s:Source {name: 'MET'})
SET s.url = 'metmuseum.org'
SET s.api = 'https://collectionapi.metmuseum.org/public/collection/v1'
WITH s, $urlbase +"/departments" AS url
CALL apoc.load.json(url) YIELD value
UNWIND value.departments as dep
MERGE (a:Department {id: dep.departmentId})-[:SOURCE]->(s)
SET a.name=dep.displayName
;

MATCH (d:Department)-[:SOURCE]->(s)
RETURN s.name, d.id, d.name;

// CREATE EMPTY ARTWORKS (with id) FROM EACH DEPARTMENT'S LIST
// Due to API throttling, and one request per each of ~ VERY

// CALL apoc.periodic.iterate(
// "
//   MATCH (d:Department)
// 	WITH d, $urlbase + '/objects?departmentIds=' + d.id AS url
// 	CALL apoc.load.json(url) YIELD value
// 	UNWIND value.objectIDs as s
// 	RETURN s, d
// ","
//   CREATE (a:Artwork {id: s})
//   CREATE (d)<-[:DEPARTMENT]-(a)
// ",
//   {batchSize:500, iterateList:true, parallel:true, params: {urlbase: $urlbase}}
// )
// ;