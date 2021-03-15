MATCH (api:OpenPipeConfig {name: 'api'})
SET api.topicPage = 0
SET api.topicRun = apoc.temporal.format( date(), 'YYYY-MM-dd')
RETURN "RESET api counts to prepare for new topic sync." as t LIMIT 1
;

CALL apoc.periodic.commit("
MERGE (script:Script {name: 'OPENPIPE_TOPICS'})
WITH script
MATCH (api:OpenPipeConfig {name: 'api'})
SET api.topicPage = (api.topicPage + 1)
SET script.page = api.topicPage
SET api.lastTopicUrl = api.allTopics + '?ps=' + api.assetsPerPage + '&p' + api.topicPage
WITH api.lastTopicUrl as url
CALL apoc.load.json(url) YIELD value

UNWIND value.availableTopics AS topicType

UNWIND value[topicType].data AS topic

WITH url, apoc.text.capitalizeAll(topicType) AS topicType, topic, SPLIT(topic.guid,'/') as guidLong
WITH url, topicType, topic, COALESCE(guidLong[size(guidLong)-2], '0') + '/' + COALESCE(guidLong[size(guidLong)-1], '0') as guid

MERGE (t:Topic {guid:guid})
SET
  t.name = topic.value,
  t.biography=topic.biography

WITH url, t, topicType
CALL apoc.create.addLabels(t, [topicType]) YIELD node

RETURN COUNT(node) AS t LIMIT {limit}
"
,{limit: 1000}
) YIELD updates, batches, failedBatches
MATCH (api:OpenPipeConfig {name: 'api'})
RETURN "COMPLETED IMPORT apoc.periodic.commit(apoc.load.json( "+ api.allTopics +" )) updates:" + updates + " batches:" + batches + " failedBatches:" + failedBatches as t LIMIT 1
;

