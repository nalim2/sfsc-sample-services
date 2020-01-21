import com.google.protobuf.ByteString;
import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscServiceApiFactory;
import servicepatterns.api.filtering.Filters;
import servicepatterns.api.tagging.Tagger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RegistryInteractionExample {
    private static final BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);

    public static void awaitServiceCreation(String[] args) {
        /*
        try {
            CountDownLatch cdl = new CountDownLatch(1);
            SfscServiceApi serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);
            serverSfscServiceApi.addRegistryStoreEventListener(
                    event -> {
                        if (event.getStoreEventType() == StoreEventType.CREATE
                                && Tagger.getName(event.getData()).equals("Bool")
                                && Filters.byteStringEqualsFilter("id", ByteString.copyFromUtf8("Some sample ID")).test(event.getData())) { //todo not sure yet how is best
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

        */ //possibility will bie discontinued

    }
}
