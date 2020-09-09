MATCH (api:OpenPipeConfig {name: 'api'})
SET api.assetPage = 0
SET api.thisRun = date()
RETURN "RESET api counts to prepare for new sync." as t
;

CALL apoc.periodic.commit("
MATCH (script:Script {name: 'OPENPIPE_SYNC'})
MATCH (canon:OpenPipeConfig {name: 'canonical'})
MATCH (fields:OpenPipeConfig {name: 'topicFields'})
MATCH (api:OpenPipeConfig {name: 'api'})
SET api.assetPage = (api.assetPage + 1)
SET script.page = api.assetPage
WITH canon, fields, api.singleAsset as assetGuidBase, api.allAssets + '?changeStart=' + api.lastRun + '&ps=20&p=' + api.assetPage as url
CALL apoc.load.json(url) YIELD value
UNWIND value.data AS asset

WITH
  asset, canon, value.total as pageAssetsCount, assetGuidBase
  LIMIT {limit}
  WHERE
  asset.name <> ''

WITH asset, pageAssetsCount, assetGuidBase,
	bsuapi.obj.singleClean(asset.openpipe_canonical.id) AS openpipe_id,
	assetGuidBase + bsuapi.coll.singleClean(asset.openpipe_canonical.id) AS guid,
	bsuapi.obj.singleCleanObj(asset.openpipe_canonical.date,[canon.date]) AS openpipe_date,
	bsuapi.obj.singleClean(asset.openpipe_canonical.latitude) AS openpipe_latitude,
	bsuapi.obj.singleClean(asset.openpipe_canonical.longitude) AS openpipe_longitude

MERGE (x:Asset {id: openpipe_id})

SET x.name = bsuapi.coll.singleClean(asset.name)
SET x.openpipe_id = bsuapi.coll.singleClean(asset.openpipe_canonical.id)
SET x.metaDataId = bsuapi.coll.singleClean(asset.metaDataId)
SET x.guid = guid
SET x.primaryImageSmall = bsuapi.obj.singleClean(asset.openpipe_canonical.smallImage)
SET x.primaryImageSmallDimensions = bsuapi.obj.singleClean(asset.openpipe_canonical.smallImageDimensions)
SET x.primaryImageLarge = bsuapi.obj.singleClean(asset.openpipe_canonical.largeImage)
SET x.primaryImageLargeDimensions = bsuapi.obj.singleClean(asset.openpipe_canonical.largeImageDimensions)
SET x.primaryImageFull = bsuapi.obj.singleClean(asset.openpipe_canonical.fullImage)
SET x.primaryImageFullDimensions = bsuapi.obj.singleClean(asset.openpipe_canonical.fullImageDimensions)

SET x.openpipe_latitude = openpipe_latitude
SET x.openpipe_longitude = openpipe_longitude
SET x.hasGeo = (openpipe_latitude IS NOT NULL AND openpipe_longitude IS NOT NULL)
SET x.openpipe_date = bsuapi.obj.singleCleanObj(asset.openpipe_canonical.date,[canon.date])

SET x.import = 0
SET x.score_generated = 0

SET x.openpipe_artist = [k IN KEYS(asset.openpipe_canonical.artist) WHERE right(k, 2) <> '-1' | asset.openpipe_canonical.artist[k]]
SET x.openpipe_culture = [k IN KEYS(asset.openpipe_canonical.culture) WHERE right(k, 2) <> '-1' | asset.openpipe_canonical.culture[k]]
SET x.openpipe_classification = [k IN KEYS(asset.openpipe_canonical.classification) WHERE right(k, 2) <> '-1' | asset.openpipe_canonical.classification[k]]
SET x.openpipe_genre = [k IN KEYS(asset.openpipe_canonical.genre) WHERE right(k, 2) <> '-1' | asset.openpipe_canonical.genre[k]]
SET x.openpipe_medium = [k IN KEYS(asset.openpipe_canonical.medium) WHERE right(k, 2) <> '-1' | asset.openpipe_canonical.medium[k]]
SET x.openpipe_nation = [k IN KEYS(asset.openpipe_canonical.nation) WHERE right(k, 2) <> '-1' | asset.openpipe_canonical.nation[k]]
SET x.openpipe_city = [k IN KEYS(asset.openpipe_canonical.city) WHERE right(k, 2) <> '-1' | asset.openpipe_canonical.city[k]]
SET x.openpipe_tags = [k IN KEYS(asset.openpipe_canonical.tags) WHERE right(k, 2) <> '-1' | asset.openpipe_canonical.tags[k]]

SET x.openpipe_guid_artist = [k IN KEYS(asset.openpipe_canonical.artist) WHERE right(k, 2) <> '-1']
SET x.openpipe_guid_culture = [k IN KEYS(asset.openpipe_canonical.culture) WHERE right(k, 2) <> '-1']
SET x.openpipe_guid_classification = [k IN KEYS(asset.openpipe_canonical.classification) WHERE right(k, 2) <> '-1']
SET x.openpipe_guid_genre = [k IN KEYS(asset.openpipe_canonical.genre) WHERE right(k, 2) <> '-1']
SET x.openpipe_guid_medium = [k IN KEYS(asset.openpipe_canonical.medium) WHERE right(k, 2) <> '-1']
SET x.openpipe_guid_nation = [k IN KEYS(asset.openpipe_canonical.nation) WHERE right(k, 2) <> '-1']
SET x.openpipe_guid_city = [k IN KEYS(asset.openpipe_canonical.city) WHERE right(k, 2) <> '-1']
SET x.openpipe_guid_tags = [k IN KEYS(asset.openpipe_canonical.tags) WHERE right(k, 2) <> '-1']

WITH x, asset, pageAssetsCount
UNWIND KEYS(asset.openpipe_canonical.artist) as guid
MERGE (t:Artist {guid: guid})
SET t :Topic
SET t.name = asset.openpipe_canonical.artist[guid]
MERGE (x)-[:BY]->(t)

WITH x, asset, pageAssetsCount
UNWIND KEYS(asset.openpipe_canonical.culture) as guid
MERGE (t:Culture {guid: guid})
SET t :Topic
SET t.name = asset.openpipe_canonical.culture[guid]
MERGE (x)-[:ASSET_CULTURE]->(t)

WITH x, asset, pageAssetsCount
UNWIND KEYS(asset.openpipe_canonical.classification) as guid
MERGE (t:Classification {guid: guid})
SET t :Topic
SET t.name = asset.openpipe_canonical.classification[guid]
MERGE (x)-[:ASSET_CLASS]->(t)

WITH x, asset, pageAssetsCount
UNWIND KEYS(asset.openpipe_canonical.genre) as guid
MERGE (t:Genre {guid: guid})
SET t :Topic
SET t.name = asset.openpipe_canonical.genre[guid]
MERGE (x)-[:ASSET_GENRE]->(t)

WITH x, asset, pageAssetsCount
UNWIND KEYS(asset.openpipe_canonical.medium) as guid
MERGE (t:Medium {guid: guid})
SET t :Topic
SET t.name = asset.openpipe_canonical.medium[guid]
MERGE (x)-[:ASSET_MEDIUM]->(t)

WITH x, asset, pageAssetsCount
UNWIND KEYS(asset.openpipe_canonical.nation) as guid
MERGE (t:Nation {guid: guid})
SET t :Topic
SET t.name = asset.openpipe_canonical.nation[guid]
MERGE (x)-[:ASSET_NATION]->(t)

WITH x, asset, pageAssetsCount
UNWIND KEYS(asset.openpipe_canonical.city) as guid
MERGE (t:City {guid: guid})
SET t :Topic
SET t.name = asset.openpipe_canonical.city[guid]
MERGE (x)-[:ASSET_CITY]->(t)

WITH x, asset, pageAssetsCount
UNWIND KEYS(asset.openpipe_canonical.tags) as guid
MERGE (t:Tag {guid: guid})
SET t :Topic
SET t.name = asset.openpipe_canonical.tags[guid]
MERGE (x)-[:ASSET_TAG]->(t)

RETURN pageAssetsCount
"
,{limit: 1000}
) YIELD updates, batches, failedBatches

