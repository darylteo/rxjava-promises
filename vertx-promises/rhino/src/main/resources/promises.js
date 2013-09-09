com.darylteo.vertx.promises.js.JavascriptAdaptor.jsClassTester(function(e){ console.log("Hello World"); } );
com.darylteo.vertx.promises.js.JavascriptAdaptor.jsClassTester({ test : "Hello" });
com.darylteo.vertx.promises.js.JavascriptAdaptor.jsClassTester("Hello");

// Register the Javascript Language Adaptor for RxJava
com.darylteo.vertx.promises.js.JavascriptAdaptor.registerLanguageAdaptor();

// Export the Promise class, so that static methods can be called
module.exports = com.darylteo.vertx.promises.Promise;