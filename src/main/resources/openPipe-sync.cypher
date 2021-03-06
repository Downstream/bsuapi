MATCH (api:OpenPipeConfig {name: 'api'})
SET api.assetPage = 0
SET api.thisRun = apoc.temporal.format( date(), 'YYYY-MM-dd')
RETURN "RESET api counts to prepare for new sync." as t LIMIT 1
;

CALL apoc.periodic.commit("
MATCH (script:Script {name: 'OPENPIPE_SYNC'})
MATCH (canon:OpenPipeConfig {name: 'canonical'})
MATCH (fields:OpenPipeConfig {name: 'topicFields'})
MATCH (api:OpenPipeConfig {name: 'api'})
SET api.assetPage = (api.assetPage + 1)
SET script.page = api.assetPage
SET api.lastUrl = api.allAssets + '?changeStart=' + api.lastRun + '&ps=' + api.assetsPerPage + '&p=' + api.assetPage
WITH canon, fields, api.lastUrl as url
CALL apoc.load.json(url) YIELD value
UNWIND value.data AS asset

WITH asset, canon, value.total as pageAssetsCount,
  bsuapi.obj.singleClean(asset.openpipe_canonical.title) AS name,
	asset.openpipe_canonical.id AS guid,
	bsuapi.obj.singleCleanObj(asset.openpipe_canonical.date,[canon.date]) AS openpipe_date,
	toFloat(bsuapi.obj.singleClean(asset.openpipe_canonical.latitude)) AS openpipe_latitude,
	toFloat(bsuapi.obj.singleClean(asset.openpipe_canonical.longitude)) AS openpipe_longitude,
	bsuapi.obj.singleCleanObj(asset.openpipe_canonical.moment,['0']) AS openpipe_moment,
	bsuapi.obj.singleClean(asset.openpipe_canonical.biography) AS openpipe_bio,
	bsuapi.obj.singleClean(asset.openpipe_canonical.physicalDimensions) AS openpipe_dimensions,

	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.artist, [canon.artist, '']) AS openpipe_artist,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.classification, [canon.classification, '']) AS openpipe_classification,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.culture, [canon.culture, '']) AS openpipe_culture,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.genre, [canon.genre, '']) AS openpipe_genre,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.medium, [canon.medium, '']) AS openpipe_medium,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.nation, [canon.nation, '']) AS openpipe_nation,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.city, [canon.city, '']) AS openpipe_city,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.tags, [canon.tags, '']) AS openpipe_tags
    LIMIT {limit}
WHERE (asset.name <> '' AND guid IS NOT NULL AND NOT openpipe_date CONTAINS 'YYYY')

MERGE (x:Asset {guid: guid})
MERGE (artists:TopicList {type: 'Artist'})
MERGE (classifications:TopicList {type: 'Classification'})
MERGE (cultures:TopicList {type: 'Culture'})
MERGE (genres:TopicList {type: 'Genre'})
MERGE (mediums:TopicList {type: 'Medium'})
MERGE (nations:TopicList {type: 'Nation'})
MERGE (cities:TopicList {type: 'City'})
MERGE (tags:TopicList {type: 'Tag'})

SET artists += openpipe_artist
SET classifications += openpipe_classification
SET cultures += openpipe_culture
SET genres += openpipe_genre
SET mediums += openpipe_medium
SET nations += openpipe_nation
SET cities += openpipe_city
SET tags += openpipe_tags

SET x.import = 0
SET x.type = 'Asset'
SET x.score_generated = 0
SET x.name = name
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
SET x.latlong = CASE WHEN x.hasGeo THEN [openpipe_latitude, openpipe_longitude] ELSE null END
SET x.wgs = CASE WHEN x.hasGeo THEN point({x: openpipe_latitude, y: openpipe_longitude, crs: 'wgs-84'}) ELSE null END
SET x.openpipe_date = openpipe_date
SET x.date = date(bsuapi.obj.openPipeDateMap(x.openpipe_date))
SET x.moment = openpipe_moment
SET x.biography = openpipe_bio
SET x.openpipe_dimensions = openpipe_dimensions
SET x.dimensions = CASE WHEN openpipe_dimensions CONTAINS ',' THEN [n IN split(openpipe_dimensions, ',') | toFloat(n)] ELSE null END

SET x.openpipe_artist = [k IN KEYS(openpipe_artist) | openpipe_artist[k]]
SET x.openpipe_culture = [k IN KEYS(openpipe_culture) | openpipe_culture[k]]
SET x.openpipe_classification = [k IN KEYS(openpipe_classification) | openpipe_classification[k]]
SET x.openpipe_genre = [k IN KEYS(openpipe_genre) | openpipe_genre[k]]
SET x.openpipe_medium = [k IN KEYS(openpipe_medium) | openpipe_medium[k]]
SET x.openpipe_city = [k IN KEYS(openpipe_city) | openpipe_city[k]]
SET x.openpipe_nation = [k IN KEYS(openpipe_nation) | openpipe_nation[k]]
SET x.openpipe_tags = [k IN KEYS(openpipe_tags) | openpipe_tags[k]]
SET x.openpipe_guid_artist = KEYS(openpipe_artist)
SET x.openpipe_guid_culture = KEYS(openpipe_culture)
SET x.openpipe_guid_classification = KEYS(openpipe_classification)
SET x.openpipe_guid_genre = KEYS(openpipe_genre)
SET x.openpipe_guid_medium = KEYS(openpipe_medium)
SET x.openpipe_guid_nation = KEYS(openpipe_nation)
SET x.openpipe_guid_city = KEYS(openpipe_city)
SET x.openpipe_guid_tags = KEYS(openpipe_tags)

