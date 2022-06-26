package base;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/*
 * @author yingchun
 * @date 2022/6/26 15:51
 * @comment:
 */
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class Tables {
    private List<Table> tables;

    public Tables() {
        tables = new ArrayList<>();

    }
}
