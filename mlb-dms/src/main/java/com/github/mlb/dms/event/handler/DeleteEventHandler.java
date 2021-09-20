package com.github.mlb.dms.event.handler;

import com.github.mlb.dms.event.EventHandler;
import com.github.mlb.dms.event.model.RowsData;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.EventData;

/**
 * @author JiHongYuan
 * @date 2021/9/20 0:55
 */
public class DeleteEventHandler implements EventHandler {

    @Override
    public boolean support(EventData event) {
        return event instanceof DeleteRowsEventData;
    }

    @Override
    public RowsData handler(EventData event) {
        DeleteRowsEventData data = (DeleteRowsEventData) event;

        RowsData rowsData = new RowsData();
        rowsData.setTableId(data.getTableId());
        rowsData.setRows(data.getRows());

        return rowsData;
    }

}