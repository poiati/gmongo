package com.gmongo

class AggregationTest extends IntegrationTestCase {

  

  void setUp() {
    super.setUp()
    db.zipcodes.drop()
    db.zipcodes << ["city": "ACMAR", "loc": [-86.51557F, 33.584132F], "pop": 6055, "state": "AL", "_id": "35004"]
    db.zipcodes << ["city": "ADAMSVILLE", "loc": [-86.959727F, 33.588437F], "pop": 10616, "state": "AL", "_id": "35005"]
    db.zipcodes << ["city": "ADGER", "loc": [-87.167455F, 33.434277F], "pop": 3205, "state": "AL", "_id": "35006"]
    db.zipcodes << ["city": "KEYSTONE", "loc": [-86.812861F, 33.236868F], "pop": 14218, "state": "AL", "_id": "35007"]
    db.zipcodes << ["city": "NEW SITE", "loc": [-85.951086F, 32.941445F], "pop": 19942, "state": "AL", "_id": "35010"]
  }

  void testSimpleAggregation() {
    def aggrOutput = db.zipcodes.aggregate(
      [ $project : [ city: 1, pop: 1 ] ],
      [ $match : [ pop: [ $gte : 10 * 1000 ] ] ],
      [ $sort: [ pop: -1] ]
    )

    assert aggrOutput.results().size() == 3
    assert aggrOutput.results()[0].city == "NEW SITE"
    assert aggrOutput.results()[1].city == "KEYSTONE"
    assert aggrOutput.results()[2].city == "ADAMSVILLE"
  }

}
