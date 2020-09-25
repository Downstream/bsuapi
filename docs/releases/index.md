# Releases
* [`1.5.3`](bsuapi-1.5.3.jar) Added settings as executable option: `OPENPIPE_SETTINGS`.
* [`1.5.2`](bsuapi-1.5.2.jar) Added settings, geo, and config improvements.
   * Folders and Topics containing Assets with geo-data will be marked hasGeo=true, and included in filter results.
   * Settings from openpipe are imported, and parsed results added to root `/` data.
      * collections
      * timeline - `"[Topic|Folder]"` OR `"[Topic|Folder] by [TopicType]"`
      * color
      * globe
      * explore
   * Better, and configurable, exception visibility.
   * Config is now also visible in `/` settings data.
* [`1.4.0`](bsuapi-1.4.0.jar) Added geo filter. 
   * New Parameter `hasGeo` (bool) `hasGeo=1` will only include assets and topics with geo data.
* [`1.3.2`](bsuapi-1.3.2.jar) Bugfixes for sync and folders.
* [`1.3.0`](bsuapi-1.3.0.jar) Adds OpenPipe Integration.
   * `/folder` returns folder list.
   * `/folder/{guid}` returns folder assets and topics, with layout/positional data if present.
   * API uses `GUID` for asset and topic keys instead of `name`. Guids are currently openpipe generated full-urls to source data in openpipe.
   * Added date handling to data.
      * Date formatting: "BC 450 JUN 7 23:59:59" = `date: -449-06-07 23:59:59` 
      * Assets now have two date fields: `openpipe_date` containing the original date format, and `date` containing a datetime.
      * Folders and Topics now have dateStart and dateEnd, extrapolated from the oldest and most recent dates for their respective Assets.
   * Bugfix: some missing assets.
* [`1.0.1`](bsuapi-1.0.1.jar) Adds TopicTypes: Genre, Medium, City.
   * Bugfix: API not returning topic assets.
* [`1.0.0`](bsuapi-1.0.0.jar) First production release.

## Dev
* [`1.3.3`](bsuapi-1.3.3.jar) WIP to workaround Jersey router bug `/folder/{guid}` 404.
* [`0.3.2`](bsuapi-0.3.2.jar) OpenPipe Integration.
   * Added openpipe sync scripts:
      * `INFO` regenerates :Info cards.
      * `OPENPIPE_RESET` clears data, and rebuilds config and indices.
      * `OPENPIPE_SYNC` pulls new Assets from OpenPipe which have changed since last sync, updates graph.
      * `OPENPIPE_TOPICIMG` attempts to select an Asset as "representative" for each Topic, so the Topics all have images.
   * Improvement to aync generation. Running script also stores status report in DB, for retrieval after Thread is shutdown. 
* [`0.3.1`](bsuapi-0.3.1.jar) Async generation: web-triggered long-running cypher-scripts.
   * `/execute/{SCRIPT}` Shows results of previous run of specified scripts.
   * `/execute/{SCRIPT}/start` Starts running that script, or returns current status and progress if already running.
   * Added functions and procedures for ingesting OpenPipe data.
   * Minor refactor of Cypher db abstraction.
   * Improvements and additions to execution and retrieval of cypher commands.  
* [`0.2.0`](bsuapi-0.2.0.jar) Grouped search results and search-completion.
   * `/search/{QUERY}` now groups results by Assets/Topics.
   * `/search/completion/{QUERY}` will return a flat array of strings of best-match topics.
   * Behavior and Query patterns major refactor.
      * Expanding and altering behaviors and queries is now much simpler.
      * Any behavior may be the parent of any behavior. Trees possible.
          * Resulting data will show all behaviors as siblings in the `data` object. 
* [`0.1.7`](bsuapi-0.1.7.jar) Search results organized, and initial simple-viewer.
   * `/viewer` simple UI for the api.
* [`0.1.6`](bsuapi-0.1.6.jar) Root filters and infoCard bugs.
   * `/?filter=period:Classical` optional filter for home results (not performant).
   * `/info/Neo4j` 404 fixed.
* [`0.1.5`](bsuapi-0.1.5.jar) Added full-text search and api-triggered cypher commands.
   * `/search/{QUERY}` follows basic Lucene syntax (no special characters).
   * `/generate/{COMMAND_NAME}` runs predefined cypher-script.
* [`0.1.4`](bsuapi-0.1.4.jar) Bugfixes for links and InfoCards.
* [`0.1.3`](bsuapi-0.1.3.jar) API CypherCommands and InfoCards.
   * `/info/`
   * removed apoc dependency.
   * domain config from config file.
   * urls in results are now absolute instead of relative.
* [`0.1.2`](bsuapi-0.1.2.jar) Core behaviors, tests, and optional request parameters.
   * refined patterns for behaviors and cypherQueries.
   * added querystring parameters: 
        * `limit` (default 20) sets max number of results in a set - e.g.: `/related` will return 20 assets, and 20 of each topic-type.
        * `page` (1 indexed) `page=2` will retrieve the next set of results according to `limit`. Sets beyond the last page will not be returned.
   * added tests (70% line coverage) and refined core patterns.
   * added formal [json-schemas](http://json-schema.org/draft-07/schema#) for related, topics, and assets.
* [`0.1.0`](bsuapi-0.1.0.jar) Framework established - core methods added.
   * root `/` lists all available methods, and all topics, with a sample top results for each topic.
   * related `/related/{TOPIC}/{KEY or NAME}` lists the matching node, and a set of nodes with the most shared relationships for each topic.
   * added optional querystring parameter: `requestToken`; if provided, it will be echoed in the response. 
* [`0.0.2`](bsuapi-0.0.2.jar) Structure and UrlEncoded Names - abstracted behaviors /w pattern for all future methods.
* [`0.0.1`](bsuapi-0.0.1.jar) Initialize - empty framing and starting point.

## Stable
* none
