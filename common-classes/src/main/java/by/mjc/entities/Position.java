package by.mjc.entities;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;

@DynamoDBDocument
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private Double lat;
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private Double lng;
}
