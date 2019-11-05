package com.xxl.executor.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.supconit.data.asset.crud.services.CrudAccessService;
import com.xxl.job.core.entity.presto.catalog.DataAccessBackResult;
import com.xxl.job.core.entity.presto.catalog.DataContainer;
import com.xxl.job.core.entity.presto.catalog.DataElement;
import com.xxl.job.core.entity.presto.catalog.DataItem;
import com.xxl.job.core.util.FieldUtil;
import com.xxl.job.core.util.HttpClientUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * executor-api client, test
 * <p>
 * Created by xuxueli on 17/5/12.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:applicationcontext-*.xml")
public class DemoJobHandlerTest {

    @Resource
    CrudAccessService crudAccessService;

    @Test
    public void curdTest() {
        String dataManageAddress = "http://10.10.77.135:8090/data_manage_web";

        String[][] results = new String[][] {{"2DW7GCWES","sa123","321412","234",null},{"2DYSR5MW3","sadga","sadg","asdga",null},{"teesss2","ssss","sss","ss",null},{"test","test","test","test",null}};
        String itemId = "4761";

        // 根据itemId获取业务库中有哪些字段
        String itemJson = HttpClientUtil.getInstance().sendHttpGet(5000,
                dataManageAddress + "/catalog/item/elements/" + itemId);
        Map<String, List<DataElement>> dataElementMap = JSON.parseObject(itemJson,
                new TypeReference<Map<String, List<DataElement>>>() {
                });

        System.out.println("dataElementMap: " + JSON.toJSONString(dataElementMap));

        // 根据itemId获取业务库信息
        String containerJson = HttpClientUtil.getInstance().sendHttpGet(5000,
                dataManageAddress + "/catalog/item/containers/" + itemId);
        List<DataContainer> dataContainerList = JSON.parseObject(containerJson,
                new TypeReference<List<DataContainer>>() {
                });

        System.out.println("containerJson: " + JSON.toJSONString(containerJson));

        List<DataElement> dataElementList = dataElementMap.get(dataContainerList.get(0).getDbName());

        // 手动构建数据容器需要的JSON
        List<String> appendList = new ArrayList<>();
        for (DataElement item : dataElementList) {
            appendList.add("\"" + FieldUtil.underlineToCamel(item.getFieldName()) + "\":"); // 格式 "testAddtime":
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int x = 0; x < results.length; x++) {
            stringBuilder.append("{");
            for (int y = 0; y < results[x].length; y++) {
                stringBuilder.append(appendList.get(y) + "\"" + results[x][y] + "\"");
                if (y < results[x].length - 1) {
                    stringBuilder.append(",");
                }
            }
            stringBuilder.append("}");
            if (x < results.length - 1) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");

        // 获取dataKey
        String itemDetailJson = HttpClientUtil.getInstance().sendHttpGet(5000,
                dataManageAddress + "/catalog/item/detail/" + itemId);
        DataItem itemDetail = JSON.parseObject(itemDetailJson,
                new TypeReference<DataItem>() {
                });

        System.out.println(JSON.toJSONString(itemDetail));
        String dataKey = itemDetail.getDataKey();

        System.out.println("dataKey: " + dataKey);
        System.out.println("stringBuilder: " + stringBuilder.toString());

        String topic = crudAccessService.batchCreate(dataKey, "763", stringBuilder.toString());
        System.out.println("===========");
        System.out.println("topic: " + topic);
        System.out.println("===========");

        DataAccessBackResult dataAccessBackResult = JSON.parseObject(topic, DataAccessBackResult.class);
        System.out.println("===========");
        System.out.println(JSON.toJSONString(dataAccessBackResult));
        System.out.println("===========");
    }

}
