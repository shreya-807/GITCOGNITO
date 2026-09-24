// package devPilot.backend.Services.ai;

// import java.util.List;

// import org.springframework.ai.document.Document;
// import org.springframework.stereotype.Component;

// import com.fasterxml.jackson.databind.ObjectMapper;

// import devPilot.backend.dto.CitationDto;
// import lombok.RequiredArgsConstructor;

// /**
//  * Converts vector-store {@link Document}s into API citations
//  */
// @Component
// @RequiredArgsConstructor
// public class CitationMapper {

//     private final ObjectMapper jsonMapper;

//     public CitationDto fromDocument(Document document) {
//         var meta = document.getMetadata();
//         if (meta == null) {
//             return new CitationDto(null, null, null, null);
//         }

//         return new CitationDto(
//                 stringVal(meta.get("filePath")),
//                 intVal(meta.get("startLine")),
//                 intVal(meta.get("endLine")),
//                 stringVal(meta.get("language")));
//     }

//     public String toJson(List<CitationDto> citations) {
//         try {
//             return jsonMapper.writeValueAsString(citations);
//         } catch (Exception e) {
//             return "[]";
//         }
//     }

//     private String stringVal(Object value) {
//         return value == null ? null : String.valueOf(value);
//     }

//     private Integer intVal(Object value) {
//         if (value == null) {
//             return null;
//         }
//         if (value instanceof Number number) {
//             return number.intValue();
//         }
//         try {
//             return Integer.valueOf(String.valueOf(value));
//         } catch (NumberFormatException e) {
//             return null;
//         }
//     }
// }
package devPilot.backend.Services.ai;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import devPilot.backend.dto.CitationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Converts vector-store {@link Document}s into API citations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CitationMapper {

    private final ObjectMapper jsonMapper;

    public CitationDto fromDocument(Document document) {
        var meta = document.getMetadata();
        if (meta == null) {
            return new CitationDto(null, null, null, null);
        }

        return new CitationDto(
                stringVal(meta.get("filePath")),
                intVal(meta.get("startLine")),
                intVal(meta.get("endLine")),
                stringVal(meta.get("language")));
    }

    public String toJson(List<CitationDto> citations) {
        try {
            return jsonMapper.writeValueAsString(citations);
        } catch (Exception e) {
            return "[]";
        }
    }

    public List<CitationDto> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return jsonMapper.readValue(json, new TypeReference<List<CitationDto>>() {});
        } catch (Exception e) {
            log.error("Error deserializing citations JSON", e);
            return List.of();
        }
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer intVal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}