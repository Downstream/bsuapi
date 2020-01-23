CREATE INDEX ON :Source(name);
CREATE INDEX ON :Department(id);
CREATE INDEX ON :Artwork(id);
CREATE INDEX ON :Artist(name);
CREATE INDEX ON :Classification(name);
CREATE INDEX ON :Tag(name);
CREATE INDEX ON :Nation(name);
CREATE INDEX ON :Culture(name);
CREATE INDEX ON :Genre(name);
CREATE INDEX ON :Medium(name);
CALL db.index.fulltext.createNodeIndex("nameIndex",["Artist","Classification","Culture","Nation","Tag","Artwork","Genre","Medium"],["name"]);

//:param urlbase => 'http://mec402.boisestate.edu/cgi-bin/dataAccess/getAllAssets.py';

MERGE (i:Import {id: 1}) SET i.page = 0;

CALL apoc.periodic.commit("
MATCH (i:Import) SET i.page = (i.page + 1)
WITH i, $urlbase + '?type=0&ps=' + $perPage + '&p=' + i.page as url
CALL apoc.load.json(url) YIELD value
UNWIND value.data AS asset

WITH
  asset,
  count(value) as pageAssetsCount,
  count(asset.openpipe_canonical_smallImage) as imgCount,
  count(asset.openpipe_canonical_title) as titleCount,
  (i.page-1) * $perPage as skipCount
LIMIT 200
WHERE imgCount > 0 AND titleCount > 0

MERGE (a:Artwork {id: head(asset.metaDataId)})
  SET a.name = head(asset.openpipe_canonical_title)
  SET a.primaryImageSmall = head(asset.openpipe_canonical_smallImage)
  SET a.primaryImageLarge = head(asset.openpipe_canonical_largeImage)
  SET a.metaDataDate = head(asset.openpipe_canonical_metaData_Date)
  SET a.openpipe_artist = asset.openpipe_canonical_artist
  SET a.openpipe_culture = asset.openpipe_canonical_culture
  SET a.openpipe_nation = asset.openpipe_canonical_nation
  SET a.openpipe_classification = asset.openpipe_canonical_classification
  SET a.openpipe_genre = asset.openpipe_canonical_genre
  SET a.openpipe_medium = asset.openpipe_canonical_medium
  SET a.openpipe_tags = asset.openpipe_canonical_tags
  SET a.import = 0

RETURN pageAssetsCount + skipCount as count
"
,{limit: 10000, perPage: 100, urlbase: 'http://mec402.boisestate.edu/cgi-bin/dataAccess/getAllAssets.py' }
)


// TOPICS
// openpipe_artist
MATCH (x:Artwork {import: 0})
  WHERE exists(x.openpipe_artist)
WITH x, count(x.openpipe_artist) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_artist as entry
MERGE (t:Artist {name: entry})
MERGE (x)-[:BY]->(t)
SET t :Topic;

// openpipe_culture
MATCH (x:Artwork {import: 0})
  WHERE exists(x.openpipe_culture)
WITH x, count(x.openpipe_culture) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_culture as entry
MERGE (t:Culture {name: entry})
MERGE (x)-[:ART_CULTURE]->(t)
SET t :Topic;

// openpipe_nation
MATCH (x:Artwork {import: 0})
  WHERE exists(x.openpipe_nation)
WITH x, count(x.openpipe_nation) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_nation as entry
MERGE (t:Nation {name: entry})
MERGE (x)-[:ART_NATION]->(t)
SET t :Topic;

// openpipe_classification
MATCH (x:Artwork {import: 0})
  WHERE exists(x.openpipe_classification)
WITH x, count(x.openpipe_classification) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_classification as entry
MERGE (t:Classification {name: entry})
MERGE (x)-[:ART_CLASS]->(t)
SET t :Topic;

// openpipe_genre
MATCH (x:Artwork {import: 0})
  WHERE exists(x.openpipe_genre)
WITH x, count(x.openpipe_genre) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_genre as entry
MERGE (t:Genre {name: entry})
MERGE (x)-[:ART_GENRE]->(t)
SET t :Topic;

// openpipe_medium
MATCH (x:Artwork {import: 0})
  WHERE exists(x.openpipe_medium)
WITH x, count(x.openpipe_medium) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_medium as entry
MERGE (t:Medium {name: entry})
MERGE (x)-[:ART_MEDIUM]->(t)
SET t :Topic;

// openpipe_tags
MATCH (x:Artwork {import: 0})
  WHERE exists(x.openpipe_tags)
WITH x, count(x.openpipe_tags) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_tags as entry
MERGE (t:Tag {name: entry})
MERGE (x)-[:ART_TAG]->(t)
SET t :Topic;


// BRIDGE TOPICS
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

CALL apoc.periodic.iterate(
"
      MATCH (a:Genre)<--(:Artwork)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[:GENRE]-(b)
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Medium)<--(:Artwork)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[:Medium]-(b)
", {batchSize:10000, iterateList:true, parallel:false}
);

MATCH (a:Classification)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Artist)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Culture)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Nation)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Tag)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Genre)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Medium)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;

// FIND AND ATTACH REPRESNTATIVE IMAGES TO TOPICS
// via Topic.smallImage = Artwork.primaryImageSmall