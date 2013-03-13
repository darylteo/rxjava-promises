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
  makePromise()
    .then(function(message){
      vassert.assertEquals("Deferred message received incorrectly", 'Hello World', message);
      return message + "!!!"
    })
    .then(function(message){
      vassert.assertEquals("Deferred message received incorrectly", 'Hello World!!!', message);
      vassert.testComplete();
    });
}

function test_repromise() {
  makePromise()
    .then(function(message){
      return makePromise();
    })
    .then(function(message){
      vassert.assertEquals("Deferred message received incorrectly", 'Hello World', message);
      vassert.testComplete();
    });
}

function test_failure() {
  makeFailPromise()
    .then(function(message){
      vassert.fail('This code should not be reached');
    })
    .fail(function(error){
      vassert.assertNotNull(error);
    })
    .fin(function(){
      vassert.testComplete();
    })
}

function test_error() {
  makePromise()
    .then(function(message){
      throw new Error('Ahh Error Occurred! Just testing...');
    })
    .then(function(message){
      vassert.fail('This code should not be reached');
    })
    .fail(function(error){
      vassert.assertNotNull(error);
    })
    .fin(function(){
      vassert.testComplete();
    })
}

function makePromise(){
  var promise = promises.defer();

  vertx.runOnLoop(function(){
    promise.fulfill('Hello World');
  });

  return promise;
}

function makeFailPromise(){
  var promise = promises.defer();

  vertx.runOnLoop(function(){
    promise.reject('Connection timed out!');
  });

  return promise;
}

initTests(this);