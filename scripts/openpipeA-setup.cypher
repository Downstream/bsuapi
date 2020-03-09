MERGE (x:OpenPipeConfig {name: 'api'})
SET x.canonical='http://mec402.boisestate.edu/cgi-bin/dataAccess/getCanonicalMetaTags.py'
SET x.allAssets='file:///openpipeSingle.json'
SET x.changedAssets='file:///openpipeChanged.json';

MATCH (api:OpenPipeConfig {name: 'api'})
MERGE (x:OpenPipeConfig {name: 'canonical'})
WITH x, api
CALL apoc.load.json(api.canonical) YIELD value
WITH x, value, keys(value) as props
UNWIND props AS prop
CALL apoc.create.setProperty(x, prop, head(value[prop])) yield node
RETURN x;

MERGE (x:OpenPipeConfig {name: 'topicFields'})
SET x.openpipe_artist = 'openpipe_canonical_artist'
SET x.openpipe_culture = 'openpipe_canonical_culture'
SET x.openpipe_classification = 'openpipe_canonical_classification'
SET x.openpipe_genre = 'openpipe_canonical_genre'
SET x.openpipe_medium = 'openpipe_canonical_medium'
SET x.openpipe_nation = 'openpipe_canonical_nation'
SET x.openpipe_city = 'openpipe_canonical_city'
SET x.openpipe_tags = 'openpipe_canonical_tags';

CREATE INDEX ON :Asset(id);
CREATE INDEX ON :Artist(name);
CREATE INDEX ON :Culture(name);
CREATE INDEX ON :Genre(name);
CREATE INDEX ON :Medium(name);
CREATE INDEX ON :Nation(name);
CREATE INDEX ON :Classification(name);
CREATE INDEX ON :City(name);
CREATE INDEX ON :Tag(name);

//CALL db.index.fulltext.createNodeIndex("nameIndex",["Artist","Classification","Culture","Nation","Tag","Artwork","Genre","Medium"],["name"]);
CALL db.index.fulltext.createNodeIndex("topicNameIndex",["Artist","Culture","Classification","Genre","Medium","Nation","City","Tag"],["name"]);
CALL db.index.fulltext.createNodeIndex("assetNameIndex",["Asset"],["name"]);
