CREATE (start:Info {
  title: "How does this work?",
  name: "Info",
  smallImage: "https://bsu.downstreamlabs.com/bsuapi/data/infoQuestion.png",
  largeImage: "https://bsu.downstreamlabs.com/bsuapi/data/infoDetail.png",
  biography: ""
})

CREATE (museums:Info {
  title: "Museums' Digital Archives",
  name: "Museums",
  smallImage: "https://bsu.downstreamlabs.com/bsuapi/data/museumIcon.png",
  largeImage: "https://bsu.downstreamlabs.com/bsuapi/data/museums.png",
  biography: "Museums around the world curate detailed digital archives, with data about every asset and artwork. This data includes the artist, time, culture, medium, and content, and many other useful details.

  Development was done entirely with data provided by The Met, thanks to their Open Access policy."
})

CREATE (openpipe:Info {
  title: "BSU CS OpenPipe",
  name: "OpenPipe",
  smallImage: "http://mec402.boisestate.edu/ui/favicon.png",
  largeImage: "https://bsu.downstreamlabs.com/bsuapi/data/openpipe.png",
  biography: "A Distributed Content Production Pipeline with a DAMS system attached that runs on AWS.

  OpenPipe is an open source federated content production pipeline this is lightweight, extensible and documented.

  It is built wherever possible using web compatible technologies such as JSON, DOM, HTML5, AJAX, and PHP.

  All content stored within the DAMS is accessible directlry via a URL that allows anyone anywhere to access the data.

  The system also supports a federated content model where data may be maintained at remote sites.

  One part of the system is an active runtime environment that runs regularly to assess the status of data within the system.

  The system supports moving remote assets vi a registery of remote content repos.

  The production component of the pipeline is written in Python and directly manipulates the Database management the content."
})

CREATE (neo4j:Info {
  title: "Neo4j Graph Database and JSON API",
  name: "Neo4j",
  smallImage: "https://bsu.downstreamlabs.com/bsuapi/data/neo4jIcon.png",
  largeImage: "https://bsu.downstreamlabs.com/bsuapi/data/neo4jLarge.png",
  biography: ""
})

CREATE (app:Info {
  title: "Exhibit Hardware and Application",
  name: "Exhibit",
  smallImage: "https://bsu.downstreamlabs.com/bsuapi/data/downstreamIcon.png",
  largeImage: "https://bsu.downstreamlabs.com/bsuapi/data/neo4jLarge.png",
  biography: ""
})

CREATE (start)<-[:INFO]-(openpipe)
CREATE (start)<-[:INFO]-(museums)
CREATE (start)<-[:INFO]-(neo4j)
CREATE (start)<-[:INFO]-(app)

CREATE (start)-[:INFO]->(openpipe)
CREATE (start)-[:INFO]->(museums)
CREATE (start)-[:INFO]->(neo4j)
CREATE (start)-[:INFO]->(app)

CREATE (openpipe)<-[:INFO]-(museums)
CREATE (openpipe)<-[:INFO]-(neo4j)
CREATE (museums)<-[:INFO]-(openpipe)

CREATE (neo4j)<-[:INFO]-(openpipe)
CREATE (neo4j)<-[:INFO]-(app)

CREATE (app)<-[:INFO]-(museums)
CREATE (app)<-[:INFO]-(neo4j)

RETURN "CREATED 5 :Info nodes (v 0.1.3) : info, museums, openpipe, neo4j, app" as t;