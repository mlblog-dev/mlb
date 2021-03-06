package org.github.mlb.search.biz.listener;

import com.github.mlb.common.model.BinlogDTO;
import org.github.mlb.search.biz.enums.IndexEnum;
import org.github.mlb.search.biz.listener.handler.DeleteEventActionHandler;
import org.github.mlb.search.biz.listener.handler.InsertEventActionHandler;
import org.github.mlb.search.biz.listener.handler.UpdateEventActionHandler;
import org.github.mlb.search.biz.listener.model.Row;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JiHongYuan
 * @date 2021/9/21 14:26
 */
public class EventActionAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventActionAdapter.class);

    /**
     * 缓存 class中所有字段
     */
    private static final Map<Class<?>, List<String>> CLAZZ_FIELD_CACHE_MAP = new HashMap<>();

    /**
     * handlers
     */
    private final List<EventActionHandler> eventHandlerList;

    /**
     * es client
     */
    private final RestHighLevelClient restHighLevelClient;

    public EventActionAdapter(RestHighLevelClient restHighLevelClient) {
        eventHandlerList = new ArrayList<>();
        eventHandlerList.add(new DeleteEventActionHandler());
        eventHandlerList.add(new InsertEventActionHandler());
        eventHandlerList.add(new UpdateEventActionHandler());

        this.restHighLevelClient = restHighLevelClient;
    }

    public void handler(BinlogDTO binlogDTO) {
        String eventAction = binlogDTO.getEventAction();
        IndexEnum indexEnum = IndexEnum.get(binlogDTO.getDatabase() + "." + binlogDTO.getTable());

        if (indexEnum == null) {
            throw new RuntimeException("can not find " + binlogDTO.getDatabase() + "." + binlogDTO.getTable() + " index match index class");
        }

        List<Row> rows = new ArrayList<>();
        for (Serializable[] row : binlogDTO.getRows()) {
            rows.add(new Row(row[0], convertArrayToMap(row, indexEnum.getClazz())));
        }

        EventActionHandler handler = getHandler(eventAction);
        if (handler == null) {
            throw new RuntimeException("can not find " + eventAction + " match handler");
        }

        BulkRequest bulkRequest = handler.handler(indexEnum.getIndex(), rows);
        try {
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(bulkResponse);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

    }

    /**
     * 将数组 转换成  map clazz field, array[i]
     *
     * @param row   table row data
     * @param clazz convert class
     */
    private Map<String, Object> convertArrayToMap(Serializable[] row, Class<?> clazz) {
        List<String> fieldNameList = getClassFieldList(clazz);

        Map<String, Object> map = new HashMap<>(row.length);
        for (int i = 0; i < row.length; i++) {
            map.put(fieldNameList.get(i), row[i]);
        }

        return map;
    }

    /**
     * 根据class获取所有字段
     * @param clazz class
     * */
    private List<String> getClassFieldList(Class<?> clazz) {
        if (CLAZZ_FIELD_CACHE_MAP.get(clazz) == null) {
            List<String> fieldNameList = new ArrayList<>();

            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                fieldNameList.add(field.getName());
            }
            CLAZZ_FIELD_CACHE_MAP.put(clazz, fieldNameList);
        }

        return CLAZZ_FIELD_CACHE_MAP.get(clazz);
    }

    @Nullable
    private EventActionHandler getHandler(String eventAction) {
        for (EventActionHandler eventHandler : eventHandlerList) {
            boolean support = eventHandler.support(eventAction);
            if (support) {
                return eventHandler;
            }
        }
        return null;
    }

}