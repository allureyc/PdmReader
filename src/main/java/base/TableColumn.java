package base;

import lombok.*;

/*
 * @author yingchun
 * @date 2022/6/25 22:26
 * @comment:
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TableColumn {
    private String id;
    private String columnName;
    private String columnCode;
    private String columnLength;
    private String type;
    private String nullable;
    private String defaultValue;
    private String comment;
}

