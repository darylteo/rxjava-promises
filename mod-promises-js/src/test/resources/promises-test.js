load("vertx.js");
load("vertx_tests.js");

var promises = require('promises.js')

// // The test methods must begin with "test"

function test_require() {
  vassert.assertNotNull(promises);
  vassert.testComplete();
}

function test_defer() {
  var promise = promises.defer();
  vassert.assertNotNull(promise);

  vassert.testComplete();
}

function test_basic() {
  var promise = promises.defer()
    .then(function(message){
      vassert.assertEquals("Deferred message received incorrectly", 'Hello World', message);
      vassert.testComplete();
    });

  vertx.runOnLoop(function(){
    promise.fulfill('Hello World');
  });
}

initTests(this);