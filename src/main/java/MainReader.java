import base.*;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import template.PostgresqlTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.Color.*;

/**
 * @author chenjazz
 */
public class MainReader {

    public static void main(String[] args) throws DocumentException {
        System.out.println();
        AnsiConsole.systemInstall();
        if (args.length < 1) {
            throw new IllegalArgumentException("第一个参数必须是pdm文件路径");
        }
        String fileName = args[0];
        System.out.println(Ansi.ansi().fg(YELLOW).a("File  path:") + Ansi.ansi().fg(Ansi.Color.GREEN).a(fileName).toString());

        long start = System.currentTimeMillis();

        Tables tables = new Tables();

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(fileName));
        Element rootElement = document.getRootElement();


        Namespace oNamespace = new Namespace("o", "object");
        Namespace cNamespace = new Namespace("c", "collection");
        Namespace aNamespace = new Namespace("a", "attribute");

        Element rootObject = rootElement.element(new QName("RootObject", oNamespace));

        Element children = rootObject.element(new QName("Children", cNamespace));
        Element model = children.element(new QName("Model", oNamespace));

        List<Element> tableEles = new ArrayList<>();

        // 解析package
        Element packagesEle = model.element(new QName("Packages", cNamespace));
        if (packagesEle != null) {
            List<Element> packageEles = packagesEle.elements(new QName("Package", oNamespace));
            for (Element packageEle : packageEles) {
                Element tablesEle = packageEle.element(new QName("Tables", cNamespace));
                if (tablesEle != null) {
                    tableEles.addAll(tablesEle.elements(new QName("Table", oNamespace)));
                }
            }
        }


        // 直接解析table
        Element tablesEle = model.element(new QName("Tables", cNamespace));
        if (tablesEle != null) {
            tableEles.addAll(tablesEle.elements(new QName("Table", oNamespace)));
        }

        System.out.println(Ansi.ansi().fg(YELLOW).a("Table size:") + Ansi.ansi().fg(Ansi.Color.GREEN).a(tableEles.size()).toString());

        System.out.println(Ansi.ansi().fgDefault().a(" "));

