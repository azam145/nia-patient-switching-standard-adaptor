SELECT d.id, d.conceptid, d.term FROM "snomedct".description_s d WHERE d.conceptid = :conceptId AND d.term = :displayName ORDER BY d.active DESC;