package task201709;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.data.DaX.reader.DSExcelReader;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/9/26
 * Time:下午2:45
 */
public class FormatRecord {

    public static void main(String[] args) throws Exception {
        formatExcel();

    }

    private static void formatExcel() throws  Exception{

        String inputFile ="../../华西病人住院信息表.xlsx";
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = sxssfWorkbook.createSheet();

        JSONObject config = new JSONObject();
        config.put("filename",inputFile);
        config.put("source_type","excel");

        DSExcelReader dsExcelReader1 = new DSExcelReader(config);
        JSONObject document=null;

        while( (document=dsExcelReader1.nextDocument())!=null)
        {


        }


    }
}
