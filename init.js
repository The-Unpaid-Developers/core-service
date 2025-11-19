use('solutions')

db.createCollection("solutionReviews", {
  collation: { locale: "en", strength: 2 }
})

db.createCollection("lookups", {
  collation: { locale: "en", strength: 2 }
})

db.createCollection("queries", {
  collation: { locale: "en", strength: 2 }
})