        int i = 0;
        for (Element tableElement : tableEles) {

            Table table = new Table();

            i++;
            Element name = tableElement.element(new QName("Name", aNamespace));
            Element code = tableElement.element(new QName("Code", aNamespace));
            Element comment = tableElement.element(new QName("Comment", aNamespace));
            System.out.println("------>" + Ansi.ansi().fg(BLUE).a("NO." + i) + Ansi.ansi().fg(RED).a(" " + name.getText() + " ") + Ansi.ansi().fg(YELLOW).a(code.getText()) + Ansi.ansi().fgDefault().a("<-------"));

            table.setId(i);
            table.setTableName(name.getText());
            table.setTableCode(code.getText());
            table.setComment(comment == null ? "" : comment.getText());

            // 解析主键
            Element primaryKeyEle = tableElement.element(new QName("PrimaryKey", cNamespace));
            List<String> pkIds = new ArrayList<>();
            if (primaryKeyEle != null) {
                List<Element> pks = primaryKeyEle.elements(new QName("Key", oNamespace));
                for (Element pk1 : pks) {
                    pkIds.add(pk1.attribute("Ref").getValue());
                }
            }

            Element keysEle = tableElement.element(new QName("Keys", cNamespace));
            List<String> pkColumnIds = new ArrayList<>();
            if (keysEle != null) {
                List<Element> keyEleList = keysEle.elements(new QName("Key", oNamespace));
                for (Element keyEle : keyEleList) {
                    Attribute id = keyEle.attribute("Id");
                    if (pkIds.contains(id.getValue())) {
                        List<Element> list = keyEle.element(new QName("Key.Columns", cNamespace)).elements(new QName("Column", oNamespace));
                        for (Element element : list) {
                            pkColumnIds.add(element.attribute("Ref").getValue());
                        }
                    }
                }
            }

            // 解析column
            List<Element> columns = tableElement.element(new QName("Columns", cNamespace)).elements(new QName("Column", oNamespace));

            List<TableColumn> tableColumnsList = new ArrayList<>();

            for (Element columnEle : columns) {

                TableColumn tableColumn = new TableColumn();

                String columnId = columnEle.attribute("Id").getValue();
                Element cname = columnEle.element(new QName("Name", aNamespace));
                Element ccode = columnEle.element(new QName("Code", aNamespace));
                Element cDataType = columnEle.element(new QName("DataType", aNamespace));

                Element cDefaultValue = columnEle.element(new QName("DefaultValue", aNamespace));

                Element cLength = columnEle.element(new QName("Length", aNamespace));
                Element cComment = columnEle.element(new QName("Comment", aNamespace));
                Element nullable = columnEle.element(new QName("Column.Mandatory", aNamespace));

                tableColumn.setId(columnId);
                tableColumn.setColumnName(cname.getText());
                tableColumn.setColumnCode(ccode.getText());
                tableColumn.setColumnLength(cLength == null ? "" : cLength.getText());
                tableColumn.setType(cDataType == null ? "" : cDataType.getText());
                tableColumn.setNullable(nullable == null ? "" : nullable.getText());
                tableColumn.setDefaultValue(cDefaultValue == null ? "" : cDefaultValue.getText());
                tableColumn.setComment(cComment == null ? "" : cComment.getText());

                tableColumnsList.add(tableColumn);
                System.out.println(tableColumn);
            }

            table.setTableColumns(tableColumnsList);
            String primaryKeyName = StringUtils.join(tableColumnsList.stream().filter(tableColumn -> pkColumnIds.contains(tableColumn.getId())).map(TableColumn::getColumnName).collect(Collectors.toSet()), ",");
            String primaryKeyCode = StringUtils.join(tableColumnsList.stream().filter(tableColumn -> pkColumnIds.contains(tableColumn.getId())).map(TableColumn::getColumnCode).collect(Collectors.toSet()), ",");

            TablePrimaryKey primaryKey = new TablePrimaryKey(primaryKeyName, primaryKeyCode);
            table.setPrimaryKey(primaryKey);

            System.out.println(table.getPrimaryKey());

            // 解析index
            Element indexRoot = tableElement.element(new QName("Indexes", cNamespace));
            if (indexRoot != null) {
                List<Element> indexes = indexRoot.elements(new QName("Index", oNamespace));

                List<TableIndex> tableIndexes = new ArrayList<>();

                for (Element index : indexes) {
                    String columnId = index.attribute("Id").getValue();
                    Element cname = index.element(new QName("Name", aNamespace));
                    Element ccode = index.element(new QName("Code", aNamespace));

                    Element indexColumnsRoot = index.element(new QName("IndexColumns", cNamespace));
                    if (indexColumnsRoot != null) {

                        List<Element> oIndexColumns = indexColumnsRoot.elements(new QName("IndexColumn", oNamespace));

                        List<String> indexValues = new ArrayList<>();
                        for (Element indexColumn : oIndexColumns) {
                            indexValues.add(indexColumn.element(new QName("Column", cNamespace)).element(new QName("Column", oNamespace)).attribute("Ref").getValue());
                        }

                        Element cComment = index.element(new QName("Comment", aNamespace));

                        TableIndex tableIndex = new TableIndex();
                        tableIndex.setIndexName(cname.getText());
                        tableIndex.setIndexCode(ccode.getText());
                        Set<String> indexValueName = table.getTableColumns().stream().filter(item -> indexValues.contains(item.getId())).map(TableColumn::getColumnName).collect(Collectors.toSet());
                        Set<String> indexValueCode = table.getTableColumns().stream().map(TableColumn::getId).filter(indexValues::contains).collect(Collectors.toSet());
                        tableIndex.setIndexValueName(StringUtils.join(indexValueName, ","));
                        tableIndex.setIndexValueCode(StringUtils.join(indexValueCode, ","));

                        tableIndexes.add(tableIndex);
                        System.out.println(tableIndexes);
                    }
                }
                table.setTableIndex(tableIndexes);
            }
            tables.getTables().add(table);
            // System.out.println(table);
            // break;
        }

        System.out.println("================================");
        System.out.println("Use time:" + Ansi.ansi().fg(RED).a((System.currentTimeMillis() - start) / 1000F) + Ansi.ansi().fg(DEFAULT).a("s"));
        System.out.println();
        System.out.print(Ansi.ansi().fg(YELLOW).a("说明： "));
        System.out.print(Ansi.ansi().fg(DEFAULT).a(""));
        // System.out.println(table);
        System.out.println();

        System.out.println(Ansi.ansi().fg(YELLOW).a("表： "));
        System.out.println(PostgresqlTemplate.getTables(tables));
    }
}
