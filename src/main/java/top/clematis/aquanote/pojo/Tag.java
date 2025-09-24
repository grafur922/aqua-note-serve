package top.clematis.aquanote.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    private Integer tagId;
    private String tagName;
    private String userId;
}
