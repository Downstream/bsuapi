CREATE (f:Folder {
  type: "Folder",
  name: "Manet Folder",
  title: "Manet Timeline",
  guid: "MANET",
  artCount: 21,
  dateStart: "1860-01-01",
  dateEnd: "1882-01-01"
})

CREATE (:Asset {
  type:"Asset",
  date:"1860-01-01",
  guid: "100/1041",
  name: "The Spanish Singer",
  moment: "CE 1860"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1860-01-01",
  name: "The Spanish Singer",
  guid: "100/6411",
  moment: "CE 1860",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1866-01-01",
  name: "Young Lady in 1866",
  guid: "100/6412",
  moment: "1866",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1879-01-01",
  name: "George Moore (1852–1933)",
  guid: "100/6413",
  moment: "CE 1879",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1864-01-01",
  name: "The Dead Christ with Angels",
  guid: "100/6414",
  moment: null,
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1866-01-01",
  name: "A Matador",
  guid: "100/6415",
  moment: "1866",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1874-01-01",
  name: "The Monet Family in Their Garden at Argenteuil",
  guid: "100/6416",
  moment: "CE 1874",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1880-01-01",
  name: "Madame Manet (Suzanne Leenhoff, 1830–1906) at Bellevue",
  guid: "100/6417",
  moment: "1880",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1874-01-01",
  name: "Boating",
  guid: "100/6418",
  moment: "CE 1874",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1870-01-01",
  name: "The Brioche",
  guid: "100/6419",
  moment: null,
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1861-01-01",
  name: "Boy with a Sword",
  guid: "100/6420",
  moment: "1861",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1862-01-01",
  name: "Fishing",
  guid: "100/6421",
  moment: null,
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1863-01-01",
  name: "Young Man in the Costume of a Majo",
  guid: "100/6422",
  moment: null,
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1862-01-01",
  name: "Mademoiselle V. . . in the Costume of an Espada",
  guid: "100/6423",
  moment: null,
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1879-01-01",
  name: "Mademoiselle Isabelle Lemonnier (1857–1926)",
  guid: "100/6424",
  moment: null,
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1867-01-01",
  name: "The Funeral",
  guid: "100/6425",
  moment: "CE 1867",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1879-01-01",
  name: "Emilie-Louise Delabigne (1848–1910), Called Valtesse de la Bigne",
  guid: "100/6426",
  moment: null,
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1882-01-01",
  name: "Strawberries",
  guid: "100/6429",
  moment: "CE 1882",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1864-01-01",
  name: "Peonies",
  guid: "100/6430",
  moment: "1864",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1882-01-01",
  name: "Jean-Baptiste Faure (1830–1914)",
  guid: "100/6431",
  moment: "1882",
  type: "Asset"
})-[:FOLDER_ASSET]->(f)

CREATE (:Asset {
  date: "1882-01-01",
  name: "Head of Jean-Baptiste Faure (1830–1914)",
  guid: "100/6432",
  moment: null,
  type: "Asset"
})-[:FOLDER_ASSET]->(f)
