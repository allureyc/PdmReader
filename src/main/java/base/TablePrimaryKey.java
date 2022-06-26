package base;

import lombok.*;

/*
 * @author yingchun
 * @date 2022/6/26 15:27
 * @comment:
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TablePrimaryKey {
    private String primaryKeyName;
    private String primaryKeyCode;
}
