
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import de.unistuttgart.isw.sfsc.adapter.BootstrapConfiguration;
import de.unistuttgart.isw.sfsc.example.services.messages.UpdateCounter;
import de.unistuttgart.isw.sfsc.example.services.messages.VoidCall;
import de.unistuttgart.isw.sfsc.framework.descriptor.RegexDefinition;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import servicepatterns.api.SfscServer;
import servicepatterns.api.SfscServiceApi;
import servicepatterns.api.SfscServiceApiFactory;
import servicepatterns.basepatterns.ackreqrep.AckServerResult;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;


public class StarterApplication {
    static BootstrapConfiguration bootstrapConfiguration1 = new BootstrapConfiguration("127.0.0.1", 1251);
    static ByteString uuid = ByteString.copyFromUtf8(UUID.randomUUID().toString());

    public static void main(String[] args) {

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
                    Map.of("id", ByteString.copyFromUtf8("MeinTestService7")),
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
                try {
                    String connectionString = "s7://192.168.2.163/0/1";
                    PlcConnection plcConnection = new PlcDriverManager().getConnection(connectionString);
                    // Check if this connection support reading of data.
                    if (plcConnection.getMetadata().canRead()) {
                        // Create a new read request:
                        // - Give the single item requested the alias name "value"
                        PlcReadRequest.Builder builder = plcConnection.readRequestBuilder();
                        builder.addItem("start", "%DB4.DB0.0:BOOL");
                        builder.addItem("sens_in", "%DB4.DB0.1:BOOL");
                        builder.addItem("sens_out", "%DB4.DB0.2:BOOL");
                        builder.addItem("counter", "%DB4.DB6.0:INT");
                        PlcReadRequest readRequest = builder.build();
                        PlcReadResponse response = readRequest.execute().get();
                        for (String fieldName : response.getFieldNames()) {
                            if (response.getResponseCode(fieldName) == PlcResponseCode.OK) {
                                int numValues = response.getNumberOfValues(fieldName);
                                // If it's just one element, output just one single line.
                                if (numValues == 1) {
                                    System.out.println("Value[" + fieldName + "]: " + response.getObject(fieldName));
                                }
                                // If it's more than one element, output each in a single row.
                                else {
                                    System.out.println("Value[" + fieldName + "]:");
                                    for (int i = 0; i < numValues; i++) {
                                        System.out.println(" - " + response.getObject(fieldName, i));
                                    }
                                }
                            }
                            // Something went wrong, to output an error message instead.
                            else {
                                System.out.println("Error[" + fieldName + "]: " + response.getResponseCode(fieldName).name());
                            }
                        }

                        PlcWriteRequest.Builder wBuilder = plcConnection.writeRequestBuilder();
                        wBuilder.addItem("start", "%DB4.DB0.0:BOOL", request.getStart());
                        PlcWriteRequest writeRequest = wBuilder.build();
                        PlcWriteResponse writeResponse = writeRequest.execute().get();
                        //wBuilder.addItem("sens_in", "%DB4.DB0.1:BOOL");
                        //wBuilder.addItem("sens_out", "%DB4.DB0.2:BOOL");
                        //wBuilder.addItem("counter", "%DB4.DB6.0:INT");
                    }


                } catch (PlcConnectionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

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



}
