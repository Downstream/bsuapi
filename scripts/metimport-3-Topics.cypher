// copy-pasta into browser, or pipe into cypher-shell:
// cat metimport-3-Topics.cypher | cypher-shell -u [user] -p [password] --format plain > cypherscript.log 2>&1 &; tail -f cypherscript.log
// RUN metimport-1-Departments.cypher AND metimport-2-Artwork.cypher first, including CREATE EMPTY ARTWORKS

// Initial build doesn't attach artwork to source, but design requires it (to attribute data and images to The Met)
MATCH (s:Source), (x:Artwork {import: 2})
MERGE (s)<-[:SOURCE]-(a);

// Artist
MATCH (x:Artwork {import: 2})
WHERE exists(x.artistDisplayName)
MERGE (a:Artist {name: x.artistDisplayName})
MERGE (x)-[:BY]->(a)
SET a :Topic;

// Classification
MATCH (x:Artwork {import: 2})
WHERE exists(x.classification)
MERGE (c:Classification {name: x.classification})
MERGE (x)-[:ART_CLASS]->(c)
SET c :Topic;

// Nation  todo: this one is squirly, and we should be focusing on culture first
// We're building by artistNationality AND country, because The MET
MATCH (x:Artwork {import: 2})
WHERE exists(x.country)
MERGE (n:Nation {name: x.country})
MERGE (x)-[:ART_NATION]->(n)
SET n :Topic;

MATCH (x:Artwork {import: 2})
WHERE exists(x.artistNationality)
MERGE (n:Nation {name: x.artistNationality})
MERGE (x)-[:ART_NATION]->(n)
SET n :Topic;

// Tags (subject (usually))
MATCH (x:Artwork {import: 2})
WHERE exists(x.tags)
WITH x, count(x.tags) as cnt
  WHERE cnt > 0
UNWIND x.tags as tagname
MERGE (t:Tag {name: tagname})
MERGE (x)-[:ART_TAG]->(t)
SET t :Topic;

// Culture (for the Met, it mostly lines up with Artwork.country
MATCH (x:Artwork {import: 2})
  WHERE exists(x.culture)
UNWIND split(x.culture, ',') as cult
MERGE (c:Culture {name: cult})
MERGE (x)-[:ART_CULTURE]->(c)
SET c :Topic;



// Topic (Metamer) Relationships bypassing Artworks
// E.G.: connections between Artist and Tags
CALL apoc.periodic.iterate(
"
      MATCH (a:Artist)<--(:Artwork)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[:ARTIST]-(b)
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Classification)<--(:Artwork)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[:CLASS]-(b)
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Nation)<--(:Artwork)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[:NATION]-(b)
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Culture)<--(:Artwork)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[:CULTURE]-(b)
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Tag)<--(:Artwork)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[:TAG]-(b)
", {batchSize:10000, iterateList:true, parallel:false}
);

MATCH (a:Classification)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c SET a :Topic;
MATCH (a:Artist)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c SET a :Topic;
MATCH (a:Culture)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c SET a :Topic;
MATCH (a:Nation)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c SET a :Topic;
MATCH (a:Tag)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c SET a :Topic;