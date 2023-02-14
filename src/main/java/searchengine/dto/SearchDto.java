package searchengine.dto;

public record SearchDto(String site, String siteName, String uri, String title, String snippet, float relevance) {
}
