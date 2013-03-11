module.exports = {
  "defer" : function() {
    return wrap(com.darylteo.promises.Promise.defer());
  }
}

function wrap(promise){
  return {
    "then" : function(onFulfilled) {
      return wrap(promise['then(java.lang.Object)'](onFulfilled));
    },
    "fulfill" : function(value) {
      return wrap(promise['fulfill(java.lang.Object)'](value));
    },
    "reject" : function(reason) {
      return wrap(promise['reject(java.lang.Object)'](reason));
    }
  }
}

com.darylteo.promises.js.JavascriptAdaptor.registerLanguageAdaptor();