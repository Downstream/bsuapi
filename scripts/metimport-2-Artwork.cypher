// copy-pasta into browser, or pipe into cypher-shell:
// cat metimport-2-Artwork.cypher | cypher-shell -u [user] -p [password] --format plain > cypherscript.log 2>&1 &; tail -f cypherscript.log
// RUN metimport-1-Departments.cypher first, including CREATE EMPTY ARTWORKS

:param urlbase => 'https://collectionapi.metmuseum.org/public/collection/v1';
:param cleanFields => ["isPublicDomain","primaryImage","objectURL","additionalImages","isHighlight","medium","metadataDate","department","objectEndDate","objectBeginDate","repository","accessionNumber","creditLine","constituents"];
:param cleanValues => ['',null];

MATCH (a:Artwork)
WHERE NOT a.import = 1 AND NOT a.import = 2
SET a.import = 0;

call apoc.periodic.commit(
  "
    MATCH (a:Artwork {import: 0})
    WITH $urlbase + '/objects/' + a.id AS url, a LIMIT 10
    CALL apoc.load.json(url) YIELD value
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