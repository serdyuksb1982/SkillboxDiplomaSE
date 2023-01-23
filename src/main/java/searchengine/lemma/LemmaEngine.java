package searchengine.lemma;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.config.LemmaConfiguration;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class LemmaEngine {
    private final LemmaConfiguration lemmaConfiguration;

    public Map<String, Integer> getLemmaMap(String text) {

        text = arrayContainsRussianWords(text);
        HashMap<String, Integer> lemmaList = new HashMap<>();
        String[] elements = text.toLowerCase(Locale.ROOT).split("\\s+");
        for (String el : elements) {
            List<String> wordsList = getLemma(el);
            for (String word : wordsList) {
                int count = lemmaList.getOrDefault(word, 0);
                lemmaList.put(word, count + 1);
            }
        }
        return lemmaList;
    }

    public List<String> getLemma(String word) {
        List<String> lemmaList = new ArrayList<>();
        try {
            List<String> baseRusForm = lemmaConfiguration.luceneMorphology().getNormalForms(word);
            if (!word.isEmpty() && !isCorrectWordForm(word)) {
                lemmaList.addAll(baseRusForm);
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return lemmaList;
    }

    private boolean isCorrectWordForm(String word) throws IOException {
        List<String> morphForm = lemmaConfiguration.luceneMorphology().getMorphInfo(word);
        for (String l : morphForm) {
            if (l.contains("ПРЕДЛ")
                    || l.contains("СОЮЗ")
                    || l.contains("МЕЖД")
                    || l.contains("МС")
                    || l.contains("ЧАСТ")
                    || l.length() <= 3) {
                return true;
            }
        }
        return false;
    }

    private String arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ").trim();
    }
}
