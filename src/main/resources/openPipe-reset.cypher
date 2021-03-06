MERGE (x:OpenPipeConfig {name: 'api'})
SET x.canonical='http://openpipe.boisestate.edu/cgi-bin/dataAccess/getCanonicalMetaTags.py'
SET x.allTopics='http://openpipe.boisestate.edu/cgi-bin/dataAccess/getAllTopics.py'
SET x.allAssets='http://openpipe.boisestate.edu/cgi-bin/dataAccess/getAllAssetsWithGUID.py'
SET x.changedAssets='http://openpipe.boisestate.edu/cgi-bin/dataAccess/getAllAssetsWithGUID.py'
SET x.folders='http://openpipe.boisestate.edu/cgi-bin/dataAccess/getCollections.py'
SET x.singleAsset='http://openpipe.boisestate.edu/cgi-bin/openpipe/data/asset/'
SET x.guidUri="http://openpipe.boisestate.edu/"
SET x.settings='http://openpipe.boisestate.edu/cgi-bin/dataAccess/settings/getWallAppSettings.py'
SET x.lastRun = '2020-01-01'
SET x.lastFolderRun = '2020-01-01'
SET x.assetsPerPage = '100'
RETURN "RESET OpenPipeConfig api settings" as t LIMIT 1
;

MATCH (api:OpenPipeConfig {name: 'api'})
MERGE (x:OpenPipeConfig {name: 'canonical'})
WITH x, api
CALL apoc.load.json(api.canonical) YIELD value
WITH x, value, keys(value) as props
UNWIND props AS prop
CALL apoc.create.setProperty(x, prop, head(value[prop])) yield node
RETURN "LOADED OpenPipeConfig canonical values" as t LIMIT 1
;

MERGE (x:OpenPipeConfig {name: 'topicFields'})
SET x.openpipe_artist = 'openpipe_canonical_artist'
SET x.openpipe_culture = 'openpipe_canonical_culture'
SET x.openpipe_classification = 'openpipe_canonical_classification'
SET x.openpipe_genre = 'openpipe_canonical_genre'
SET x.openpipe_medium = 'openpipe_canonical_medium'
SET x.openpipe_nation = 'openpipe_canonical_nation'
SET x.openpipe_city = 'openpipe_canonical_city'
SET x.openpipe_tags = 'openpipe_canonical_tags'
RETURN "RESET OpenPipeConfig topicFields" as t LIMIT 1
;

CREATE INDEX ON :Asset(id);
CREATE INDEX ON :Artist(name);
CREATE INDEX ON :Artist(guid);
CREATE INDEX ON :Culture(name);
CREATE INDEX ON :Culture(guid);
CREATE INDEX ON :Genre(name);
CREATE INDEX ON :Genre(guid);
CREATE INDEX ON :Medium(name);
CREATE INDEX ON :Medium(guid);
CREATE INDEX ON :Nation(name);
CREATE INDEX ON :Nation(guid);
CREATE INDEX ON :Classification(name);
CREATE INDEX ON :Classification(guid);
CREATE INDEX ON :City(name);
CREATE INDEX ON :City(guid);
CREATE INDEX ON :Tag(name);
CREATE INDEX ON :Tag(guid);
CREATE INDEX ON :Folder(guid);

CALL db.indexes() YIELD indexName
WITH COLLECT(indexName) AS indexList
WITH [i IN indexList WHERE i IN ['assetNameIndex', 'topicNameIndex', 'folderNameIndex'] | i] AS dropList
UNWIND dropList AS index
CALL db.index.fulltext.drop(index)
WITH count(index) as dropCount
RETURN "DROPPED " + dropCount + " fulltext indices" as t LIMIT 1;

CALL db.index.fulltext.createNodeIndex("topicNameIndex",["Artist","Culture","Classification","Genre","Medium","Nation","City","Tag"],["name"])
RETURN "CREATED full-text-index topicNameIndex" as t LIMIT 1;
CALL db.index.fulltext.createNodeIndex("assetNameIndex",["Asset"],["name"])
RETURN "CREATED full-text-index assetNameIndex" as t LIMIT 1;
CALL db.index.fulltext.createNodeIndex("folderNameIndex",["Folder"],["name"])
RETURN "CREATED full-text-index folderNameIndex" as t LIMIT 1;

MATCH (a:Topic) DETACH DELETE a;
MATCH (a:Asset) DETACH DELETE a;
