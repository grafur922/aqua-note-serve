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
public class SyncRequest {
    @JsonProperty("lastSyncVersion")
    private Long lastSyncVersion;

    @JsonProperty("localChanges")
    private List<Note> localChanges;
}
