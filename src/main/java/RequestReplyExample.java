import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.example.services.messages.UpdateCounter;
import de.unistuttgart.isw.sfsc.example.services.messages.VoidCall;
import de.unistuttgart.isw.sfsc.framework.descriptor.RegexDefinition;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import servicepatterns.api.SfscClient;
import servicepatterns.api.SfscServer;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscServiceApiFactory;
import servicepatterns.api.filtering.Filters;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class RequestReplyExample {
    static BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
    static ByteString uuid = ByteString.copyFromUtf8(UUID.randomUUID().toString());

    public static void StartRequester(String[] args) {
        try {
            SfscServiceApi clientSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);

            Map<String, ByteString> exampleServiceTags = clientSfscServiceApi.getServices("isw.sfsf.readPLC")
                    .stream()
//                    .filter(Filters.byteStringEqualsFilter("id", uuid)) // example to set a filter on attributes
                    .findAny().orElseThrow();

            SfscClient client = clientSfscServiceApi.client();
            client.request(exampleServiceTags, VoidCall.getDefaultInstance(),
                    replyConsumer(), 1000, () -> System.out.println("timeout"));


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void StartReplier(String[] args) {

        try {
            SfscServiceApi serverSfscServiceApi = SfscServiceApiFactory.getSfscServiceApi(bootstrapConfiguration1);
            SfscServer server = serverSfscServiceApi.server("isw.sfsf.readPLC",
                    ByteString.copyFromUtf8("plc4xtype"),
                    ByteString.copyFromUtf8(UUID.randomUUID().toString()),
                    ByteString.copyFromUtf8("plc4xtype"),
                    RegexDefinition.newBuilder()
                            .addRegexes(RegexDefinition.VarRegex.newBuilder()
                                    .setVarName("name")
                                    .setStringRegex(RegexDefinition.VarRegex.StringRegex.newBuilder().setRegex("*").build())
                                    .build())
                            .build(),
                    Map.of("id", uuid),
                    replyFunction(), // Hier wird die Reply Funktion hineingegeben
                    1000,
                    100,
                    3);


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }


    static Function<ByteString, AckServerResult> replyFunction() {
        return requestByteString -> {
            try {
                VoidCall request = VoidCall.parseFrom(requestByteString);
                // Do cool Stuff with the request


                // reply Answer
                UpdateCounter reply = UpdateCounter.newBuilder().setName("TollerName").setValue("Interesanter Wert").build();
                return serverResult(reply);
            } catch (Exception e) {
                e.printStackTrace();
                return serverResult(UpdateCounter.getDefaultInstance());
            }
        };
    }

    static AckServerResult serverResult(Message response) {
        return new AckServerResult(
                response,
                () -> System.out.println("plc4x server acknowledge succeeded"),
                () -> System.out.println("plc4x server acknowledge didnt succeed")
        );

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