RETURN max(pageAssetsCount) LIMIT 1
"
,{limit: 1000}
) YIELD updates, batches, failedBatches

MATCH (api:OpenPipeConfig {name: 'api'})
RETURN "COMPLETED IMPORT apoc.periodic.commit(apoc.load.json( "+ api.allAssets +" )) updates:" + updates + " batches:" + batches + " failedBatches:" + failedBatches as t LIMIT 1
;

MATCH (x:Asset {import: 0})
SET x.import = 1
WITH x
OPTIONAL MATCH (x)-[r]->()
DELETE r
RETURN "Wiped old asset relations for imported assets" as t;

RETURN "STARTING Asset:Topic relationships" as t;

CALL apoc.periodic.iterate("MATCH (x:Asset) RETURN x","
  UNWIND x.openpipe_guid_artist as guid MERGE (t:Artist {guid: guid})
  MERGE (x)-[:BY]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
  {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:ARTIST relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset) RETURN x","
  UNWIND x.openpipe_guid_culture as guid MERGE (t:Culture {guid: guid})
  MERGE (x)-[:ASSET_CULTURE]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:CULTURE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset) RETURN x","
  UNWIND x.openpipe_guid_classification as guid MERGE (t:Classification {guid: guid})
  MERGE (x)-[:ASSET_CLASS]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:CLASS relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset) RETURN x","
  UNWIND x.openpipe_guid_genre as guid MERGE (t:Genre {guid: guid})
  MERGE (x)-[:ASSET_GENRE]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:GENRE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset) RETURN x","
  UNWIND x.openpipe_guid_medium as guid MERGE (t:Medium {guid: guid})
  MERGE (x)-[:ASSET_MEDIUM]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:MEDIUM relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset) RETURN x","
  UNWIND x.openpipe_guid_nation as guid MERGE (t:Nation {guid: guid})
  MERGE (x)-[:ASSET_NATION]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:NATION relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset) RETURN x","
  UNWIND x.openpipe_guid_city as guid MERGE (t:City {guid: guid})
  MERGE (x)-[:ASSET_CITY]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:CITY relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset) RETURN x","
  UNWIND x.openpipe_guid_tags as guid MERGE (t:Tag {guid: guid})
  MERGE (x)-[:ASSET_TAG]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:TAG relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (:Asset {hasGeo: true})-[]->(t:Topic) RETURN t","
  SET t.hasGeo = true
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "SET Topic hasGeo from Assets - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;


RETURN "STARTING Topic MetaGraph" as t;

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
RETURN "BUILDING Topic MetaGraph, ARTIST relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
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
RETURN "BUILDING Topic MetaGraph, CULTURE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
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
RETURN "BUILDING Topic MetaGraph, GENRE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
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
RETURN "BUILDING Topic MetaGraph, MEDIUM relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
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
RETURN "BUILDING Topic MetaGraph, NATION relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
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
RETURN "BUILDING Topic MetaGraph, CLASSIFICATION relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
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
RETURN "BUILDING Topic MetaGraph, CITY relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
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
RETURN "BUILDING Topic MetaGraph, TAG relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

MATCH (l:TopicList {type: "Artist"}) UNWIND KEYS(l) as guid
MATCH (a:Artist) WHERE a.guid = guid
SET a.name = l[guid]
RETURN "Updated Artist names" as t;

MATCH (l:TopicList {type: "Classification"}) UNWIND KEYS(l) as guid
MATCH (a:Classification) WHERE a.guid = guid
SET a.name = l[guid]
RETURN "Updated Classification names" as t;

MATCH (l:TopicList {type: "Culture"}) UNWIND KEYS(l) as guid
MATCH (a:Culture) WHERE a.guid = guid
SET a.name = l[guid]
RETURN "Updated Culture names" as t;

MATCH (l:TopicList {type: "Genre"}) UNWIND KEYS(l) as guid
MATCH (a:Genre) WHERE a.guid = guid
SET a.name = l[guid]
RETURN "Updated Genre names" as t;

MATCH (l:TopicList {type: "Medium"}) UNWIND KEYS(l) as guid
MATCH (a:Medium) WHERE a.guid = guid
SET a.name = l[guid]
RETURN "Updated Medium names" as t;

MATCH (l:TopicList {type: "Nation"}) UNWIND KEYS(l) as guid
MATCH (a:Nation) WHERE a.guid = guid
SET a.name = l[guid]
RETURN "Updated Nation names" as t;

MATCH (l:TopicList {type: "City"}) UNWIND KEYS(l) as guid
MATCH (a:City) WHERE a.guid = guid
SET a.name = l[guid]
RETURN "Updated City names" as t;

MATCH (l:TopicList {type: "Tag"}) UNWIND KEYS(l) as guid
MATCH (a:Tag) WHERE a.guid = guid
SET a.name = l[guid]
RETURN "Updated Tag names" as t;


MATCH (a:Artist)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for ARTIST" as t;

MATCH (a:Classification)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for CLASSIFICATION" as t;

MATCH (a:Culture)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for CULTURE" as t;

MATCH (a:Genre)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for GENRE" as t;

MATCH (a:Medium)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for MEDIUM" as t;

MATCH (a:Nation)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for NATION" as t;

MATCH (a:City)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for CITY" as t;

MATCH (a:Tag)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for TAG" as t;

MATCH (api:OpenPipeConfig {name: 'api'})
SET api.lastRun = api.thisRun
RETURN "Sync COMPLETE" as t;
