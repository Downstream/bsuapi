MERGE (api:OpenPipeConfig {name: 'api'}) SET api.assetPage = 0;

/* To copy ALL data from OpenPipe */
//MERGE (a:Asset {id: bsuapi.coll.singleClean(asset.metaDataId)})
//ON CREATE SET a = asset, a.id = bsuapi.coll.singleClean(asset.metaDataId)
//ON MATCH SET a = asset, a.id = bsuapi.coll.singleClean(asset.metaDataId), a.reflowTopics = true

CALL apoc.periodic.commit("
MATCH (canon:OpenPipeConfig {name: 'canonical'})
MATCH (fields:OpenPipeConfig {name: 'topicFields'})
MATCH (api:OpenPipeConfig {name: 'api'}) SET api.assetPage = (api.assetPage + 1)
WITH canon, fields, api.allAssets + '?type=1&ps=20&p=' + api.assetPage as url
CALL apoc.load.json(url) YIELD value
  UNWIND value.data AS asset

WITH
  canon, fields, asset, value.total as pageAssetsCount
LIMIT {limit}
WHERE
  asset.openpipe_canonical_title[0] <> ''

MERGE (a:Asset {id: bsuapi.coll.singleClean(asset.metaDataId)})

SET a.name = bsuapi.coll.singleClean(asset.openpipe_canonical_title)
SET a.primaryImageSmall = bsuapi.coll.singleClean(asset.openpipe_canonical_smallImage)
SET a.primaryImageSmallDimensions = bsuapi.coll.singleClean(asset.openpipe_canonical_smallImageDimensions)
SET a.primaryImageLarge = bsuapi.coll.singleClean(asset.openpipe_canonical_largeImage)
SET a.primaryImageLargeDimensions = bsuapi.coll.singleClean(asset.openpipe_canonical_largeImageDimensions)
SET a.primaryImageFull = bsuapi.coll.singleClean(asset.openpipe_canonical_fullImage)
SET a.primaryImageFullDimensions = bsuapi.coll.singleClean(asset.openpipe_canonical_fullImageDimensions)

SET a.openpipe_latitude = bsuapi.coll.singleClean(asset.openpipe_canonical_latitude)
SET a.openpipe_longitude = bsuapi.coll.singleClean(asset.openpipe_canonical_longitude)
SET a.hasGeo = EXISTS(a.openpipe_latitude) AND EXISTS(a.openpipe_longitude)
SET a.openpipe_date = bsuapi.coll.singleCleanList(asset.openpipe_canonical_date,[canon.openpipe_canonical_date])

SET a.import = 0

WITH a, canon, fields, pageAssetsCount, asset
  UNWIND [x in keys(fields) WHERE x<>'name'] as prop
    WITH a, prop, pageAssetsCount, bsuapi.coll.cleanOfList(asset[fields[prop]], [canon[fields[prop]]]) as val
    CALL apoc.create.setProperty(a, prop, val) YIELD node AS unusedProcResult

RETURN pageAssetsCount
"
,{limit: 1000}
);


// TOPICS
// openpipe_artist
MATCH (x:Asset {import: 0})
  WHERE exists(x.openpipe_artist)
WITH x, count(x.openpipe_artist) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_artist as entry
MERGE (t:Artist {name: entry})
MERGE (x)-[:BY]->(t)
SET t :Topic;

// openpipe_culture
MATCH (x:Asset {import: 0})
  WHERE exists(x.openpipe_culture)
WITH x, count(x.openpipe_culture) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_culture as entry
MERGE (t:Culture {name: entry})
MERGE (x)-[:ASSET_CULTURE]->(t)
SET t :Topic;

// openpipe_genre
MATCH (x:Asset {import: 0})
  WHERE exists(x.openpipe_genre)
WITH x, count(x.openpipe_genre) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_genre as entry
MERGE (t:Genre {name: entry})
MERGE (x)-[:ASSET_GENRE]->(t)
SET t :Topic;

// openpipe_medium
MATCH (x:Asset {import: 0})
  WHERE exists(x.openpipe_medium)
WITH x, count(x.openpipe_medium) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_medium as entry
MERGE (t:Medium {name: entry})
MERGE (x)-[:ASSET_MEDIUM]->(t)
SET t :Topic;

// openpipe_nation
MATCH (x:Asset {import: 0})
  WHERE exists(x.openpipe_nation)
WITH x, count(x.openpipe_nation) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_nation as entry
MERGE (t:Nation {name: entry})
MERGE (x)-[:ASSET_NATION]->(t)
SET t :Topic;

// openpipe_classification
MATCH (x:Asset {import: 0})
  WHERE exists(x.openpipe_classification)
WITH x, count(x.openpipe_classification) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_classification as entry
MERGE (t:Classification {name: entry})
MERGE (x)-[:ASSET_CLASS]->(t)
SET t :Topic;

// openpipe_city
MATCH (x:Asset {import: 0})
  WHERE exists(x.openpipe_city)
WITH x, count(x.openpipe_city) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_city as entry
MERGE (t:City {name: entry})
MERGE (x)-[:ASSET_CITY]->(t)
SET t :Topic;

// openpipe_tags
MATCH (x:Asset {import: 0})
  WHERE exists(x.openpipe_tags)
WITH x, count(x.openpipe_tags) as cnt
  WHERE cnt > 0
UNWIND x.openpipe_tags as entry
MERGE (t:Tag {name: entry})
MERGE (x)-[:ASSET_TAG]->(t)
SET t :Topic;


// BRIDGE TOPICS
CALL apoc.periodic.iterate(
"
      MATCH (a:Artist)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:ARTIST]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Culture)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:CULTURE]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Genre)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:GENRE]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Medium)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:Medium]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Nation)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:NATION]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
);


CALL apoc.periodic.iterate(
"
      MATCH (a:Classification)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:CLASS]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
);


CALL apoc.periodic.iterate(
"
      MATCH (a:City)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:CITY]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
);

CALL apoc.periodic.iterate(
"
      MATCH (a:Tag)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:TAG]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
);

MATCH (a:Artist)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Culture)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Genre)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Medium)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Nation)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Classification)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:City)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
MATCH (a:Tag)<-[r]-(:Artwork) WITH a, count(r) as c SET a.artCount = c;
