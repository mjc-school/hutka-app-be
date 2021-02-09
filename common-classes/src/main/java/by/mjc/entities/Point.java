package by.mjc.entities;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.*;

import java.util.List;

@DynamoDBDocument
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point {
    @Getter(onMethod = @__({@DynamoDBHashKey, @DynamoDBAutoGeneratedKey}))
    private String id;
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private String region;
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private String sight;
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private String description;
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private String imageUrl;
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private Double latitude;
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private Double longitude;
    @Getter(onMethod = @__({@DynamoDBAttribute}))
    private List<String> tags;
}
