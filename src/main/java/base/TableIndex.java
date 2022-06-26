package base;

import lombok.*;

/*
 * @author yingchun
 * @date 2022/6/25 22:25
 * @comment:
 */

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TableIndex {
    private String indexName;
    private String indexCode;
    private String indexValueName;
    private String indexValueCode;
    private String comment;
}
