# Vertx-Promises

### Maven
```XML
<dependency>
  <groupId>com.darylteo.vertx</groupId>
  <artifactId>vertx-promises-{lang}</artifactId>
  <version>1.2.0</version>
</dependency>
````
### mod.json
```javascript
{
  includes: "com.darylteo.vertx~vertx-promises-{lang}~1.2.0"
}
````


## Vertx Specific Functionality

The main addition to the Vertx version of RxJava-Promises is its ability to be used as a Vertx Handler. 

```java
private Promise<Buffer> readAFile(String filename) {
  final Promise<Buffer> p = new Promise();

  vertx.fileSystem().readFile(filname, new Handler<AsyncResult<Buffer>>() {
    @Override
    public void handle(AsyncResult<Buffer> event) {
      if (event.succeeded()) {
        p.fulfill(event.result());
      } else {
        p.reject(event.cause());
      }
    }
  });

  return p;
}
````

In the future there will be a more convenient way of coping with AsyncResult's succeed() value.
