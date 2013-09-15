# RxJava-Promises #

Promises library for JVM based languages

## What is it? ##

Promises is a pattern for callback-based flow control. This particular implementation of Promises is useful
in places where blocking calls are undesired.

## Where to Start

 - If you wish to use RxJava-Promises as a standalone library, see [RxJava-Promises](rxjava-promises/). 
 - If you wish to use RxJava-Promises as a vert.x Module, see [Vertx-Promises](vertx-promises/).

## Notes ##

### Technical Details ###
This library implements **most** of the [Promises/A+](http://promises-aplus.github.io/promises-spec/) spec. 
It is also based on [Q](https://github.com/kriskowal/q) for Node.JS, which adds additional conveniences such 
as reject and finally. Finally, it is built using the [RxJava](https://github.com/Netflix/RxJava) library.

This library is unlike other similar language based implementations (such as Futures, or Groovy Promise)
as it is completely unblocking. It is designed to work with asynchronous-callback-heavy platforms.

### Vert.x ###

The primary motivation for this project is to provide a more convenient means of callback flow-control for the 
[vert.x](http://github.com/eclipse/vert.x) platform. However, I decided that it would be nice if this can be 
used in other places as well. Therefore, the project contains two separate groups of subprojects. 

## Future Work ##

### rx.Observable<> Limitations

While built on the RxJava library, it currently does not fully support the polyglot nature of the library as 
it is still in flux. Once that work has stabilised, a working implementation can be released.

### Promises/A+ spec 

This implementation tries its best to fulfill all the points proposed in the Promises/A+ spec, but is still
lacking in some areas. Work will continue to improve its conformance with the spec.

### More languages

Only Java and Groovy are supported currently. More languages should come (if interest is high).

### Overall Documentation and Testing Quality

This code-base is currently lacking in this area, and I hope I will improve this thoroughly in the future.

## Documentation ##

View the README.md in each project for documentation.
