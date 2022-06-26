package base;

import lombok.*;

import java.util.List;

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
public class Table {
    private Integer id;
    private String tableName;
    private String tableCode;
    private List<TableColumn> tableColumns;
    private TablePrimaryKey primaryKey;
    private List<TableIndex> tableIndex;
    private String comment;
}
