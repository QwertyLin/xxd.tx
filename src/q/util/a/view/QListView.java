package q.util.a.view;

import android.content.Context;
import android.widget.ListView;

public class QListView {
	
	public static final void init(Context ctx, ListView lv){
		lv.setCacheColorHint(0x00000000);
		//lv.setDivider(ctx.getResources().getDrawable(R.drawable.list_divider));
		//lv.setSelector(R.drawable.list_selector);
	}

}