MATCH (api:OpenPipeConfig {name: 'api'})
RETURN "COMPLETED IMPORT apoc.periodic.commit(apoc.load.json( "+ api.allAssets +" )) updates:" + updates + " batches:" + batches + " failedBatches:" + failedBatches as t
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Artist)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:ARTIST]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, ARTIST relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Culture)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:CULTURE]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, CULTURE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Genre)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:GENRE]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, GENRE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Medium)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:Medium]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, MEDIUM relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Nation)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:NATION]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, NATION relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t
;


CALL apoc.periodic.iterate(
"
      MATCH (a:Classification)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:CLASS]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, CLASSIFICATION relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t
;


CALL apoc.periodic.iterate(
"
      MATCH (a:City)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:CITY]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, CITY relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Tag)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:TAG]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, TAG relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t
;

MATCH (a:Artist)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for ARTIST" as t;

MATCH (a:Culture)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for CULTURE" as t;

MATCH (a:Genre)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for GENRE" as t;

MATCH (a:Medium)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for MEDIUM" as t;

MATCH (a:Nation)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for NATION" as t;

MATCH (a:Classification)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for CLASSIFICATION" as t;

MATCH (a:City)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for CITY" as t;

MATCH (a:Tag)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for TAG" as t;

MATCH (api:OpenPipeConfig {name: 'api'})
SET api.lastRun = api.thisRun
RETURN "Sync COMPLETE" as t;
