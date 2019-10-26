# Releases

## Dev
* [`0.1.3`](bsuapi-0.1.3.jar) 
* [`0.1.2`](bsuapi-0.1.2.jar) Core behaviors, tests, and optional request parameters
   * refined patterns for behaviors and cypherQueries.
   * added querystring parameters: 
        * `limit` (default 20) sets max number of results in a set - e.g.: `/related` will return 20 assets, and 20 of each topic-type.
        * `page` (1 indexed) `page=2` will retrieve the next set of results according to `limit`. Sets beyond the last page will not be returned.
   * added tests (70% line coverage) and refined core patterns.
   * added formal [json-schemas](http://json-schema.org/draft-07/schema#) for related, topics, and assets.
* [`0.1.0`](bsuapi-0.1.0.jar) Framework established - core methods added
   * root `/` lists all available methods, and all topics, with a sample top results for each topic.
   * related `/related/{TOPIC}/{KEY or NAME}` lists the matching node, and a set of nodes with the most shared relationships for each topic.
   * added optional querystring parameter: `requestToken`; if provided, it will be echoed in the response. 
* [`0.0.2`](bsuapi-0.0.2.jar) Structure and UrlEncoded Names - abstracted behaviors /w pattern for all future methods
* [`0.0.1`](bsuapi-0.0.1.jar) Initialize - empty framing and starting point

## Stable
* none
