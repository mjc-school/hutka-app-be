package by.mjc.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SearchRoutesRequest {
    private List<String> tags;
}
