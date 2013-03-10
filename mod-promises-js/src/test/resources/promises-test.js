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
  var promise = makePromise()
    .then(function(message){
      vassert.assertEquals("Deferred message received incorrectly", 'Hello World', message);
      return message + "!!!"
    })
    .then(function(message){
      console.log('Handler: ' + message);
      vassert.assertEquals("Deferred message received incorrectly", 'Hello World!!!', message);
      vassert.testComplete();
    });
}

function makePromise(){
  var promise = promises.defer();

  vertx.runOnLoop(function(){
    promise.fulfill('Hello World');
  });

  return promise;
}

initTests(this);