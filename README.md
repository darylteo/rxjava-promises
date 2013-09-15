# RxJava-Promises #

Promises library for JVM based languages

## What is it? ##

Promises is a pattern for callback-based flow control. This particular implementation of Promises is useful
in places where blocking calls are undesired.

## Notes ##

### Technical Details ###
This library partially implements the [Promises/A+](http://promises-aplus.github.io/promises-spec/) spec with 
the exception of 4.1, as most languages do not have an equivalent of setTimer for Javascript. It is also based 
on [Q](https://github.com/kriskowal/q) for Node.JS, which adds additional conveniences such as reject and finally. 
Finally, it is built using the [RxJava](https://github.com/Netflix/RxJava) library.

### Vert.x ###

The primary motivation for this project is to provide a more convenient means of callback flow-control for the 
[vert.x](http://github.com/eclipse/vert.x) platform. However, I decided that it would be nice if this can be 
used in other places as well. Therefore, the project contains two separate groups of subprojects. 

## Future Work ##

While built on the RxJava library, it currently does not fully support the polyglot nature of the library as 
it is still in flux. Once that work has stabilised, a working implementation can be released.

## Documentation ##

View the README.md in each project for documentation.
