# Releases

## Dev
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
