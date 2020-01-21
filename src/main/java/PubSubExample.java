import com.google.protobuf.ByteString;
import com.google.protobuf.StringValue;
import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.example.services.messages.UpdateCounter;
import servicepatterns.api.SfscPublisher;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscServiceApiFactory;
import servicepatterns.api.SfscSubscriber;
import servicepatterns.api.filtering.Filters;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class PubSubExample {
    static BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
    static String ServiceName = "de.universitystuttgart.isw.sfsc.publisherExampleService";

    public static void startPublisher(String[] args) {
        ByteString uuid = ByteString.copyFromUtf8(UUID.randomUUID().toString());
        SfscServiceApi serverSfscServiceApi = null;
        try {
            serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);
            SfscPublisher examplePublisher = serverSfscServiceApi
                    .publisher(ServiceName, ByteString.copyFromUtf8(UUID.randomUUID().toString()), ByteString.copyFromUtf8("MessageType"), Map.of("id", uuid));


            examplePublisher.publish(UpdateCounter.getDefaultInstance());
            // singel example publishing event which should be triggert some where else in the code

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    public static void startSubscriber(String[] args) {
        try {
            SfscServiceApi serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);
            Map<String, ByteString> pubTags = serverSfscServiceApi.getServices("Bool").stream()
                    //       .filter(Filters.byteStringEqualsFilter("id", uuid))
                    .findAny().orElseThrow();

            SfscSubscriber subscriber = serverSfscServiceApi.subscriber(pubTags, bytestring -> System.out.println("Received message on subscriber"));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }


}
