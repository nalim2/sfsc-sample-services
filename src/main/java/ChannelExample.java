import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import servicepatterns.api.*;
import servicepatterns.api.filtering.Filters;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class ChannelExample {
    static BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
    static String ServiceName = "de.universitystuttgart.isw.sfsc.channelExampleService";
    static String ChannelName = "channelName";

    public static void startChannelCreator(String[] args) {
        ByteString uuid = ByteString.copyFromUtf8(UUID.randomUUID().toString());
        SfscServiceApi serverSfscServiceApi = null;
        try {
            serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);
            ChannelGenerator channelGenerator = new ChannelGenerator(serverSfscServiceApi);
            SfscServer channelGeneratorServer = serverSfscServiceApi.channelGenerator(
                    ServiceName,
                    Map.of("id", uuid), // custom Tags
                    ByteString.copyFromUtf8(UUID.randomUUID().toString()), // Server Topic --> will be encapsulated
                    ByteString.copyFromUtf8("ignored"), // Description of the input message type
                    channelGenerator); // channel factory

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    public static void startChannelConsumer(String[] args) {
        SfscServiceApi serverSfscServiceApi = null;
        try {
            serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);
            Map<String, ByteString> genServerTags = serverSfscServiceApi.getServices(ServiceName).stream()
//                    .filter(Filters.byteStringEqualsFilter("id", uuid)) // possibility to include some attribute filtering
                    .findAny().orElseThrow();
            SfscClient client2 = serverSfscServiceApi.client();
            Thread.sleep(1000);
            SfscSubscriber sfscSubscriber = client2.requestChannel(
                    genServerTags,
                    ByteString.EMPTY,
                    1000,
                    message -> {
                        try {
                            System.out.println("generated subscriber received message: " + StringValue.parseFrom(message).toString());
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
            ).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }


    static class ChannelGenerator implements Function<ByteString, SfscPublisher> {

        private final SfscServiceApi sfscServiceApi;

        ChannelGenerator(SfscServiceApi sfscServiceApi) {
            this.sfscServiceApi = sfscServiceApi;
        }

        @Override
        public SfscPublisher apply(ByteString sfscMessage) {
            SfscPublisher publisher = sfscServiceApi.unregisteredPublisher(
                    ChannelName,
                    ByteString.copyFromUtf8(UUID.randomUUID().toString()), // Topic
                    ByteString.copyFromUtf8("String"), // Message Type
                    Collections.emptyMap()); // custom Tags
            publisher.onSubscription(() -> publisher.publish(StringValue.of("myIndividualMessage")));
            return publisher;
        }
    }

}
