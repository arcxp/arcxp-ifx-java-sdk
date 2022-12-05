# **Arc XP IFX Java SDK**

The Arc XP IFX Java SDK enables Java developers to easily work with arc services and build solutions on the arc platform.

# **Getting Started**

To run the SDK you will need Java 1.8+

**1. Create a new Java project using your choice of build tool.**

**2. Install the SDK**

**Maven**

    <dependencies>
        <dependency>
            <groupId>com.arcxp.platform.sdk</groupId>
            <artifactId>arcxp-ifx-java-sdk</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

**SBT**

    libraryDependencies += "com.arcxp.platform.sdk" % "arc-platform-sdk" % "1.0-SNAPSHOT"

**Gradle**

    compileOnly group: 'com.arcxp.platform.sdk', name: 'arc-platform-sdk', version: '1.0-SNAPSHOT'

**3. Create an asynchronous event handler**

Events are fired for actions happening in the arc platform. For example when a new user registration happens a USER_SIGN_UP event if fired. An EventHandler allows you to catch this event and perform custom actions against arc or other systems.

The event type is mapped to the handling class by the @ArcAsyncEvent({"USER_SIGN_UP"} annotation. This annotation accepts an array of arc events to map multiple to a single handler.

Note: Asynchronous events are fired asynchronously and code running within an event handler will have no performance impacts on your arc environments.

````
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
````

**3. Create a synchronous event handler**

Arc also allows for custom code execution within the platform to perform custom business logic. In the below example we are modifying the request payload and overriding the sku.

Note: Synchronous events are fired synchronously and should perform fast to minimize performance impacts on your arc environment.

````
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

````

**Local Development**

You can run your project locally

Set the following env variables

```
export ENV=local
```

```
mvnw spring-boot:run
```

Running this will connect you to your hosted sandbox environment and let you debug and test your code in your IDE.

Note: When making http requests to arc you need to add your developerkey as a request header for the hooks to be handled by your local environment. --header

Ex:

    'developerKey':'mydeveloperkey'

**Deployment**

Deployment can be done via Jenkins

See [Jenkins Integration Tab](http://jenkins.subscriptions.aws.arc.pub/view/Integration/)

