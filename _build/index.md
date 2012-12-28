---
layout: index
---
# Promises for Vert.x

## Why

Because [Callback Hell](http://callbackhell.com/).

Vert.x is a relatively new platform utilising the event loop, usable by multiple languages (and not just Javascript). These languages do not play very well with nested levels of callbacks creating a mess of callbacks that are difficult to read and maintain.

## How

Promises aims to flatten these "Callback Pyramids" in a way that is both readable and maintainable.

As an example, you can turn this:

    asyncStep1(new Handler<String>(){
      public void handle(String value){
        ...
        asyncStep2(new Handler<String>(){
          public void handle(String value){
            ...
            asyncStep3(new Handler<String>(){
              public void handle(String value){
                ...
              }
            });
          }
        });
      }
    });

Into This:

    Promise<Void> promise = asyncGetString()
      .then(new RepromiseHandler<String, String>() {
        public Promise<String> handle(final String result) {
          ...
        }
      })
      .then(new PromiseHandler<String, String>() {
        public String handle(String result) {
          ...
        }
      })
      .then(new PromiseHandler<String, Void>() {
        public Void handle(String result) {
          ...
        }
      });

## Why Not Java Futures/JavaFlow/Continuations?

All of the above technologies are blocking (i.e. the executing Thread has to wait before returning, making them incompatible with the Vert.x event loop. The goal of this project was to implement a non-blocking Promises api that is built on the event loop, even in a multi-core environment, by guaranteeing the sequence of handlers that are run.

## Language Support

I plan to support all supported Vert.x languages, however only Java is supported at present.

## Inspiration
Most of the concepts comes from Kris Kowal's [Q](//github.com/kriskowal/q) CommonJS module. Major liberties and workarounds were made to get it to play nicely with Java.
