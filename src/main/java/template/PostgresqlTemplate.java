package template;

import base.Table;
import base.TableColumn;
import base.TableIndex;
import base.Tables;

import java.util.Objects;

/*
 * @author yingchun
 * @date 2022/6/26 16:07
 * @comment:
 */
public class PostgresqlTemplate extends BaseTemplate {

    public static String getTables(Tables tables) {

        StringBuilder sb = new StringBuilder();

        for (Table table : tables.getTables()) {
            sb.append(getTable(table));
        }

        return sb.toString();
    }

    private static String getTable(Table table) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTableCreate(table)).append("\n").append("(").append("\n");
        sb.append(getTableColumn(table)).append("\n");
        sb.append(getTablePrimaryKey(table)).append("\n").append(")").append(";").append("\n");
        sb.append(getTableComment(table)).append("\n");
        sb.append(getTableIndex(table)).append("\n");

        return sb.toString();
    }

    private static String getTableCreate(Table table) {

        return "CREATE TABLE " + table.getTableName();
    }

    private static String getTableColumn(Table table) {

        StringBuilder sb = new StringBuilder();

        for (TableColumn column : table.getTableColumns()) {

            sb.append(column.getColumnName()).append(" ").append(Objects.equals(column.getType(), "") ? "CHAR(10)" : column.getType()).append(" ").append((Objects.equals(column.getNullable(), "") || !Objects.equals(column.getNullable(), "1")) ? "null" : "not null").append(" ").append(Objects.equals(column.getDefaultValue(), "") ? "" : "default").append(" ").append(column.getDefaultValue()).append(" ").append(",").append("\n");
        }

        return sb.toString();
    }

    private static String getTablePrimaryKey(Table table) {

        return "constraint pk_" + table.getTableName().replace(' ', '_').toLowerCase() + " " + "primary key " + "(" + (Objects.equals(table.getPrimaryKey().getPrimaryKeyName(), "") ? "id" : table.getPrimaryKey().getPrimaryKeyName()) + ")";
    }

    private static String getTableComment(Table table) {

        return "comment on table " + table.getTableName().replace(' ', '_').toLowerCase() + " is '" + table.getComment() + "';";
    }

    private static String getTableIndex(Table table) {
        StringBuilder sb = new StringBuilder();

        if (table.getTableIndex() == null) {
            return "";
        }

        for (TableIndex index : table.getTableIndex()) {
            sb.append("create index ").append(index.getIndexName()).append(" on ").append(table.getTableName().replace(' ', '_').toLowerCase()).append(" (").append(index.getIndexValueName()).append(");").append("\n");
        }
        return sb.toString();
    }
}

