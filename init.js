use('solutions')

db.createCollection("solutions", {
  collation: { locale: "en", strength: 2 }
})

db.createCollection("lookups", {
  collation: { locale: "en", strength: 2 }
})
