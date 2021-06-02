package by.mjc.entities;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;

@DynamoDBDocument
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private String name;
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private Position coords;
}
