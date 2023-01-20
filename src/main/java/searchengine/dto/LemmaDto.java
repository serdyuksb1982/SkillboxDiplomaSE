package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

@Data
@AllArgsConstructor
public class LemmaDto {
    private String lemma;
    private int frequency;
}
