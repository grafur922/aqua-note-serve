package top.clematis.aquanote.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import top.clematis.aquanote.pojo.Note;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponse {
    @JsonProperty("currentSyncVersion")
    private Long currentSyncVersion;

    @JsonProperty("serverChanges")
    private List<Note> serverChanges;

    @JsonProperty("conflicts")
    private List<ConflictNote> conflicts;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictNote {
        @JsonProperty("serverVersion")
        private Note serverVersion;

        @JsonProperty("clientVersion")
        private Note clientVersion;

        @JsonProperty("conflictReason")
        private String conflictReason;
    }
}
