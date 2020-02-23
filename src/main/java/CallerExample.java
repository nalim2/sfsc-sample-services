import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.commonjava.util.StoreEvent;
import de.unistuttgart.isw.sfsc.example.services.messages.UpdateCounter;
import de.unistuttgart.isw.sfsc.example.services.messages.VoidCall;
import servicepatterns.api.SfscClient;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscServiceApiFactory;
import servicepatterns.api.filtering.Filters;
import servicepatterns.api.tagging.Tagger;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class CallerExample {
    static BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);

    public static void main(String[] args) {

        try {
            SfscServiceApi clientSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);

            try {
                CountDownLatch cdl = new CountDownLatch(1);
                SfscServiceApi serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);
                serverSfscServiceApi.addRegistryStoreEventListener(
                        event -> {
                            if (event.getStoreEventType() == StoreEvent.StoreEventType.CREATE
                                    && Tagger.getName(event.getData()).equals("isw.sfsf.readPLC")
                                    && Filters.byteStringEqualsFilter("id", ByteString.copyFromUtf8("MeinTestService7")).test(event.getData())) {
                                System.out.println("matching service found");
                                cdl.countDown();
                            }
                        }
                );
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }


            Map<String, ByteString> exampleServiceTags = clientSfscServiceApi.getServices("isw.sfsf.readPLC")
                    .stream()
                    .filter(Filters.byteStringEqualsFilter("id", ByteString.copyFromUtf8("MeinTestService7"))) // example to set a filter on attributes
                    .findAny().orElseThrow();

            SfscClient client = clientSfscServiceApi.client();
            client.request(exampleServiceTags, VoidCall.newBuilder().setName("My Name").setStart(false).build(),
                    replyConsumer(), 5000, () -> System.out.println("timeout"));


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    static Consumer<ByteString> replyConsumer() {
        return response -> {
            try {
                UpdateCounter updateCounter = UpdateCounter.parseFrom(response);
                System.out.println("Read request got response: \n" + updateCounter);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        };
    }

}
