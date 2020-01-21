
import de.unistuttgart.isw.sfsc.example.services.messages.UpdateCounter;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class StarterApplication {
    public static void main(String[] args) {

        UpdateCounter message = UpdateCounter.newBuilder().build();
        String connectionString = "s7://192.168.2.163/0/1";

        try {
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
                wBuilder.addItem("start", "%DB4.DB0.0:BOOL", false);
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
    }
}
