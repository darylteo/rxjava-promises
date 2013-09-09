var container = require("vertx/container")
var vertx = require("vertx");
var vertxTests = require("vertx_tests");
var vassert = require("vertx_assert");

var promises = require('promises.js')
var logger = container.logger;

logger.info(function(){});

// The test methods must begin with "test"
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
      vassert.assertTrue("Deferred message received incorrectly", 'Hello World' == message);
      return message + "!!!"
    })
    .then(function(message){
      vassert.assertTrue("Deferred message received incorrectly", 'Hello World!!!' == message);
      vassert.testComplete();
    });
}

function test_repromise() {
  makePromise()
    .then(function(message){
      return makePromise();
    })
    .then(function(message){
      vassert.assertTrue("Deferred message received incorrectly", 'Hello World' == message);
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
      throw new Error('Ahh!!!');
    })
    .then(function(message){
      vassert.fail('This code should not be reached');
    })
    .fail(function(error){
      vassert.assertNotNull(error);
      console.log(error.message);
      vassert.assertTrue(error.message == 'Ahh!!!');
    })
    .fin(function(){
      vassert.testComplete();
    })
}

function test_scope() {
  var _message = "Hello";

  makePromise()
    .then(function(message){
      _message = message;
    }).then(function(){
      vassert.assertEquals("Scoped variable not correct!", "Hello World", _message);
    }).fin(function(){
      vassert.assertEquals("Scoped variable not correct!", "Hello World", _message);
      vassert.testComplete();
    })
}

function test_handler() {
	var p = promises.defer();
	vertx.setTimer(1000, p);
	
	p.then(function(){
		vassert.testComplete();
	});
}

function makePromise(){
  var promise = promises.defer();

  vertx.runOnContext(function(){
    promise.fulfill('Hello World');
  });

  return promise;
}

function makeFailPromise(){
  var promise = promises.defer();

  vertx.runOnContext(function(){
    promise.reject('Connection timed out!');
  });

  return promise;
}

vertxTests.startTests(this);