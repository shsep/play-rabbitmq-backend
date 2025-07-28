package com.shs.playrabbitmqbackend.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaService {

    private final OllamaConfigurationProperties ollamaConfigurationProperties;
    private final ObjectMapper objectMapper;
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public String getBotResponse(String query) {
        OkHttpClient client = new OkHttpClient();

        // JSON 필드 값 내 이중따옴표 이스케이프 처리
        String escapedQuery = query.replace("\"", "\\\"");

        // 요청 본문 준비
        String model = ollamaConfigurationProperties.getModel();
        String jsonBody = String.format("{\"model\": \"%s\", \"prompt\": \"%s\"}", model, escapedQuery);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url(OLLAMA_URL)
            .post(body)
            .build();

        StringBuilder completeResponse = new StringBuilder();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Ollama API returned error response: HTTP {} - {}", response.code(), response.message());
                return "Failed to get a response from the AI.";
            }

            if (response.body() != null) {
                // 응답을 라인 단위로 처리
                String[] lines = response.body().string().split("\\r?\\n");
                for (String line : lines) {
                    try {
                        JsonNode json = objectMapper.readTree(line);
                        if (json.has("response")) {
                            completeResponse.append(json.get("response").asText());
                        }

                        // `done` 필드가 true일 경우 처리 완료
                        if (json.has("done") && json.get("done").asBoolean()) {
                            break;
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing partial response: {}", line, e);
                    }
                }
            } else {
                log.warn("Response body from Ollama API is empty");
                return "AI response is empty. Please try again.";
            }
        } catch (IOException e) {
            log.error("Error while communicating with Ollama API", e);
            return "Couldn't connect to the AI service.";
        }

        // 최종 응답에서 불필요한 태그 제거
        return removeTags(completeResponse.toString().strip()); // 앞뒤 공백 제거 후 태그 제거
    }

    /**
     * 문자열에서 특정 태그를 제거하는 헬퍼 메서드
     * @param input 원본 문자열
     * @return 태그가 제거된 문자열
     */
    private String removeTags(String input) {
        return input.replaceAll("<.*?>", ""); // 모든 태그 형식 < ... > 제거
    }
}