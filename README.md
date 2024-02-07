# Arc XP IFX Java SDK

The Arc XP IFX Java SDK enables Java developers to easily work with arc services and build solutions on the arc platform.

# Getting Started

To run the SDK you will need Java 1.8+

1. Create a new Java project using your choice of build tool.

2. Install the SDK

**Maven**
```xml
    <dependencies>
        <dependency>
            <groupId>com.arcxp.platform.sdk</groupId>
            <artifactId>arcxp-ifx-java-sdk</artifactId>
            <version>{arc-sdk.version}</version>
        </dependency>
    </dependencies>
```

**SBT**
```
libraryDependencies += "com.arcxp.platform.sdk" % "arc-platform-sdk" % "1.0-SNAPSHOT"
```

**Gradle**
```
compileOnly group: 'com.arcxp.platform.sdk', name: 'arc-platform-sdk', version: '1.0-SNAPSHOT'
```
3. Create an asynchronous event handler

Events are fired for actions happening in the arc platform. For example when a new user registration happens, a USER_SIGN_UP event if fired. 
An EventHandler allows you to catch this event and perform custom actions against arc or other systems.

The event type is mapped to the handling class by the @ArcAsyncEvent({"USER_SIGN_UP"} annotation. 
This annotation accepts an array of arc events to map multiple to a single handler.

Note: Asynchronous events are fired asynchronously and code running within an event handler will have no performance 
impacts on your arc environments.

```java
import com.arcxp.platform.sdk.annotations.ArcAsyncEvent;
import com.arcxp.platform.sdk.handlers.async.EventHandler;
import com.arcxp.platform.sdk.handlers.async.EventPayload;

@ArcAsyncEvent({"USER_SIGN_UP"})
public class UserRegistrationEvent extends EventHandler {
    @Override
    public void handle(EventPayload eventPayload) {
        System.out.println("Received event " + eventPayload.getType() + " " + eventPayload.getBody().getString("email"));
    }
}
```

4. Create a synchronous event handler

Arc also allows for custom code execution within the platform to perform custom business logic. 
In the below example we are modifying the request payload and overriding the sku.

Note: Synchronous events are fired synchronously and should perform fast to minimize performance impacts 
on your arc environment.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;

import com.arcxp.platform.sdk.annotations.ArcSyncEvent;
import com.arcxp.platform.sdk.handlers.sync.RequestException;
import com.arcxp.platform.sdk.handlers.sync.RequestHandler;
import com.arcxp.platform.sdk.handlers.sync.RequestPayload;


@ArcSyncEvent({"TEST_SYNC_EVENT"})
public class SampleSyncEventHandler extends RequestHandler {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void handle(RequestPayload requestPayload) {
        ObjectNode request = requestPayload.getBody();
        if (request.get("sku").asText() != null ) {
            // Creating response body
            ObjectNode response = objectMapper.createObjectNode();
            response.put("sku", "test2");
            // Set to the body of requestPayload
            requestPayload.setBody(response);
        } else {
            throw new RequestException("E0002:Unknown type");
        }
    }
}
```

## Component Scanning
The `basePackage` of your integration when deployed is set to `com.{org}`. This means that you must change the
starter code from `com.myorg` to `com.{org}` where `{org}` is your organization ID. Otherwise, Spring won't
be able to find your handlers that extend `RequestHandler` or `EventHandler`.

## Local Testing
The Java SDK can be utilized to locally test events invoking your handlers. After building your project, the command
`java -jar ./target/<my-jar-here>.jar` will spin up a server at
http://localhost:8080 that can be used to test your event handlers. By making a POST request to
http://localhost:8080/ifx/local/invoke, the payload body will be directly sent to your handler
based on the `key` property.

The only required field is `key` when locally testing. If any fields are omitted on a payload using
the local development server, default values will be provided to the handlers.

Example payload to use in a POST request:
```json
{
    "key": "story:create",
    "body": {
        "subheadlines": { 
            "basic": "My Subheadline here"
        }
    }
}
```

If your handler is marked with the annotation `@ArcSyncEvent`,
then your payload on the local testing server must include a typeId parameter like so
```json
{
    "key": "commerce:CHARGE",
    "typeId": 5,
    "body": {
        "subheadlines": { 
            "basic": "My Subheadline here"
        }
    }
}
```