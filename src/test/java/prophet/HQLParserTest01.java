package prophet;

import org.apache.hadoop.hive.ql.parse.ParseException;

import com.prophet.common.HQLParser;

public class HQLParserTest01 {
	public static void main(String[] args) {
        String parsesql = "select a from t1";
        
        HQLParser hp= new HQLParser();
		try {
			hp.parseHQL(parsesql);
		} catch (ParseException | org.antlr.runtime.NoViableAltException e) {
			System.out.println("catch");
		}
		System.out.println(hp.getOper()+"==================");
        for (String table : hp.getTables()) {
        	System.out.println(table);
        }
        
	}
}
