// copy-pasta into browser, or pipe into cypher-shell:
// cat metimport-2-Artwork.cypher | cypher-shell -u [user] -p [password] --format plain > cypherscript.log 2>&1 &; tail -f cypherscript.log
// RUN metimport-1-Departments.cypher first, including CREATE EMPTY ARTWORKS
// You'll get about 100 to 200 per min, => 1.5 - 4 days

:param urlbase => 'https://collectionapi.metmuseum.org/public/collection/v1';
:param cleanFields => ["isPublicDomain","primaryImage","objectURL","additionalImages","isHighlight","medium","metadataDate","department","objectEndDate","objectBeginDate","repository","accessionNumber","creditLine","constituents"];
:param cleanValues => ['',null];

MATCH (a:Artwork)
WHERE NOT a.import = 1 AND a.import > 0
SET a.import = 0;

call apoc.periodic.commit(
  "
    MATCH (a:Artwork {import: 0})
    WITH $urlbase + '/objects/' + a.id AS url, a LIMIT 10
    CALL apoc.load.json(url, '', failOnError:false) YIELD value
    WITH a, apoc.map.clean(value, $cleanFields, $cleanValues) as artdata
      SET a += artdata
      SET a.import = 1
      RETURN count(a)
  ",
  {
    limit: 10,
    urlbase: $urlbase,
    cleanFields: $cleanFields,
    cleanValues: $cleanValues
  }
);

// anything not set, had a bad uri - we can retry them later, but best to avoid them for now
MATCH (a:Artwork {import: 0})
SET a.import = -1;

// find only artwork with images, and give the artwork a graph-name, for interacting via Browser
MATCH (a:Artwork {import: 1})
WHERE EXISTS(a.name)
SET a.origName = a.name;

MATCH (a:Artwork {import: 1})
WHERE EXISTS(a.primaryImageSmall)
WITH a, COALESCE (a.title, COALESCE(COALESCE(a.period, a.culture)+' '+a.objectName, a.objectName)) as n
SET a.name = n
SET a.import = 2;
// these will be the artworks we operate on.
// todo: script downloading and resizing images (can, should, this be done in this plugin!?)
