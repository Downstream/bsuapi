MERGE (x:OpenPipeConfig {name: 'api'})
SET x.canonical='http://mec402.boisestate.edu/cgi-bin/dataAccess/getCanonicalMetaTags.py'
SET x.allAssets='http://mec402.boisestate.edu/cgi-bin/dataAccess/getAllAssetsWithGUID.py'
SET x.changedAssets='http://mec402.boisestate.edu/cgi-bin/dataAccess/getAllAssetsWithGUID.py'
SET x.folders='http://mec402.boisestate.edu/cgi-bin/dataAccess/getCollections.py'
SET x.singleAsset='http://mec402.boisestate.edu/cgi-bin/openpipe/data/asset/'
SET x.singleFolder='http://mec402.boisestate.edu/cgi-bin/openpipe/data/folder/'
SET x.lastRun = '2020-01-01'
SET x.lastFolderRun = '2020-01-01'
RETURN "RESET OpenPipeConfig api settings" as t
;


MATCH (api:OpenPipeConfig {name: 'api'})
MERGE (x:OpenPipeConfig {name: 'canonical'})
WITH x, api
CALL apoc.load.json(api.canonical) YIELD value
WITH x, value, keys(value) as props
UNWIND props AS prop
CALL apoc.create.setProperty(x, prop, head(value[prop])) yield node
RETURN "LOADED OpenPipeConfig canonical values" as t
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
RETURN "RESET OpenPipeConfig topicFields" as t
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

CALL db.index.fulltext.createNodeIndex("topicNameIndex",["Artist","Culture","Classification","Genre","Medium","Nation","City","Tag"],["name"])
RETURN "CREATED full-text-index topicNameIndex" as t;
CALL db.index.fulltext.createNodeIndex("assetNameIndex",["Asset"],["name"])
RETURN "CREATED full-text-index assetNameIndex" as t;

MATCH (a:Topic) DETACH DELETE a;
MATCH (a:Asset) DETACH DELETE a;
