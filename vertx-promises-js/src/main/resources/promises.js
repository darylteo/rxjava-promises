// Register the Javascript Language Adaptor for RxJava
com.darylteo.vertx.promises.js.JavascriptAdaptor.registerLanguageAdaptor();

// Export the Promise class, so that static methods can be called
module.exports = com.darylteo.rx.promises.Promise