# RxJava-Promises #

Promises library for Vert.x 

## What is it? ##

Promises is a pattern for callback-based flow control. This particular implementation of Promises is useful
in places where blocking calls are undesired.

## Notes ##

This library partially implements the Promises/A+ spec with the exception of 4.1, as most languages do not have
an equivalent of setTimer for Javascript. It is also based on Q for Node.JS, which adds additional conveniences
such as reject and finally.

## Future Work ##

While built on the RxJava library, it currently does not fully support the polyglot nature of the library as 
it is still in flux. Once that work has stabilised, a working implementation can be released.

## Documentation ##

View the README.md in each project for documentation.
