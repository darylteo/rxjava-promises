load("vertx.js");
load("vertx_tests.js");

var promises = require('promises.js')

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

initTests(this);