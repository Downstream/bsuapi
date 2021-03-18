CREATE (art:Asset {
  artistDisplayName: "Edgar Degas",
  artistAlphaSort: "Degas, Edgar",
  country: "France",
  objectDate: "n.d.",
  geographyType: "Made in",
  import: 2,
  classification: "Drawings",
  title: "Head of a Saint (profile to the right), after Fra Angelico",
  artistEndDate: "1917",
  artistBeginDate: "1834",
  tags: ["Men","Profiles","Saints"],
  artistNationality: "French",
  artistRole: "Draftsman",
  name: "Drawing",
  artistDisplayBio: "French, Paris 1834–1917 Paris",
  objectName: "Drawing",
  id: 334323,
  guid: "ASSET",
  primaryImageSmall: "https://images.metmuseum.org/CRDImages/dp/web-large/DP805823.jpg",
  objectID: 334323,
  dimensions: "Sheet: 9 1/16 x 4 3/16 in. (23 x 10.6cm)"
})

CREATE (degas:Artist:Topic {name: "Edgar Degas", artCount: 203, guid: "DEGAS"})
CREATE (draw:Classification:Topic {name: "Drawings",artCount: 6260, guid: "DRAW"})
CREATE (french:Nation:Topic {name: "French",artCount: 13575, guid: "FRENCH"})
CREATE (tMen:Tag:Topic {name: "Men",artCount: 32172, guid: "MEN"})
CREATE (tProfiles:Tag:Topic {name: "Profiles",artCount: 4101, guid: "PROFILE"})
CREATE (tSaints:Tag:Topic {name: "Saints",artCount: 896, guid: "SAINTS"})

// SEARCH TEST LIMIT and PAGE
CREATE (searchDegas1:Artist {name: "Degas One", guid: "DEGAS1"})
CREATE (searchDegas2:Artist {name: "degas two", guid: "DEGAS2"})
CREATE (searchDegas3:Artist {name: "DEGAS THREE", guid: "DEGAS3"})
CREATE (searchDegas4:Artist {name: "degas four", guid: "DEGAS4"})
CREATE (searchDegas5:Artist {name: "Some other Bob Degasa", guid: "DEGAS5"}) // no match
CREATE (searchDegas6:Artist {name: "dega", guid: "DEGASNONE"}) // no match

CREATE (degas)<-[:BY]-(art)
CREATE (draw)<-[:ART_CLASS]-(art)
CREATE (french)<-[:ART_NATION]-(art)
CREATE (tMen)<-[:ART_TAG]-(art)
CREATE (tProfiles)<-[:ART_TAG]-(art)
CREATE (tSaints)<-[:ART_TAG]-(art)

CREATE (draw)-[:ARTIST]->(degas)
CREATE (french)-[:ARTIST]->(degas)
CREATE (tMen)-[:ARTIST]->(degas)
CREATE (tProfiles)-[:ARTIST]->(degas)
CREATE (tSaints)-[:ARTIST]->(degas)

CREATE (degas)-[:CLASS]->(draw)
CREATE (french)-[:CLASS]->(draw)
CREATE (tMen)-[:CLASS]->(draw)
CREATE (tProfiles)-[:CLASS]->(draw)
CREATE (tSaints)-[:CLASS]->(draw)

CREATE (degas)-[:NATION]->(french)
CREATE (draw)-[:NATION]->(french)
CREATE (tMen)-[:NATION]->(french)
CREATE (tProfiles)-[:NATION]->(french)
CREATE (tSaints)-[:NATION]->(french)

CREATE (degas)-[:TAG]->(tMen)
CREATE (french)-[:TAG]->(tMen)
CREATE (draw)-[:TAG]->(tMen)
CREATE (tProfiles)-[:TAG]->(tMen)
CREATE (tSaints)-[:TAG]->(tMen)

CREATE (degas)-[:TAG]->(tProfiles)
CREATE (french)-[:TAG]->(tProfiles)
CREATE (draw)-[:TAG]->(tProfiles)
CREATE (tMen)-[:TAG]->(tProfiles)
CREATE (tSaints)-[:TAG]->(tProfiles)

CREATE (degas)-[:TAG]->(tSaints)
CREATE (french)-[:TAG]->(tSaints)
CREATE (draw)-[:TAG]->(tSaints)
CREATE (tProfiles)-[:TAG]->(tSaints)
CREATE (tMen)-[:TAG]->(tSaints)

CREATE (fa:Folder {guid: "A", name: "Folder A"})
CREATE (fb:Folder {guid: "B", name: "Folder B"})

CREATE (fa)<-[:FOLDER_ASSET]-(art)
CREATE (fb)<-[:FOLDER_ASSET]-(art)
